package org.bot.nullbot.plugin.component.delta;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.entity.game.delta.Match;
import org.bot.nullbot.entity.game.delta.Player;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.scheduling.annotation.Scheduled;

@Slf4j
@Service
public class GameService {

    // 存储在线玩家：userId -> Player
    private final Map<Long, Player> onlinePlayers = new ConcurrentHashMap<>();

    // 等待匹配的玩家列表
    private final List<Player> waitingPlayers = new ArrayList<>();

    // 进行中的游戏：matchId -> Match
    private final Map<String, Match> activeMatches = new ConcurrentHashMap<>();

    // 游戏历史：userId -> 游戏历史列表（限制最近50条）
    private final Map<Long, List<Match>> gameHistory = new ConcurrentHashMap<>();

    /**
     * 匹配玩家 - 开始和加入功能
     * 如果已有玩家在等待，则创建游戏；否则自己进入等待
     */
    public MatchResult matchPlayer(Long userId, String userName) {
        log.info("玩家 {} [{}] 请求匹配", userName, userId);

        // 更新或创建玩家信息
        Player player = getOrCreatePlayer(userId, userName);

        // 检查玩家是否已经在游戏中
        Optional<Match> existingMatch = findActiveMatchByPlayer(userId);
        if (existingMatch.isPresent()) {
            return MatchResult.error("您已经在游戏中，请先完成当前游戏");
        }

        synchronized (waitingPlayers) {
            // 查找是否有等待中的其他玩家
            Optional<Player> opponent = waitingPlayers.stream()
                    .filter(p -> !p.getUserId().equals(userId))
                    .findFirst();

            if (opponent.isPresent()) {
                // 找到对手，创建游戏对局
                Player player1 = opponent.get();

                // 从等待队列移除双方
                waitingPlayers.remove(player1);

                // 创建游戏对局
                Match match = createMatch(player1, player);

                log.info("创建游戏对局 {}: {} [{}] vs {} [{}]",
                        match.getMatchId(),
                        player1.getUserName(), player1.getUserId(),
                        player.getUserName(), player.getUserId());

                return MatchResult.success(match);

            } else {
                // 没有对手，自己加入等待队列
                if (!waitingPlayers.contains(player)) {
                    player.setStatus(Player.PlayerStatus.WAITING);
                    waitingPlayers.add(player);
                }

                log.info("玩家 {} [{}] 加入等待队列", userName, userId);

                return MatchResult.waiting(player);
            }
        }
    }

    /**
     * 取消匹配
     */
    public boolean cancelMatch(Long userId) {
        log.info("玩家 {} 取消匹配", userId);

        synchronized (waitingPlayers) {
            waitingPlayers.removeIf(p -> p.getUserId().equals(userId));
        }

        // 更新玩家状态
        Player player = onlinePlayers.get(userId);
        if (player != null) {
            player.setStatus(Player.PlayerStatus.IDLE);
            player.setLastActive(LocalDateTime.now());
        }

        return true;
    }

    /**
     * 获取玩家状态
     */
    public PlayerStatusResult getPlayerStatus(Long userId) {
        PlayerStatusResult result = new PlayerStatusResult();
        result.setUserId(userId);
        result.setTimestamp(LocalDateTime.now());

        Player player = onlinePlayers.get(userId);
        if (player == null) {
            result.setStatus("OFFLINE");
            return result;
        }

        result.setUserName(player.getUserName());
        result.setStatus(player.getStatus().name());

        switch (player.getStatus()) {
            case WAITING:
                synchronized (waitingPlayers) {
                    // 计算排队位置（前面有多少人）
                    int position = waitingPlayers.indexOf(player);
                    if (position >= 0) {
                        result.setQueuePosition(position);
                        result.setQueueSize(waitingPlayers.size());
                    }
                }
                break;

            case PLAYING:
                Optional<Match> match = findActiveMatchByPlayer(userId);
                match.ifPresent(m -> {
                    result.setMatchId(m.getMatchId());
                    result.setOpponentId(getOpponentId(m, userId));
                    result.setOpponentName(getOpponentName(m, userId));
                    result.setMatchStatus(m.getStatus().name());
                });
                break;

            case IDLE:
                // 无需额外信息
                break;
        }

        return result;
    }

    /**
     * 开始游戏（双方准备就绪）
     */
    public boolean startGame(String matchId) {
        Match match = activeMatches.get(matchId);
        if (match == null) {
            log.warn("游戏对局不存在: {}", matchId);
            return false;
        }

        match.setStatus(Match.MatchStatus.PLAYING);
        match.setStartTime(LocalDateTime.now());

        // 更新玩家状态
        updatePlayerStatus(match.getPlayer1Id(), Player.PlayerStatus.PLAYING);
        updatePlayerStatus(match.getPlayer2Id(), Player.PlayerStatus.PLAYING);

        log.info("游戏开始: {} - {} vs {}", matchId, match.getPlayer1Name(), match.getPlayer2Name());
        return true;
    }

    /**
     * 结束游戏并保存结果
     */
    public GameResult endGame(String matchId, Long winnerId, String gameData) {
        Match match = activeMatches.get(matchId);
        if (match == null) {
            log.warn("游戏对局不存在: {}", matchId);
            return GameResult.error("游戏对局不存在");
        }

        match.setStatus(Match.MatchStatus.FINISHED);
        match.setEndTime(LocalDateTime.now());
        match.setGameData(gameData);

        // 保存到历史记录
        saveToHistory(match);

        // 更新玩家状态
        updatePlayerStatus(match.getPlayer1Id(), Player.PlayerStatus.IDLE);
        updatePlayerStatus(match.getPlayer2Id(), Player.PlayerStatus.IDLE);

        // 从活跃游戏中移除
        activeMatches.remove(matchId);

        log.info("游戏结束: {} - 胜者: {}", matchId, winnerId);

        GameResult result = new GameResult();
        result.setMatchId(matchId);
        result.setWinnerId(winnerId);
        result.setPlayer1Id(match.getPlayer1Id());
        result.setPlayer1Name(match.getPlayer1Name());
        result.setPlayer2Id(match.getPlayer2Id());
        result.setPlayer2Name(match.getPlayer2Name());
        result.setEndTime(LocalDateTime.now());
        result.setGameData(gameData);

        return result;
    }

    /**
     * 获取游戏历史
     */
    public List<Match> getGameHistory(Long userId, int limit) {
        List<Match> history = gameHistory.getOrDefault(userId, new ArrayList<>());

        // 按结束时间倒序排序，取最近的记录
        return history.stream()
                .sorted((a, b) -> {
                    if (a.getEndTime() == null) return 1;
                    if (b.getEndTime() == null) return -1;
                    return b.getEndTime().compareTo(a.getEndTime());
                })
                .limit(limit)
                .toList();
    }

    /**
     * 清理过期的等待玩家（超过5分钟）
     */
    @Scheduled(fixedRate = 60000) // 每分钟执行一次
    public void cleanupExpiredPlayers() {
        LocalDateTime expireTime = LocalDateTime.now().minusMinutes(5);

        synchronized (waitingPlayers) {
            waitingPlayers.removeIf(player -> {
                if (player.getLastActive().isBefore(expireTime)) {
                    log.info("清理过期等待玩家: {} [{}]", player.getUserName(), player.getUserId());
                    player.setStatus(Player.PlayerStatus.IDLE);
                    return true;
                }
                return false;
            });
        }

        // 清理离线玩家（超过10分钟）
        LocalDateTime offlineTime = LocalDateTime.now().minusMinutes(10);
        onlinePlayers.entrySet().removeIf(entry ->
                entry.getValue().getLastActive().isBefore(offlineTime)
        );

        log.debug("清理过期玩家完成");
    }

    /**
     * 获取等待队列信息
     */
    public QueueInfo getQueueInfo() {
        QueueInfo info = new QueueInfo();
        info.setTimestamp(LocalDateTime.now());

        synchronized (waitingPlayers) {
            info.setQueueSize(waitingPlayers.size());
            info.setPlayers(waitingPlayers.stream()
                    .map(p -> new PlayerInfo(p.getUserId(), p.getUserName(), p.getLastActive()))
                    .toList());
        }

        return info;
    }

    /**
     * 获取活跃游戏列表
     */
    public List<Match> getActiveMatches() {
        return new ArrayList<>(activeMatches.values());
    }

    // ========== 私有方法 ==========

    private Player getOrCreatePlayer(Long userId, String userName) {
        Player player = onlinePlayers.get(userId);

        if (player == null) {
            player = new Player();
            player.setUserId(userId);
            player.setUserName(userName);
            player.setStatus(Player.PlayerStatus.IDLE);
            player.setLastActive(LocalDateTime.now());
            onlinePlayers.put(userId, player);
        } else {
            player.setUserName(userName); // 更新用户名
            player.setLastActive(LocalDateTime.now());
        }

        return player;
    }

    private Match createMatch(Player player1, Player player2) {
        String matchId = generateMatchId();

        Match match = new Match();
        match.setMatchId(matchId);
        match.setPlayer1Id(player1.getUserId());
        match.setPlayer1Name(player1.getUserName());
        match.setPlayer2Id(player2.getUserId());
        match.setPlayer2Name(player2.getUserName());
        match.setCreateTime(LocalDateTime.now());
        match.setStatus(Match.MatchStatus.READY);

        activeMatches.put(matchId, match);
        return match;
    }

    private Optional<Match> findActiveMatchByPlayer(Long userId) {
        return activeMatches.values().stream()
                .filter(match -> match.getPlayer1Id().equals(userId) ||
                        match.getPlayer2Id().equals(userId))
                .filter(match -> match.getStatus() != Match.MatchStatus.FINISHED)
                .findFirst();
    }

    private Long getOpponentId(Match match, Long currentUserId) {
        if (match.getPlayer1Id().equals(currentUserId)) {
            return match.getPlayer2Id();
        } else {
            return match.getPlayer1Id();
        }
    }

    private String getOpponentName(Match match, Long currentUserId) {
        if (match.getPlayer1Id().equals(currentUserId)) {
            return match.getPlayer2Name();
        } else {
            return match.getPlayer1Name();
        }
    }

    private void updatePlayerStatus(Long userId, Player.PlayerStatus status) {
        Player player = onlinePlayers.get(userId);
        if (player != null) {
            player.setStatus(status);
            player.setLastActive(LocalDateTime.now());
        }
    }

    private void saveToHistory(Match match) {
        // 为两个玩家都保存历史
        Long player1Id = match.getPlayer1Id();
        Long player2Id = match.getPlayer2Id();

        // 为玩家1保存
        List<Match> history1 = gameHistory.computeIfAbsent(player1Id, k -> new ArrayList<>());
        synchronized (history1) {
            history1.add(match.copy());
            // 限制历史记录数量
            if (history1.size() > 50) {
                history1.remove(0);
            }
        }

        // 为玩家2保存
        List<Match> history2 = gameHistory.computeIfAbsent(player2Id, k -> new ArrayList<>());
        synchronized (history2) {
            history2.add(match.copy());
            if (history2.size() > 50) {
                history2.remove(0);
            }
        }
    }

    private String generateMatchId() {
        return "MATCH-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    // ========== 返回结果类 ==========

    @Data
    public static class MatchResult {
        private boolean success;
        private String message;
        private String status; // WAITING, MATCHED
        private String matchId;
        private Long player1Id;
        private String player1Name;
        private Long player2Id;
        private String player2Name;
        private LocalDateTime createTime;

        public static MatchResult success(Match match) {
            MatchResult result = new MatchResult();
            result.setSuccess(true);
            result.setStatus("MATCHED");
            result.setMessage("匹配成功！游戏已创建");
            result.setMatchId(match.getMatchId());
            result.setPlayer1Id(match.getPlayer1Id());
            result.setPlayer1Name(match.getPlayer1Name());
            result.setPlayer2Id(match.getPlayer2Id());
            result.setPlayer2Name(match.getPlayer2Name());
            result.setCreateTime(match.getCreateTime());
            return result;
        }

        public static MatchResult waiting(Player player) {
            MatchResult result = new MatchResult();
            result.setSuccess(true);
            result.setStatus("WAITING");
            result.setMessage("等待其他玩家加入...");
            result.setPlayer1Id(player.getUserId());
            result.setPlayer1Name(player.getUserName());
            return result;
        }

        public static MatchResult error(String message) {
            MatchResult result = new MatchResult();
            result.setSuccess(false);
            result.setMessage(message);
            return result;
        }
    }

    @Data
    public static class PlayerStatusResult {
        private Long userId;
        private String userName;
        private String status; // OFFLINE, IDLE, WAITING, PLAYING
        private String matchId;
        private Long opponentId;
        private String opponentName;
        private String matchStatus;
        private Integer queuePosition;
        private Integer queueSize;
        private LocalDateTime timestamp;
    }

    @Data
    public static class QueueInfo {
        private LocalDateTime timestamp;
        private Integer queueSize;
        private List<PlayerInfo> players;
    }

    @Data
    public static class PlayerInfo {
        private final Long userId;
        private final String userName;
        private final LocalDateTime waitSince;

        public PlayerInfo(Long userId, String userName, LocalDateTime waitSince) {
            this.userId = userId;
            this.userName = userName;
            this.waitSince = waitSince;
        }
    }

    @Data
    public static class GameResult {
        private String matchId;
        private Long winnerId;
        private Long player1Id;
        private String player1Name;
        private Long player2Id;
        private String player2Name;
        private String gameData;
        private LocalDateTime endTime;

        public static GameResult error(String message) {
            GameResult result = new GameResult();
            // 这里可以添加错误信息字段
            return result;
        }
    }
}
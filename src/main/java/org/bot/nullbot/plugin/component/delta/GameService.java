package org.bot.nullbot.plugin.component.delta;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.entity.game.delta.Match;
import org.bot.nullbot.entity.game.delta.Player;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class GameService {

    // 存储所有玩家: userId -> Player
    private final Map<Long, Player> allPlayers = new ConcurrentHashMap<>();

    // 等待队列: gameType -> List<Player>
    private final Map<String, Queue<Player>> waitingQueues = new ConcurrentHashMap<>();

    // 进行中的对局: matchId -> Match
    private final Map<String, Match> activeMatches = new ConcurrentHashMap<>();

    // 已完成的對局歷史: matchId -> Match
    private final Map<String, Match> completedMatches = new ConcurrentHashMap<>();

    /**
     * 玩家匹配 - 整合开始和加入
     */
    public MatchResult matchPlayer(Long userId, String userName, String gameType) {
        log.info("玩家 {} [{}] 请求匹配，游戏类型: {}", userName, userId, gameType);

        // 获取或创建玩家
        Player player = allPlayers.computeIfAbsent(userId, id -> {
            Player p = new Player();
            p.setUserId(id);
            p.setUserName(userName);
            return p;
        });

        // 更新玩家信息
        player.setUserName(userName);
        player.setLastActive(LocalDateTime.now());

        // 检查玩家是否已经在游戏中
        if (player.getStatus() == Player.PlayerStatus.PLAYING) {
            return MatchResult.error("您已经在游戏中，请先完成当前游戏");
        }

        // 检查是否已经在等待队列中
        if (player.getStatus() == Player.PlayerStatus.WAITING) {
            return MatchResult.error("您已经在等待队列中，请耐心等待");
        }

        // 获取该游戏类型的等待队列
        Queue<Player> queue = waitingQueues.computeIfAbsent(gameType, k -> new LinkedList<>());

        synchronized (queue) {
            // 查找是否有等待中的其他玩家
            Optional<Player> opponent = queue.stream()
                    .filter(p -> !p.getUserId().equals(userId))
                    .findFirst();

            if (opponent.isPresent()) {
                // 找到对手，创建对局
                Player player1 = opponent.get();

                // 从等待队列移除双方
                queue.remove(player1);

                // 创建对局
                Match match = createMatch(player1, player, gameType);

                log.info("创建对局 {}: {} [{}] vs {} [{}]",
                        match.getMatchId(),
                        player1.getUserName(), player1.getUserId(),
                        player.getUserName(), player.getUserId());

                return MatchResult.success(match);

            } else {
                // 没有对手，加入等待队列
                player.setStatus(Player.PlayerStatus.WAITING);
                queue.add(player);

                log.info("玩家 {} [{}] 加入等待队列，游戏类型: {}", userName, userId, gameType);

                return MatchResult.waiting(player);
            }
        }
    }

    /**
     * 取消匹配
     */
    public boolean cancelMatch(Long userId, String gameType) {
        log.info("玩家 {} 取消匹配，游戏类型: {}", userId, gameType);

        Queue<Player> queue = waitingQueues.get(gameType);
        if (queue != null) {
            synchronized (queue) {
                boolean removed = queue.removeIf(p -> p.getUserId().equals(userId));
                if (removed) {
                    Player player = allPlayers.get(userId);
                    if (player != null) {
                        player.setStatus(Player.PlayerStatus.IDLE);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 获取玩家状态
     */
    public PlayerStatus getPlayerStatus(Long userId) {
        Player player = allPlayers.get(userId);
        if (player == null) {
            return PlayerStatus.notFound(userId);
        }

        PlayerStatus status = new PlayerStatus();
        status.setUserId(userId);
        status.setUserName(player.getUserName());
        status.setStatus(player.getStatus().name());
        status.setLastActive(player.getLastActive());

        // 如果在游戏中，查找对局信息
        if (player.getStatus() == Player.PlayerStatus.PLAYING) {
            Optional<Match> match = findActiveMatchByPlayer(userId);
            if (match.isPresent()) {
                status.setMatchId(match.get().getMatchId());
                status.setOpponentId(getOpponentId(match.get(), userId));
                status.setMatchStatus(match.get().getStatus().name());
            }
        }

        return status;
    }

    /**
     * 开始游戏（双方准备就绪）
     */
    public boolean startGame(String matchId) {
        Match match = activeMatches.get(matchId);
        if (match == null) {
            log.warn("对局不存在: {}", matchId);
            return false;
        }

        match.setStatus(Match.MatchStatus.PLAYING);
        match.setStartTime(LocalDateTime.now());

        // 更新玩家状态
        updatePlayerStatus(match.getPlayer1Id(), Player.PlayerStatus.PLAYING);
        updatePlayerStatus(match.getPlayer2Id(), Player.PlayerStatus.PLAYING);

        log.info("游戏开始: {} - {} vs {}",
                matchId, match.getPlayer1Id(), match.getPlayer2Id());

        return true;
    }

    /**
     * 结束游戏
     */
    public Match endGame(String matchId, String gameData) {
        Match match = activeMatches.get(matchId);
        if (match == null) {
            log.warn("对局不存在: {}", matchId);
            return null;
        }

        match.setStatus(Match.MatchStatus.FINISHED);
        match.setEndTime(LocalDateTime.now());
        match.setGameData(gameData);

        // 更新玩家状态
        updatePlayerStatus(match.getPlayer1Id(), Player.PlayerStatus.IDLE);
        updatePlayerStatus(match.getPlayer2Id(), Player.PlayerStatus.IDLE);

        // 移动到完成对局
        activeMatches.remove(matchId);
        completedMatches.put(matchId, match);

        log.info("游戏结束: {} - {} vs {}",
                matchId, match.getPlayer1Id(), match.getPlayer2Id());

        return match;
    }

    /**
     * 获取玩家历史对局
     */
    public List<Match> getPlayerHistory(Long userId, int limit) {
        return completedMatches.values().stream()
                .filter(match -> match.getPlayer1Id().equals(userId) ||
                        match.getPlayer2Id().equals(userId))
                .sorted((a, b) -> b.getEndTime().compareTo(a.getEndTime()))
                .limit(limit)
                .toList();
    }

    /**
     * 获取活跃对局列表
     */
    public List<Match> getActiveMatches() {
        return new ArrayList<>(activeMatches.values());
    }

    /**
     * 获取等待队列信息
     */
    public Map<String, Integer> getWaitingQueueInfo() {
        Map<String, Integer> info = new HashMap<>();
        waitingQueues.forEach((gameType, queue) -> {
            info.put(gameType, queue.size());
        });
        return info;
    }

    /**
     * 清理过期玩家（超过5分钟未活动）
     */
    public void cleanupExpiredPlayers() {
        LocalDateTime expireTime = LocalDateTime.now().minusMinutes(5);

        waitingQueues.forEach((gameType, queue) -> {
            synchronized (queue) {
                queue.removeIf(player ->
                        player.getLastActive() != null &&
                                player.getLastActive().isBefore(expireTime)
                );
            }
        });

        log.debug("清理过期等待玩家完成");
    }

    // ========== 私有方法 ==========

    private Match createMatch(Player player1, Player player2, String gameType) {
        Match match = new Match();
        match.setMatchId(generateMatchId());
        match.setCreateTime(LocalDateTime.now());
        match.setPlayer1Id(player1.getUserId());
        match.setPlayer2Id(player2.getUserId());
        match.setStatus(Match.MatchStatus.CREATED);

        // 游戏类型信息可以存储在gameData中，或者扩展Match类
        // 这里我们存储在gameData中作为JSON的一部分
        String gameData = String.format("{\"gameType\":\"%s\"}", gameType);
        match.setGameData(gameData);

        activeMatches.put(match.getMatchId(), match);
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

    private void updatePlayerStatus(Long userId, Player.PlayerStatus status) {
        Player player = allPlayers.get(userId);
        if (player != null) {
            player.setStatus(status);
            player.setLastActive(LocalDateTime.now());
        }
    }

    private String generateMatchId() {
        return "M" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }

    // ========== 响应类 ==========

    @Data
    public static class MatchResult {
        private boolean success;
        private String message;
        private String matchId;
        private Long player1Id;
        private Long player2Id;
        private LocalDateTime createTime;

        public static MatchResult success(Match match) {
            MatchResult result = new MatchResult();
            result.setSuccess(true);
            result.setMessage("匹配成功！对局已创建");
            result.setMatchId(match.getMatchId());
            result.setPlayer1Id(match.getPlayer1Id());
            result.setPlayer2Id(match.getPlayer2Id());
            result.setCreateTime(match.getCreateTime());
            return result;
        }

        public static MatchResult waiting(Player player) {
            MatchResult result = new MatchResult();
            result.setSuccess(true);
            result.setMessage("等待其他玩家加入...");
            result.setPlayer1Id(player.getUserId());
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
    public static class PlayerStatus {
        private Long userId;
        private String userName;
        private String status;
        private LocalDateTime lastActive;
        private String matchId;
        private Long opponentId;
        private String matchStatus;

        public static PlayerStatus notFound(Long userId) {
            PlayerStatus status = new PlayerStatus();
            status.setUserId(userId);
            status.setStatus("NOT_FOUND");
            return status;
        }
    }
}
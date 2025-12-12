package org.bot.nullbot.plugin.component.delta;

import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.entity.game.delta.Match;
import org.bot.nullbot.entity.game.delta.Player;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


@Slf4j
@Service
public class GameService
{
    // 存储玩家信息: groupId:userId -> Player
    private final Map<String, Player> players = new ConcurrentHashMap<>();

    // 存储等待匹配的玩家: groupId -> 玩家列表
    private final Map<Long, List<Player>> waitingPlayers = new ConcurrentHashMap<>();

    // 存储进行中的游戏: matchId -> Match
    private final Map<String, Match> activeMatches = new ConcurrentHashMap<>();

    // 存储已完成的游戏: matchId -> Match（历史记录）
    private final Map<String, Match> finishedMatches = new ConcurrentHashMap<>();

    // 存储玩家历史记录: groupId:userId -> matchId列表
    private final Map<String, List<String>> playerHistory = new ConcurrentHashMap<>();

    /**
     * 匹配玩家 - 整合开始和加入功能
     * 如果群组内有等待玩家，则匹配创建游戏；否则自己进入等待
     */
    public MatchResult matchPlayer(Long groupId, Long userId, String userName) {
        log.info("玩家 [{}:{}] {} 请求匹配", groupId, userId, userName);

        String playerKey = getPlayerKey(groupId, userId);

        // 检查玩家是否已经在游戏中
        Optional<Match> existingMatch = findActiveMatchByPlayer(groupId, userId);
        if (existingMatch.isPresent()) {
            return MatchResult.error("您已经在游戏中，请先完成当前游戏");
        }

        // 更新或创建玩家信息
        Player player = getOrCreatePlayer(groupId, userId, userName);
        player.setStatus(Player.PlayerStatus.WAITING);
        player.setLastActive(LocalDateTime.now());
        players.put(playerKey, player);

        // 获取该群组的等待队列
        List<Player> queue = waitingPlayers.computeIfAbsent(groupId, k -> new ArrayList<>());

        synchronized (queue) {
            // 查找是否有等待中的其他玩家（同一群组）
            Optional<Player> opponent = queue.stream()
                    .filter(p -> !p.getUserId().equals(userId))
                    .findFirst();

            if (opponent.isPresent()) {
                // 找到对手，创建游戏对局
                Player player2 = opponent.get();

                // 从等待队列移除双方
                queue.remove(player);
                queue.remove(player2);

                // 创建游戏对局
                Match match = createMatch(player, player2);

                // 更新玩家状态
                player.setStatus(Player.PlayerStatus.PLAYING);
                player.setInProgressMatchId(match.getMatchId());

                player2.setStatus(Player.PlayerStatus.PLAYING);
                player2.setInProgressMatchId(match.getMatchId());

                players.put(getPlayerKey(player), player);
                players.put(getPlayerKey(player2), player2);

                log.info("创建游戏对局 {}: {} [{}] vs {} [{}]",
                        match.getMatchId(),
                        player.getUserName(), player.getUserId(),
                        player2.getUserName(), player2.getUserId());

                return MatchResult.success(match);

            } else {
                // 没有对手，自己加入等待队列
                queue.add(player);

                log.info("玩家 {} [{}] 加入等待队列，群组: {}", userName, userId, groupId);

                return MatchResult.waiting(player);
            }
        }
    }

    /**
     * 取消匹配
     */
    public boolean cancelMatch(Long groupId, Long userId) {
        log.info("玩家 [{}:{}] 取消匹配", groupId, userId);

        List<Player> queue = waitingPlayers.get(groupId);
        if (queue != null) {
            synchronized (queue) {
                return queue.removeIf(p -> p.getUserId().equals(userId));
            }
        }
        return false;
    }

    /**
     * 获取玩家状态
     */
    public PlayerStatusResult getPlayerStatus(Long groupId, Long userId) {
        Player player = players.get(getPlayerKey(groupId, userId));

        PlayerStatusResult result = new PlayerStatusResult();
        result.setGroupId(groupId);
        result.setUserId(userId);
        result.setTimestamp(LocalDateTime.now());

        if (player == null) {
            result.setStatus("NOT_FOUND");
            return result;
        }

        result.setStatus(player.getStatus().name());
        result.setUserName(player.getUserName());

        if (player.getStatus() == Player.PlayerStatus.WAITING) {
            // 获取等待队列信息
            List<Player> queue = waitingPlayers.get(groupId);
            if (queue != null) {
                result.setQueueSize(queue.size() - 1); // 减去自己
            }
        } else if (player.getStatus() == Player.PlayerStatus.PLAYING) {
            // 获取游戏信息
            Optional<Match> match = findActiveMatchByPlayer(groupId, userId);
            if (match.isPresent()) {
                result.setMatchId(match.get().getMatchId());
                result.setMatchStatus(match.get().getStatus().name());
                result.setOpponentId(getOpponentId(match.get(), groupId, userId));
            }
        }

        return result;
    }

    /**
     * 获取群组等待队列信息
     */
    public QueueInfoResult getQueueInfo(Long groupId) {
        List<Player> queue = waitingPlayers.getOrDefault(groupId, new ArrayList<>());

        QueueInfoResult result = new QueueInfoResult();
        result.setGroupId(groupId);
        result.setQueueSize(queue.size());
        result.setWaitingPlayers(queue.stream()
                .map(p -> p.getUserName() + "[" + p.getUserId() + "]")
                .toList());
        result.setTimestamp(LocalDateTime.now());

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

        log.info("游戏开始: {} - {} vs {}", matchId, match.getPlayer1Name(), match.getPlayer2Name());
        return true;
    }

    /**
     * 结束游戏
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

        // 更新玩家状态
        updatePlayersAfterGame(match);

        // 移动到历史记录
        finishedMatches.put(matchId, match);
        activeMatches.remove(matchId);

        // 添加到玩家历史
        addToPlayerHistory(match);

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
     * 取消游戏
     */
    public boolean cancelGame(String matchId) {
        Match match = activeMatches.get(matchId);
        if (match == null) {
            return false;
        }

        match.setStatus(Match.MatchStatus.CANCELLED);
        match.setEndTime(LocalDateTime.now());

        // 更新玩家状态
        updatePlayersAfterGame(match);

        // 移动到历史记录
        finishedMatches.put(matchId, match);
        activeMatches.remove(matchId);

        log.info("游戏取消: {}", matchId);
        return true;
    }

    /**
     * 获取游戏信息
     */
    public Match getMatch(String matchId) {
        // 先查活跃游戏，再查历史记录
        Match match = activeMatches.get(matchId);
        if (match == null) {
            match = finishedMatches.get(matchId);
        }
        return match;
    }

    /**
     * 获取玩家游戏历史
     */
    public List<Match> getPlayerHistory(Long groupId, Long userId, int limit) {
        String playerKey = getPlayerKey(groupId, userId);
        List<String> matchIds = playerHistory.getOrDefault(playerKey, new ArrayList<>());

        // 获取所有历史对局
        return matchIds.stream()
                .map(this::getMatch)
                .filter(Objects::nonNull)
                .sorted((a, b) -> b.getEndTime().compareTo(a.getEndTime()))
                .limit(limit)
                .toList();
    }

    /**
     * 清理过期的等待玩家（超过5分钟）
     */
    @Scheduled(fixedRate = 60000) // 每分钟执行一次
    public void cleanupExpiredPlayers() {
        LocalDateTime expireTime = LocalDateTime.now().minusMinutes(5);

        waitingPlayers.forEach((groupId, playersList) -> {
            synchronized (playersList) {
                playersList.removeIf(player ->
                        player.getLastActive().isBefore(expireTime)
                );
            }
        });

        log.debug("清理过期等待玩家完成");
    }

    /**
     * 获取所有活跃游戏（管理用）
     */
    public List<Match> getActiveMatches() {
        return new ArrayList<>(activeMatches.values());
    }

    // ========== 私有方法 ==========

    private Player getOrCreatePlayer(Long groupId, Long userId, String userName) {
        String playerKey = getPlayerKey(groupId, userId);
        Player player = players.get(playerKey);

        if (player == null) {
            player = new Player();
            player.setGroupId(groupId);
            player.setUserId(userId);
            player.setUserName(userName);
        }

        return player;
    }

    private Match createMatch(Player player1, Player player2) {
        String matchId = "MATCH-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Match match = new Match();
        match.setMatchId(matchId);
        match.setCreateTime(LocalDateTime.now());
        match.setStatus(Match.MatchStatus.READY);

        // 玩家1信息
        match.setGroup1Id(player1.getGroupId());
        match.setPlayer1Id(player1.getUserId());
        match.setPlayer1Name(player1.getUserName());

        // 玩家2信息
        match.setGroup2Id(player2.getGroupId());
        match.setPlayer2Id(player2.getUserId());
        match.setPlayer2Name(player2.getUserName());

        activeMatches.put(matchId, match);

        startGame(matchId);  // 自动开始对局
        return match;
    }

    private Optional<Match> findActiveMatchByPlayer(Long groupId, Long userId) {
        return activeMatches.values().stream()
                .filter(match -> (match.getGroup1Id().equals(groupId) && match.getPlayer1Id().equals(userId)) ||
                        (match.getGroup2Id().equals(groupId) && match.getPlayer2Id().equals(userId)))
                .filter(match -> match.getStatus() != Match.MatchStatus.FINISHED &&
                        match.getStatus() != Match.MatchStatus.CANCELLED)
                .findFirst();
    }

    private Long getOpponentId(Match match, Long groupId, Long userId) {
        if (match.getGroup1Id().equals(groupId) && match.getPlayer1Id().equals(userId)) {
            return match.getPlayer2Id();
        } else {
            return match.getPlayer1Id();
        }
    }

    private void updatePlayersAfterGame(Match match) {
        // 更新玩家1状态
        String player1Key = getPlayerKey(match.getGroup1Id(), match.getPlayer1Id());
        Player player1 = players.get(player1Key);
        if (player1 != null) {
            player1.setStatus(Player.PlayerStatus.IDLE);
            player1.setInProgressMatchId(null);
            player1.setLastActive(LocalDateTime.now());
        }

        // 更新玩家2状态
        String player2Key = getPlayerKey(match.getGroup2Id(), match.getPlayer2Id());
        Player player2 = players.get(player2Key);
        if (player2 != null) {
            player2.setStatus(Player.PlayerStatus.IDLE);
            player2.setInProgressMatchId(null);
            player2.setLastActive(LocalDateTime.now());
        }
    }

    private void addToPlayerHistory(Match match) {
        // 添加到玩家1历史
        String player1Key = getPlayerKey(match.getGroup1Id(), match.getPlayer1Id());
        List<String> history1 = playerHistory.computeIfAbsent(player1Key, k -> new ArrayList<>());
        synchronized (history1) {
            history1.add(match.getMatchId());
            if (history1.size() > 50) {
                history1.remove(0);
            }
        }

        // 添加到玩家2历史
        String player2Key = getPlayerKey(match.getGroup2Id(), match.getPlayer2Id());
        List<String> history2 = playerHistory.computeIfAbsent(player2Key, k -> new ArrayList<>());
        synchronized (history2) {
            history2.add(match.getMatchId());
            if (history2.size() > 50) {
                history2.remove(0);
            }
        }
    }

    private String getPlayerKey(Long groupId, Long userId) {
        return groupId + ":" + userId;
    }

    private String getPlayerKey(Player player) {
        return getPlayerKey(player.getGroupId(), player.getUserId());
    }

    // ========== 返回结果类 ==========

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

        // getters and setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getMatchId() { return matchId; }
        public void setMatchId(String matchId) { this.matchId = matchId; }
        public Long getPlayer1Id() { return player1Id; }
        public void setPlayer1Id(Long player1Id) { this.player1Id = player1Id; }
        public String getPlayer1Name() { return player1Name; }
        public void setPlayer1Name(String player1Name) { this.player1Name = player1Name; }
        public Long getPlayer2Id() { return player2Id; }
        public void setPlayer2Id(Long player2Id) { this.player2Id = player2Id; }
        public String getPlayer2Name() { return player2Name; }
        public void setPlayer2Name(String player2Name) { this.player2Name = player2Name; }
        public LocalDateTime getCreateTime() { return createTime; }
        public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    }

    public static class PlayerStatusResult {
        private Long groupId;
        private Long userId;
        private String userName;
        private String status;
        private String matchId;
        private String matchStatus;
        private Long opponentId;
        private int queueSize;
        private LocalDateTime timestamp;

        // getters and setters
        public Long getGroupId() { return groupId; }
        public void setGroupId(Long groupId) { this.groupId = groupId; }
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public String getUserName() { return userName; }
        public void setUserName(String userName) { this.userName = userName; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getMatchId() { return matchId; }
        public void setMatchId(String matchId) { this.matchId = matchId; }
        public String getMatchStatus() { return matchStatus; }
        public void setMatchStatus(String matchStatus) { this.matchStatus = matchStatus; }
        public Long getOpponentId() { return opponentId; }
        public void setOpponentId(Long opponentId) { this.opponentId = opponentId; }
        public int getQueueSize() { return queueSize; }
        public void setQueueSize(int queueSize) { this.queueSize = queueSize; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    }

    public static class QueueInfoResult {
        private Long groupId;
        private int queueSize;
        private List<String> waitingPlayers;
        private LocalDateTime timestamp;

        // getters and setters
        public Long getGroupId() { return groupId; }
        public void setGroupId(Long groupId) { this.groupId = groupId; }
        public int getQueueSize() { return queueSize; }
        public void setQueueSize(int queueSize) { this.queueSize = queueSize; }
        public List<String> getWaitingPlayers() { return waitingPlayers; }
        public void setWaitingPlayers(List<String> waitingPlayers) { this.waitingPlayers = waitingPlayers; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    }

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

        // getters and setters
        public String getMatchId() { return matchId; }
        public void setMatchId(String matchId) { this.matchId = matchId; }
        public Long getWinnerId() { return winnerId; }
        public void setWinnerId(Long winnerId) { this.winnerId = winnerId; }
        public Long getPlayer1Id() { return player1Id; }
        public void setPlayer1Id(Long player1Id) { this.player1Id = player1Id; }
        public String getPlayer1Name() { return player1Name; }
        public void setPlayer1Name(String player1Name) { this.player1Name = player1Name; }
        public Long getPlayer2Id() { return player2Id; }
        public void setPlayer2Id(Long player2Id) { this.player2Id = player2Id; }
        public String getPlayer2Name() { return player2Name; }
        public void setPlayer2Name(String player2Name) { this.player2Name = player2Name; }
        public String getGameData() { return gameData; }
        public void setGameData(String gameData) { this.gameData = gameData; }
        public LocalDateTime getEndTime() { return endTime; }
        public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    }
}
package com.zincoid.nullbot.bot.interactor;

import com.mikuac.shiro.core.Bot;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import com.zincoid.nullbot.core.module.control.BotInputManager;
import com.zincoid.nullbot.core.enums.BniMode;
import com.zincoid.nullbot.core.functional.BotGroupEntityConsumer;

import java.util.List;
import java.util.stream.IntStream;

@Slf4j
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BotPageSelector<K, V> {

    private static final int DEFAULT_INPUT_TIMEOUT = 30;
    private static final String INPUT_REGEX = "[1-9]\\d*|(?i)up|down|end";

    private final Bot bot;
    private final Long groupId;
    private final Long userId;
    private final String title;
    private final String info;
    private final boolean continuous;

    private final List<K> keys;
    private final List<V> values;
    private final BotGroupEntityConsumer<K> action;

    private final int total;
    private final int size;
    private final int pages;

    private int current;

    public static <K, V> Builder<K, V> builder(
            Bot bot, Long groupId,
            String title, boolean continuous,
            List<K> keys, List<V> values,
            BotGroupEntityConsumer<K> action
    ) {
        return new Builder<>(bot, groupId, title, continuous, keys, values, action);
    }

    @RequiredArgsConstructor
    public static class Builder<K, V> {

        private final Bot bot;
        private final Long groupId;
        private final String title;
        private final boolean continuous;
        private final List<K> keys;
        private final List<V> values;
        private final BotGroupEntityConsumer<K> action;

        private Long userId;
        private String info = "";
        private int size = 10;
        private int current = 1;

        public Builder<K, V> userId(Long userId) {
            this.userId = userId;
            return this;
        }
        public Builder<K, V> info(String info) {
            this.info = info;
            return this;
        }
        public Builder<K, V> size(int size) {
            this.size = size;
            return this;
        }
        public Builder<K, V> current(int current) {
            this.current = current;
            return this;
        }

        public BotPageSelector<K, V> build() {
            if (keys.size() != values.size())
                throw new IllegalArgumentException("键值大小不匹配");
            int total = keys.size();
            int pages = (total + size - 1) / size;
            int current = Math.max(1, Math.min(this.current, pages));
            return new BotPageSelector<>(
                    bot, groupId, userId, title, info, continuous,
                    keys, values, action,
                    total, size, pages, current
            );
        }
    }

    // =================== BotInputer 控制方案 ====================

    public void start(BotInputer inputer) {
        init();
        while (input(inputer)) {
            log.info("▽ [BotPageSelector] 已操作{}分页器", title);
        }
    }

    public boolean input(BotInputer inputer) {
        inputer.setMode(BniMode.PS);
        inputer.setPattern(INPUT_REGEX);
        inputer.setCoverable(false);
        List<Pair<Long, String>> inputs = inputer.next();
        if (inputs.isEmpty()) return end();
        return input(inputs.getFirst().getRight().toUpperCase());
    }

    // ================= BotInputManager 控制方案 =================

    public void start(BotInputManager manager) {
        init();
        while (input(manager)) {
            log.info("▽ [BotPageSelector] 已操作{}分页器", title);
        }
    }

    public boolean input(BotInputManager manager) {
        return input(manager, userId, DEFAULT_INPUT_TIMEOUT);
    }

    public boolean input(BotInputManager manager, int timeout) {
        if (userId == null)
            throw new NullPointerException("选择器未设置用户信息");
        return input(manager, userId, timeout);
    }

    public boolean input(BotInputManager manager, Long userId) {
        return input(manager, userId, DEFAULT_INPUT_TIMEOUT);
    }

    public boolean input(BotInputManager manager, Long userId, int timeout) {
        List<Pair<Long, String>> inputs = manager
                .request(BniMode.PS, userId, INPUT_REGEX, timeout);
        if (inputs.isEmpty()) return end();
        return input(inputs.getFirst().getRight().toUpperCase());
    }

    // ======================= 直接控制方案 =======================

    public boolean input(String cmd) {
        return switch (cmd.toUpperCase()) {
            case "INIT" -> init();
            case "UP" -> prev();
            case "DOWN" -> next();
            case "END" -> end();
            default -> select(Integer.parseInt(cmd));
        };
    }

    public boolean init() {
        return page();
    }

    // ======================= 内部操作工具 =======================

    private boolean next() {
        if (current < pages) {
            current++;
            return page();
        }
        bot.sendGroupMsg(groupId, "\uD83D\uDCA6到底啦", false);
        return true;
    }

    private boolean prev() {
        if (current > 1) {
            current--;
            return page();
        }
        bot.sendGroupMsg(groupId, "\uD83D\uDCA6到顶啦", false);
        return true;
    }

    private boolean end() {
        bot.sendGroupMsg(groupId, "⛔️查询结束", false);
        return false;
    }

    private boolean page() {
        int from = (current - 1) * size;
        int to = Math.min(from + size, total);
        List<String> lines = IntStream.range(from, to)
                .mapToObj(i -> (i + 1) + ". " + values.get(i).toString())
                .toList();
        String content = String.join("\n", lines);
        String footer = """
                [第 %s/%s 页 (每页%s条)]%s
                操作 - Up/Down/End
                选择 - 发送序号 (上同)""".formatted(current, pages, size, info);
        bot.sendGroupMsg(groupId, "[%s] \uD83D\uDD0D共%s个结果\n%s\n\n%s"
                .formatted(title, total, content, footer), true);
        return true;
    }

    private boolean select(int i) {
        i = Math.max(1, Math.min(i, total));
        action.accept(bot, groupId, keys.get(i - 1));
        return continuous;
    }
}

package org.bot.nullbot.entity;

import com.mikuac.shiro.core.Bot;
import org.apache.commons.lang3.tuple.Pair;
import org.bot.nullbot.component.control.BotNextInputer;
import org.bot.nullbot.enums.BniMode;
import org.bot.nullbot.exception.NullBotMsgException;
import org.bot.nullbot.function.BotFunction;

import java.util.List;
import java.util.stream.IntStream;

public class BotPageSelector<K, V> {

    private static final int DEFAULT_INPUTER_TIMEOUT = 30;

    private final Bot bot;
    private final Long groupId;
    private final Long userId;
    private final String title;
    private final String info;
    private final boolean continuous;

    private final List<K> keys;
    private final List<V> values;
    private final BotFunction<Bot, Long, K, Void> action;

    private final int total;
    private final int size;
    private final int pages;

    private int current;

    private BotPageSelector(Builder<K, V> builder) {
        if (builder.keys.size() != builder.values.size())
            throw new IllegalArgumentException("键值大小不匹配");
        this.bot = builder.bot;
        this.groupId = builder.groupId;
        this.userId = builder.userId;
        this.title = builder.title;
        this.info = builder.info;
        this.continuous = builder.continuous;
        this.keys = builder.keys;
        this.values = builder.values;
        this.action = builder.action;
        this.total = builder.keys.size();
        this.size = builder.size;
        this.pages = (total + builder.size - 1) / builder.size;
        this.current = Math.max(1, Math.min(builder.current, pages));
    }

    public static class Builder<K, V> {
        private final Bot bot;
        private final Long groupId;
        private final String title;
        private final boolean continuous;
        private final List<K> keys;
        private final List<V> values;
        private final BotFunction<Bot, Long, K, Void> action;

        private Long userId;
        private String info = "";
        private int size = 10;
        private int current = 1;

        public Builder(Bot bot, Long groupId,
                       String title, boolean continuous,
                       List<K> keys, List<V> values,
                       BotFunction<Bot, Long, K, Void> action) {
            this.bot = bot;
            this.groupId = groupId;
            this.title = title;
            this.continuous = continuous;
            this.keys = keys;
            this.values = values;
            this.action = action;
        }

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
            return new BotPageSelector<>(this);
        }
    }

    public boolean input(BotNextInputer inputer) {
        return input(inputer, userId, DEFAULT_INPUTER_TIMEOUT);
    }

    public boolean input(BotNextInputer inputer, int timeout) {
        if (userId == null)
            throw new NullPointerException("BotPageSelector未指定UserId");
        return input(inputer, userId, timeout);
    }

    public boolean input(BotNextInputer inputer, Long userId, int timeout) {
        List<Pair<Long, String>> inputs = inputer
                .request(BniMode.PS, userId, "[1-9]\\d*|(?i)up|down|end", timeout);
        if (inputs.isEmpty())
            throw new NullBotMsgException("[%s] ⌛️输入超时".formatted(title));
        return input(inputs.getFirst().getRight().toUpperCase());
    }

    public boolean input(String cmd) {
        return switch (cmd.toUpperCase()) {
            case "INIT" -> init();
            case "UP" -> prev();
            case "DOWN" -> next();
            case "END" -> end();
            default -> {
                try {
                    yield select(Integer.parseInt(cmd));
                } catch (NumberFormatException e) {
                    bot.sendGroupMsg(groupId, "[%s] ❌格式错误".formatted(title), false);
                    yield true;
                }
            }
        };
    }

    public boolean init() {
        return page();
    }

    private boolean next() {
        if (current < pages) {
            current++;
            return page();
        }
        bot.sendGroupMsg(groupId, "到底啦！", false);
        return true;
    }

    private boolean prev() {
        if (current > 1) {
            current--;
            return page();
        }
        bot.sendGroupMsg(groupId, "到顶啦！", false);
        return true;
    }

    private boolean end() {
        bot.sendGroupMsg(groupId, "[%s] ⛔️查询终止".formatted(title), false);
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
                .formatted(title, total, content, footer), false);
        return true;
    }

    private boolean select(int i) {
        if (i < 1 || i > total)
            throw new IllegalArgumentException("索引超出范围");
        action.apply(bot, groupId, keys.get(i - 1));
        return continuous;
    }
}

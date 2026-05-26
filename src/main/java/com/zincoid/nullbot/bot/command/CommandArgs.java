package com.zincoid.nullbot.bot.command;

import com.zincoid.nullbot.bot.exception.NullBotException;

import java.util.List;

public final class CommandArgs {

    private final List<String> params;
    private int cursor;

    public CommandArgs(List<String> params) {
        this.params = params;
        this.cursor = 0;
    }

    // ── utility ──────────────────────────────────

    public int size() { return params.size(); }
    public boolean isEmpty() { return params.isEmpty(); }
    public boolean hasNext() { return cursor < params.size(); }
    public String peek() { return getString(cursor); }

    // ── iterator-style access ──────────────────────

    public String nextString() {
        String v = getString(cursor);
        cursor++;
        return v;
    }

    public String nextFullString() {
        if (cursor >= params.size()) throw missingArg();
        return String.join(" ", params.subList(cursor, params.size()));
    }

    public int nextInt() {
        String v = nextString();
        try { return Integer.parseInt(v); }
        catch (NumberFormatException e) { throw formatError(); }
    }

    public long nextLong() {
        String v = nextString();
        try { return Long.parseLong(v); }
        catch (NumberFormatException e) { throw formatError(); }
    }

    public double nextDouble() {
        String v = nextString();
        try { return Double.parseDouble(v); }
        catch (NumberFormatException e) { throw formatError(); }
    }

    public String nextString(String defaultVal) {
        if (!hasNext()) return defaultVal;
        return params.get(cursor++);
    }

    public String nextFullString(String defaultVal) {
        if (!hasNext()) return defaultVal;
        return String.join(" ", params.subList(cursor, params.size()));
    }

    public int nextInt(int defaultVal) {
        if (!hasNext()) return defaultVal;
        try { return Integer.parseInt(params.get(cursor++)); }
        catch (NumberFormatException e) { throw formatError(); }
    }

    public long nextLong(long defaultVal) {
        if (!hasNext()) return defaultVal;
        try { return Long.parseLong(params.get(cursor++)); }
        catch (NumberFormatException e) { throw formatError(); }
    }

    public double nextDouble(double defaultVal) {
        if (!hasNext()) return defaultVal;
        try { return Double.parseDouble(params.get(cursor++)); }
        catch (NumberFormatException e) { throw formatError(); }
    }

    // ── indexed access (non-advancing) ─────────────

    public String getString(int index) {
        if (index >= params.size()) throw missingArg();
        return params.get(index);
    }

    public String getFullString(int index) {
        if (index >= params.size()) throw missingArg();
        return String.join(" ", params.subList(index, params.size()));
    }

    public int getInt(int index) {
        String v = getString(index);
        try { return Integer.parseInt(v); }
        catch (NumberFormatException e) { throw formatError(); }
    }

    public long getLong(int index) {
        String v = getString(index);
        try { return Long.parseLong(v); }
        catch (NumberFormatException e) { throw formatError(); }
    }

    public double getDouble(int index) {
        String v = getString(index);
        try { return Double.parseDouble(v); }
        catch (NumberFormatException e) { throw formatError(); }
    }

    // ── private helpers ────────────────────────────

    private NullBotException missingArg() {
        return new NullBotException("参数不足");
    }

    private NullBotException formatError() {
        return new NullBotException("参数错误");
    }
}

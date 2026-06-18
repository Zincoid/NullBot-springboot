package com.zincoid.nullbot.bot.command;

import com.zincoid.nullbot.bot.exception.BotWarnException;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@RequiredArgsConstructor(staticName = "of")
public final class CmdArgs {

    private final List<String> params;
    private int cursor = 0;

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

    public String getString(int index, String defaultVal) {
        if (index >= params.size()) return defaultVal;
        return params.get(index);
    }

    public String getFullString(int index, String defaultVal) {
        if (index >= params.size()) return defaultVal;
        return String.join(" ", params.subList(index, params.size()));
    }

    public int getInt(int index, int defaultVal) {
        if (index >= params.size()) return defaultVal;
        try { return Integer.parseInt(params.get(index)); }
        catch (NumberFormatException e) { throw formatError(); }
    }

    public long getLong(int index, long defaultVal) {
        if (index >= params.size()) return defaultVal;
        try { return Long.parseLong(params.get(index)); }
        catch (NumberFormatException e) { throw formatError(); }
    }

    public double getDouble(int index, double defaultVal) {
        if (index >= params.size()) return defaultVal;
        try { return Double.parseDouble(params.get(index)); }
        catch (NumberFormatException e) { throw formatError(); }
    }

    // ── named option support (--xxx / -x) ──────────

    public boolean hasOpt(String longName, String shortName) {
        for (String p : params) {
            if (p.equals("--" + longName)) return true;
            if (shortName != null && p.equals("-" + shortName)) return true;
        }
        return false;
    }

    public String getOptString(String longName, String shortName) {
        String val = getOptStringOrNull(longName, shortName);
        if (val == null) throw missingOpt(longName, shortName);
        return val;
    }

    public String getOptString(String longName, String shortName, String defaultVal) {
        String val = getOptStringOrNull(longName, shortName);
        return val != null ? val : defaultVal;
    }

    public String getOptFullString(String longName, String shortName) {
        String val = getOptFullStringOrNull(longName, shortName);
        if (val == null) throw missingOpt(longName, shortName);
        return val;
    }

    public String getOptFullString(String longName, String shortName, String defaultVal) {
        String val = getOptFullStringOrNull(longName, shortName);
        return val != null ? val : defaultVal;
    }

    public int getOptInt(String longName, String shortName, int defaultVal) {
        String val = getOptStringOrNull(longName, shortName);
        if (val == null || val.isEmpty()) return defaultVal;
        try { return Integer.parseInt(val); }
        catch (NumberFormatException e) { throw formatError(); }
    }

    public long getOptLong(String longName, String shortName, long defaultVal) {
        String val = getOptStringOrNull(longName, shortName);
        if (val == null || val.isEmpty()) return defaultVal;
        try { return Long.parseLong(val); }
        catch (NumberFormatException e) { throw formatError(); }
    }

    public double getOptDouble(String longName, String shortName, double defaultVal) {
        String val = getOptStringOrNull(longName, shortName);
        if (val == null || val.isEmpty()) return defaultVal;
        try { return Double.parseDouble(val); }
        catch (NumberFormatException e) { throw formatError(); }
    }

    // ── internal: returns null on not-found ─────────

    private String getOptStringOrNull(String longName, String shortName) {
        for (int i = 0; i < params.size(); i++) {
            String p = params.get(i);
            if (p.equals("--" + longName) || (shortName != null && p.equals("-" + shortName))) {
                if (i + 1 < params.size() && !params.get(i + 1).startsWith("-"))
                    return params.get(i + 1);
                return "";
            }
        }
        return null;
    }

    private String getOptFullStringOrNull(String longName, String shortName) {
        for (int i = 0; i < params.size(); i++) {
            String p = params.get(i);
            if (p.equals("--" + longName) || (shortName != null && p.equals("-" + shortName))) {
                return String.join(" ", params.subList(i + 1, params.size()));
            }
        }
        return null;
    }

    // ── private helpers ────────────────────────────

    private BotWarnException missingArg() {
        return new BotWarnException("参数不足");
    }

    private BotWarnException formatError() {
        return new BotWarnException("参数错误");
    }

    private BotWarnException missingOpt(String longName, String shortName) {
        return new BotWarnException("参数缺失: --%s / -%s".formatted(longName, shortName));
    }
}

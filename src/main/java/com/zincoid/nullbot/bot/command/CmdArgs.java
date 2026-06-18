package com.zincoid.nullbot.bot.command;

import com.zincoid.nullbot.bot.exception.BotWarnException;
import lombok.Getter;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.*;

@NullMarked
@Getter
@SuppressWarnings("unused")
public final class CmdArgs {

    private final List<String> raw;
    private final Map<String, String> opt;
    private final List<String> pos;
    private int cursor;

    private CmdArgs(List<String> raw, Map<String, String> opt, List<String> pos) {
        this.raw = List.copyOf(raw);
        this.opt = Collections.unmodifiableMap(opt);
        this.pos = Collections.unmodifiableList(pos);
    }

    /**
     * Parse raw params into options and positional args.
     * Options (--flag, -f, --key=value, -k=value) are extracted and removed from positional.
     */
    public static CmdArgs of(List<String> params) {
        if (params.isEmpty())
            return new CmdArgs(List.of(), Map.of(), List.of());
        Map<String, String> opt = new LinkedHashMap<>();
        List<String> pos = new ArrayList<>();
        for (String p : params) if (!tryParseOpt(p, opt)) pos.add(p);
        return new CmdArgs(params, opt, pos);
    }

    // ── Option access ──────────────────────────────────

    public boolean hasOpt(String name, @Nullable String alias) {
        return opt.containsKey(name) ||
                alias != null && opt.containsKey(alias);
    }

    public String getOpt(String name, @Nullable String alias) {
        String v = opt.get(name);
        if (v != null) return v;
        if (alias != null) {
            v = opt.get(alias);
            if (v != null) return v;
        }
        throw missingOpt(name, alias);
    }

    public @Nullable String getOpt(String name, @Nullable String alias, @Nullable String or) {
        String v = opt.get(name);
        if (v != null) return v;
        if (alias != null) {
            v = opt.get(alias);
            if (v != null) return v;
        }
        return or;
    }

    public int optInt(String name, @Nullable String alias) {
        try { return Integer.parseInt(getOpt(name, alias)); }
        catch (NumberFormatException e) { throw formatError(); }
    }

    public int optInt(String name, @Nullable String alias, int or) {
        String v = getOpt(name, alias, null);
        if (v == null) return or;
        try { return Integer.parseInt(v); }
        catch (NumberFormatException e) { throw formatError(); }
    }

    public long optLong(String name, @Nullable String alias) {
        try { return Long.parseLong(getOpt(name, alias)); }
        catch (NumberFormatException e) { throw formatError(); }
    }

    public long optLong(String name, @Nullable String alias, long or) {
        String v = getOpt(name, alias, null);
        if (v == null) return or;
        try { return Long.parseLong(v); }
        catch (NumberFormatException e) { throw formatError(); }
    }

    public double optDouble(String name, @Nullable String alias) {
        try { return Double.parseDouble(getOpt(name, alias)); }
        catch (NumberFormatException e) { throw formatError(); }
    }

    public double optDouble(String name, @Nullable String alias, double or) {
        String v = getOpt(name, alias, null);
        if (v == null) return or;
        try { return Double.parseDouble(v); }
        catch (NumberFormatException e) { throw formatError(); }
    }

    // ── Positional access (iterator) ───────────────────

    public boolean hasNext() { return cursor < pos.size(); }

    public int size() { return pos.size(); }

    public boolean isEmpty() { return pos.isEmpty(); }

    public String peek() { return get(cursor); }

    public String next() {
        String v = get(cursor);
        cursor++;
        return v;
    }

    public String next(String or) {
        if (!hasNext()) return or;
        return pos.get(cursor++);
    }

    public String rest() {
        if (cursor >= pos.size()) throw missingArg();
        String result = String.join(" ", pos.subList(cursor, pos.size()));
        cursor = pos.size();
        return result;
    }

    public String rest(String or) {
        if (cursor >= pos.size()) return or;
        String result = String.join(" ", pos.subList(cursor, pos.size()));
        cursor = pos.size();
        return result;
    }

    public int nextInt() {
        String v = next();
        try { return Integer.parseInt(v); }
        catch (NumberFormatException e) { throw formatError(); }
    }

    public int nextInt(int or) {
        if (!hasNext()) return or;
        try { return Integer.parseInt(pos.get(cursor++)); }
        catch (NumberFormatException e) { throw formatError(); }
    }

    public long nextLong() {
        String v = next();
        try { return Long.parseLong(v); }
        catch (NumberFormatException e) { throw formatError(); }
    }

    public long nextLong(long or) {
        if (!hasNext()) return or;
        try { return Long.parseLong(pos.get(cursor++)); }
        catch (NumberFormatException e) { throw formatError(); }
    }

    public double nextDouble() {
        String v = next();
        try { return Double.parseDouble(v); }
        catch (NumberFormatException e) { throw formatError(); }
    }

    public double nextDouble(double or) {
        if (!hasNext()) return or;
        try { return Double.parseDouble(pos.get(cursor++)); }
        catch (NumberFormatException e) { throw formatError(); }
    }

    // ── Positional access (indexed) ────────────────────

    public String get(int index) {
        if (index >= pos.size()) throw missingArg();
        return pos.get(index);
    }

    public String get(int index, String or) {
        return index < pos.size() ? pos.get(index) : or;
    }

    public String rest(int index) {
        if (index >= pos.size()) throw missingArg();
        return String.join(" ", pos.subList(index, pos.size()));
    }

    public String rest(int index, String or) {
        if (index >= pos.size()) return or;
        return String.join(" ", pos.subList(index, pos.size()));
    }

    public int getInt(int index) {
        try { return Integer.parseInt(get(index)); }
        catch (NumberFormatException e) { throw formatError(); }
    }

    public int getInt(int index, int or) {
        if (index >= pos.size()) return or;
        try { return Integer.parseInt(pos.get(index)); }
        catch (NumberFormatException e) { throw formatError(); }
    }

    public long getLong(int index) {
        try { return Long.parseLong(get(index)); }
        catch (NumberFormatException e) { throw formatError(); }
    }

    public long getLong(int index, long or) {
        if (index >= pos.size()) return or;
        try { return Long.parseLong(pos.get(index)); }
        catch (NumberFormatException e) { throw formatError(); }
    }

    public double getDouble(int index) {
        try { return Double.parseDouble(get(index)); }
        catch (NumberFormatException e) { throw formatError(); }
    }

    public double getDouble(int index, double or) {
        if (index >= pos.size()) return or;
        try { return Double.parseDouble(pos.get(index)); }
        catch (NumberFormatException e) { throw formatError(); }
    }

    // ── Private helpers ─────────────────────────────────

    private static BotWarnException missingArg() {
        return new BotWarnException("参数不足");
    }

    private static BotWarnException formatError() {
        return new BotWarnException("参数错误");
    }

    private static BotWarnException missingOpt(String name, @Nullable String alias) {
        return alias != null
                ? new BotWarnException("参数缺失: --%s / -%s".formatted(name, alias))
                : new BotWarnException("参数缺失: --%s".formatted(name));
    }

    private static boolean tryParseOpt(String p, Map<String, String> opt) {
        if (p.startsWith("--")) {
            String body = p.substring(2);
            if (body.isEmpty() || body.startsWith("-")) return false;
            int eq = body.indexOf('=');
            if (eq >= 0) {
                String name = body.substring(0, eq);
                if (!name.isEmpty()) { opt.put(name, body.substring(eq + 1)); return true; }
            } else {
                opt.put(body, "true");
                return true;
            }
        } else if (p.startsWith("-") && p.length() >= 2 && Character.isLetter(p.charAt(1))) {
            String name = p.substring(1, 2);
            if (p.length() > 2 && p.charAt(2) == '=') {
                opt.put(name, p.substring(3));
                return true;
            }
            if (p.length() == 2) {
                opt.put(name, "true");
                return true;
            }
        }
        return false;
    }
}

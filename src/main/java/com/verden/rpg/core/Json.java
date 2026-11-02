package com.verden.rpg.core;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * JSON mínimo e sem dependências (Map/List/String/Long/Double/Boolean/null).
 * Existe para que o núcleo do RPG seja Java puro, testável fora do Minecraft.
 * A ordem das chaves é preservada (LinkedHashMap), o que torna a saída determinística
 * — requisito do checksum do salvamento.
 */
public final class Json {
    private Json() {}

    public static String write(Object o) {
        StringBuilder sb = new StringBuilder();
        w(sb, o);
        return sb.toString();
    }

    private static void w(StringBuilder sb, Object o) {
        if (o == null) {
            sb.append("null");
        } else if (o instanceof String s) {
            str(sb, s);
        } else if (o instanceof Boolean || o instanceof Long || o instanceof Integer || o instanceof Short || o instanceof Byte) {
            sb.append(o);
        } else if (o instanceof Double || o instanceof Float) {
            double d = ((Number) o).doubleValue();
            if (Double.isNaN(d) || Double.isInfinite(d)) {
                throw new IllegalArgumentException("número não finito");
            }
            sb.append(d);
        } else if (o instanceof Map<?, ?> m) {
            sb.append('{');
            boolean first = true;
            for (Map.Entry<?, ?> e : m.entrySet()) {
                if (!first) sb.append(',');
                first = false;
                str(sb, String.valueOf(e.getKey()));
                sb.append(':');
                w(sb, e.getValue());
            }
            sb.append('}');
        } else if (o instanceof Iterable<?> it) {
            sb.append('[');
            boolean first = true;
            for (Object x : it) {
                if (!first) sb.append(',');
                first = false;
                w(sb, x);
            }
            sb.append(']');
        } else {
            throw new IllegalArgumentException("tipo não suportado: " + o.getClass());
        }
    }

    private static void str(StringBuilder sb, String s) {
        sb.append('"');
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) sb.append(String.format("\\u%04x", (int) c));
                    else sb.append(c);
                }
            }
        }
        sb.append('"');
    }

    // ---------------------------------------------------------------- parser

    public static Object parse(String s) {
        P p = new P(s);
        p.ws();
        Object v = p.value();
        p.ws();
        if (p.i != s.length()) throw p.err("conteúdo extra após o JSON");
        return v;
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> parseObject(String s) {
        Object o = parse(s);
        if (!(o instanceof Map)) throw new IllegalArgumentException("esperado objeto JSON");
        return (Map<String, Object>) o;
    }

    private static final class P {
        final String s;
        int i;

        P(String s) { this.s = s; }

        IllegalArgumentException err(String m) {
            return new IllegalArgumentException("JSON inválido na posição " + i + ": " + m);
        }

        void ws() {
            while (i < s.length() && Character.isWhitespace(s.charAt(i))) i++;
        }

        Object value() {
            if (i >= s.length()) throw err("fim inesperado");
            char c = s.charAt(i);
            if (c == '{') return obj();
            if (c == '[') return arr();
            if (c == '"') return string();
            if (s.startsWith("true", i)) { i += 4; return Boolean.TRUE; }
            if (s.startsWith("false", i)) { i += 5; return Boolean.FALSE; }
            if (s.startsWith("null", i)) { i += 4; return null; }
            return number();
        }

        Map<String, Object> obj() {
            Map<String, Object> m = new LinkedHashMap<>();
            i++;
            ws();
            if (i >= s.length()) throw err("objeto não terminado");
            if (s.charAt(i) == '}') { i++; return m; }
            while (true) {
                ws();
                if (i >= s.length() || s.charAt(i) != '"') throw err("chave esperada");
                String k = string();
                ws();
                if (i >= s.length() || s.charAt(i) != ':') throw err("':' esperado");
                i++;
                ws();
                m.put(k, value());
                ws();
                if (i >= s.length()) throw err("fim inesperado");
                char c = s.charAt(i++);
                if (c == '}') return m;
                if (c != ',') throw err("',' ou '}' esperado");
            }
        }

        List<Object> arr() {
            List<Object> l = new ArrayList<>();
            i++;
            ws();
            if (i >= s.length()) throw err("array não terminado");
            if (s.charAt(i) == ']') { i++; return l; }
            while (true) {
                ws();
                l.add(value());
                ws();
                if (i >= s.length()) throw err("fim inesperado");
                char c = s.charAt(i++);
                if (c == ']') return l;
                if (c != ',') throw err("',' ou ']' esperado");
            }
        }

        String string() {
            StringBuilder sb = new StringBuilder();
            i++;
            while (true) {
                if (i >= s.length()) throw err("string não terminada");
                char c = s.charAt(i++);
                if (c == '"') return sb.toString();
                if (c == '\\') {
                    if (i >= s.length()) throw err("escape incompleto");
                    char e = s.charAt(i++);
                    switch (e) {
                        case '"' -> sb.append('"');
                        case '\\' -> sb.append('\\');
                        case '/' -> sb.append('/');
                        case 'n' -> sb.append('\n');
                        case 'r' -> sb.append('\r');
                        case 't' -> sb.append('\t');
                        case 'b' -> sb.append('\b');
                        case 'f' -> sb.append('\f');
                        case 'u' -> {
                            if (i + 4 > s.length()) throw err("\\u incompleto");
                            sb.append((char) Integer.parseInt(s.substring(i, i + 4), 16));
                            i += 4;
                        }
                        default -> throw err("escape inválido");
                    }
                } else {
                    sb.append(c);
                }
            }
        }

        Object number() {
            int st = i;
            while (i < s.length() && "+-0123456789.eE".indexOf(s.charAt(i)) >= 0) i++;
            if (st == i) throw err("valor inesperado");
            String t = s.substring(st, i);
            try {
                if (t.contains(".") || t.contains("e") || t.contains("E")) return Double.parseDouble(t);
                return Long.parseLong(t);
            } catch (NumberFormatException ex) {
                throw err("número inválido");
            }
        }
    }
}

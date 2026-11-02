package com.verden.rpg.core;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Sessão de campanha: junta o estado persistente, as definições carregadas e o interpretador de
 * condições/efeitos. É o ponto único de entrada do gameplay — a camada Minecraft só chama
 * {@link #fire}, {@link #apply} e {@link #eval}, e consome {@link #notifications} e {@link #grants}.
 *
 * Sintaxe de CONDIÇÃO (lista = todas devem valer; "a||b" dentro de um item = ou; prefixo "!" nega):
 *   flag:ID | quest:ID=active|done|failed|none | var:NOME>=N | rep:FACCAO>=N | aff:PERSONAGEM>=N
 *   choice:DECISAO=OPCAO | party:ID | protagonist:ID | memory:ID | codex:ID | region:ID | level>=N
 *   (operadores numéricos: >= <= > < == !=)
 *
 * Sintaxe de EFEITO:
 *   flag:ID | unflag:ID | var:NOME=N | var:NOME+=N | rep:FACCAO+=N | aff:PERSONAGEM+=N | choice:DECISAO=OPCAO
 *   quest.start:ID | codex:ID | region:ID | party+:ID | party-:ID | memory:ID | xp:N | chapter:ID
 *   protagonist:ID | item:ID*N | notify:TEXTO
 */
public final class Session {
    public final StoryState state;
    public final QuestEngine quests;
    public final Map<String, Dialogue> dialogues = new HashMap<>();
    public final Deque<Notification> notifications = new ArrayDeque<>();
    /** Itens a entregar ao jogador; a camada Minecraft esvazia esta lista. */
    public final List<Grant> grants = new ArrayList<>();

    public record Grant(String itemId, int amount) {}

    public Session(StoryState state) {
        this.state = state;
        this.quests = new QuestEngine(this);
    }

    // ------------------------------------------------------------------ eventos

    public void fire(GameEvent e) {
        quests.onEvent(e);
    }

    public void tick(long delta) {
        state.playTicks += delta;
    }

    // ------------------------------------------------------------------ condições

    public boolean eval(List<String> conds) {
        if (conds == null) return true;
        for (String c : conds) if (!evalOr(c)) return false;
        return true;
    }

    private boolean evalOr(String expr) {
        for (String part : expr.split("\\|\\|")) if (atom(part.trim())) return true;
        return false;
    }

    private static final Pattern NUM = Pattern.compile("^([\\w.]*?)(>=|<=|==|!=|>|<)(-?\\d+)$");

    private boolean atom(String a) {
        boolean neg = a.startsWith("!");
        if (neg) a = a.substring(1);
        return evalPositive(a) != neg;
    }

    private boolean evalPositive(String a) {
        if (a.startsWith("level")) {
            Matcher m = NUM.matcher(a);
            if (m.matches()) return cmp(state.current().level, m.group(2), Integer.parseInt(m.group(3)));
        }
        int i = a.indexOf(':');
        if (i < 0) throw new IllegalArgumentException("condição inválida: " + a);
        String k = a.substring(0, i), v = a.substring(i + 1);
        switch (k) {
            case "flag": return state.flags.contains(v);
            case "party": return state.party.contains(v);
            case "protagonist": return state.protagonist.equals(v);
            case "memory": return state.hasMemory(v);
            case "codex": return state.codex.contains(v);
            case "region": return state.regions.contains(v);
            case "choice": {
                int e = v.indexOf('=');
                if (e < 0) throw new IllegalArgumentException("choice precisa de '=': " + a);
                return v.substring(e + 1).equals(state.choices.get(v.substring(0, e)));
            }
            case "quest": {
                int e = v.indexOf('=');
                if (e < 0) throw new IllegalArgumentException("quest precisa de '=': " + a);
                return quests.statusOf(v.substring(0, e)).equals(v.substring(e + 1));
            }
            case "var": case "rep": case "aff": {
                Matcher m = NUM.matcher(v);
                if (!m.matches()) throw new IllegalArgumentException("comparação inválida: " + a);
                Map<String, Integer> map = k.equals("var") ? state.vars : k.equals("rep") ? state.reputation : state.affinity;
                return cmp(map.getOrDefault(m.group(1), 0), m.group(2), Integer.parseInt(m.group(3)));
            }
            default: throw new IllegalArgumentException("condição desconhecida: " + a);
        }
    }

    private static boolean cmp(int l, String op, int r) {
        return switch (op) {
            case ">=" -> l >= r;
            case "<=" -> l <= r;
            case ">" -> l > r;
            case "<" -> l < r;
            case "==" -> l == r;
            default -> l != r;
        };
    }

    // ------------------------------------------------------------------ efeitos

    private static final Pattern ASSIGN = Pattern.compile("^([\\w.]+)(\\+=|-=|=)(-?\\d+)$");

    public void apply(List<String> effects) {
        if (effects == null) return;
        for (String e : effects) applyOne(e);
        quests.recheck();
    }

    private void applyOne(String e) {
        int i = e.indexOf(':');
        if (i < 0) throw new IllegalArgumentException("efeito inválido: " + e);
        String k = e.substring(0, i), v = e.substring(i + 1);
        switch (k) {
            case "flag" -> state.flags.add(v);
            case "unflag" -> state.flags.remove(v);
            case "var" -> numeric(state.vars, v, e, Integer.MIN_VALUE, Integer.MAX_VALUE);
            case "rep" -> numeric(state.reputation, v, e, -100, 100);
            case "aff" -> numeric(state.affinity, v, e, -100, 100);
            case "choice" -> {
                int x = v.indexOf('=');
                if (x < 0) throw new IllegalArgumentException("choice precisa de '=': " + e);
                state.choices.put(v.substring(0, x), v.substring(x + 1));
            }
            case "quest.start" -> quests.start(v);
            case "codex" -> {
                if (state.codex.add(v)) notifications.add(new Notification(Notification.Kind.DISCOVERY, null, "Codex: " + v));
            }
            case "region" -> {
                if (state.regions.add(v)) notifications.add(new Notification(Notification.Kind.DISCOVERY, null, "Região descoberta: " + v));
            }
            case "party+" -> state.party.add(v);
            case "party-" -> state.party.remove(v);
            case "memory" -> {
                if (!state.hasMemory(v)) {
                    List<String> w = new ArrayList<>();
                    w.add(state.protagonist);
                    for (String p : state.party) if (!w.contains(p)) w.add(p);
                    state.memories.add(new StoryState.Memory(v, state.chapter, state.playTicks, w));
                }
            }
            case "xp" -> giveXp(Integer.parseInt(v));
            case "chapter" -> state.chapter = v;
            case "protagonist" -> state.protagonist = v;
            case "item" -> {
                int x = v.indexOf('*');
                grants.add(new Grant(x < 0 ? v : v.substring(0, x), x < 0 ? 1 : Integer.parseInt(v.substring(x + 1))));
            }
            case "notify" -> notifications.add(new Notification(Notification.Kind.INFO, null, v));
            default -> throw new IllegalArgumentException("efeito desconhecido: " + e);
        }
    }

    private static void numeric(Map<String, Integer> map, String v, String whole, int lo, int hi) {
        Matcher m = ASSIGN.matcher(v);
        if (!m.matches()) throw new IllegalArgumentException("efeito numérico inválido: " + whole);
        int cur = map.getOrDefault(m.group(1), 0), n = Integer.parseInt(m.group(3));
        int r = switch (m.group(2)) {
            case "+=" -> cur + n;
            case "-=" -> cur - n;
            default -> n;
        };
        map.put(m.group(1), Math.max(lo, Math.min(hi, r)));
    }

    void giveXp(int amount) {
        int up = state.current().addXp(amount);
        for (int i = 0; i < up; i++) {
            notifications.add(new Notification(Notification.Kind.LEVEL_UP, null,
                    state.protagonist + " alcançou o nível " + (state.current().level - up + i + 1)));
        }
    }
}

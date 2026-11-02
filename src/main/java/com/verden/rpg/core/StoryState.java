package com.verden.rpg.core;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Todo o estado persistente da campanha. É a única fonte de verdade do que o jogador
 * viveu: capítulo, escolhas, reputação, afinidade, memórias compartilhadas, progresso de
 * missões e fichas das protagonistas. Tudo que precisa sobreviver a fechar o jogo está aqui,
 * e somente aqui — sistemas de gameplay lêem e escrevem este objeto, nunca guardam estado próprio.
 *
 * Leitura tolerante: chaves desconhecidas são ignoradas e as ausentes recebem padrão, para que
 * saves de versões antigas/novas não quebrem o carregamento.
 */
public final class StoryState {
    public static final int SCHEMA = 1;

    public String protagonist = "trinity";
    public String chapter = "p1_01";
    public long playTicks = 0;

    public final Set<String> flags = new TreeSet<>();
    public final Map<String, Integer> vars = new TreeMap<>();
    /** Escolhas feitas: id da decisão → id da opção. Ordem de inserção = ordem cronológica. */
    public final Map<String, String> choices = new LinkedHashMap<>();
    public final Map<String, Integer> reputation = new TreeMap<>();
    public final Map<String, Integer> affinity = new TreeMap<>();
    public final Set<String> codex = new TreeSet<>();
    public final Set<String> regions = new TreeSet<>();
    public final Set<String> party = new LinkedHashSet<>();
    public final List<Memory> memories = new ArrayList<>();
    public final Map<String, QuestProgress> quests = new LinkedHashMap<>();
    public final Map<String, CharacterSheet> sheets = new LinkedHashMap<>();

    /** Memória Compartilhada: um acontecimento + quem estava presente. Base de diálogos tardios. */
    public record Memory(String id, String chapter, long tick, List<String> witnesses) {}

    public static final class QuestProgress {
        public enum Status { ACTIVE, DONE, FAILED }

        public Status status = Status.ACTIVE;
        public int stage = 0;
        public final Map<String, Integer> counts = new LinkedHashMap<>();
        public long startedTick;
    }

    public StoryState() {
        sheets.put("trinity", new CharacterSheet());
        sheets.put("marie", new CharacterSheet());
    }

    public CharacterSheet sheet(String id) {
        return sheets.computeIfAbsent(id, k -> new CharacterSheet());
    }

    public CharacterSheet current() {
        return sheet(protagonist);
    }

    public boolean hasMemory(String id) {
        for (Memory m : memories) if (m.id().equals(id)) return true;
        return false;
    }

    // ---------------------------------------------------------------- serialização

    public Map<String, Object> toMap() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("protagonist", protagonist);
        m.put("chapter", chapter);
        m.put("playTicks", playTicks);
        m.put("flags", new ArrayList<>(flags));
        m.put("vars", new LinkedHashMap<>(vars));
        m.put("choices", new LinkedHashMap<>(choices));
        m.put("reputation", new LinkedHashMap<>(reputation));
        m.put("affinity", new LinkedHashMap<>(affinity));
        m.put("codex", new ArrayList<>(codex));
        m.put("regions", new ArrayList<>(regions));
        m.put("party", new ArrayList<>(party));
        List<Object> mem = new ArrayList<>();
        for (Memory x : memories) {
            Map<String, Object> o = new LinkedHashMap<>();
            o.put("id", x.id());
            o.put("chapter", x.chapter());
            o.put("tick", x.tick());
            o.put("witnesses", new ArrayList<>(x.witnesses()));
            mem.add(o);
        }
        m.put("memories", mem);
        Map<String, Object> q = new LinkedHashMap<>();
        for (Map.Entry<String, QuestProgress> e : quests.entrySet()) {
            Map<String, Object> o = new LinkedHashMap<>();
            o.put("status", e.getValue().status.name());
            o.put("stage", e.getValue().stage);
            o.put("counts", new LinkedHashMap<>(e.getValue().counts));
            o.put("startedTick", e.getValue().startedTick);
            q.put(e.getKey(), o);
        }
        m.put("quests", q);
        Map<String, Object> sh = new LinkedHashMap<>();
        for (Map.Entry<String, CharacterSheet> e : sheets.entrySet()) sh.put(e.getKey(), e.getValue().toMap());
        m.put("sheets", sh);
        return m;
    }

    @SuppressWarnings("unchecked")
    public static StoryState fromMap(Map<String, Object> m) {
        StoryState s = new StoryState();
        if (m.get("protagonist") instanceof String p && !p.isBlank()) s.protagonist = p;
        if (m.get("chapter") instanceof String c && !c.isBlank()) s.chapter = c;
        s.playTicks = Math.max(0, lng(m.get("playTicks"), 0));
        strings(m.get("flags"), s.flags);
        ints(m.get("vars"), s.vars);
        if (m.get("choices") instanceof Map<?, ?> c) {
            for (Map.Entry<?, ?> e : c.entrySet()) s.choices.put(String.valueOf(e.getKey()), String.valueOf(e.getValue()));
        }
        ints(m.get("reputation"), s.reputation);
        ints(m.get("affinity"), s.affinity);
        strings(m.get("codex"), s.codex);
        strings(m.get("regions"), s.regions);
        strings(m.get("party"), s.party);
        if (m.get("memories") instanceof List<?> l) {
            for (Object o : l) {
                if (!(o instanceof Map<?, ?> mm)) continue;
                List<String> w = new ArrayList<>();
                strings(mm.get("witnesses"), w);
                s.memories.add(new Memory(String.valueOf(mm.get("id")), String.valueOf(mm.get("chapter")), lng(mm.get("tick"), 0), w));
            }
        }
        if (m.get("quests") instanceof Map<?, ?> q) {
            for (Map.Entry<?, ?> e : q.entrySet()) {
                if (!(e.getValue() instanceof Map<?, ?> o)) continue;
                QuestProgress p = new QuestProgress();
                try {
                    p.status = QuestProgress.Status.valueOf(String.valueOf(o.get("status")));
                } catch (IllegalArgumentException ex) {
                    p.status = QuestProgress.Status.ACTIVE;
                }
                p.stage = Math.max(0, num(o.get("stage"), 0));
                ints(o.get("counts"), p.counts);
                p.startedTick = lng(o.get("startedTick"), 0);
                s.quests.put(String.valueOf(e.getKey()), p);
            }
        }
        if (m.get("sheets") instanceof Map<?, ?> sh) {
            for (Map.Entry<?, ?> e : sh.entrySet()) {
                if (e.getValue() instanceof Map<?, ?> o) s.sheets.put(String.valueOf(e.getKey()), CharacterSheet.fromMap((Map<String, Object>) o));
            }
        }
        return s;
    }

    static int num(Object o, int def) {
        return o instanceof Number n ? n.intValue() : def;
    }

    static long lng(Object o, long def) {
        return o instanceof Number n ? n.longValue() : def;
    }

    private static void strings(Object o, java.util.Collection<String> out) {
        if (o instanceof List<?> l) for (Object x : l) out.add(String.valueOf(x));
    }

    private static void ints(Object o, Map<String, Integer> out) {
        if (o instanceof Map<?, ?> mm) for (Map.Entry<?, ?> e : mm.entrySet()) out.put(String.valueOf(e.getKey()), num(e.getValue(), 0));
    }
}

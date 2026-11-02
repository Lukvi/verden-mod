package com.verden.rpg.core;

import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * Converte JSON em {@link Quest}/{@link Dialogue} e valida referências cruzadas.
 * Leitura de arquivos é injetada ({@code reader}) para manter o núcleo livre de Minecraft:
 * no jogo lê do classpath do mod; nos testes, do disco.
 *
 * Falhas de conteúdo lançam IllegalArgumentException com o arquivo/id, nunca ficam silenciosas.
 */
public final class ContentLoader {
    private ContentLoader() {}

    /** Carrega tudo que o índice lista. Formato: {"quests":["caminho",...],"dialogues":["caminho",...]} */
    public static void loadIndex(Session s, String indexJson, Function<String, String> reader) {
        Map<String, Object> idx = Json.parseObject(indexJson);
        for (String p : strList(idx.get("quests"))) {
            try {
                s.quests.register(parseQuest(Json.parseObject(reader.apply(p))));
            } catch (RuntimeException e) {
                throw new IllegalArgumentException("erro em " + p + ": " + e.getMessage(), e);
            }
        }
        for (String p : strList(idx.get("characters"))) {
            try {
                CharacterDef c = parseCharacter(Json.parseObject(reader.apply(p)));
                if (s.characters.putIfAbsent(c.id(), c) != null) throw new IllegalArgumentException("personagem duplicado: " + c.id());
            } catch (RuntimeException e) {
                throw new IllegalArgumentException("erro em " + p + ": " + e.getMessage(), e);
            }
        }
        for (String p : strList(idx.get("dialogues"))) {
            try {
                Dialogue d = parseDialogue(Json.parseObject(reader.apply(p)));
                if (s.dialogues.putIfAbsent(d.id(), d) != null) throw new IllegalArgumentException("diálogo duplicado: " + d.id());
            } catch (RuntimeException e) {
                throw new IllegalArgumentException("erro em " + p + ": " + e.getMessage(), e);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static Quest parseQuest(Map<String, Object> m) {
        String id = req(m, "id");
        List<Quest.Stage> stages = new ArrayList<>();
        for (Object so : list(m.get("stages"))) {
            Map<String, Object> sm = (Map<String, Object>) so;
            List<Quest.Objective> objs = new ArrayList<>();
            for (Object oo : list(sm.get("objectives"))) {
                Map<String, Object> om = (Map<String, Object>) oo;
                String kind = req(om, "kind");
                objs.add(new Quest.Objective(req(om, "id"), req(om, "text"), kind,
                        kind.equals("state") ? str(om.get("target"), "") : req(om, "target"),
                        Math.max(1, StoryState.num(om.get("count"), 1)), Boolean.TRUE.equals(om.get("optional")), strList(om.get("when"))));
            }
            stages.add(new Quest.Stage(req(sm, "id"), req(sm, "journal"), objs, strList(sm.get("onEnter"))));
        }
        Map<String, Object> rw = m.get("rewards") instanceof Map<?, ?> r ? (Map<String, Object>) r : new LinkedHashMap<>();
        Quest.Category cat;
        try {
            cat = Quest.Category.valueOf(req(m, "category").toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("categoria inválida em " + id);
        }
        return new Quest(id, req(m, "title"), cat, str(m.get("chapter"), ""), str(m.get("giver"), ""), str(m.get("summary"), ""),
                strList(m.get("requires")), stages, strList(m.get("onStart")), strList(rw.get("effects")),
                StoryState.num(rw.get("xp"), 0), strList(rw.get("items")));
    }

    @SuppressWarnings("unchecked")
    public static CharacterDef parseCharacter(Map<String, Object> m) {
        String id = req(m, "id");
        List<CharacterDef.Outfit> outfits = new ArrayList<>();
        for (Object o : list(m.get("outfits"))) {
            Map<String, Object> om = (Map<String, Object>) o;
            outfits.add(new CharacterDef.Outfit(req(om, "id"), req(om, "name"), req(om, "skin"),
                    !Boolean.FALSE.equals(om.get("canon")), strList(om.get("requires")), str(om.get("description"), "")));
        }
        Map<String, Integer> attrs = new LinkedHashMap<>();
        if (m.get("attributes") instanceof Map<?, ?> a) for (Map.Entry<?, ?> e : a.entrySet()) attrs.put(String.valueOf(e.getKey()), StoryState.num(e.getValue(), 0));
        return new CharacterDef(id, req(m, "name"), Boolean.TRUE.equals(m.get("playable")), strList(m.get("unlock")),
                req(m, "model"), req(m, "animations"), str(m.get("portrait"), ""), req(m, "defaultOutfit"), outfits,
                strList(m.get("abilities")), attrs, str(m.get("bookNote"), ""));
    }

    @SuppressWarnings("unchecked")
    public static Dialogue parseDialogue(Map<String, Object> m) {
        String id = req(m, "id");
        Map<String, Dialogue.Node> nodes = new LinkedHashMap<>();
        Map<String, Object> nm = (Map<String, Object>) m.get("nodes");
        if (nm == null) throw new IllegalArgumentException("diálogo sem nós: " + id);
        for (Map.Entry<String, Object> e : nm.entrySet()) {
            Map<String, Object> n = (Map<String, Object>) e.getValue();
            List<Dialogue.Variant> vars = new ArrayList<>();
            for (Object v : list(n.get("variants"))) {
                Map<String, Object> vm = (Map<String, Object>) v;
                vars.add(new Dialogue.Variant(strList(vm.get("if")), req(vm, "text")));
            }
            List<Dialogue.Option> opts = new ArrayList<>();
            for (Object o : list(n.get("options"))) {
                Map<String, Object> om = (Map<String, Object>) o;
                opts.add(new Dialogue.Option(req(om, "text"), strList(om.get("if")), strList(om.get("effects")), str(om.get("next"), "")));
            }
            List<Dialogue.Branch> brs = new ArrayList<>();
            for (Object b : list(n.get("branches"))) {
                Map<String, Object> bm = (Map<String, Object>) b;
                brs.add(new Dialogue.Branch(strList(bm.get("if")), str(bm.get("next"), "")));
            }
            nodes.put(e.getKey(), new Dialogue.Node(e.getKey(), str(n.get("speaker"), ""), str(n.get("mood"), "neutral"),
                    str(n.get("text"), ""), vars, strList(n.get("onEnter")), opts, brs, str(n.get("next"), "")));
        }
        String start = req(m, "start");
        if (!nodes.containsKey(start)) throw new IllegalArgumentException("start inexistente em " + id + ": " + start);
        return new Dialogue(id, start, nodes);
    }

    /**
     * Procura referências quebradas: nós inexistentes, missões inexistentes em "quest.start:",
     * condições/efeitos com sintaxe inválida. Devolve a lista de problemas (vazia = ok).
     */
    public static List<String> validate(Session s) {
        List<String> problems = new ArrayList<>();
        Set<String> questIds = new java.util.HashSet<>();
        for (Quest q : s.quests.all()) questIds.add(q.id());
        for (Quest q : s.quests.all()) {
            checkConds(s, q.requires(), "quest " + q.id() + " requires", problems);
            checkEffects(q.onStart(), questIds, "quest " + q.id() + " onStart", problems);
            checkEffects(q.onComplete(), questIds, "quest " + q.id() + " rewards", problems);
            if (q.stages().isEmpty()) problems.add("quest " + q.id() + " sem estágios");
            Set<String> seen = new java.util.HashSet<>();
            for (Quest.Stage st : q.stages()) {
                checkEffects(st.onEnter(), questIds, "quest " + q.id() + "/" + st.id() + " onEnter", problems);
                if (st.objectives().isEmpty()) problems.add("quest " + q.id() + "/" + st.id() + " sem objetivos");
                for (Quest.Objective o : st.objectives()) {
                    if (!seen.add(o.id())) problems.add("quest " + q.id() + ": objetivo duplicado " + o.id());
                    checkConds(s, o.when(), "quest " + q.id() + "/" + o.id() + " when", problems);
                }
            }
        }
        for (CharacterDef c : s.characters.values()) {
            String w = "character " + c.id();
            if (c.outfit(c.defaultOutfit()) == null) problems.add(w + ": roupa padrão inexistente '" + c.defaultOutfit() + "'");
            Set<String> oi = new java.util.HashSet<>();
            for (CharacterDef.Outfit o : c.outfits()) {
                if (!oi.add(o.id())) problems.add(w + ": roupa duplicada " + o.id());
                checkConds(s, o.requires(), w + "/" + o.id() + " requires", problems);
            }
            checkConds(s, c.unlock(), w + " unlock", problems);
            if (c.playable() && c.abilities().isEmpty()) problems.add(w + ": personagem jogável sem habilidades base");
        }
        for (Dialogue d : s.dialogues.values()) {
            for (Dialogue.Node n : d.nodes().values()) {
                String w = "dialogue " + d.id() + "/" + n.id();
                refNode(d, n.next(), w + " next", problems);
                checkEffects(n.onEnter(), questIds, w + " onEnter", problems);
                for (Dialogue.Variant v : n.variants()) checkConds(s, v.cond(), w + " variant", problems);
                for (Dialogue.Branch b : n.branches()) {
                    refNode(d, b.next(), w + " branch", problems);
                    checkConds(s, b.cond(), w + " branch", problems);
                }
                for (Dialogue.Option o : n.options()) {
                    refNode(d, o.next(), w + " option", problems);
                    checkConds(s, o.cond(), w + " option", problems);
                    checkEffects(o.effects(), questIds, w + " option", problems);
                }
            }
        }
        return problems;
    }

    private static void refNode(Dialogue d, String next, String where, List<String> problems) {
        if (next != null && !next.isEmpty() && !d.nodes().containsKey(next)) problems.add(where + " → nó inexistente '" + next + "'");
    }

    private static void checkConds(Session s, List<String> conds, String where, List<String> problems) {
        // avalia contra um estado vazio só para detectar erros de sintaxe
        Session probe = new Session(new StoryState());
        for (String c : conds) {
            try {
                probe.eval(List.of(c));
            } catch (RuntimeException e) {
                problems.add(where + ": " + e.getMessage());
            }
        }
    }

    private static void checkEffects(List<String> effects, Set<String> questIds, String where, List<String> problems) {
        for (String e : effects) {
            if (e.startsWith("quest.start:") && !questIds.contains(e.substring("quest.start:".length()))) {
                problems.add(where + ": missão inexistente em '" + e + "'");
                continue;
            }
            if (e.startsWith("quest.start:")) continue;
            try {
                Session probe = new Session(new StoryState());
                probe.apply(List.of(e));
            } catch (RuntimeException ex) {
                problems.add(where + ": " + ex.getMessage());
            }
        }
    }

    // ------------------------------------------------------------------ util

    private static String req(Map<String, Object> m, String k) {
        Object v = m.get(k);
        if (!(v instanceof String s) || s.isBlank()) throw new IllegalArgumentException("campo obrigatório ausente: " + k);
        return s;
    }

    private static String str(Object o, String def) {
        return o instanceof String s ? s : def;
    }

    private static List<Object> list(Object o) {
        return o instanceof List<?> l ? new ArrayList<>(l) : new ArrayList<>();
    }

    private static List<String> strList(Object o) {
        List<String> out = new ArrayList<>();
        if (o instanceof List<?> l) for (Object x : l) out.add(String.valueOf(x));
        return out;
    }
}

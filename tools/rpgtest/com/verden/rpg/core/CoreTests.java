package com.verden.rpg.core;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Testes do núcleo do RPG, sem dependências externas. Rodar: tools/run_core_tests.sh */
public final class CoreTests {
    static int passed, failed;
    static final List<String> failures = new ArrayList<>();

    interface T { void run() throws Exception; }

    static void test(String name, T t) {
        try {
            t.run();
            passed++;
            System.out.println("  ok   " + name);
        } catch (Throwable e) {
            failed++;
            failures.add(name + ": " + e);
            System.out.println("  FAIL " + name + " -> " + e);
        }
    }

    static void check(boolean c, String msg) { if (!c) throw new AssertionError(msg); }

    static void eq(Object a, Object b, String msg) {
        if (!java.util.Objects.equals(a, b)) throw new AssertionError(msg + " (esperado=" + b + ", obtido=" + a + ")");
    }

    static Session loadedSession() {
        Session s = new Session(new StoryState());
        ContentLoader.loadIndex(s, read("data/verden/rpg/index.json"), CoreTests::read);
        return s;
    }

    static String read(String p) {
        try {
            return Files.readString(Path.of("src/main/resources", p), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /** Percorre um diálogo escolhendo sempre a opção indicada por índice (ou avançando). */
    static DialogueRunner play(Session s, String id, int... picks) {
        DialogueRunner r = new DialogueRunner(s, s.dialogues.get(id));
        int k = 0, guard = 0;
        while (!r.finished() && guard++ < 100) {
            if (r.options().isEmpty()) r.advance();
            else r.choose(k < picks.length ? picks[k++] : 0);
        }
        check(r.finished(), "diálogo não terminou: " + id);
        return r;
    }

    public static void main(String[] args) throws Exception {
        System.out.println("== JSON");
        test("roundtrip com escapes e unicode", () -> {
            Map<String, Object> m = new java.util.LinkedHashMap<>();
            m.put("a", "linha1\nlinha2 \"aspas\" \\ tab\t ção ø");
            m.put("n", 42L);
            m.put("d", 1.5);
            m.put("l", List.of(1L, "x", true));
            m.put("z", null);
            eq(Json.parseObject(Json.write(m)), m, "roundtrip");
        });
        test("rejeita JSON inválido", () -> {
            for (String bad : new String[]{"{", "{\"a\":}", "[1,2", "{\"a\":1} x", "\"abc"}) {
                try { Json.parse(bad); throw new AssertionError("aceitou: " + bad); } catch (IllegalArgumentException ok) { }
            }
        });

        System.out.println("== condições e efeitos");
        test("todas as condições", () -> {
            Session s = new Session(new StoryState());
            s.apply(List.of("flag:a", "var:x=3", "rep:flerin+=10", "aff:dan+=4", "choice:c=op", "party+:dan", "region:yettus", "codex:k"));
            check(s.eval(List.of("flag:a", "var:x>=3", "var:x==3", "rep:flerin>=10", "aff:dan>3", "choice:c=op", "party:dan", "region:yettus", "codex:k", "protagonist:trinity", "level>=1")), "positivas");
            check(!s.eval(List.of("flag:b")), "flag ausente");
            check(s.eval(List.of("!flag:b")), "negação");
            check(s.eval(List.of("flag:b||flag:a")), "ou");
            check(!s.eval(List.of("flag:b||flag:c")), "ou falso");
            check(!s.eval(List.of("var:x!=3")), "!=");
            check(s.eval(List.of("quest:q=none")), "quest none");
        });
        test("reputação e afinidade ficam em [-100,100]", () -> {
            Session s = new Session(new StoryState());
            s.apply(List.of("rep:f+=500", "aff:d-=900"));
            eq(s.state.reputation.get("f"), 100, "rep teto");
            eq(s.state.affinity.get("d"), -100, "aff piso");
        });
        test("sintaxe inválida falha alto", () -> {
            Session s = new Session(new StoryState());
            for (String bad : new String[]{"foo:bar", "semdoispontos", "var:x*=2"}) {
                try { s.apply(List.of(bad)); throw new AssertionError("aceitou efeito " + bad); } catch (IllegalArgumentException ok) { }
            }
            try { s.eval(List.of("zzz:1")); throw new AssertionError("aceitou condição"); } catch (IllegalArgumentException ok) { }
        });

        System.out.println("== progressão");
        test("curva de XP, pontos e níveis", () -> {
            CharacterSheet c = new CharacterSheet();
            eq(c.addXp(99), 0, "99 xp não sobe");
            eq(c.addXp(1), 1, "100 xp sobe 1");
            eq(c.level, 2, "nível 2");
            eq(c.skillPoints, 1, "1 ponto");
            int g = c.addXp(100000);
            check(g > 5 && c.level <= CharacterSheet.MAX_LEVEL, "sobe vários, respeita teto");
            check(!c.unlock("x", 9999), "sem pontos não libera");
            int before = c.skillPoints;
            check(c.unlock("x", 1) && c.skillPoints == before - 1, "libera gastando");
            check(!c.unlock("x", 1), "não libera duas vezes");
        });
        test("Trinity e Marie têm progressão independente", () -> {
            Session s = new Session(new StoryState());
            s.apply(List.of("xp:500"));
            int lvT = s.state.sheet("trinity").level;
            s.apply(List.of("protagonist:marie", "xp:100"));
            check(lvT > 1, "Trinity subiu");
            eq(s.state.sheet("trinity").level, lvT, "Trinity intacta ao jogar de Marie");
            check(s.state.sheet("marie").level >= 2 && s.state.sheet("marie").level < lvT, "Marie evoluiu separada");
        });

        System.out.println("== conteúdo");
        test("carrega o índice e valida referências", () -> {
            Session s = loadedSession();
            List<String> p = ContentLoader.validate(s);
            check(p.isEmpty(), "problemas: " + p);
            check(s.quests.def("p1_q01_yettus") != null && s.dialogues.containsKey("nefertari_audiencia"), "carregou");
        });
        test("validador detecta referências quebradas", () -> {
            Session s = new Session(new StoryState());
            s.dialogues.put("d", ContentLoader.parseDialogue(Json.parseObject(
                    "{\"id\":\"d\",\"start\":\"a\",\"nodes\":{\"a\":{\"text\":\"x\",\"next\":\"fantasma\",\"onEnter\":[\"quest.start:nada\",\"bogus:1\"]}}}")));
            List<String> p = ContentLoader.validate(s);
            check(p.size() == 3, "esperava 3 problemas: " + p);
        });

        System.out.println("== campanha (protótipo cap. 1-3)");
        test("partida completa do arco inicial", () -> {
            Session s = loadedSession();
            check(s.quests.start("p1_q01_yettus"), "inicia missão 1");
            eq(s.quests.statusOf("p1_q01_yettus"), "active", "ativa");
            check(s.state.regions.contains("deserto_yettus"), "região do deserto");

            // evento fora do estágio atual não conta
            s.fire(new GameEvent("reach", "zigurate_entrada"));
            eq(s.state.quests.get("p1_q01_yettus").stage, 0, "ignorou evento fora de ordem");

            play(s, "dan_yettus", 1, 1, 0); // escolhas: cuidado, informal, reservada (única)
            check(s.state.flags.contains("dan_curado") && s.state.flags.contains("principe_identificado"), "flags do encontro");
            eq(s.state.choices.get("yettus_apresentacao"), "informal", "escolha registrada");
            eq(s.state.affinity.get("dan"), 2, "afinidade 2 (cuidado + informal)");
            eq(s.state.quests.get("p1_q01_yettus").stage, 1, "foi para as muralhas");

            s.fire(new GameEvent("reach", "flerin_portao"));
            s.fire(new GameEvent("reach", "zigurate_entrada"));
            eq(s.state.quests.get("p1_q01_yettus").stage, 3, "chegou à audiência (mercado opcional não bloqueou)");

            DialogueRunner r = new DialogueRunner(s, s.dialogues.get("nefertari_audiencia"));
            check(r.text().contains("dispensa títulos"), "Nefertari reage à apresentação informal");
            while (r.options().isEmpty()) r.advance();
            r.choose(0); // direto
            while (!r.finished()) { if (r.options().isEmpty()) r.advance(); else r.choose(0); }

            eq(s.quests.statusOf("p1_q01_yettus"), "done", "missão 1 concluída");
            check(s.state.flags.contains("proposta_entregue") && s.state.flags.contains("assembleia_marcada"), "marcos canônicos");
            eq(s.state.chapter, "p1_02", "capítulo avançou");
            check(s.state.hasMemory("audiencia_nefertari"), "memória registrada");
            check(s.quests.isAvailable("p1_q02_lena"), "missão 2 disponível");
            check(s.state.sheet("trinity").xp >= 80 || s.state.sheet("trinity").level > 1, "xp concedido");

            // missão 2
            play(s, "dan_parapeito", 0);
            eq(s.quests.statusOf("p1_q02_lena"), "active", "missão 2 iniciada pelo diálogo");
            for (String[] ev : new String[][]{
                    {"reach", "sala_secreta_dan"}, {"examine", "caderno_lena"}, {"reach", "biblioteca_leste"},
                    {"examine", "quadro_biblioteca"}, {"collect", "grimorio_rishnuu"}, {"examine", "grimorio_rishnuu"}}) {
                s.fire(new GameEvent(ev[0], ev[1]));
            }
            eq(s.quests.statusOf("p1_q02_lena"), "active", "ainda falta o feitiço");
            s.fire(new GameEvent("cast", "desvinculo"));
            eq(s.quests.statusOf("p1_q02_lena"), "done", "missão 2 concluída");
            check(s.state.flags.contains("caderno_aberto"), "caderno aberto");
            check(s.state.codex.contains("runa_trancamento"), "codex da runa");
            boolean q = false, c = false;
            for (Notification n : s.notifications) {
                q |= n.kind() == Notification.Kind.QUEST_STARTED;
                c |= n.kind() == Notification.Kind.QUEST_COMPLETED;
            }
            check(q && c, "notificações de início e conclusão");
        });
        test("escolha direta x diplomática muda reação e reputação", () -> {
            Session a = loadedSession(), b = loadedSession();
            for (Session s : new Session[]{a, b}) {
                s.quests.start("p1_q01_yettus");
                play(s, "dan_yettus", 0, 0, 0);
                s.fire(new GameEvent("reach", "flerin_portao"));
                s.fire(new GameEvent("reach", "zigurate_entrada"));
            }
            play(a, "nefertari_audiencia", 0);
            play(b, "nefertari_audiencia", 1);
            eq(a.state.choices.get("proposta_tom"), "direto", "tom a");
            eq(b.state.choices.get("proposta_tom"), "diplomatico", "tom b");
            eq(b.state.reputation.get("flerin_corte"), 2, "diplomacia rende reputação");
            check(a.state.reputation.getOrDefault("flerin_corte", 0) == 0, "franqueza não rende");
            check(a.state.flags.contains("proposta_entregue") && b.state.flags.contains("proposta_entregue"), "cânone igual nos dois caminhos");
        });
        test("opção com condição falsa fica oculta", () -> {
            Session s = new Session(new StoryState());
            s.dialogues.put("d", ContentLoader.parseDialogue(Json.parseObject(
                    "{\"id\":\"d\",\"start\":\"a\",\"nodes\":{\"a\":{\"text\":\"x\",\"options\":[{\"text\":\"1\",\"next\":\"\"},{\"text\":\"2\",\"if\":[\"flag:vip\"],\"next\":\"\"}]}}}")));
            eq(new DialogueRunner(s, s.dialogues.get("d")).options().size(), 1, "oculta");
            s.apply(List.of("flag:vip"));
            eq(new DialogueRunner(s, s.dialogues.get("d")).options().size(), 2, "visível");
        });
        test("laço infinito em diálogo é interrompido", () -> {
            Session s = new Session(new StoryState());
            s.dialogues.put("d", ContentLoader.parseDialogue(Json.parseObject(
                    "{\"id\":\"d\",\"start\":\"a\",\"nodes\":{\"a\":{\"next\":\"b\"},\"b\":{\"next\":\"a\"}}}")));
            try { new DialogueRunner(s, s.dialogues.get("d")); throw new AssertionError("não detectou"); } catch (IllegalStateException ok) { }
        });
        test("memória compartilhada guarda quem estava presente", () -> {
            Session s = new Session(new StoryState());
            s.apply(List.of("party+:dan", "memory:fuga_do_deserto"));
            check(s.state.hasMemory("fuga_do_deserto"), "registrada");
            check(s.state.memories.get(0).witnesses().containsAll(List.of("trinity", "dan")), "testemunhas");
            check(s.eval(List.of("memory:fuga_do_deserto")), "condição memory");
        });

        System.out.println("== salvamento");
        Path tmp = Files.createTempDirectory("verden_save");
        Path f = tmp.resolve("state.json");
        test("save/load preserva tudo", () -> {
            Session s = loadedSession();
            s.quests.start("p1_q01_yettus");
            play(s, "dan_yettus", 1, 1, 0);
            s.apply(List.of("protagonist:marie", "xp:300", "protagonist:trinity", "party+:dan", "memory:m1"));
            s.state.playTicks = 12345;
            SaveManager.save(f, s.state);
            SaveManager.LoadResult r = SaveManager.load(f);
            check(r != null && !r.recovered(), "carregou do principal");
            eq(Json.write(r.state().toMap()), Json.write(s.state.toMap()), "estado idêntico");
            check(!Files.exists(tmp.resolve("state.json.tmp")), "sem .tmp sobrando");
        });
        test("principal corrompido recupera do backup", () -> {
            Session s = new Session(new StoryState());
            s.apply(List.of("flag:v1"));
            SaveManager.save(f, s.state);
            s.apply(List.of("flag:v2"));
            SaveManager.save(f, s.state); // v1 vai para bak1
            Files.writeString(f, "{\"schema\":1,\"checksum\":\"lixo\",\"state\":{}}");
            SaveManager.LoadResult r = SaveManager.load(f);
            check(r != null && r.recovered(), "recuperou");
            check(r.state().flags.contains("v1") || r.state().flags.contains("v2"), "estado de backup válido");
            check(!r.warnings().isEmpty(), "avisou");
        });
        test("arquivo truncado é rejeitado", () -> {
            Path g = tmp.resolve("trunc.json");
            Session s = new Session(new StoryState());
            SaveManager.save(g, s.state);
            String txt = Files.readString(g);
            Files.writeString(g, txt.substring(0, txt.length() / 2));
            check(SaveManager.load(g) == null, "sem backup e truncado → null");
        });
        test("somente 3 backups; corrompido não contamina backups", () -> {
            Path g = tmp.resolve("rot.json");
            Session s = new Session(new StoryState());
            for (int i = 0; i < 8; i++) { s.apply(List.of("var:n=" + i)); SaveManager.save(g, s.state); }
            check(Files.exists(tmp.resolve("rot.json.bak3")) && !Files.exists(tmp.resolve("rot.json.bak4")), "rotação limitada");
            Files.writeString(g, "lixo");
            SaveManager.save(g, s.state); // principal ilegível: não deve ir para os backups
            for (int i = 1; i <= 3; i++) check(SaveManager.load(tmp.resolve("rot.json.bak" + i)) != null, "bak" + i + " íntegro");
        });
        test("save de versão mais nova é recusado e não é sobrescrito", () -> {
            Path g = tmp.resolve("new.json");
            String body = "{\"schema\":99,\"checksum\":\"x\",\"state\":{}}";
            Files.writeString(g, body);
            try { SaveManager.load(g); throw new AssertionError("deveria recusar"); } catch (SaveManager.SchemaTooNewException ok) { }
            eq(Files.readString(g), body, "arquivo intacto");
        });
        test("sem save → null (jogo novo)", () -> check(SaveManager.load(tmp.resolve("naoexiste.json")) == null, "null"));
        test("carregamento tolera chaves desconhecidas e ausentes", () -> {
            StoryState s = StoryState.fromMap(Json.parseObject("{\"chapter\":\"p9\",\"campo_do_futuro\":[1,2,3]}"));
            eq(s.chapter, "p9", "capítulo");
            check(s.sheets.containsKey("trinity") && s.sheets.containsKey("marie"), "fichas padrão");
        });

        System.out.println("\n" + passed + " passaram, " + failed + " falharam");
        if (failed > 0) { failures.forEach(x -> System.out.println(" - " + x)); System.exit(1); }
    }
}

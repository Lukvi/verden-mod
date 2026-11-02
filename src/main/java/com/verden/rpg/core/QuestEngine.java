package com.verden.rpg.core;

import com.verden.rpg.core.StoryState.QuestProgress;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Motor de missões. Não conhece Minecraft: recebe {@link GameEvent}s e atualiza o
 * {@link StoryState}. Conceitos:
 *  - missão = lista de ESTÁGIOS em sequência; estágio = conjunto de objetivos;
 *  - o estágio avança quando todos os objetivos não opcionais estão completos;
 *  - objetivos "state" se completam sozinhos quando as condições passam a valer.
 */
public final class QuestEngine {
    private final Session session;
    private final Map<String, Quest> defs = new LinkedHashMap<>();
    private boolean rechecking;

    QuestEngine(Session session) {
        this.session = session;
    }

    public void register(Quest q) {
        if (defs.putIfAbsent(q.id(), q) != null) throw new IllegalArgumentException("missão duplicada: " + q.id());
    }

    public Quest def(String id) {
        return defs.get(id);
    }

    public Iterable<Quest> all() {
        return defs.values();
    }

    /** none | active | done | failed */
    public String statusOf(String id) {
        QuestProgress p = session.state.quests.get(id);
        return p == null ? "none" : p.status.name().toLowerCase();
    }

    /** Disponível = existe, ainda não iniciada e com os requisitos cumpridos. */
    public boolean isAvailable(String id) {
        Quest q = defs.get(id);
        return q != null && !session.state.quests.containsKey(id) && session.eval(q.requires());
    }

    public List<Quest> available() {
        List<Quest> out = new ArrayList<>();
        for (Quest q : defs.values()) if (isAvailable(q.id())) out.add(q);
        return out;
    }

    public boolean start(String id) {
        Quest q = defs.get(id);
        if (q == null) throw new IllegalArgumentException("missão inexistente: " + id);
        if (session.state.quests.containsKey(id)) return false;
        QuestProgress p = new QuestProgress();
        p.startedTick = session.state.playTicks;
        session.state.quests.put(id, p);
        session.notifications.add(new Notification(Notification.Kind.QUEST_STARTED, id, q.title()));
        session.apply(q.onStart());
        if (!q.stages().isEmpty()) enterStage(q, p);
        recheck();
        return true;
    }

    /** Progresso de um objetivo (para o diário). */
    public int countOf(String questId, String objectiveId) {
        QuestProgress p = session.state.quests.get(questId);
        return p == null ? 0 : p.counts.getOrDefault(objectiveId, 0);
    }

    public void onEvent(GameEvent ev) {
        for (Map.Entry<String, QuestProgress> en : new ArrayList<>(session.state.quests.entrySet())) {
            QuestProgress p = en.getValue();
            Quest q = defs.get(en.getKey());
            if (q == null || p.status != QuestProgress.Status.ACTIVE || p.stage >= q.stages().size()) continue;
            for (Quest.Objective o : q.stages().get(p.stage).objectives()) {
                if (o.kind().equals("state") || !o.kind().equals(ev.kind())) continue;
                if (!o.target().equals("*") && !o.target().equals(ev.target())) continue;
                int cur = p.counts.getOrDefault(o.id(), 0);
                if (cur >= o.count()) continue;
                int now = Math.min(o.count(), cur + Math.max(1, ev.amount()));
                p.counts.put(o.id(), now);
                String txt = o.count() > 1 ? o.text() + " (" + now + "/" + o.count() + ")" : o.text();
                session.notifications.add(new Notification(Notification.Kind.QUEST_UPDATED, q.id(), txt));
            }
        }
        recheck();
    }

    /**
     * Reavalia objetivos "state" e avança estágios/conclui missões até estabilizar.
     * Protegido contra reentrada: efeitos de conclusão podem iniciar outras missões.
     */
    public void recheck() {
        if (rechecking) return;
        rechecking = true;
        try {
            boolean changed = true;
            int guard = 0;
            while (changed && guard++ < 200) {
                changed = false;
                for (Map.Entry<String, QuestProgress> en : new ArrayList<>(session.state.quests.entrySet())) {
                    QuestProgress p = en.getValue();
                    Quest q = defs.get(en.getKey());
                    if (q == null || p.status != QuestProgress.Status.ACTIVE || p.stage >= q.stages().size()) continue;
                    Quest.Stage st = q.stages().get(p.stage);
                    for (Quest.Objective o : st.objectives()) {
                        if (o.kind().equals("state") && p.counts.getOrDefault(o.id(), 0) < o.count() && session.eval(o.when())) {
                            p.counts.put(o.id(), o.count());
                            session.notifications.add(new Notification(Notification.Kind.QUEST_UPDATED, q.id(), o.text()));
                            changed = true;
                        }
                    }
                    if (stageDone(st, p)) {
                        p.stage++;
                        changed = true;
                        if (p.stage >= q.stages().size()) complete(q, p);
                        else enterStage(q, p);
                    }
                }
            }
        } finally {
            rechecking = false;
        }
    }

    private static boolean stageDone(Quest.Stage st, QuestProgress p) {
        for (Quest.Objective o : st.objectives()) {
            if (!o.optional() && p.counts.getOrDefault(o.id(), 0) < o.count()) return false;
        }
        return true;
    }

    private void enterStage(Quest q, QuestProgress p) {
        Quest.Stage st = q.stages().get(p.stage);
        if (p.stage > 0) session.notifications.add(new Notification(Notification.Kind.QUEST_UPDATED, q.id(), st.journal()));
        session.apply(st.onEnter());
    }

    private void complete(Quest q, QuestProgress p) {
        p.status = QuestProgress.Status.DONE;
        session.notifications.add(new Notification(Notification.Kind.QUEST_COMPLETED, q.id(), q.title()));
        session.apply(q.onComplete());
        if (q.xp() > 0) session.giveXp(q.xp());
        for (String it : q.items()) session.apply(List.of("item:" + it));
    }

    public void fail(String id) {
        QuestProgress p = session.state.quests.get(id);
        if (p != null && p.status == QuestProgress.Status.ACTIVE) p.status = QuestProgress.Status.FAILED;
    }

    /** Linhas do diário para uma missão ativa: texto do estágio atual + objetivos. */
    public List<String> journalLines(String id) {
        List<String> out = new ArrayList<>();
        Quest q = defs.get(id);
        QuestProgress p = session.state.quests.get(id);
        if (q == null || p == null) return out;
        if (p.status == QuestProgress.Status.DONE) {
            out.add("Concluída.");
            return out;
        }
        if (p.stage >= q.stages().size()) return out;
        Quest.Stage st = q.stages().get(p.stage);
        out.add(st.journal());
        for (Quest.Objective o : st.objectives()) {
            int c = p.counts.getOrDefault(o.id(), 0);
            if (o.optional() && c == 0 && !o.when().isEmpty() && !session.eval(o.when())) { /* mostra mesmo assim */ }
            out.add((c >= o.count() ? "[x] " : "[ ] ") + o.text() + (o.count() > 1 ? " (" + c + "/" + o.count() + ")" : "") + (o.optional() ? " (opcional)" : ""));
        }
        return out;
    }
}

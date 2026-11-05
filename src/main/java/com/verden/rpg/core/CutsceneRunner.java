package com.verden.rpg.core;

import java.util.ArrayList;
import java.util.List;

/**
 * Relógio de uma cutscene. A cada tick do servidor, {@link #tick()} devolve as instruções que começam naquele
 * instante; instruções com duração (fade, fala, espera) seguram a próxima até acabarem.
 */
public final class CutsceneRunner {
    /** Instrução a executar agora. */
    public record Cue(String kind, java.util.Map<String, Object> params, boolean essential) {}

    private final Cutscene cs;
    private int index, wait;
    private boolean done;

    public CutsceneRunner(Cutscene cs) {
        this.cs = cs;
    }

    public Cutscene cutscene() { return cs; }

    public boolean finished() { return done; }

    public List<Cue> tick() {
        List<Cue> out = new ArrayList<>();
        if (done) return out;
        if (wait > 0) {
            wait--;
            if (wait > 0) return out;
        }
        while (index < cs.steps().size()) {
            Cutscene.Step s = cs.steps().get(index++);
            out.add(new Cue(s.kind(), s.params(), s.essential()));
            if (s.ticks() > 0) {
                wait = s.ticks();
                return out;
            }
        }
        done = true;
        return out;
    }

    /**
     * Pula o resto. Devolve só as instruções essenciais restantes (para manter o mundo coerente).
     * Cenas não puláveis devolvem lista vazia e continuam normalmente.
     */
    public List<Cue> skip() {
        List<Cue> out = new ArrayList<>();
        if (done || !cs.skippable()) return out;
        while (index < cs.steps().size()) {
            Cutscene.Step s = cs.steps().get(index++);
            if (s.essential() || s.kind().equals("effect")) out.add(new Cue(s.kind(), s.params(), true));
        }
        done = true;
        return out;
    }
}

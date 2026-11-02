package com.verden.rpg.core;

import java.util.List;

/** Definição estática (carregada de JSON) de uma missão. O progresso fica em {@link StoryState.QuestProgress}. */
public record Quest(String id, String title, Category category, String chapter, String giver, String summary,
                    List<String> requires, List<Stage> stages, List<String> onStart, List<String> onComplete,
                    int xp, List<String> items) {

    public enum Category { MAIN, SIDE, OPTIONAL, PERSONAL, EXPLORATION, SECRET, CHALLENGE, BOSS, INVESTIGATION }

    /**
     * Objetivo. {@code kind} casa com {@link GameEvent#kind()}; o tipo especial {@code state} é cumprido
     * automaticamente quando as condições {@code when} passam a valer (ex.: "o caderno foi aberto").
     */
    public record Objective(String id, String text, String kind, String target, int count, boolean optional, List<String> when) {}

    /** Um estágio termina quando todos os objetivos não opcionais estão completos. */
    public record Stage(String id, String journal, List<Objective> objectives, List<String> onEnter) {}
}

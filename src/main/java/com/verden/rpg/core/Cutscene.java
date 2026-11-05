package com.verden.rpg.core;

import java.util.List;
import java.util.Map;

/**
 * Cutscene como DADO: uma lista de passos executados em sequência. Os passos são "instruções de direção"
 * (câmera, fade, fala, som, música, título, ações do mundo); o núcleo só controla o TEMPO e a ordem — quem
 * executa cada instrução é a camada Minecraft. Passos marcados {@code essential} (ações que mudam o estado do mundo)
 * ainda são executados quando o jogador pula a cena, para que pular nunca deixe a campanha inconsistente.
 */
public record Cutscene(String id, boolean skippable, List<Step> steps, List<String> onEnd) {

    /** @param kind fade | letterbox | shot | line | wait | sound | music | title | act | effect */
    public record Step(String kind, Map<String, Object> params, int ticks, boolean essential) {}

    public static final java.util.Set<String> KINDS = java.util.Set.of("fade", "letterbox", "shot", "line", "wait", "sound", "music", "title", "act", "effect");
}

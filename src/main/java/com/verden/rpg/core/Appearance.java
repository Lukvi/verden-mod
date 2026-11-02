package com.verden.rpg.core;

import java.util.List;

/**
 * O que a camada cliente precisa para desenhar e animar o personagem controlado agora.
 * Tudo vem do mesmo lugar que a troca de protagonista, então não há como a skin mudar sem
 * o personagem mudar (nem o contrário).
 */
public record Appearance(String characterId, String model, String skin, String animations, String portrait, List<String> abilities) {}

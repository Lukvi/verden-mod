package com.verden.rpg.core;

/**
 * Algo que aconteceu no jogo. A camada Minecraft traduz o mundo para eventos
 * (kill, reach, interact, collect, examine, dialogue...) e o motor decide o que isso significa.
 */
public record GameEvent(String kind, String target, int amount) {
    public GameEvent(String kind, String target) {
        this(kind, target, 1);
    }
}

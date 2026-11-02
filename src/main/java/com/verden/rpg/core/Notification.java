package com.verden.rpg.core;

/** Aviso para a HUD (início/atualização/conclusão de missão, subida de nível, descoberta...). */
public record Notification(Kind kind, String questId, String text) {
    public enum Kind { QUEST_STARTED, QUEST_UPDATED, QUEST_COMPLETED, LEVEL_UP, DISCOVERY, INFO }
}

package com.verden.rpg.core;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Ficha de progressão de um personagem jogável (Trinity, Marie) ou companheiro.
 * Cada protagonista possui a sua; nada aqui é compartilhado entre elas.
 * Os números da curva de XP são provisórios (marcados para balanceamento posterior).
 */
public final class CharacterSheet {
    public static final int MAX_LEVEL = 50;

    public int level = 1;
    public int xp = 0;
    public int skillPoints = 0;
    public final Set<String> skills = new TreeSet<>();
    public final Map<String, Integer> attributes = new LinkedHashMap<>();

    /** XP necessário para sair do nível {@code lvl} para o próximo. PROVISÓRIO. */
    public static int xpToNext(int lvl) {
        int n = lvl - 1;
        return 100 + 40 * n + 6 * n * n;
    }

    /** Soma XP e devolve quantos níveis foram ganhos. Cada nível concede 1 ponto de habilidade. */
    public int addXp(int amount) {
        if (amount <= 0 || level >= MAX_LEVEL) return 0;
        xp += amount;
        int gained = 0;
        while (level < MAX_LEVEL && xp >= xpToNext(level)) {
            xp -= xpToNext(level);
            level++;
            skillPoints++;
            gained++;
        }
        if (level >= MAX_LEVEL) xp = 0;
        return gained;
    }

    /** Gasta pontos para liberar uma habilidade. Retorna false se não puder (sem alterar nada). */
    public boolean unlock(String skillId, int cost) {
        if (skills.contains(skillId) || cost < 0 || skillPoints < cost) return false;
        skillPoints -= cost;
        skills.add(skillId);
        return true;
    }

    Map<String, Object> toMap() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("level", level);
        m.put("xp", xp);
        m.put("skillPoints", skillPoints);
        m.put("skills", new java.util.ArrayList<>(skills));
        m.put("attributes", new LinkedHashMap<>(attributes));
        return m;
    }

    @SuppressWarnings("unchecked")
    static CharacterSheet fromMap(Map<String, Object> m) {
        CharacterSheet c = new CharacterSheet();
        c.level = Math.max(1, Math.min(MAX_LEVEL, StoryState.num(m.get("level"), 1)));
        c.xp = Math.max(0, StoryState.num(m.get("xp"), 0));
        c.skillPoints = Math.max(0, StoryState.num(m.get("skillPoints"), 0));
        if (m.get("skills") instanceof java.util.List<?> l) for (Object o : l) c.skills.add(String.valueOf(o));
        if (m.get("attributes") instanceof Map<?, ?> a) {
            for (Map.Entry<?, ?> e : a.entrySet()) c.attributes.put(String.valueOf(e.getKey()), StoryState.num(e.getValue(), 0));
        }
        return c;
    }
}

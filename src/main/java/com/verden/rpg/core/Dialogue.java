package com.verden.rpg.core;

import java.util.List;
import java.util.Map;

/**
 * Grafo de diálogo (dado, não código). Cada nó tem falante, texto (com variantes condicionais,
 * que permitem reagir a escolhas e memórias anteriores), efeitos de entrada e, ou opções do
 * jogador, ou ramos automáticos ({@code branches}), ou {@code next} direto.
 */
public record Dialogue(String id, String start, Map<String, Node> nodes) {

    public record Node(String id, String speaker, String mood, String text, List<Variant> variants,
                       List<String> onEnter, List<Option> options, List<Branch> branches, String next) {}

    /** Texto alternativo usado quando as condições valem (a primeira variante que casar vence). */
    public record Variant(List<String> cond, String text) {}

    public record Option(String text, List<String> cond, List<String> effects, String next) {}

    public record Branch(List<String> cond, String next) {}
}

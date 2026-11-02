package com.verden.rpg.core;

import java.util.List;
import java.util.Map;

/**
 * Definição estática de um personagem (carregada de JSON): quem ele é, como aparece e o que sabe fazer.
 * Adicionar um personagem novo = adicionar um JSON; nenhuma classe precisa mudar.
 *
 * @param playable   pode ser controlado pelo jogador (Trinity, Marie; outros só se o livro sustentar)
 * @param unlock     condições para poder ser controlado (vazio = sempre)
 * @param model      id do modelo/rig usado pela camada cliente
 * @param animations id do conjunto de animações
 * @param abilities  habilidades base (ids), independentes da árvore de progressão
 */
public record CharacterDef(String id, String name, boolean playable, List<String> unlock, String model,
                           String animations, String portrait, String defaultOutfit, List<Outfit> outfits,
                           List<String> abilities, Map<String, Integer> baseAttributes, String bookNote) {

    /**
     * Aparência. {@code canon=true} = descrita no livro; false = alternativa criada para o jogo.
     * {@code requires} são as condições de desbloqueio (história, missões, conquistas).
     */
    public record Outfit(String id, String name, String skin, boolean canon, List<String> requires, String description) {}

    public Outfit outfit(String outfitId) {
        for (Outfit o : outfits) if (o.id().equals(outfitId)) return o;
        return null;
    }
}

package com.verden.rpg.net;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/**
 * Protocolo cliente↔servidor do RPG. Três pacotes, todos com um texto (JSON ou comando curto):
 *  - {@link State}    S2C: retrato da campanha (HUD, diário, personagens, toasts, pontos de interação);
 *  - {@link Dialogue} S2C: estado do diálogo aberto (ou {"open":false});
 *  - {@link Action}   C2S: ação do jogador ("choose:N", "advance", "close", "switch:ID", "start", "restart", "sync").
 * O servidor é a única autoridade: o cliente só desenha e pede ações.
 */
public final class RpgNet {
    private RpgNet() {}

    public record State(String json) implements CustomPayload {
        public static final Id<State> ID = new Id<>(Identifier.of("verden", "rpg_state"));
        public static final PacketCodec<RegistryByteBuf, State> CODEC = PacketCodecs.STRING.xmap(State::new, State::json).cast();

        @Override public Id<? extends CustomPayload> getId() { return ID; }
    }

    public record Dialogue(String json) implements CustomPayload {
        public static final Id<Dialogue> ID = new Id<>(Identifier.of("verden", "rpg_dialogue"));
        public static final PacketCodec<RegistryByteBuf, Dialogue> CODEC = PacketCodecs.STRING.xmap(Dialogue::new, Dialogue::json).cast();

        @Override public Id<? extends CustomPayload> getId() { return ID; }
    }

    /** Direção de cena S2C: begin/end/fade/letterbox/shot/line/title/music/stopmusic. */
    public record Cine(String json) implements CustomPayload {
        public static final Id<Cine> ID = new Id<>(Identifier.of("verden", "rpg_cine"));
        public static final PacketCodec<RegistryByteBuf, Cine> CODEC = PacketCodecs.STRING.xmap(Cine::new, Cine::json).cast();

        @Override public Id<? extends CustomPayload> getId() { return ID; }
    }

    public record Action(String cmd) implements CustomPayload {
        public static final Id<Action> ID = new Id<>(Identifier.of("verden", "rpg_action"));
        public static final PacketCodec<RegistryByteBuf, Action> CODEC = PacketCodecs.STRING.xmap(Action::new, Action::cmd).cast();

        @Override public Id<? extends CustomPayload> getId() { return ID; }
    }

    /** Registra os tipos (deve rodar no cliente E no servidor, no initializer comum). */
    public static void registerTypes() {
        PayloadTypeRegistry.playS2C().register(State.ID, State.CODEC);
        PayloadTypeRegistry.playS2C().register(Dialogue.ID, Dialogue.CODEC);
        PayloadTypeRegistry.playS2C().register(Cine.ID, Cine.CODEC);
        PayloadTypeRegistry.playC2S().register(Action.ID, Action.CODEC);
    }
}

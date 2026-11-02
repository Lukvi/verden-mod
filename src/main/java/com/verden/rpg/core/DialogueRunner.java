package com.verden.rpg.core;

import java.util.ArrayList;
import java.util.List;

/**
 * Executa um {@link Dialogue}. A interface (tela de diálogo, câmera) só precisa de
 * {@link #speaker()}, {@link #text()}, {@link #options()}, {@link #choose(int)} e {@link #advance()}.
 * Ao terminar, dispara o evento {@code dialogue:<id>}, que as missões podem usar como objetivo.
 */
public final class DialogueRunner {
    private final Session session;
    private final Dialogue dialogue;
    private Dialogue.Node node;
    private boolean finished;

    public DialogueRunner(Session session, Dialogue dialogue) {
        this.session = session;
        this.dialogue = dialogue;
        goTo(dialogue.start());
    }

    public boolean finished() { return finished; }

    public String speaker() { return node == null ? null : node.speaker(); }

    public String mood() { return node == null ? null : node.mood(); }

    /** Texto do nó atual, já resolvendo variantes condicionais. */
    public String text() {
        if (node == null) return "";
        for (Dialogue.Variant v : node.variants()) if (session.eval(v.cond())) return v.text();
        return node.text();
    }

    /** Opções visíveis agora (as com condição falsa ficam ocultas). */
    public List<Dialogue.Option> options() {
        List<Dialogue.Option> out = new ArrayList<>();
        if (node != null) for (Dialogue.Option o : node.options()) if (session.eval(o.cond())) out.add(o);
        return out;
    }

    public void choose(int visibleIndex) {
        if (finished) throw new IllegalStateException("diálogo já terminou");
        List<Dialogue.Option> opts = options();
        if (visibleIndex < 0 || visibleIndex >= opts.size()) throw new IllegalArgumentException("opção inválida: " + visibleIndex);
        Dialogue.Option o = opts.get(visibleIndex);
        session.apply(o.effects());
        goTo(o.next());
    }

    /** Avança um nó sem opções (o jogador "continua"). */
    public void advance() {
        if (finished) return;
        if (!options().isEmpty()) throw new IllegalStateException("este nó exige escolha");
        autoNext();
    }

    private void autoNext() {
        for (Dialogue.Branch b : node.branches()) {
            if (session.eval(b.cond())) { goTo(b.next()); return; }
        }
        goTo(node.next());
    }

    private void goTo(String id) {
        int guard = 0;
        while (true) {
            if (id == null || id.isEmpty()) { finish(); return; }
            if (guard++ > 500) throw new IllegalStateException("laço infinito no diálogo " + dialogue.id());
            Dialogue.Node n = dialogue.nodes().get(id);
            if (n == null) throw new IllegalStateException("nó inexistente: " + id + " em " + dialogue.id());
            node = n;
            session.apply(n.onEnter());
            // nós "silenciosos" (sem texto e sem opções) apenas roteiam
            boolean silent = n.text().isEmpty() && n.variants().isEmpty() && n.options().isEmpty();
            if (!silent) return;
            String next = n.next();
            for (Dialogue.Branch b : n.branches()) if (session.eval(b.cond())) { next = b.next(); break; }
            id = next;
        }
    }

    private void finish() {
        finished = true;
        node = null;
        session.fire(new GameEvent("dialogue", dialogue.id()));
    }
}

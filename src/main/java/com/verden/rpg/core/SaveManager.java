package com.verden.rpg.core;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Salvamento do estado da campanha, desenhado para NUNCA perder progresso:
 *  1. escreve em arquivo temporário e força o flush no disco (fsync);
 *  2. rotaciona backups (.bak1 → .bak2 → .bak3) antes de substituir o principal;
 *  3. troca o arquivo principal de forma atômica (rename);
 *  4. grava checksum SHA-256 do estado — leitura detecta truncamento/corrupção;
 *  5. ao carregar, se o principal estiver inválido, tenta os backups em ordem;
 *  6. save de versão MAIS NOVA que o código é recusado (nunca sobrescrito por engano).
 */
public final class SaveManager {
    private SaveManager() {}

    public static final int BACKUPS = 3;

    public record LoadResult(StoryState state, String source, List<String> warnings) {
        public boolean recovered() { return !source.equals("main"); }
    }

    public static void save(Path file, StoryState s) throws IOException {
        Path dir = file.toAbsolutePath().getParent();
        Files.createDirectories(dir);
        Map<String, Object> state = s.toMap();
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("schema", StoryState.SCHEMA);
        root.put("savedAtEpochMs", System.currentTimeMillis());
        root.put("checksum", checksum(state));
        root.put("state", state);
        byte[] bytes = Json.write(root).getBytes(StandardCharsets.UTF_8);

        Path tmp = file.resolveSibling(file.getFileName() + ".tmp");
        try (FileChannel ch = FileChannel.open(tmp, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
            ch.write(java.nio.ByteBuffer.wrap(bytes));
            ch.force(true);
        }
        // só rotaciona se o principal atual for legível — não propaga lixo para os backups
        if (Files.exists(file) && readValid(file) != null) rotate(file);
        try {
            Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static void rotate(Path file) throws IOException {
        for (int i = BACKUPS; i >= 2; i--) {
            Path from = bak(file, i - 1);
            if (Files.exists(from)) Files.move(from, bak(file, i), StandardCopyOption.REPLACE_EXISTING);
        }
        Files.copy(file, bak(file, 1), StandardCopyOption.REPLACE_EXISTING);
    }

    private static Path bak(Path file, int n) {
        return file.resolveSibling(file.getFileName() + ".bak" + n);
    }

    /** Devolve null se não existir nenhum save utilizável (jogo novo). */
    public static LoadResult load(Path file) {
        List<String> warnings = new ArrayList<>();
        List<Path> order = new ArrayList<>();
        order.add(file);
        for (int i = 1; i <= BACKUPS; i++) order.add(bak(file, i));
        for (int i = 0; i < order.size(); i++) {
            Path p = order.get(i);
            if (!Files.exists(p)) continue;
            try {
                StoryState st = readValid(p);
                if (st != null) {
                    String src = i == 0 ? "main" : "bak" + i;
                    if (i > 0) warnings.add("save principal inválido; recuperado de " + p.getFileName());
                    return new LoadResult(st, src, warnings);
                }
                warnings.add("arquivo inválido: " + p.getFileName());
            } catch (SchemaTooNewException e) {
                throw e; // não tenta backups mais antigos: o jogador deve atualizar o mod
            }
        }
        return null;
    }

    /** O save foi criado por uma versão mais nova do mod. */
    public static final class SchemaTooNewException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public SchemaTooNewException(int found) {
            super("save com schema " + found + " (> " + StoryState.SCHEMA + "): atualize o mod");
        }
    }

    private static StoryState readValid(Path p) {
        try {
            Map<String, Object> root = Json.parseObject(Files.readString(p, StandardCharsets.UTF_8));
            int schema = StoryState.num(root.get("schema"), -1);
            if (schema > StoryState.SCHEMA) throw new SchemaTooNewException(schema);
            if (schema < 1 || !(root.get("state") instanceof Map<?, ?>)) return null;
            @SuppressWarnings("unchecked")
            Map<String, Object> state = (Map<String, Object>) root.get("state");
            if (!checksum(state).equals(root.get("checksum"))) return null;
            return StoryState.fromMap(state);
        } catch (SchemaTooNewException e) {
            throw e;
        } catch (IOException | RuntimeException e) {
            return null;
        }
    }

    static String checksum(Map<String, Object> state) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(Json.write(state).getBytes(StandardCharsets.UTF_8)));
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}

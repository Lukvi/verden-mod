package com.verden.rpg.server;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LanternBlock;
import net.minecraft.block.LecternBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.WallTorchBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

/**
 * Constrói o "Zigurate Mubarak" da Fase 1 (protótipo). Coordenadas LOCAIS: x leste, z sul, ly=0 é a camada do piso
 * (o jogador pisa em ly=1). Planta (simplificada em relação ao livro; documentado em DEVELOPMENT_STATUS):
 * <pre>
 *   z=-16  entrada (norte)      salão do trono z∈[-15,3]  — Nefertari, trono em (0,·,2)
 *   z=4    parede c/ porta oeste x∈[-14,-12]
 *   z=5..6 corredor das 7 tochas (x=-12,-8,-4,0,4,8,12; a 4ª, x=0, abre a passagem em x∈[2,3] z=7)
 *   z=8..15 oeste: biblioteca (x∈[-15,-2], arco em x∈[-11,-9]) | leste: sala secreta de Dan (x∈[2,15])
 *   teto ly=9 = terraço/parapeito, acessível pela escada externa leste (x=17)
 * </pre>
 */
final class ZigurateBuilder {
    private final ServerWorld w;
    private final int ax, ay, az;
    private final BlockPos.Mutable m = new BlockPos.Mutable();

    ZigurateBuilder(ServerWorld w, int ax, int ay, int az) {
        this.w = w;
        this.ax = ax;
        this.ay = ay;
        this.az = az;
    }

    private static BlockState b(Block x) { return x.getDefaultState(); }

    private void set(int x, int y, int z, BlockState s) {
        w.setBlockState(m.set(ax + x, ay + y, az + z), s, 18); // 2 = avisar clientes, 16 = sem atualizar vizinhos
    }

    private void fill(int x1, int y1, int z1, int x2, int y2, int z2, BlockState s) {
        for (int x = Math.min(x1, x2); x <= Math.max(x1, x2); x++)
            for (int y = Math.min(y1, y2); y <= Math.max(y1, y2); y++)
                for (int z = Math.min(z1, z2); z <= Math.max(z1, z2); z++) set(x, y, z, s);
    }

    void build() {
        BlockState sand = b(Blocks.SANDSTONE), cut = b(Blocks.CUT_SANDSTONE), smooth = b(Blocks.SMOOTH_SANDSTONE), air = b(Blocks.AIR);
        for (int cx = (ax - 22) >> 4; cx <= (ax + 22) >> 4; cx++)
            for (int cz = (az - 22) >> 4; cz <= (az + 22) >> 4; cz++) w.getChunk(cx, cz); // garante chunks gerados

        fill(-18, -6, -18, 18, -1, 18, sand);       // fundação (cobre irregularidades do terreno)
        fill(-18, 0, -18, 18, 26, 18, air);         // limpa dunas/vegetação
        fill(-16, 0, -16, 16, 0, 16, smooth);       // piso
        fill(-3, 0, -18, 3, 0, -17, smooth);        // calçada da entrada
        // mosaico do piso do salão
        for (int x = -15; x <= 15; x++)
            for (int z = -15; z <= 3; z++) {
                if (Math.abs(x) <= 1) set(x, 0, z, b(Blocks.BLUE_TERRACOTTA));
                else if ((x + z) % 2 == 0) set(x, 0, z, b(Blocks.ORANGE_TERRACOTTA));
            }
        // casca do primeiro nível + teto/terraço
        fill(-16, 1, -16, 16, 8, 16, cut);
        fill(-15, 1, -15, 15, 8, 15, air);
        fill(-16, 9, -16, 16, 9, 16, sand);
        fill(-2, 1, -16, 2, 4, -16, air);           // portal de entrada (norte)
        // divisórias internas
        fill(-15, 1, 4, 15, 8, 4, cut);
        fill(-14, 1, 4, -12, 4, 4, air);            // porta salão → corredor
        fill(-15, 1, 7, 15, 8, 7, cut);
        fill(-11, 1, 7, -9, 3, 7, air);             // arco do corredor → biblioteca
        fill(-1, 1, 8, 1, 8, 15, cut);              // divisória grossa (3) entre biblioteca e sala secreta
        // as 7 tochas do corredor (apontam para o norte, presas à parede z=7)
        BlockState torch = b(Blocks.WALL_TORCH).with(WallTorchBlock.FACING, Direction.NORTH);
        for (int x = -12; x <= 12; x += 4) set(x, 3, 6, torch);
        // iluminação
        BlockState lantern = b(Blocks.LANTERN).with(LanternBlock.HANGING, true);
        int[][] ls = {{-8, -12}, {8, -12}, {-8, -5}, {8, -5}, {-8, 1}, {8, 1}, {0, -8}, {-8, 5}, {8, 5}, {-8, 10}, {-8, 13}, {8, 10}, {8, 13}};
        for (int[] l : ls) set(l[0], 8, l[1], lantern);
        // pilares do salão
        for (int px : new int[]{-9, 9}) for (int pz : new int[]{-12, -6, 0}) fill(px, 1, pz, px, 8, pz, cut);
        // trono e relevo (mosaico de Lys) na parede sul do salão
        set(0, 1, 2, b(Blocks.SANDSTONE_STAIRS).with(StairsBlock.FACING, Direction.NORTH));
        fill(0, 1, 3, 0, 3, 3, b(Blocks.GOLD_BLOCK));
        for (int x = -4; x <= 4; x++)
            for (int y = 4; y <= 8; y++) {
                Block c = (Math.abs(x) + y) % 3 == 0 ? Blocks.YELLOW_TERRACOTTA : ((Math.abs(x) + y) % 2 == 0 ? Blocks.LIGHT_BLUE_TERRACOTTA : Blocks.WHITE_TERRACOTTA);
                set(x, y, 3, b(c));
            }
        // sala secreta de Dan (x∈[2,15]): caderno, esboços, grimório (estudo) e estante
        BlockState lect = b(Blocks.LECTERN).with(LecternBlock.FACING, Direction.NORTH);
        set(4, 1, 14, lect);                        // caderno de Lena
        set(9, 1, 14, lect);                        // estudo do grimório
        set(13, 1, 10, b(Blocks.CARTOGRAPHY_TABLE));// esboços de Dan
        fill(14, 1, 13, 14, 2, 14, b(Blocks.BOOKSHELF)); // inclui (14,1,14) = página das bolhas
        // biblioteca (x∈[-15,-2]): estantes e o quadro
        fill(-15, 1, 9, -15, 4, 14, b(Blocks.BOOKSHELF));      // inclui (-15,2,10) = livro de moda
        fill(-2, 2, 9, -2, 5, 13, b(Blocks.GOLD_BLOCK));       // moldura
        for (int z = 10; z <= 12; z++)
            for (int y = 3; y <= 4; y++) set(-2, y, z, b((z + y) % 2 == 0 ? Blocks.ORANGE_TERRACOTTA : Blocks.LIGHT_BLUE_TERRACOTTA));
        // escada externa leste (estilo zigurate), parapeito e degrau superior
        for (int i = 0; i <= 8; i++) {
            int z = 14 - i, ly = 1 + i;
            fill(17, 0, z, 17, ly - 1, z, sand);
            set(17, ly, z, b(Blocks.SANDSTONE_STAIRS).with(StairsBlock.FACING, Direction.NORTH));
        }
        for (int i = -16; i <= 16; i++) {
            set(i, 10, -16, cut);
            set(i, 10, 16, cut);
            set(-16, 10, i, cut);
            if (i < 5 || i > 7) set(16, 10, i, cut); // vão do parapeito onde a escada chega
        }
        fill(-8, 10, -8, 8, 14, 8, cut);
        fill(-3, 15, -3, 3, 17, 3, sand);
        for (int[] l : new int[][]{{12, -9}, {12, 9}, {-12, -9}, {-12, 9}}) set(l[0], 10, l[1], b(Blocks.LANTERN));
    }
}

package com.verden.item;

import com.verden.entity.NpcRole;
import com.verden.entity.VerdenNpcEntity;
import com.verden.registry.ModEntities;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/** "Ovo" de NPC: cria o personagem escolhido no bloco clicado. */
public class NpcSpawnItem extends Item {
    private final NpcRole role;

    public NpcSpawnItem(NpcRole role, Settings settings) {
        super(settings);
        this.role = role;
    }

    @Override
    public Text getName(net.minecraft.item.ItemStack stack) {
        return Text.translatable("item.verden.npc_spawn", Text.translatable("npc.verden." + role.id + ".name"));
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext ctx) {
        World w = ctx.getWorld();
        if (!(w instanceof ServerWorld sw)) {
            return ActionResult.SUCCESS;
        }
        BlockPos pos = ctx.getBlockPos().offset(ctx.getSide());
        VerdenNpcEntity e = ModEntities.NPC.create(sw);
        if (e == null) {
            return ActionResult.FAIL;
        }
        PlayerEntity p = ctx.getPlayer();
        e.setVariant(role.ordinal());
        e.refreshPositionAndAngles(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, p != null ? p.getYaw() + 180f : 0f, 0f);
        sw.spawnEntity(e);
        if (p == null || !p.isCreative()) {
            ctx.getStack().decrement(1);
        }
        return ActionResult.CONSUME;
    }
}

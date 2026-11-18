package com.advancedposturing.decofirmacraft.common.item;

import com.advancedposturing.decofirmacraft.common.block.DFCBlocks;
import com.advancedposturing.decofirmacraft.common.block.PouredConcreteBlock;
import com.advancedposturing.decofirmacraft.common.block.state.DFCBlockStateProperties;
import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.util.Helpers;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class ConcreteLayerItem extends Item
{
    public ConcreteLayerItem(Properties properties)
    {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context)
    {
        final Level level = context.getLevel();
        final BlockPos pos = context.getClickedPos();
        final BlockState state = level.getBlockState(pos);
        final BlockPos newPos = pos.relative(context.getClickedFace());
        final boolean shifting = context.getPlayer().isShiftKeyDown();
        if (state.getBlock() instanceof PouredConcreteBlock)
        {
            final int height = state.getValue(DFCBlockStateProperties.LEVEL);
            if (height < 4)
            {
                level.setBlockAndUpdate(pos, state.setValue(DFCBlockStateProperties.LEVEL, shifting ? 4 : height + 1));
                return InteractionResult.SUCCESS;
            }
            else
            {
                level.setBlockAndUpdate(newPos, DFCBlocks.POURED_CONCRETE.get().defaultBlockState().setValue(DFCBlockStateProperties.LEVEL, shifting ? 4 : 1));
                return InteractionResult.SUCCESS;
            }
        }
        else if (Helpers.isBlock(level.getBlockState(pos), TFCTags.Blocks.SINGLE_BLOCK_REPLACEABLE) || level.getBlockState(pos).is(Blocks.CAVE_AIR))
        {
            level.setBlockAndUpdate(pos, DFCBlocks.POURED_CONCRETE.get().defaultBlockState().setValue(DFCBlockStateProperties.LEVEL, shifting ? 4 : 1));
            return InteractionResult.SUCCESS;
        }
        else if (level.getBlockState(newPos).is(Blocks.AIR) || Helpers.isBlock(level.getBlockState(newPos), TFCTags.Blocks.SINGLE_BLOCK_REPLACEABLE) || level.getBlockState(newPos).is(Blocks.CAVE_AIR))
        {
            level.setBlockAndUpdate(newPos, DFCBlocks.POURED_CONCRETE.get().defaultBlockState().setValue(DFCBlockStateProperties.LEVEL, shifting ? 4 : 1));
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
}

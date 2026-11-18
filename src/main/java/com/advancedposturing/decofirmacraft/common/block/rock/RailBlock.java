package com.advancedposturing.decofirmacraft.common.block.rock;

import com.advancedposturing.decofirmacraft.util.DFCTags;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.state.BlockState;

public class RailBlock extends FenceBlock {
    public RailBlock(Properties properties) {super(properties);}

    private boolean isSameFence(BlockState state) {
        return  state.is(DFCTags.Blocks.ROCK_RAILS);
    }
}

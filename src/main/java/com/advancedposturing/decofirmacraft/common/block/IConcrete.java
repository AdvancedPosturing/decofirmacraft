package com.advancedposturing.decofirmacraft.common.block;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public interface IConcrete
{
    default boolean canFlow(BlockState state, Direction flowDirection)
    {
        return true;
    }
}

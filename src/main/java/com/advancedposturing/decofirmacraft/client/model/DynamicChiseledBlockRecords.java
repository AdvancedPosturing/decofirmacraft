package com.advancedposturing.decofirmacraft.client.model;

import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelProperty;

public class DynamicChiseledBlockRecords
{
    public record ChiseledChunkData(
            int[] chunkMap)
    {
        public static final ModelProperty<ChiseledChunkData> PROPERTY = new ModelProperty<>();
    }

    //todo - not the blockpos, just an int of its randomness or something
    // determine int range by multiplying all unique blockstate randomness counts, then get the remainder of the
    // random number generated from the blockPos and that
    public record ChunkSet(int[] chunkArray, int rand)
    {
    }

    public record BlockStateWithRand(BlockState state, int rand)
    {
    }
}

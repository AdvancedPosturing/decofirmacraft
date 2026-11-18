package com.advancedposturing.decofirmacraft.common;

import com.advancedposturing.decofirmacraft.DecoFirmaCraft;
import com.advancedposturing.decofirmacraft.util.DynamicTextureDataMap;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;

public final class DecoFirmaCraftDataMaps
{
    public static final DataMapType<Block, DynamicTextureDataMap> CHISELED_BLOCK = DataMapType.builder(
                    DecoFirmaCraft.location("chiseled_block"), Registries.BLOCK, DynamicTextureDataMap.CODEC).synced(DynamicTextureDataMap.CODEC, true)
            .build();

    public static void registerDataMaps(final RegisterDataMapTypesEvent event)
    {
        event.register(CHISELED_BLOCK);
    }
}

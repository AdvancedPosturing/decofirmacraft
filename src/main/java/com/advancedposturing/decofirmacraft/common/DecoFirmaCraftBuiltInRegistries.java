package com.advancedposturing.decofirmacraft.common;

import com.advancedposturing.decofirmacraft.util.DynamicTextureData;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;

public final class DecoFirmaCraftBuiltInRegistries
{
    public static void registerDatapackRegistries(final DataPackRegistryEvent.NewRegistry event)
    {
        event.dataPackRegistry(DecoFirmaCraftRegistries.DYNAMIC_TEXTURE_DATA, DynamicTextureData.DIRECT_CODEC,
                DynamicTextureData.DIRECT_CODEC);
    }
}

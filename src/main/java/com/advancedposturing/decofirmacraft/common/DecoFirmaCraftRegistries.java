package com.advancedposturing.decofirmacraft.common;

import com.advancedposturing.decofirmacraft.DecoFirmaCraft;
import com.advancedposturing.decofirmacraft.util.DynamicTextureData;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public class DecoFirmaCraftRegistries
{
    public static final ResourceKey<net.minecraft.core.Registry<DynamicTextureData>> DYNAMIC_TEXTURE_DATA =
            createRegistryKey("dynamic_textures");

    private static <T> ResourceKey<Registry<T>> createRegistryKey(final String name)
    {
        return ResourceKey.createRegistryKey(DecoFirmaCraft.location(name));
    }
}

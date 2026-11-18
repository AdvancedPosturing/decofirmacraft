package com.advancedposturing.decofirmacraft.util;

import com.advancedposturing.decofirmacraft.common.block.IModular;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;

public record DynamicTextureDataMap(Block block, Holder<DynamicTextureData> dynamicTextureData)
{
    private static final Codec<Block> DYNAMIC_BLOCK_CODEC = BuiltInRegistries.BLOCK.byNameCodec()
            .validate(block -> block instanceof IModular ? DataResult.success(block) : DataResult.error(
                    () -> BuiltInRegistries.BLOCK.getKey(block) + " is not a Dynamic Block"));

    public static final Codec<DynamicTextureDataMap> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(DYNAMIC_BLOCK_CODEC.fieldOf("block").forGetter(DynamicTextureDataMap::block),
                            DynamicTextureData.CODEC.fieldOf("dynamic_textures").forGetter(DynamicTextureDataMap::dynamicTextureData))
                    .apply(instance, DynamicTextureDataMap::new));

    public DynamicTextureData getDynamicTextureData() {
        return this.dynamicTextureData.value();
    }
}

package com.advancedposturing.decofirmacraft.mixin.client.accessors;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.WeightedBakedModel;
import net.minecraft.util.random.WeightedEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(WeightedBakedModel.class)
public interface WeightedBakedModelMixin
{
    @Accessor
    List<WeightedEntry.Wrapper<BakedModel>> getList();
}

package com.advancedposturing.decofirmacraft.mixin;

import com.advancedposturing.decofirmacraft.common.player.DFCPlayerInfo;
import com.advancedposturing.decofirmacraft.common.player.DFCPlayerBridge;
import com.advancedposturing.decofirmacraft.common.player.IDFCPlayerInfo;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class DFCPlayerMixin extends Entity implements DFCPlayerBridge
{
    @Unique
    private DFCPlayerInfo dfc$dfcPlayerInfo;

    private DFCPlayerMixin(EntityType<?> entityType, Level level)
    {
        super(entityType, level);
    }

    @NotNull
    @Override
    public IDFCPlayerInfo dfc$getPlayerInfo()
    {
        return dfc$dfcPlayerInfo;
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void addPlayerInfo(CallbackInfo ci)
    {
        this.dfc$dfcPlayerInfo = new DFCPlayerInfo((Player) (Object) this);
    }
}

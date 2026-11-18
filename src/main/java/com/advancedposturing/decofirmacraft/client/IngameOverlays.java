package com.advancedposturing.decofirmacraft.client;

import com.advancedposturing.decofirmacraft.common.player.IDFCPlayerInfo;
import com.advancedposturing.decofirmacraft.util.DFCHelpers;
import com.advancedposturing.decofirmacraft.util.DFCTags;
import com.mojang.blaze3d.vertex.PoseStack;
import net.dries007.tfc.client.ClientHelpers;
import net.dries007.tfc.util.Helpers;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;

import java.util.Locale;

public enum IngameOverlays
{
    TROWEL(IngameOverlays::renderTrowelMode);

    private final ResourceLocation id;
    final LayeredDraw.Layer overlay;

    IngameOverlays(LayeredDraw.Layer overlay)
    {
        this.id = DFCHelpers.resourceLocation(name().toLowerCase(Locale.ROOT));
        this.overlay = overlay;
    }

    public static final ResourceLocation TEXTURE = DFCHelpers.identifier("textures/gui/icons/overlay.png");

    public static void registerOverlays(RegisterGuiLayersEvent event)
    {
        top(event, TROWEL);
    }

    private static void top(RegisterGuiLayersEvent event, IngameOverlays overlay)
    {
        event.registerAboveAll(overlay.id, overlay.overlay);
    }

    private static void renderTrowelMode(GuiGraphics graphics, DeltaTracker delta)
    {
        final PoseStack stack = graphics.pose();
        final Minecraft mc = Minecraft.getInstance();
        if (setup(graphics, mc))
        {
            final Player player = ClientHelpers.getPlayer();
            if (player != null && Helpers.isItem(player.getItemInHand(InteractionHand.MAIN_HAND), DFCTags.Items.TOOLS_TROWELS))
            {
                stack.pushPose();
                // Delegate to the trowel mode
                IDFCPlayerInfo.get(player).trowelMode().createHotbarIcon((texture, u, v) -> graphics.blit(texture, graphics.guiWidth() / 2 + 100, graphics.guiHeight() - 21, u, v, 20, 20));
                stack.popPose();
            }
        }
    }

    public static boolean setup(GuiGraphics gui, Minecraft minecraft)
    {
        if (!minecraft.options.hideGui && minecraft.getCameraEntity() instanceof Player)
        {
            return true;
        }
        return false;
    }
}

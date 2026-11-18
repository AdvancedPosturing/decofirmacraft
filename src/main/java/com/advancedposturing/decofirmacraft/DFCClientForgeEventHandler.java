package com.advancedposturing.decofirmacraft;

import com.advancedposturing.decofirmacraft.common.block.DFCBlocks;
import com.advancedposturing.decofirmacraft.common.block.IContinuouslyInteractable;
import com.advancedposturing.decofirmacraft.common.block.PouredConcreteBlock;
import com.advancedposturing.decofirmacraft.common.block.metal.DFCExtendedMetal;
import com.advancedposturing.decofirmacraft.common.block.state.DFCBlockStateProperties;
import com.advancedposturing.decofirmacraft.network.CycleTrowelModePacket;
import com.advancedposturing.decofirmacraft.network.StartContinuousUsePacket;
import com.advancedposturing.decofirmacraft.network.StopContinuousUsePacket;
import com.advancedposturing.decofirmacraft.util.DFCHelpers;
import net.dries007.tfc.client.TFCKeyBindings;
import net.dries007.tfc.client.render.blockentity.PlacedItemBlockEntityRenderer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

public final class DFCClientForgeEventHandler {

	public static void init(IEventBus modBus) {
		final IEventBus bus = NeoForge.EVENT_BUS;

		bus.register(DFCClientForgeEventHandler.class);
		bus.addListener(DFCClientForgeEventHandler::onClientPlayerLoggedOut);
		bus.addListener(DFCClientForgeEventHandler::onClientTick);
		modBus.addListener(DFCClientForgeEventHandler::addPlacedModel);
		bus.addListener(DFCClientForgeEventHandler::onKeyEvent);
		bus.addListener(DFCClientForgeEventHandler::onFogColor);
		bus.addListener(DFCClientForgeEventHandler::onFog);
	}

	public static void onKeyEvent(InputEvent.Key event)
	{
		if (TFCKeyBindings.CYCLE_CHISEL_MODE.isDown())
		{
			PacketDistributor.sendToServer(CycleTrowelModePacket.PACKET);
		}
	}

	@SubscribeEvent
	public static void onClientPlayerLoggedOut(ClientPlayerNetworkEvent.LoggingOut event)
	{
		if (currentHit != null)
		{
			PacketDistributor.sendToServer(new StopContinuousUsePacket(currentHit));
			currentHit = null;
		}
	}

	@Nullable
	private static BlockPos currentHit = null;

	@SubscribeEvent
	public static void onClientTick(ClientTickEvent.Post event)
	{
		Minecraft mc = Minecraft.getInstance();
		@Nullable
		final Level level = mc.level;

		if (level != null)
		{
			LocalPlayer player = mc.player;


			if (player != null)
			{
				boolean using = mc.options.keyUse.isDown();

				if (using)
				{
					HitResult hit = mc.hitResult;

					if (hit instanceof BlockHitResult blockHit)
					{
						BlockPos hitPos = blockHit.getBlockPos();

						if (level.getBlockEntity(hitPos) instanceof IContinuouslyInteractable interactable)
						{
							if (interactable.canInteract(player))
							{
								if (currentHit == null)
								{
									PacketDistributor.sendToServer(new StartContinuousUsePacket(hitPos));
									currentHit = hitPos;
								}
								if (currentHit != null && !currentHit.equals(hitPos))
								{
									PacketDistributor.sendToServer(new StopContinuousUsePacket(currentHit));

									currentHit = hitPos;
									PacketDistributor.sendToServer(new StartContinuousUsePacket(currentHit));
								}
								return;
							}
						}
					}
				}
				if (currentHit != null)
				{
					PacketDistributor.sendToServer(new StopContinuousUsePacket(currentHit));
					currentHit = null;
				}
			}
		}
	}

	public synchronized static void addPlacedModel(FMLClientSetupEvent event)
	{
		for(DFCExtendedMetal metal : DFCExtendedMetal.values())
		{
			if (DFCBlocks.DFC_METAL_BLOCKS.get(metal).get(DFCExtendedMetal.DFCMetalBlockType.GOBLET) != null)
			{
				PlacedItemBlockEntityRenderer.MODELS.put(DFCBlocks.DFC_METAL_BLOCKS.get(metal).get(DFCExtendedMetal.DFCMetalBlockType.GOBLET).asItem(), cutout("block/metal/goblet/" + metal.getSerializedName()));
			}
		}
	}

	private static PlacedItemBlockEntityRenderer.Provider translucent(String model)
	{
		return new PlacedItemBlockEntityRenderer.Provider(ModelResourceLocation.standalone(DFCHelpers.identifier(model)), RenderType.translucent());
	}

	private static PlacedItemBlockEntityRenderer.Provider cutout(String model)
	{
		return new PlacedItemBlockEntityRenderer.Provider(ModelResourceLocation.standalone(DFCHelpers.identifier(model)), RenderType.cutout());
	}
	@SubscribeEvent
	public static void onFogColor(ViewportEvent.ComputeFogColor  event)
	{
		final Camera camera = event.getCamera();
		final BlockState state = camera.getBlockAtCamera();
		if (state.getBlock() instanceof PouredConcreteBlock &&
				state.getValue(DFCBlockStateProperties.LEVEL) > 1)
		{
			final BlockPos pos = camera.getBlockPosition();
			boolean clipping = false;
			for (AABB boundingBox : state.getShape(camera.getEntity().level(), pos).toAabbs())
			{
				if (boundingBox.contains(camera.getPosition().subtract(pos.getX(), pos.getY(), pos.getZ())))
				{
					clipping = true;
					break;
				}
			}
			if (clipping)
			{
				event.setRed(0.6f);
				event.setGreen(0.6f);
				event.setBlue(0.6f);
			}
		}
	}

	@SubscribeEvent
	public static void onFog(ViewportEvent.RenderFog event)
	{
		final Camera camera = event.getCamera();
		final BlockState state = camera.getBlockAtCamera();
		if (state.getBlock() instanceof PouredConcreteBlock &&
				state.getValue(DFCBlockStateProperties.LEVEL) > 1)
		{
			final BlockPos pos = camera.getBlockPosition();
			boolean clipping = false;
			for (AABB boundingBox : state.getShape(camera.getEntity().level(), pos).toAabbs())
			{
				if (boundingBox.contains(camera.getPosition().subtract(pos.getX(), pos.getY(), pos.getZ())))
				{
					clipping = true;
					break;
				}
			}
			if (clipping)
			{
				event.setNearPlaneDistance(0.5f);
				event.setFarPlaneDistance(2.0f);
				event.setCanceled(true);
			}
		}
	}
}
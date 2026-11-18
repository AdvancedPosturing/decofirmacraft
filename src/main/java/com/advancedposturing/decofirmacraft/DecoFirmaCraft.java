package com.advancedposturing.decofirmacraft;

import com.advancedposturing.decofirmacraft.common.DecoFirmaCraftBuiltInRegistries;
import com.advancedposturing.decofirmacraft.common.DecoFirmaCraftDataMaps;
import com.advancedposturing.decofirmacraft.common.block.DFCBlocks;
import com.advancedposturing.decofirmacraft.common.block.wood.DFCExtendedWood;
import com.advancedposturing.decofirmacraft.common.blockentities.DFCBlockEntities;
import com.advancedposturing.decofirmacraft.common.capabilities.DFCBlockCapabilities;
import com.advancedposturing.decofirmacraft.common.capabilities.DFCItemCapabilities;
import com.advancedposturing.decofirmacraft.common.item.DFCCreativeTabs;
import com.advancedposturing.decofirmacraft.common.item.DFCItems;
import com.advancedposturing.decofirmacraft.common.player.DFCChiselMode;
import com.advancedposturing.decofirmacraft.common.player.TrowelMode;
import com.advancedposturing.decofirmacraft.config.DFCConfig;
import com.advancedposturing.decofirmacraft.network.DFCPackets;
import com.mojang.logging.LogUtils;
import net.dries007.tfc.util.Helpers;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.*;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig.Type;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import net.minecraft.resources.ResourceLocation;

@Mod(DecoFirmaCraft.MOD_ID)
public final class DecoFirmaCraft {

	public static final Logger LOG = LogUtils.getLogger();
	public static final String MOD_ID = "dfc";
	public static final String MOD_NAME = "DecoFirmaCraft";

	private @Nullable Throwable syncLoadError;

	public DecoFirmaCraft(final ModContainer modContainer, final IEventBus modBus, final Dist dist) {

		// You likely don't want all of these.
		modContainer.registerConfig(Type.COMMON, DFCConfig.COMMON.spec());
		modContainer.registerConfig(Type.CLIENT, DFCConfig.CLIENT.spec());
		modContainer.registerConfig(Type.SERVER, DFCConfig.SERVER.spec());
		modContainer.registerConfig(Type.STARTUP, DFCConfig.STARTUP.spec());

		modBus.addListener(this::setup);
		modBus.addListener(DFCItemCapabilities::register);
		modBus.addListener(DFCBlockCapabilities::register);
		modBus.addListener(DFCPackets::setup);
		modBus.addListener(this::registerRegistries);

		modBus.register(DecoFirmaCraft.class);
		DFCItems.ITEMS.register(modBus);
		DFCBlocks.BLOCKS.register(modBus);
		DFCBlockEntities.BLOCK_ENTITIES.register(modBus);
		DFCCreativeTabs.CREATIVE_TABS.register(modBus);

		DFCChiselMode.MODES.register(modBus);
		TrowelMode.MODES.register(modBus);

		DFCAttachments.TYPES.register(modBus);

		modBus.addListener(DecoFirmaCraftBuiltInRegistries::registerDatapackRegistries);
		modBus.addListener(DecoFirmaCraftDataMaps::registerDataMaps);

		DFCForgeEventHandler.init(NeoForge.EVENT_BUS);

		if (FMLEnvironment.dist == Dist.CLIENT)
		{
			ClientEventHandler.init(modContainer, modBus);
			DFCClientForgeEventHandler.init(modBus);
		}
	}

	public void setup(FMLCommonSetupEvent event)
	{
		event.enqueueWork(() -> {
			DFCExtendedWood.registerBlockSetTypes();

//			TFCBlocks.registerFlowerPotFlowers();
		}).exceptionally(e -> {
			LOG.error("An unhandled exception was thrown during synchronous mod loading:", e);
			syncLoadError = e;
			return null;
		});

		TrowelMode.setupOrdering();
		//PatchouliIntegration.registerMultiBlocks();
		//if (JADE) JadeIntegration.registerToolHandlers();
	}

	@SubscribeEvent
	private static void onCreativeTabBuild(final BuildCreativeModeTabContentsEvent event) {
	}

	public void registerRegistries(NewRegistryEvent event)
	{
		event.register(TrowelMode.REGISTRY);
	}

	/**
	 * Shorthand for {@code ResourceLocation.fromNamespaceAndPath(MOD_ID, path)}
	 */
	public static ResourceLocation location(final String path) {
		return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
	}

	/**
	 * Helper for creating modid prepended lang keys
	 */
	public static String lang(final String langKey) {
		return MOD_ID + "." + langKey;
	}

	public void loadComplete(FMLLoadCompleteEvent event)
	{
		if (syncLoadError != null)
		{
			Helpers.throwAsUnchecked(syncLoadError);
		}
	}
}
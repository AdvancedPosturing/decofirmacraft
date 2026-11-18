package com.advancedposturing.decofirmacraft;

import com.advancedposturing.decofirmacraft.common.block.DFCBlocks;
import com.advancedposturing.decofirmacraft.common.block.DynamicChiseledBlock;
import com.advancedposturing.decofirmacraft.common.block.metal.CandleholderBlock;
import com.advancedposturing.decofirmacraft.common.blockentities.CandleholderBlockEntity;
import com.advancedposturing.decofirmacraft.common.blockentities.DynamicChiseledBlockEntity;
import com.advancedposturing.decofirmacraft.common.player.DFCChiselMode;
import com.advancedposturing.decofirmacraft.common.player.IDFCPlayerInfo;
import com.advancedposturing.decofirmacraft.util.DFCTags;
import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.common.player.ChiselMode;
import net.dries007.tfc.common.player.IPlayerInfo;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.events.DouseFireEvent;
import net.dries007.tfc.util.events.StartFireEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.bus.api.*;
import net.neoforged.neoforge.event.entity.player.UseItemOnBlockEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public final class DFCForgeEventHandler {

	public static void init(final IEventBus eventBus) {
		// Register all static @SubscribeEvent annotated event methods
		eventBus.register(DFCForgeEventHandler.class);
		eventBus.addListener(DFCForgeEventHandler::onFireStart);
		eventBus.addListener(DFCForgeEventHandler::onFireStop);
		eventBus.addListener(DFCForgeEventHandler::onUseItemOnBlock);
		eventBus.addListener(DFCForgeEventHandler::firePlayerTickPost);
	}

	@SubscribeEvent
	public static void onFireStart(StartFireEvent event) {
		Level level = event.getLevel();
		BlockPos pos = event.getPos();
		BlockState state = event.getState();
		Block block = state.getBlock();

		if (block instanceof CandleholderBlock)
		{
			if (state.getValue(CandleholderBlock.CANDLES) > 0) {
				level.setBlock(pos, state.setValue(CandleholderBlock.LIT, true), Block.UPDATE_ALL_IMMEDIATE);
				CandleholderBlockEntity.reset(level, pos);
				event.setCanceled(true);
			}
		}
	}

	@SubscribeEvent
	public static void onFireStop(DouseFireEvent event) {
		final Level level = event.getLevel();
		final BlockPos pos = event.getPos();
		final BlockState state = event.getState();
		final Block block = state.getBlock();
		final Player player = event.getPlayer();

		if (state.isAir())
			return;
		if (CandleholderBlock.isLit(state))
		{
			CandleholderBlock.extinguish(null, state, level, pos);
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public static void onUseItemOnBlock(UseItemOnBlockEvent event)
	{
		Player player = event.getPlayer();
		if (player != null)
		{
			final ItemStack itemStack = event.getItemStack();
            if (event.getUsePhase().equals(UseItemOnBlockEvent.UsePhase.ITEM_BEFORE_BLOCK))
                if (Helpers.isItem(itemStack, TFCTags.Items.TOOLS_CHISEL) &&
						Helpers.isItem(player.getOffhandItem(), TFCTags.Items.TOOLS_HAMMER))
                    if (!player.getCooldowns().isOnCooldown(itemStack.getItem()))
					{
                        final UseOnContext context = event.getUseOnContext();
                        final BlockPos pos = context.getClickedPos();
                        final BlockState state = event.getLevel().getBlockState(pos);
                        final ChiselMode mode = IPlayerInfo.get(event.getPlayer()).chiselMode();

                        if ((mode.equals(DFCChiselMode.SMALL_CHISEL.get()) || mode.equals(DFCChiselMode.LARGE_CHISEL.get())) && Helpers.isBlock(state, DFCTags.Blocks.CHISELABLE))
						{
                            final Level level = event.getLevel();
                            final BlockHitResult hit = new BlockHitResult(context.getClickLocation(), context.getClickedFace(), pos, context.isInside());
                            final int[] removedChunks = DFCChiselMode.removeChunks(hit, mode);
                            if (state.getBlock() instanceof DynamicChiseledBlock)
							{
                                final DynamicChiseledBlockEntity chiseledBlockEntity = (DynamicChiseledBlockEntity) level.getBlockEntity(pos);
                                if (chiseledBlockEntity != null)
								{
									chiseledBlockEntity.removeChunks(removedChunks);
								}
                            }
							else
							{
                                level.setBlockAndUpdate(pos, DFCBlocks.DYNAMIC_CHISELED_BLOCK.get().defaultBlockState());
                                final DynamicChiseledBlockEntity newChiseledBlockEntity = (DynamicChiseledBlockEntity) level.getBlockEntity(pos);
								if (newChiseledBlockEntity != null)
								{
									newChiseledBlockEntity.addBlock(state);
									newChiseledBlockEntity.removeChunks(removedChunks);
								}
                            }
                            Helpers.damageItem(event.getItemStack(), event.getPlayer(), InteractionHand.MAIN_HAND);
                            event.getPlayer().getCooldowns().addCooldown(event.getItemStack().getItem(), 5);
                        }
                    }
		}
	}

	@SubscribeEvent
	public static void firePlayerTickPost(PlayerTickEvent.Post event)
	{
		Player player = event.getEntity();

		IDFCPlayerInfo playerInfo = IDFCPlayerInfo.get(player);

		if (playerInfo != null)
		{
			playerInfo.tick(player);
		}
    }

	@SubscribeEvent
	private static void onTick(final LevelTickEvent.Post event) {
//		if (event.getLevel().getGameTime() % 100 != 0) {
//			return;
//		}
//		DecoFirmaCraft.LOG.debug("This is a debug log every 100 ticks! (5 seconds)");
	}
}
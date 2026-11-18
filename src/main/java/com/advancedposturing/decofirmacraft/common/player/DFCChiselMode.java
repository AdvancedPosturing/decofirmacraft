package com.advancedposturing.decofirmacraft.common.player;

import com.advancedposturing.decofirmacraft.DecoFirmaCraft;
import com.advancedposturing.decofirmacraft.common.block.rock.RockRailBlock;
import net.dries007.tfc.common.fluids.FluidHelpers;
import net.dries007.tfc.common.player.ChiselMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public abstract class DFCChiselMode {
    public static final DeferredRegister<ChiselMode> MODES = DeferredRegister.create(ChiselMode.REGISTRY, DecoFirmaCraft.MOD_ID);

    public static final DeferredHolder<ChiselMode, ChiselMode> SMALL_CHISEL = register("small_chisel", new ChiselMode(-100) {
        @Override
        public BlockState modifyStateForPlacement(BlockState state, BlockState chiseled, Player player, BlockHitResult hit)
        {
            return chiseled;
        }


        @Override
        public <T> T createIcon(IconCallback<T> callback)
        {
            return callback.accept(com.advancedposturing.decofirmacraft.client.IngameOverlays.TEXTURE, 20, 0, 20, 20);
        }

        @Override
        public void createHotbarIcon(HotbarIconCallback callback)
        {
            callback.accept(com.advancedposturing.decofirmacraft.client.IngameOverlays.TEXTURE, 0, 0);
        }
    });

    public static final DeferredHolder<ChiselMode, ChiselMode> LARGE_CHISEL = register("large_chisel", new ChiselMode(-50) {
        @Override
        public BlockState modifyStateForPlacement(BlockState state, BlockState chiseled, Player player, BlockHitResult hit)
        {
            return chiseled;
        }


        @Override
        public <T> T createIcon(IconCallback<T> callback)
        {
            return callback.accept(com.advancedposturing.decofirmacraft.client.IngameOverlays.TEXTURE, 20, 0, 20, 20);
        }

        @Override
        public void createHotbarIcon(HotbarIconCallback callback)
        {
            callback.accept(com.advancedposturing.decofirmacraft.client.IngameOverlays.TEXTURE, 100, 0);
        }
    });

    public static final DeferredHolder<ChiselMode, ChiselMode> RAIL = register("rail", new ChiselMode(400) {
        @Override
        public BlockState modifyStateForPlacement(BlockState state, BlockState chiseled, Player player, BlockHitResult hit)
        {
            if (chiseled.getBlock() instanceof RockRailBlock stair)
            {
                //todo - set the block to a rail of the horizontal axis the player is aiming towards
                chiseled = stair.getStateForPlacement(new BlockPlaceContext(player, InteractionHand.MAIN_HAND, new ItemStack(stair), hit));
                if (chiseled != null)
                {
                    chiseled = FluidHelpers.fillWithFluid(chiseled, state.getFluidState().getType());
                }
            }
            return chiseled;
        }


        @Override
        public <T> T createIcon(IconCallback<T> callback)
        {
            return callback.accept(com.advancedposturing.decofirmacraft.client.IngameOverlays.TEXTURE, 20, 0, 20, 20);
        }

        @Override
        public void createHotbarIcon(HotbarIconCallback callback)
        {
            callback.accept(com.advancedposturing.decofirmacraft.client.IngameOverlays.TEXTURE, 80, 0);
        }
    });

    public static int[] removeChunks(BlockHitResult hit, ChiselMode mode)
    {
        final Direction hitFace = hit.getDirection();
        final Vec3 hitLocation = hit.getLocation();
        final BlockPos hitPos = hit.getBlockPos();
        final double x = hitLocation.x() - hitPos.getX();
        final double y = hitLocation.y() - hitPos.getY();
        final double z = hitLocation.z() - hitPos.getZ();

        if (mode.equals(DFCChiselMode.SMALL_CHISEL.get()))
        {
            int chunk = (int) ((Math.floor(x * 4)) + (Math.floor(z * 4) * 4) + (Math.floor(y * 4) * 16));

            if (hitFace.equals(Direction.UP) && y % .25D == 0)
            {
                chunk -= 16;
            }
            if (hitFace.equals(Direction.SOUTH) && z % .25D == 0)
            {
                chunk -= 4;
            }
            if (hitFace.equals(Direction.EAST) && x % .25D == 0)
            {
                chunk -= 1;
            }

            return new int[]{chunk};
        }
        else
        {
            int chunk = (int) ((Math.floor(x * 2)) + (Math.floor(z * 2) * 2) + (Math.floor(y * 2) * 4));

            if (hitFace.equals(Direction.UP) && y % .5D == 0)
            {
                chunk -= 4;
            }
            if (hitFace.equals(Direction.SOUTH) && z % .5D == 0)
            {
                chunk -= 2;
            }
            if (hitFace.equals(Direction.EAST) && x % .5D == 0)
            {
                chunk -= 1;
            }

            final int chunkOrigin = ((chunk % 2 == 0 ? 0 : 2) + (chunk % 4 < 2 ? 0 : 8) + (chunk < 4 ? 0 : 32));
            final int[] chunks = new int[8];

            for (int i = 0; i < 8; i++)
            {
                chunks[i] = chunkOrigin + ((i % 2) + (i % 4 < 2 ? 0 : 4) + (i < 4 ? 0 : 16));
            }


            return chunks;
        }
    }

    private static <T extends ChiselMode> DeferredHolder<ChiselMode, T> register(String name, T mode)
    {
        return MODES.register(name, () -> mode);
    }
}

package com.advancedposturing.decofirmacraft.common.player;

import com.advancedposturing.decofirmacraft.DecoFirmaCraft;
import com.advancedposturing.decofirmacraft.client.IngameOverlays;
import com.advancedposturing.decofirmacraft.common.block.DFCBlocks;
import com.advancedposturing.decofirmacraft.common.block.PouredConcreteBlock;
import com.advancedposturing.decofirmacraft.common.block.state.DFCBlockStateProperties;
import com.advancedposturing.decofirmacraft.common.item.ConcreteMoldItem;
import com.advancedposturing.decofirmacraft.util.DFCHelpers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.RegistryBuilder;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public abstract class TrowelMode
{
    //shamelessly, i copy the chisel mode system. ALL OF IT!
    public static final ResourceKey<Registry<TrowelMode>> KEY = ResourceKey.createRegistryKey(DFCHelpers.identifier("chisel_mode"));
    public static final DefaultedRegistry<TrowelMode> REGISTRY = (DefaultedRegistry<TrowelMode>) new RegistryBuilder<>(KEY)
            .sync(true)
            .defaultKey(DFCHelpers.identifier("smooth"))
            .create();
    public static final DeferredRegister<TrowelMode> MODES = DeferredRegister.create(KEY, DecoFirmaCraft.MOD_ID);

    /**
     * Populates the chisel mode order. These are ordered by priority, then by name, then the {@link #next} field is assigned
     * to each based on the computed order. Addons desiring to register additional chisel modes may change the priority, keeping
     * in mind the existing TFC priorities.
     */
    public static void setupOrdering()
    {
        final List<TrowelMode> order = REGISTRY.entrySet()
                .stream()
                .sorted(Comparator.<Map.Entry<ResourceKey<TrowelMode>, TrowelMode>>comparingInt(e -> e.getValue().priority)
                        .thenComparing(Map.Entry::getKey))
                .map(Map.Entry::getValue)
                .toList();
        for (int i = 0; i < order.size(); i++)
        {
            order.get(i == 0 ? order.size() - 1 : i - 1).next = order.get(i);
        }
    }

    public static final DeferredHolder<TrowelMode, TrowelMode> SMOOTH = register("smooth", new TrowelMode(0)
    {
        @Override
        public boolean modifyState(UseOnContext context)
        {
            //these arent like prompts i told an ai i wrote this before going to sleep so i remembered what to make them do
            //JUST MAKE IT TURN OFF THE DISHEVELED STATE
            final Level level = context.getLevel();
            final BlockPos pos = context.getClickedPos();
            final BlockState state = level.getBlockState(pos);
            final Direction direction = context.getClickedFace();

            if (state.getBlock() instanceof PouredConcreteBlock && state.getValue(DFCBlockStateProperties.DISHEVELED))
            {
                level.setBlockAndUpdate(pos, state.setValue(DFCBlockStateProperties.DISHEVELED, false));
                return true;
            }
            return false;
        }

        @Override
        public <T> T createIcon(IconCallback<T> callback)
        {
            return callback.accept(IngameOverlays.TEXTURE, 20, 0, 20, 20);
        }

        @Override
        public void createHotbarIcon(HotbarIconCallback callback)
        {
            callback.accept(IngameOverlays.TEXTURE, 20, 0);
        }
    });

    public static final DeferredHolder<TrowelMode, TrowelMode> LINE = register("line", new TrowelMode(100)
    {
        @Override
        public boolean modifyState(UseOnContext context)
        {
            //TURN THE CLICKED HORIZONTAL DIRECTION INTO BLOCKSTATE 2 IN THAT DIRECTION. IF CLICKED A HORIZONTAL FACE
            //USE THAT INSTEAD
            final Level level = context.getLevel();
            final BlockPos pos = context.getClickedPos();
            final BlockState state = level.getBlockState(pos);
            final Direction direction = context.getClickedFace();

            if (state.getBlock() instanceof PouredConcreteBlock && !state.getValue(DFCBlockStateProperties.DISHEVELED))
            {
                if (direction == Direction.UP)
                {
                    IntegerProperty lineDirection = DFCHelpers.directionPropertyFromDirection(ConcreteMoldItem.getDirection(context, pos));
                    if (state.getValue(lineDirection) == 0)
                    {
                        level.setBlockAndUpdate(pos, state.setValue(lineDirection, 2));
                        return true;
                    }
                }
                else if (Direction.Plane.HORIZONTAL.test(direction) &&
                        state.getValue(DFCHelpers.directionPropertyFromDirection(direction)) == 0)
                {
                    level.setBlockAndUpdate(pos, state.setValue(DFCHelpers.directionPropertyFromDirection(direction), 2));
                    return true;
                }
            }
            return false;
        }

        @Override
        public <T> T createIcon(IconCallback<T> callback)
        {
            return callback.accept(IngameOverlays.TEXTURE, 20, 0, 20, 20);
        }

        @Override
        public void createHotbarIcon(HotbarIconCallback callback)
        {
            callback.accept(IngameOverlays.TEXTURE, 40, 0);
        }
    });

    public static final DeferredHolder<TrowelMode, TrowelMode> MOVE = register("move", new TrowelMode(200)
    {
        @Override
        public boolean modifyState(UseOnContext context)
        {
            //MOVE 1 LAYER IN THE OPPOSITE DIRECTION OF WHERE THE PLAYER CLICKS. RETURN FALSE IF IT COULDNT MOVE
            //todo - i just realized also make it lay one layer of concrete at a time if you use this with a bucket in the offhand
            //todo - also make it take the moved to blockstates account if it can be placed (if it has a block on top dont move it)
            final Level level = context.getLevel();
            final BlockPos pos = context.getClickedPos();
            final BlockState state = level.getBlockState(pos);
            final Direction direction = context.getClickedFace();

            if (state.getBlock() instanceof PouredConcreteBlock && state.getValue(DFCBlockStateProperties.LEVEL) > 0)
            {
                Direction moveDirection = direction.getOpposite();
                BlockState newState = level.getBlockState(pos.relative(moveDirection));
                if (!Direction.Plane.HORIZONTAL.test(direction))
                {
                    moveDirection = ConcreteMoldItem.getDirection(context, pos).getOpposite();
                    newState = level.getBlockState(pos.relative(moveDirection));
                }

                if (ConcreteMoldItem.canPlace(newState))
                {
                    if (newState.getBlock() instanceof PouredConcreteBlock)
                    {
                        if(newState.getValue(DFCBlockStateProperties.LEVEL) < 4)
                        {
                            level.setBlockAndUpdate(pos.relative(moveDirection), newState.setValue(DFCBlockStateProperties.LEVEL, newState.getValue(DFCBlockStateProperties.LEVEL) + 1).setValue(DFCBlockStateProperties.DISHEVELED, true));
                            if (state.getValue(DFCBlockStateProperties.LEVEL) == 1 && !((PouredConcreteBlock) state.getBlock()).supportsEmpty)
                            {
                                level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                            }
                            else
                            {
                                level.setBlockAndUpdate(pos, state.setValue(DFCBlockStateProperties.DISHEVELED, true).setValue(DFCBlockStateProperties.LEVEL, state.getValue(DFCBlockStateProperties.LEVEL) - 1));
                            }
                            return true;
                        }
                    }
                    else
                    {
                        level.setBlockAndUpdate(pos.relative(moveDirection), DFCBlocks.POURED_CONCRETE.get().defaultBlockState().setValue(DFCBlockStateProperties.LEVEL, 1).setValue(DFCBlockStateProperties.DISHEVELED, true));
                        if (state.getValue(DFCBlockStateProperties.LEVEL) == 1 && !((PouredConcreteBlock) state.getBlock()).supportsEmpty)
                        {
                            level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                        }
                        else
                        {
                            level.setBlockAndUpdate(pos, state.setValue(DFCBlockStateProperties.DISHEVELED, true).setValue(DFCBlockStateProperties.LEVEL, state.getValue(DFCBlockStateProperties.LEVEL) - 1));
                        }
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public <T> T createIcon(IconCallback<T> callback)
        {
            return callback.accept(IngameOverlays.TEXTURE, 20, 0, 20, 20);
        }

        @Override
        public void createHotbarIcon(HotbarIconCallback callback)
        {
            callback.accept(IngameOverlays.TEXTURE, 60, 0);
        }
    });

    private static <T extends TrowelMode> DeferredHolder<TrowelMode, T> register(String name, T mode)
    {
        return MODES.register(name, () -> mode);
    }

    private final int priority;
    private @Nullable TrowelMode next = null; // Initialized by setup()

    protected TrowelMode(int priority)
    {
        this.priority = priority;
    }

    public TrowelMode next()
    {
        return Objects.requireNonNull(next);
    }

    /**
     * When a chisel recipe is being performed, this allows modifying the state placed, based on the chisel mode. It is used in
     * TFC for slabs and stairs, which have contextually-dependent state placement.
     * @param state The original block state
     * @param chiseled The state the recipe wishes to place
     * @param player The player doing the chiseling
     * @param hit The hit of the player targeting the block
     * @return The new chiseled state, or {@code null} if the placement is not possible
     */
    @Nullable
    public abstract boolean modifyState(UseOnContext context);

    /**
     * Used to interact with JEI. Since this is independent of JEI being present, we don't require that the JEI API exists here,
     * however, a chisel mode must be able to create an icon.
     */
    public abstract <T> T createIcon(TrowelMode.IconCallback<T> callback);

    public interface IconCallback<T>
    {
        T accept(ResourceLocation texture, int u, int v, int width, int height);
    }

    /**
     * Used to render an icon for the current chisel mode in the hotbar. This is client-only, and is just forwarded to a
     * {@code graphics.blit} call.
     * @see IngameOverlays#CHISEL
     */
    public abstract void createHotbarIcon(TrowelMode.HotbarIconCallback callback);

    public interface HotbarIconCallback
    {
        void accept(ResourceLocation texture, int u, int v);
    }

}

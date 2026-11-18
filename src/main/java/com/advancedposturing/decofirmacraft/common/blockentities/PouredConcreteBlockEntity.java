package com.advancedposturing.decofirmacraft.common.blockentities;

import com.advancedposturing.decofirmacraft.common.block.DFCBlocks;
import com.advancedposturing.decofirmacraft.common.block.IConcrete;
import com.advancedposturing.decofirmacraft.common.block.PouredConcreteBlock;
import com.advancedposturing.decofirmacraft.common.block.state.DFCBlockStateProperties;
import com.advancedposturing.decofirmacraft.util.DFCHelpers;
import com.google.common.collect.TreeMultimap;
import lombok.Getter;
import lombok.Setter;
import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.common.blockentities.TickableBlockEntity;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.calendar.CalendarTransaction;
import net.dries007.tfc.util.calendar.Calendars;
import net.dries007.tfc.util.calendar.ICalendarTickable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class PouredConcreteBlockEntity extends TickableBlockEntity implements ICalendarTickable
{
    public PouredConcreteBlockEntity(BlockPos pos, BlockState state)
    {
        this(DFCBlockEntities.POURED_CONCRETE.get(), pos, state);
    }

    protected PouredConcreteBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
    }

    private long lastPlayerTick = Integer.MIN_VALUE;

    @Getter
    @Setter
    private long pouredTick = -1;

    @Setter
    private boolean stable = false;

    public static void tick(Level level, BlockPos pos, BlockState state, PouredConcreteBlockEntity concrete)
    {
        concrete.checkForLastTickSync();
        concrete.checkForCalendarUpdate();

        PouredConcreteBlock concreteBlock = state.getBlock() instanceof PouredConcreteBlock ? (PouredConcreteBlock) state.getBlock() : null;

        if (concreteBlock != null)
        {
            long pouredTick = concrete.getPouredTick();
            if (state.getValue(DFCBlockStateProperties.LEVEL) < 1)
            {
                if (concreteBlock.supportsEmpty)
                {
                    if (pouredTick > -1)
                    {
                        concrete.pouredTick = -1;
                    }
                }
                else
                {
                    level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                    return;
                }
            }
            else
            {
                long currentTick = Calendars.get(level).getTicks();
                if (pouredTick == -1)
                {
                    concrete.setPouredTick(currentTick);
                    pouredTick = concrete.getPouredTick() - 1;
                }
                final long timeSince = currentTick - pouredTick;
                if (timeSince >= 14400)
                {
                    concrete.solidify(level, pos, state);
                    return;
                }
                if (!concrete.stable)
                {
                    if (timeSince % 20 == 0)
                    {
                        if (!concrete.attemptFall(level, pos, state))
                        {
                            if (timeSince % 40 == 0)
                            {
                                concrete.attemptSpread(level, pos, state);
                            }
                        }
                    }
                }
            }
        }
    }

    //use to turn concrete into a solid block
    private void solidify(Level level, BlockPos pos, BlockState state)
    {
        PouredConcreteBlock concrete = state.getBlock() instanceof PouredConcreteBlock ? (PouredConcreteBlock) state.getBlock() : null;
        if (concrete != null)
        {
            level.setBlockAndUpdate(pos, concrete.solidified.withPropertiesOf(state));
        }
    }

    private boolean attemptFall(Level level, BlockPos pos, BlockState state)
    {
        PouredConcreteBlock concreteBlock = state.getBlock() instanceof PouredConcreteBlock ? (PouredConcreteBlock) state.getBlock() : null;
        assert concreteBlock != null;
        IConcrete concrete = (IConcrete) state.getBlock();
        final BlockState belowState = level.getBlockState(pos.below());
        //concrete is set as disheveled when moved so you have to plane it again using a trowel
        if (concrete.canFlow(state, Direction.DOWN))
        {
            if (belowState.is(Blocks.AIR) || Helpers.isBlock(belowState, TFCTags.Blocks.SINGLE_BLOCK_REPLACEABLE) || belowState.is(Blocks.CAVE_AIR))
            {
                level.setBlockAndUpdate(pos.below(), DFCBlocks.POURED_CONCRETE.get().defaultBlockState().setValue(DFCBlockStateProperties.LEVEL, state.getValue(DFCBlockStateProperties.LEVEL)).setValue(DFCBlockStateProperties.DISHEVELED, true));
                if (concreteBlock.supportsEmpty)
                {
                    level.setBlockAndUpdate(pos, state.setValue(DFCBlockStateProperties.LEVEL, 0));
                }
                else
                {
                    level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                }
                return true;
            }
            else if (belowState.getBlock() instanceof PouredConcreteBlock && belowState.getValue(DFCBlockStateProperties.LEVEL) < 4)
            {
                final int layers1 = state.getValue(DFCBlockStateProperties.LEVEL);
                final int layers2 = level.getBlockState(pos.below()).getValue(DFCBlockStateProperties.LEVEL);
                final int added = layers1 + layers2;
                level.setBlockAndUpdate(pos.below(), belowState.setValue(DFCBlockStateProperties.LEVEL, Math.min(4, added)).setValue(DFCBlockStateProperties.DISHEVELED, true));
                if (added > 4)
                {
                    level.setBlockAndUpdate(pos, state.setValue(DFCBlockStateProperties.LEVEL, added % 4).setValue(DFCBlockStateProperties.DISHEVELED, true));
                }
                else
                {
                    if (concreteBlock.supportsEmpty)
                    {
                        level.setBlockAndUpdate(pos, state.setValue(DFCBlockStateProperties.LEVEL, 0));
                    }
                    else
                    {
                        //todo - replace with level.destroyBlock()
                        level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                    }
                }
                return true;
            }
        }
        return false;
    }

    private void attemptSpread(Level level, BlockPos pos, BlockState state)
    {
        PouredConcreteBlock concreteBlock = state.getBlock() instanceof PouredConcreteBlock ? (PouredConcreteBlock) state.getBlock() : null;
        assert concreteBlock != null;
        IConcrete concrete = (IConcrete) state.getBlock();

        TreeMultimap<Integer, BlockPos> heightMap = TreeMultimap.create();

        boolean isStable = true;

        for (Direction direction : Direction.Plane.HORIZONTAL)
        {
            if (concrete.canFlow(state, direction))
            {
                final BlockState relativeState = level.getBlockState(pos.relative(direction));
                if (relativeState.is(Blocks.AIR) || Helpers.isBlock(relativeState, TFCTags.Blocks.SINGLE_BLOCK_REPLACEABLE) || relativeState.is(Blocks.CAVE_AIR))
                {
                    final BlockState diagonalState = level.getBlockState(pos.relative(direction).below());
                    if (diagonalState.is(Blocks.AIR) || Helpers.isBlock(diagonalState, TFCTags.Blocks.SINGLE_BLOCK_REPLACEABLE) || diagonalState.is(Blocks.CAVE_AIR))
                    {
                        heightMap.put(-4, pos.relative(direction).below());
                        isStable = false;
                    }
                    else if (diagonalState.getBlock() instanceof PouredConcreteBlock && diagonalState.getValue(DFCBlockStateProperties.LEVEL) < 4)
                    {
                        heightMap.put(diagonalState.getValue(DFCBlockStateProperties.LEVEL) - 4, pos.relative(direction).below());
                        isStable = false;
                    }
                    else
                    {
                        if (state.getValue(DFCBlockStateProperties.LEVEL) > 1)
                        {
                            heightMap.put(0, pos.relative(direction));
                            isStable = false;
                        }
                    }
                }
                else if (relativeState.getBlock() instanceof PouredConcreteBlock)
                {
                    if (relativeState.getValue(DFCBlockStateProperties.LEVEL) < 4 && state.getValue(DFCBlockStateProperties.LEVEL) > relativeState.getValue(DFCBlockStateProperties.LEVEL) + 1)
                    {
                        heightMap.put(relativeState.getValue(DFCBlockStateProperties.LEVEL), pos.relative(direction));
                        isStable = false;
                    }
                }
            }
        }

        if (isStable)
        {
            stable = isStable;
            return;
        }

        int currentHeight = state.getValue(DFCBlockStateProperties.LEVEL);

        for (int height : heightMap.keySet())
        {
            for(BlockPos relativePos : heightMap.get(height))
            {
                int layerHeight = height < 0 ? height + 4 : height;
                if (currentHeight > height + 1 && currentHeight > 0 && concrete.canFlow(state, DFCHelpers.horizontalDirectionFromPos(pos, relativePos)))
                {
                    final BlockState relativeState = level.getBlockState(relativePos);
                    if (relativeState.getBlock() instanceof PouredConcreteBlock)
                    {
                        if (relativePos.getY() != pos.getY() || ((IConcrete) relativeState.getBlock()).canFlow(relativeState, DFCHelpers.horizontalDirectionFromPos(relativePos, pos)))
                        {
                            level.setBlockAndUpdate(relativePos, relativeState.setValue(DFCBlockStateProperties.LEVEL, layerHeight + 1).setValue(DFCBlockStateProperties.DISHEVELED, true));
                            currentHeight--;
                        }
                    }
                    else if (relativeState.is(Blocks.AIR) || Helpers.isBlock(relativeState, TFCTags.Blocks.SINGLE_BLOCK_REPLACEABLE) || relativeState.is(Blocks.CAVE_AIR))
                    {
                        level.setBlockAndUpdate(relativePos, DFCBlocks.POURED_CONCRETE.get().defaultBlockState().setValue(DFCBlockStateProperties.LEVEL, 1).setValue(DFCBlockStateProperties.DISHEVELED, true));
                        currentHeight--;
                    }
                }
            }
        }
        if (currentHeight > 0 && currentHeight != state.getValue(DFCBlockStateProperties.LEVEL))
        {
            level.setBlockAndUpdate(pos, state.setValue(DFCBlockStateProperties.LEVEL, currentHeight).setValue(DFCBlockStateProperties.DISHEVELED, true));
        }
        else if (currentHeight == 0)
        {
            if (concreteBlock.supportsEmpty)
            {
                level.setBlockAndUpdate(pos, state.setValue(DFCBlockStateProperties.LEVEL, 0));
            }
            else
            {
                level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
            }
        }
        else
        {
            stable = true;
        }
    }

    public void resetTimer()
    {
        assert level != null;
        setPouredTick(Calendars.get(level).getTicks());
    }

    @Override
    public void loadAdditional(CompoundTag nbt, HolderLookup.Provider provider)
    {
        pouredTick = nbt.getLong("pouredTick");
        lastPlayerTick = nbt.getLong("lastTick");
        super.loadAdditional(nbt, provider);
    }

    @Override
    public void saveAdditional(CompoundTag nbt, HolderLookup.Provider provider)
    {
        nbt.putLong("pouredTick", pouredTick);
        nbt.putLong("lastTick", lastPlayerTick);
        super.saveAdditional(nbt, provider);
    }

    @Override
    public void onCalendarUpdate(long tick)
    {
        assert level != null;

        if (level.isClientSide)
        {
            return;
        }

        if (pouredTick > -1)
        {
            final long finishTick = pouredTick + 14400;
            if (finishTick <= Calendars.SERVER.getTicks())
            {
                final long offset = Calendars.SERVER.getTicks() - finishTick;
                try (CalendarTransaction tr = Calendars.SERVER.transaction())
                {
                    tr.add(-offset);
                    solidify(level, getBlockPos(), getBlockState());
                }
            }
        }
    }

    @Override
    public long getLastCalendarUpdateTick()
    {
        return lastPlayerTick;
    }

    @Override
    public void setLastCalendarUpdateTick(long tick)
    {
        lastPlayerTick = tick;
    }
}

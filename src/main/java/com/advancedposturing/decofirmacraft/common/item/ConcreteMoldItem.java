package com.advancedposturing.decofirmacraft.common.item;

import com.advancedposturing.decofirmacraft.common.block.DFCBlocks;
import com.advancedposturing.decofirmacraft.common.block.PouredConcreteBlock;
import com.advancedposturing.decofirmacraft.common.block.PouredConcreteBlockMoldBlock;
import com.advancedposturing.decofirmacraft.common.block.state.DFCBlockStateProperties;
import com.advancedposturing.decofirmacraft.util.DFCHelpers;
import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.util.Helpers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class ConcreteMoldItem extends BlockItem
{
    public ConcreteMoldItem(Block block, Properties properties)
    {
        super(block, properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context)
    {
        final Level level = context.getLevel();
        final BlockPos pos = context.getClickedPos();
        final BlockState state = level.getBlockState(pos);
        final BlockPos newPos = pos.relative(context.getClickedFace());
        final boolean shifting = context.getPlayer().isShiftKeyDown();
        if (state.getBlock() instanceof PouredConcreteBlock ||  level.getBlockState(newPos).getBlock() instanceof PouredConcreteBlockMoldBlock)
        {
            BlockPos usePos = pos;
            BlockState useState = state;
            if (!(state.getBlock() instanceof PouredConcreteBlock))
            {
                usePos = newPos;
                useState = level.getBlockState(newPos);
            }
            if (shifting)
            {
                //if its a mold with a floor and you place on its top side, place a down facing mold in that direction
                if (context.getClickedFace().equals(Direction.UP) && (useState.getBlock() instanceof PouredConcreteBlockMoldBlock && useState.getValue(BlockStateProperties.DOWN)))
                {
                    Direction placementDirection = getDirection(context, pos);
                    final BlockState newState = level.getBlockState(usePos.relative(placementDirection));

                    if (canPlace(newState))
                    {
                        setConcreteBlock(level, usePos.relative(placementDirection), Direction.DOWN, newState);
                        return InteractionResult.SUCCESS;
                    }
                    return InteractionResult.FAIL;
                }
                //if its the bottom quarter of a mold wall, place the floor mold
                else if (context.getClickLocation().y - context.getClickedPos().getY() < .25 && (useState.getBlock() instanceof PouredConcreteBlockMoldBlock && !useState.getValue(BlockStateProperties.DOWN)))
                {
                    setConcreteBlock(level, usePos, Direction.DOWN, useState);
                }
                else
                {
                    //if its the top of a regular block, try to place a floor mold else just get the side its on
                    Direction placementSide;
                    if (context.getClickedFace().equals(Direction.UP))
                    {
                        if (!(useState.getBlock() instanceof PouredConcreteBlock))
                        {
                            placementSide = Direction.DOWN;
                        }
                        else
                        {
                            placementSide = getDirection(context, pos);
                        }
                    }
                    //if its not the top use the side you clicked instead
                    else
                    {
                        placementSide = context.getClickedFace();
                    }

                    //if the block is a mold and doesnt have a panel on that side, add one
                    if (useState.getBlock() instanceof PouredConcreteBlockMoldBlock)
                    {
                        if ((!placementSide.equals(Direction.DOWN) && !(useState.getValue(DFCHelpers.directionPropertyFromDirection(placementSide)) == 1) || (placementSide.equals(Direction.DOWN) && !useState.getValue(BlockStateProperties.DOWN))))
                        {
                            setConcreteBlock(level, usePos, placementSide, useState);
                        }
                    }
                    //otherwise try to place a panel on the block adjacent to the direction you clicked
                    else
                    {
                        Direction placementDirection2 = context.getClickedFace();
                        BlockState newState = level.getBlockState(pos.relative(placementDirection2));

                        if (canPlace(newState))
                        {
                            setConcreteBlock(level, pos.relative(placementDirection2), placementSide, newState);
                            return InteractionResult.SUCCESS;
                        }
                        return InteractionResult.FAIL;
                    }
                }
            }
            else
            {
                Direction placementSide = getDirection(context, pos);
                //if rightclicking the top face try to place a panel in its closest direction
                if (context.getClickedFace().equals(Direction.UP))
                {

                    if ((useState.getBlock() instanceof PouredConcreteBlockMoldBlock) && useState.getValue(DFCHelpers.directionPropertyFromDirection(placementSide)) != 1)
                    {
                        setConcreteBlock(level, usePos, placementSide, useState);
                        return InteractionResult.SUCCESS;
                    }
                    return InteractionResult.FAIL;
                }
                else
                {
                    //otherwise try to place a panel on top of the closest direction. if there already is one,
                    //place it on top of that. only tries up to 5 blocks
                    boolean validSpot = false;

                    for (int i = 0; i < 5; i++)
                    {
                        if ((useState.getBlock() instanceof PouredConcreteBlockMoldBlock) && useState.getValue(DFCHelpers.directionPropertyFromDirection(placementSide)) == 1)
                        {
                            useState = level.getBlockState(usePos.above());
                            usePos = usePos.above();
                        }
                        else if (canPlace(useState))
                        {
                            validSpot = true;
                            break;
                        }
                    }

                    if (validSpot)
                    {
                        setConcreteBlock(level, usePos, placementSide, useState);
                    }
                    else
                    {
                        return InteractionResult.FAIL;
                    }
                }
            }
            return InteractionResult.SUCCESS;
        }
        //otherwise if the block is replaceable or can be placed on
        else if (Helpers.isBlock(level.getBlockState(pos), TFCTags.Blocks.SINGLE_BLOCK_REPLACEABLE) || level.getBlockState(newPos).is(Blocks.AIR) || Helpers.isBlock(level.getBlockState(newPos), TFCTags.Blocks.SINGLE_BLOCK_REPLACEABLE))
        {
            //get the pos to use
            BlockPos usePos = pos;
            if (!Helpers.isBlock(level.getBlockState(pos), TFCTags.Blocks.SINGLE_BLOCK_REPLACEABLE))
            {
                if (canPlace(level.getBlockState(newPos)))
                {
                    usePos = newPos;
                }
            }
            if (shifting)
            {
                level.setBlockAndUpdate(usePos, DFCBlocks.POURED_CONCRETE_BLOCK_MOLD.get().defaultBlockState()
                        .setValue(BlockStateProperties.DOWN, true)
                        .setValue(DFCBlockStateProperties.DISHEVELED, true));
            }
            else
            {
                Direction placementSide = getDirection(context, pos);

                setConcreteBlock(level, usePos, placementSide, state);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    public static Direction getDirection(UseOnContext context, BlockPos pos) {
        final double x1 = pos.getX() - context.getClickLocation().x();
        final double z1 = pos.getZ() - context.getClickLocation().z();
        final double x2 = x1 < 0 ? x1 + 1 : x1;
        final double z2 = z1 < 0 ? z1 + 1 : z1;
        if (x2 == 0 || z2 == 0)
        {
            return context.getClickedFace();
        }
        return Math.abs(x2 - .5) > Math.abs(z2 - .5) ? x2 > .5 ? Direction.WEST : Direction.EAST : z2 > .5 ? Direction.NORTH : Direction.SOUTH;
    }

    public static void setConcreteBlock(Level level, BlockPos pos, Direction direction, BlockState state)
    {
        if (state.getBlock() instanceof PouredConcreteBlock)
        {
            if (direction == Direction.DOWN)
            {
                //if the selected direction is down, set all direction values that are 2 to 0
                level.setBlockAndUpdate(pos, DFCBlocks.POURED_CONCRETE_BLOCK_MOLD.get().defaultBlockState().getBlock()
                        .withPropertiesOf(state)
                        .setValue(BlockStateProperties.DOWN, true)
                        .setValue(DFCBlockStateProperties.NORTH, state.getValue(DFCBlockStateProperties.NORTH) == 1 ? 1 : 0)
                        .setValue(DFCBlockStateProperties.EAST, state.getValue(DFCBlockStateProperties.EAST) == 1 ? 1 : 0)
                        .setValue(DFCBlockStateProperties.SOUTH, state.getValue(DFCBlockStateProperties.SOUTH) == 1 ? 1 : 0)
                        .setValue(DFCBlockStateProperties.WEST, state.getValue(DFCBlockStateProperties.WEST) == 1 ? 1 : 0)
                        .setValue(DFCBlockStateProperties.DISHEVELED, true));
            }
            else
            {
                //else if the direction is horizontal, still set all values of 2 to 0 but set the given direction to 1
                level.setBlockAndUpdate(pos, DFCBlocks.POURED_CONCRETE_BLOCK_MOLD.get().defaultBlockState().getBlock()
                        .withPropertiesOf(state)
                        .setValue(DFCBlockStateProperties.NORTH,
                                DFCHelpers.directionPropertyFromDirection(direction).equals(DFCBlockStateProperties.NORTH) ?
                                        1 : state.getValue(DFCBlockStateProperties.NORTH) == 1 ? 1 : 0)
                        .setValue(DFCBlockStateProperties.EAST,
                                DFCHelpers.directionPropertyFromDirection(direction).equals(DFCBlockStateProperties.EAST) ?
                                        1 : state.getValue(DFCBlockStateProperties.EAST) == 1 ? 1 : 0)
                        .setValue(DFCBlockStateProperties.SOUTH,
                                DFCHelpers.directionPropertyFromDirection(direction).equals(DFCBlockStateProperties.SOUTH) ?
                                        1 : state.getValue(DFCBlockStateProperties.SOUTH) == 1 ? 1 : 0)
                        .setValue(DFCBlockStateProperties.WEST,
                                DFCHelpers.directionPropertyFromDirection(direction).equals(DFCBlockStateProperties.WEST) ?
                                        1 : state.getValue(DFCBlockStateProperties.WEST) == 1 ? 1 : 0)
                        .setValue(DFCBlockStateProperties.DISHEVELED, true));
            }
        }
        //creating a new blank mold so just set the relevant direction as the panel
        else
        {
            if (direction == Direction.DOWN)
            {
                level.setBlockAndUpdate(pos, DFCBlocks.POURED_CONCRETE_BLOCK_MOLD.get().defaultBlockState()
                        .setValue(BlockStateProperties.DOWN, true)
                        .setValue(DFCBlockStateProperties.NORTH, 0)
                        .setValue(DFCBlockStateProperties.EAST, 0)
                        .setValue(DFCBlockStateProperties.SOUTH, 0)
                        .setValue(DFCBlockStateProperties.WEST, 0)
                        .setValue(DFCBlockStateProperties.DISHEVELED, true));
            }
            else
            {
                level.setBlockAndUpdate(pos, DFCBlocks.POURED_CONCRETE_BLOCK_MOLD.get().defaultBlockState()
                        .setValue(BlockStateProperties.DOWN, false)
                        .setValue(DFCBlockStateProperties.NORTH,
                                DFCHelpers.directionPropertyFromDirection(direction).equals(DFCBlockStateProperties.NORTH) ?
                                        1 : 0)
                        .setValue(DFCBlockStateProperties.EAST,
                                DFCHelpers.directionPropertyFromDirection(direction).equals(DFCBlockStateProperties.EAST) ?
                                        1 : 0)
                        .setValue(DFCBlockStateProperties.SOUTH,
                                DFCHelpers.directionPropertyFromDirection(direction).equals(DFCBlockStateProperties.SOUTH) ?
                                        1 : 0)
                        .setValue(DFCBlockStateProperties.WEST,
                                DFCHelpers.directionPropertyFromDirection(direction).equals(DFCBlockStateProperties.WEST) ?
                                        1 : 0)
                        .setValue(DFCBlockStateProperties.DISHEVELED, true));
            }
        }
    }

    public static boolean canPlace(BlockState state)
    {
        return state.getBlock() instanceof PouredConcreteBlock || Helpers.isBlock(state, TFCTags.Blocks.SINGLE_BLOCK_REPLACEABLE) || state.is(Blocks.AIR);
    }
}

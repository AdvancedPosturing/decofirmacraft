package com.advancedposturing.decofirmacraft.common.block;

import com.advancedposturing.decofirmacraft.common.block.state.DFCBlockStateProperties;
import net.dries007.tfc.common.blocks.ExtendedBlock;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;

public class ConcreteBlock extends ExtendedBlock
{
    public static final IntegerProperty LEVEL = DFCBlockStateProperties.LEVEL;
    public static final BooleanProperty DISHEVELED = DFCBlockStateProperties.DISHEVELED;
    public static final IntegerProperty NORTH = DFCBlockStateProperties.NORTH;
    public static final IntegerProperty EAST = DFCBlockStateProperties.EAST;
    public static final IntegerProperty SOUTH = DFCBlockStateProperties.SOUTH;
    public static final IntegerProperty WEST = DFCBlockStateProperties.WEST;
    public static final BooleanProperty DOWN = BlockStateProperties.DOWN;
    public static final List<IntegerProperty> directionProperties = PouredConcreteBlock.directionProperties;

    public ConcreteBlock(ExtendedProperties properties)
    {
        super(properties);
        this.registerDefaultState(this.getStateDefinition().any()
                .setValue(LEVEL, 1)
                .setValue(DISHEVELED, false)
                .setValue(NORTH, 0)
                .setValue(EAST, 0)
                .setValue(SOUTH, 0)
                .setValue(WEST, 0)
                .setValue(DOWN, false));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context)
    {
        return Shapes.box(0, 0, 0, 1, .25 * state.getValue(LEVEL), 1);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(LEVEL, DISHEVELED, NORTH, EAST, SOUTH, WEST, DOWN);
    }
}

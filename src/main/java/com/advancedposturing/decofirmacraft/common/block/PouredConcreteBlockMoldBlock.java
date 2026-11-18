package com.advancedposturing.decofirmacraft.common.block;

import com.advancedposturing.decofirmacraft.common.block.state.DFCBlockStateProperties;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
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

public class PouredConcreteBlockMoldBlock extends PouredConcreteBlock implements IConcrete
{
    public static final IntegerProperty LEVEL = DFCBlockStateProperties.LEVEL;
    public static final BooleanProperty DISHEVELED = DFCBlockStateProperties.DISHEVELED;
    public static final IntegerProperty NORTH = DFCBlockStateProperties.NORTH;
    public static final IntegerProperty EAST = DFCBlockStateProperties.EAST;
    public static final IntegerProperty SOUTH = DFCBlockStateProperties.SOUTH;
    public static final IntegerProperty WEST = DFCBlockStateProperties.WEST;
    public static final BooleanProperty DOWN = BlockStateProperties.DOWN;
    public static final List<IntegerProperty> directionProperties = PouredConcreteBlock.directionProperties;


    public PouredConcreteBlockMoldBlock(ExtendedProperties properties, Block solidified, boolean supportsEmpty)
    {
        super(properties, solidified, supportsEmpty);
        this.registerDefaultState(this.getStateDefinition().any()
                .setValue(LEVEL, 0)
                .setValue(DISHEVELED, false)
                .setValue(NORTH, 0)
                .setValue(EAST, 0)
                .setValue(SOUTH, 0)
                .setValue(WEST, 0)
                .setValue(DOWN, false)
        );
    }

    public static final VoxelShape northShape = Shapes.box(0, 0, 0, 1, 1, 1/16D);
    public static final VoxelShape eastShape = Shapes.box(15/16D, 0, 0, 1, 1, 1);
    public static final VoxelShape southShape = Shapes.box(0, 0, 15/16D, 1, 1, 1);
    public static final VoxelShape westShape = Shapes.box(0, 0, 0, 1/16D, 1, 1);
    public static final VoxelShape downShape = Shapes.box(0, 0, 0, 1, 1/16D, 1);

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context)
    {
        int north = state.getValue(NORTH);
        int east = state.getValue(EAST);
        int south = state.getValue(SOUTH);
        int west = state.getValue(WEST);
        boolean down = state.getValue(DOWN);
        return Shapes.or(super.getShape(state, world, pos, context),
                north == 1 ? northShape : Shapes.empty(),
                east == 1 ? eastShape : Shapes.empty(),
                south == 1 ? southShape : Shapes.empty(),
                west == 1 ? westShape : Shapes.empty(),
                down ? downShape : Shapes.empty());
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context)
    {
        int north = state.getValue(NORTH);
        int east = state.getValue(EAST);
        int south = state.getValue(SOUTH);
        int west = state.getValue(WEST);
        boolean down = state.getValue(DOWN);
        return Shapes.or(
                north == 1 ? northShape : Shapes.empty(),
                east == 1 ? eastShape : Shapes.empty(),
                south == 1 ? southShape : Shapes.empty(),
                west == 1 ? westShape : Shapes.empty(),
                down ? downShape : Shapes.empty());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
    }

    @Override
    public boolean canFlow(BlockState state, Direction flowDirection)
    {
        return switch (flowDirection)
        {
            case NORTH -> state.getValue(NORTH) != 1;
            case EAST -> state.getValue(EAST) != 1;
            case SOUTH -> state.getValue(SOUTH) != 1;
            case WEST -> state.getValue(WEST) != 1;
            case DOWN -> !state.getValue(DOWN);
            default -> true;
        };
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston)
    {
        boolean exists = false;
        for (IntegerProperty property : directionProperties)
        {
            if (state.getValue(property) == 1)
            {
                exists = true;
                break;
            }
            //dumb ass shit because DOWN is now an outlier
            if (property == WEST)
            {
                if (state.getValue(DOWN))
                {
                    exists = true;
                    break;
                }
            }
        }
        if (!exists)
        {
            level.setBlockAndUpdate(pos, DFCBlocks.POURED_CONCRETE.get().withPropertiesOf(state));
        }
        super.onPlace(state, level, pos, oldState, movedByPiston);
    }
}

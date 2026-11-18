package com.advancedposturing.decofirmacraft.common.block;

import com.advancedposturing.decofirmacraft.common.block.state.DFCBlockStateProperties;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.minecraft.core.BlockPos;
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

public class ConcreteBlockMoldBlock extends ConcreteBlock
{
    public static final IntegerProperty LEVEL = DFCBlockStateProperties.LEVEL;
    //0 = no panel/rough face
    //1 = panel/smooth face
    //2 = no panel but smoothed top edge
    //if 1 and smooth enabled at the same time itll get the top edge anyways
    public static final IntegerProperty NORTH = DFCBlockStateProperties.NORTH;
    public static final IntegerProperty EAST = DFCBlockStateProperties.EAST;
    public static final IntegerProperty SOUTH = DFCBlockStateProperties.SOUTH;
    public static final IntegerProperty WEST = DFCBlockStateProperties.WEST;
    public static final BooleanProperty DOWN = BlockStateProperties.DOWN;
    public static final BooleanProperty DISHEVELED = DFCBlockStateProperties.DISHEVELED;
    public static final List<IntegerProperty> directionProperties = PouredConcreteBlock.directionProperties;


    public ConcreteBlockMoldBlock(ExtendedProperties properties)
    {
        super(properties);
        this.registerDefaultState(this.getStateDefinition().any()
                .setValue(LEVEL, 1)
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
        return Shapes.or(Shapes.box(0, 0, 0, 1, .25 * state.getValue(LEVEL), 1),
                north == 1? northShape : Shapes.empty(),
                east == 1? eastShape : Shapes.empty(),
                south == 1? southShape : Shapes.empty(),
                west == 1? westShape : Shapes.empty(),
                down ? downShape : Shapes.empty());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
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
            level.setBlockAndUpdate(pos, DFCBlocks.CONCRETE.get().withPropertiesOf(state));
        }
        super.onPlace(state, level, pos, oldState, movedByPiston);
    }
}

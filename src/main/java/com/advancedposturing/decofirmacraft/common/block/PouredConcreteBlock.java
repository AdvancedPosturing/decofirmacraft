package com.advancedposturing.decofirmacraft.common.block;

import com.advancedposturing.decofirmacraft.common.block.state.DFCBlockStateProperties;
import com.advancedposturing.decofirmacraft.common.blockentities.PouredConcreteBlockEntity;
import com.google.common.collect.Lists;
import net.dries007.tfc.common.blocks.EntityBlockExtension;
import net.dries007.tfc.common.blocks.ExtendedBlock;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;

public class PouredConcreteBlock extends ExtendedBlock implements EntityBlockExtension, IConcrete
{
    public static final IntegerProperty LEVEL = DFCBlockStateProperties.LEVEL;
    public static final BooleanProperty DISHEVELED = DFCBlockStateProperties.DISHEVELED;
    //0 = no panel/rough face
    //1 = panel/smooth face
    //2 = no panel but smoothed top edge
    //if 1 and smooth enabled at the same time itll get the top edge anyways
    public static final IntegerProperty NORTH = DFCBlockStateProperties.NORTH;
    public static final IntegerProperty EAST = DFCBlockStateProperties.EAST;
    public static final IntegerProperty SOUTH = DFCBlockStateProperties.SOUTH;
    public static final IntegerProperty WEST = DFCBlockStateProperties.WEST;
    public static final BooleanProperty DOWN = BlockStateProperties.DOWN;
    public static final List<IntegerProperty> directionProperties = List.of(NORTH, EAST, SOUTH, WEST);

    public final Block solidified;
    //todo - im dumb i shouldve made this rely on #canSurvive
    public final boolean supportsEmpty;

    public PouredConcreteBlock(ExtendedProperties properties, Block solidified, boolean supportsEmpty)
    {
        super(properties);
        this.registerDefaultState(this.getStateDefinition().any()
                .setValue(LEVEL, 1)
                .setValue(DISHEVELED, true)
                .setValue(NORTH, 0)
                .setValue(EAST, 0)
                .setValue(SOUTH, 0)
                .setValue(WEST, 0)
                .setValue(DOWN, false));
        this.solidified = solidified;
        this.supportsEmpty = supportsEmpty;
    }

    @Override
    protected boolean skipRendering(BlockState state, BlockState adjacentState, Direction direction)
    {
        return adjacentState.is(this) ? true : super.skipRendering(state, adjacentState, direction);
    }

    @Override
    protected VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos)
    {
        return getCollisionShape(state, level, pos, CollisionContext.empty());
    }

    @Override
    protected VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context)
    {
        return getCollisionShape(state, level, pos, context);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context)
    {
        return Shapes.box(0, 0, 0, 1, .25 * state.getValue(LEVEL), 1);
    }

    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context)
    {
        return Shapes.empty();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(LEVEL, DISHEVELED, NORTH, EAST, SOUTH, WEST, DOWN);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston)
    {
        //todo - make if a block is placed on top it turns disheveled
        if (neighborPos.getY() - pos.getY() < 1)
        {
            PouredConcreteBlockEntity concrete = (PouredConcreteBlockEntity) level.getBlockEntity(pos);
            if (concrete != null)
            {
                concrete.setStable(false);
            }
        }
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston)
    {
        PouredConcreteBlockEntity concrete = (PouredConcreteBlockEntity) level.getBlockEntity(pos);
        if (concrete != null)
        {
            concrete.setStable(false);
            if(state.getValue(LEVEL) > 0 && (oldState.getBlock() instanceof PouredConcreteBlock) && oldState.getValue(LEVEL) != state.getValue(LEVEL))
            {
                concrete.resetTimer();
            }
        }
        if (state.getValue(LEVEL) == 0 && !this.supportsEmpty)
        {
            level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
        }
        //dumb as fuck and probably laggy too for large pours
        List<IntegerProperty> propertyList = Lists.newArrayList();
        if (state.getValue(DISHEVELED))
        {
            for (IntegerProperty property : directionProperties)
            {
                if (state.getValue(property) == 2)
                {
                    propertyList.add(property);
                }
            }
        }
        for (IntegerProperty property : propertyList)
        {
            level.setBlockAndUpdate(pos, DFCBlocks.CONCRETE.get().withPropertiesOf(state).setValue(property, 0));
        }
        super.onPlace(state, level, pos, oldState, movedByPiston);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random)
    {
        PouredConcreteBlockEntity concrete = (PouredConcreteBlockEntity) level.getBlockEntity(pos);
        if (concrete != null)
        {
            concrete.setStable(false);
        }
        super.randomTick(state, level, pos, random);
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity)
    {
        if (!level.isClientSide)
        {
            if (entity instanceof LivingEntity)
            {

                Vec3 eye = entity.getEyePosition();
                VoxelShape shape = state.getShape(level, pos);
                Vec3 localEye = eye.subtract(pos.getX(), pos.getY(), pos.getZ());
                final AABB entityBox = entity.getBoundingBox().move(-pos.getX(), -pos.getY(), -pos.getZ());

                boolean clipping1 = false;
                boolean clipping2 = false;
                for (AABB boundingBox : shape.toAabbs())
                {
                    if (boundingBox.contains(localEye))
                    {
                        clipping1 = true;
                    }
                    if (!state.getValue(DISHEVELED) && boundingBox.intersects(entityBox))
                    {
                        clipping2 = true;
                    }
                    if (clipping1 && clipping2)
                    {
                        break;
                    }
                }
                if (clipping1)
                {
                    entity.setAirSupply(entity.getAirSupply() - 4);
                }
                if (clipping2)
                {
                    level.setBlockAndUpdate(pos, state.setValue(DISHEVELED, true).setValue(NORTH, 0).setValue(EAST, 0).setValue(SOUTH, 0).setValue(WEST, 0).setValue(DOWN, false));
                }
            }
        }
        boolean clipping = false;
        final AABB entityBox = entity.getBoundingBox().move(-pos.getX(), -pos.getY(), -pos.getZ());
        for (AABB boundingBox : state.getShape(level, pos).toAabbs())
        {
            if (boundingBox.intersects(entityBox))
            {
                clipping = true;
                break;
            }
        }
        if (clipping)
        {
            final Vec3 motion = entity.getDeltaMovement();
            entity.setDeltaMovement(motion.multiply(.8, motion.y < 0 ? 1 - 0.2f * (1 - .8) : 1, .8));
        }
    }
}
package com.advancedposturing.decofirmacraft.common.block;

import com.advancedposturing.decofirmacraft.common.blockentities.DynamicChiseledBlockEntity;
import com.advancedposturing.decofirmacraft.util.DFCTags;
import net.dries007.tfc.common.blocks.EntityBlockExtension;
import net.dries007.tfc.common.blocks.ExtendedBlock;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.util.Helpers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class DynamicChiseledBlock extends ExtendedBlock implements EntityBlockExtension, IModular
{
    public DynamicChiseledBlock(ExtendedProperties properties)
    {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new DynamicChiseledBlockEntity(pos, state);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context)
    {
        final DynamicChiseledBlockEntity chiseledBlock = (DynamicChiseledBlockEntity) world.getBlockEntity(pos);

        if (chiseledBlock != null)
        {
            return chiseledBlock.getShape();
        }
        return Shapes.empty();
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston)
    {
        if (!state.canSurvive(level, pos))
        {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL | Block.UPDATE_SUPPRESS_DROPS);
        }
        else
        {
            final DynamicChiseledBlockEntity chiseledBlock = (DynamicChiseledBlockEntity) level.getBlockEntity(pos);
            if(chiseledBlock != null)
            {
                if (chiseledBlock.isMonogomous())
                {
                    final int idValue = chiseledBlock.getStateId(0);
                    level.setBlock(pos, Block.stateById(idValue), Block.UPDATE_ALL);
                }
                else
                {
                    super.onPlace(state, level, pos, oldState, movedByPiston);
                }
            }
            else
            {
                super.onPlace(state, level, pos, oldState, movedByPiston);
            }
        }
    }

    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos)
    {
        if (!canSurvive(state, level, currentPos))
        {
            return Blocks.AIR.defaultBlockState();
        }
        return state;
    }

        @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos)
    {
        DynamicChiseledBlockEntity chiseledBlock = (DynamicChiseledBlockEntity) level.getBlockEntity(pos);
        if (chiseledBlock != null)
        {
            return chiseledBlock.canSurvive();
        }
        return false;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult)
    {
        final DynamicChiseledBlockEntity chiseledBlockEntity = (DynamicChiseledBlockEntity) level.getBlockEntity(pos);
        if (stack.getItem() instanceof BlockItem blockItem && Helpers.isBlock(blockItem.getBlock(), DFCTags.Blocks.CHISELABLE) && chiseledBlockEntity != null)
        {
            double hitOffsetY = hitResult.getLocation().y - pos.getY();
            double hitOffsetX = hitResult.getLocation().x - pos.getX();
            double hitOffsetZ = hitResult.getLocation().z - pos.getZ();

            if(hitOffsetY > 0D && hitOffsetY < 1D &&
                    hitOffsetX > 0D && hitOffsetX < 1D &&
                    hitOffsetZ > 0D && hitOffsetZ < 1D)
            {
                if (chiseledBlockEntity.addBlock(blockItem.getBlock().getStateForPlacement(
                        new BlockPlaceContext(player, hand, stack, hitResult))))
                {
                    if (!player.hasInfiniteMaterials())
                    {
                        stack.shrink(1);
                    }
                    return ItemInteractionResult.SUCCESS;
                }
            }
            else if (level.getBlockState(pos.relative(hitResult.getDirection().getOpposite())).getBlock() instanceof DynamicChiseledBlock)
            {
                final DynamicChiseledBlockEntity newChiseledBlockEntity = (DynamicChiseledBlockEntity) level.getBlockEntity(pos.relative(hitResult.getDirection().getOpposite()));
                if (newChiseledBlockEntity != null)
                {
                    if (newChiseledBlockEntity.addBlock(blockItem.getBlock().getStateForPlacement(
                            new BlockPlaceContext(player, hand, stack, hitResult))))
                    {
                        if (!player.hasInfiniteMaterials())
                        {
                            stack.shrink(1);
                        }
                        return ItemInteractionResult.SUCCESS;

                    }
                }
            }
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }
}

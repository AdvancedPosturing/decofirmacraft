package com.advancedposturing.decofirmacraft.common.blockentities;

import com.advancedposturing.decofirmacraft.client.model.DynamicChiseledBlockRecords;
import lombok.Getter;
import net.dries007.tfc.common.blockentities.TFCBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.client.model.data.ModelData;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DynamicChiseledBlockEntity extends TFCBlockEntity
{
    public static Map<Integer, Map<Integer, VoxelShape>> stateVoxelShapeCache = new ConcurrentHashMap<>();

    public static Map<int[], VoxelShape> voxelShapeCache = new ConcurrentHashMap<>();

    private boolean created = false;

    @Getter
    private int[] stateMap = new int[64];

    public DynamicChiseledBlockEntity(BlockPos pos, BlockState blockState)
    {
        super(DFCBlockEntities.DYNAMIC_CHISELED_BLOCK_ENTITY.get(), pos, blockState);

        //sets up default keys
        for (int i = 0; i < 64; i++)
        {
            this.stateMap[i] = 0;
        }
    }

    public VoxelShape getShape()
    {
        return voxelShapeCache.computeIfAbsent(this.stateMap, this::makeVoxelShape);
    }

    //cant make it static because i need a level in order to get a states voxelshape and i dont want to do all that
    private VoxelShape makeVoxelShape(int[] states)
    {
        List<Integer> uniqueInts = new ArrayList<>();

        Map<Integer, Map<Integer, VoxelShape>> voxelShapes = new HashMap<>();

        VoxelShape shape = Shapes.empty();

        for (int i = 0; i < 64; i++)
        {
            int stateId = states[i];

            if (!uniqueInts.contains(stateId))
            {
                uniqueInts.add(stateId);

                voxelShapes.put(stateId, stateVoxelShapeCache.computeIfAbsent(stateId, p -> produceShapes(Block.stateById(p))));
            }

            shape = Shapes.or(shape, stateId == 0 ? Shapes.empty() : voxelShapes.get(stateId).get(i));
        }

        return shape;
    }

    private Map<Integer, VoxelShape> produceShapes(BlockState state)
    {
        Map<Integer, VoxelShape> shapeMap = new HashMap<>();

        Level level = this.getLevel();
        if (level != null)
        {
            VoxelShape initialShape = state.getShape(level, this.getBlockPos());

            for (int i = 0; i < 64; i++)
            {
                final float minX;
                final float maxX;
                final float minY;
                final float maxY;
                final float minZ;
                final float maxZ;

                //4x4x4 grid
                minX = (i%4)/4f;
                maxX = minX + 4f/16f;
                minY = (Math.floorDiv(i, 16))/4f;
                maxY = minY + 4f/16f;
                minZ = (Math.floorDiv(i%16, 4))/4f;
                maxZ = minZ + 4f/16f;

                AABB chunkShape = Shapes.box(minX, minY, minZ, maxX, maxY, maxZ).bounds();

                VoxelShape currentChunk = Shapes.empty();

                for(AABB shape : initialShape.toAabbs())
                {
                    if (chunkShape.intersects(shape))
                    {
                        final VoxelShape newShape = Shapes.create(shape.intersect(chunkShape));

                        currentChunk = Shapes.or(currentChunk, newShape);
                    }
                }

                shapeMap.put(i, currentChunk);
            }
        }
        return shapeMap;
    }

    public boolean canSurvive()
    {
        if (!this.created)
        {
            System.out.println("DEBUG CREATED = FALSE");
            return true;
        }
        else
        {
            for (int i : this.stateMap)
            {
                if (i != 0)
                {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isMonogomous()
    {
        int initialState = this.stateMap[0];
        if (initialState == 0)
        {
            return false;
        }
        for (int i : stateMap)
        {
            if (i != initialState)
            {
                return false;
            }
        }
        return true;
    }

    public int getStateId(int position)
    {
        return stateMap[position];
    }

    public boolean addBlock(BlockState blockState)
    {
        final int stateId = Block.getId(blockState);
        boolean added = false;

        for (int i = 0; i < 64; i++)
        {
            if (stateMap[i] == 0)
            {
                added = true;
                stateMap[i] = stateId;
            }
        }

        if (added)
        {
            this.requestModelDataUpdate();

            this.created = true;

            Level level = this.getLevel();
            if (level != null)
            {
                level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), Block.UPDATE_ALL);
            }
        }

        return added;
    }

    public void removeChunks(int[] chunks)
    {
        for (int chunk : chunks)
        {
            this.stateMap[chunk] = 0;
        }

        this.created = true;

        Level level = this.getLevel();
        if (level != null)
        {
            level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), Block.UPDATE_ALL);
        }

        this.requestModelDataUpdate();
    }

    @Override
    public ModelData getModelData()
    {
        return ModelData.of(DynamicChiseledBlockRecords.ChiseledChunkData.PROPERTY,
                new DynamicChiseledBlockRecords.ChiseledChunkData(this.stateMap));
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider)
    {
        super.loadAdditional(tag, provider);
        this.stateMap = tag.getIntArray("chunk map");
        this.created = true;
        this.requestModelDataUpdate();
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider provider)
    {
        super.saveAdditional(tag, provider);
        tag.putIntArray("chunk map", this.stateMap);
    }
}

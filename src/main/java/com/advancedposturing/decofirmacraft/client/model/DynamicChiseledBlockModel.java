package com.advancedposturing.decofirmacraft.client.model;

import com.advancedposturing.decofirmacraft.DecoFirmaCraft;
import com.advancedposturing.decofirmacraft.mixin.client.accessors.WeightedBakedModelMixin;
import com.advancedposturing.decofirmacraft.util.FakeRandomSource;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import lombok.Getter;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.FaceInfo;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.RenderTypeGroup;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.client.model.EmptyModel;
import net.neoforged.neoforge.client.model.IDynamicBakedModel;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import net.neoforged.neoforge.client.model.geometry.IGeometryLoader;
import net.neoforged.neoforge.client.model.geometry.IUnbakedGeometry;
import org.jetbrains.annotations.Nullable;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@Slf4j
public class DynamicChiseledBlockModel implements IUnbakedGeometry<DynamicChiseledBlockModel>
{
    public static final ResourceLocation LOADER_ID = DecoFirmaCraft.location("dynamic_chiseled");

    public static final IGeometryLoader<DynamicChiseledBlockModel> LOADER = DynamicChiseledBlockModel::deserialize;

    public DynamicChiseledBlockModel()
    {
    }

    //todo - the model is irrelevant. it might as well just be empty and have the "modelloader" set to this
    public static DynamicChiseledBlockModel deserialize(final JsonObject jsonObject,
                                                        final JsonDeserializationContext context) throws JsonParseException
    {
        return new DynamicChiseledBlockModel();
    }

    @Override
    public BakedModel bake(final IGeometryBakingContext context, final ModelBaker baker,
                           final Function<Material, TextureAtlasSprite> spriteGetter, final ModelState modelState,
                           final ItemOverrides overrides)
    {
        return new BakedDynamicChiseledModel();
    }

    //todo - thinking of just directly cutting this out and applying it to the blockentity directly instead
    protected static class BakedDynamicChiseledModel extends BakedModelWrapper<BakedModel> implements IDynamicBakedModel
    {
        //keyed to the array of ints that stores what blockstates every chunk in a block is made of
        //this stores a processed modeldata that contains a total weight. if an entry in that object doesnt exist for
        // the requested rand value, itll bake a new one and store it within itself, as a second layer of the int map
        private final Map<DynamicChiseledBlockRecords.ChiseledChunkData, WeightedChunkMap> chunkCache = new ConcurrentHashMap<>();

        private final Map<Integer, WeightedBlockStateQuads> statesCache = new ConcurrentHashMap<>();

        public BakedDynamicChiseledModel()
        {
            super(EmptyModel.BAKED);
        }


        public boolean useAmbientOcclusion() {
            return this.originalModel.useAmbientOcclusion();
        }


        private BakedModel getModel(DynamicChiseledBlockRecords.ChiseledChunkData property, @Nullable RandomSource random)
        {
            //Todo - damn i need a way to store the chunks randomness without storing it as a blockpos
            // maybe the multiple of all its constituent states random models? then just store that number from 0 - that
            return this.chunkCache.computeIfAbsent(property, p -> new WeightedChunkMap(this, p)).getModel(random);
        }

        //this is fed a 64 int array and a position (as a long) to determine the model to derive from.
        //the model is derived from cached quads corresponding to the 64 chunks of the blockstate and the random value
        //if that blockstate hasnt been split into 64 lists of quads, do that separately. also produce 8 2x2x2 chunks
        //additionally. that will be used if a corner chunk is monogomous in the returned baked model
        private BakedModel bake(DynamicChiseledBlockRecords.ChunkSet chunkSet)
        {
            Map<Integer, WeightedBlockStateQuads> statesUsed = new HashMap<>();

            final int rand = chunkSet.rand();

            int[] chunkMap = chunkSet.chunkArray();
            Map<Integer, Integer> usedIDs = new HashMap<>();

            boolean[] yHalfSlabs = new boolean[2];
            boolean[] xHalfSlabs = new boolean[2];
            boolean[] zHalfSlabs = new boolean[2];
            boolean[] largeChunks = new boolean[8];

            for (int i = 0; i < 64; i++)
            {
                int yHalfSlab = i < 32 ? 0 : 1;
                int xHalfSlab = i % 4 < 2 ? 0 : 1;
                int zHalfSlab = i % 16 < 8 ? 0 : 1;
                int chunk = (i % 4 < 2 ? 0 : 1) + (i % 16 < 8 ? 0 : 2) + (i < 32 ? 0 : 4);

                //todo - THIS IS BROKEN AND DOESNT WORK CORRECTLY SOMETIMES
                //theres probably a way more elegant way to check this stuff
                //if this larger chunk hasnt already been marked
                if (!yHalfSlabs[yHalfSlab])
                {
                    //and if the blockstate id at the current chunk isnt the same as the corner chunks id
                    if (chunkMap[yHalfSlab == 0 ? 0 : 32] != chunkMap[i])
                    {
                        //mark the chunk
                        log.debug("Y HALF SLAB INVALID {}", yHalfSlab);
                        yHalfSlabs[yHalfSlab] = true;
                    }
                }
                if (!xHalfSlabs[xHalfSlab])
                {
                    if (chunkMap[xHalfSlab == 0 ? 0 : 2] != chunkMap[i])
                    {
                        log.debug("X HALF SLAB INVALID {}", xHalfSlab);
                        xHalfSlabs[xHalfSlab] = true;
                    }
                }
                if (!zHalfSlabs[zHalfSlab])
                {
                    if (chunkMap[zHalfSlab == 0 ? 0 : 8] != chunkMap[i])
                    {
                        log.debug("Z HALF SLAB INVALID {}", zHalfSlab);
                        zHalfSlabs[zHalfSlab] = true;
                    }
                }
                if (!largeChunks[chunk])
                {
                    //and if the blockstate id at the current chunk isnt the same as the chunks corner id
                    if (chunkMap[(chunk % 2 == 0 ? 0 : 2) + (chunk % 4 < 2 ? 0 : 8) + (chunk < 4 ? 0 : 32)] != chunkMap[i])
                    {
                        log.debug("LARGE CHUNK INVALID {}", chunk);
                        //mark the chunk
                        largeChunks[chunk] = true;
                    }
                }

                //if the blockstate in the map isnt air and isnt already used, add it to the list
                //todo - in here also check the render type group. if its higher than the current one, update it
                // (render type groups go normal - cutout - transparent in lowest to highest order
                if (chunkMap[i] != 0 && !usedIDs.containsKey(chunkMap[i]))
                {
                    //todo - this is stupid it should just be a boolean, or better yet dont store a value at all. oh well
                    usedIDs.put(chunkMap[i], 1);

                    statesUsed.put(chunkMap[i], statesCache.computeIfAbsent(chunkMap[i], id ->
                            new WeightedBlockStateQuads(net.minecraft.world.level.block.Block.stateById(id))));
                }
            }

            List<BakedQuad> bakedQuads = new ArrayList<>();

            //todo - take the neighbors face into account to see if the quad can be culled
            //todo - this will check if each i value is tied to a successful larger chunk, if it is only one corner
            // of that chunk will be used to obtain the quads to add to the list. that list will then be passed to
            // the model bakery
            for (int i = 0; i < 64; i++)
            {
                //0 is air iirc
                if (chunkMap[i] != 0)
                {
                    //simplify the way these values are obtained, maybe just make them a method
                    int yHalfSlab = i < 32 ? 0 : 1;
                    int xHalfSlab = i % 4 < 2 ? 0 : 1;
                    int zHalfSlab = i % 16 < 8 ? 0 : 1;
                    int chunk = (i % 4 < 2 ? 0 : 1) + (i % 16 < 8 ? 0 : 2) + (i < 32 ? 0 : 4);

                    //this if else chain isnt a big deal and is probably the best way to go about this
                    if (!yHalfSlabs[yHalfSlab])
                    {
                        //only add the quads if its the corner of the slab, dont want to go around adding 64 versions of
                        // the same quads
                        if (i == 0 || i == 32)
                        {
                            log.debug("USING VERTICAL SLAB FOR CHUNK {}", i);
                            for (BakedQuadRecord bakedQuadRecord : statesUsed.get(chunkMap[i]).getChunk(rand, 72 + yHalfSlab))
                            {
                                //todo - add logic to check neighboring chunks culling
                                //maybe do a per direction check that if that direction has culling enabled, isnt an edge, and the
                                // relevant quad at its adjacent chunk also has culling enabled, then it doesnt include it?
                                bakedQuads.add(bakedQuadRecord.bakedQuad());
                            }
                        }
                    }
                    //has to check the other half slab also isnt there since otherwise it could overlap and in that case
                    // i want it to just use the 2x2x2 chunk instead
                    else if (!xHalfSlabs[xHalfSlab] && yHalfSlabs[yHalfSlab == 0 ? 1 : 0])
                    {
                        if (i == 0 || i == 2)
                        {
                            log.debug("USING X SLAB FOR CHUNK {}", i);
                            for (BakedQuadRecord bakedQuadRecord : statesUsed.get(chunkMap[i]).getChunk(rand, 74 + xHalfSlab))
                            {
                                //todo - add logic to check neighboring chunks culling
                                bakedQuads.add(bakedQuadRecord.bakedQuad());
                            }
                        }
                    }
                    else if (!zHalfSlabs[xHalfSlab] && yHalfSlabs[yHalfSlab == 0 ? 1 : 0])
                    {
                        if (i == 0 || i == 8)
                        {
                            log.debug("USING Z SLAB FOR CHUNK {}", i);
                            for (BakedQuadRecord bakedQuadRecord : statesUsed.get(chunkMap[i]).getChunk(rand, 76 + zHalfSlab))
                            {
                                //todo - add logic to check neighboring chunks culling
                                bakedQuads.add(bakedQuadRecord.bakedQuad());
                            }
                        }
                    }
                    else if (!largeChunks[chunk])
                    {
                        //weird check maybe improve? probably just not a big deal
                        if (i % 32 < 16 && i % 8 < 4 && i % 2 == 0)
                        {
                            log.debug("USING LARGE CHUNK FOR CHUNK {}", i);
                            for (BakedQuadRecord bakedQuadRecord : statesUsed.get(chunkMap[i]).getChunk(rand, 64 + chunk))
                            {
                                //todo - add logic to check neighboring chunks culling
                                //this check will be weird. for the 2x2x2 chunks itll have to check either if all 4 of
                                // the neighboring sides have a culled face or if its a 2x2x2 chunk or half slab if
                                // it just has a culled face. seems really annoying and probably expensive
                                // to check this on every single chunk, but the savings are 100% worthwhile
                                bakedQuads.add(bakedQuadRecord.bakedQuad());
                            }
                        }
                    }
                    else
                    {
                        log.debug("USING SMALL CHUNK FOR CHUNK {}", i);
                        for (BakedQuadRecord bakedQuadRecord : statesUsed.get(chunkMap[i]).getChunk(rand, i))
                        {
                            //todo - add logic to check neighboring chunks culling
                            //this will be identical to the 2x2x2 check except i dont have to do anything special
                            bakedQuads.add(bakedQuadRecord.bakedQuad());
                        }
                    }
                }
            }

            SimpleBakedModel.Builder builder = new SimpleBakedModel.Builder(true, true, true, ItemTransforms.NO_TRANSFORMS, ItemOverrides.EMPTY);

            builder.particle(Minecraft.getInstance()
                    .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                    .apply(MissingTextureAtlasSprite.getLocation()));

            for (BakedQuad bakedQuad : bakedQuads)
            {
                builder.addUnculledFace(bakedQuad);
            }

            // todo - make the render type group use the highest one from all the states available
            return builder.build(RenderTypeGroup.EMPTY);
        }

        @Override
        public List<BakedQuad> getQuads(final @Nullable BlockState state, final @Nullable Direction side,
                                        final RandomSource random, final ModelData data, final @Nullable RenderType renderType)
        {
            final DynamicChiseledBlockRecords.ChiseledChunkData property = data.get(DynamicChiseledBlockRecords.ChiseledChunkData.PROPERTY);
            if (property != null)
            {
                return this.getModel(property, random).getQuads(state, side, random, data, renderType);
            }
            return super.getQuads(state, side, random, data, renderType);
        }

        @Override
        public TextureAtlasSprite getParticleIcon(final ModelData data)
        {
            final DynamicChiseledBlockRecords.ChiseledChunkData property = data.get(DynamicChiseledBlockRecords.ChiseledChunkData.PROPERTY);
            if (property != null)
            {
                return this.getModel(property, null).getParticleIcon(data);
            }
            return super.getParticleIcon();
        }

        private DynamicTextureRecords.DynamicTextureSet getWeightedItem(List<DynamicTextureRecords.WeightedDynamicTextureSet> entries, @Nullable RandomSource random)
        {
            if (random != null)
            {
                int totalWeight = 0;
                for (DynamicTextureRecords.WeightedDynamicTextureSet weight : entries)
                {
                    totalWeight += weight.weight();
                }
                int weightedIndex = Math.abs((int) random.nextLong()) % totalWeight;
                for (DynamicTextureRecords.WeightedDynamicTextureSet weight : entries)
                {
                    weightedIndex -= weight.weight();
                    if (weightedIndex < 0)
                    {
                        return weight.dynamicTextureSet();
                    }
                }
            }
            else
            {
                return entries.getFirst().dynamicTextureSet();
            }
            return null;
        }

        //the individual bakedquads for each random blockstate variant are stored in the quads list. obtain them to bake
        // a new model when one isnt available for this combination of the int array of blockstates
        protected class WeightedChunkMap
        {
            int[] totalWeightArray;
            int totalWeightLCM;
            int[] chunkMap;

            final BakedDynamicChiseledModel model;

            //keys are ints ranging to the size of the lcm
            private final Map<Integer, BakedModel> modelCache = new ConcurrentHashMap<>();

            //todo - add math for calculating the total weight LCM from all the total weights in the model data
            public WeightedChunkMap(BakedDynamicChiseledModel model, DynamicChiseledBlockRecords.ChiseledChunkData modelData)
            {
                this.model = model;
                this.chunkMap = modelData.chunkMap();
                this.totalWeightLCM = 1;
            }

            //return cached model or baked new one using the given models and their random states
            public BakedModel getModel(@Nullable RandomSource rand)
            {
                if ( rand == null || totalWeightLCM > 1)
                {
                    return modelCache.computeIfAbsent(1, this::bake);
                }
                else
                {
                    return modelCache.computeIfAbsent(Math.abs((int)rand.nextLong()) % this.totalWeightLCM, this::bake);
                }
            }

            public BakedModel bake(Integer rand)
            {
                return model.bake(new DynamicChiseledBlockRecords.ChunkSet(chunkMap, rand));
            }
        }

        protected class WeightedBlockStateQuads
        {
            final int totalVariants;

            final BlockState state;

            //todo - this doesnt take the weight values into account yet. make a more advanced storage method to also keep
            // their weight values, and just keep the total variants as a limit to the amount of states to obtain
            //first key is variant number, second is chunk number, stores lists of blockquads per chunk number
            private final Map<Integer, Map<Integer, List<BakedQuadRecord>> > stateQuadCache = new ConcurrentHashMap<>();

            public WeightedBlockStateQuads(BlockState state)
            {
                this.state = state;

                BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
                BakedModel model = dispatcher.getBlockModel(state);

                if (model instanceof WeightedBakedModel)
                {
                    this.totalVariants = ((WeightedBakedModelMixin) model).getList().size();
                }
                else
                {
                    this.totalVariants = 1;
                }
            }

            public Map<Integer, List<BakedQuadRecord>> getChunks(int variant)
            {
                //todo - replace totalVariants with more complex form of obtaining the correct variant
                return stateQuadCache.computeIfAbsent(variant%totalVariants, this::makeChunks);
            }

            public List<BakedQuadRecord> getChunk(int variant, int chunk)
            {
                //todo - replace totalVariants with more complex form of obtaining the correct variant
                Map<Integer, List<BakedQuadRecord>> stateQuads = stateQuadCache.computeIfAbsent(variant%totalVariants, this::makeChunks);
                return stateQuads.get(chunk);
            }
            //chunks 0-63 are from the 4x4x4 grid
            //chunks 64-71 are of the 2x2x2 grid
            //chunks 72-73 are y slabs
            //chunks 74-75 are x slabs
            //chunks 76-77 are z slabs
            //of the 4x4x4, floorDiv(int, 16) returns 0-3, going bottom to top on the layers,
            // int%4 returns its slice in the x axis going west - east, 0-3,
            // and floorDiv(int%16, 4) returns its slice on the z axis, going north - south, 0-3
            private Map<Integer, List<BakedQuadRecord>> makeChunks(int variant)
            {

                final Map<Integer, List<BakedQuadRecord>> chunks = new HashMap<>();

                List<Block> blocks = constructBlocks(this.state, new FakeRandomSource(1));

                //construct 78 lists of baked quads that will be used in constructing baked models
                for (int i = 0; i < 78; i++)
                {
                    final List<BakedQuadRecord> chunk = new ArrayList<>();

                    final float minX;
                    final float maxX;
                    final float minY;
                    final float maxY;
                    final float minZ;
                    final float maxZ;
                    if (i < 64)
                    {
                        //4x4x4 grid
                        minX = (i%4)/4f;
                        maxX = minX + 4f/16f;
                        minY = (Math.floorDiv(i, 16))/4f;
                        maxY = minY + 4f/16f;
                        minZ = (Math.floorDiv(i%16, 4))/4f;
                        maxZ = minZ + 4f/16f;
                    }
                    else if (i < 72)
                    {
                        //2x2x2 grid
                        final int modifiedI = i - 64;
                        minX = (modifiedI%2)/2f;
                        maxX = minX + 8f/16f;
                        minY = Math.floorDiv(modifiedI, 4)/2f;
                        maxY = minY + 8f/16f;
                        minZ = Math.floorDiv(modifiedI%4, 2)/2f;
                        maxZ = minZ + 8f/16f;
                    }
                    else if (i < 74)
                    {
                        //vertical slabs
                        minX = 0;
                        maxX = 1;
                        minY = (i%2)/2f;
                        maxY = minY + 8f/16f;
                        minZ = 0;
                        maxZ = 1;
                    }
                    else if (i < 76)
                    {
                        //horizontal slabs, X
                        minX = (i%2)/2f;
                        maxX = minX + 8f/16f;
                        minY = 0;
                        maxY = 1;
                        minZ = 0;
                        maxZ = 1;
                    }
                    else
                    {
                        //horizontal slabs, Z
                        minX = 0;
                        maxX = 1;
                        minY = 0;
                        maxY = 1;
                        minZ = (i%2)/2f;
                        maxZ = minZ + 8f/16f;
                    }

                    if (i == 16)
                    {
                        log.debug("MAKING 2ND LAYER TOP LEFT QUADS, BLOCK COUNT IS {}", blocks.size());
                    }

                    for (Block block : blocks)
                    {
                        //the vertices are stored as floats between 0-1 per block they extend through.

                        float blockMinX = Float.MAX_VALUE;
                        float blockMaxX = -Float.MAX_VALUE;
                        float blockMinY = Float.MAX_VALUE;
                        float blockMaxY = -Float.MAX_VALUE;
                        float blockMinZ = Float.MAX_VALUE;
                        float blockMaxZ = -Float.MAX_VALUE;

                        for (Vec3 vertex : block.getVertices().values())
                        {
                            if (vertex != null)
                            {
                                blockMinX = Math.min(blockMinX, (float) vertex.x());
                                blockMaxX = Math.max(blockMaxX, (float) vertex.x());
                                blockMinY = Math.min(blockMinY, (float) vertex.y());
                                blockMaxY = Math.max(blockMaxY, (float) vertex.y());
                                blockMinZ = Math.min(blockMinZ, (float) vertex.z());
                                blockMaxZ = Math.max(blockMaxZ, (float) vertex.z());
                            }
                        }

                        if (i == 16)
                        {
                            log.debug("WORKING ON BLOCK COORDINATES {}", block.vertices);
                        }

                        if (blockMinX == Float.MAX_VALUE)
                        {
                            continue;
                        }

                        if (minX < blockMaxX &&
                                maxX > blockMinX &&
                                minY < blockMaxY &&
                                maxY > blockMinY &&
                                minZ < blockMaxZ &&
                                maxZ > blockMinZ)
                        {
                            if (i == 16)
                            {
                                log.debug("BLOCK WAS IN BOUNDS");
                            }
                            final float newMinX = Math.clamp(blockMinX, minX, maxX);
                            final float newMaxX = Math.clamp(blockMaxX, minX, maxX);
                            final float newMinY = Math.clamp(blockMinY, minY, maxY);
                            final float newMaxY = Math.clamp(blockMaxY, minY, maxY);
                            final float newMinZ = Math.clamp(blockMinZ, minZ, maxZ);
                            final float newMaxZ = Math.clamp(blockMaxZ, minZ, maxZ);

                            final float xLength = blockMaxX - blockMinX;
                            final float yLength = blockMaxY - blockMinY;
                            final float zLength = blockMaxZ - blockMinZ;

                            final float xUVOffset1 = xLength == 0 ? 0 : (blockMinX - newMinX)/xLength;
                            final float yUVOffset1 = yLength == 0 ? 0 : (blockMinY - newMinY)/yLength;
                            final float zUVOffset1 = zLength == 0 ? 0 : (blockMinZ - newMinZ)/zLength;

                            final float xUVOffset2 = xLength == 0 ? 0 : (blockMaxX - newMaxX)/xLength;
                            final float yUVOffset2 = yLength == 0 ? 0 : (blockMaxY - newMaxY)/yLength;
                            final float zUVOffset2 = zLength == 0 ? 0 : (blockMaxZ - newMaxZ)/zLength;

                            final float xUVPortion = xLength == 0 ? 0 : (newMaxX - newMinX)/xLength;
                            final float yUVPortion = yLength == 0 ? 0 : (newMaxY - newMinY)/yLength;
                            final float zUVPortion = zLength == 0 ? 0 : (newMaxZ - newMinZ)/zLength;

                            float[] afloat = new float[Direction.values().length];
                            afloat[FaceInfo.Constants.MIN_X] = newMinX;
                            afloat[FaceInfo.Constants.MAX_X] = newMaxX;
                            afloat[FaceInfo.Constants.MIN_Y] = newMinY;
                            afloat[FaceInfo.Constants.MAX_Y] = newMaxY;
                            afloat[FaceInfo.Constants.MIN_Z] = newMinZ;
                            afloat[FaceInfo.Constants.MAX_Z] = newMaxZ;

                            if (i == 16)
                            {
                                log.debug("NEW BLOCK COORDINATES ARE {}", afloat);
                            }

                            float[] bfloat = new float[Direction.values().length];
                            bfloat[FaceInfo.Constants.MIN_X] = minX;
                            bfloat[FaceInfo.Constants.MAX_X] = maxX;
                            bfloat[FaceInfo.Constants.MIN_Y] = minY;
                            bfloat[FaceInfo.Constants.MAX_Y] = maxY;
                            bfloat[FaceInfo.Constants.MIN_Z] = minZ;
                            bfloat[FaceInfo.Constants.MAX_Z] = maxZ;

                            for (BakedQuad quad : block.getQuads().values())
                            {
                                final int[] vertices = quad.getVertices();
                                int[] newVertices = new int[vertices.length];
                                System.arraycopy(vertices, 0, newVertices, 0, vertices.length);

                                final Direction direction = quad.getDirection();

                                final float uOffset1;
                                final float uOffset2;
                                final float vOffset1;
                                final float vOffset2;

                                switch (direction)
                                {
                                    case NORTH:
                                    {
                                        uOffset1 = -xUVOffset2;
                                        uOffset2 = -xUVOffset1;
                                        vOffset1 = yUVOffset1;
                                        vOffset2 = yUVOffset2;
                                        break;
                                    }
                                    case EAST:
                                    {
                                        uOffset1 = -zUVOffset2;
                                        uOffset2 = -zUVOffset1;
                                        vOffset1 = yUVOffset1;
                                        vOffset2 = yUVOffset2;
                                        break;
                                    }
                                    case SOUTH:
                                    {
                                        uOffset1 = xUVOffset1;
                                        uOffset2 = xUVOffset2;
                                        vOffset1 = yUVOffset1;
                                        vOffset2 = yUVOffset2;
                                        break;
                                    }
                                    case WEST:
                                    {
                                        uOffset1 = zUVOffset1;
                                        uOffset2 = zUVOffset2;
                                        vOffset1 = yUVOffset1;
                                        vOffset2 = yUVOffset2;
                                        break;
                                    }
                                    case UP:
                                    {
                                        uOffset1 = xUVOffset1;
                                        uOffset2 = xUVOffset2;
                                        vOffset1 = -zUVOffset2;
                                        vOffset2 = -zUVOffset1;
                                        break;
                                    }
                                    default:
                                    {
                                        uOffset1 = xUVOffset1;
                                        uOffset2 = xUVOffset2;
                                        vOffset1 = zUVOffset1;
                                        vOffset2 = zUVOffset2;
                                        break;
                                    }
                                }

                                if (i == 16)
                                {
                                    log.debug("QUAD IS FACING {}", direction);
                                }

                                final float[] UVs = new float[8];

                                for (int j = 0; j < 4; j++)
                                {
                                    int k = j * 8;

                                    UVs[j * 2] = Float.intBitsToFloat(vertices[k + 4]);
                                    UVs[j * 2 + 1] = Float.intBitsToFloat(vertices[k + 5]);
                                }

                                float[] newUVs = new float[8];

                                System.arraycopy(UVs, 0, newUVs, 0, UVs.length);

                                for (int j = 0; j < 4; j++)
                                {
                                    final float u1 = UVs[j * 2];
                                    final float u2 = UVs[j * 2 + (j == 3 ? -6 : 2)];
                                    final float v1 = UVs[j * 2 + 1];
                                    final float v2 = UVs[j * 2 + (j == 3 ? -6 : 2) + 1];
                                    float uDiff = u2 - u1;
                                    float vDiff = v2 - v1;

                                    final float sideOffset1;
                                    final float sideOffset2;

                                    switch (j)
                                    {
                                        case 0:
                                        {
                                            sideOffset1 = vOffset2;
                                            sideOffset2 = vOffset1;
                                            break;
                                        }
                                        case 1:
                                        {
                                            sideOffset1 = -uOffset1;
                                            sideOffset2 = -uOffset2;
                                            break;
                                        }
                                        case 2:
                                        {
                                            sideOffset1 = -vOffset1;
                                            sideOffset2 = -vOffset2;
                                            break;
                                        }
                                        default:
                                        {
                                            sideOffset1 = uOffset2;
                                            sideOffset2 = uOffset1;
                                        }
                                    }

                                    if (i == 16)
                                    {
                                        log.debug("CHECKING UVS FOR VERTEX {}", j);
                                    }

                                    if (uDiff != 0)
                                    {
                                        float newU1 = u1 + uDiff * (sideOffset1);
                                        float newU2 = u2 + uDiff * (sideOffset2);

                                        newUVs[j * 2] = newU1;
                                        newUVs[j * 2 + (j == 3 ? -6 : 2)] = newU2;

                                        if (i == 16)
                                        {
                                            log.debug("UPDATING U1 {} NEWU1 {} U2 {} NEWU2 {} DIFF IS {} OFFSET 1 IS {} OFFSET 2 IS {}", u1, newU1, u2, newU2, uDiff, j > 2 ? uOffset2 : -uOffset1, j > 2 ? uOffset1 : -uOffset2);
                                        }
                                    }
                                    if (vDiff != 0)
                                    {
                                        float newV1 = v1 + vDiff * (sideOffset1);
                                        float newV2 = v2 + vDiff * (sideOffset2);

                                        newUVs[j * 2 + 1] = newV1;
                                        newUVs[j * 2 + (j == 3 ? -6 : 2) + 1] = newV2;

                                        if (i == 16)
                                        {
                                            log.debug("UPDATING V1 {} NEWV1 {} V2 {} NEWV2 {} DIFF IS {} OFFSET 1 IS {} OFFSET 2 IS {}", v1, newV1, v2, newV2, vDiff, j != 1 && j!= 2 ? vOffset2 : -vOffset1, j != 1 && j!= 2 ? vOffset1 : -vOffset2);
                                        }
                                    }
                                }

                                Vec3[] newCoords = new Vec3[4];

                                Vec3[] chunkCoords = new Vec3[4];

                                for (int j = 0; j < 4; j++)
                                {
                                    final FaceInfo.VertexInfo faceinfo$vertexinfo = FaceInfo.fromFacing(direction).getVertexInfo(j);

                                    final int k = j * 8;

                                    newVertices[k] = Float.floatToRawIntBits(
                                            afloat[faceinfo$vertexinfo.xFace]);
                                    newVertices[k + 1] = Float.floatToRawIntBits(
                                            afloat[faceinfo$vertexinfo.yFace]);
                                    newVertices[k + 2] = Float.floatToRawIntBits(
                                            afloat[faceinfo$vertexinfo.zFace]);
                                    newVertices[k + 4] = Float.floatToRawIntBits(newUVs[j * 2]);
                                    newVertices[k + 5] = Float.floatToRawIntBits(newUVs[j * 2 + 1]);

                                    newCoords[j] = new Vec3(afloat[faceinfo$vertexinfo.xFace], afloat[faceinfo$vertexinfo.yFace], afloat[faceinfo$vertexinfo.zFace]);

                                    chunkCoords[j] = new Vec3(bfloat[faceinfo$vertexinfo.xFace], bfloat[faceinfo$vertexinfo.yFace], bfloat[faceinfo$vertexinfo.zFace]);
                                }

                                if (i == 16)
                                {
                                    for (int j = 0; j < 4; j++)
                                    {
                                        log.debug("NEW COORDS FOR VERTEX {} ARE {}", j, newCoords);
                                    }
                                }

                                final boolean cullable = Arrays.equals(newCoords, chunkCoords);

                                BakedQuad newQuad = new BakedQuad(newVertices, quad.getTintIndex(), quad.getDirection(), quad.getSprite(), quad.isShade(), quad.hasAmbientOcclusion());

                                chunk.add(new BakedQuadRecord(newQuad, cullable));
                            }
                        }
                    }

                    if (!chunk.isEmpty())
                    {
                        chunks.put(i, chunk);
                    }
                }
                return chunks;
            }

            //todo - THIS DOESNT WORK CORRECTLY ON SOME ORIENTATIONS
            //splits every baked quad from a model up into a list of list of quads, each of those sublists being for
            // a single blocks quads
            private List<Block> constructBlocks(BlockState state, RandomSource random)
            {
                BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
                BakedModel model = dispatcher.getBlockModel(state);

                List<BakedQuad> baseQuads = new ArrayList<>();
                baseQuads.addAll(model.getQuads(state, null, random));
                for (Direction direction : Direction.values())
                {
                    baseQuads.addAll(model.getQuads(state, direction, random));
                }

                final List<Block> blocks= new ArrayList<>();
                final List<BakedQuad> baseQuads1 = baseQuads;

                //cycles back around as long as this copy of all the quads in the model arent accounted for
                while(!baseQuads1.isEmpty())
                {
                    boolean added = false;

                    //keep as false. if there is no face corresponding in the block to the current quad then flip true
                    boolean sameDirection = false;
                    Iterator<BakedQuad> iterator = baseQuads1.iterator();

                    while (iterator.hasNext())
                    {
                        BakedQuad quad = iterator.next();

                        for (Block block : blocks)
                        {
                            //im pretty sure this wont allow wrong directions as entries(?) since then they wouldnt have
                            // vertices in the right spots and its not just checking that a vertex coincides at all but
                            // that it coincides in the right spot

                            if (!block.directionExists(quad.getDirection()))
                            {
                                sameDirection = true;
                            }
                            else
                            {
                                continue;
                            }

                            if(block.checkQuad(quad))
                            {
                                block.addQuad(quad);
                                iterator.remove();
                                added = true;
                                break;
                            }
                        }
                    }

                    if (!added || !sameDirection)
                    {

                        blocks.add(new Block(baseQuads1.removeFirst()));
                    }
                }

                return blocks;
            }
        }
    }

    public static class Block
    {
//            3--------0
//           /|       /|  ⌃ U
//          / |      / |  |
//         2--7-----1--4  *--> E
//         | /      | /  /
//         |/       |/  ⌄ S
//         6--------5
        @Getter
        Map<Integer, Vec3> vertices = new HashMap<>(8);

        //these are just the direction enum ordinals btw
//        0 - DOWN
//        1 - UP
//        2 - NORTH
//        3 - SOUTH
//        4 - WEST
//        5 - EAST
        @Getter
        final Map<Integer, BakedQuad> quads = new HashMap<>(6);

        public boolean checkQuad(BakedQuad quad)
        {
            if (quads.isEmpty())
            {
                return true;
            }
            else
            {
                if (directionExists(quad.getDirection()))
                {
                    return false;
                }

                final Direction direction = quad.getDirection();
                final int[] vertices = DirectionToVertices.fromFacing(direction).vertices;
                final int[] vertices1 = quad.getVertices();

                boolean coincides = false;

                for (int i = 1; i < 4; i++)
                {
                    final int vertexPosition = vertices[i];
                    final Vec3 vertex = new Vec3(
                            Float.intBitsToFloat(vertices1[i * 8]),
                            Float.intBitsToFloat(vertices1[i * 8 + 1]),
                            Float.intBitsToFloat(vertices1[i * 8 + 2]));

                    if(this.vertices.get(vertexPosition) != null)
                    {
                        if (this.vertices.get(vertexPosition).equals(vertex))
                        {
                            coincides = true;
                        }
                        //if one of this blocks vertices doesnt coincide with the given quad AND it does exist then
                        // that quad isnt part of the block
                        else
                        {
                            return false;
                        }
                    }
                }

                return coincides;
            }
        }

        public boolean directionExists(Direction direction)
        {
            return quads.get(direction.ordinal()) != null;
        }

        public void addQuad(BakedQuad quad)
        {
            final Direction direction = quad.getDirection();
            final int[] vertices = DirectionToVertices.fromFacing(direction).vertices;
            final int[] vertices1 = quad.getVertices();

            for (int i = 1; i < 4; i++)
            {
                final int vertexPosition = vertices[i];
                final Vec3 vertex = new Vec3(
                        Float.intBitsToFloat(vertices1[i * 8]),
                        Float.intBitsToFloat(vertices1[i * 8 + 1]),
                        Float.intBitsToFloat(vertices1[i * 8 + 2]));

                this.vertices.putIfAbsent(vertexPosition, vertex);
            }

            this.quads.put(direction.ordinal(), quad);
        }

        public Vec3 getVertex(Direction direction, int corner)
        {
            return this.vertices.get(DirectionToVertices.fromFacing(direction).vertices[corner]);
        }

        public Vec3 getVertex(int corner)
        {
            return this.vertices.get(corner);
        }

        public Block(BakedQuad... quads)
        {
            for(BakedQuad quad : quads)
            {
                if (this.checkQuad(quad))
                {
                    this.addQuad(quad);
                }
            }
        }

        public enum DirectionToVertices
        {
//            3--------0
//           /|       /|  ⌃ U
//          / |      / |  |
//         2--7-----1--4  *--> E
//         | /      | /  /
//         |/       |/  ⌄ S
//         6--------5

            DOWN(new int[]{6,7,4,5}),
            UP(new int[]{3,2,1,0}),
            NORTH(new int[]{0,4,7,3}),
            SOUTH(new int[]{2,6,5,1}),
            WEST(new int[]{3,7,6,2}),
            EAST(new int[]{1,5,4,0});

            private static final DirectionToVertices[] BY_FACING = Util.make(new DirectionToVertices[6], (vertices) -> {
                vertices[0] = DOWN;
                vertices[1] = UP;
                vertices[2] = NORTH;
                vertices[3] = SOUTH;
                vertices[4] = WEST;
                vertices[5] = EAST;
            });

            public static DirectionToVertices fromFacing(Direction facing)
            {
                return BY_FACING[facing.get3DDataValue()];
            }

            private final int[] vertices;

            DirectionToVertices(int[] vertices)
            {
                this.vertices = vertices;
            }

            public int getVertex(int corner)
            {
                return this.vertices[corner];
            }
        }
    }
    public record BakedQuadRecord(BakedQuad bakedQuad, boolean cullable)
    {

    }
}

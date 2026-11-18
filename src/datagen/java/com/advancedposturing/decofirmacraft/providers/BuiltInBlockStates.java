package com.advancedposturing.decofirmacraft.providers;

import com.advancedposturing.decofirmacraft.DecoFirmaCraft;
import com.advancedposturing.decofirmacraft.common.block.DFCBlocks;
import com.advancedposturing.decofirmacraft.common.block.rock.DFCExtendedRock;
import com.advancedposturing.decofirmacraft.common.block.state.DFCBlockStateProperties;
import net.dries007.tfc.TerraFirmaCraft;
import net.dries007.tfc.common.blocks.TFCBlockStateProperties;
import net.dries007.tfc.common.blocks.rock.RockCategory;
import net.dries007.tfc.common.blocks.rock.RockSpikeBlock;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.neoforged.neoforge.client.model.generators.*;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredHolder;

public class BuiltInBlockStates extends BlockStateProvider {

    public BuiltInBlockStates(final PackOutput packOutput, final ExistingFileHelper existingFileHelper)
    {
        super(packOutput, DecoFirmaCraft.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerStatesAndModels()
    {
        for (DFCExtendedRock rock : DFCExtendedRock.values()){
            if ((rock.category().equals(RockCategory.IGNEOUS_EXTRUSIVE) || rock.category().equals(RockCategory.IGNEOUS_INTRUSIVE)) && rock.isDFCRock())
            {
                simpleBlockWithItem(DFCBlocks.DFC_MAGMA_BLOCKS.get(rock).get(), cubeAll(DFCBlocks.DFC_MAGMA_BLOCKS.get(rock).get()));
                simpleBlockWithItem(DFCBlocks.DFC_ROCK_ANVILS.get(rock).get(), anvilBlockModel(rock));
            }
            for (DFCExtendedRock.DFCRockBlockType rockType : DFCExtendedRock.DFCRockBlockType.values())
            {
                if (rockType.has(rock))
                {
                    if (rockType == DFCExtendedRock.DFCRockBlockType.RAW)
                    {
//                        rawRockBlock(rock);
                        ResourceLocation texture = getBlockModelLocation(DFCBlocks.DFC_ROCK_BLOCKS.get(rock).get(rockType).getId());
                        stairsBlock(DFCBlocks.DFC_ROCK_DECORATIONS.get(rock).get(rockType).stair().get(), texture);
                        slabBlock(DFCBlocks.DFC_ROCK_DECORATIONS.get(rock).get(rockType).slab().get(), getBlockModelLocation(DFCBlocks.DFC_ROCK_BLOCKS.get(rock).get(rockType).getId()), texture);
                        wallBlock(DFCBlocks.DFC_ROCK_DECORATIONS.get(rock).get(rockType).wall().get(), getBlockModelString(DFCBlocks.DFC_ROCK_BLOCKS.get(rock).get(rockType).getId()), texture);
                    }
                    else if (rockType.hasVariants())
                    {
                        ResourceLocation texture = getBlockModelLocation(DFCBlocks.DFC_ROCK_BLOCKS.get(rock).get(rockType).getId());
                        simpleBlockWithItem(DFCBlocks.DFC_ROCK_BLOCKS.get(rock).get(rockType).get(), models().cubeAll(getBlockModelString(DFCBlocks.DFC_ROCK_BLOCKS.get(rock).get(rockType).getId()), texture));
                        stairsBlock(DFCBlocks.DFC_ROCK_DECORATIONS.get(rock).get(rockType).stair().get(), texture);
                        slabBlock(DFCBlocks.DFC_ROCK_DECORATIONS.get(rock).get(rockType).slab().get(), getBlockModelLocation(DFCBlocks.DFC_ROCK_BLOCKS.get(rock).get(rockType).getId()), texture);
                        wallBlock(DFCBlocks.DFC_ROCK_DECORATIONS.get(rock).get(rockType).wall().get(), getBlockModelString(DFCBlocks.DFC_ROCK_BLOCKS.get(rock).get(rockType).getId()), texture);
                    }
                }
            }

            if (rock.isDFCRock() || rock.isSandstone() || rock.isBeneathRock())
            {
                //hardened todo - fix for sandstone
//                hardenedRockBlock(rock);
                //gravel
                ResourceLocation gravelTexture = getBlockModelLocation(DFCBlocks.DFC_ROCK_BLOCKS.get(rock).get(DFCExtendedRock.DFCRockBlockType.GRAVEL).getId());
                simpleBlockWithItem(DFCBlocks.DFC_ROCK_BLOCKS.get(rock).get(DFCExtendedRock.DFCRockBlockType.GRAVEL).get(), models().cubeAll(getBlockModelString(DFCBlocks.DFC_ROCK_BLOCKS.get(rock).get(DFCExtendedRock.DFCRockBlockType.GRAVEL).getId()), gravelTexture));
                //spike
                rockSpikeBlock(rock);
                //chiseled stone todo - fix for sandstone
//                simpleBlockWithItem(DFCBlocks.DFC_ROCK_BLOCKS.get(rock).get(DFCExtendedRock.DFCRockBlockType.CHISELED).get(), cubeAll(DFCBlocks.DFC_ROCK_BLOCKS.get(rock).get(DFCExtendedRock.DFCRockBlockType.CHISELED).get()));
                //loose stones
                looseStoneBlock(rock);
                //mossy loose stones
                looseMossyStoneBlock(rock);
                //brick items todo - do these in the itemModel file, same with the loose stones
                //pressure plates
                pressurePlateBlock(rock);
                //buttons
                buttonBlock(rock);
                //aqueduct
                aqueductBlock(rock);
                //ores
                //todo - do oregen
                //todo - rope anchor
            }

            //dfc rock blocktypes

            //pillar
            pillarBlock(DFCBlocks.DFC_ROCK_BLOCKS.get(rock).get(DFCExtendedRock.DFCRockBlockType.PILLAR).holder());
            //outlined todo - need to get custom modelloader accounted for
            //polished todo - need to get custom modelloader accounted for
            //large bricks todo - need to get custom modelloader accounted for
            //column
            columnBlock(DFCBlocks.DFC_ROCK_BLOCKS.get(rock).get(DFCExtendedRock.DFCRockBlockType.COLUMN).holder());
            //rail
            railBlock(rock);
            //railBlock
            railBlockBlock(rock);
            //railStairsBlock
            railStairsBlock(rock);
        }
    }

    private void rawRockBlock(Block block, ModelFile modelUpright, ModelFile modelUpsideDown)
    {
        VariantBlockStateBuilder builder = getVariantBuilder(block);

        builder
                .partialState().with(BlockStateProperties.AXIS, Direction.Axis.X).modelForState()
                .modelFile(modelUpright).rotationX(90).rotationY(90).nextModel()
                .modelFile(modelUpsideDown).rotationX(90).rotationY(90).addModel()
                .partialState().with(BlockStateProperties.AXIS, Direction.Axis.Y).modelForState()
                .modelFile(modelUpright).nextModel()
                .modelFile(modelUpsideDown).nextModel()
                .modelFile(modelUpright).rotationY(180).nextModel()
                .modelFile(modelUpsideDown).rotationY(180).addModel()
                .partialState().with(BlockStateProperties.AXIS, Direction.Axis.Z).modelForState()
                .modelFile(modelUpright).rotationX(90).nextModel()
                .modelFile(modelUpsideDown).rotationX(90).nextModel()
                .modelFile(modelUpright).rotationX(90).rotationY(180).nextModel()
                .modelFile(modelUpsideDown).rotationX(90).rotationY(180).addModel();

//        simpleBlockItem(blockHolder.get(), modelUpright);
    }

    private void hardenedRockBlock(DFCExtendedRock rock)
    {
        boolean hasTop = rock == DFCExtendedRock.ARKOSE || rock == DFCExtendedRock.BLUESCHIST || rock == DFCExtendedRock.TRAVERTINE;
        ResourceLocation texture = dfcLoc("block/rock/raw/" + rock.getSerializedName());
        DeferredHolder<Block, Block> rawBlockHolder = DFCBlocks.DFC_ROCK_BLOCKS.get(rock).get(DFCExtendedRock.DFCRockBlockType.RAW).holder();
        DeferredHolder<Block, Block> blockHolder = DFCBlocks.DFC_ROCK_BLOCKS.get(rock).get(DFCExtendedRock.DFCRockBlockType.HARDENED).holder();
        ModelFile modelUpright = createModel(getBlockModelString(rawBlockHolder.getId()), vanillaLoc("block/cube_column")).texture("side", texture).texture("end", hasTop ? texture.withSuffix("_top") : texture).texture("particle", texture);
        ModelFile modelUpsideDown = createModel(getBlockModelString(rawBlockHolder.getId()) + "_mirrored", vanillaLoc("block/cube_column_mirrored")).texture("side", texture).texture("end", hasTop ? texture.withSuffix("_top") : texture).texture("particle", texture);

        VariantBlockStateBuilder builder = getVariantBuilder(blockHolder.get());

        builder
                .partialState().with(BlockStateProperties.AXIS, Direction.Axis.X).modelForState()
                .modelFile(modelUpright).rotationX(90).rotationY(90).nextModel()
                .modelFile(modelUpsideDown).rotationX(90).rotationY(90).addModel()
                .partialState().with(BlockStateProperties.AXIS, Direction.Axis.Y).modelForState()
                .modelFile(modelUpright).nextModel()
                .modelFile(modelUpsideDown).nextModel()
                .modelFile(modelUpright).rotationY(180).nextModel()
                .modelFile(modelUpsideDown).rotationY(180).addModel()
                .partialState().with(BlockStateProperties.AXIS, Direction.Axis.Z).modelForState()
                .modelFile(modelUpright).rotationX(90).nextModel()
                .modelFile(modelUpsideDown).rotationX(90).nextModel()
                .modelFile(modelUpright).rotationX(90).rotationY(180).nextModel()
                .modelFile(modelUpsideDown).rotationX(90).rotationY(180).addModel();

//        simpleBlockItem(blockHolder.get(), modelUpright);
    }

    private void rockSpikeBlock(DFCExtendedRock rock)
    {
        ResourceLocation texture = dfcLoc("block/rock/raw/" + rock.getSerializedName());
        DeferredHolder<Block, Block> blockHolder = DFCBlocks.DFC_ROCK_BLOCKS.get(rock).get(DFCExtendedRock.DFCRockBlockType.SPIKE).holder();
        ModelFile base = createModel(getBlockModelString(blockHolder.getId()) + "_base", tfcLoc("block/rock/spike_base")).texture("texture", texture).texture("particle", texture);
        ModelFile middle = createModel(getBlockModelString(blockHolder.getId()) + "_middle", tfcLoc("block/rock/spike_middle")).texture("texture", texture).texture("particle", texture);
        ModelFile tip = createModel(getBlockModelString(blockHolder.getId()) + "_tip", tfcLoc("block/rock/spike_tip")).texture("texture", texture).texture("particle", texture);

        VariantBlockStateBuilder builder = getVariantBuilder(blockHolder.get());

        builder
                .partialState().with(TFCBlockStateProperties.ROCK_SPIKE_PART, RockSpikeBlock.Part.BASE).modelForState()
                .modelFile(base).addModel()
                .partialState().with(TFCBlockStateProperties.ROCK_SPIKE_PART, RockSpikeBlock.Part.MIDDLE).modelForState()
                .modelFile(middle).addModel()
                .partialState().with(TFCBlockStateProperties.ROCK_SPIKE_PART, RockSpikeBlock.Part.TIP).modelForState()
                .modelFile(tip).addModel();

//        simpleBlockItem(blockHolder.get(), base);
    }

    private void looseStoneBlock(DFCExtendedRock rock)
    {
        ResourceLocation texture = dfcLoc("block/rock/raw/" + rock.getSerializedName());
        DeferredHolder<Block, Block> blockHolder = DFCBlocks.DFC_ROCK_BLOCKS.get(rock).get(DFCExtendedRock.DFCRockBlockType.LOOSE).holder();
        ModelFile model1 = createModel(getBlockModelString(blockHolder.getId()) + "_1", getLooseRockModelParent(rock, 1)).texture("texture", texture).texture("particle", texture);
        ModelFile model2 = createModel(getBlockModelString(blockHolder.getId()) + "_2", getLooseRockModelParent(rock, 2)).texture("texture", texture).texture("particle", texture);
        ModelFile model3 = createModel(getBlockModelString(blockHolder.getId()) + "_3", getLooseRockModelParent(rock, 3)).texture("texture", texture).texture("particle", texture);

        VariantBlockStateBuilder builder = getVariantBuilder(blockHolder.get());

        builder
                .partialState().with(TFCBlockStateProperties.COUNT_1_3, 1).modelForState()
                .modelFile(model1).rotationY(0).nextModel()
                .modelFile(model1).rotationY(90).nextModel()
                .modelFile(model1).rotationY(180).nextModel()
                .modelFile(model1).rotationY(270).addModel()
                .partialState().with(TFCBlockStateProperties.COUNT_1_3, 2).modelForState()
                .modelFile(model2).rotationY(0).nextModel()
                .modelFile(model2).rotationY(90).nextModel()
                .modelFile(model2).rotationY(180).nextModel()
                .modelFile(model2).rotationY(270).addModel()
                .partialState().with(TFCBlockStateProperties.COUNT_1_3, 3).modelForState()
                .modelFile(model3).rotationY(0).nextModel()
                .modelFile(model3).rotationY(90).nextModel()
                .modelFile(model3).rotationY(180).nextModel()
                .modelFile(model3).rotationY(270).addModel();
    }

    private void looseMossyStoneBlock(DFCExtendedRock rock)
    {
        ResourceLocation texture = getBlockModelLocation(DFCBlocks.DFC_ROCK_BLOCKS.get(rock).get(DFCExtendedRock.DFCRockBlockType.MOSSY_COBBLE).getId());
        DeferredHolder<Block, Block> blockHolder = DFCBlocks.DFC_ROCK_BLOCKS.get(rock).get(DFCExtendedRock.DFCRockBlockType.MOSSY_LOOSE).holder();
        ModelFile model1 = createModel(getBlockModelString(blockHolder.getId()) + "_1", getLooseRockModelParent(rock, 1)).texture("texture", texture).texture("particle", texture);
        ModelFile model2 = createModel(getBlockModelString(blockHolder.getId()) + "_2", getLooseRockModelParent(rock, 2)).texture("texture", texture).texture("particle", texture);
        ModelFile model3 = createModel(getBlockModelString(blockHolder.getId()) + "_3", getLooseRockModelParent(rock, 3)).texture("texture", texture).texture("particle", texture);

        VariantBlockStateBuilder builder = getVariantBuilder(blockHolder.get());

        builder
                .partialState().with(TFCBlockStateProperties.COUNT_1_3, 1).modelForState()
                .modelFile(model1).rotationY(0).nextModel()
                .modelFile(model1).rotationY(90).nextModel()
                .modelFile(model1).rotationY(180).nextModel()
                .modelFile(model1).rotationY(270).addModel()
                .partialState().with(TFCBlockStateProperties.COUNT_1_3, 2).modelForState()
                .modelFile(model2).rotationY(0).nextModel()
                .modelFile(model2).rotationY(90).nextModel()
                .modelFile(model2).rotationY(180).nextModel()
                .modelFile(model2).rotationY(270).addModel()
                .partialState().with(TFCBlockStateProperties.COUNT_1_3, 3).modelForState()
                .modelFile(model3).rotationY(0).nextModel()
                .modelFile(model3).rotationY(90).nextModel()
                .modelFile(model3).rotationY(180).nextModel()
                .modelFile(model3).rotationY(270).addModel();
    }

    private void pressurePlateBlock(DFCExtendedRock rock)
    {
        ResourceLocation texture = dfcLoc("block/rock/raw/" + rock.getSerializedName());
        DeferredHolder<Block, Block> blockHolder = DFCBlocks.DFC_ROCK_BLOCKS.get(rock).get(DFCExtendedRock.DFCRockBlockType.PRESSURE_PLATE).holder();
        ModelFile pressurePlate = this.models().pressurePlate(getBlockModelString(blockHolder.getId()), texture);
        ModelFile pressurePlateDown = this.models().pressurePlateDown(getBlockModelString(blockHolder.getId()) + "_down", texture);

        VariantBlockStateBuilder builder = getVariantBuilder(blockHolder.get());

        builder
                .partialState().with(BlockStateProperties.POWERED, false).modelForState().modelFile(pressurePlate).addModel()
                .partialState().with(BlockStateProperties.POWERED, true).modelForState().modelFile(pressurePlateDown).addModel();

//        simpleBlockItem(blockHolder.get(), pressurePlate);
    }

    private void buttonBlock(DFCExtendedRock rock)
    {
        ResourceLocation texture = dfcLoc("block/rock/raw/" + rock.getSerializedName());
        DeferredHolder<Block, Block> blockHolder = DFCBlocks.DFC_ROCK_BLOCKS.get(rock).get(DFCExtendedRock.DFCRockBlockType.BUTTON).holder();
        ModelFile button = this.models().button(getBlockModelString(blockHolder.getId()), texture);
        ModelFile buttonPressed = this.models().buttonPressed(getBlockModelString(blockHolder.getId()) + "_pressed", texture);
        ModelFile buttonInventory = this.models().buttonInventory(getBlockModelString(blockHolder.getId()) + "_inventory", texture);

        VariantBlockStateBuilder builder = getVariantBuilder(blockHolder.get());

        builder
                .partialState().with(BlockStateProperties.POWERED, false).modelForState().modelFile(button).addModel()
                .partialState().with(BlockStateProperties.POWERED, true).modelForState().modelFile(buttonPressed).addModel();

//        simpleBlockItem(blockHolder.get(), buttonInventory);
    }

    private void aqueductBlock(DFCExtendedRock rock)
    {
        ResourceLocation texture = getBlockModelLocation(DFCBlocks.DFC_ROCK_BLOCKS.get(rock).get(DFCExtendedRock.DFCRockBlockType.BRICKS).getId());
        DeferredHolder<Block, Block> blockHolder = DFCBlocks.DFC_ROCK_BLOCKS.get(rock).get(DFCExtendedRock.DFCRockBlockType.AQUEDUCT).holder();
        ModelFile modelBase = createModel(getBlockModelString(blockHolder.getId()) + "/base", tfcLoc("block/aqueduct/base")).texture("texture", texture).texture("particle", texture);
        ModelFile modelNorth = createModel(getBlockModelString(blockHolder.getId()) + "/north", tfcLoc("block/aqueduct/north")).texture("texture", texture).texture("particle", texture);
        ModelFile modelSouth = createModel(getBlockModelString(blockHolder.getId()) + "/south", tfcLoc("block/aqueduct/south")).texture("texture", texture).texture("particle", texture);
        ModelFile modelEast = createModel(getBlockModelString(blockHolder.getId()) + "/east", tfcLoc("block/aqueduct/east")).texture("texture", texture).texture("particle", texture);
        ModelFile modelWest = createModel(getBlockModelString(blockHolder.getId()) + "/west", tfcLoc("block/aqueduct/west")).texture("texture", texture).texture("particle", texture);

        MultiPartBlockStateBuilder builder = getMultipartBuilder(blockHolder.get());

        builder.part().modelFile(modelBase).addModel();
        builder.part().modelFile(modelNorth).addModel().condition(BlockStateProperties.NORTH, false);
        builder.part().modelFile(modelSouth).addModel().condition(BlockStateProperties.SOUTH, false);
        builder.part().modelFile(modelEast).addModel().condition(BlockStateProperties.EAST, false);
        builder.part().modelFile(modelWest).addModel().condition(BlockStateProperties.WEST, false);

//        simpleBlockItem(blockHolder.get(), modelBase);
    }

    private void pillarBlock(DeferredHolder<Block, Block> blockHolder)
    {
        ResourceLocation side = getBlockModelLocation(blockHolder.getId());
        ResourceLocation end = getBlockModelLocation(blockHolder.getId().withSuffix("_top"));
        ModelFile modelBase = createModel(getBlockModelString(blockHolder.getId()), vanillaLoc("block/cube_column")).texture("side", side).texture("end", end).texture("particle", end);

        VariantBlockStateBuilder builder = getVariantBuilder(blockHolder.get());

        builder
                .partialState().with(BlockStateProperties.AXIS, Direction.Axis.X).modelForState().modelFile(modelBase).rotationX(90).rotationY(90).addModel()
                .partialState().with(BlockStateProperties.AXIS, Direction.Axis.Y).modelForState().modelFile(modelBase).addModel()
                .partialState().with(BlockStateProperties.AXIS, Direction.Axis.Z).modelForState().modelFile(modelBase).rotationX(90).addModel();

//        simpleBlockItem(blockHolder.get(), modelBase);
    }

    private void columnBlock(DeferredHolder<Block, Block> blockHolder)
    {
        ResourceLocation texture = getBlockModelLocation(blockHolder.getId());
        ModelFile modelColumnBottom = createModel(getBlockModelString(blockHolder.getId()) + "_bottom", dfcLoc("block/rock/column_bottom")).texture("texture", texture).texture("particle", texture);
        ModelFile modelColumnCenterBottom = createModel(getBlockModelString(blockHolder.getId()) + "_center_bottom", dfcLoc("block/rock/column_center_bottom")).texture("texture", texture).texture("particle", texture);
        ModelFile modelColumnCenterBottomTuscan = createModel(getBlockModelString(blockHolder.getId()) + "_center_bottom_tuscan", dfcLoc("block/rock/column_center_bottom_tuscan")).texture("texture", texture).texture("particle", texture);
        ModelFile modelColumnCenterMiddleTop = createModel(getBlockModelString(blockHolder.getId()) + "_center_middle_top", dfcLoc("block/rock/column_center_middle_top")).texture("texture", texture).texture("particle", texture);
        ModelFile modelColumnCenterMiddleTopCorinthian = createModel(getBlockModelString(blockHolder.getId()) + "_center_middle_top_corinthian", dfcLoc("block/rock/column_center_middle_top_corinthian")).texture("texture", texture).texture("particle", texture);
        ModelFile modelColumnCenterMiddleTopTuscan = createModel(getBlockModelString(blockHolder.getId()) + "_center_middle_top_tuscan", dfcLoc("block/rock/column_center_middle_top_tuscan")).texture("texture", texture).texture("particle", texture);
        ModelFile modelColumnCenterTop = createModel(getBlockModelString(blockHolder.getId()) + "_center_top", dfcLoc("block/rock/column_center_top")).texture("texture", texture).texture("particle", texture);
        ModelFile modelColumnCenterTopTuscan = createModel(getBlockModelString(blockHolder.getId()) + "_center_top_tuscan", dfcLoc("block/rock/column_center_top_tuscan")).texture("texture", texture).texture("particle", texture);
        ModelFile modelColumnTop = createModel(getBlockModelString(blockHolder.getId()) + "_top", dfcLoc("block/rock/column_top")).texture("texture", texture).texture("particle", texture);
        ModelFile modelColumnTopIonicX = createModel(getBlockModelString(blockHolder.getId()) + "_top_ionic_x", dfcLoc("block/rock/column_top_ionic_x")).texture("texture", texture).texture("particle", texture);
        ModelFile modelColumnTopIonicZ = createModel(getBlockModelString(blockHolder.getId()) + "_top_ionic_z", dfcLoc("block/rock/column_top_ionic_z")).texture("texture", texture).texture("particle", texture);
        ModelFile modelColumnTopCorinthian = createModel(getBlockModelString(blockHolder.getId()) + "_top_corinthian", dfcLoc("block/rock/column_top_corinthian")).texture("texture", texture).texture("particle", texture);
        ModelFile modelColumnInventory = createModel(getBlockModelString(blockHolder.getId()) + "_inventory", dfcLoc("block/rock/column_inventory")).texture("texture", texture).texture("particle", texture);

        MultiPartBlockStateBuilder builder = getMultipartBuilder(blockHolder.get());

        builder
                .part().modelFile(modelColumnBottom).addModel().condition(BlockStateProperties.DOWN, true).end()
                .part().modelFile(modelColumnCenterBottom).addModel().condition(BlockStateProperties.DOWN, false).condition(DFCBlockStateProperties.STYLES, DFCBlockStateProperties.ColumnStyles.DORIC, DFCBlockStateProperties.ColumnStyles.IONIC, DFCBlockStateProperties.ColumnStyles.CORINTHIAN).end()
                .part().modelFile(modelColumnCenterBottomTuscan).addModel().condition(BlockStateProperties.DOWN, false).condition(DFCBlockStateProperties.STYLES, DFCBlockStateProperties.ColumnStyles.TUSCAN).end()
                .part().modelFile(modelColumnCenterMiddleTop).addModel().condition(BlockStateProperties.UP, true).condition(DFCBlockStateProperties.STYLES, DFCBlockStateProperties.ColumnStyles.DORIC, DFCBlockStateProperties.ColumnStyles.IONIC).end()
                .part().modelFile(modelColumnCenterMiddleTopCorinthian).addModel().condition(BlockStateProperties.UP, true).condition(DFCBlockStateProperties.STYLES, DFCBlockStateProperties.ColumnStyles.CORINTHIAN).end()
                .part().modelFile(modelColumnCenterMiddleTopTuscan).addModel().condition(BlockStateProperties.UP, true).condition(DFCBlockStateProperties.STYLES, DFCBlockStateProperties.ColumnStyles.TUSCAN).end()
                .part().modelFile(modelColumnCenterTop).addModel().condition(BlockStateProperties.UP, false).condition(DFCBlockStateProperties.STYLES, DFCBlockStateProperties.ColumnStyles.DORIC, DFCBlockStateProperties.ColumnStyles.IONIC, DFCBlockStateProperties.ColumnStyles.CORINTHIAN).end()
                .part().modelFile(modelColumnCenterTopTuscan).addModel().condition(BlockStateProperties.UP, false).condition(DFCBlockStateProperties.STYLES, DFCBlockStateProperties.ColumnStyles.TUSCAN).end()
                .part().modelFile(modelColumnTop).addModel().condition(BlockStateProperties.UP, true).condition(DFCBlockStateProperties.STYLES, DFCBlockStateProperties.ColumnStyles.DORIC, DFCBlockStateProperties.ColumnStyles.TUSCAN, DFCBlockStateProperties.ColumnStyles.CORINTHIAN).end()
                .part().modelFile(modelColumnTopIonicX).addModel().condition(BlockStateProperties.UP, true).condition(DFCBlockStateProperties.STYLES, DFCBlockStateProperties.ColumnStyles.IONIC).condition(BlockStateProperties.HORIZONTAL_AXIS, Direction.Axis.X).end()
                .part().modelFile(modelColumnTopIonicZ).addModel().condition(BlockStateProperties.UP, true).condition(DFCBlockStateProperties.STYLES, DFCBlockStateProperties.ColumnStyles.IONIC).condition(BlockStateProperties.HORIZONTAL_AXIS, Direction.Axis.Z).end()
                .part().modelFile(modelColumnTopCorinthian).addModel().condition(BlockStateProperties.UP, true).condition(DFCBlockStateProperties.STYLES, DFCBlockStateProperties.ColumnStyles.CORINTHIAN).end();

//        simpleBlockItem(blockHolder.get(), modelColumnInventory);
    }

    private void railBlock(DFCExtendedRock rock)
    {
        ResourceLocation texture = rock.isDFCRock() || rock.isBeneathRock() ? dfcLoc("block/rock/raw/" + rock.getSerializedName()) : rock.isSandstone() ? tfcLoc("block/sandstone/bottom/" + (rock.getSerializedName().replace("_sandstone", ""))) : tfcLoc("block/rock/raw/" + (rock.getSerializedName()));
        DeferredHolder<Block, Block> blockHolder = DFCBlocks.DFC_ROCK_BLOCKS.get(rock).get(DFCExtendedRock.DFCRockBlockType.RAIL).holder();
        ModelFile modelRailPost = createModel(getBlockModelString(blockHolder.getId()) + "_post", dfcLoc("block/rock/rail_post")).texture("rock", texture).texture("particle", texture);
        ModelFile modelRailSide = createModel(getBlockModelString(blockHolder.getId()) + "_side", dfcLoc("block/rock/rail_side")).texture("rock", texture).texture("particle", texture);
        ModelFile modelRailInventory = createModel(getBlockModelString(blockHolder.getId()) + "_inventory", dfcLoc("block/rock/rail_inventory")).texture("rock", texture).texture("particle", texture);

        MultiPartBlockStateBuilder builder = getMultipartBuilder(blockHolder.get());

        builder
                .part().modelFile(modelRailPost).addModel()
                .nestedGroup()
                .nestedGroup().condition(BlockStateProperties.NORTH, false).condition(BlockStateProperties.EAST, false).condition(BlockStateProperties.SOUTH, false).endNestedGroup()
                .nestedGroup().condition(BlockStateProperties.EAST, false).condition(BlockStateProperties.SOUTH, false).condition(BlockStateProperties.WEST, false).endNestedGroup()
                .nestedGroup().condition(BlockStateProperties.NORTH, false).condition(BlockStateProperties.SOUTH, false).condition(BlockStateProperties.WEST, false).endNestedGroup()
                .nestedGroup().condition(BlockStateProperties.NORTH, false).condition(BlockStateProperties.EAST, false).condition(BlockStateProperties.WEST, false).endNestedGroup()
                .nestedGroup().condition(BlockStateProperties.NORTH, true).condition(BlockStateProperties.EAST, true).endNestedGroup()
                .nestedGroup().condition(BlockStateProperties.EAST, true).condition(BlockStateProperties.SOUTH, true).endNestedGroup()
                .nestedGroup().condition(BlockStateProperties.SOUTH, true).condition(BlockStateProperties.WEST, true).endNestedGroup()
                .nestedGroup().condition(BlockStateProperties.NORTH, true).condition(BlockStateProperties.WEST, true).endNestedGroup()
                .useOr().end().end()
                .part().modelFile(modelRailSide).rotationY(180).uvLock(true).addModel().condition(BlockStateProperties.NORTH, true).end()
                .part().modelFile(modelRailSide).rotationY(270).uvLock(true).addModel().condition(BlockStateProperties.EAST, true).end()
                .part().modelFile(modelRailSide).uvLock(true).addModel().condition(BlockStateProperties.SOUTH, true).end()
                .part().modelFile(modelRailSide).rotationY(90).uvLock(true).addModel().condition(BlockStateProperties.WEST, true).end();

//        simpleBlockItem(blockHolder.get(), modelRailInventory);
    }

    private void railBlockBlock(DFCExtendedRock rock)
    {
        ResourceLocation straightRailTexture = ResourceLocation.fromNamespaceAndPath("dfc", "block/rock/rail/" + rock.getSerializedName() + "_straight");
        ResourceLocation curvedRailTexture = ResourceLocation.fromNamespaceAndPath("dfc", "block/rock/rail/" + rock.getSerializedName() + "_curved");
        DeferredHolder<Block, Block> blockHolder = DFCBlocks.DFC_ROCK_BLOCKS.get(rock).get(DFCExtendedRock.DFCRockBlockType.RAIL_BLOCK).holder();
        ModelFile modelRailStraight = createModel(getBlockModelString(blockHolder.getId()) + "_straight", dfcLoc("block/rock/rail_block")).texture("rail", straightRailTexture);
        ModelFile modelRailCurved = createModel(getBlockModelString(blockHolder.getId()) + "_curved", dfcLoc("block/rock/rail_block")).texture("rail", curvedRailTexture);

        ModelFile rawStone = new ModelFile.UncheckedModelFile(rock.isDFCRock() ? getBlockModelString(DFCBlocks.DFC_ROCK_BLOCKS.get(rock).get(DFCExtendedRock.DFCRockBlockType.RAW).getId()) : rock.isSandstone() ? getBlockModelString(tfcLoc("raw_sandstone/" + (rock.getSerializedName().replace("_sandstone", "")))) : getBlockModelString(tfcLoc("rock/raw/" + (rock.getSerializedName()))));

        MultiPartBlockStateBuilder builder = getMultipartBuilder(blockHolder.get());

        builder
                .part().modelFile(rawStone).addModel().end()
                .part().modelFile(modelRailStraight).addModel().condition(BlockStateProperties.RAIL_SHAPE, RailShape.NORTH_SOUTH).end()
                .part().modelFile(modelRailStraight).rotationY(90).addModel().condition(BlockStateProperties.RAIL_SHAPE, RailShape.EAST_WEST).end()
                .part().modelFile(modelRailCurved).rotationY(270).addModel().condition(BlockStateProperties.RAIL_SHAPE, RailShape.NORTH_EAST).end()
                .part().modelFile(modelRailCurved).rotationY(180).addModel().condition(BlockStateProperties.RAIL_SHAPE, RailShape.NORTH_WEST).end()
                .part().modelFile(modelRailCurved).addModel().condition(BlockStateProperties.RAIL_SHAPE, RailShape.SOUTH_EAST).end()
                .part().modelFile(modelRailCurved).rotationY(90).addModel().condition(BlockStateProperties.RAIL_SHAPE, RailShape.SOUTH_WEST).end();
    }

    private void railStairsBlock(DFCExtendedRock rock)
    {
        ResourceLocation straightRailTexture = ResourceLocation.fromNamespaceAndPath("dfc", "block/rock/rail/" + rock.getSerializedName() + "_straight");
        DeferredHolder<Block, Block> blockHolder = DFCBlocks.DFC_ROCK_BLOCKS.get(rock).get(DFCExtendedRock.DFCRockBlockType.RAIL_STAIRS_BLOCK).holder();
        ModelFile modelRailStairs = createModel(getBlockModelString(blockHolder.getId()), dfcLoc("block/rock/rail_stairs_block")).texture("rail", straightRailTexture);

        ModelFile rawStone = new ModelFile.UncheckedModelFile(rock.isDFCRock() ? getBlockModelString(DFCBlocks.DFC_ROCK_BLOCKS.get(rock).get(DFCExtendedRock.DFCRockBlockType.RAW).getId()) + "_stairs" : rock.isSandstone() ? getBlockModelString(tfcLoc("raw_sandstone/" + (rock.getSerializedName().replace("_sandstone", "")) + "_stairs")) : getBlockModelString(tfcLoc("rock/raw/" + (rock.getSerializedName()))) + "_stairs");

        MultiPartBlockStateBuilder builder = getMultipartBuilder(blockHolder.get());

        builder
                .part().modelFile(rawStone).uvLock(true).addModel().condition(BlockStateProperties.FACING, Direction.EAST).end()
                .part().modelFile(rawStone).rotationY(270).uvLock(true).addModel().condition(BlockStateProperties.FACING, Direction.NORTH).end()
                .part().modelFile(rawStone).rotationY(180).uvLock(true).addModel().condition(BlockStateProperties.FACING, Direction.WEST).end()
                .part().modelFile(rawStone).rotationY(90).uvLock(true).addModel().condition(BlockStateProperties.FACING, Direction.SOUTH).end()
                .part().modelFile(modelRailStairs).rotationY(270).addModel().condition(BlockStateProperties.FACING, Direction.EAST).end()
                .part().modelFile(modelRailStairs).rotationY(180).addModel().condition(BlockStateProperties.FACING, Direction.NORTH).end()
                .part().modelFile(modelRailStairs).rotationY(90).addModel().condition(BlockStateProperties.FACING, Direction.WEST).end()
                .part().modelFile(modelRailStairs).addModel().condition(BlockStateProperties.FACING, Direction.SOUTH).end();
    }

    private void makeRockState(DFCExtendedRock rock, DFCExtendedRock.DFCRockBlockType rockType)
    {
        switch (rockType)
        {
            case RAW:
            {
//                ModelFile modelUpright = RockHelpers.getRockBlockModel(rock, rockType, 0);
//                rawRockBlock(DFCBlocks.DFC_ROCK_BLOCKS.get(rock).get(rockType).get(), );
                break;
            }
            case HARDENED:
            {
                break;
            }
            case PRESSURE_PLATE:
            {
                break;
            }
            case BUTTON:
            {
                break;
            }
            case LOOSE:
            {
                break;
            }
            case SPIKE:
            {
                break;
            }
            case AQUEDUCT:
            {
                break;
            }
            case CHISELED:
            {
                break;
            }
            case ROPE_ANCHOR:
            {
                break;
            }
            case MOSSY_LOOSE:
            {
                break;
            }
            case PILLAR:
            {
                break;
            }
            case COLUMN:
            {
                break;
            }
            case RAIL:
            {
                break;
            }
            case RAIL_BLOCK:
            {
                break;
            }
            case RAIL_STAIRS_BLOCK:
            {
                break;
            }
            case ROAD:
            {
                break;
            }
            case OUTLINED:
            {
                break;
            }
            case POLISHED:
            {
                break;
            }
            case LARGE_BRICKS:
            {
                break;
            }
            //everything thats just a default cube
            default:
            {

            }
        }
        if (rockType.hasVariants())
        {

        }
    }

    private ModelFile anvilBlockModel(DFCExtendedRock rock)
    {
        ResourceLocation texture = dfcLoc("block/rock/raw/" + rock.getSerializedName());

        return createModel(getBlockModelString(DFCBlocks.DFC_ROCK_ANVILS.get(rock).getId()), tfcLoc("block/rock/anvil")).texture("texture", texture).texture("particle", texture);
    }

    private String getLooseRockModelParent(DFCExtendedRock rock, int count){
        RockCategory category = rock.displayCategory().category();

        switch (category){
            case METAMORPHIC -> {
                return "tfc:block/rock/loose_metamorphic_" + count;
            }
            case SEDIMENTARY -> {
                return "tfc:block/rock/loose_sedimentary_" + count;
            }
            case IGNEOUS_EXTRUSIVE -> {
                return "tfc:block/rock/loose_igneous_extrusive_" + count;
            }
            case IGNEOUS_INTRUSIVE -> {
                return "tfc:block/rock/loose_igneous_intrusive_" + count;
            }
            default -> throw new AssertionError("No category found for rock: " + rock.getSerializedName());
        }

    }

    private String getBlockModelString(ResourceLocation block)
    {
        return block.getNamespace() + ":block/" + block.getPath();
    }

    private ResourceLocation getBlockModelLocation(ResourceLocation block)
    {
        return ResourceLocation.fromNamespaceAndPath(block.getNamespace(), "block/" + block.getPath());
    }

    private ModelBuilder<BlockModelBuilder> createModel(String name, ResourceLocation parent)
    {
        return this.models().withExistingParent(name, parent);
    }

    private ModelBuilder<BlockModelBuilder> createModel(String name, String parent)
    {
        return this.models().withExistingParent(name, parent);
    }

    public ResourceLocation dfcLoc(String name) {
        return ResourceLocation.fromNamespaceAndPath(DecoFirmaCraft.MOD_ID, name);
    }

    public ResourceLocation tfcLoc(String name) {
        return ResourceLocation.fromNamespaceAndPath(TerraFirmaCraft.MOD_ID, name);
    }

    public ResourceLocation vanillaLoc(String name) {
        return ResourceLocation.fromNamespaceAndPath("minecraft", name);
    }
}

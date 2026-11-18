package com.advancedposturing.decofirmacraft.util;

import com.advancedposturing.decofirmacraft.common.block.metal.DFCExtendedMetal;
import com.advancedposturing.decofirmacraft.common.block.rock.DFCExtendedRock;
import com.advancedposturing.decofirmacraft.common.block.state.DFCBlockStateProperties;
import com.advancedposturing.decofirmacraft.common.block.wood.DFCExtendedWood;
import net.dries007.tfc.util.registry.RegistryRock;
import net.dries007.tfc.util.registry.RegistryWood;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.material.MapColor;

import java.util.function.Supplier;

import static com.advancedposturing.decofirmacraft.DecoFirmaCraft.MOD_ID;

public class DFCHelpers
{
    public interface DFCRockRegistry extends StringRepresentable, RegistryRock
    {

        /**
         * @return A custom material color for this rock type, which affects map color and raw rock unsupported particle colors.
         */
        Supplier<? extends Block> dfcGetBlock(DFCExtendedRock.DFCRockBlockType type);

        Supplier<? extends SlabBlock> dfcGetSlab(DFCExtendedRock.DFCRockBlockType type);

        Supplier<? extends StairBlock> dfcGetStair(DFCExtendedRock.DFCRockBlockType type);

        Supplier<? extends WallBlock> dfcGetWall(DFCExtendedRock.DFCRockBlockType type);
    }

    public interface DFCMetalRegistry extends StringRepresentable
    {

        Block getBlock(DFCExtendedMetal.DFCMetalBlockType type);

        MapColor mapColor();

        Rarity rarity();

        default boolean weatheredParts()
        {
            return weatheringResistance() != -1;
        }

        float weatheringResistance();
    }

    public interface DFCWoodRegistry extends StringRepresentable, RegistryWood
    {
        MapColor woodColor();

        MapColor barkColor();

        TreeGrower tree();

        Supplier<Integer> ticksToGrow();

        /**
         * @return The vertical coordinate (from 0-255) on the foliage_fall colormap for this wood type's leaves.
         */
        int autumnIndex();

        /**
         * @return A block of this wood, of the provided type. It is <strong>not necessary</strong> to implement this for every type, only the ones that are needed.
         */
        Supplier<Block> getBlock(DFCExtendedWood.DFCWoodBlockType type);

        BlockSetType getBlockSet();

        WoodType getVanillaWoodType();
    }

    public static ResourceLocation identifier(String name)
    {
        return resourceLocation(MOD_ID, name);
    }

    public static ResourceLocation identifierMC(String name)
    {
        return resourceLocation("minecraft", name);
    }

    public static ResourceLocation resourceLocation(String name)
    {
        return ResourceLocation.parse(name);
    }

    public static ResourceLocation resourceLocation(String domain, String path)
    {
        return ResourceLocation.fromNamespaceAndPath(domain, path);
    }

    /**
     * @param pos1 - original position
     * @param pos2 - returns horizontal direction of this position relative to the original position
     * @return
     */
    public static Direction horizontalDirectionFromPos(BlockPos pos1, BlockPos pos2)
    {
        final int x1 = pos1.getX();
        final int z1 = pos1.getZ();
        final int x2 = pos2.getX();
        final int z2 = pos2.getZ();

        int x3 = x1 - x2;
        int z3 = z1 - z2;
        return Math.abs(x3) > Math.abs(z3) ? x3 > 0 ? Direction.WEST : Direction.EAST : z3 > 0 ? Direction.NORTH : Direction.SOUTH;
    }

    public static IntegerProperty directionPropertyFromDirection(Direction direction)
    {
        return switch (direction)
        {
            case NORTH -> DFCBlockStateProperties.NORTH;
            case EAST -> DFCBlockStateProperties.EAST;
            case SOUTH -> DFCBlockStateProperties.SOUTH;
            case WEST -> DFCBlockStateProperties.WEST;
            default -> DFCBlockStateProperties.NORTH;
        };
    }
}

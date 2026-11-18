package com.advancedposturing.decofirmacraft.common;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.*;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.ticks.LevelTickAccess;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class ChiseledBlockSubLevel implements LevelAccessor
{
    //todo - structure ---
    // store map here of blockpos, keyed to blockstates, keyed to the blockpos of a chiseled block.
    // necessary methods - add block, remove blockstate, alter blockstate, update blockstates (updates all states from a chiseled block)
    // useon and useitemon methods, chiseled block requests one of its own states to be queried by passing its pos and the state requested
    // setBlock will request the chiseled block that owns that block to change its state internally, while also altering the map
    // chiseled blocks tick will tick all block entities in here keyed to it
    // chiseledblocks block entity renderer will query all BERs of all blockentities inside of here using its parameters, while adjusting
    // their positions to be that of the chiseled block.
    // getBlock will return the block relative to the chiseled block of the block that requested it
    // onplace, onremove get simulated

    //todo - uhmmmm idk
    @Override
    public @Nullable BlockEntity getBlockEntity(BlockPos blockPos) {
        return null;
    }

    //idk
    @Override
    public boolean isStateAtPosition(BlockPos blockPos, Predicate<BlockState> predicate) {
        return false;
    }

    //idk
    @Override
    public boolean isFluidAtPosition(BlockPos blockPos, Predicate<FluidState> predicate) {
        return false;
    }

    //todo - uhmmmm idk
    @Override
    public <T extends BlockEntity> Optional<T> getBlockEntity(BlockPos pos, BlockEntityType<T> blockEntityType) {
        return LevelAccessor.super.getBlockEntity(pos, blockEntityType);
    }

    //idk how to handle this exactly, ideally itd get the blockpos at the real world relative to the chiseled block the block
    // calling this is inside of
    @Override
    public BlockState getBlockState(BlockPos blockPos) {
        return null;
    }

    //useless
    @Override
    public FluidState getFluidState(BlockPos blockPos) {
        return null;
    }

    //useless
    @Override
    public int getHeight() {
        return 0;
    }

    //useless
    @Override
    public RegistryAccess registryAccess() {
        return null;
    }

    //useless
    @Override
    public FeatureFlagSet enabledFeatures() {
        return null;
    }

    //useless
    @Override
    public @Nullable ChunkAccess getChunk(int i, int i1, ChunkStatus chunkStatus, boolean b) {
        return null;
    }

    //idk
    @Override
    public int getHeight(Heightmap.Types types, int i, int i1) {
        return 0;
    }

    //useless
    @Override
    public int getSkyDarken() {
        return 0;
    }

    //useless
    @Override
    public BiomeManager getBiomeManager() {
        return null;
    }

    //useless
    @Override
    public Holder<Biome> getUncachedNoiseBiome(int i, int i1, int i2) {
        return null;
    }

    //idk
    @Override
    public boolean isClientSide() {
        return false;
    }

    //useless
    @Override
    public int getSeaLevel() {
        return 0;
    }

    //idk
    @Override
    public DimensionType dimensionType() {
        return null;
    }

    //useless
    @Override
    public int getMinBuildHeight() {
        return 0;
    }

    //todo - this will change the block at the blockpos, and then pass that back up to its chiseledblock to change the state in its array
    @Override
    public boolean setBlock(BlockPos blockPos, BlockState blockState, int i, int i1) {
        return false;
    }

    //todo - this will change the block at the blockpos, and then pass that back up to its chiseledblock to change the state in its array
    @Override
    public boolean setBlock(BlockPos pos, BlockState newState, int flags) {
        return LevelAccessor.super.setBlock(pos, newState, flags);
    }

    //todo - this will remove the block from the map that stores blocks keyed to blockposes keyed to blockstates keyed to a final blockpos
    // that represents a chiseled block in the real world
    @Override
    public boolean removeBlock(BlockPos blockPos, boolean b) {
        return false;
    }

    //todo - this will remove the block from the map that stores blocks keyed to blockposes keyed to blockstates keyed to a final blockpos
    // that represents a chiseled block in the real world
    @Override
    public boolean destroyBlock(BlockPos pos, boolean dropBlock) {
        return LevelAccessor.super.destroyBlock(pos, dropBlock);
    }

    //todo - this will remove the block from the map that stores blocks keyed to blockposes keyed to blockstates keyed to a final blockpos
    // that represents a chiseled block in the real world
    @Override
    public boolean destroyBlock(BlockPos pos, boolean dropBlock, @Nullable Entity entity) {
        return LevelAccessor.super.destroyBlock(pos, dropBlock, entity);
    }

    //todo - this will remove the block from the map that stores blocks keyed to blockposes keyed to blockstates keyed to a final blockpos
    // that represents a chiseled block in the real world
    @Override
    public boolean destroyBlock(BlockPos blockPos, boolean b, @Nullable Entity entity, int i) {
        return false;
    }

    //TODO - this will run for chiseled blocks to get the modelData of internal blockmodels, if they provide it
    @Override
    public ModelData getModelData(BlockPos pos)
    {
        return LevelAccessor.super.getModelData(pos);
    }

    //BELOW THIS IS USELESS SHIT I DONT NEED

    @Override
    public long nextSubTickCount() {
        return 0;
    }

    @Override
    public LevelTickAccess<Block> getBlockTicks() {
        return null;
    }

    @Override
    public LevelTickAccess<Fluid> getFluidTicks() {
        return null;
    }

    @Override
    public LevelData getLevelData() {
        return null;
    }

    @Override
    public DifficultyInstance getCurrentDifficultyAt(BlockPos blockPos) {
        return null;
    }

    @Override
    public @Nullable MinecraftServer getServer() {
        return null;
    }

    @Override
    public ChunkSource getChunkSource() {
        return null;
    }

    @Override
    public RandomSource getRandom() {
        return null;
    }

    @Override
    public void playSound(@Nullable Player player, BlockPos blockPos, SoundEvent soundEvent, SoundSource soundSource, float v, float v1) {

    }

    @Override
    public void addParticle(ParticleOptions particleOptions, double v, double v1, double v2, double v3, double v4, double v5) {

    }

    @Override
    public void levelEvent(@Nullable Player player, int i, BlockPos blockPos, int i1) {

    }

    @Override
    public void gameEvent(Holder<GameEvent> holder, Vec3 vec3, GameEvent.Context context) {

    }

    @Override
    public float getShade(Direction direction, boolean b) {
        return 0;
    }

    @Override
    public LevelLightEngine getLightEngine() {
        return null;
    }

    @Override
    public WorldBorder getWorldBorder() {
        return null;
    }

    @Override
    public List<Entity> getEntities(@Nullable Entity entity, AABB aabb, Predicate<? super Entity> predicate) {
        return List.of();
    }

    @Override
    public <T extends Entity> List<T> getEntities(EntityTypeTest<Entity, T> entityTypeTest, AABB aabb, Predicate<? super T> predicate) {
        return List.of();
    }

    @Override
    public List<? extends Player> players() {
        return List.of();
    }
}

package com.advancedposturing.decofirmacraft.util;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;

//needed a random source with a single static value so i can force out weighted blockstate models
public class FakeRandomSource implements RandomSource
{
    final int value;

    public FakeRandomSource(int value)
    {
        this.value = value;
    }

    @Override
    public RandomSource fork() {
        return null;
    }

    @Override
    public PositionalRandomFactory forkPositional() {
        return null;
    }

    @Override
    public void setSeed(long l)
    {
    }

    @Override
    public int nextInt() {
        return value;
    }

    @Override
    public int nextInt(int i) {
        return value;
    }

    @Override
    public long nextLong() {
        return value;
    }

    @Override
    public boolean nextBoolean() {
        return false;
    }

    @Override
    public float nextFloat() {
        return value;
    }

    @Override
    public double nextDouble() {
        return value;
    }

    @Override
    public double nextGaussian() {
        return 0;
    }
}

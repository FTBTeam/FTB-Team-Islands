package com.feed_the_beast.mods.teamislands;

import com.mojang.serialization.Codec;
import net.minecraft.block.BlockState;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryLookupCodec;
import net.minecraft.world.Blockreader;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeManager;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.biome.provider.SingleBiomeProvider;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.*;
import net.minecraft.world.gen.feature.structure.StructureManager;
import net.minecraft.world.gen.settings.DimensionStructuresSettings;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class VoidChunkGenerator extends ChunkGenerator {
    public static final Codec<VoidChunkGenerator> CODEC = RegistryLookupCodec.create(Registry.BIOME_REGISTRY)
        .xmap(VoidChunkGenerator::new, VoidChunkGenerator::biomes).stable().codec();

    private final Registry<Biome> biomes;
    public VoidChunkGenerator(Registry<Biome> biomes) {
        super(new SingleBiomeProvider(biomes.getOrThrow(Biomes.PLAINS)), new DimensionStructuresSettings(false));
        this.biomes = biomes;
    }

    public Registry<Biome> biomes() {
        return this.biomes;
    }

    @Override
    protected Codec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public ChunkGenerator withSeed(long p_230349_1_) {
        return this;
    }

    @Override
    public void buildSurfaceAndBedrock(WorldGenRegion p_225551_1_, IChunk p_225551_2_) {
    }

    @Override
    public void applyCarvers(long p_230350_1_, BiomeManager p_230350_3_, IChunk p_230350_4_, GenerationStage.Carving p_230350_5_) {
    }

    @Override
    public void applyBiomeDecoration(WorldGenRegion p_230351_1_, StructureManager p_230351_2_) {
    }

    @Override
    public void fillFromNoise(IWorld p_230352_1_, StructureManager p_230352_2_, IChunk p_230352_3_) {
    }

    @Override
    public int getBaseHeight(int p_222529_1_, int p_222529_2_, Heightmap.Type p_222529_3_) {
        return 0;
    }

    @Override
    public IBlockReader getBaseColumn(int p_230348_1_, int p_230348_2_) {
        return new Blockreader(new BlockState[0]);
    }
}

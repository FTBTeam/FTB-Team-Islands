package com.feed_the_beast.mods.teamislands;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryLookupCodec;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.StructureSettings;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class VoidChunkGenerator extends ChunkGenerator {
    public static final Codec<VoidChunkGenerator> CODEC = RegistryLookupCodec.create(Registry.BIOME_REGISTRY)
        .xmap(VoidChunkGenerator::new, VoidChunkGenerator::biomes).stable().codec();

    private final Registry<Biome> biomes;
    public VoidChunkGenerator(Registry<Biome> biomes) {
        super(new FixedBiomeSource(biomes.getOrThrow(Biomes.PLAINS)), new StructureSettings(false));
        this.biomes = biomes;
    }

    public Registry<Biome> biomes() {
        return this.biomes;
    }

    @Override
    protected Codec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    @OnlyIn(Dist.CLIENT)
    public ChunkGenerator withSeed(long p_230349_1_) {
        return this;
    }

    @Override
    public void buildSurfaceAndBedrock(WorldGenRegion p_225551_1_, ChunkAccess p_225551_2_) {
    }

    @Override
    public void applyCarvers(long p_230350_1_, BiomeManager p_230350_3_, ChunkAccess p_230350_4_, GenerationStep.Carving p_230350_5_) {
    }

    @Override
    public void applyBiomeDecoration(WorldGenRegion p_230351_1_, StructureFeatureManager p_230351_2_) {
    }

    @Override
    public void fillFromNoise(LevelAccessor p_230352_1_, StructureFeatureManager p_230352_2_, ChunkAccess p_230352_3_) {
    }

    @Override
    public int getBaseHeight(int p_222529_1_, int p_222529_2_, Heightmap.Types p_222529_3_) {
        return 0;
    }

    @Override
    public BlockGetter getBaseColumn(int p_230348_1_, int p_230348_2_) {
        return new NoiseColumn(new BlockState[0]);
    }
}

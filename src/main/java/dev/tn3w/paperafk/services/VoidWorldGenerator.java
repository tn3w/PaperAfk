package dev.tn3w.paperafk.services;

import java.util.Random;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;

public class VoidWorldGenerator extends ChunkGenerator {

  @Override
  public boolean shouldGenerateCaves() {
    return false;
  }

  @Override
  public boolean shouldGenerateDecorations() {
    return false;
  }

  @Override
  public boolean shouldGenerateMobs() {
    return false;
  }

  @Override
  public boolean shouldGenerateStructures() {
    return false;
  }

  @Override
  public void generateNoise(
      WorldInfo worldInfo,
      Random random,
      int chunkX,
      int chunkZ,
      ChunkGenerator.ChunkData chunkData) {
    // Generate a void world
  }
}

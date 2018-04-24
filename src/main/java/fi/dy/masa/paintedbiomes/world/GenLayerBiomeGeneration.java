package fi.dy.masa.paintedbiomes.world;

import net.minecraft.world.WorldType;
import net.minecraft.world.gen.ChunkGeneratorSettings;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.GenLayerBiome;
import fi.dy.masa.paintedbiomes.image.handler.BiomeHandler;

public class GenLayerBiomeGeneration extends GenLayerBiome
{
    public GenLayerBiomeGeneration(long seed, GenLayer parent, WorldType worldType, ChunkGeneratorSettings chunkProviderSettings)
    {
        super(seed, parent, worldType, chunkProviderSettings);
    }

    @Override
    public int[] getInts(int x, int z, int width, int length)
    {
        int[] ints = this.parent.getInts(x, z, width, length);

        int i = 0;
        int endX = x + width;
        int endZ = z + length;
        BiomeHandler biomeHandler = BiomeHandler.getBiomeHandler(0);

        for (int tz = z; tz < endZ; ++tz)
        {
            for (int tx = x; tx < endX; ++tx)
            {
                ints[i] = biomeHandler.getBiomeIDAt(tx << 2, tz << 2, ints[i]);
                i++;
            }
        }

        return ints;
    }
}

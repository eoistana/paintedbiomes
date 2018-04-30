package fi.dy.masa.paintedbiomes.image.handler;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import org.apache.commons.lang3.StringUtils;

import fi.dy.masa.paintedbiomes.config.Configs;
import fi.dy.masa.paintedbiomes.mappings.ColorToBiomeMapping;
import fi.dy.masa.paintedbiomes.image.reader.IImageReader;
import fi.dy.masa.paintedbiomes.image.reader.ImageReaderSingle;
import fi.dy.masa.paintedbiomes.image.reader.ImageReaderSingleRepeating;

public class BiomeHandler extends Handler<BiomeHandler>
{
    private int unpaintedAreaBiomeID;

    public BiomeHandler(int dimension)
    {
        super(dimension);
    }

    public static BiomeHandler getBiomeHandler(int dimension)
    {        
        return getHandler(BiomeHandler.class, dimension);
    }

    public boolean isBiomeDefinedAt(int blockX, int blockZ)
    {
        if (isLocationCoveredByTemplate(blockX, blockZ) == false || getImageAlpha(blockX, blockZ) == 0)
        {
            return false;
        }

        int biomeID = ColorToBiomeMapping.getInstance().getBiomeIDForColor(this.getRGB(blockX, blockZ));
        return biomeID != -1;
    }

    public int getBiomeIDAt(int blockX, int blockZ, int defaultBiomeID)
    {
        // Outside area or completely transparent pixel
        if (isLocationCoveredByTemplate(blockX, blockZ) == false || getImageAlpha(blockX, blockZ) == 0)
        {
            return getUnpaintedBiomeID(defaultBiomeID);
        }

        int biomeID = ColorToBiomeMapping.getInstance().getBiomeIDForColor(this.getRGB(blockX, blockZ));
        return biomeID != -1 ? biomeID : this.getUnpaintedBiomeID(defaultBiomeID);
    }

    private int getUnpaintedBiomeID(int defaultBiomeID) {
        // If there is a biome defined for unpainted areas, then use that,
        // otherwise use the biome from the regular terrain generation
        return this.unpaintedAreaBiomeID != -1 ? this.unpaintedAreaBiomeID : defaultBiomeID;
    }


    protected static int getBiomeIDForRegistryName(String regName)
    {
        if (StringUtils.isBlank(regName) == false)
        {
            Biome biome = ForgeRegistries.BIOMES.getValue(new ResourceLocation(regName));
            return biome != null ? Biome.getIdForBiome(biome) : -1;
        }
        return -1;
    }

    @Override
    protected void onInit(Configs configs)
    {
        this.unpaintedAreaBiomeID = getBiomeIDForRegistryName(configs.unpaintedAreaBiomeName);
        
    }

    @Override
    protected IImageReader getImageReader()
    {
        Configs conf = Configs.getConfig(this.dimension);
        BlockPos pos = new BlockPos(conf.templateAlignmentX, 0, conf.templateAlignmentZ);
        if(this.useSingleTemplateImage)
            if(conf.useTemplateRepeating)
                return new ImageReaderSingleRepeating(this.templatePath, "biomes", pos, conf);
            else 
                return new ImageReaderSingle(this.templatePath, "biomes", pos);
        return null;
    }


}

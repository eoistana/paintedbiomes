package fi.dy.masa.paintedbiomes.image.reader;

import java.io.File;

import fi.dy.masa.paintedbiomes.config.Configs;
import net.minecraft.util.math.BlockPos;

public class ImageReaderSingleRepeating extends ImageReaderSingle
{
    protected final boolean repeatXNeg;
    protected final boolean repeatXPos;
    protected final boolean repeatZNeg;
    protected final boolean repeatZPos;
    
    protected final int repeatTemplateNegativeX;
    protected final int repeatTemplatePositiveX;
    protected final int repeatTemplateNegativeZ;
    protected final int repeatTemplatePositiveZ;

    public ImageReaderSingleRepeating(File imagePath, String imageName, BlockPos initPos, Configs conf)
    {
        super(imagePath, imageName, initPos);
        this.repeatTemplateNegativeX = conf.repeatTemplateNegativeX;
        this.repeatTemplatePositiveX = conf.repeatTemplatePositiveX;
        this.repeatTemplateNegativeZ = conf.repeatTemplateNegativeZ;
        this.repeatTemplatePositiveZ = conf.repeatTemplatePositiveZ;
        
        this.repeatXNeg = this.repeatTemplateNegativeX != 0;
        this.repeatXPos = this.repeatTemplatePositiveX != 0;
        this.repeatZNeg = this.repeatTemplateNegativeZ != 0;
        this.repeatZPos = this.repeatTemplatePositiveZ != 0;
    }
    
    @Override
    public int getImageAlpha(int blockX, int blockZ)
    {
        BlockPos pos = new BlockPos(blockX, 0, blockZ);
        BlockPos translatedToAreaPos = pos.subtract(worldPosMin);
        
        int areaX = translatedToAreaPos.getX();
        int areaZ = translatedToAreaPos.getZ();
        int newBlockX = areaX;
        int newBlockZ = areaZ;
        if(this.repeatTemplateNegativeX == 2 && areaX < this.worldPosMin.getX()) newBlockX = worldPosMin.getX();
        if(this.repeatTemplatePositiveX == 2 && areaX > this.worldPosMax.getX()) newBlockX = worldPosMax.getX();
        if(this.repeatTemplateNegativeZ == 2 && areaZ < this.worldPosMin.getZ()) newBlockZ = worldPosMin.getZ();
        if(this.repeatTemplatePositiveZ == 2 && areaZ > this.worldPosMax.getZ()) newBlockZ = worldPosMax.getZ();
        return super.getImageAlpha(newBlockX, newBlockZ);
    }
    
    @Override
    public int getRGB(int blockX, int blockZ)
    {
        BlockPos pos = new BlockPos(blockX, 0, blockZ);
        BlockPos translatedToAreaPos = pos.subtract(worldPosMin);
        
        int areaX = translatedToAreaPos.getX();
        int areaZ = translatedToAreaPos.getZ();
        int newBlockX = areaX;
        int newBlockZ = areaZ;
        if(this.repeatTemplateNegativeX == 2 && areaX < this.worldPosMin.getX()) newBlockX = worldPosMin.getX();
        if(this.repeatTemplatePositiveX == 2 && areaX > this.worldPosMax.getX()) newBlockX = worldPosMax.getX();
        if(this.repeatTemplateNegativeZ == 2 && areaZ < this.worldPosMin.getZ()) newBlockZ = worldPosMin.getZ();
        if(this.repeatTemplatePositiveZ == 2 && areaZ > this.worldPosMax.getZ()) newBlockZ = worldPosMax.getZ();
        return super.getRGB(newBlockX, newBlockZ);
    }
    
    @Override
    protected BlockPos getPosTranslatedToImage(BlockPos pos)
    {
        BlockPos translatedToAreaPos = pos.subtract(worldPosMin);
        int i = translatedToAreaPos.getX();
        int j = translatedToAreaPos.getZ();

        int areaX = i;
        int areaZ = j;
        if(!repeatXNeg && i < 0)
        {
            areaX = 0;
        }
        else if(!repeatXPos && i >= this.areaSizeX)
        {
            areaX = this.areaSizeX - 1;
        }
        else
        {
            areaX %= this.areaSizeX;
        }
        
        if(!repeatZNeg && j < 0)
        {
            areaZ = 0;
        }
        else if(!repeatZPos && j >= this.areaSizeZ)
        {
            areaZ = this.areaSizeZ - 1;
        }
        else
        {
            areaZ %= this.areaSizeZ;
        }
        
        BlockPos clampedByRepeatingImage = new BlockPos(areaX, 0, areaZ);
        BlockPos rotatedToImageDirection = rotateAndFlip(clampedByRepeatingImage, reverseTemplateRotation, templateFlip);

        int x = rotatedToImageDirection.getX();
        int z = rotatedToImageDirection.getZ();
        return new BlockPos(x<0?this.imageWidth+x:x, 0, z<0?this.imageHeight+z:z);
    }

    @Override
    public boolean isLocationCoveredByTemplate(int blockX, int blockZ)
    {
        return (repeatXNeg || blockX >= this.worldPosMin.getX())
            && (repeatXPos || blockX <= this.worldPosMax.getX())
            && (repeatZNeg || blockZ >= this.worldPosMin.getZ())
            && (repeatZPos || blockZ <= this.worldPosMax.getZ());
    }
}

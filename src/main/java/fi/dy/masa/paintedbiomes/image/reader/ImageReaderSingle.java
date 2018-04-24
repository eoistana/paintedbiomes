package fi.dy.masa.paintedbiomes.image.reader;

import java.awt.image.BufferedImage;
import java.io.File;

import net.minecraft.util.math.BlockPos;

public class ImageReaderSingle extends ImageReaderBase
{
    protected String fileNamePrefix;
    protected BufferedImage imageData;

    protected int minX;
    protected int maxX;
    protected int minZ;
    protected int maxZ;

    protected BlockPos worldPosMin;
    protected BlockPos worldPosMax;
    protected BlockPos areaSize;

    protected int templateRotation;
    protected int reverseTemplateRotation;
    protected int templateFlip;

    protected int worldX;
    protected int worldZ;
    protected int areaSizeX;
    protected int areaSizeZ;


    protected long lastAccessed;


    public ImageReaderSingle(File imagePath, String imageName, BlockPos initPos)
    {
        super(imagePath);
        this.fileNamePrefix = imageName;
        
        this.worldPosMin = this.worldPosMax = initPos;
        this.imageData = loadImage(this.worldPosMin.getX(), this.worldPosMin.getZ());
    }

    @Override
    protected BlockPos getPosTranslatedToImage(BlockPos pos)
    {
        BlockPos translatedToAreaPos = pos.subtract(worldPosMin);
        BlockPos rotatedToImageDirection = rotateAndFlip(translatedToAreaPos, reverseTemplateRotation, templateFlip);
        int x = rotatedToImageDirection.getX();
        int z = rotatedToImageDirection.getZ();
        return new BlockPos(x<0?this.imageWidth+x:x, 0, z<0?this.imageHeight+z:z);
    }

    @Override
    protected BufferedImage getImageAt(int blockX, int blockZ)
    {
        if(isLocationCoveredByTemplate(blockX, blockZ))
        {
            if(this.imageData == null)
            {
                this.imageData = loadImage(blockX, blockZ);
            }
            lastAccessed = System.currentTimeMillis();
            return this.imageData;
        }
        return null;
    }

    @Override
    public boolean isLocationCoveredByTemplate(int blockX, int blockZ) 
    {
        return blockX >= this.worldPosMin.getX() && blockX <= this.worldPosMax.getX() 
                && blockZ >= this.worldPosMin.getZ() && blockZ <= this.worldPosMax.getZ();
    }

    @Override
    protected String getFileName(int blockX, int blockZ)
    {
        if(isLocationCoveredByTemplate(blockX, blockZ)) return fileNamePrefix;
        return null;
    }

    @Override
    protected void onInitData()
    {
        this.templateRotation = 0;
        this.reverseTemplateRotation = this.templateRotation;
        if ((this.templateRotation & 1) == 1 ) this.reverseTemplateRotation = this.templateRotation ^ 2;

        this.templateFlip = 0;

        this.areaSize = rotateAndFlip(new BlockPos(this.imageWidth, 0, this.imageHeight), this.templateRotation, this.templateFlip);		
        this.worldPosMax = new BlockPos(Math.abs(this.areaSize.getX()), 0, Math.abs(this.areaSize.getZ())).add(this.worldPosMin);

        this.worldX = 0;
        this.worldZ = 0;
        this.minX = 0;
        this.minZ = 0;
        this.maxX = this.imageWidth;
        this.maxZ = this.imageHeight;
        this.areaSizeX = this.imageWidth;
        this.areaSizeZ = this.imageHeight;
    }

    protected int rotateAndFlipX(int x, int z, int templateRotation, int templateFlip)
    {
        int imageX = 0;

        switch (templateRotation)
        {
        case 0: // normal (0 degrees) template rotation
            imageX = x;
            break;
        case 1: // 90 degree template rotation clock-wise
            imageX = z;
            break;
        case 2: // 180 degree template rotation clock-wise
            imageX = -x;
            break;
        case 3: // 270 degree template rotation clock-wise
            imageX = -z;
            break;
        default:
        }

        // Flip the template on the X-axis
        if ((templateFlip & 0x1) != 0)
        {
            imageX = -imageX;
        }

        return imageX;
    }

    protected int rotateAndFlipZ(int x, int z, int templateRotation, int templateFlip)
    {
        int imageZ = 0;

        switch (templateRotation)
        {
        case 0: // normal (0 degrees) template rotation
            imageZ = z;
            break;
        case 1: // 90 degree template rotation clock-wise
            imageZ = -x;
            break;
        case 2: // 180 degree template rotation clock-wise
            imageZ = -z;
            break;
        case 3: // 270 degree template rotation clock-wise
            imageZ = x;
            break;
        default:
        }

        // Flip the template on the X-axis
        if ((templateFlip & 0x2) != 0)
        {
            imageZ = -imageZ;
        }

        return imageZ;
    }

    @Override
    protected void onExpireImage(int threshold)
    {
        long now = System.currentTimeMillis();
        if(now > lastAccessed+threshold)
        {
            imageData = null;
        }
    }
}

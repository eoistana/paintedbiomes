package fi.dy.masa.paintedbiomes.image.reader;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.Rotation;
import fi.dy.masa.paintedbiomes.PaintedBiomes;

public abstract class ImageReaderBase implements IImageReader
{
    protected File imagePath;
    protected int imageWidth;
    protected int imageHeight;

    protected abstract BlockPos getPosTranslatedToImage(BlockPos pos);
    protected abstract BufferedImage getImageAt(int blockX, int blockZ);
    public abstract boolean isLocationCoveredByTemplate(int blockX, int blockZ);

    protected abstract String getFileName(int blockX, int blockZ);
    protected String getFileSuffix() { return ".png"; }

    protected ImageReaderBase(File imagePath)
    {
        this.imagePath = imagePath;
    }

    @Override
    public int getImageAlpha(int blockX, int blockZ)
    {
        BufferedImage imageAt = getImageAt(blockX, blockZ);
        if(imageAt == null) return 0;
        BlockPos imagePos = getPosTranslatedToImage(new BlockPos(blockX, 0, blockZ));
        return getImageAlphaAt(imageAt, imagePos.getX(), imagePos.getZ());
    }

    private int getImageAlphaAt(BufferedImage imageData, int imageX, int imageY)
    {
        try
        {
            WritableRaster raster = imageData.getAlphaRaster();
            if (raster == null) return 0xFF;

            int[] alpha = new int[1];
            raster.getPixel(imageX, imageY, alpha);
            return alpha[0];
        }
        catch (ArrayIndexOutOfBoundsException e)
        {
            PaintedBiomes.logger.error("getImageAlphaAt(): Error reading the alpha channel of the template image;\n" +
                    "imageX: {} imageY: {}", imageX, imageY);
            return 0;
        }
    }

    @Override
    public int getRGB(int blockX, int blockZ)
    {
        BufferedImage imageAt = getImageAt(blockX, blockZ);
        if(imageAt == null) return 0;
        BlockPos imagePos = getPosTranslatedToImage(new BlockPos(blockX, 0, blockZ));
        return imageAt.getRGB(imagePos.getX(), imagePos.getZ());
    }

    private File getImageFileName(int blockX, int blockZ)
    {
        File templateFile = new File(this.imagePath, this.getFileName(blockX, blockZ) + this.getFileSuffix());
        return templateFile;
    }

    protected BufferedImage loadImage(int blockX, int blockZ)
    {
        File imageFile = getImageFileName(blockX, blockZ);
        try
        {
            if (imageFile.exists())
            {
                BufferedImage image = ImageIO.read(imageFile);

                PaintedBiomes.logger.info("Successfully read template image from '{}' (dimensions: {}x{})",
                        imageFile.getAbsolutePath(), image.getWidth(), image.getHeight());
                initData(image);
                return image;
            }
            else
            {
                PaintedBiomes.logger.warn("Template image not found in '{}'", imageFile.getAbsolutePath());
            }
        }
        catch (IOException e)
        {
            PaintedBiomes.logger.warn("Failed to read template image from '{}'", imageFile.getAbsolutePath());
        }
        return null;
    }

    protected abstract void onExpireImage(int threshold);
    public void expireImage(int threshold)
    {
        onExpireImage(threshold);
    }

    protected abstract void onInitData(); 

    protected void initData(BufferedImage imageData)
    {
        this.imageHeight = imageData.getHeight();
        this.imageWidth = imageData.getWidth();
        onInitData();
    }

    protected static BlockPos rotateAndFlip(BlockPos pos, int templateRotation, int templateFlip)
    {
        pos = pos.rotate(Rotation.values()[templateRotation]);
        int x = pos.getX();
        if ((templateFlip & 0x1) != 0) x = -x; // Flip the template on the X-axis
        int y = pos.getY();
        int z = pos.getZ();
        if ((templateFlip & 0x2) != 0) z = -z; // Flip the template on the Z-axis
        return new BlockPos(x, y, z);
    }
}
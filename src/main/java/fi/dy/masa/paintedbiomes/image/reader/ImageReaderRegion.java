package fi.dy.masa.paintedbiomes.image.reader;

import gnu.trove.iterator.TLongObjectIterator;
import gnu.trove.map.hash.TLongObjectHashMap;

import java.awt.image.BufferedImage;
import java.io.File;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

public class ImageReaderRegion extends ImageReaderBase
{
    protected TLongObjectHashMap<ImageCache> images;

    protected ImageReaderRegion(File imagePath) {
        super(imagePath);
        // TODO Auto-generated constructor stub
    }

    @Override
    protected BlockPos getPosTranslatedToImage(BlockPos pos)
    {
        //TODO: 
        int reverseTemplateRotation = 0, templateFlip = 0;
        
        BlockPos regionStart = new BlockPos(pos.getX()>>10, 0, pos.getZ() >> 10);
        BlockPos translatedToAreaPos = pos.subtract(regionStart);
        BlockPos rotatedToImageDirection = rotateAndFlip(translatedToAreaPos, reverseTemplateRotation, templateFlip);
        int x = rotatedToImageDirection.getX();
        int z = rotatedToImageDirection.getZ();
        return new BlockPos(x<0?this.imageWidth+x:x, 0, z<0?this.imageHeight+z:z);
    }

    @Override
    protected BufferedImage getImageAt(int blockX, int blockZ)
    {
        long regionPos = ChunkPos.asLong(blockX >> 10, blockZ >> 10);
        if(images.containsKey(regionPos) == false)
        {
            BufferedImage image = loadImage(blockX, blockZ);
            images.put(regionPos, new ImageCache(image));
        }
        ImageCache cached = images.get(regionPos);
        if(cached==null) return null;
        cached.lastAccessed = System.currentTimeMillis();
        return cached.imageData;
    }

    @Override
    public boolean isLocationCoveredByTemplate(int blockX, int blockZ) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    protected String getFileName(int blockX, int blockZ) {
        return "r." + (blockX >> 10) + "." + (blockZ >> 10);
    }

    @Override
    protected void onExpireImage(int threshold)
    {
        long then = System.currentTimeMillis() - threshold;
        TLongObjectIterator<ImageCache> it = images.iterator();
        
        while(it.hasNext())
        {
            it.advance();
            if(then > it.value().lastAccessed)
            {
                it.remove();
            }
        }
    }

    @Override
    protected void onInitData()
    {
        // TODO Auto-generated method stub

    }

    class ImageCache
    {
        public BufferedImage imageData;
        public long lastAccessed;

        public ImageCache(BufferedImage imageData)
        {
            this.imageData = imageData;
        }
    }
}

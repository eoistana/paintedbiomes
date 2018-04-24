package fi.dy.masa.paintedbiomes.image.reader;

import java.awt.image.BufferedImage;
import java.io.File;

import net.minecraft.util.math.BlockPos;

public class ImageReaderRegion extends ImageReaderBase {

    protected ImageReaderRegion(File imagePath) {
        super(imagePath);
        // TODO Auto-generated constructor stub
    }

    @Override
    protected BlockPos getPosTranslatedToImage(BlockPos pos) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected BufferedImage getImageAt(int blockX, int blockZ) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isLocationCoveredByTemplate(int blockX, int blockZ) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    protected String getFileName(int blockX, int blockY) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected void onExpireImage(int threshold) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void onInitData() {
        // TODO Auto-generated method stub

    }

}

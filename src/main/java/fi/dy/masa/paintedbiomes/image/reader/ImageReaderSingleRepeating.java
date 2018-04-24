package fi.dy.masa.paintedbiomes.image.reader;

import java.io.File;

import net.minecraft.util.math.BlockPos;

public class ImageReaderSingleRepeating extends ImageReaderSingle
{
	protected boolean repeatX;
	protected boolean repeatZ;

	public ImageReaderSingleRepeating(File imagePath, String imageName)
	{
		super(imagePath, imageName);
	}

	@Override
	protected BlockPos getPosTranslatedToImage(BlockPos pos)
	{
		BlockPos translatedToAreaPos = pos.subtract(worldPosMin);
		int i = translatedToAreaPos.getX();
		int j = translatedToAreaPos.getZ();
		BlockPos clampedByRepeatingImage = new BlockPos(i % this.areaSizeX, 0, j % this.areaSizeZ);
		BlockPos rotatedToImageDirection = rotateAndFlip(clampedByRepeatingImage, reverseTemplateRotation, templateFlip);
		int x = rotatedToImageDirection.getX();
		int z = rotatedToImageDirection.getZ();
		return new BlockPos(x<0?this.imageWidth+x:x, 0, z<0?this.imageHeight+z:z);
	}
	
	@Override
	public boolean isLocationCoveredByTemplate(int blockX, int blockZ)
	{
		return (repeatX || (blockX >= this.worldPosMin.getX() && blockX <= this.worldPosMax.getX())) 
			&& (repeatZ || (blockZ >= this.worldPosMin.getZ() && blockZ <= this.worldPosMax.getZ()));
	}
}

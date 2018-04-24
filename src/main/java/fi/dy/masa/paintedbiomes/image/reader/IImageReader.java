package fi.dy.masa.paintedbiomes.image.reader;

public interface IImageReader
{
	public int getImageAlpha(int blockX, int blockZ);
	public int getRGB(int blockX, int blockZ);
	public boolean isLocationCoveredByTemplate(int blockX, int blockZ);
	public void expireImage(int threshold);
}

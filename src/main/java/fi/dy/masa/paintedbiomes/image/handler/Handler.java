package fi.dy.masa.paintedbiomes.image.handler;

import fi.dy.masa.paintedbiomes.config.Configs;
import fi.dy.masa.paintedbiomes.image.reader.IImageReader;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import javax.annotation.Nullable;

public abstract class Handler<H extends Handler<H>> 
{
    private static final TIntObjectHashMap<Handler<?>> HANDLERS = new TIntObjectHashMap<Handler<?>>();
    protected static File templateBasePathGlobal;
    protected static File templateBasePathWorld;

    protected final int dimension;
    protected File templatePath;
    protected Long seed;

    protected boolean useSingleTemplateImage;
    protected IImageReader imageReader;

    protected Handler(int dimension)
    {
        this.dimension = dimension;
    }

    @SuppressWarnings("unchecked")
    public static <H extends Handler<H>> H getHandler(Class<H> hClass, int dimension)
    {
        Handler<?> handler = HANDLERS.get(dimension);
        if (handler != null) return (H)handler;

        try {
            handler =  hClass.getConstructor(int.class).newInstance(dimension);
            HANDLERS.put(dimension, handler);
            return (H)handler;
        } catch (InstantiationException | IllegalAccessException
                | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException e)
        {
            //TODO: Log error
            return null;
        }
    }

    public static void removeImageHandler(int dimension)
    {
        HANDLERS.remove(dimension);
    }

    public static void setTemplateBasePaths(File pathGlobal, @Nullable File pathWorld)
    {
        templateBasePathGlobal = pathGlobal;
        templateBasePathWorld = pathWorld;
    }

    protected void createAndSetTemplateDir()
    {
        this.templatePath = null;
        if (templateBasePathWorld != null)
        {
            this.templatePath = new File(templateBasePathWorld, "dim" + this.dimension);
        }

        if (this.templatePath == null || this.templatePath.exists() == false || this.templatePath.isDirectory() == false)
        {
            this.templatePath = new File(templateBasePathGlobal, "dim" + this.dimension);
        }
    }

    protected abstract void onInit(Configs configs);
    protected abstract IImageReader getImageReader();

    @SuppressWarnings("unchecked")
    public H init(long seed)
    {
        this.seed = seed;

        Configs configs = Configs.getConfig(this.dimension);
        this.useSingleTemplateImage = configs.useSingleTemplateImage;

        this.createAndSetTemplateDir();
        onInit(configs);
        this.imageReader = getImageReader();

        return (H)this;
    }

    protected boolean isLocationCoveredByTemplate(int blockX, int blockZ)
    {
        return this.imageReader.isLocationCoveredByTemplate(blockX, blockZ);
    }

    protected int getImageAlpha(int blockX, int blockZ)
    {
        return this.imageReader.getImageAlpha(blockX, blockZ);
    }

    protected int getRGB(int blockX, int blockZ)
    {
        return this.imageReader.getRGB(blockX, blockZ);
    }

    protected void expireUnusedImage(int threshold)
    {
        // TODO Auto-generated method stub
        this.imageReader.expireImage(threshold);
    }

    private static int timer;
    public static void tickTimeouts()
    {
        if (++timer >= 200)
        {
            timer = 0;
            int threshold = 300 * 1000; // 5 minute timeout for non-accessed images
            TIntObjectIterator<Handler<?>> iterator = HANDLERS.iterator();

            for (int i = HANDLERS.size(); i > 0; --i)
            {
                iterator.advance();
                Handler<?> handler = iterator.value();

                if (handler.useSingleTemplateImage == false)
                {
                    handler.expireUnusedImage(threshold);
                }
            }
        }
    }
}
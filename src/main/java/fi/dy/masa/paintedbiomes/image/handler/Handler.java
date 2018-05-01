package fi.dy.masa.paintedbiomes.image.handler;

import fi.dy.masa.paintedbiomes.PaintedBiomes;
import fi.dy.masa.paintedbiomes.config.Configs;
import fi.dy.masa.paintedbiomes.image.reader.IImageReader;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import javax.annotation.Nullable;

import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.util.ReportedException;

public abstract class Handler<H extends Handler<H>> 
{
    private static final THashMap<Class<?>, TIntObjectHashMap<Handler<?>>> HANDLERS = new THashMap<>(); 
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
        HANDLERS.putIfAbsent(hClass, new TIntObjectHashMap<>());
        Handler<?> handler = HANDLERS.get(hClass).get(dimension);
        if (handler != null) return (H)handler;

        try {
            handler =  hClass.getConstructor(int.class).newInstance(dimension);
            HANDLERS.get(hClass).put(dimension, handler);
            return (H)handler;
        } catch (InstantiationException | IllegalAccessException
                | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException e)
        {
            PaintedBiomes.logger.error("Error in handler initialization.", e);
            
            CrashReport crashreport = CrashReport.makeCrashReport(e, "Error in handler initialization.");
            CrashReportCategory crashreportcategory = crashreport.makeCategory("HandlerType");
            crashreportcategory.addCrashSection("Class<H> ", hClass.getName());
            throw new ReportedException(crashreport);
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
        this.imageReader.expireImage(threshold);
    }

    private static int timer;
    public static void tickTimeouts(Class<?> hClass)
    {
        if (++timer >= 200)
        {
            timer = 0;
            int threshold = 300 * 1000; // 5 minute timeout for non-accessed images
            TIntObjectIterator<Handler<?>> iterator = HANDLERS.get(hClass).iterator();
            
            while(iterator.hasNext())
            {
                iterator.advance();
                Handler<?> handler = iterator.value();
                handler.expireUnusedImage(threshold);
            }
        }
    }
}
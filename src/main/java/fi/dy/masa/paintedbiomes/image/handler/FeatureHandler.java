package fi.dy.masa.paintedbiomes.image.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;

import net.minecraft.command.CommandSenderWrapper;
import net.minecraft.command.FunctionObject;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import net.minecraft.world.gen.structure.template.Template;
import fi.dy.masa.paintedbiomes.config.Configs;
import fi.dy.masa.paintedbiomes.image.reader.IImageReader;
import fi.dy.masa.paintedbiomes.image.reader.ImageReaderSingle;
import fi.dy.masa.paintedbiomes.image.reader.ImageReaderSingleRepeating;
import fi.dy.masa.paintedbiomes.mappings.StructureIdToTemplateMapping;

public class FeatureHandler extends Handler<FeatureHandler>
{
    protected StructureSpots structureSpots;

    public FeatureHandler(int dimension) {
        super(dimension);
    }

    public static FeatureHandler getFeatureHandler(int dimension)
    {        
        return getHandler(FeatureHandler.class, dimension);
    }

    public int getHeightAt(int blockX, int blockZ, int defaultHeight)
    {
        if(isLocationCoveredByTemplate(blockX, blockZ))
        {
            int alpha = imageReader.getImageAlpha(blockX, blockZ);
            if(alpha == 0x00) return defaultHeight;
            int pixel = imageReader.getRGB(blockX, blockZ);
            int height = getHeightFromPixel(pixel);
            if(alpha == 0xFF) return height;
            return defaultHeight + (int)(((double)alpha / 255.0) * (height-defaultHeight));
        }
        return defaultHeight;
    }

    public int getAlphaAt(int blockX, int blockZ)
    {
        if(isLocationCoveredByTemplate(blockX, blockZ))
        {
            return imageReader.getImageAlpha(blockX, blockZ);
        }
        return 0;
    }

    public ArrayList<StructureSpots.StructureSpot> getStructureSpots(MinecraftServer server, int chunkX, int chunkZ) 
    {
        if(isChunkInsideBounds(chunkX, chunkZ))
        {
            long pos = ChunkPos.asLong(chunkX, chunkZ);
            if(!structureSpots.isChunkProcessed(pos)) 
            {
                int blockX = (chunkX-2)<<4;
                int blockZ = (chunkZ-2)<<4;
                for(int x=blockX; x < blockX+(16*3); ++x) // Templates can max be 32 wide
                {
                    for(int z=blockZ; z < blockZ+(16*3); ++z) // Templates can max be 32 long
                    {
                        if(isLocationCoveredByTemplate(x, z))
                        {
                            int pixel = imageReader.getRGB(x, z);
                            int c = pixel & 0x0000FFFF; // Structures are contained in the green and blue channels
                            if(c != 0)
                            {
                                Template template = StructureIdToTemplateMapping.getInstance().getTemplate(server, c);
                                if(template != null)
                                {
                                    BlockPos templateAnchor = new BlockPos(x, getHeightFromPixel(pixel), z);
                                    BlockPos templateSize = template.getSize();
                                    BlockPos templateAnchorOpposite = templateAnchor.add(templateSize);
                                    int chunkXOpposite = templateAnchorOpposite.getX() >> 4;
                                    int chunkZOpposite = templateAnchorOpposite.getZ() >> 4;
    
                                    if(chunkXOpposite>=chunkX && chunkZOpposite>=chunkZ)
                                    {
                                        structureSpots.addStructureAt(template, pos, c, templateAnchor);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return structureSpots.getStructureSpots(pos);
        }
        return null;
    }

    private int getHeightFromPixel(int pixel) {
        return (pixel&0x00FF0000)>>16;
    }

    public boolean isChunkInsideBounds(int chunkX, int chunkZ)
    {
        int lowerBlockX = chunkX<<4;
        int lowerBlockZ = chunkZ<<4;
        int higherBlockX = lowerBlockX + 15; 
        int higherBlockZ = lowerBlockZ + 15; 

        return this.isLocationCoveredByTemplate(lowerBlockX, lowerBlockZ)
                || this.isLocationCoveredByTemplate(higherBlockX, lowerBlockZ)
                || this.isLocationCoveredByTemplate(lowerBlockX, higherBlockZ)
                || this.isLocationCoveredByTemplate(higherBlockX, higherBlockZ);
    }

    @Override
    protected void onInit(Configs configs)
    {
        this.structureSpots = new StructureSpots(configs, seed);
    }

    @Override
    protected IImageReader getImageReader()
    {
        Configs conf = Configs.getConfig(dimension);
        BlockPos initPos = new BlockPos(conf.templateAlignmentX, 0, conf.templateAlignmentZ);
        if(this.useSingleTemplateImage)
            if(conf.useTemplateRepeating)
                return new ImageReaderSingleRepeating(this.templatePath, "features", initPos, conf);
            else 
                return new ImageReaderSingle(this.templatePath, "features", initPos);
        return null;
    }

    public class StructureSpots
    {
        protected HashMap<Long, StructureList> spots = new HashMap<>();

        protected boolean withRotation;
        protected boolean withMirror;
        protected long seed;
        protected Random rand; 

        public StructureSpots(Configs configs, long seed)
        {
            this.rand = new Random(seed);
            this.withRotation = configs.useTemplateRandomRotation;
            this.withMirror = configs.useTemplateRandomFlipping;
        }

        public boolean addStructureAt(Template template, long pos, int structureId, BlockPos blockPos)
        {
            StructureList spotList;
            if(!spots.containsKey(pos)) spotList = spots.put(pos, new StructureList());
            spotList = spots.get(pos);

            for(StructureSpot spot : spotList.spots)
            {
                if(spot.structureId == structureId && spot.blockPos.equals(blockPos)) return false;
            }
            spotList.add(new StructureSpot(template, structureId, blockPos, getStructureRotation(blockPos), getStructureMirror(blockPos)));
            return true;
        }

        private Rotation getStructureRotation(BlockPos blockPos) 
        {
            if(withRotation)
            {
                rand.setSeed((blockPos.getX() * 6245645L + blockPos.getZ() * 235847645L) ^ seed);
                switch(rand.nextInt(4))
                {
                case 0: return Rotation.NONE;
                case 1: return Rotation.CLOCKWISE_90;
                case 2: return Rotation.CLOCKWISE_180;
                case 3: return Rotation.COUNTERCLOCKWISE_90;
                }
            }
            return Rotation.NONE;
        }

        private Mirror getStructureMirror(BlockPos blockPos)
        {
            if(withMirror)
            {
                rand.setSeed((blockPos.getX() * 6438245645L + blockPos.getZ() * 115235753845L) ^ seed);
                switch(rand.nextInt(3))
                {
                case 0: return Mirror.NONE;
                case 1: return Mirror.FRONT_BACK;
                case 2: return Mirror.LEFT_RIGHT;
                }
            }
            return Mirror.NONE;
        }

        public boolean isChunkProcessed(long pos)
        {
            return spots.containsKey(pos);
        }

        public ArrayList<StructureSpot> getStructureSpots(long pos)
        {
            if(spots.containsKey(pos)) return spots.get(pos).spots;
            return null;
        }

        public class StructureList
        {
            public ArrayList<StructureSpot> spots = new ArrayList<>();

            public void add(StructureSpot structureSpot)
            {
                spots.add(structureSpot);
            }
        }

        public class StructureSpot
        {
            public final int structureId;
            public final BlockPos blockPos;
            public final Rotation rotation;
            public final Mirror mirror;
            private final Template template;

            public StructureSpot(Template template, int structureId, BlockPos blockPos, Rotation rotation, Mirror mirror)
            {
                this.structureId = structureId;
                this.blockPos = blockPos;

                this.template = template;

                this.rotation = rotation;
                this.mirror = mirror;
            }

            public void addBlocksToWorldChunk(World world, ChunkPos chunkPos)
            {
                PlacementSettings placement = new PlacementSettings()
                .setRotation(rotation)
                .setMirror(mirror)
                .setChunk(chunkPos);

                BlockPos templateSize = template.getSize();
                BlockPos transformedSize = Template.transformedBlockPos(placement, templateSize);
                BlockPos pos = blockPos.add(-Math.min(0, transformedSize.getX()+1), 0, -Math.min(0, transformedSize.getZ()+1));

                template.addBlocksToWorldChunk(world, pos, placement);
                parseStructureBlocks(world, template, pos, placement);
            }

            private void parseStructureBlocks(World world, Template template,
                    BlockPos blockPos, PlacementSettings placement)
            {
                Map<BlockPos, String> map = template.getDataBlocks(blockPos, placement);
                if(map != null)
                {
                    MinecraftServer server = world.getMinecraftServer();
                    for (Entry<BlockPos, String> entry : map.entrySet())
                    {
                        BlockPos posDataBlock = entry.getKey();
                        world.setBlockState(posDataBlock, Blocks.AIR.getDefaultState());

                        ResourceLocation functionName = new ResourceLocation(entry.getValue());
                        FunctionObject function = server.getFunctionManager().getFunction(functionName);

                        if (function != null)
                        {
                            ICommandSender functionSender = new CommandSenderWrapper(server, null, posDataBlock, 2, (Entity)null, false);
                            server.getFunctionManager().execute(function, functionSender);
                        }
                    }
                }
            }
        }
    }
}

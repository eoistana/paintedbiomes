package fi.dy.masa.paintedbiomes.world.generator;

import java.util.List;
import java.util.Random;

import fi.dy.masa.paintedbiomes.image.ImageHandler;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biome.SpawnListEntry;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.ChunkGeneratorOverworld;
import net.minecraft.world.gen.ChunkGeneratorSettings;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.MapGenBase;
import net.minecraft.world.gen.MapGenCaves;
import net.minecraft.world.gen.MapGenRavine;
import net.minecraft.world.gen.structure.MapGenMineshaft;
import net.minecraft.world.gen.structure.MapGenScatteredFeature;
import net.minecraft.world.gen.structure.MapGenStronghold;
import net.minecraft.world.gen.structure.MapGenVillage;
import net.minecraft.world.gen.structure.StructureOceanMonument;
import net.minecraft.world.gen.structure.WoodlandMansion;

public class ChunkGeneratorPaintedBiomes extends ChunkGeneratorOverworld {

	protected final World world;
	protected ChunkGeneratorSettings settings;
	protected final boolean mapFeaturesEnabled;
	protected final ImageHandler imageHandler;
    
	protected MapGenBase caveGenerator = new MapGenCaves();
	protected MapGenStronghold strongholdGenerator = new MapGenStronghold();
    protected MapGenVillage villageGenerator = new MapGenVillage();
    protected MapGenMineshaft mineshaftGenerator = new MapGenMineshaft();
    protected MapGenScatteredFeature scatteredFeatureGenerator = new MapGenScatteredFeature();
    protected MapGenBase ravineGenerator = new MapGenRavine();
    protected StructureOceanMonument oceanMonumentGenerator = new StructureOceanMonument();
    protected WoodlandMansion woodlandMansionGenerator = new WoodlandMansion(this);
    
    protected Biome[] biomesForGeneration;
	protected Random rand;
    protected final int[] heightMap;
	protected final IBlockState oceanBlock;

	public ChunkGeneratorPaintedBiomes(World worldIn, long seed, boolean mapFeaturesEnabledIn,
			String generatorOptions, ImageHandler imageHandler) {
		super(worldIn, seed, mapFeaturesEnabledIn, generatorOptions);


		// Duplicated fields
		{
            caveGenerator = net.minecraftforge.event.terraingen.TerrainGen.getModdedMapGen(caveGenerator, net.minecraftforge.event.terraingen.InitMapGenEvent.EventType.CAVE);
            strongholdGenerator = (MapGenStronghold)net.minecraftforge.event.terraingen.TerrainGen.getModdedMapGen(strongholdGenerator, net.minecraftforge.event.terraingen.InitMapGenEvent.EventType.STRONGHOLD);
            villageGenerator = (MapGenVillage)net.minecraftforge.event.terraingen.TerrainGen.getModdedMapGen(villageGenerator, net.minecraftforge.event.terraingen.InitMapGenEvent.EventType.VILLAGE);
            mineshaftGenerator = (MapGenMineshaft)net.minecraftforge.event.terraingen.TerrainGen.getModdedMapGen(mineshaftGenerator, net.minecraftforge.event.terraingen.InitMapGenEvent.EventType.MINESHAFT);
            scatteredFeatureGenerator = (MapGenScatteredFeature)net.minecraftforge.event.terraingen.TerrainGen.getModdedMapGen(scatteredFeatureGenerator, net.minecraftforge.event.terraingen.InitMapGenEvent.EventType.SCATTERED_FEATURE);
            ravineGenerator = net.minecraftforge.event.terraingen.TerrainGen.getModdedMapGen(ravineGenerator, net.minecraftforge.event.terraingen.InitMapGenEvent.EventType.RAVINE);
            oceanMonumentGenerator = (StructureOceanMonument)net.minecraftforge.event.terraingen.TerrainGen.getModdedMapGen(oceanMonumentGenerator, net.minecraftforge.event.terraingen.InitMapGenEvent.EventType.OCEAN_MONUMENT);
            woodlandMansionGenerator = (WoodlandMansion)net.minecraftforge.event.terraingen.TerrainGen.getModdedMapGen(woodlandMansionGenerator, net.minecraftforge.event.terraingen.InitMapGenEvent.EventType.WOODLAND_MANSION);
        }
	    this.rand = new Random(seed);
        this.world = worldIn;
		this.mapFeaturesEnabled = mapFeaturesEnabledIn;
		this.imageHandler = imageHandler;
        this.heightMap = new int[16*16];
        
        if (generatorOptions != null)
        {
        	this.settings = ChunkGeneratorSettings.Factory.jsonToFactory(generatorOptions).build();
            this.oceanBlock = this.settings.useLavaOceans ? Blocks.LAVA.getDefaultState() : Blocks.WATER.getDefaultState();
        }
        else
        {
        	this.oceanBlock = Blocks.WATER.getDefaultState();
        }
	}

	@Override
	public Chunk generateChunk(int x, int z) {
		this.rand.setSeed((long)x * 341873128712L + (long)z * 132897987541L);
        ChunkPrimer chunkprimer = new ChunkPrimer();
        this.setBlocksInChunkWithPaintedHeight(x, z, chunkprimer);
        this.biomesForGeneration = this.world.getBiomeProvider().getBiomes(this.biomesForGeneration, x * 16, z * 16, 16, 16);
        this.replaceBiomeBlocks(x, z, chunkprimer, this.biomesForGeneration);

        if (this.settings.useCaves)
        {
            this.caveGenerator.generate(this.world, x, z, chunkprimer);
        }

        if (this.settings.useRavines)
        {
            this.ravineGenerator.generate(this.world, x, z, chunkprimer);
        }

        if (this.mapFeaturesEnabled)
        {
            if (this.settings.useMineShafts)
            {
                this.mineshaftGenerator.generate(this.world, x, z, chunkprimer);
            }

            if (this.settings.useVillages)
            {
                this.villageGenerator.generate(this.world, x, z, chunkprimer);
            }

            if (this.settings.useStrongholds)
            {
                this.strongholdGenerator.generate(this.world, x, z, chunkprimer);
            }

            if (this.settings.useTemples)
            {
                this.scatteredFeatureGenerator.generate(this.world, x, z, chunkprimer);
            }

            if (this.settings.useMonuments)
            {
                this.oceanMonumentGenerator.generate(this.world, x, z, chunkprimer);
            }

            if (this.settings.useMansions)
            {
                this.woodlandMansionGenerator.generate(this.world, x, z, chunkprimer);
            }
        }

        Chunk chunk = new Chunk(this.world, chunkprimer, x, z);
        byte[] abyte = chunk.getBiomeArray();

        for (int i = 0; i < abyte.length; ++i)
        {
            abyte[i] = (byte)Biome.getIdForBiome(this.biomesForGeneration[i]);
        }

        chunk.generateSkylightMap();
        return chunk;
	}

	public void setBlocksInChunkWithPaintedHeight(int x, int z, ChunkPrimer primer)
    {
        this.biomesForGeneration = this.world.getBiomeProvider().getBiomesForGeneration(this.biomesForGeneration, x * 4 - 2, z * 4 - 2, 10, 10);
        this.generateHeightmapFromImage(x, z);

        int chunkX = x<<4;
        int chunkZ = z<<4;
        for (int gx = 0; gx < 16; ++gx)
        {
        	//int offset = gx << 4;
        	for(int gz = 0; gz < 16; ++gz)
        	{
        		int h = this.imageHandler.getHeightAt(gx + chunkX, gz + chunkZ, 0);
    			//int h = this.heightMap[offset + gz];
        		for(int gy = 0; gy < 256; ++gy)
        		{
        			if(gy < h) primer.setBlockState(gx, gy, gz, STONE);
        			else if(gy < this.settings.seaLevel) primer.setBlockState(gx, gy, gz, this.oceanBlock);
        			else break;
        		}
        	}
        }
    }
	
	private void generateHeightmapFromImage(int x, int z)
    {
		int i = 0;
		//TODO: translate x to image coord
		//TODO: translate z to image coord
		
		for(int hx = 0; hx < 16; ++hx)
		{
			int offset = hx * 16;
			for(int hz = 0; hz < 16; ++hz)
			{
				this.heightMap[offset + hz] = 65;
			}
		}
    }
}

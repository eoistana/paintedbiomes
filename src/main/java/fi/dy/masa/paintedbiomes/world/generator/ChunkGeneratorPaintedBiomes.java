package fi.dy.masa.paintedbiomes.world.generator;

import java.util.ArrayList;
import java.util.Random;

import net.minecraft.block.BlockFalling;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldEntitySpawner;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.ChunkGeneratorOverworld;
import net.minecraft.world.gen.ChunkGeneratorSettings;
import net.minecraft.world.gen.MapGenBase;
import net.minecraft.world.gen.MapGenCaves;
import net.minecraft.world.gen.MapGenRavine;
import net.minecraft.world.gen.feature.WorldGenDungeons;
import net.minecraft.world.gen.feature.WorldGenLakes;
import net.minecraft.world.gen.structure.MapGenMineshaft;
import net.minecraft.world.gen.structure.MapGenScatteredFeature;
import net.minecraft.world.gen.structure.MapGenStronghold;
import net.minecraft.world.gen.structure.MapGenVillage;
import net.minecraft.world.gen.structure.StructureOceanMonument;
import net.minecraft.world.gen.structure.WoodlandMansion;
import fi.dy.masa.paintedbiomes.image.handler.FeatureHandler;
import fi.dy.masa.paintedbiomes.image.handler.FeatureHandler.StructureSpots.StructureSpot;

public class ChunkGeneratorPaintedBiomes extends ChunkGeneratorOverworld {

	protected final World world;
	protected ChunkGeneratorSettings settings;
	protected final boolean mapFeaturesEnabled;
	protected final FeatureHandler featureHandler;
    
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
	protected final IBlockState oceanBlock;

	public ChunkGeneratorPaintedBiomes(World worldIn, long seed, boolean mapFeaturesEnabledIn,
			String generatorOptions, FeatureHandler featureHandler) {
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
		this.featureHandler = featureHandler;
        
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
		if(!this.featureHandler.isChunkInsideBounds(x,z))
		{
			return super.generateChunk(x, z);
		}
		this.rand.setSeed((long)x * 341873128712L + (long)z * 132897987541L);
        ChunkPrimer chunkprimer = new ChunkPrimer();
        this.setBlocksInChunkWithPaintedHeight(x, z, chunkprimer);
        this.biomesForGeneration = this.world.getBiomeProvider().getBiomes(this.biomesForGeneration, x * 16, z * 16, 16, 16);
        this.replaceBiomeBlocks(x, z, chunkprimer, this.biomesForGeneration);

        if (this.settings.useCaves)
        {
            //this.caveGenerator.generate(this.world, x, z, chunkprimer);
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

        int chunkX = x<<4;
        int chunkZ = z<<4;
        for (int gx = 0; gx < 16; ++gx)
        {
        	for(int gz = 0; gz < 16; ++gz)
        	{
        		//TODO: get default height
        		int h = this.featureHandler.getHeightAt(gx + chunkX, gz + chunkZ, 0);
        		for(int gy = 0; gy < 256; ++gy)
        		{
        			if(gy < h) primer.setBlockState(gx, gy, gz, STONE);
        			else if(gy < this.settings.seaLevel) primer.setBlockState(gx, gy, gz, this.oceanBlock);
        			else break;
        		}
        	}
        }
    }
	
	public void populate(int x, int z)
    {
		BlockFalling.fallInstantly = true;
        int i = x * 16;
        int j = z * 16;
        BlockPos blockpos = new BlockPos(i, 0, j);
        Biome biome = this.world.getBiome(blockpos.add(16, 0, 16));
        this.rand.setSeed(this.world.getSeed());
        long k = this.rand.nextLong() / 2L * 2L + 1L;
        long l = this.rand.nextLong() / 2L * 2L + 1L;
        this.rand.setSeed((long)x * k + (long)z * l ^ this.world.getSeed());
        boolean flag = false;
        ChunkPos chunkpos = new ChunkPos(x, z);
        
        net.minecraftforge.event.ForgeEventFactory.onChunkPopulate(true, this, this.world, this.rand, x, z, flag);
        
        if(!this.featureHandler.isChunkInsideBounds(x, z))
        {
	        if (this.mapFeaturesEnabled)
	        {
	            if (this.settings.useMineShafts)
	            {
	                this.mineshaftGenerator.generateStructure(this.world, this.rand, chunkpos);
	            }
	
	            if (this.settings.useVillages)
	            {
	                flag = this.villageGenerator.generateStructure(this.world, this.rand, chunkpos);
	            }
	
	            if (this.settings.useStrongholds)
	            {
	                this.strongholdGenerator.generateStructure(this.world, this.rand, chunkpos);
	            }
	
	            if (this.settings.useTemples)
	            {
	                this.scatteredFeatureGenerator.generateStructure(this.world, this.rand, chunkpos);
	            }
	
	            if (this.settings.useMonuments)
	            {
	                this.oceanMonumentGenerator.generateStructure(this.world, this.rand, chunkpos);
	            }
	
	            if (this.settings.useMansions)
	            {
	                this.woodlandMansionGenerator.generateStructure(this.world, this.rand, chunkpos);
	            }
	        }
	
	        if (biome != Biomes.DESERT && biome != Biomes.DESERT_HILLS && this.settings.useWaterLakes && !flag && this.rand.nextInt(this.settings.waterLakeChance) == 0)
	        if (net.minecraftforge.event.terraingen.TerrainGen.populate(this, this.world, this.rand, x, z, flag, net.minecraftforge.event.terraingen.PopulateChunkEvent.Populate.EventType.LAKE))
	        {
	            int i1 = this.rand.nextInt(16) + 8;
	            int j1 = this.rand.nextInt(256);
	            int k1 = this.rand.nextInt(16) + 8;
	            (new WorldGenLakes(Blocks.WATER)).generate(this.world, this.rand, blockpos.add(i1, j1, k1));
	        }
	
	        if (!flag && this.rand.nextInt(this.settings.lavaLakeChance / 10) == 0 && this.settings.useLavaLakes)
	        if (net.minecraftforge.event.terraingen.TerrainGen.populate(this, this.world, this.rand, x, z, flag, net.minecraftforge.event.terraingen.PopulateChunkEvent.Populate.EventType.LAVA))
	        {
	            int i2 = this.rand.nextInt(16) + 8;
	            int l2 = this.rand.nextInt(this.rand.nextInt(248) + 8);
	            int k3 = this.rand.nextInt(16) + 8;
	
	            if (l2 < this.world.getSeaLevel() || this.rand.nextInt(this.settings.lavaLakeChance / 8) == 0)
	            {
	                (new WorldGenLakes(Blocks.LAVA)).generate(this.world, this.rand, blockpos.add(i2, l2, k3));
	            }
	        }
	
	        if (this.settings.useDungeons)
	        if (net.minecraftforge.event.terraingen.TerrainGen.populate(this, this.world, this.rand, x, z, flag, net.minecraftforge.event.terraingen.PopulateChunkEvent.Populate.EventType.DUNGEON))
	        {
	            for (int j2 = 0; j2 < this.settings.dungeonChance; ++j2)
	            {
	                int i3 = this.rand.nextInt(16) + 8;
	                int l3 = this.rand.nextInt(256);
	                int l1 = this.rand.nextInt(16) + 8;
	                (new WorldGenDungeons()).generate(this.world, this.rand, blockpos.add(i3, l3, l1));
	            }
	        }
	
	        biome.decorate(this.world, this.rand, new BlockPos(i, 0, j));
	        if (net.minecraftforge.event.terraingen.TerrainGen.populate(this, this.world, this.rand, x, z, flag, net.minecraftforge.event.terraingen.PopulateChunkEvent.Populate.EventType.ANIMALS))
	        WorldEntitySpawner.performWorldGenSpawning(this.world, biome, i + 8, j + 8, 16, 16, this.rand);
	        blockpos = blockpos.add(8, 0, 8);
	
	        if (net.minecraftforge.event.terraingen.TerrainGen.populate(this, this.world, this.rand, x, z, flag, net.minecraftforge.event.terraingen.PopulateChunkEvent.Populate.EventType.ICE))
	        {
	        for (int k2 = 0; k2 < 16; ++k2)
	        {
	            for (int j3 = 0; j3 < 16; ++j3)
	            {
	                BlockPos blockpos1 = this.world.getPrecipitationHeight(blockpos.add(k2, 0, j3));
	                BlockPos blockpos2 = blockpos1.down();
	
	                if (this.world.canBlockFreezeWater(blockpos2))
	                {
	                    this.world.setBlockState(blockpos2, Blocks.ICE.getDefaultState(), 2);
	                }
	
	                if (this.world.canSnowAt(blockpos1, true))
	                {
	                    this.world.setBlockState(blockpos1, Blocks.SNOW_LAYER.getDefaultState(), 2);
	                }
	            }
	        }
	        }//Forge: End ICE
        }
        
        //PaintedBiomes painted structures::
        generatePaintedStructures(x,z);
        
        net.minecraftforge.event.ForgeEventFactory.onChunkPopulate(false, this, this.world, this.rand, x, z, flag);

        BlockFalling.fallInstantly = false;
    }

	private void generatePaintedStructures(int x, int z)
	{
		ArrayList<StructureSpot> structureSpots = featureHandler.getStructureSpots(world.getMinecraftServer(), x, z);
		if(structureSpots != null)
		{
			ChunkPos chunkPos = new ChunkPos(x, z);
			for(StructureSpot spot : structureSpots)
			{
				spot.addBlocksToWorldChunk(world, chunkPos);
			}
		}
	}
}

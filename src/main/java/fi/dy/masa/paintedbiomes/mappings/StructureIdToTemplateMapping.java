package fi.dy.masa.paintedbiomes.mappings;

import fi.dy.masa.paintedbiomes.PaintedBiomes;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.io.File;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.gen.structure.template.Template;
import net.minecraft.world.gen.structure.template.TemplateManager;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class StructureIdToTemplateMapping
{
	private static StructureIdToTemplateMapping instance;
    /** Mapping from an RGB color value to a Biome ID */
    private final TIntObjectHashMap<String> structureIdToNameMappings;
    private final TIntObjectHashMap<Template> structureIdToTemplateMappings;
    
    public StructureIdToTemplateMapping()
    {
    	instance = this;
    	this.structureIdToNameMappings = new TIntObjectHashMap<>();
    	this.structureIdToTemplateMappings = new TIntObjectHashMap<>();
    }
    
    public static StructureIdToTemplateMapping getInstance()
    {
    	return instance;
    }
    
    public void addMapping(int structureId, String structureName)
    {
    	this.structureIdToNameMappings.putIfAbsent(structureId, structureName);
    }

    public String getStructureNameForId(int structureId)
    {
        if(!this.structureIdToNameMappings.containsKey(structureId)) return null;
        return this.structureIdToNameMappings.get(structureId);
    }
    
    public Template getTemplate(MinecraftServer server, int structureId)
	{
    	if(this.structureIdToTemplateMappings.containsKey(structureId)) return this.structureIdToTemplateMappings.get(structureId);
    	if(!this.structureIdToNameMappings.containsKey(structureId)) return null;    	
		Template template = getTemplateManager().getTemplate(server, new ResourceLocation(getStructureNameForId(structureId)));
		this.structureIdToTemplateMappings.putIfAbsent(structureId, template);
		return template;
	}
	
	private static TemplateManager templateManager;
	private static TemplateManager getTemplateManager()
    {
        // Lazy load/create the TemplateManager, so that the MinecraftServer is actually running at this point
        if (templateManager == null)
        {
            templateManager = new TemplateManager(getStructureDirectory().toString(), FMLCommonHandler.instance().getDataFixer());
        }

        return templateManager;
    }
	
	private static File getStructureDirectory()
    {
        return new File(new File(PaintedBiomes.configDirPath), "structures");
    }
}

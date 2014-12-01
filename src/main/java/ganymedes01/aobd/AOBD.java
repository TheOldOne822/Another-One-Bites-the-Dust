package ganymedes01.aobd;

import ganymedes01.aobd.configuration.ConfigHandler;
import ganymedes01.aobd.lib.CompatType;
import ganymedes01.aobd.lib.Reference;
import ganymedes01.aobd.ore.OreFinder;
import ganymedes01.aobd.recipes.ModulesHandler;
import ganymedes01.aobd.recipes.modules.MekanismModule;
import ganymedes01.aobd.recipes.modules.UltraTechModule;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, version = Reference.VERSION_NUMBER, dependencies = Reference.DEPENDENCIES, guiFactory = Reference.GUI_FACTORY_CLASS)
public class AOBD {

	@Instance(Reference.MOD_ID)
	public static AOBD instance;

	public static String userDefinedItems = "";
	public static String userDefinedGases = "";

	public static CreativeTabs tab = new CreativeTabs(Reference.MOD_ID) {

		@Override
		public Item getTabIconItem() {
			return Items.glowstone_dust;
		}
	};

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		ConfigHandler.INSTANCE.preInit(event.getSuggestedConfigurationFile());
		FMLCommonHandler.instance().bus().register(ConfigHandler.INSTANCE);
		MinecraftForge.EVENT_BUS.register(this);
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		if (CompatType.ULTRA_TECH.isEnabled())
			UltraTechModule.registerOres();

		// Find ores
		OreFinder.preInit();

		// Create configs for each ore
		ConfigHandler.INSTANCE.initOreConfigs();

		// Creates the necessary support modules
		ModulesHandler.createModules();

		// Add items (dusts, crushed, cluster, etc)
		OreFinder.init();

		// Add recipes
		ModulesHandler.init();
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		// Add the rest of the recipes
		ModulesHandler.postInit();
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void stitchEventPost(TextureStitchEvent.Post event) {
		// Calculate the ores colours
		OreFinder.initColours();

		// Create colour configs
		ConfigHandler.INSTANCE.initColourConfigs();
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void stitchEventPre(TextureStitchEvent.Pre event) {
		// Register icons for Mekanism's gases
		if (Loader.isModLoaded("Mekanism"))
			MekanismModule.registerIcons(event.map);
	}
}
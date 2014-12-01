package ganymedes01.aobd.ore;

import ganymedes01.aobd.AOBD;
import ganymedes01.aobd.items.AOBDItem;
import ganymedes01.aobd.lib.CompatType;
import ganymedes01.aobd.lib.Reference;
import ganymedes01.aobd.recipes.ModulesHandler;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.imageio.ImageIO;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;

public class OreFinder {

	public static final HashMap<String, AOBDItem> itemMap = new HashMap<String, AOBDItem>();

	private static Collection<String> getMetalsWithPrefixes(String prefix1, String prefix2) {
		Set<String> ores = new LinkedHashSet<String>();
		for (String name : OreDictionary.getOreNames())
			if (name.startsWith(prefix1) && !OreDictionary.getOres(name).isEmpty()) {
				String oreName = name.substring(prefix1.length());
				for (String n : OreDictionary.getOreNames())
					if (n.equals(prefix2 + oreName) && !OreDictionary.getOres(n).isEmpty())
						ores.add(oreName);
			}
		if (ores.contains("Aluminum") && ores.contains("Aluminium"))
			ores.remove("Aluminum");
		if (ores.contains("AluminumBrass") && ores.contains("AluminiumBrass"))
			ores.remove("AluminumBrass");

		return Collections.unmodifiableSet(ores);
	}

	public static void preInit() {
		Collection<String> ores = getMetalsWithPrefixes("ore", "ingot");
		for (String ore : ores)
			Ore.newOre(ore);

		if (CompatType.NETHER_ORES.isEnabled())
			for (String ore : getMetalsWithPrefixes("oreNether", "ingot"))
				if (!ores.contains(ore))
					Ore.newNetherOre(ore);
	}

	public static void initColours() {
		for (Ore ore : Ore.ores)
			ore.setColour(getColour(ore.name()));
	}

	public static void init() {
		for (CompatType compat : CompatType.values())
			generateItems(compat, compat.prefixes());

		// Just for testing obviously. Remove after at least one compat is done
		for (Ore ore : Ore.ores) {
			register("chunk", ore);
			register("cleanGravel", ore);
			register("clump", ore);
			register("cluster", ore);
			register("crushed", ore);
			register("crushedPurified", ore);
			register("crystal", ore);
			register("crystalline", ore);
			register("dirtyGravel", ore);
			register("dust", ore);
			register("dustDirty", ore);
			register("dustTiny", ore);
			register("nugget", ore);
			register("ore", ore);
			register("reduced", ore);
			register("shard", ore);
		}

		String[] items = AOBD.userDefinedItems.trim().split(",");
		if (items.length > 0)
			for (String prefix : items) {
				prefix = prefix.trim();
				if (!prefix.isEmpty())
					for (Ore ore : Ore.ores) {
						String name = ore.name();
						registerOre(prefix + name, new AOBDItem(prefix, ore));
					}
			}
	}

	private static void register(String str, Ore ore) {
		registerOre(str + ore.name(), new AOBDItem(str, ore));
	}

	private static void generateItems(CompatType compat, String[] prefixes) {
		if (compat.isEnabled())
			for (Ore ore : Ore.ores) {
				String name = ore.name();
				if (!ore.isCompatEnabled(compat) || ModulesHandler.isOreBlacklisted(compat, name))
					continue;

				for (String prefix : prefixes) {
					String str = prefix.trim();
					registerOre(str + name, new AOBDItem(str, ore));
				}
			}
	}

	private static void registerOre(String ore, AOBDItem item) {
		if (OreDictionary.getOres(ore).isEmpty()) {
			GameRegistry.registerItem(item, ore);
			OreDictionary.registerOre(ore, item);
			itemMap.put(ore, item);

			ItemModelMesher mesher = Minecraft.getMinecraft().getRenderItem().getItemModelMesher();
			mesher.register(item, 0, new ModelResourceLocation(Reference.MOD_ID + ":" + item.getBaseName(), "inventory"));
			ModelBakery.addVariantName(item, Reference.MOD_ID + ":" + item.getBaseName());
		}
	}

	private static int getStackColour(ItemStack stack, int pass) {
		if (Loader.isModLoaded("gregtech"))
			try {
				Class<?> cls = Class.forName("gregtech.api.items.GT_MetaGenerated_Item");
				if (cls.isAssignableFrom(stack.getItem().getClass())) {
					Method m = cls.getMethod("getRGBa", ItemStack.class);
					short[] rgba = (short[]) m.invoke(stack.getItem(), stack);
					return new Color(rgba[0], rgba[1], rgba[2], rgba[3]).getRGB();
				}
			} catch (Exception e) {
			}
		return stack.getItem().getColorFromItemStack(stack, pass);
	}

	@SuppressWarnings("unchecked")
	private static Color getColour(String oreName) {
		ArrayList<ItemStack> ores = OreDictionary.getOres("ingot" + oreName);
		if (ores.isEmpty())
			return null;

		float red = 0;
		float green = 0;
		float blue = 0;
		ArrayList<Color> colours = new ArrayList<Color>();
		ItemModelMesher mesher = Minecraft.getMinecraft().getRenderItem().getItemModelMesher();
		for (ItemStack stack : ores) {
			IBakedModel model = mesher.getItemModel(stack);
			try {
				BufferedImage texture = ImageIO.read(Minecraft.getMinecraft().getResourceManager().getResource(getIconResource(model)).getInputStream());
				Color texColour = getAverageColour(texture);
				colours.add(texColour);
				for (BakedQuad quad : (List<BakedQuad>) model.func_177550_a()) {
					int c = getStackColour(stack, quad.func_178211_c());
					if (c != 0xFFFFFF) {
						colours.add(new Color(c));
						colours.remove(texColour);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}
		}

		for (Color c : colours) {
			red += c.getRed();
			green += c.getGreen();
			blue += c.getBlue();
		}
		float count = colours.size();
		return new Color((int) (red / count), (int) (green / count), (int) (blue / count)).brighter();
	}

	private static Color getAverageColour(BufferedImage image) {
		float red = 0;
		float green = 0;
		float blue = 0;
		float count = 0;
		for (int i = 0; i < image.getWidth(); i++)
			for (int j = 0; j < image.getHeight(); j++) {
				Color c = new Color(image.getRGB(i, j));
				if (c.getAlpha() <= 10 || c.getRed() <= 10 && c.getGreen() <= 10 && c.getBlue() <= 10)
					continue;
				red += c.getRed();
				green += c.getGreen();
				blue += c.getBlue();
				count++;
			}

		return new Color((int) (red / count), (int) (green / count), (int) (blue / count));
	}

	private static ResourceLocation getIconResource(IBakedModel model) {
		String iconName = model.getTexture().getIconName();
		if (iconName == null)
			return null;

		String string = "minecraft";

		int colonIndex = iconName.indexOf(58);
		if (colonIndex >= 0) {
			if (colonIndex > 1)
				string = iconName.substring(0, colonIndex);

			iconName = iconName.substring(colonIndex + 1, iconName.length());
		}

		string = string.toLowerCase();
		iconName = "textures/" + iconName + ".png";
		System.out.println(new ResourceLocation(string, iconName));
		return new ResourceLocation(string, iconName);
	}
}
package ganymedes01.aobd.items;

import ganymedes01.aobd.AOBD;
import ganymedes01.aobd.lib.Reference;
import ganymedes01.aobd.ore.Ore;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;

public class AOBDItem extends Item /*implements IItemColor*/{

	private Boolean hasEffect = null;

	private final Ore ore;
	private final String base;

	public AOBDItem(String base, Ore ore) {
		this.ore = ore;
		this.base = base;
		setCreativeTab(AOBD.tab);
		setUnlocalizedName(Reference.MOD_ID + "." + base + ore);
	}

	public String getBaseName() {
		return base;
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack) {
		return String.format(I18n.translateToLocal("item.aobd." + base + ".name"), ore.name());
	}

//	@Override
//	@SideOnly(Side.CLIENT)
//	public int getColorFromItemstack(ItemStack stack, int pass) {
//		return pass == 0 ? ore.colour() : -1;
//	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean hasEffect(ItemStack stack) {
		if (hasEffect == null) {
			hasEffect = false;
			for (ItemStack ingot : OreDictionary.getOres("ingot" + ore.name()))
				if (ingot != null && ingot.getItem().hasEffect(stack))
					hasEffect = true;
		}

		return hasEffect;
	}

}
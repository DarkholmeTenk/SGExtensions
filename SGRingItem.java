//------------------------------------------------------------------------------------------------
//
//   SG Craft - Stargate ring block item
//
//------------------------------------------------------------------------------------------------

package sgextensions;

import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;

public class SGRingItem extends ItemBlock
{
	
	public final static int subBlockMask = 0x01;

	public SGRingItem(int id)
	{
		super(id);
		setHasSubtypes(true);
	}

	@Override
	public Icon getIconFromDamage(int i)
	{
		SGRingBlock t = (SGRingBlock) SGExtensions.sgRingBlock;
		return t.getBlockTextureFromLocalSideAndMetadata(0, i);
	}

	@Override
	public int getMetadata(int i)
	{
		return i;
	}

	@Override
	public String getUnlocalizedName(ItemStack stack)
	{
		String result = subItemName(stack.getItemDamage());
		//System.out.printf("SGRingItem.getItemNameIS: %s --> %s\n", stack, result);
		return result;
	}

	public static String subItemName(int i)
	{
		return "tile.gcewing.sg.stargateRing." + i;
	}

//	@Override
//	public String getLocalItemName(ItemStack par1ItemStack) {
//		String var2 = this.getItemNameIS(par1ItemStack);
//		System.out.printf("SGRingItem.getLocalItemName: key = %s\n", var2);
//		if (var2 == null)
//			return "";
//		String result = StatCollector.translateToLocal(var2);
//		System.out.printf("SGRingItem.getLocalItemName: result = %s\n", result);
//		return result;
//	}

}

//------------------------------------------------------------------------------------------------
//
//   Greg's Mod Base - Generic Textured Item
//
//------------------------------------------------------------------------------------------------

package sgextensions;

import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

public class BaseItem extends Item
{
	String textureName;

	public BaseItem(int id)
	{
		super(id);
		setCreativeTab(SGExtensions.sgCreative);
	}
	
	@Override
	public void updateIcons(IconRegister iR)
	{
		this.iconIndex = iR.registerIcon(SGExtensions.baseLocation + this.getUnlocalizedName());
	}

}

package sgextensions;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class SGDarkGDO extends BaseItem
{
	
	int gdoCode;
	
	public int getGDOCode()
	{
		return gdoCode;
	}
	
	public void setGDOCode(int c)
	{
		gdoCode = c;
	}
	

	public SGDarkGDO(int id)
	{
		super(id);
		this.setCreativeTab(SGExtensions.sgCreative);
		this.setUnlocalizedName("sgDarkGDO");
		this.setMaxStackSize(1);
	}
	
	@Override
	public void addInformation(ItemStack IS, EntityPlayer player, List data,boolean Huh)
	{
		if(IS.stackTagCompound != null)
		{
			if(IS.stackTagCompound.getString("gdoCode") != null)
			{
				data.add("GDO Code: " + IS.stackTagCompound.getString("gdoCode"));
			}
		}
	}
	
	@Override
	public ItemStack onItemRightClick(ItemStack is,World w,EntityPlayer player)
	{
		player.openGui(SGExtensions.instance, SGExtensions.GUIELEMENT_GDO, w,(int)player.posX,(int)player.posY, (int)player.posZ);
		return is;
	}

}

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
		super(id, "/sgextensions/resources/textures.png");
		this.setCreativeTab(SGExtensions.sgCreative);
		this.setItemName("sgDarkGDO");
		this.setIconIndex(0x54);
		this.setMaxStackSize(1);
	}
	
	@Override
	public void addInformation(ItemStack IS, EntityPlayer player, List data,boolean Huh)
	{
		if(IS.stackTagCompound.getString("gdoCode") != null)
		{
			data.add("GDO Code: " + IS.stackTagCompound.getString("gdoCode"));
		}
	}
	
	@Override
	public ItemStack onItemRightClick(ItemStack is,World w,EntityPlayer player)
	{
		Vec3 pos = player.getPosition(0);
		player.openGui(SGExtensions.instance, SGExtensions.GUIELEMENT_GDO, w,(int)pos.xCoord,(int)pos.yCoord, (int)pos.zCoord);
		return is;
	}
	
	/*@Override
	public boolean onItemUse(ItemStack par1ItemStack, EntityPlayer player, World world, int x, int y, int z, int par7, float par8, float par9, float par10)
    {
		player.openGui(SGExtensions.instance, SGExtensions.GUIELEMENT_GDO, world, x, y, z);
		return true;
    }*/
	
	@Override
	public String getTextureFile()
	{
		return "/sgextensions/resources/textures.png";
	}

}

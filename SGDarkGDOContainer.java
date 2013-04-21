package sgextensions;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class SGDarkGDOContainer extends Container
{
	private String gdoCode = "0000";
	EntityPlayer player;
	
	SGDarkGDOContainer(EntityPlayer player)
	{
		ItemStack GDOIS = player.getCurrentEquippedItem();
		if(GDOIS != null)
		{
			if(GDOIS.stackTagCompound == null)
			{
				GDOIS.stackTagCompound = new NBTTagCompound();
				GDOIS.stackTagCompound.setString("gdoCode", "0000");
				gdoCode = "0000";
			}
			else
			{
				gdoCode = GDOIS.stackTagCompound.getString("gdoCode");
				if(gdoCode == null)
				{
					GDOIS.stackTagCompound.setString("gdoCode", "0000");
					gdoCode = "0000";
				}
			}
		}
		this.player = player;
	}
	
	@Override
	public boolean canInteractWith(EntityPlayer var1) {
		// TODO Auto-generated method stub
		return true;
	}

}

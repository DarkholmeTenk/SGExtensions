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
	boolean gate = false;
	int gX,gY,gZ;
	int delay = 1;
	String message = "";
	
	SGDarkGDOContainer(EntityPlayer player)
	{
		ItemStack GDOIS = player.getCurrentEquippedItem();
		
		if(GDOIS.stackTagCompound == null)
		{
			GDOIS.stackTagCompound = new NBTTagCompound();
			GDOIS.stackTagCompound.setString("gdoCode", "0000");
		}
		else
		{
			gdoCode = GDOIS.stackTagCompound.getString("gdoCode");
		}
		this.player = player;
		message = getMessage();
	}
	
	public String getMessage()
	{
		SGBaseTE g = findNearestStargate(player.worldObj,(int) player.posX,(int) player.posY,(int) player.posZ);
		if(g != null)
		{
			return g.getRadioSignal();
		}
		return "";
	}
	
	public SGBaseTE findNearestStargate(World w,int x,int y,int z)
	{
		if(gate == true)
		{
			TileEntity te = w.getBlockTileEntity(gX,gY,gZ);
			if(te != null)
			{
				if(te instanceof SGBaseTE)
				{
					delay = 1;
					return (SGBaseTE) te;
				}
			}
			gate = false;
		}
		delay--;
		if(delay == 0)
		{
			if(!gate)
			{
				Vec3 pos = player.getPosition(1);
				int radius = 7;
				int zx = (int) pos.xCoord;
				int zy = (int) pos.yCoord;
				int zz = (int) pos.zCoord;
				boolean fGate = false;
				for(int i=-radius;i<=radius && !fGate;i++)
				{
					for(int j=-radius;j<=radius && !fGate;j++)
					{
						for(int k=-radius;k<=radius && !fGate;k++)
						{
							if((Math.pow(i,2) + Math.pow(j,2) + Math.pow(k,2)) <= Math.pow(radius, 2))
							{
								TileEntity te = w.getBlockTileEntity(i+x, j+y, k+z);
								if(te != null)
								{
									if(te instanceof SGBaseTE)
									{
										gate = true;
										gX = te.xCoord;
										gY = te.yCoord;
										gZ = te.zCoord;
									}
								}
							}
						}
					}
				}
			}
		}
		delay = 20;
		return null;
	}
	
	@Override
	public boolean canInteractWith(EntityPlayer var1) {
		// TODO Auto-generated method stub
		return true;
	}

}

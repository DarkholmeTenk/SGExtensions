//------------------------------------------------------------------------------------------------
//
//   SG Craft - Structure representing the location of a stargate
//
//------------------------------------------------------------------------------------------------

package sgextensions;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;

public class SGLocation
{

	public int dimension;
	public int x, y, z;

	public SGLocation(TileEntity te)
	{
		this(te.worldObj.provider.dimensionId, te.xCoord, te.yCoord, te.zCoord);
	}

	public SGLocation(int dimension, int x, int y, int z)
	{
		this.dimension = dimension;
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public SGLocation(NBTTagCompound nbt)
	{
		dimension = nbt.getInteger("dimension");
		x = nbt.getInteger("x");
		y = nbt.getInteger("y");
		z = nbt.getInteger("z");
	}

	NBTTagCompound toNBT()
	{
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setInteger("dimension", dimension);
		nbt.setInteger("x", x);
		nbt.setInteger("y", y);
		nbt.setInteger("z", z);
		return nbt;
	}

	SGBaseTE getStargateTE()
	{
		World world = DimensionManager.getWorld(dimension);
		if(world != null)
		{
			TileEntity te = world.getBlockTileEntity(x, y, z);
			if (te instanceof SGBaseTE)
			{
				Ticket ticket = ((SGBaseTE) te).getChunkTicket();
				((SGBaseTE) te).reinstateChunkTicket(ticket);
				return (SGBaseTE) te;
			}
		}
		else
		{
			if(DimensionManager.getWorld(0) != null)
			{
				DimensionManager.initDimension(dimension);
				world = DimensionManager.getWorld(dimension);
				if(world != null)
				{
					TileEntity te = world.getBlockTileEntity(x, y, z);
					if (te instanceof SGBaseTE)
					{
						Ticket ticket = ((SGBaseTE) te).getChunkTicket();
						((SGBaseTE) te).reinstateChunkTicket(ticket);
						return (SGBaseTE) te;
					}
				}
			}
		}
		return null;
	}

}

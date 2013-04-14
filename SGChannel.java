//------------------------------------------------------------------------------------------------
//
//   SG Craft - Packet Handling
//
//------------------------------------------------------------------------------------------------

package sgextensions;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

//import net.minecraft.item.Item;
//import net.minecraft.item.ItemStack;

enum PacketType
{
	SetHomeAddress, ConnectOrDisconnect, gdoChange, gdoMess;
}

public class SGChannel extends BaseNBTChannel<PacketType>
{

	static SGChannel network;

	public SGChannel(String channelName)
	{
		super(channelName);
		network = this;
	}

	@Override
	public void onReceiveFromClient(PacketType type, NBTTagCompound nbt, EntityPlayer player)
	{
		System.out.println("datarecieve");
		switch (type)
		{
			case SetHomeAddress:
				//handleSetHomeAddressFromClient(nbt, player);
				break;
			case ConnectOrDisconnect:
				handleConnectOrDisconnectFromClient(nbt, player);
				break;
			case gdoChange:
				gdoData(player,nbt);
				break;
		}
	}
	
	public static void gdoData(EntityPlayer player,NBTTagCompound nbt)
	{
		if(nbt.getBoolean("gdoSend"))
		{
			String code = nbt.getString("gdoCode");
			World w = player.worldObj;
			Vec3 pos = player.getPosition(1);
			int radius = 7;
			int x = (int) pos.xCoord;
			int y = (int) pos.yCoord;
			int z = (int) pos.zCoord;
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
									fGate = true;
									((SGBaseTE)te).sendRadioSignal(code + "," + player.username);
								}
							}
						}
					}
				}
			}
		}
		else
		{
			String code = nbt.getString("gdoCode");
			if(player.getCurrentEquippedItem() != null)
				if(player.getCurrentEquippedItem().getItem() instanceof SGDarkGDO)
					player.getCurrentEquippedItem().stackTagCompound.setString("gdoCode", code);
		}
	}

	public static void sendConnectOrDisconnectToServer(SGBaseTE te, String address)
	{
		NBTTagCompound nbt = te.nbtWithCoords();
		nbt.setString("address", address);
		network.sendToServer(PacketType.ConnectOrDisconnect, nbt);
	}
	
	public static void sendGdoChangeToServer(EntityPlayer player,String code,boolean Send)
	{
		NBTTagCompound itemNBT = new NBTTagCompound();
		itemNBT.setString("gdoCode", code);
		itemNBT.setBoolean("gdoSend", Send);
		network.sendToServer(PacketType.gdoChange, itemNBT);
	}

	static void handleConnectOrDisconnectFromClient(NBTTagCompound nbt, EntityPlayer player)
	{
		SGBaseTE te = SGBaseTE.at(player.worldObj, nbt);
		String address = nbt.getString("address");
		te.connectOrDisconnect(address, player);
	}

	public static void sendSetHomeAddressToServer(SGBaseTE te, String address)
	{
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setInteger("x", te.xCoord);
		nbt.setInteger("y", te.yCoord);
		nbt.setInteger("z", te.zCoord);
		nbt.setString("address", address);
		network.sendToServer(PacketType.SetHomeAddress, nbt);
	}

//	void handleSetHomeAddressFromClient(NBTTagCompound nbt, EntityPlayer player) {
//		int x = nbt.getInteger("x");
//		int y = nbt.getInteger("y");
//		int z = nbt.getInteger("z");
//		String address = nbt.getString("address");
//		System.out.printf("SGChannel.handleSetHomeAddressFromClient: (%d,%d,%d) '%s' \n", x, y, z, address);
//		SGBaseTE te = SGBaseTE.at(player.worldObj, x, y, z);
//		if (te != null)
//			te.setHomeAddress(address);
//	}

}

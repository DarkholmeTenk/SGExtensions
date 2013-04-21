//------------------------------------------------------------------------------------------------
//
//   SG Craft - Stargate base tile entity
//
//------------------------------------------------------------------------------------------------

package sgextensions;

import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityTrackerEntry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet132TileEntityData;
import net.minecraft.network.packet.Packet41EntityEffect;
import net.minecraft.network.packet.Packet9Respawn;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.Teleporter;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.DimensionManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public class SGBaseTE extends BaseChunkLoadingTE implements IInventory
{

//	public final static String symbolChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ-";
//	public final static int numRingSymbols = 27;
//	public final static double ringAcceleration = 1.0;
//	final static double diallingRelaxationRate = 0.1;

	public final static String symbolChars = SGAddressing.symbolChars;
	public final static int numRingSymbols = SGAddressing.numSymbols;
	public final static double ringSymbolAngle = 360.0 / numRingSymbols;

	final static int diallingTime = 40; // ticks
	final static int quickDiallingTime = 5;
	final static int interDiallingTime = 10; // ticks
	final static int quickInterDiallingTime = 2;
	final static int transientDuration = 20; // ticks
	final static int disconnectTime = 30; // ticks
	private int timeSinceLastTeleport = -1;

	final static double openingTransientIntensity = 1.3; //2.0;
	final static double openingTransientRandomness = 0.15;
	final static double closingTransientRandomness = 0.15;
	final static double transientDamageRate = 50;
	final static int fuelPerItem = SGExtensions.fuelAmount;
	final static int maxFuelBuffer = SGExtensions.fuelStore * fuelPerItem;
	final static int fuelToOpen = fuelPerItem;
	private int modifiedFuelToOpen;
	final static int irisTimerVal = 1;
	
	public boolean useFuel = true;

	static Random random = new Random();
	static DamageSource transientDamage = new TransientDamageSource();
	static DamageSource irisDamage = new irisDamageSource();
	static DamageSource recieveDamage = new recieveDamageSource();

	public boolean isMerged;
	
	public SGIrisState irisVarState = SGIrisState.Open;
	public String irisType = "iris";
	public int irisSlide;
	private int irisTimer;
	
	public SGState state = SGState.Idle;
	public double ringAngle, lastRingAngle, targetRingAngle; // degrees
	public int numEngagedChevrons;
	public String dialledAddress = "";

	//public String dialledAddress =  "MYNCRFT"; // "AAAAAAA";
	public boolean isLinkedToController;
	public int linkedX, linkedY, linkedZ;
	SGLocation connectedLocation;
	boolean isInitiator;
	int timeout;
	public int fuelBuffer;

	public boolean safeDial = false;
	public boolean quickDial = false;
	public boolean isAdminGate = false;
	
	private ArrayList safeEnts = new ArrayList<Entity>();
	private HashMap safeTime = new HashMap();
	final static int safeTicks = 20*10;
	
	IInventory inventory = new InventoryBasic("Stargate", 7);
	final static int upgradeSlots = 3;
	final static int fuelSlots = 4;
	final static int fuelSlot = 0;
	static int timeToList = 20;
	static boolean hasNBTListed = false;
	
	protected String radioMessage;

	//ArrayList<PendingTeleportation> pendingTeleportations = new ArrayList<PendingTeleportation>();
	//public String homeAddress = "";

	double ehGrid[][][];
//	double ringVelocity; // degrees per tick

	@Override
	void onAddedToWorld()
	{
		//System.out.printf("SGBaseTE.onAddedToWorld\n");
		setForcedChunkRange(0, 0, 0, 0);
	}

	public static SGBaseTE at(IBlockAccess world, int x, int y, int z)
	{
		TileEntity te = world.getBlockTileEntity(x, y, z);
		if (te instanceof SGBaseTE)
			return (SGBaseTE) te;
		else
			return null;
	}
	
	public void checkUpgrades()
	{
		for (int i=0;i<upgradeSlots;i++)
		{
			ItemStack slotItem = inventory.getStackInSlot(i+fuelSlots);
			if(slotItem != null)
			{
				if(slotItem.getItem() instanceof SGDarkMultiItem)
				{
					if(((SGDarkMultiItem)(slotItem.getItem())).isUpgradeType("Stargate Upgrade - Admin",slotItem))
					{
						inventory.setInventorySlotContents(i+fuelSlots, null);
						inventory.decrStackSize(i+fuelSlots, 1);
						isAdminGate = true;
						onInventoryChanged();
						markBlockForUpdate();
					}
				}
			}
		}
	}
	
	public String getIrisType()
	{
		ItemStack is = getStackInSlot(4);
		if(is != null)
		{
			if(is.getItem() instanceof SGDarkMultiItem)
			{
				SGDarkMultiItem mI = (SGDarkMultiItem)is.getItem();
				if(mI.isUpgradeType("Stargate Upgrade - Iris",is))
				{
					return "Iris";
				}
				else if(mI.isUpgradeType("Stargate Upgrade - Shield",is))
				{
					return "Shield";
				}
			}
		}
		if(irisVarState != SGIrisState.Open)
		{
			IrisStateFromNum(0);
			irisChangeNotify();
		}
		return null;
	}
	
	public int IrisStateToNum()
	{
		switch(irisVarState)
		{
			case Open:return 0;
			case Closing:return 1;
			case Closed:return 2;
			case Opening:return 3;
			default:
				return 0;
		}
	}
	
	public void IrisStateFromNum(int num)
	{
		if(num == 0)irisVarState=SGIrisState.Open;
		if(num == 1)irisVarState=SGIrisState.Closing;
		if(num == 2)irisVarState=SGIrisState.Closed;
		if(num == 3)irisVarState=SGIrisState.Opening;
		this.sendComputerEvent("iris", irisState(), "");
		markBlockForUpdate();
	}
	
	public String irisState()
	{
		//System.out.printf("SGBaseTE Iris State - %d\n", irisVarState);
		if(getIrisType() == null)
		{
			return "Error - No Iris";
		}
		else
		{
			if(IrisStateToNum() == 0)
				return "Iris - Open";
			else if(IrisStateToNum() == 1)
				return "Iris - Closing";
			else if(IrisStateToNum() == 2)
				return "Iris - Closed";
			else if(IrisStateToNum() == 3)
				return "Iris - Opening";
		}
		return "Error - Unknown state";
	}
	
	public boolean solidIris()
	{
		String iS = irisState();
		if(iS == "Iris - Open" || iS == "Error - Unknown state" || iS == "Error - No Iris")
			return false;
		return true;
	}
	
	private void irisChangeNotify()
	{
		int x = this.xCoord;
		int y = this.yCoord+1;
		int z = this.zCoord;
		World world = worldObj;
		world.setBlockMetadataWithNotify(x, y, z, 1 - world.getBlockMetadata(x,y,z));
	}
	
	public String openIris()
	{
		String IT = getIrisType();
		if(IT != null)
		{
			if(IrisStateToNum() == 2)
			{
				if(IT == "Iris")
				{
					IrisStateFromNum(3);
					irisSlide = 0;
					irisTimer = irisTimerVal;
				}
				else if(IT == "Shield")
				{
					IrisStateFromNum(0);
				}
				irisChangeNotify();
				return "Iris opened";
			}
			else if(IrisStateToNum() == 1 || IrisStateToNum() == 3)
			{
				return "Error - Iris in motion";
			}
		}
		return "Error - No iris";
	}
	
	public String closeIris()
	{
		String IT = getIrisType();
		if(IT != null)
		{
			if(IrisStateToNum() == 0)
			{
				if(IT == "Iris")
				{
					IrisStateFromNum(1);
					irisSlide = SGExtensions.irisFrames - 1;
					irisTimer = irisTimerVal;
				}
				else if(IT == "Shield")
				{
					IrisStateFromNum(2);
				}
				irisChangeNotify();
				return "Iris closed";
			}
			else if(IrisStateToNum() == 1 || IrisStateToNum() == 3)
			{
				return "Error - Iris in motion";
			}
			
		}
		return "Error - No iris";
	}
	
	public String toggleIris()
	{
		String IT = getIrisType();
		if(IT != null)
		{
			if(irisState() == "Iris - Open")
			{
				return closeIris();
			}
			else if(irisState() == "Iris - Closed")
			{
				return openIris();
			}
			else
			{
				return "Error - Iris moving";
			}
		}
		return "Error - No iris";
	}

	public static SGBaseTE at(SGLocation loc)
	{
		if (loc != null)
		{
			World world = DimensionManager.getWorld(loc.dimension);
			if (world != null)
				return SGBaseTE.at(world, loc.x, loc.y, loc.z);
		}
		return null;
	}

	public static SGBaseTE at(IBlockAccess world, NBTTagCompound nbt)
	{
		return SGBaseTE.at(world, nbt.getInteger("x"), nbt.getInteger("y"), nbt.getInteger("z"));
	}

	public int dimension()
	{
		if (worldObj != null)
			return worldObj.provider.dimensionId;
		else
			return -999;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		isMerged = nbt.getBoolean("isMerged");
		state = SGState.valueOf(nbt.getInteger("state"));
		targetRingAngle = nbt.getDouble("targetRingAngle");
		numEngagedChevrons = nbt.getInteger("numEngagedChevrons");
		//homeAddress = nbt.getString("homeAddress");
		dialledAddress = nbt.getString("dialledAddress");
		isLinkedToController = nbt.getBoolean("isLinkedToController");
		linkedX = nbt.getInteger("linkedX");
		linkedY = nbt.getInteger("linkedY");
		linkedZ = nbt.getInteger("linkedZ");
		irisVarState = SGIrisState.valueOf(nbt.getInteger("irisState"));
		irisSlide = nbt.getInteger("irisSlide");
		if (nbt.hasKey("connectedLocation"))
			connectedLocation = new SGLocation(nbt.getCompoundTag("connectedLocation"));
		isInitiator = nbt.getBoolean("isInitiator");
		timeout = nbt.getInteger("timeout");
		fuelBuffer = nbt.getInteger("fuelBuffer");
		isAdminGate= nbt.getBoolean("isAdminGate");
		radioMessage = nbt.getString("radioMessage");
		if(!hasNBTListed)
		{
			timeToList = 10;
			hasNBTListed = true;
		}
		//System.out.printf("SGBaseTE.readFromNBT: (%d; %d, %d, %d) state = %s(%d)\n",
		//	dimension(), xCoord, yCoord, zCoord, state, state.ordinal());
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		nbt.setBoolean("isMerged", isMerged);
		nbt.setInteger("state", state.ordinal());
		nbt.setDouble("targetRingAngle", targetRingAngle);
		int TestInt = irisVarState.ordinal();
		nbt.setInteger("irisState", TestInt);
		nbt.setInteger("irisSlide", irisSlide);
		nbt.setInteger("numEngagedChevrons", numEngagedChevrons);
		//nbt.setString("homeAddress", homeAddress);
		nbt.setString("dialledAddress", dialledAddress);
		nbt.setBoolean("isLinkedToController", isLinkedToController);
		nbt.setInteger("linkedX", linkedX);
		nbt.setInteger("linkedY", linkedY);
		nbt.setInteger("linkedZ", linkedZ);
		if (connectedLocation != null)
			nbt.setCompoundTag("connectedLocation", connectedLocation.toNBT());
		nbt.setBoolean("isInitiator", isInitiator);
		nbt.setInteger("timeout", timeout);
		nbt.setInteger("fuelBuffer", fuelBuffer);
		nbt.setBoolean("isAdminGate",isAdminGate);
		if(radioMessage == null){radioMessage="";	}
		nbt.setString("radioMessage", radioMessage);
		if (!worldObj.isRemote)
		{
			//System.out.printf("SGBaseTE.writeToNBT: (%d; %d, %d, %d) state = %s\n",
			//	dimension(), xCoord, yCoord, zCoord, nbt.getInteger("state"));
		}
	}

	public NBTTagCompound nbtWithCoords()
	{
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setInteger("x", xCoord);
		nbt.setInteger("y", yCoord);
		nbt.setInteger("z", zCoord);
		return nbt;
	}

	static boolean isValidSymbolChar(String c)
	{
		return SGAddressing.isValidSymbolChar(c);
	}

	static char symbolToChar(int i)
	{
		return SGAddressing.symbolToChar(i);
	}

	static int charToSymbol(char c)
	{
		return SGAddressing.charToSymbol(c);
	}

	static int charToSymbol(String c)
	{
		return SGAddressing.charToSymbol(c);
	}

	public String getHomeAddress() throws SGAddressing.AddressingError
	{
		return SGAddressing.addressForLocation(new SGLocation(this));
	}

	public SGBaseBlock getBlock()
	{
		return (SGBaseBlock) getBlockType();
	}

	public int getRotation()
	{
		return getBlockMetadata() & SGBaseBlock.rotationMask;
	}

	public double interpolatedRingAngle(double t)
	{
		return Utils.interpolateAngle(lastRingAngle, ringAngle, t);
	}

	@Override
	public boolean canUpdate()
	{
		return true;
	}

	@Override
	public void updateEntity()
	{
		if (worldObj.isRemote)
			clientUpdate();
		else
			serverUpdate();
	}

	String side()
	{
		return worldObj.isRemote ? "Client" : "Server";
	}

	void enterState(SGState newState, int newTimeout)
	{
		//System.out.printf("SGBaseTE: %s entering state %s with timeout %s\n",
				//side(), newState, newTimeout);
		state = newState;
		timeout = newTimeout;
		onInventoryChanged();
		markBlockForUpdate();
	}

	public boolean isConnected()
	{
		return state == SGState.Transient || state == SGState.Connected || state == SGState.Disconnecting;
	}

//	public void setHomeAddress(String newAddress) {
//		System.out.printf("SGBaseTE.setHomeAddress to %s in %s\n", newAddress, worldObj);
//		if (worldObj.isRemote) {
//			homeAddress = newAddress;
//			SGChannel.sendSetHomeAddressToServer(this, newAddress);
//		}
//		else {
//			if (homeAddress != newAddress) {
//				System.out.printf("SGBaseTE.setHomeAddress: Updating address\n");
//				homeAddress = newAddress;
//				SGLocation location = new SGLocation(this);
//				SGGlobal.setLocationForAddress(homeAddress, location);
//				worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
//			}
//		}
//	}

	SGControllerTE getLinkedControllerTE()
	{
		if (isLinkedToController)
		{
			TileEntity cte = worldObj.getBlockTileEntity(linkedX, linkedY, linkedZ);
			if (cte instanceof SGControllerTE)
				return (SGControllerTE) cte;
		}
		return null;
	}

	void checkForLink()
	{
		int range = SGControllerTE.linkRangeZ;
		for (int i = -range; i <= range; i++)
			for (int j = -range; j <= range; j++)
				for (int k = -range; k <= range; k++)
				{
					TileEntity te = worldObj.getBlockTileEntity(xCoord + i, yCoord + j, zCoord + k);
					if (te instanceof SGControllerTE)
						((SGControllerTE) te).checkForLink();
				}
	}
	
	public boolean isLinked()
	{
		if(isLinkedToController)
		{
			SGControllerTE test = getLinkedControllerTE();
			if(test != null)
			{
				return true;
			}
			else
			{
				TileEntity te = worldObj.getBlockTileEntity(linkedX, linkedY, linkedZ);
				if(te instanceof SGDarkDiallerTE)
				{
					SGBaseTE meTest = ((SGDarkDiallerTE)te).getLinkedGate();
					if(meTest != null)
					{
						if(meTest.xCoord == xCoord && meTest.yCoord == yCoord && meTest.zCoord == zCoord)
						{
							return true;
						}
					}
				}
			}
		}
		isLinkedToController = false;
		return false;
	}

	public void unlinkFromController()
	{
		if (isLinkedToController)
		{
			SGControllerTE cte = getLinkedControllerTE();
			clearLinkToController();
			if (cte != null)
				cte.clearLinkToStargate();
		}
	}

	public void clearLinkToController()
	{
		//System.out.printf("SGBaseTE: Unlinking stargate at (%d, %d, %d) from controller\n",
				//xCoord, yCoord, zCoord);
		isLinkedToController = false;
		//markBlockForUpdate();
		onInventoryChanged();
	}

	//------------------------------------   Server   --------------------------------------------
	
	public String ControlledDisconnect()
	{
		return ControlledDisconnect(false);
	}
	
	public String ControlledDisconnect(boolean force)
	{
		if(state != SGState.Idle)
		{
			boolean canDisconnect = isInitiator  || !SGExtensions.recieveHardMode || force; //isInitiator;
			SGBaseTE dte = getConnectedStargateTE();
			boolean validConnection =
					(dte != null) && (dte.getConnectedStargateTE() == this);
			if (canDisconnect || !validConnection)
			{
				if (state != SGState.Disconnecting)
					 return disconnect();
			} 
			else if (!canDisconnect)
				return "Error - Not initiator";
		}
		return "Error - Not active";
	}
	
	String ControlledConnect(String address, EntityPlayer player, boolean safe, boolean quick)
	{
		String homeAddress = findHomeAddress();
		if(address.toUpperCase() != homeAddress.toUpperCase())
		{
			SGBaseTE dte = SGAddressing.findAddressedStargate(address);
			//System.out.printf("SGBaseTE.connect: addressed TE = %s\n", dte);
			if (dte == null)
			{
				diallingFailure(player, "No stargate at address " + address);
				return "Error - No stargate at address " + address;
			}
			this.radioMessage = null;
			//System.out.printf("SGBaseTE.connect: addressed TE state = %s\n", dte.state);
			if (dte.state != SGState.Idle)
			{
				diallingFailure(player, "Stargate at address " + address + " is busy");
				return "Error - Stargate at address " + address + " is busy";
			}
			if (!reloadFuel(fuelToOpen))
			{
				diallingFailure(player, "Stargate has insufficient fuel");
				return "Error - Stargate has insufficient fuel";
			}
			modifiedFuelToOpen = fuelToOpen;
			
			safeDial = safe && (safeDial || shouldSafeDial());
			if(safeDial)
			{
				if(reloadFuel(fuelToOpen*SGExtensions.safeFuelMod))
				{
					modifiedFuelToOpen = modifiedFuelToOpen * SGExtensions.safeFuelMod;
				}
				else
				{
					safeDial = false;
				}
			}
			
			quickDial = quick && (quickDial || shouldQuickDial());
			if(quickDial)
			{
				if(reloadFuel(fuelToOpen*SGExtensions.quickFuelMod))
				{
					modifiedFuelToOpen = modifiedFuelToOpen * SGExtensions.quickFuelMod;
				}
				else
				{
					quickDial = false;
				}
			}
			
			if (!reloadFuel(modifiedFuelToOpen))
			{
				diallingFailure(player, "Stargate has insufficient fuel");
				return "Error - Stargate has insufficient fuel";
			}
			dte.safeDial = (this.safeDial && !this.solidIris()) || dte.solidIris();
			dte.quickDial = this.quickDial;
			startDiallingStargate(address, dte, true);
			dte.startDiallingStargate(homeAddress, this, false);
			return "Dialling";
		}
		return "Cannot dial self";
	}
	
	public String connectOrDisconnect(String address, EntityPlayer player)
	{
		//System.out.printf("SGBaseTE: %s: connectOrDisconnect('%s') in state %s by %s\n",
				//side(), address, state, player);
		if (state == SGState.Idle)
		{
			if (address.length() == SGAddressing.addressLength)
				return connect(address, player);
			else
				return "Error - Invalid Address";
		} 
		else
		{
			return ControlledDisconnect();
		}
	}

	String connect(String address, EntityPlayer player)
	{
		return ControlledConnect(address,player,true,true);
	}
	
	String connect(String address, EntityPlayer player,Boolean noFuel)
	{
		useFuel = !noFuel;
		return ControlledConnect(address,player,true,true);
	}
	
	void diallingFailure(EntityPlayer player, String mess)
	{
		useFuel = !this.isAdminGate;
		if(player != null)
			player.addChatMessage(mess);
		playSoundEffect("sgextensions.sg_abort", 2.0F, 1.0F);
	}

	String findHomeAddress()
	{
		String homeAddress;
		try
		{
			return getHomeAddress();
		}
		catch (SGAddressing.AddressingError e)
		{
			//System.out.printf("SGBaseTE.findHomeAddress: %s\n", e);
			return "";
		}
	}

	public String disconnect()
	{
		useFuel = !this.isAdminGate;
		this.radioMessage = null;
		safeEnts.clear();
		safeTime.clear();
		//System.out.printf("SGBaseTE: %s: disconnect()\n", side());
		SGBaseTE dte = SGBaseTE.at(connectedLocation);
		if (dte != null)
			dte.clearConnection();
		clearConnection();
		return "Disconncted";
	}

	public void clearConnection()
	{
		if (state != SGState.Idle || connectedLocation != null)
		{
			//System.out.printf("SGBaseTE.clearConnection: Resetting state\n");
			dialledAddress = "";
			connectedLocation = null;
			isInitiator = false;
			numEngagedChevrons = 0;
			onInventoryChanged();
			markBlockForUpdate();
			if (state == SGState.Connected)
			{
				enterState(SGState.Disconnecting, disconnectTime);
				//sendClientEvent(SGEvent.StartDisconnecting, 0);
				playSoundEffect("sgextensions.sg_close", 1.0F, 1.0F);
			} 
			else
			{
				if (state != SGState.Idle && state != SGState.Disconnecting)
					playSoundEffect("sgextensions.sg_abort", 1.0F, 1.0F);
				enterState(SGState.Idle, 0);
				//sendClientEvent(SGEvent.FinishDisconnecting, 0);
			}
		}
	}

	void startDiallingStargate(String address, SGBaseTE dte, boolean initiator)
	{
		//System.out.printf("SGBaseTE.startDiallingStargate %s, initiator = %s\n",
				//dte, initiator);
		dialledAddress = address;
		if(address != this.findHomeAddress())
		{
			connectedLocation = new SGLocation(dte);
			isInitiator = initiator;
			//markBlockForUpdate();
			onInventoryChanged();
			if(quickDial == false)
			{
				startDiallingNextSymbol();
			}
			else
			{
				numEngagedChevrons = SGAddressing.addressLength;
				finishDiallingAddress();
			}
			this.sendComputerEvent("dial", address, String.valueOf(initiator));
		}
	}

	void serverUpdate()
	{
		if (isMerged)
		{
			SGExtensions.AddressStore.addAddress(findHomeAddress());
			//performPendingTeleportations();
			if(useFuel)
			{
				fuelUsage();
				useFuel();
			}
			if(isAdminGate)
			{
				fuelBuffer = maxFuelBuffer;
			}
			if(state == SGState.Connected)
			{
				if(timeSinceLastTeleport >= 0)
				{
					if(timeSinceLastTeleport == 0)
					{
						disconnect();
					}
					timeSinceLastTeleport--;
				}
				List<Integer> removeArray = new ArrayList<Integer>();
				if (safeEnts.size() > 0)
				{
					//System.out.printf("SGSC: S(%d)\n",safeEnts.size());
					int length = safeEnts.size();
					for(int i = 0;i<length;i++)
					{
						Entity ent = (Entity) safeEnts.get(i);
						UUID entityID = ent.getPersistentID();
						//System.out.printf("SGSC: TL(%d)\n", (int)safeTime.get(entityID));
						safeTime.put(entityID,Integer.parseInt((safeTime.get(entityID).toString())) - 1);
						if(ent.posY < 0)
						{
							ent.setPosition(this.xCoord, this.yCoord+1, this.zCoord);
						}
						if(Integer.parseInt(safeTime.get(entityID).toString()) <= 0)
						{
							removeArray.add(i);
						}
					}
					if(removeArray.size() > 0)
					{
						for(int i = 0;i<removeArray.size();i++)
						{
							int toRemove = removeArray.get(i);
							//System.out.printf("SGSC: REMOVING (%d)\n",toRemove);
							safeEnts.remove(toRemove);
							if(removeArray.size() > i)
							{
								for(int j = i+1;j<removeArray.size();j++)
								{
									if(removeArray.get(j) > removeArray.get(i))
									{
										removeArray.set(j,removeArray.get(j)-1);
									}
								}
							}
						}
					}
				}
			}
			
			if(irisState() == "Iris - Opening" || irisState() == "Iris - Closing")
			{
				irisTimer--;
				if(irisTimer <= 0)
				{
					//System.out.printf("Iris Slide: (%d)\n", irisSlide);
					irisTimer = irisTimerVal;
					if(irisState() == "Iris - Opening")
					{
						irisSlide++;
						if(irisSlide >= SGExtensions.irisFrames)
						{
							IrisStateFromNum(0);
							irisChangeNotify();
						}
					}
					else
					{
						irisSlide --;
						if(irisSlide == 0)
						{
							IrisStateFromNum(2);
							irisChangeNotify();
						}
					}
					markBlockForUpdate();
				}
				
			}
			
			if (timeout > 0)
			{
				//int dimension = worldObj.provider.dimensionId;
				//System.out.printf(
				//	"SGBaseTE.serverUpdate: (%d, %d, %d, %d) state %s, timeout %s\n",
				//		dimension, xCoord, yCoord, zCoord, state, timeout);
				if (state == SGState.Transient)
					performTransientDamage();
				--timeout;
			} 
			else switch (state)
			{
				case Idle:
					if (undialledDigitsRemaining())
						startDiallingNextSymbol();
					break;
				case Dialling:
					finishDiallingSymbol();
					break;
				case InterDialling:
					startDiallingNextSymbol();
					break;
				case Transient:
					enterState(SGState.Connected, 20*60*SGExtensions.maxOpenTime);
					//markBlockForUpdate();
					break;
				case Disconnecting:
					//sendClientEvent(SGEvent.FinishDisconnecting, 0);
					enterState(SGState.Idle, 0);
					//markBlockForUpdate();
					break;
				case Connected:
					disconnect();
					break;
			}
		}
	}

	void fuelUsage()
	{
		if (state == SGState.Connected && isInitiator)
			if (!useFuel(1))
				disconnect();
	}

	boolean useFuel(int amount)
	{
		if(useFuel)
		{
			//System.out.printf("SGBaseTE.useFuel: %d\n", amount);
			if (reloadFuel(amount))
			{
				setFuelBuffer(fuelBuffer - amount);
				return true;
			} else
				return false;
		}
		return true;
	}

	boolean reloadFuel(int amount)
	{
		if(useFuel)
		{
			while (fuelBuffer < amount && fuelBuffer + fuelPerItem <= maxFuelBuffer)
			{
				if (useFuelItem())
					setFuelBuffer(fuelBuffer + fuelPerItem);
				else
					break;
			}
			return fuelBuffer >= amount;
		}
		return true;
	}
	
	public boolean useFuel()
	{
		if(useFuel)
		{
			//System.out.printf("SGBaseTE: Use fuel attempt\n");
			int n = fuelSlots;
			if((fuelBuffer + fuelPerItem) <= maxFuelBuffer)
			{
				for (int i = n - 1; i >= 0; i--)
				{
					//System.out.printf("SGBaseTE: Checking slot %d\n", i);
					ItemStack stack = getStackInSlot(i);
					if (stack != null && stack.getItem() == SGExtensions.stargateFuel.getItem() && stack.getItemDamage() == SGExtensions.stargateFuel.getItemDamage() && stack.stackSize > 0)
					{
						//System.out.printf("SGBaseTE: Valid item in %d\n", i);
						fuelBuffer += fuelPerItem;
						decrStackSize(i, 1);
						onInventoryChanged();
						markBlockForUpdate();
						return true;
					}
				}
			}
		}
		return false;
	}
	
	public void useAllFuel()
	{
		if(useFuel)
		{
			int n = fuelSlots;
			boolean isDone;
			do
			{
				isDone = useFuel();
			}
			while(isDone == true);
		}
	}

	boolean useFuelItem()
	{
		int n = fuelSlots;
		for (int i = n - 1; i >= 0; i--)
		{
			ItemStack stack = getStackInSlot(i);
			if (stack != null && stack == SGExtensions.stargateFuel && stack.stackSize > 0)
			{
				decrStackSize(i, 1);
				return true;
			}
		}
		return false;
	}

	void setFuelBuffer(int amount)
	{
		if (fuelBuffer != amount)
		{
			fuelBuffer = amount;
			onInventoryChanged();
			markBlockForUpdate();
			//System.out.printf("SGBaseTE: Fuel level now %d\n", fuelBuffer);
		}
	}

	void performTransientDamage()
	{
		Trans3 t = localToGlobalTransformation();
		Vector3 p0 = t.p(-1.5, 0.5, 0.5);
		Vector3 p1 = t.p(1.5, 3.5, 5.5);
		Vector3 q0 = p0.min(p1);
		Vector3 q1 = p0.max(p1);
		AxisAlignedBB box = AxisAlignedBB.getBoundingBox(q0.x, q0.y, q0.z, q1.x, q1.y, q1.z);
		//System.out.printf("SGBaseTE.performTransientDamage: players in world:\n");
		//for (Entity ent : (List<Entity>)worldObj.loadedEntityList)
		//	if (ent instanceof EntityPlayer)
		//		System.out.printf("--- %s\n", ent);
		//System.out.printf("SGBaseTE.performTransientDamage: box = %s\n", box);
		List<EntityLiving> ents = worldObj.getEntitiesWithinAABB(EntityLiving.class, box);
		//System.out.printf("SGBaseTE.performTransientDamage: entities in box:\n", box);
		for (EntityLiving ent : ents)
		{
			Vector3 ep = new Vector3(ent.posX, ent.posY, ent.posZ);
			Vector3 gp = t.p(0, 2, 0.5);
			double dist = ep.distance(gp);
			//System.out.printf("SGBaseTE.performTransientDamage: found %s\n", ent);
			if (dist > 1.0)
				dist = 1.0;
			int damage = (int) Math.ceil(dist * transientDamageRate);
			//System.out.printf("SGBaseTE.performTransientDamage: distance = %s, damage = %s\n",
					//dist, damage);
			ent.attackEntityFrom(transientDamage, damage);
		}
	}

//	void performPendingTeleportations() {
//		for (PendingTeleportation port : pendingTeleportations)
//			port.perform();
//		pendingTeleportations.clear();
//	}

	boolean undialledDigitsRemaining()
	{
		int n = numEngagedChevrons;
		return n < 7 && n < dialledAddress.length();
	}

	void startDiallingNextSymbol()
	{
		startDiallingSymbol(dialledAddress.charAt(numEngagedChevrons));
	}

	void startDiallingSymbol(char c)
	{
		int i = Character.getNumericValue(c) - Character.getNumericValue('A');
		if (i >= 0 && i < numRingSymbols)
		{
			startDiallingToAngle(i * ringSymbolAngle - 45 * numEngagedChevrons);
			playSoundEffect("sgextensions.sg_dial", 1.0F, 1.0F);
		}
	}

	void startDiallingToAngle(double a)
	{
		targetRingAngle = Utils.normaliseAngle(a);
		//sendClientEvent(SGEvent.StartDialling, (int)(targetRingAngle * 1000));
		if(quickDial == true)
		{
			enterState(SGState.Dialling, quickDiallingTime);
		}
		else
		{
			enterState(SGState.Dialling, diallingTime);
		}
	}

	void finishDiallingSymbol()
	{
		++numEngagedChevrons;
		//sendClientEvent(SGEvent.FinishDialling, numEngagedChevrons);
		if (numEngagedChevrons == SGAddressing.addressLength)
			finishDiallingAddress();
		else if (undialledDigitsRemaining())
			//startDiallingNextSymbol();
			if(quickDial == true)
			{
				enterState(SGState.InterDialling, quickInterDiallingTime);
			}
			else
			{
				enterState(SGState.InterDialling, interDiallingTime);
			}	
		else
			enterState(SGState.Idle, 0);
	}
	
	boolean shouldSafeDial()
	{
		if(this.solidIris())
			return true;
		ItemStack X = getStackInSlot(5);
		if(X != null)
		{
			if (((SGDarkMultiItem)(X.getItem())).isUpgradeType("Stargate Upgrade - Safe Dial",X))
			{
				return true;
			}
		}
		return false;
	}
	
	boolean shouldQuickDial()
	{
		for(int i=0;i<upgradeSlots;i++)
		{
			ItemStack X = getStackInSlot(4+i);
			if(X != null)
			{
				if (((SGDarkMultiItem)(X.getItem())).isUpgradeType("Stargate Upgrade - Fast Dial",X))
					return true;
			}
		}
		return false;
	}

	void finishDiallingAddress()
	{
		//System.out.printf("SGBaseTE: Connecting to '%s'\n", dialledAddress);
		if (!isInitiator || useFuel(modifiedFuelToOpen))
		{
			//sendClientEvent(SGEvent.Connect, 0);
			if(safeDial == true)
			{
				enterState(SGState.Connected, 20*60*SGExtensions.maxOpenTime);
				safeDial = shouldSafeDial();
				quickDial = shouldQuickDial();
			}
			else
			{
				safeDial = shouldSafeDial();
				quickDial = shouldQuickDial();
				enterState(SGState.Transient, transientDuration);
			}
			playSoundEffect("sgextensions.sg_open", 1.0F, 1.0F);
		}
		else
		{
			//enterState(SGState.Idle, 0);
			//playSoundEffect("gcewing.sg.sg_abort", 1.0F, 1.0F);
			disconnect();
		}
	}

//	void sendClientEvent(SGEvent type, int data) {
//		System.out.printf("SGBaseTE.sendClientEvent: %s, %d\n", type, data);
//		worldObj.addBlockEvent(xCoord, yCoord, zCoord, getBlockType().blockID, type.ordinal(), data);
//	}
	
	public boolean isEntitySafe(Entity entity)
	{
		//System.out.printf("SGSC: Checking for safety\n");
		if(safeEnts.size() >0)
		{
			//System.out.printf("SGSC: (%d)\n",safeEnts.size());
			Object[] SE = safeEnts.toArray();
			for(int i=0;i<safeEnts.size();i++)
			{
				//System.out.printf("SGSC: i (%d,%s,%s)\n",i,((Entity)(SE[i])).getPersistentID().toString(),entity.getPersistentID().toString());
				if(((Entity)(SE[i])).getPersistentID() == entity.getPersistentID())
				{
					return true;
				}
			}
		}
		return false;
	}
	
	public void sendIntegrationMessage(Entity e, boolean succ)
	{
		String n = e.getEntityName();
		if(succ)
			sendComputerEvent("reintegration","success",n);
		else
			sendComputerEvent("reintegration","fail",n);
		SGBaseTE dte = this.getConnectedStargateTE();
		if(succ && dte != null && this.isInitiator)
			dte.sendComputerEvent("reintegration","success",n);
	}

	public void entityInPortal(Entity entity)
	{
		if (state == SGState.Connected && (isInitiator || !SGExtensions.recieveHardMode) && irisState() != "Iris - Closed")
		{
			if(!isEntitySafe(entity))
			{
				//System.out.printf("SGBaseTE.entityInPortal: global (%.3f, %.3f, %.3f)\n",
				//	entity.posX, entity.posY, entity.posZ);
				Trans3 t = localToGlobalTransformation();
				//System.out.printf("SGBaseTE.entityInPortal: Transformation:\n");
				//t.dump();
				Vector3 p1 = t.ip(entity.posX, entity.posY, entity.posZ);
				Vector3 p0 = t.ip(entity.prevPosX, entity.prevPosY, entity.prevPosZ);
				//System.out.printf("SGBaseTE.entityInPortal: local (%.3f, %.3f, %.3f)\n", p1.x, p1.y, p1.z);
				//System.out.printf("SGBaseTE.entityInPortal: prev local position = %s\n", p0);
				//System.out.printf("SGBaseTE.entityInPortal: z0 = %.3f z1 = %.3f\n", p0.z, p1.z);
				if (p0.z >= 0.0 && p1.z < 0.0)
				{
					//System.out.printf("SGBaseTE.entityInPortal: Passed through event horizon of stargate at (%d,%d,%d) in %s\n",
							//xCoord, yCoord, zCoord, worldObj);
					SGBaseTE dte = getConnectedStargateTE();
					if (dte != null)
					{
						if(dte.irisState() != "Iris - Closed")
						{
							dte.addEntToSafety(entity);
							Trans3 dt = dte.localToGlobalTransformation();
							teleportEntity(entity, t, dt, connectedLocation.dimension);
							//addEntToSafety(entity);
							timeSinceLastTeleport = 30*20;
							sendIntegrationMessage(entity,true);
						}
						else if(entity instanceof EntityPlayerMP)
						{
							if(SGExtensions.irisKillClearInv)
							{
								if(!((EntityPlayer)entity).capabilities.isCreativeMode)
									((EntityPlayerMP)entity).inventory.clearInventory(-1, -1);
							}
							((EntityPlayerMP)entity).attackEntityFrom(irisDamage, 1000);
							sendIntegrationMessage(entity,false);
						}
						else
						{
							entity.setDead();
							sendIntegrationMessage(entity,false);
						}
					}
				}
			}
		}
		else if(state == SGState.Connected && isInitiator == false && irisState() != "Iris - Closed")
		{
			if(SGExtensions.recieveKill)
			{
				if(!isEntitySafe(entity))
				{
					if(SGExtensions.irisKillClearInv)
					{
						if(entity instanceof EntityPlayerMP)
						{
							if(!((EntityPlayer)entity).capabilities.isCreativeMode)
								((EntityPlayerMP)entity).inventory.clearInventory(-1, -1);
						}
					}
					if(entity instanceof EntityPlayerMP)
					{
						((EntityPlayerMP)entity).attackEntityFrom(recieveDamage, 1000);
					}
					else
					{
						entity.setDead();
					}
				}
				sendIntegrationMessage(entity,false);
			}
		}
	}

	void teleportEntity(Entity entity, Trans3 t1, Trans3 t2, int dimension)
	{
		//System.out.printf(
		//	"SGBaseTE.teleportEntity: old (%.3f, %.3f, %.3f) velocity (%.3f, %.3f, %.3f) yaw %.2f\n",
		//	entity.posX, entity.posY, entity.posZ, entity.motionX, entity.motionY, entity.motionZ,
		//	entity.rotationYaw);
		Vector3 p = t1.ip(entity.posX, entity.posY, entity.posZ); // local position
		Vector3 v = t1.iv(-entity.motionX, -entity.motionY, -entity.motionZ); // new local velocity
		Vector3 r = t1.iv(yawVector(entity)); // local facing
		Vector3 q = t2.p(p); // new global position
		Vector3 u = t2.v(v); // new global velocity
		Vector3 s = t2.v(r.mul(-1)); // new global facing
		double a = yawAngle(s); // new global yaw angle
		//System.out.printf(
		//	"SGBaseTE.teleportEntity: new (%.3f, %.3f, %.3f) velocity (%.3f, %.3f, %.3f) yaw %.2f\n",
		//	q.x, q.y, q.z, u.x, u.y, u.z, a);
		//pendingTeleportations.add(new PendingTeleportation(entity, q, u, a));
		if (entity.dimension == dimension)
			teleportWithinDimension(entity, q, u, a);
		else
			teleportToOtherDimension(entity, q, u, a, dimension);
	}

	void teleportWithinDimension(Entity entity, Vector3 p, Vector3 v, double a)
	{
		setVelocity(entity, v);
		if (entity instanceof EntityLiving)
		{
			entity.rotationYaw = (float) a;
			((EntityLiving) entity).setPositionAndUpdate(p.x, p.y, p.z);
		} else
			entity.setLocationAndAngles(p.x, p.y, p.z, (float) a, entity.rotationPitch);
	}
	
	public void addEntToSafety(Entity Ent)
	{
		if(isEntitySafe(Ent))
		{
			//System.out.printf("Entity is already safe\n");
			UUID EUID = Ent.getPersistentID();
			safeTime.put(EUID,safeTicks);
		}
		else
		{
			//System.out.printf("Entity is unsafe\n");
			safeEnts.add(Ent);
			UUID EUID = Ent.getPersistentID();
			safeTime.put(EUID,safeTicks);
		}
	}

	void teleportToOtherDimension(Entity entity, Vector3 p, Vector3 v, double a, int dimension)
	{
		if (entity instanceof EntityPlayerMP)
		{
			EntityPlayerMP player = (EntityPlayerMP) entity;
			Vector3 q = p.add(yawVector(a));
			transferPlayerToDimension(player, dimension, q, a);
			//player.rotationYaw = (float)a;
			//player.setVelocity(v.x, v.y, v.z);
			//player.setPositionAndUpdate(q.x, q.y, q.z);
		} else
			teleportEntityToDimension(entity, p, v, a, dimension);
	}

	//<<<

	static void transferPlayerToDimension(EntityPlayerMP player, int newDimension, Vector3 p, double a)
	{
		MinecraftServer server = MinecraftServer.getServer();
		ServerConfigurationManager scm = server.getConfigurationManager();
		int oldDimension = player.dimension;
		player.dimension = newDimension;
		WorldServer oldWorld = server.worldServerForDimension(oldDimension);
		WorldServer newWorld = server.worldServerForDimension(newDimension);
		player.playerNetServerHandler.sendPacketToPlayer(new Packet9Respawn(player.dimension,
				(byte) player.worldObj.difficultySetting, newWorld.getWorldInfo().getTerrainType(),
				newWorld.getHeight(), player.theItemInWorldManager.getGameType()));
		oldWorld.getEntityTracker().removeEntityFromAllTrackingPlayers(player);
		oldWorld.removeEntity(player);
		oldWorld.getEntityTracker().updateTrackedEntities();
		player.isDead = false;
		newWorld.spawnEntityInWorld(player);
		player.setLocationAndAngles(player.posX, player.posY, player.posZ, player.rotationYaw, player.rotationPitch);
		newWorld.updateEntityWithOptionalForce(player, false);
		player.setWorld(newWorld);
		scm.func_72375_a(player, oldWorld);
		player.playerNetServerHandler.setPlayerLocation(p.x, p.y, p.z, (float) a, player.rotationPitch);
		player.theItemInWorldManager.setWorld(newWorld);
		scm.updateTimeAndWeatherForPlayer(player, newWorld);
		scm.syncPlayerInventory(player);
		Iterator var6 = player.getActivePotionEffects().iterator();
		while (var6.hasNext())
		{
			PotionEffect effect = (PotionEffect) var6.next();
			player.playerNetServerHandler.sendPacketToPlayer(new Packet41EntityEffect(player.entityId, effect));
		}
		GameRegistry.onPlayerChangedDimension(player);
	}

	//>>>

	void teleportEntityToDimension(Entity oldEntity, Vector3 p, Vector3 v, double a, int dimension)
	{
		MinecraftServer server = MinecraftServer.getServer();
		WorldServer oldWorld = server.worldServerForDimension(oldEntity.dimension);
		WorldServer newWorld = server.worldServerForDimension(dimension);
		oldEntity.setDead();
		Entity newEntity = EntityList.createEntityByName(EntityList.getEntityString(oldEntity), newWorld);
		//System.out.printf("SGBaseTE.teleportEntityToDimension: newEntity = %s\n", newEntity);
		if (newEntity != null)
		{
			newEntity.copyDataFrom(oldEntity, true);
			setVelocity(newEntity, v);
			newEntity.setLocationAndAngles(p.x, p.y, p.z, (float) a, oldEntity.rotationPitch);
			checkChunk(newWorld, newEntity);
			newWorld.spawnEntityInWorld(newEntity);
		}
		oldWorld.resetUpdateEntityTick();
		newWorld.resetUpdateEntityTick();
	}

	static void setVelocity(Entity entity, Vector3 v)
	{
		entity.motionX = v.x;
		entity.motionY = v.y;
		entity.motionZ = v.z;
	}

	void checkChunk(World world, Entity entity)
	{
		int cx = MathHelper.floor_double(entity.posX / 16.0D);
		int cy = MathHelper.floor_double(entity.posZ / 16.0D);
		Chunk chunk = world.getChunkFromChunkCoords(cx, cy);
		//SGCraft.forceChunk(chunk);
		//System.out.printf("SGBaseTE.checkChunk: (%d, %d) chunk = %s\n", cx, cy, chunk);
	}

	Vector3 yawVector(Entity entity)
	{
		return yawVector(entity.rotationYaw);
	}

	Vector3 yawVector(double yaw)
	{
		double a = Math.toRadians(yaw);
		Vector3 v = new Vector3(-Math.sin(a), 0, Math.cos(a));
		//System.out.printf("SGBaseTE.yawVector: %.2f --> (%.3f, %.3f)\n", yaw, v.x, v.z);
		return v;
	}

	double yawAngle(Vector3 v)
	{
		double a = Math.atan2(-v.x, v.z);
		double d = Math.toDegrees(a);
		//System.out.printf("SGBaseTE.yawAngle: (%.3f, %.3f) --> %.2f\n", v.x, v.z, d);
		return d;
	}

	SGBaseTE getConnectedStargateTE()
	{
		if (connectedLocation != null)
			return connectedLocation.getStargateTE();
		else
			return null;
	}

	//------------------------------------   Client   --------------------------------------------

	@Override
	public void onDataPacket(INetworkManager net, Packet132TileEntityData pkt)
	{
		//System.out.printf("SGBaseTE.onDataPacket: with state %s numEngagedChevrons %s\n",
		//	SGState.valueOf(pkt.customParam1.getInteger("state")),
		//	pkt.customParam1.getInteger("numEngagedChevrons"));
		SGState oldState = state;
		super.onDataPacket(net, pkt);
		if (isMerged && state != oldState)
		{
			switch (state)
			{
				case Transient:
					initiateOpeningTransient();
					break;
				case Disconnecting:
					initiateClosingTransient();
					break;
				default:
					break;
			}
		}
	}


	void clientUpdate()
	{
		lastRingAngle = ringAngle;
		applyRandomImpulse();
		updateEventHorizon();
		switch (state)
		{
			case Dialling:
				//System.out.printf("SGBaseTe: Relaxing angle %s towards %s at rate %s\n",
				//	ringAngle, targetRingAngle, diallingRelaxationRate);
				//setRingAngle(Utils.relaxAngle(ringAngle, targetRingAngle, diallingRelaxationRate));
				updateRingAngle();
				//System.out.printf("SGBaseTe: Ring angle now %s\n", ringAngle);
				break;
			default:
				break;
		}
	}

	void setRingAngle(double a)
	{
//		lastRingAngle = ringAngle;
		ringAngle = a;
	}

	void updateRingAngle()
	{
		if (timeout > 0)
		{
			double da = Utils.diffAngle(ringAngle, targetRingAngle) / timeout;
			setRingAngle(Utils.addAngle(ringAngle, da));
			--timeout;
		} else
			setRingAngle(targetRingAngle);
	}

	public double[][][] getEventHorizonGrid()
	{
		if (ehGrid == null)
		{
			int m = SGBaseTERenderer.ehGridRadialSize;
			int n = SGBaseTERenderer.ehGridPolarSize;
			ehGrid = new double[2][n + 2][m + 1];
			for (int i = 0; i < 2; i++)
			{
				ehGrid[i][0] = ehGrid[i][n];
				ehGrid[i][n + 1] = ehGrid[i][1];
			}
		}
		return ehGrid;
	}

	void initiateOpeningTransient()
	{
		double v[][] = getEventHorizonGrid()[1];
		int n = SGBaseTERenderer.ehGridPolarSize;
		for (int j = 0; j <= n + 1; j++)
		{
			v[j][0] = openingTransientIntensity;
			v[j][1] = v[j][0] + openingTransientRandomness * random.nextGaussian();
		}
	}

	void initiateClosingTransient()
	{
		double v[][] = getEventHorizonGrid()[1];
		int m = SGBaseTERenderer.ehGridRadialSize;
		int n = SGBaseTERenderer.ehGridPolarSize;
		for (int i = 1; i < m; i++)
			for (int j = 1; j <= n; j++)
				v[j][i] += closingTransientRandomness * random.nextGaussian();
	}

	void applyRandomImpulse()
	{
		double v[][] = getEventHorizonGrid()[1];
		int m = SGBaseTERenderer.ehGridRadialSize;
		int n = SGBaseTERenderer.ehGridPolarSize;
		int i = random.nextInt(m - 1) + 1;
		int j = random.nextInt(n) + 1;
		v[j][i] += 0.05 * random.nextGaussian();
	}

	void updateEventHorizon()
	{
		double grid[][][] = getEventHorizonGrid();
		double u[][] = grid[0];
		double v[][] = grid[1];
		int m = SGBaseTERenderer.ehGridRadialSize;
		int n = SGBaseTERenderer.ehGridPolarSize;
		double dt = 1.0;
		double asq = 0.03;
		double d = 0.95;
		for (int i = 1; i < m; i++)
			for (int j = 1; j <= n; j++)
			{
				double du_dr = 0.5 * (u[j][i + 1] - u[j][i - 1]);
				double d2u_drsq = u[j][i + 1] - 2 * u[j][i] + u[j][i - 1];
				double d2u_dthsq = u[j + 1][i] - 2 * u[j][i] + u[j - 1][i];
				v[j][i] = d * v[j][i] + (asq * dt) * (d2u_drsq + du_dr / i + d2u_dthsq / (i * i));
			}
		for (int i = 1; i < m; i++)
			for (int j = 1; j <= n; j++)
				u[j][i] += v[j][i] * dt;
		double u0 = 0, v0 = 0;
		for (int j = 1; j <= n; j++)
		{
			u0 += u[j][1];
			v0 += v[j][1];
		}
		u0 /= n;
		v0 /= n;
		for (int j = 1; j <= n; j++)
		{
			u[j][0] = u0;
			v[j][0] = v0;
		}
		//dumpGrid("u", u);
		//dumpGrid("v", v);
	}

	void dumpGrid(String label, double g[][])
	{
		//System.out.printf("SGBaseTE: %s:\n", label);
		int m = SGBaseTERenderer.ehGridRadialSize;
		int n = SGBaseTERenderer.ehGridPolarSize;
		for (int j = 0; j <= n + 1; j++)
		{
			for (int i = 0; i <= m; i++)
				System.out.printf(" %6.3f", g[j][i]);
			System.out.printf("\n");
		}
	}

	@Override
	BaseTEChunkManager getChunkManager()
	{
		return SGExtensions.chunkManager;
	}

	@Override
	IInventory getInventory()
	{
		return inventory;
	}

////////////////////////////////////////////////
///////////////////COMPUTERCRAFT EVENTS/////////
////////////////////////////////////////////////

	public void sendComputerEvent(String event,String data, String data2)
	{
		if(isLinkedToController)
		{
			TileEntity te = worldObj.getBlockTileEntity(linkedX, linkedY, linkedZ);
			if(te != null)
			{
				if(te instanceof SGDarkDiallerTE)
				{
					((SGDarkDiallerTE) te).queueEv(event, data, data2);
				}
			}
		}
	}
	
////////////////////////////////////////////////
///////////////////RADIO AND GDO RELATED////////
////////////////////////////////////////////////
	public boolean sendRadioSignal(String signal)
	{
		System.out.printf("Radiostream: %s\n", signal);
		if(state == SGState.Connected)
		{
			SGBaseTE te = this.getConnectedStargateTE();
			if(te != null)
			{
				te.recieveRadioSignal(signal);
				return true;
			}
		}
		return false;
	}
	
	public String getRadioSignal()
	{
		if(state == SGState.Connected)
		{
			return radioMessage;
		}
		return null;
	}
	
	public void recieveRadioSignal(String signal)
	{
		radioMessage = signal;
		sendComputerEvent("radio",signal,null);
		markBlockForUpdate();
	}

}
////////////////////////////////////////////////
///////////////////TELEPORTERS AND DAMAGE/////
//////////////////////////////////////



class DummyTeleporter extends Teleporter
{

	public DummyTeleporter(WorldServer world)
	{
		super(world);
	}

	public void placeInPortal(Entity par1Entity, double par2, double par4, double par6, float par8)
	{
	}

	public boolean placeInExistingPortal(Entity par1Entity, double par2, double par4, double par6, float par8)
	{
		return true;
	}

}

//------------------------------------------------------------------------------------------------

class TransientDamageSource extends DamageSource
{

	public TransientDamageSource()
	{
		super("sgTransient");
	}

	public String getDeathMessage(EntityPlayer player)
	{
		return player.username + " was torn apart by an event horizon";
	}

}

class irisDamageSource extends DamageSource
{

	public irisDamageSource()
	{
		super("sgIris");
	}

	public String getDeathMessage(EntityPlayer player)
	{
		return player.username + " walked into an iris";
	}

}

class recieveDamageSource extends DamageSource
{

	public recieveDamageSource()
	{
		super("sgRecieve");
	}

	public String getDeathMessage(EntityPlayer player)
	{
		return player.username + " walked through a receiving stargate";
	}

}

//------------------------------------------------------------------------------------------------

//class PendingTeleportation {
//
//	Entity entity;
//	Vector3 v;
//	Vector3 p;
//	double a;
//	
//	public PendingTeleportation(Entity ent, Vector3 pos, Vector3 vel, double ang) {
//		entity = ent;
//		p = pos;
//		v = vel;
//		a = ang;
//	}
//
//	public void perform() {
//		System.out.printf("PendingTeleportation.perform: to pos (%.3f, %.3f, %.3f) vel (%.3f, %.3f, %.3f) yaw %.2f\n",
//			p.x, p.y, p.z, v.x, v.y, v.z, a);
//		entity.setVelocity(p.x, p.y, p.z);
//		if (entity instanceof EntityLiving) {
//			entity.rotationYaw = (float)a;
//			((EntityLiving)entity).setPositionAndUpdate(p.x, p.y, p.z);
//		}
//		else
//			entity.setLocationAndAngles(p.x, p.y, p.z, (float)a, entity.rotationPitch);
//	}
//	
//}

package sgextensions;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import dan200.computer.api.IComputerAccess;
import dan200.computer.api.IPeripheral;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.DimensionManager;

public class SGDarkDiallerTE extends BaseTileEntity implements IPeripheral
{
	public int x, y, z;
	public SGBaseTE ownedGate = null;
	public String gateAddress;
	
	public boolean isLinkedToStargate;
	public int linkedX, linkedY, linkedZ;

	public final String symbolChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	public final int numSymbols = symbolChars.length();
	public final int addressLength = 7;
	public final int numDimensionSymbols = 2;
	public final int numCoordSymbols = addressLength - numDimensionSymbols;
	public final int coordPower = (int) Math.pow(numSymbols, numCoordSymbols);
	public final int dimensionPower = (int) Math.pow(numSymbols, numDimensionSymbols);
	public final int maxCoord = ((int) Math.floor(Math.sqrt(coordPower - 1))) / 2;
	public final int minCoord = -maxCoord;
	public final int coordRange = maxCoord - minCoord + 1;
	public final int minDimension = -1;
	public final int maxDimension = minDimension + dimensionPower - 1;
	private final int range = 6;
	
	private HashMap<String,IComputerAccess> comps = new HashMap<String,IComputerAccess>();
	//Bits of the SGAddressing class required for the Dialling Computer to work its magic.

	@Override
	public String getType()
	{
		return "Dialling Computer";
	}

	@Override
	public String[] getMethodNames()
	{
		return new String[]{"info","dialGate","controlledDial", "disconnect", "hasGate", "thisAddress", "findAddress","gateInfo","closeIris","openIris","toggleIris","getRadio","sendRadio"};
	}	

	@Override
	public Object[] callMethod(IComputerAccess computer, int method, Object[] arguments) throws Exception
	{
		//System.out.printf("METHOD CALL!!!");
		switch (method)
		{
			case 0:
				return new Object[]{getInfo()};
			case 1:
				return new Object[]{DialGate(arguments[0].toString())};
			case 2:
				{
					String add = arguments[0].toString();
					int safe = (int) Double.parseDouble(arguments[1].toString());
					int quick = (int) Double.parseDouble(arguments[2].toString());
					return new Object[]{controlledDial(add,safe,quick)};
				}
			case 3:
				return new Object[]{disconnectGate()};
			case 4:
				return new Object[]{hasGate()};
			case 5:
				return new Object[]{getThisAddress()};
			case 6:
				return new Object[]{findAddressedStargate(arguments)};
			case 7:
				return new Object[]{getGateInfo()};
			case 8:
				return new Object[]{closeIris()};
			case 9:
				return new Object[]{openIris()};
			case 10:
				return new Object[]{toggleIris()};
			case 11:
				return new Object[]{getRadio()};
			case 12:
			{
				//System.out.println("SDFSCXC");
				return new Object[]{this.sendRadio(arguments)};
			}
			default:
				return new Object[0];
		}
	}
	
	public void queueEv(String e,String a,String b)
	{
		String[] list = {e,a,b};
		Object[] keys = comps.keySet().toArray();
		int Size = keys.length;
		for (int i=0;i<Size;i++)
		{
			IComputerAccess tV = comps.get(keys[i].toString());
			tV.queueEvent("Stargate",list);
		}
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		isLinkedToStargate = nbt.getBoolean("isLinkedToStargate");
		linkedX = nbt.getInteger("linkedX");
		linkedY = nbt.getInteger("linkedY");
		linkedZ = nbt.getInteger("linkedZ");
	}
	
	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		nbt.setBoolean("isLinkedToStargate",isLinkedToStargate);
		nbt.setInteger("linkedX", linkedX);
		nbt.setInteger("linkedY", linkedY);
		nbt.setInteger("linkedZ", linkedZ);
	}

	@Override
	public boolean canAttachToSide(int side)
	{
		return true;
	}

	@Override
	public void attach(IComputerAccess computer)
	{
		if(!comps.containsValue(computer))
		{
			comps.put("" + computer.hashCode(), computer); 
		}
	}

	@Override
	public void detach(IComputerAccess computer)
	{
		if(comps.containsValue(computer))
		{
			comps.remove("" + computer.hashCode());
		}
	}
	
	public SGBaseTE getLinkedGate()
	{
		TileEntity te = worldObj.getBlockTileEntity(linkedX,linkedY,linkedZ);
		if(te instanceof SGBaseTE)
		{
			return (SGBaseTE) te;
		}
		return null;
	}
	
	public String getRadio()
	{
		if(hasGate())
		{
			return ownedGate.getRadioSignal();
		}
		return "Error - No gate";
	}
	
	public String sendRadio(String out)
	{
		System.out.printf("SDFSCV:"+out+"\n");
		if(hasGate())
		{
			ownedGate.sendRadioSignal(out);
			return "Message sent";
		}
		return "Error - No gate";
	}
	
	public String sendRadio(Object[] args)
	{
		//System.out.printf("SDFSCV:"+args[1].toString()+"\n");
		if(hasGate())
		{
			if(args.length == 1)
			{
				Boolean ret = ownedGate.sendRadioSignal(args[0].toString());
				if(ret)
					return "Message sent";
				return "Message failed";
			}
		}
		return "Error - No gate";
	}
	
	public String disconnectGate()
	{
		if(hasGate())
		{
			return ownedGate.ControlledDisconnect();
		}
		return "Error - No gate";
	}
	
	public String openIris()
	{
		if(hasGate())
		{
			return ownedGate.openIris();
		}
		return "Error - No gate";
	}
	
	public String closeIris()
	{
		if(hasGate())
		{
			return ownedGate.closeIris();
		}
		return "Error - No gate";
	}
	
	public String toggleIris()
	{
		if(hasGate())
		{
			return ownedGate.toggleIris();
		}
		return "Error - No gate";
	}
	
	public String getThisAddress()
	{
		if(hasGate())
		{
			return gateAddress;
		}
		return "";
	}
	
	public Map getInfo()
	{
		Map retMap = new HashMap();
		retMap.put("info", "Returns this help array");
		retMap.put("dialGate", "dialGate(address) attempts to dial the address specified and returns info");
		retMap.put("controlledDial", "controlledDial(address,safe,quick) attempts to dial with/without safe and quick dialling");
		return retMap;
	}
	
	public Map getGateInfo()
	{
		Map retMap = new HashMap();
		if(hasGate())
		{
			SGState state = ownedGate.state;
			String outState = "Unknown";
			if(state == SGState.Connected) 		outState = "Connected";
			if(state == SGState.Dialling) 		outState = "Dialling";
			if(state == SGState.Disconnecting) 	outState = "Disconnecting";
			if(state == SGState.Idle) 			outState = "Idle";
			if(state == SGState.InterDialling) 	outState = "Interdialling";
			if(state == SGState.Transient) 		outState = "Kawoosh";
			retMap.put("address", getThisAddress());
			retMap.put("numEngagedChevs", ownedGate.numEngagedChevrons);
			retMap.put("state", outState);
			retMap.put("fuelBuffer", ownedGate.fuelBuffer);
			retMap.put("irisType", ownedGate.getIrisType());
			retMap.put("irisState", ownedGate.irisState());
			retMap.put("isInitiator", ownedGate.isInitiator && (state != SGState.Idle) && (state != SGState.Disconnecting));
		}
		else
		{
			retMap.put("0","Error - No Gate");
		}
		return retMap;
	}

	public String DialGate(String address)
	{
		if (!hasGate()) return "ERROR - No gate connected";
		return ownedGate.connectOrDisconnect(address,null);
	}
	
	public String controlledDial(String address, int safe, int quick)
	{
		if(hasGate())
		{
			boolean safeDial = false;
			boolean quickDial = false;
			if(safe == 1)
			{
				safeDial = true;
			}
			if(quick == 1)
			{
				quickDial = true;
			}
			return ownedGate.ControlledConnect(address, null,safeDial,quickDial);
		}
		else
		{
			return "Error - No gate connected";
		}
	}
	
	public double getTEDistance(TileEntity A, TileEntity B)
	{
		if(A != null && B != null)
		{
			Vector3 APos = new Vector3(A.xCoord,A.yCoord,A.zCoord);
			Vector3 BPos = new Vector3(B.xCoord,B.yCoord,B.zCoord);
			Vector3 InvalidPos = new Vector3(0,0,0);
			if(APos != InvalidPos)
			{
				if(BPos != InvalidPos)
				{
					return APos.distance(BPos);
				}
			}
		}
		return -1;
	}
	
	private boolean linkGate(SGBaseTE gate)
	{
		if(gate.isMerged)
		{
			double Distance = getTEDistance((TileEntity) gate,(TileEntity) this);
			if(Distance > 0 && Distance < 6)
			{
				if(gate.isLinked() == false)
				{
					ownedGate = gate;
					linkedX = ((SGBaseTE) gate).xCoord;
					linkedY = ((SGBaseTE) gate).yCoord;
					linkedZ = ((SGBaseTE) gate).zCoord;
					isLinkedToStargate = true;
					gate.linkedX = this.xCoord;
					gate.linkedY = this.yCoord;
					gate.linkedZ = this.zCoord;
					gate.isLinkedToController = true;
					gateAddress =  ownedGate.findHomeAddress();
					return true;
				}
				else
				{
					if(gate.linkedX == this.xCoord && gate.linkedY == this.yCoord && gate.linkedZ == this.zCoord)
					{
						linkedX = gate.xCoord;
						linkedY = gate.yCoord;
						linkedZ = gate.zCoord;
						isLinkedToStargate = true;
						ownedGate = gate;
						gateAddress =  ownedGate.findHomeAddress();
						return true;
					}
				}
			}
		}
		this.ownedGate = null;
		this.isLinkedToStargate = false;
		return false;
	}
	
	private boolean findGate()
	{
		x = this.xCoord;
		y = this.yCoord;
		z = this.zCoord;
		if(x==0 && y == 0 && z == 0)
		{
			return false;
		}
		else
		{
			//System.out.printf("My coords are real\n");
			TileEntity gate;
			if(isLinkedToStargate)
			{
				gate = worldObj.getBlockTileEntity(linkedX, linkedY, linkedZ);
				if(gate instanceof SGBaseTE)
				{
					return linkGate((SGBaseTE) gate);
				}
			}
			Trans3 t = localToGlobalTransformation();
			for (int i = -range; i <= range; i++)
				for (int j = -range; j <= range; j++)
					for (int k = -range; k <= range; k++)
					{
						Vector3 p = t.p(i, j, k);
						//System.out.printf("SGControllerTE: Looking for stargate at (%d,%d,%d)\n",
						//	p.floorX(), p.floorY(), p.floorZ());
						TileEntity te = worldObj.getBlockTileEntity(p.floorX(), p.floorY(), p.floorZ());
						if (te instanceof SGBaseTE)
						{
							//System.out.printf("SGControllerTE: Found stargate at (%d,%d,%d)\n",
							//	te.xCoord, te.yCoord, te.zCoord);
							return linkGate((SGBaseTE) te);
						}
					}
		}
		return false;
	}

	public void unlinkStargate()
	{
		if(isLinkedToStargate)
		{
			if(ownedGate != null)
			{
				ownedGate.clearLinkToController();
				ownedGate.markBlockForUpdate();
			}
			ownedGate = null;
			isLinkedToStargate = false;
		}
	}
	
	public boolean hasGate()
	{
		//System.out.printf("Checking for gate\n");
		if(ownedGate == null)
		{
			//System.out.printf("TEST - NO GATE FOUND\n");
			return findGate();
		}
		else
		{
			return true;
		}
		
	}

	public String findAddressedStargate(Object[] addressObj)
	{
		if(addressObj.length == 1)
		{
			String address = addressObj[1].toString();
			if(SGAddressing.addressLength == address.length())
			{
				address = address.toUpperCase();
				String csyms = address.substring(0, 5);
				String dsyms = address.substring(5, 5 + 2);
				int s = intFromSymbols(csyms);
				int chunkx = minCoord + s / coordRange;
				int chunkz = minCoord + s % coordRange;
				int dimension = minDimension + intFromSymbols(dsyms);
				World world = DimensionManager.getWorld(dimension);
				if (world != null)
				{
					return "The address leads to chunk coords X: " + chunkx + " Z: " + chunkz + " in dimension " + dimension + ".";
				} 
				else 
					return "The address is in a non-existent dimension " + dimension + ".";
			}
			else
				return "Invalid address";
		}
		else
			return "Missing address";
				
	}

	int intFromSymbols(String s)
	{
		int i = 0;
		int n = s.length();
		for (int j = n - 1; j >= 0; j--)
		{
			char c = s.charAt(j);
			i = i * numSymbols + charToSymbol(c);
		}
		return i;
	}
	
	

	int charToSymbol(char c)
	{
		return charToSymbol(String.valueOf(c));
	}

	int charToSymbol(String c)
	{
		return symbolChars.indexOf(c);
	}
}

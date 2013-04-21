package sgextensions;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

public class SGDarkECommand extends CommandBase
{

	@Override
	public String getCommandName() {
		return "stargate";
	}

	@Override
	public void processCommand(ICommandSender sen, String[] par)
	{
		if(sen.canCommandSenderUseCommand(3, "stargate"))
		{
			int size = par.length;
			if(size == 0)
			{
				sen.sendChatToPlayer("Stargate Commands:");
				sen.sendChatToPlayer("/stargate close <address> <force> - Closes gate at address");
				sen.sendChatToPlayer("/stargate to <address> - Takes you to the gate at address");
				sen.sendChatToPlayer("/stargate sfb <address> <amount> - Sets the fuel buffer at address to amount");
				sen.sendChatToPlayer("/stargate list - Lists all stargates");
				sen.sendChatToPlayer("/stargate dial <address> <address> <force> - Dials from the first gate to the second");
			}
			else
			{
				String sC = par[0].toLowerCase();
				if(sC.equals("close") && size > 1)
					closeGate(par,sen);
				if(sC.equals("to") &&size > 1)
					toGate(par[1],sen);
				if(sC.equals("list"))
					gateList(sen);
				if(sC.equals("dial"))
					dialGate(sen,par);
			}
		}
	}
	
	private void dialGate(ICommandSender com, String[] args)
	{
		if(args[1] != null && args[2] != null)
		{
			String from = args[1].toUpperCase();
			String to = args[2].toUpperCase();
			Boolean force = false;
			if(args.length >= 4)
				force = args[3].equals("true");
			SGBaseTE fromGate = SGAddressing.findAddressedStargate(from);
			if(fromGate != null)
			{
				String res = fromGate.connect(to, null, force);
				com.sendChatToPlayer(res);
			}
		}
	}
	
	private void closeGate(String[] par,ICommandSender sen)
	{
		if(par.length >= 1)
		{
			String address = par[0].toString();
			boolean forced = false;
			if(par.length >= 2)
				forced = par[1].toLowerCase().equals("true");
			SGBaseTE gate = SGAddressing.findAddressedStargate(address.toUpperCase());
			if(gate == null)
			{
				sen.sendChatToPlayer("No gate at address");
			}
			else
			{
				String E = gate.ControlledDisconnect(forced);
				if(E == "Error - Not initiator")
					E = E + " - Gate connected to: " + gate.dialledAddress;
				sen.sendChatToPlayer(E);
			}
		}
	}
	
	private void toGate(String address,ICommandSender sen)
	{
		SGBaseTE gate = SGAddressing.findAddressedStargate(address.toUpperCase());
		if(gate == null)
		{
			sen.sendChatToPlayer("No gate at address");
		}
		else
		{
			World[] Dims = DimensionManager.getWorlds();
			String Name = sen.getCommandSenderName();
			if(Name != null && !Name.toLowerCase().equals("rcon"))
			{
				EntityPlayer ep = null;
				for (int i=0;i<Dims.length&&ep==null;i++)
				{
					ep = Dims[i].getPlayerEntityByName(Name);
					if(ep != null)
					{
						Trans3 a = new Trans3(ep.posX,ep.posY,ep.posZ);
						Trans3 t = gate.localToGlobalTransformation();
						gate.teleportEntity(ep, a, t, gate.dimension());
					}
				}
			}
		}
	}
	
	private void gateList(ICommandSender sen)
	{
		System.out.printf("CMDTEST: listTest\n");
		sen.sendChatToPlayer("--STARGATE ADDRESSES--");
		String[] adds = SGExtensions.AddressStore.getAddresses();
		if(adds.length > 0)
		{
			for(int i =0;i<adds.length;i++)
			{
				sen.sendChatToPlayer(i + " : " + adds[i]);
			}
		}
		else
		{
			sen.sendChatToPlayer("No addresses found");
		}
	}
	
	
	
	@Override
	public int getRequiredPermissionLevel()
    {
        return 3;
    }

}

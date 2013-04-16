package sgextensions;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;

public class SGDarkECommand extends CommandBase
{

	@Override
	public String getCommandName() {
		return "stargate";
	}

	@Override
	public void processCommand(ICommandSender sen, String[] par)
	{
		System.out.println("CMDTEST");
		if(sen.canCommandSenderUseCommand(3, "stargate"))
		{
			int size = par.length;
			if(size == 0)
			{
				sen.sendChatToPlayer("Stargate Commands:");
				sen.sendChatToPlayer("/stargate close <address> - Closes gate at address");
				sen.sendChatToPlayer("/stargate fclose <address> - Force closes gate at address");
				sen.sendChatToPlayer("/stargate to <address> - Takes you to the gate at address");
				sen.sendChatToPlayer("/stargate sfb <address> <amount> - Sets the fuel buffer at address to amount");
				sen.sendChatToPlayer("/stargate list - Lists all stargates");
			}
			else
			{
				String sC = par[0].toLowerCase();
				System.out.printf("CMDTEST: %s\n",sC);
				if(sC.equals("close") && size > 1)
					closeGate(par[1],sen,false);
				if(sC.equals("fclose") && size > 1)
					closeGate(par[1],sen,true);
				if(sC.equals("to") &&size > 1)
					toGate(par[1],sen);
				if(sC.equals("list"))
					gateList(sen);
			}
		}
	}
	
	private void closeGate(String address,ICommandSender sen,boolean forced)
	{
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
	
	private void toGate(String address,ICommandSender sen)
	{
		SGBaseTE gate = SGAddressing.findAddressedStargate(address.toUpperCase());
		if(gate == null)
		{
			sen.sendChatToPlayer("No gate at address");
		}
		else
		{
			sen.sendChatToPlayer("WIP");
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

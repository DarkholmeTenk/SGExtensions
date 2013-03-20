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
		if(sen.canCommandSenderUseCommand(3, "stargate"))
		{
			int size = par.length;
			if(size == 0)
			{
				sen.sendChatToPlayer("Stargate Commands:");
				sen.sendChatToPlayer("/stargate close <address> - closes gate at address");
				sen.sendChatToPlayer("/stargate to <address> - Takes you to the gate at address");
				sen.sendChatToPlayer("/stargate sfb <address> <amount> - Sets the fuel buffer at address to amount");
				sen.sendChatToPlayer("/stargate list - Lists all stargates");
			}
			else
			{
				String sC = par[0].toLowerCase();
				if(sC == "close" && size > 1)closeGate(par[1],sen);
				if(sC == "to" &&size > 1)toGate(par[1],sen);
				if(sC == "list")gateList(sen);
			}
		}
	}
	
	private void closeGate(String address,ICommandSender sen)
	{
		SGBaseTE gate = SGAddressing.findAddressedStargate(address.toUpperCase());
		if(gate == null)
		{
			sen.sendChatToPlayer("No gate at address");
		}
		else
		{
			sen.sendChatToPlayer(gate.ControlledDisconnect());
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

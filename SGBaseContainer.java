//------------------------------------------------------------------------------------------------
//
//   SG Craft - Stargate base gui container
//
//------------------------------------------------------------------------------------------------

package sgextensions;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class SGBaseContainer extends BaseContainer
{

	static final int numUpdradeSlotColumns = 1;
	static final int upgradeSlotsX = 10;
	static final int upgradeSlotsY = 124;		
	static final int numFuelSlotColumns = 2;
	static final int fuelSlotsX = 174;
	static final int fuelSlotsY = 84;
	static final int playerSlotsX = 48;
	static final int playerSlotsY = 124;
	static final String[] upgradeList = {"Stargate Upgrade - Iris","Stargate Upgrade - Safe Dial","Stargate Upgrade - Fast Dial"};
	
	private int tx = 0;

	SGBaseTE te;
	
	public static void debugPrint(String mess)
	{
		System.out.println(mess);
	}
	
	public SGBaseContainer(EntityPlayer player, SGBaseTE te)
	{
		debugPrint("A000012");
		this.te = te;
		tx = 0;
		addFuelSlots();
		addUpgradeSlots();
		addPlayerSlots(player, playerSlotsX, playerSlotsY);
		debugPrint("A000013");
	}


	public static SGBaseContainer create(EntityPlayer player, World world, int x, int y, int z)
	{
		debugPrint("A000014");
		SGBaseTE te = SGBaseTE.at(world, x, y, z);
		if (te != null)
			return new SGBaseContainer(player, te);
		else
			return null;
	}
	
	void addFuelSlots()
	{
		debugPrint("A000015");
		int n = te.fuelSlots;
		//System.out.printf("SGBaseContainer: %s fuel slots\n", n);
		for (int i = 0; i < n; i++)
		{
			debugPrint("A000016");
			int row = i / numFuelSlotColumns;
			int col = i % numFuelSlotColumns;
			int x = fuelSlotsX + col * 18;
			int y = fuelSlotsY + row * 18;
			//System.out.printf("SGBaseContainer: adding fuel slot %s at (%s, %s)\n", i, x, y);
			Slot TSlot = new SGDarkUpgradeSlot(te, tx, x, y);
			((SGDarkUpgradeSlot)TSlot).setAllowedItemArray(new ItemStack[] {SGExtensions.stargateFuel});
			addSlotToContainer(TSlot);
			tx++;
		}
		debugPrint("A000017");
	}
	
	void addUpgradeSlots()
	{
		int n = te.upgradeSlots;
		ItemStack AU = ((SGDarkMultiItem) (SGExtensions.sgDarkUpgrades)).getUpgrade("Stargate Upgrade - Admin");
		for (int i = 0; i < n; i++)
		{
			int row = i / numUpdradeSlotColumns;
			int col = i % numUpdradeSlotColumns;
			int x = upgradeSlotsX + col * 18;
			int y = upgradeSlotsY + row * 18;
			Slot TSlot = new SGDarkUpgradeSlot(te, tx, x, y);
			ItemStack TI = ((SGDarkMultiItem) (SGExtensions.sgDarkUpgrades)).getUpgrade(upgradeList[i]);
			((SGDarkUpgradeSlot)TSlot).setAllowedItemArray(new ItemStack[] {TI,AU});
			addSlotToContainer(TSlot);
			tx++;
		}
	}

	@Override
	void sendStateTo(ICrafting crafter)
	{
		crafter.sendProgressBarUpdate(this, 0, te.fuelBuffer);
	}

	@Override
	public void updateProgressBar(int i, int value)
	{
		switch (1)
		{
			case 0:
				te.fuelBuffer = value;
				break;
		}
	}
	
	@Override
	public void detectAndSendChanges()
	{
		debugPrint("A000018");
		te.checkUpgrades();
		super.detectAndSendChanges();
		for (int i = 0; i < crafters.size(); i++)
		{
			ICrafting crafter = (ICrafting) crafters.get(i);
			sendStateTo(crafter);
		}
		debugPrint("A000019");
	}

}

//------------------------------------------------------------------------------------------------
//
//   SG Craft - Naquadah ore block
//
//------------------------------------------------------------------------------------------------

package sgextensions;

import net.minecraft.block.BlockOre;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraftforge.common.MinecraftForge;

import java.util.Random;

public class NaquadahOreBlock extends BlockOre
{


	public NaquadahOreBlock(int id)
	{
		super(id);
		setHardness(5.0F);
		setResistance(10.0F);
		setStepSound(soundStoneFootstep);
		MinecraftForge.setBlockHarvestLevel(this, "pickaxe", 3);
		setCreativeTab(CreativeTabs.tabBlock);
	}

	@Override
	public int idDropped(int par1, Random par2Random, int par3)
	{
		return ConfigHandler.itemNaquadahID;
	}
	
	public void registerIcons(IconRegister icon)
	{
		this.blockIcon = icon.registerIcon(SGExtensions.baseLocation + "naquadahOre");
	}

	@Override
	public int quantityDropped(Random random)
	{
		return 2 + random.nextInt(5);
	}

}

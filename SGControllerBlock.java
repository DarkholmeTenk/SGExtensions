//------------------------------------------------------------------------------------------------
//
//   SG Craft - Stargate Controller Block
//
//------------------------------------------------------------------------------------------------

package sgextensions;

import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;
import net.minecraft.world.World;

public class SGControllerBlock extends Base4WayBlock<SGControllerTE>
{
	
	Icon[] side = new Icon[3];

	public SGControllerBlock(int id)
	{
		super(id, Material.rock /*SGRingBlock.ringMaterial*/, SGControllerTE.class);
		setHardness(1.5F);
		setCreativeTab(SGExtensions.sgCreative);
	}
	
	@Override
	public void registerIcons(IconRegister IR)
	{
		side[0] = IR.registerIcon(SGExtensions.baseLocation + "controllerSide");
		side[1] = IR.registerIcon(SGExtensions.baseLocation + "controllerTop");
		side[2] = IR.registerIcon(SGExtensions.baseLocation + "controllerBottom");
	}

//	@Override
//	public void onBlockAdded(World world, int x, int y, int z) {
//		getTileEntity(world, x, y, z).checkForLink();
//	}

	@Override
	public boolean canHarvestBlock(EntityPlayer player, int meta)
	{
		return true;
	}

	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLiving player,ItemStack is)
	{
		super.onBlockPlacedBy(world, x, y, z, player, is);
		getTileEntity(world, x, y, z).checkForLink();
	}
	
	@Override
	public boolean canDragonDestroy(World w, int x,int y, int z)
	{
		return false;
	}

	@Override
	public void breakBlock(World world, int x, int y, int z, int id, int data)
	{
		SGControllerTE cte = getTileEntity(world, x, y, z);
		if (cte == null)
		{
			System.out.printf("SGControllerBlock.breakBlock: No tile entity at (%d,%d,%d)\n",
					x, y, z);
			return;
		}
		if (cte.isLinkedToStargate)
		{
			cte.clearLinkToStargate();
			SGBaseTE gte = cte.getLinkedStargateTE();
			if (gte != null)
				gte.clearLinkToController();
		}
	}
	
	@Override
	public Icon getBlockTextureFromLocalSideAndMetadata(int Iside, int data) {
		switch (Iside) {
			case 0: return side[2];
			case 1: return side[1];
			default: return side[0];
		}
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player,
	                                int side, float cx, float cy, float cz)
	{
		player.openGui(SGExtensions.instance, SGExtensions.GUIELEMENT_DHD, world, x, y, z);
		return true;
	}

}

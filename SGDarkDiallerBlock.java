package sgextensions;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLiving;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.World;

public class SGDarkDiallerBlock extends BaseBlock<SGDarkDiallerTE>
{
	Class<? extends TileEntity> TEClass = null;
	
	private Icon[] sides = new Icon[3];
	
	public SGDarkDiallerBlock(int i)
	{
		super(i, Material.rock, SGDarkDiallerTE.class);
		setHardness(1.5F);
		this.setCreativeTab(SGExtensions.sgCreative);
	}
	
	@Override
	public boolean canDragonDestroy(World w,int x,int y,int z)
	{
		return false;
	}
	
	@Override
	public Icon getIcon(int i,int j)
	{
		switch(i)
		{
			case 0:return sides[2];
			case 1:return sides[1];
			default:return sides[0];
		}
	}

	@Override
	public Icon getBlockTextureFromLocalSideAndMetadata(int i, int data)
	{
		return getIcon(i,data);
	}
	
	@Override
	public void registerIcons(IconRegister reg)
	{
		sides[0] = reg.registerIcon(SGExtensions.baseLocation + "darkDiallerSide");
		sides[1] = reg.registerIcon(SGExtensions.baseLocation + "darkDiallerTop");
		sides[2] = reg.registerIcon(SGExtensions.baseLocation + "darkDiallerBottom");
	}
	
	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLiving player,ItemStack IS)
	{
		super.onBlockPlacedBy(world, x, y, z, player, IS);
		getTileEntity(world, x, y, z).hasGate();
	}


	@Override
	public TileEntity createNewTileEntity(World var1)
	{
		return new SGDarkDiallerTE();
	}
	
	public void breakBlock(World world, int x, int y, int z, int id, int data)
	{
		TileEntity Dialler = world.getBlockTileEntity(x,y,z);
		if(Dialler instanceof SGDarkDiallerTE)
		{
			SGDarkDiallerTE Dial = (SGDarkDiallerTE) Dialler;
			Dial.unlinkStargate();
		}
	}
}

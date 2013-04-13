package sgextensions;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLiving;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class SGDarkDiallerBlock extends BaseBlock<SGDarkDiallerTE>
{
	Class<? extends TileEntity> TEClass = null;
	public SGDarkDiallerBlock(int i)
	{
		super(i, Material.rock, SGDarkDiallerTE.class);
		this.setCreativeTab(SGExtensions.sgCreative);
	}

	public String getTextureFile()
	{
		return "/sgextensions/resources/blocks.png";
	}

	@Override
	public int getBlockTextureFromSide(int i)
	{
		if(i == 0)
			return 2;
		else if(i == 1)
			return 1;
		return 0;
	}
	
	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLiving player)
	{
		super.onBlockPlacedBy(world, x, y, z, player);
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

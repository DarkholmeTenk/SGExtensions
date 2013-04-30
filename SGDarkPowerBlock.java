package sgextensions;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class SGDarkPowerBlock extends BlockContainer
{
	Icon icon;

	public SGDarkPowerBlock(int i, int j)
	{
		super(i, Material.rock);
		this.setCreativeTab(SGExtensions.sgCreative);
	}

	public String getTextureFile()
	{
		return "/sgextensions/resources/blocks.png";
	}
	
	@Override
	public TileEntity createNewTileEntity(World var1)
	{
		return new SGDarkPowerTE();
	}
	
	@Override
	public void registerIcons(IconRegister IR)
	{
		this.blockIcon = IR.registerIcon(SGExtensions.baseLocation + "darkPowerBlock");
	}
}

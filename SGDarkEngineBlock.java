package sgextensions;

import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;

public class SGDarkEngineBlock extends BaseBlock<SGDarkEngineTE>
{
	public SGDarkEngineBlock(int id) 
	{
		super(id, Material.iron, SGDarkEngineTE.class);
	}
	
	@Override
	public boolean isOpaqueCube() {
		return false;
	}
	
	@Override
	public boolean renderAsNormalBlock() {
		return false;
	}
	
	@Override
	public boolean canHarvestBlock(EntityPlayer player, int meta)
	{
		return true;
	}
}

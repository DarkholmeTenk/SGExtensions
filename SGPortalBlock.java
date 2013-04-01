//------------------------------------------------------------------------------------------------
//
//   SG Craft - Stargate portal block
//
//------------------------------------------------------------------------------------------------

package sgextensions;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.event.world.WorldEvent;

import java.util.Random;

public class SGPortalBlock extends Block
{
	
	public SGPortalBlock(int id)
	{
		super(id, Material.rock);
		this.setBlockUnbreakable();
		setBlockBounds(0, 0, 0, 0, 0, 0);
	}

	@Override
	public int getRenderType()
	{
		return -1;
	}

	@Override
	public boolean isOpaqueCube()
	{
		return false;
	}

	@Override
	public boolean renderAsNormalBlock()
	{
		return false;
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World A, int X, int Y, int Z)
	{
		if(isIris(A,X,Y,Z))
			return AxisAlignedBB.getAABBPool().addOrModifyAABBInPool((double)X, (double)Y, (double)Z, (double)X + 1, (double)Y + 1, (double)Z + 1);
		return null;
	}
	
	@Override
	public int quantityDropped(Random par1Random)
	{
		return 0;
	}
	
	@Override
	public void updateTick(World A,int X,int Y, int Z, Random Huh)
	{
		
	}
	
	@Override
	public int tickRate()
	{
		return 3;
	}

	@Override
	public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity)
	{
		if (!world.isRemote)
		{
			//System.out.printf("SGPortalBlock.onEntityCollidedWithBlock (%d,%d,%d) in %s\n",
			//	x, y, z, world);
			SGBaseTE te = getStargateTE(world, x, y, z);
			if (te != null)
				te.entityInPortal(entity);
		}
	}
	
	private boolean isIris(World world, int x,int y,int z)
	{
		int Meta = world.getBlockMetadata(x,y,z);
		return isIris(Meta);
	}
	
	private boolean isIris(int Meta)
	{
		if(Meta == 1)
			return true;
		else
			return false;
	}
	
	@Override
	public boolean getBlocksMovement(IBlockAccess par1IBlockAccess, int par2, int par3, int par4)
	{
		if(isIris(par1IBlockAccess.getBlockMetadata(par2, par3, par4)))
			return false;
		return true;
	}
	
	@Override
	public void onNeighborBlockChange(World world,int x,int y, int z, int blockID)
	{
		if(!world.isRemote)
		{
			SGBaseTE SG = getStargateTE(world,x,y,z);
			int meta = world.getBlockMetadata(x, y, z);
			if(SG != null)
			{
				if(SG.solidIris())
				{
					if(meta != 1)
					{
						world.setBlockMetadataWithNotify(x, y, z, 1);
						world.markBlockForUpdate(x,y,z);
					}
				}
				else
				{
					if(meta == 1)
					{
						world.setBlockMetadataWithNotify(x, y, z, 0);
						world.markBlockForUpdate(x,y,z);
					}
				}
			}
			else
			{
				if(meta == 1)
				{
					world.setBlockMetadataWithNotify(x, y, z, 0);
					world.markBlockForUpdate(x,y,z);
				}
			}
		}
	}

	SGBaseTE getStargateTE(World world, int x, int y, int z)
	{
		for (int i = -1; i <= 1; i++)
			for (int j = -3; j <= -1; j++)
				for (int k = -1; k <= 1; k++)
				{
					TileEntity te = world.getBlockTileEntity(x + i, y + j, z + k);
					if (te instanceof SGBaseTE)
						return (SGBaseTE) te;
				}
		return null;
	}

}

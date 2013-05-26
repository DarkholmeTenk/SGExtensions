//------------------------------------------------------------------------------------------------
//
//   SG Craft - Stargate base block
//
//------------------------------------------------------------------------------------------------

package sgextensions;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Icon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class SGBaseBlock extends Base4WayBlock<SGBaseTE>
{

	static boolean debugMerge = false;

	Icon topAndBottomTexture;
	Icon frontTexture;
	Icon sideTexture;
	static final int mergedBit = 0x8;

	static int southSide[] = {3, 5, 2, 4};
	static int unitX[] = {1, 0, -1, 0};
	static int unitZ[] = {0, -1, 0, 1};

	static int pattern[][] = {
			{2, 1, 2, 1, 2},
			{1, 0, 0, 0, 1},
			{2, 0, 0, 0, 2},
			{1, 0, 0, 0, 1},
			{2, 1, 0, 1, 2},
	};

	public SGBaseBlock(int id)
	{
		super(id, Material.rock /*SGRingBlock.ringMaterial*/, SGBaseTE.class);
		setHardness(1.5F);
		setCreativeTab(SGExtensions.sgCreative);
	}

	@Override
	public boolean isOpaqueCube()
	{
		return false;
	}
	
	@Override
	public boolean canDragonDestroy(World w, int x,int y, int z)
	{
		return false;
	}

	@Override
	public boolean canHarvestBlock(EntityPlayer player, int meta)
	{
		return true;
	}

	public boolean isMerged(IBlockAccess world, int x, int y, int z)
	{
		SGBaseTE te = getTileEntity(world, x, y, z);
		return te != null && te.isMerged;
	}

	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLiving player,ItemStack st)
	{
		int data = Math.round((180 - player.rotationYaw) / 90) & 3;
		//System.out.printf("SGBaseBlock.onBlockPlacedBy: yaw = %.1f data = %d\n", player.rotationYaw, data);
		world.setBlockMetadataWithNotify(x, y, z, data, 3);
		if (!world.isRemote)
			checkForMerge(world, x, y, z);
	}
	
	public void debugPrint(String mess)
	{
		System.out.println(mess);
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player,
	                                int side, float cx, float cy, float cz)
	{
		String Side = world.isRemote ? "Client" : "Server";
		SGBaseTE te = getTileEntity(world, x, y, z);
		te.markBlockForUpdate();
		//System.out.printf("SGBaseBlock.onBlockActivated: %s: Tile entity = %s\n", Side, te);
		if (te != null)
		{
			if (debugMerge)
				//System.out.printf("SGBaseBlock.onBlockActivated: %s: isMerged = %s\n", Side, te.isMerged);
			if (!world.isRemote)
				te.dumpChunkLoadingState("SGBaseBlock.onBlockActivated");
			if (te.isMerged)
			{
				debugPrint("A000001");
                player.openGui(SGExtensions.instance,SGExtensions.GUIELEMENT_GATE,world,x,y,z);
                debugPrint("A000002");
				return true;
			}
		}
		return false;
	}
	
	
	@Override
	public void registerIcons(IconRegister iR)
	{
		topAndBottomTexture = iR.registerIcon(SGExtensions.baseLocation + "ringTB");
		frontTexture = iR.registerIcon(SGExtensions.baseLocation + "baseFront");
		sideTexture = iR.registerIcon(SGExtensions.baseLocation + "ringTB");
	}

	@Override
	public Icon getBlockTextureFromLocalSideAndMetadata(int side, int data)
	{
		if (side <= 1)
			return topAndBottomTexture;
			//else if (side == southSide[data & rotationMask])
		else if (side == 3) // south
			return frontTexture;
		else
			return sideTexture;
	}

    //int getRotation(World world, int x, int y, int z) {s
	//    return world.getBlockMetadata(x, y, z) & rotationMask;
    //}

	void checkForMerge(World world, int x, int y, int z)
	{
		if (debugMerge)
			System.out.printf("SGBaseBlock.checkForMerge at (%d,%d,%d)\n", x, y, z);
		if (!isMerged(world, x, y, z))
		{
			int rot = getRotation(world, x, y, z);
			int dx = unitX[rot];
			int dz = unitZ[rot];
			if (debugMerge)
				System.out.printf("SGBaseBlock: rot = %d, dx = %d, dz = %d\n", rot, dx, dz);
			for (int i = -2; i <= 2; i++)
				for (int j = 0; j <= 4; j++)
					if (!(i == 0 && j == 0))
					{
						int xr = x + i * dx;
						int yr = y + j;
						int zr = z + i * dz;
						int type = getRingBlockType(world, xr, yr, zr);
						if (debugMerge)
						{
							System.out.printf("SGBaseBlock: type %d at (%d,%d,%d)\n", type, xr, yr, zr);
							System.out.printf("SGBaseBlock: pattern = %d\n", pattern[j][2 + i]);
						}
						if (type != pattern[4 - j][2 + i])
							return;
					}
			if (debugMerge)
				System.out.printf("SGBaseBlock: Merging\n");
			SGBaseTE te = getTileEntity(world, x, y, z);
			te.isMerged = true;
			world.markBlockForUpdate(x, y, z);
			for (int i = -2; i <= 2; i++)
				for (int j = 0; j <= 4; j++)
					if (!(i == 0 && j == 0))
					{
						int xr = x + i * dx;
						int yr = y + j;
						int zr = z + i * dz;
						int id = world.getBlockId(xr, yr, zr);
						Block block = Block.blocksList[id];
						if (block instanceof SGRingBlock)
							((SGRingBlock) block).mergeWith(world, xr, yr, zr, x, y, z);
						else
							world.setBlock(xr, yr, zr, ConfigHandler.blockSGPortalID, 0,3);
					}
			te.checkForLink();
		}
	}

	int getRingBlockType(World world, int xr, int yr, int zr)
	{
		int id = world.getBlockId(xr, yr, zr);
		if (id == 0)
			return 0;
		if (id == ConfigHandler.blockSGRingID)
			if (!((SGRingBlock)SGExtensions.sgRingBlock).isMerged(world, xr, yr, zr))
			{
				int data = world.getBlockMetadata(xr, yr, zr);
				switch (data & 1)
				{
					case 0:
						return 1;
					case 1:
						return 2;
				}
			}
		return -1;
	}

	@Override
	public void breakBlock(World world, int x, int y, int z, int id, int data)
	{
		SGBaseTE te = getTileEntity(world, x, y, z);
		unmerge(world, x, y, z);
		super.breakBlock(world, x, y, z, id, data);
	}

	public void unmerge(World world, int x, int y, int z)
	{
		SGBaseTE te = getTileEntity(world, x, y, z);
		Boolean goBang = false;
		if (te != null /*&& te.isMerged*/)
		{
			if (te.isMerged && te.state == SGState.Connected)
			{
				te.state = SGState.Idle;
				goBang = true && SGExtensions.explosionsEnabled;
			}
			te.disconnect();
			te.unlinkFromController();
			SGExtensions.AddressStore.removeAddress(te.findHomeAddress());
			te.isMerged = false;
			world.markBlockForUpdate(x, y, z);
			unmergeRing(world, x, y, z);
		}
		if (goBang)
		{
			explode(world, x + 0.5, y + 2.5, z + 0.5, 10);
			//world.newExplosion(null, x + 0.5, y + 2.5, z + 0.5, 10, true, true);
			//SGExplosion exp = new SGExplosion(world, null, x + 0.5, y + 2.5, z + 0.5, 10);
			//exp.isFlaming = true;
			//exp.isSmoking = true;
			//exp.doExplosionA();
			//exp.doExplosionB(true);
		}
	}

	static DamageSource explodingStargateDamage = new ExplodingStargateDamage();

	void explode(World world, double x, double y, double z, double s)
	{
		world.newExplosion(null, x, y, z, (float)s, true, true);
	}

	void unmergeRing(World world, int x, int y, int z)
	{
		for (int i = -2; i <= 2; i++)
			for (int j = 0; j <= 4; j++)
				for (int k = -2; k <= 2; k++)
					unmergeRingBlock(world, x, y, z, x + i, y + j, z + k);
	}

	void unmergeRingBlock(World world, int x, int y, int z, int xr, int yr, int zr)
	{
		//System.out.printf("SGBaseBlock.unmergeRingBlock at (%d,%d,%d)\n", xr, yr, zr);
		Block block = Block.blocksList[world.getBlockId(xr, yr, zr)];
		if (debugMerge)
			System.out.printf("SGBaseBlock.unmergeRingBlock: found %s at (%d,%d,%d)\n",
					block, xr, yr, zr);
		if (block instanceof SGRingBlock)
		{
			//System.out.printf("SGBaseBlock: unmerging ring block\n");
			((SGRingBlock) block).unmergeFrom(world, xr, yr, zr, x, y, z);
		} else if (block instanceof SGPortalBlock)
			world.setBlock(xr, yr, zr, 0,0,3);
	}

}

//------------------------------------------------------------------------------------------------

class ExplodingStargateDamage extends DamageSource
{

	public ExplodingStargateDamage()
	{
		super("sgExplosion");
	}

	public String getDeathMessage(EntityPlayer player)
	{
		return player.username + " messed with forces beyond their comprehension";
	}

}

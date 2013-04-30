//------------------------------------------------------------------------------------------------
//
//   SG Craft - Stargate ring block
//
//------------------------------------------------------------------------------------------------

package sgextensions;

import cpw.mods.fml.common.registry.LanguageRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.List;

public class SGRingBlock extends BaseBlock<SGRingTE>
{

	static Icon ringText;
	static Icon chevText;
	static Icon[] sideTextures;
	static Icon topAndBottomTexture;
	static int numSubBlocks = 2;
	static int subBlockMask = 0x1;

	public static Material ringMaterial = new Material(MapColor.stoneColor);

	static String[] subBlockTitles = {
			"Stargate Ring Block",
			"Stargate Chevron Block",
	};

	public SGRingBlock(int id)
	{
		super(id, Material.rock /*ringMaterial*/, SGRingTE.class);
		setHardness(1.5F);
		setCreativeTab(SGExtensions.sgCreative);
		registerSubItemNames();
	}
	
	@Override
	public boolean canDragonDestroy(World w, int x,int y, int z)
	{
		return false;
	}

	@Override
	public boolean isOpaqueCube()
	{
		return false;
	}

	@Override
	public boolean canHarvestBlock(EntityPlayer player, int meta)
	{
		return true;
	}

	@Override
	public int damageDropped(int data)
	{
		return data;
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player,
	                                int side, float cx, float cy, float cz)
	{
//		System.out.printf("SGRingBlock.onBlockActivated at (%d, %d, %d)\n", x, y, z);
		SGRingTE te = getTileEntity(world, x, y, z);
		if (te.isMerged)
		{
//			System.out.printf("SGRingBlock.onBlockActivated: base at (%d, %d, %d)\n",
//					te.baseX, te.baseY, te.baseZ);
			Block block = Block.blocksList[world.getBlockId(te.baseX, te.baseY, te.baseZ)];
			if (block instanceof SGBaseBlock)
				block.onBlockActivated(world, te.baseX, te.baseY, te.baseZ, player,
						side, cx, cy, cz);
			return true;
		}
		return false;
	}

	@Override
	public Icon getBlockTextureFromLocalSideAndMetadata(int side, int data)
	{
		if (side <= 1)
			return topAndBottomTexture;
		return sideTextures[data & subBlockMask];
	}
	
	@Override
	public void registerIcons(IconRegister iR)
	{
		topAndBottomTexture = iR.registerIcon(SGExtensions.baseLocation + "ringTB");
		ringText = iR.registerIcon(SGExtensions.baseLocation + "ringS");
		chevText = iR.registerIcon(SGExtensions.baseLocation + "chevS");
		sideTextures  = new Icon[2];
		sideTextures[0] = ringText;
		sideTextures[1] = chevText;
	}

	@Override
	public void getSubBlocks(int itemID, CreativeTabs tab, List list)
	{
		for (int i = 0; i < numSubBlocks; i++)
			list.add(new ItemStack(itemID, 1, i));
	}

	void registerSubItemNames()
	{
		LanguageRegistry registry = LanguageRegistry.instance();
		for (int i = 0; i < SGRingBlock.numSubBlocks; i++)
		{
			String name = SGRingItem.subItemName(i) + ".name";
			String title = subBlockTitles[i];
			//System.out.printf("SGRingBlock.registerSubItemNames: %s --> %s\n", name, title);
			registry.addStringLocalization(name, "en_US", title);
		}
	}

	public boolean isMerged(IBlockAccess world, int x, int y, int z)
	{
		SGRingTE te = getTileEntity(world, x, y, z);
		return te.isMerged;
	}

	public void mergeWith(World world, int x, int y, int z, int xb, int yb, int zb)
	{
		SGRingTE te = getTileEntity(world, x, y, z);
		te.isMerged = true;
		te.baseX = xb;
		te.baseY = yb;
		te.baseZ = zb;
		//te.onInventoryChanged();
		world.markBlockForUpdate(x, y, z);
	}

	public void unmergeFrom(World world, int x, int y, int z, int xb, int yb, int zb)
	{
		SGRingTE te = getTileEntity(world, x, y, z);
		if (te.isMerged && te.baseX == xb && te.baseY == yb && te.baseZ == zb)
		{
			//System.out.printf("SGRingBlock.unmergeFrom: unmerging\n");
			te.isMerged = false;
			//te.onInventoryChanged();
			world.markBlockForUpdate(x, y, z);
		}
	}

	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLiving player,ItemStack IS)
	{
		SGRingTE te = getTileEntity(world, x, y, z);
		updateBaseBlocks(world, x, y, z, te);
	}

	@Override
	public void breakBlock(World world, int x, int y, int z, int id, int data)
	{
		SGRingTE te = getTileEntity(world, x, y, z);
		super.breakBlock(world, x, y, z, id, data);
		if (te.isMerged)
		{
			updateBaseBlocks(world, x, y, z, te);
		}
	}
	
	public void updateRingBlocks(SGBaseTE base,World w,int X,int Y,int Z)
	{
		if(base != null)
		{
			if(base.isAdminGate)
			{
				if(this.getBlockHardness(w, X, Y, Z) == 1.5F)
				{
					this.setBlockUnbreakable();
					w.markBlockForUpdate(X, Y, Z);
				}
			}
			else
			{
				if(this.getBlockHardness(w, X, Y, Z) != 1.5F)
				{
					this.setHardness(1.5F);
					w.markBlockForUpdate(X, Y, Z);
				}
			}
		}
	}
	
	@Override
	public void onNeighborBlockChange(World world,int x,int y, int z, int blockID)
	{
		updateRingBlocks(getBase(world,x,y,z),world,x,y,z);
	}
	
	public SGBaseTE getBase(World world, int x, int y, int z)
	{
//		te.isMerged, te.baseX, te.baseY, te.baseZ);
		for (int i = -2; i <= 2; i++)
			for (int j = -4; j <= 0; j++)
				for (int k = -2; k <= 2; k++)
				{
					int xb = x + i;
					int yb = y + j;
					int zb = z + k;
					TileEntity te = world.getBlockTileEntity(x, y, z);
					if (te instanceof SGBaseTE)
					{
						return (SGBaseTE)te;
					}
				}
		return null;
	}

	void updateBaseBlocks(World world, int x, int y, int z, SGRingTE te)
	{
		//System.out.printf("SGRingBlock.updateBaseBlocks: merged = %s, base = (%d,%d,%d)\n",
		//	te.isMerged, te.baseX, te.baseY, te.baseZ);
		for (int i = -2; i <= 2; i++)
			for (int j = -4; j <= 0; j++)
				for (int k = -2; k <= 2; k++)
				{
					int xb = x + i;
					int yb = y + j;
					int zb = z + k;
					Block block = Block.blocksList[world.getBlockId(xb, yb, zb)];
					if (block instanceof SGBaseBlock)
					{
						//System.out.printf("SGRingBlock.updateBaseBlocks: found base at (%d,%d,%d)\n",
						//	xb, yb, zb);
						SGBaseBlock base = (SGBaseBlock) block;
						if (!te.isMerged)
							base.checkForMerge(world, xb, yb, zb);
						else if (te.baseX == xb && te.baseY == yb && te.baseZ == zb)
							base.unmerge(world, xb, yb, zb);
					}
				}
	}

}

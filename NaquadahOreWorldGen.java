//------------------------------------------------------------------------------------------------
//
//   SG Craft - Naquadah ore world generation
//
//------------------------------------------------------------------------------------------------

package sgextensions;

import cpw.mods.fml.common.IWorldGenerator;
import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;

import java.util.Random;

//import net.minecraft.world.gen.feature.*;

public class NaquadahOreWorldGen implements IWorldGenerator
{

	Random random;
	World world;
	Chunk chunk;
	int x0, z0;

	int stone = Block.stone.blockID;
	int lava = Block.lavaStill.blockID;
	int naquadah = ConfigHandler.blockOreNaquadahID;

	public void generate(Random random, int chunkX, int chunkZ, World world,
	                     IChunkProvider chunkGenerator, IChunkProvider chunkProvider)
	{
		if(SGExtensions.addOres)
		{
			//System.out.printf("NaquadahOreWorldGen: chunk (%d, %d)\n", chunkX, chunkZ);
			this.random = random;
			this.world = world;
			x0 = chunkX * 16;
			z0 = chunkZ * 16;
			chunk = world.getChunkFromChunkCoords(chunkX, chunkZ);
			generateChunk();
		}
	}

	public void regenerate(Chunk chunk)
	{
		if(SGExtensions.addOres)
		{
			this.chunk = chunk;
			world = chunk.worldObj;
			int chunkX = chunk.xPosition;
			int chunkZ = chunk.zPosition;
			long worldSeed = world.getSeed();
			random = new Random(worldSeed);
			long xSeed = random.nextLong() >> 2 + 1L;
			long zSeed = random.nextLong() >> 2 + 1L;
			random.setSeed((xSeed * chunkX + zSeed * chunkZ) ^ worldSeed);
			x0 = chunkX * 16;
			z0 = chunkZ * 16;
			generateChunk();
		}
	}

	int getBlock(int x, int y, int z)
	{
		//return world.getBlockId(x0 + x, y, z0 + z);
		return chunk.getBlockID(x, y, z);
	}

	void setBlock(int x, int y, int z, int id)
	{
		//world.setBlock(x0 + x, y, z0 + z, id);
		chunk.setBlockIDWithMetadata(x, y, z, id, 2);
	}

	void generateNode(int id, int x, int y, int z, int sx, int sy, int sz)
	{
		if(SGExtensions.addOres)
		{
			int dx = random.nextInt(sx);
			int dy = random.nextInt(sy);
			int dz = random.nextInt(sz);
			int h = world.getHeight();
			//System.out.printf("generateNode: %d x %d x %d at (%d, %d, %d)\n",
			//	dx, dy, dz, x, y, z);
			for (int i = x; i <= x + dx; i++)
				for (int j = y; j <= y + dy; j++)
					for (int k = z; k <= z + dz; k++)
						if (i < 16 && j < h && k < 16)
							if (getBlock(i, j, k) == stone) {
								//System.out.printf("generateNode: putting at (%d, %d, %d)\n", i, j, k);
								setBlock(i, j, k, id);
							}
		}
	}

	boolean odds(int n)
	{
		return random.nextInt(n) == 0;
	}

	void generateChunk()
	{
		if(SGExtensions.addOres)
		{
			int n = random.nextInt(9);
			for (int i = 0; i < n; i++) {
				int x = random.nextInt(16);
				int z = random.nextInt(16);
				for (int y = 0; y < 64; y++)
					if (getBlock(x, y, z) == stone && getBlock(x, y+1, z) == lava) {
						generateNode(naquadah, x, y, z, 3, 1, 3);
				}
			}
			n = random.nextInt(6);
			for (int i = 0; i < n; i++) {
				int x = random.nextInt(16);
				int y = random.nextInt(64);
				int z = random.nextInt(16);
				if (getBlock(x, y, z) == stone) {
					generateNode(naquadah, x, y, z, 2, 2, 2);
				}
			}
			SGChunkData.forChunk(chunk).oresGenerated = true;
		}
	}

}

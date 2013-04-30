//------------------------------------------------------------------------------------------------
//
//   SG Craft - Naquadah alloy block
//
//------------------------------------------------------------------------------------------------

package sgextensions;

import net.minecraft.block.BlockOreStorage;
import net.minecraft.client.renderer.texture.IconRegister;

public class NaquadahBlock extends BlockOreStorage
{


	public NaquadahBlock(int id)
	{
		super(id);
		setHardness(5.0F);
		setResistance(10.0F);
		setStepSound(soundMetalFootstep);
	}
	
	public void registerIcons(IconRegister icon)
	{
		this.blockIcon = icon.registerIcon(SGExtensions.baseLocation + "naquadahBlock");
	}

}

package sgextensions;

import java.util.List;

import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;

public class SGDarkMultiItem extends Item
{
	private String[] subNames;
	private String[] subIcons;
	private String[] subInfo;
	private Icon[] subIconItems;
	
	public void setSubNames(String[] names)
	{
		subNames = names;
	}
	
	public void setSubIcons(String[] icons)
	{
		subIcons = icons;
	}
	
	public void setSubInfo(String[] info)
	{
		subInfo = info;
	}
	
	public boolean isUpgradeType(String Name, int Damage)
	{
		if(subNames[Damage] == Name)
		{
			return true;
		}
		return false;
	}
	
	public boolean isUpgradeType(String Name, ItemStack IS)
	{
		if(IS.getItem() instanceof SGDarkMultiItem)
		{
			int damage = IS.getItemDamage();
			if(subNames[damage] == Name)
			{
				return true;
			}
		}
		return false;
	}
	
	public ItemStack getUpgrade(String Name)
	{
		ItemStack retIS = new ItemStack(this);
		for(int i = 0;i<subNames.length;i++)
		{
			if(Name == subNames[i])
			{
				retIS.setItemDamage(i);
			}
		}
		return retIS;
	}
	
	public SGDarkMultiItem(int par1,String[] sNames,String[] iconList,String[] info,int Stack)
	{
		super(par1);
		setHasSubtypes(true);
		setCreativeTab(SGExtensions.sgCreative);
		this.setSubNames(sNames);
		this.setSubIcons(iconList);
		this.setSubInfo(info);
		this.setMaxStackSize(Stack);
	}
	
	@Override
	public Icon getIconFromDamage(int meta)
	{
		return subIconItems[meta];
	}
	
	@Override
	public int getMetadata (int damageValue)
	{
		return damageValue;
	}
	
	@Override
	public String getUnlocalizedName(ItemStack IS)
	{
		IS.setItemName(subNames[IS.getItemDamage()]);
		return subNames[IS.getItemDamage()];
	}
	
	@Override
	public void registerIcons(IconRegister iR)
	{
		if(subNames != null)
		{
			if(subNames.length > 0)
			{
				Icon[] D = new Icon[subNames.length];
				for(int i=0;i<this.subNames.length;i++)
				{
					D[i] = iR.registerIcon(SGExtensions.baseLocation + this.subIcons[i]);
				}
				subIconItems = D;
			}
		}
	}
	
	@Override
	public void addInformation(ItemStack IS, EntityPlayer player, List data,boolean Huh)
	{
		if(subInfo.length > 0)
		{
			int damage = IS.getItemDamage();
			String text = subInfo[damage];
			if(text != null)
			{
				String[] result = text.split("#");
				for (int i=0;i<result.length;i++)
				{
					data.add(result[i]);
				}
			}
		}
	}
	
	public void getSubItems(int par1, CreativeTabs tab, List subItems)
	{
		for (int ix = 0; ix < subNames.length; ix++)
		{
			subItems.add(new ItemStack(this, 1, ix));
		}
	}

}

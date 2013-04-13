package sgextensions;

import java.awt.List;
import java.util.HashMap;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;

public class SGDarkGDOScreen extends SGScreen
{
	private ItemStack gdo;
	
	private final static int guiWidth = 256;
	private final static int guiHeight = 39;
	
	private final static int numWidth = 16;
	private final static int numHeight = 16;
	
	private int xO,yO;
	private static String screenTitle = "Garage Door Opener";
	
	private boolean isSendDown = false;
	
	private HashMap<String,SGDarkScreenClicker> clickers = new HashMap<String, SGDarkScreenClicker>();
	
	EntityPlayer owner;
	
	private String code;

	private int[] numXY(int num)
	{
		if(num == 0)
		{
			int[] Sq = {144,48};
			return Sq;
		}
		else if(num <= 9 && num > 0)
		{
			int[] Sq = {16*(num-1),48};
			return Sq;
		}
		return null;
	}
	
	public SGDarkGDOScreen(EntityPlayer player, ItemStack item) {
		super(new SGDarkGDOContainer(player),guiWidth,guiHeight);
		this.gdo = item;
		this.owner = player;
		this.code = item.stackTagCompound.getString("gdoCode");
		
	}
	
	@Override
	public void mouseClicked(int a,int b,int c)
	{
		//System.out.printf("Test: %d %d %d\n", a,b,c);
		for(int i=0;i<4;i++)
		{
			if(clickers.get("num"+Integer.toString(i)).isXYInRegion(a, b))
			{
				char t = code.charAt(i);
				String tS = Character.toString(t);
				int tI = Integer.parseInt(tS);
				if(c == 0){tI++;}
				if(c == 1){tI--;}
				if(tI > 9){tI = 0;}
				if(tI < 0){tI = 9;}
				char[] tA = code.toCharArray();
				tA[i] = Character.forDigit(tI,10);
				code = new String(tA);
				SGChannel.sendGdoChangeToServer(owner,code,false);
			}
		}
		if(clickers.get("sendButton").isXYInRegion(a, b))
		{
			isSendDown = true;
		}
	}
	
	@Override
	public void mouseMovedOrUp(int x,int y,int which)
	{
		if(which != -1)
		{
			if(isSendDown)
			{
				SGChannel.sendGdoChangeToServer(owner,code,true);
				isSendDown = false;
			}
		}
	}
	
	@Override
	public boolean doesGuiPauseGame()
	{
		return false;
	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float f,int a,int b)
	{
		xO = (width - guiWidth)/2;
		yO = (height - guiHeight)/2;
		bindTexture("/sgextensions/resources/gdo_gui.png", 256, 64);
		drawTexturedRect(xO,yO, guiWidth, guiHeight, 0, 0);
		int cx = xO + (xSize / 2);
		int color = 0x52aeff;
		drawCenteredString(fontRenderer, screenTitle, cx, yO +4, color, false);
		for (int i=0;i<4;i++)
		{
			if(!clickers.containsKey("num"+Integer.toString(i)))
			{
				SGDarkScreenClicker tThin = new SGDarkScreenClicker(xO + 11 + 18*i,yO + 16,xO + 27 + 18*i,yO + 32);
				clickers.put("num"+Integer.toString(i), tThin);
			}
			int value = Integer.parseInt(Character.toString(code.charAt(i)));
			int[] xy = numXY(value);
			if(xy == null)
			{
				System.out.println(code);
			}
			bindTexture("/sgextensions/resources/gdo_gui.png", 256, 64);
			drawTexturedRect((int)(xO + 11 + 18*i),(int)(yO + 16),(int)16,(int)16,xy[0],48);
		}
		if(!clickers.containsKey("sendButton"))
		{
			clickers.put("sendButton", new SGDarkScreenClicker(xO +170,yO + 15,xO + 208,yO + 33));
		}
		if(isSendDown)
		{
			bindTexture("/sgextensions/resources/gdo_gui.png", 256, 64);
			drawTexturedRect(xO +170,yO + 15,38,18,198,46);
		}
		else
		{
			bindTexture("/sgextensions/resources/gdo_gui.png", 256, 64);
			drawTexturedRect(xO +170,yO + 15,38,18,160,46);
		}
	}

}

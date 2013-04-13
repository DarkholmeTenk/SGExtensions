package sgextensions;

public class SGDarkScreenClicker 
{
	private int x,X,y,Y;
	
	SGDarkScreenClicker(int xT,int yT,int XT,int YT)
	{
		this.x = xT;
		this.y = yT;
		this.X = XT;
		this.Y = YT;
	}
	
	public boolean isXYInRegion(int xI,int yI)
	{
		if(xI >= x && yI >= y)
			if(xI <= X && yI <= Y)
				return true;
		return false;
	}
}

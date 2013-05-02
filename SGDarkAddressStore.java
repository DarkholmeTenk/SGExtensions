package sgextensions;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class SGDarkAddressStore {
	private Map<Integer,String> storedAddresses = new HashMap<Integer,String>();
	
	public void validateAddresses()
	{
		int pos = 0;
		boolean deletion = false;
		Object[] addCol = storedAddresses.values().toArray();
		if(storedAddresses.size() > 0)
		{
			int size = addCol.length;
			for(int i = pos;i<size;i++)
			{
				if(SGAddressing.findAddressedStargate(addCol[i].toString()) == null)
				{
					removeAddress(addCol[i].toString());
					deletion = true;
				}
			}
			if(deletion)
			{
				Collection<String> vals = storedAddresses.values();
				storedAddresses.clear();
				if(vals.size() > 0)
				{
					Object[] valArr = vals.toArray();
					for(int i =0;i<vals.size();i++)
					{
						addAddress(valArr[i].toString());
					}
				}
			}
		}
	}
	
	public String[] getAddresses()
	{
		String[] vals = new String[storedAddresses.size()];
		if(storedAddresses.size() > 0)
		{	
			int size = storedAddresses.size();
			for(int i = 0;i<size;i++)
			{
				vals[i] = storedAddresses.get(i);
			}
		}
		return vals;
	}
	
	public void addAddress(String Add)
	{
		if(!storedAddresses.containsValue(Add))
		{
			//System.out.printf("SGAS - Put: (%d,%s)\n",storedAddresses.size(),Add);
			storedAddresses.put(storedAddresses.size(), Add);
		}
	}
	
	public void removeAddress(String Add)
	{
		if(storedAddresses.containsValue(Add))
		{
			int Size = storedAddresses.size();
			for(int i =0;i<Size;i++)
			{
				if(storedAddresses.get(i) == Add )
				{
					storedAddresses.remove(i);
					break;
				}
			}
		}
	}
}

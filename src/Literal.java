
public class Literal {
	
	
	String name;
	String length;
	String address;
	
	public Literal(String name, String length, String address)
	{
		this.name = name;
		this.length = length;
		this.address = address;
	}
	
	public static String calculateValue(String operand)
	{
		StringBuilder s1 = new StringBuilder();
		if(operand.indexOf("c") == 1 || operand.indexOf("C") == 1)
		{
			
			char[] operand_array = operand.toCharArray();
			for(int i1 =2; i1<operand_array.length; i1++)
			{
				if(operand_array[i1] != '\'')
				{
					int x = (int)operand_array[i1];
					String Hex = Integer.toHexString(x);
					s1.append(Hex);
				}
			}
			return (s1.toString());
	
		}
		if(operand.indexOf("x") == 1 || operand.indexOf("X") == 1)
		{
			char[] operand_array = operand.toCharArray();
			for(int i1 =2; i1<operand_array.length; i1++)
			{
				if(operand_array[i1] != '\'')
				{
					s1.append(operand_array[i1]);
				}
			}
			return (s1.toString());
			
		}
		return (s1.toString());
	}
	
	public static String calculateLength(String operand)
	{
		int x = 0;
		String Length = "";
		if(operand.indexOf("c") == 1 || operand.indexOf("C") == 1)
		{
			
			x = (operand.length() - 4);
			
			Length =Integer.toHexString(x);
			
		}
		if(operand.indexOf("x") == 1 || operand.indexOf("X") == 1)
		{
			x = (operand.length() - 4)/2;
			
			Length =Integer.toHexString(x);
		}
		
		return Length;
	}
}

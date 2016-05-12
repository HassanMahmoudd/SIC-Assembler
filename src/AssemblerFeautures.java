import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import java.io.IOException;
import java.io.*;
import java.io.FileReader;
import java.io.BufferedReader;
import static java.lang.System.*;
import java.util.Hashtable;
import javax.naming.Binding;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.ScriptEngine;

public class AssemblerFeautures {
	static HashMap<String, String> OPCode = new HashMap<String,String>();
	static HashMap<String, String> SymbolTable = new HashMap<String,String>();
	static HashMap<Integer, String> errorTable = new HashMap<Integer, String>();
	static String lbl="",operand="",operation="";
	static String StartingAddress=GetFirstAddress();
	static String ProgrammName=GetProgammName();
	static ArrayList<String> addresses = new ArrayList<String>();
	static ArrayList<String> lines = new ArrayList<String>();
	static ArrayList<String> objectcodes = new ArrayList<String>();
	static ArrayList<String> labels = new ArrayList<String>();
	static ArrayList<String> operations = new ArrayList<String>();
	static boolean isArrayStart = false;
	static boolean isOperandIndexed = false;
	static String[] errors = new String[100];
	static ArrayList<String> comment = new ArrayList<String>();
	static HashMap<String, Literal> LitTable = new HashMap<String, Literal>();     
	static String LTORGAddress = "";     
	static boolean isStartParsed = true;     
	static HashMap<String, String> SymbolTableType = new HashMap<String,String>();     
	static String labelType = "";     
	static boolean isLineAfterEnd = false;     
	static String addressOfEnd = "";     
	static String nextAddressEQU = "";     
	static int lineCounter = 0;     

	public static void main(String[] args) {
		OPCode.put("ADD","18");  OPCode.put("add","18");  OPCode.put("AND","40");  OPCode.put("and","40");
		OPCode.put("COMP","28");  OPCode.put("comp","28"); OPCode.put("DIV","24");  OPCode.put("div","24");
		OPCode.put("J","3C");  OPCode.put("j","3C"); OPCode.put("JEQ","30");  OPCode.put("jeq","30");
		OPCode.put("JGT","34");  OPCode.put("jgt","34");  OPCode.put("JLT","38");  OPCode.put("jlt","38");
		OPCode.put("JSUB","48");  OPCode.put("jsub","48"); OPCode.put("LDA","00");  OPCode.put("lda","00");
		OPCode.put("LDCH","50");  OPCode.put("ldch","50"); OPCode.put("LDL","08");  
		OPCode.put("ldl","08"); OPCode.put("LDX", "04");   OPCode.put("ldx", "04");
        OPCode.put("MUL","20");  OPCode.put("mul","20");
		OPCode.put("OR","44");  OPCode.put("or","44"); OPCode.put("RD","D8");  OPCode.put("rd","D8");
		OPCode.put("RSUB","4C");  OPCode.put("rsub","4c"); OPCode.put("STA","0C");  OPCode.put("sta","0C");
		OPCode.put("STCH","54");  OPCode.put("stch","54"); OPCode.put("STL","14");  OPCode.put("stl","14");
		OPCode.put("STX","10");  OPCode.put("stx","10"); OPCode.put("SUB","1C");  OPCode.put("sub","1C");
		OPCode.put("TD","E0");  OPCode.put("td","E0"); OPCode.put("TIX","2C");  OPCode.put("tix","2C");
		OPCode.put("WD","DC");  OPCode.put("wd","DC");
		for(int i=0; i<errors.length; i++)
		{
			errors[i] = "";
		}
		boolean isLabelDuplicate = false;
		int count = 0;
		int counter = 0;     
		try{
			FileReader fr=new FileReader("ASD.txt");
			BufferedReader br=new BufferedReader(fr);
			Scanner s = new Scanner(fr);
			while(s.hasNext())
			{
				String line=s.nextLine();
				//System.out.println(line.startsWith("."));
				if(!line.startsWith("."))
				{
					String old_operation = operation;
					String old_operand = operand;
					lbl=line.substring(0,7).trim();
					if(line.length()>=15)
					{
						operation=line.substring(9,14).trim();
						operations.add(operation);
						if(line.length()>=35)
						{
							operand=line.substring(17,34).trim();
						}
						else 
							operand=line.substring(17).trim();
					} 
					else
					{
						operation=line.substring(9).trim();
						operations.add(operation);
						operand="";
					}
					//System.out.println(operation);
					
					if(line.indexOf("=") == 16)
					{
						System.out.println("LitTable is entered");
						String new_operand = "=" + operand;
						String value = Literal.calculateValue(new_operand);
						String length = Literal.calculateLength(new_operand);
						Literal lit = new Literal(new_operand, length, "");
						if(!LitTable.containsKey(value))
							LitTable.put(value, lit);
					}
					
					
					
					StartingAddress=CalculateAddress(StartingAddress,old_operation,old_operand);
					//System.out.println(StartingAddress);
					labels.add(lbl);     
					if(lbl != "")
					{
						
						if(!SymbolTable.containsKey(lbl))
						{
							SymbolTable.put(lbl,StartingAddress);
							SymbolTableType.put(lbl, "R");     
						}
						else
							isLabelDuplicate = true;
					}
					
					
					if(operation.equalsIgnoreCase("ORG"))
					{
						if(!operand.equals("*") && !operand.equals(""))
						{
							String result = evaluateExpression(operand, lineCounter);
							StartingAddress = result;
						}
						else if(operand.equals("*"))
							continue;
						else
						{
							errors[lineCounter] += " **** missing operand in ORG statement\r\n";
							errorTable.put(lineCounter, errors[lineCounter]);
						}
						
					}
					
					
					if(operation.equalsIgnoreCase("EQU"))
					{
						nextAddressEQU = StartingAddress;
						if(!operand.equals("*") && !operand.equals(""))
						{
							String result = evaluateExpression(operand, lineCounter);
							SymbolTable.put(lbl, result);
							SymbolTableType.put(lbl, labelType);
							StartingAddress = result;
						}
						else if(operand.equals("*"))
						{
							SymbolTable.put(lbl, StartingAddress);
							SymbolTableType.put(lbl, "R");
						}
						else
						{
							errors[lineCounter] += " **** missing operand in EQU statement\r\n";
							errorTable.put(lineCounter, errors[lineCounter]);
						}
					}
					
					
					if(StartingAddress.equals(""))
						StartingAddress = "0000";
					addresses.add(StartingAddress);
					System.out.println(StartingAddress);
					lines.add(line);
					if(!isLabelDuplicate)
						count++;
					
					
					
					
					
					
					if(operation.equalsIgnoreCase("LTORG"))
					{
						if(!operand.equals(""))
						{
							errors[lineCounter] += " **** illegal operand in LTORG statement\r\n";
							errorTable.put(lineCounter, errors[lineCounter]);
						}
						String displacement = StartingAddress;
						LTORGAddress = displacement;
						//System.out.println(displacement);
						for (Map.Entry<String, Literal> entry : LitTable.entrySet()) {
							  counter++;
							}
						System.out.println("Number of entries " + counter);
						for (Map.Entry<String, Literal> entry : LitTable.entrySet()) {
							  String key = entry.getKey();
							  Literal value = entry.getValue();
							  if(value.address.equals(""))
							  {
								  addresses.add(displacement);
								  System.out.println(displacement);
								  //System.out.println(operation);
								  lines.add("*       " + value.name);
								  labels.add("");
								  value.address = displacement;
								  int x = Integer.parseInt(displacement, 16);
								  System.out.println(value.length);
								  int y = Integer.parseInt(value.length, 16);
								  int z = x+y;
								  displacement = Integer.toHexString(z);
								  LTORGAddress = displacement;
								  //System.out.println(LTORGAddress);
							  }
							}
						//StartingAddress = displacement;
					}
					
					
					
					if(operation.equalsIgnoreCase("End"))
					{
						addressOfEnd = StartingAddress;
						String displacement = StartingAddress;
						LTORGAddress = displacement;
						for (Map.Entry<String, Literal> entry : LitTable.entrySet()) {
							  String key = entry.getKey();
							  Literal value = entry.getValue();
							  if(value.address.equals(""))
							  {
								  addresses.add(displacement);
								  System.out.println(displacement);
								  lines.add("*       " + value.name);
								  labels.add("");
								  value.address = displacement;
								  int x = Integer.parseInt(displacement, 16);
								  System.out.println(value.length);
								  int y = Integer.parseInt(value.length, 16);
								  int z = x+y;
								  displacement = Integer.toHexString(z);
								  LTORGAddress = displacement;
								  isLineAfterEnd = true;
							  }
							}
						
					}
					
				}
				else
				{
					labels.add("");     
					lines.add(line);
					addresses.add("");
					System.out.println("");
				}
				lineCounter++;     
			}
			//SymbolTable.put("Hassan", "1000"); //Test evaluateExpression()
			//SymbolTableType.put("Hassan", "R"); //Test evaluateExpression()
			//SymbolTable.put("H", "2000"); //Test evaluateExpression()
			//SymbolTableType.put("H", "R"); //Test evaluateExpression()
			//System.out.println(evaluateExpression("Hassan-H+2000")); //Test evaluateExpression()
			//System.out.println("Type is " + labelType); //Test evaluateExpression()
			//System.out.println(addresses.size());
			//System.out.println(lines.size());
			//System.out.println(OPCode.get("Add"));
			//System.out.println(SymbolTable.get(""));
			for(int j=0; j<labels.size(); j++)
			{
				if(!labels.get(j).equals(""))
				for(int k=0; k<labels.size(); k++)
				{
					if(k != j)
					{
						if(labels.get(j).equals(labels.get(k)) && !labels.get(k).equals(""))
						{
							errors[j] += " **** duplicate label definition\r\n";
							errorTable.put(j, errors[j]);
						}
						/*
						if(labels.get(j).equals(labels.get(k)) && labels.get(k).equals("START"))
						{
							errors[j] += " **** duplicate start statement\r\n";
							errorTable.put(j, errors[j]);
						}
						*/
						
					}
				}
			}
			for(int j=0; j<operations.size(); j++)
			{
				if(!operations.get(j).equals(""))
				for(int k=0; k<operations.size(); k++)
				{
					if(k != j)
					{
		
						if(operations.get(j).equals(operations.get(k)) && operations.get(k).equals("START"))
						{
							errors[j] += " **** duplicate start statement\r\n";
							errorTable.put(j, errors[j]);
						}
						
					}
				}
			}
			
			for (Map.Entry<String,String> entry : SymbolTable.entrySet()) {
				  String key = entry.getKey();
				  String value = entry.getValue();
				  //System.out.println(key + "   " + value);
				}
			for(int i = 0; i < addresses.size(); i++)
			{
				//System.out.println(addresses.get(i));
			}
			br.close();
		}catch (IOException e){
			System.out.println("File Not Found");
		} 
		
		
		//Pass 2
		boolean isStart = false;
		int indexStart = 0;
		int endIndex = 0; 
		for(int i = 0; i < lines.size(); i++)
		{
			if(lines.get(i).startsWith("."))
			{
				objectcodes.add("");
				continue;
			}
				
			String line = lines.get(i);
			lbl=line.substring(0,7).trim();
			System.out.println(line);
			//System.out.println(!line.substring(0,8).contains(" "));
			
			
			if(line.startsWith("*"))
			{
				String new_operand = line.substring(8).trim();
				String value = Literal.calculateValue(new_operand);
				String objectcode = "";
				for (Map.Entry<String, Literal> entry : LitTable.entrySet()) 
				{
					  String key = entry.getKey();
					  Literal valueOfEntry = entry.getValue();
					  if(value.equals(key))
					  {
						  objectcode = key;
					  }
						   
				}
			    
				objectcodes.add(objectcode);
				System.out.println(objectcode);
				continue;
			}
			if(!line.substring(0,8).contains(" "))
			{
				errors[i] += " **** illegal format in label field\r\n";
				errorTable.put(i, errors[i]);
			}
			
			if(line.length()>=15)
			{
				operation=line.substring(9,14).trim();
				if(operation.length() == 0)
				{
					errors[i] += " **** missing operation code\r\n";
					errorTable.put(i, errors[i]);
				}
				if(!line.substring(9,15).contains(" "))
				{
					errors[i] += " **** illegal format in operation field\r\n";
					errorTable.put(i, errors[i]);
				}
				
				if(line.length()>=35)
				{
					operand=line.substring(17,34).trim();
					
					if(!line.substring(17,35).contains(" "))
					{
						errors[i] += " **** illegal format in operand field\r\n";
						errorTable.put(i, errors[i]);
					}
					
				}
				else 
					operand=line.substring(17).trim();
			} 
			else
			{
				operation=line.substring(9).trim();
				operand="";
				if(operation.length() == 0)
				{
					errors[i] += " **** missing operation code\r\n";
					errorTable.put(i, errors[i]);
				}
			}
			
			
			if(line.indexOf("=") == 16)
			{
				String new_operand = "=" + operand;
				String value = Literal.calculateValue(new_operand);
				String targetAddress = "";
				for (Map.Entry<String, Literal> entry : LitTable.entrySet()) 
				{
					  String key = entry.getKey();
					  Literal valueOfEntry = entry.getValue();
					  if(value.equals(key))
					  {
						  targetAddress = valueOfEntry.address;
					  }
						   
				}
			    String opcode = OPCode.get(operation);
				objectcodes.add(opcode + targetAddress);
				System.out.println(opcode + targetAddress);
			}
			
			
			else if(operation.equalsIgnoreCase("ORG"))
			{
				objectcodes.add("");
			}
			
			
			else if(operation.equalsIgnoreCase("LTORG"))
			{
				objectcodes.add("");
			}
			
			
			else if(operation.equalsIgnoreCase("EQU"))
			{
				objectcodes.add("");
			}
			
			else if(operation.equalsIgnoreCase("Start"))
			{
				isStart = true;
				indexStart = i;
				if(operand.trim().length() == 0)
				{
					errors[i] += " **** missing or misplaced operand in start statement\r\n";
					errorTable.put(i, errors[i]);
				}
				if(isNumeric(operand))
				{
					objectcodes.add("");
				}
				else
				{
					errors[i] += " **** illegal operand in start statement\r\n";
					errorTable.put(i, errors[i]);
					objectcodes.add("");
				}
			}
			else if(operation.equalsIgnoreCase("End"))
			{
				endIndex = i;
				if(operand.trim().length() == 0)
				{
					errors[i] += " **** missing or misplaced operand in end statement\r\n";
					errorTable.put(i, errors[i]);
				}
				if(operand.equals(GetProgammName()) || operand.equals(labels.get(1)))     
				{
					objectcodes.add("");
				}
				else
				{
					errors[i] += " **** illegal operand in end statement\r\n";
					errorTable.put(i, errors[i]);
					objectcodes.add("");
				}
			}
			else if(operation.equalsIgnoreCase("Resw"))
			{
				if(operand.trim().length() == 0)
				{
					errors[i] += " **** missing or misplaced operand in resw statement\r\n";
					errorTable.put(i, errors[i]);
				}
				if(isNumeric(operand))
				{
					objectcodes.add("");
				}
				else
				{
					errors[i] += " **** illegal operand in resw statement\r\n";
					errorTable.put(i, errors[i]);
					objectcodes.add("");
				}
			}
			else if(operation.equalsIgnoreCase("Resb"))
			{
				if(operand.trim().length() == 0)
				{
					errors[i] += " **** missing or misplaced operand in resb statement\r\n";
					errorTable.put(i, errors[i]);
				}
				if(isNumeric(operand))
				{
					objectcodes.add("");
				}
				else
				{
					errors[i] += " **** illegal operand in resb statement\r\n";
					errorTable.put(i, errors[i]);
					objectcodes.add("");
				}
			}
			else if(operation.equalsIgnoreCase("Word"))
			{
				//System.out.println(operation);
				//System.out.println(operand);
				if(operand.trim().length() == 0)
				{
					errors[i] += " **** missing or misplaced operand in word statement\r\n";
					errorTable.put(i, errors[i]);
				}
				if(isNumeric(operand))
				{
					int y=Integer.parseInt(operand);
					//System.out.println(y);
					String Hex=Integer.toHexString(y);
					//System.out.println(Hex);
					int L=6-Hex.length();
					//System.out.println(L);
					for(int index = 0; index < L; index++)
						Hex="0"+Hex;
					//System.out.println(Hex);
					objectcodes.add(Hex);
				}
				else
				{
					errors[i] += " **** illegal operand in word statement\r\n";
					errorTable.put(i, errors[i]);
					objectcodes.add("");
				}
					
				//System.out.println(operation);
				//System.out.println(Hex);
			}
			else if(operation.equalsIgnoreCase("Rsub"))
			{
				String x = OPCode.get("rsub");
				x += "0000";
				objectcodes.add(x);
			}
			else if(operation.equalsIgnoreCase("Byte"))
			{
				if(operand.trim().length() == 0)
				{
					errors[i] += " **** missing or misplaced operand in byte statement\r\n";
					errorTable.put(i, errors[i]);
				}
				StringBuilder s1 = new StringBuilder();
				boolean flag = false;
				if(operand.startsWith("c") || operand.startsWith("C"))
				{
					
					char[] operand_array = operand.toCharArray();
					for(int i1 =1; i1<operand_array.length; i1++)
					{
						if(operand_array[i1] != '\'')
						{
							int x = (int)operand_array[i1];
							String Hex = Integer.toHexString(x);
							s1.append(Hex);
						}
					}
					objectcodes.add(s1.toString());
					flag = true;
				}
				if(operand.startsWith("x") || operand.startsWith("X"))
				{
					char[] operand_array = operand.toCharArray();
					for(int i1 =1; i1<operand_array.length; i1++)
					{
						if(operand_array[i1] != '\'')
						{
							s1.append(operand_array[i1]);
						}
					}
					objectcodes.add(s1.toString());
					flag = true;
				}
				if(!flag)
				{
					errors[i] += " **** illegal operand in byte statement\r\n";
					errorTable.put(i, errors[i]);
					objectcodes.add("");
				}
				
				//System.out.println(s1.toString());
			}
			else
			{
				if(operand.trim().length() == 0)
				{
					errors[i] += " **** missing or misplaced operand in instruction\r\n";
					errorTable.put(i, errors[i]);
				}
				if(operand.indexOf(',')>=0)
				{
					boolean flag = false;
					isOperandIndexed = true;
					operand = operand.substring(0, operand.indexOf(','));
					if(!SymbolTable.containsKey(operand))
					{
						errors[i] += " **** undefined symbol in operand\r\n";
						errorTable.put(i, errors[i]);
						flag = true;
						
					}
					if(!OPCode.containsKey(operation))
					{
						errors[i] += " **** unrecognized operation code\r\n";
						errorTable.put(i, errors[i]);
						flag = true;
						
					}
					if(flag)
						objectcodes.add("");
					if(SymbolTable.containsKey(operand) && OPCode.containsKey(operation))
						objectcodes.add(CalculateObjectCode(OPCode.get(operation),SymbolTable.get(operand)));
					//System.out.println(CalculateObjectCode(OPCode.get(operation),SymbolTable.get(operand)));
				}
				else
				{
					boolean flag = false;
					//System.out.println(operation);
					//System.out.println(!SymbolTable.containsKey(operand));
					
					
					if((operand.indexOf("+") >= 0) || (operand.indexOf("-") >= 0))
					{
						operand = evaluateExpression(operand, i);     
						if(operand.equals(""))
							objectcodes.add("");
						else
							objectcodes.add(CalculateObjectCode(OPCode.get(operation),operand));
					}
					else
					{
						if(!SymbolTable.containsKey(operand))
						{
							errors[i] += " **** undefined symbol in operand\r\n";
							errorTable.put(i, errors[i]);
							flag = true;
							
						}
						if(!OPCode.containsKey(operation))
						{
							errors[i] += " **** unrecognized operation code\r\n";
							errorTable.put(i, errors[i]);
							flag = true;
							
						}
						if(flag)
							objectcodes.add("");
						if(SymbolTable.containsKey(operand) && OPCode.containsKey(operation))
							objectcodes.add(CalculateObjectCode(OPCode.get(operation),SymbolTable.get(operand)));
						//System.out.println(CalculateObjectCode(OPCode.get(operation),SymbolTable.get(operand)));
					}
				}
					
			}
			
		}
		if(!isStart)
		{
			errors[indexStart] += " **** missing or misplaced start statement\r\n";
			errorTable.put(indexStart, errors[indexStart]);
		}
		if(endIndex != lines.size()-1 && (!isLineAfterEnd))
		{
			errors[endIndex] += " **** statement should not follow end statement\r\n";
			errorTable.put(endIndex, errors[endIndex]);
			isLineAfterEnd = false;     
		}
		for(int i = 0; i < objectcodes.size(); i++)
		{
			//System.out.println(objectcodes.get(i));
		}
		//System.out.println(objectcodes.size());
		Printer();
		PrinterObjectProgram();
		
	}
	public static String CalculateAddress(String StartingAddress,String Operation,String Operand){
		//System.out.println("Operation in calclateAddress" + Operation);

		int x=0,y=0;
		if(isArrayStart)
		{
			//System.out.println("LTORG is entered in calculate address");
			isArrayStart = false;
			return StartingAddress;
		}
		
		if((Operation.equals("org")||Operation.equals("ORG")||Operation.equals("Org")))
		{
			System.out.println("ORG is entered in calculate address");
			return StartingAddress;
		}
		
		if((Operation.equals("equ")||Operation.equals("EQU")||Operation.equals("Equ")))
		{
			System.out.println("EQU is entered in calculate address");
			return nextAddressEQU;
		}
		
		if((Operation.equals("ltorg")||Operation.equals("LTORG")||Operation.equals("Ltorg")))
		{
			System.out.println("LTORG is entered in calculate address");
			return LTORGAddress;
		}
		if((Operation.equals("START")||Operation.equals("start")||Operation.equals("Start")||isStartParsed) && !Operation.equalsIgnoreCase("LTORG"))
		{
			isStartParsed = false;     
			isArrayStart = true;
			return StartingAddress;
		}
		
		if((Operation.equals("RESW")||Operation.equals("resw")||Operation.equals("Resw")))
		{
			if(isNumeric(Operand))
			{
				//System.out.println("1");
				x=Integer.parseInt(StartingAddress,16);
				y=Integer.parseInt(Operand);
				//System.out.println("This is y"+y);
				x=x+3*y;
				StartingAddress=Integer.toHexString(x);
				
			}
			return StartingAddress;
		}
		if((Operation.equals("Word")||Operation.equals("WORD")||Operation.equals("word")))
		{
			//System.out.println("2");
			x=Integer.parseInt(StartingAddress,16);
			x=x+3;
			StartingAddress=Integer.toHexString(x);
			
			return StartingAddress;
		}
		if((Operation.equals("RESB")||Operation.equals("Resb")||Operation.equals("resb")))
		{
			if(isNumeric(Operand))
			{
				//System.out.println("3");
				x=Integer.parseInt(StartingAddress,16);
				y=Integer.parseInt(Operand);
				x=x+1*y;
				StartingAddress=Integer.toHexString(x);
				
			}
			return StartingAddress;
		}
		if((Operation.equals("byte")||Operation.equals("BYTE")||Operation.equals("Byte")))
		{
			//System.out.println("2");
			//System.out.println(Operand);
			if(Operand.startsWith("c") || Operand.startsWith("C"))
			{
				x=Integer.parseInt(StartingAddress,16);
				x+= (Operand.length() - 3);
				//System.out.println(Operand.length());
				StartingAddress=Integer.toHexString(x);
				//System.out.println(StartingAddress);
			}
			if(Operand.startsWith("x") || Operand.startsWith("X"))
			{
				x=Integer.parseInt(StartingAddress,16);
				x+= (Operand.length() - 3)/2;
				StartingAddress=Integer.toHexString(x);
			}
			
			return StartingAddress;
		}
		
		else
		{
			//System.out.println("4");
			x=Integer.parseInt(StartingAddress,16);
			x=x+3;
			StartingAddress=Integer.toHexString(x);
			//System.out.println(Operation);
			return StartingAddress;
		}

	}
	public static String CalculateObjectCode(String OPCode,String Address)
	{
		int xAddress = Integer.parseInt(Address, 16);
		String binAddress = Integer.toBinaryString(xAddress);
		int z=15-(binAddress.length());
		//System.out.println(z);
		for(int i=0;i<z;i++)
			binAddress="0"+binAddress;
		//System.out.println(xAddress);
		int x=Integer.parseInt(OPCode,16);
		String bin=Integer.toBinaryString(x);
		if(isOperandIndexed)
		{
			//System.out.println(isOperandIndexed);
			bin=bin+"1";
			isOperandIndexed = false;
		}
		else
			bin=bin+"0";
		bin=bin+binAddress;
		//System.out.println(bin);
		int y=Integer.parseInt(bin,2);
		String Hex=Integer.toHexString(y);
		int L=6-Hex.length();
		for(int i=0;i<L;i++)
			Hex="0"+Hex;
		
		return Hex;
	}
	public static String GetFirstAddress()
	{
		String StartingAddress=null;
		try{

			FileReader fr=new FileReader("ASD.txt");
			BufferedReader br=new BufferedReader(fr);
			String text = br.readLine();
			StartingAddress=text.substring(17,34).trim();
			if(StartingAddress.length() == 0)
			{
				StartingAddress = "1000";
			}
		} catch(IOException a) {}
		return StartingAddress;

	}


	public static String GetProgammName(){
		String ProgrammName=null;
		try{

			FileReader fr=new FileReader("ASD.txt");
			BufferedReader br=new BufferedReader(fr);
			String text = br.readLine();
			ProgrammName=text.substring(0,7).trim();	 
		} catch(IOException z) {}
		return ProgrammName;
	}



	public static void  Printer(){
		try{
			File file=new File("ListFile.txt");
			
			PrintWriter pw = new PrintWriter(new FileWriter(file, true));
			
			for(int i = 0; i<addresses.size(); i++)
			{
				if(lines.get(i).startsWith("."))
				{
					pw.println("            " + lines.get(i)); 
					continue;
				}
				//System.out.println(lines.get(i));
				int diff = 6 - objectcodes.get(i).length();
				String spaces = "";
				for(int j =0; j<diff; j++)
				{
					spaces += " ";
				}
				int index = 0;
				if(objectcodes.get(i).length() > 6)
				{
					pw.println(addresses.get(i) + " " + objectcodes.get(i).substring(index, index+6) +  " " + spaces + lines.get(i));
					index = index + 6;
					//System.out.println(objectcodes.get(i).length());
					for(int i1 =0; i1<(objectcodes.get(i).length())/6; i1++)
					{
						if(objectcodes.get(i).length() > 12 && i1 == 0)
						{
							pw.println("     " + objectcodes.get(i).substring(index, index+6));
							index = index + 6;
						}
						else if(objectcodes.get(i).length() > 18 && i1 == 1)
						{
							pw.println("     " + objectcodes.get(i).substring(index, index+6));
							index = index + 6;
						}
						else if(objectcodes.get(i).length() > 24 && i1 == 2)
						{
							pw.println("     " + objectcodes.get(i).substring(index, index+6));
							index = index + 6;
						}
						else
							pw.println("     " + objectcodes.get(i).substring(index));
					}
				}
				else
					pw.println(addresses.get(i) + " " + objectcodes.get(i) +  " " + spaces + lines.get(i));
				if(errorTable.containsKey(i))
				{
					pw.println(errorTable.get(i));
				}
			}
			
			pw.close();
		} catch(IOException c)
		{
			System.out.println("NO WAY !!");
		}

	}
	public static void PrinterObjectProgram(){
		try{
			File file=new File("ObjectProgram.txt");
			
			PrintWriter pw = new PrintWriter(new FileWriter(file, true));
			String header = "H";
			String programName = GetProgammName();
			int length1 = 6 - programName.length();
			for(int i = 0; i < length1; i++)
			{
				programName+= " ";
			}
			String startingAddress = GetFirstAddress();
			int length2 = 6 - startingAddress.length();
			for(int i = 0; i < length2; i++)
			{
				startingAddress = "0" + startingAddress;
			}
			String lastAddress = addresses.get(addresses.size() - 1);
			int x = Integer.parseInt(lastAddress, 16);
			int y = Integer.parseInt(startingAddress, 16);
			int z = x-y;
			String length = Integer.toHexString(z);
			int length3 = 6 - length.length();
			for(int i = 0; i < length3; i++)
			{
				length = "0" + length;
			}
			pw.println(header + " " + programName + " " + startingAddress + " " + length);
			//Text
			int total = 0;
			int counter = 0;
			for(int k = 0; k < objectcodes.size(); k++)
			{
				total+= objectcodes.get(k).length();
			}
			int startingIndex = 1;
			int endingIndex = 0;
			for(int b =0; b < (total/60)+1+counter; b++)
			{
				int sum = 0;
				
				String objectCode1 = "";
				String objectCode1Length = "";
				String startingAddressOC = addresses.get(startingIndex);
				for(int j = startingIndex; j < objectcodes.size(); j++)
				{
					if(lines.get(j).startsWith(".") && j == 1)
					{
						continue;
					}
					sum+= objectcodes.get(j).length();
					if(sum > 60)
					{
						sum -= objectcodes.get(j).length();
						startingIndex = j;
						break;
					}
					else if(sum <= 60 && objectcodes.get(j).length() == 0 && j!=objectcodes.size()-1 
							&& !lines.get(j).substring(9, 14).trim().equalsIgnoreCase("LTORG") && !lines.get(j).substring(9, 14).trim().equalsIgnoreCase("EQU")
							)     
					{
						System.out.println("Operation entered in object program is " + lines.get(j).substring(9, 14).trim());
						counter++;
						int temp1 = j;
						//System.out.println(j);
						while(objectcodes.get(temp1).length() == 0 && temp1 != objectcodes.size()-1)
						{
							temp1++;
						}
						startingIndex = temp1;
						if(temp1 == objectcodes.size()-1)
							counter--;
						break;
					}
					startingIndex = j;
				}
				int temp = sum/2;
				//System.out.println(startingIndex);
				//System.out.println(endingIndex);
				//System.out.println(sum);
				for(int v = endingIndex; v < startingIndex; v++)
				{
					if(!objectcodes.get(v).equals(""))
					{
						objectCode1 += objectcodes.get(v);
					}
				}
				endingIndex = startingIndex;
				objectCode1Length = Integer.toHexString(temp);
				int length4 = 2 - objectCode1Length.length();
				for(int i = 0; i < length4; i++)
				{
					objectCode1Length = "0" + objectCode1Length;
				}
				String text = "T";
				if(!startingAddressOC.equals(addressOfEnd))     //*New condition added to avoid printing last line of end statement
					pw.println(text + " " + startingAddressOC + " " + objectCode1Length + " " + objectCode1);
			}
			String end = "E";
			pw.println(end + " " + startingAddress);
			pw.close();
		} catch(IOException c)
		{
			System.out.println("NO WAY !!");
		}

	}
	public static boolean isNumeric(String str)  
	{  
	  try  
	  {  
	    double d = Double.parseDouble(str);  
	  }  
	  catch(NumberFormatException nfe)  
	  {  
	    return false;  
	  }  
	  return true;  
	}
	
	
	public static String evaluateExpression(String Expression1, int index)
	{
	
        
        String Expression2 = "";
        String temp = "";
        String address;
        String typeExpression = "";
        String type;
        int termsCount = 0;
        int RCount = 0;
        int ACount = 0;
        boolean flag = false;
        for (int i = 0; i < Expression1.length(); i++) {
            if (Expression1.charAt(i) == '+' || Expression1.charAt(i) == '-') {
            	if(isNumeric(temp))
            	{
            		int x = Integer.parseInt(temp, 16);
            		System.out.println("Number in hexa " + x);
            		Expression2 = Expression2 + x + Expression1.charAt(i);
            		temp = "";
            		typeExpression += "A" + Expression1.charAt(i);
            		termsCount++;
            		ACount++;
            	}
            	else
            	{
	                address = SymbolTable.get(temp);
	                type = SymbolTableType.get(temp);
	                System.out.println("Address is " + address);
	                int addressInt = Integer.parseInt(address, 16);
	                System.out.println("AddressInt is " + addressInt);
	                Expression2 = Expression2 + addressInt + Expression1.charAt(i);
	                temp = "";
	                typeExpression += type + Expression1.charAt(i);
	                termsCount++;
	                if(type.equals("R"))
	                	RCount++;
	                else
	                	ACount++;
            	}
            } else {
                temp = temp + Expression1.charAt(i);
            }
        }
        if(isNumeric(temp))
    	{
    		int x = Integer.parseInt(temp, 16);
    		Expression2 = Expression2 + x;
    		typeExpression += "A";
    		termsCount++;
    		ACount++;
    	}
        else
        {
        	address = SymbolTable.get(temp);
        	type = SymbolTableType.get(temp);
        	System.out.println("Address is " + address);
        	int addressInt = Integer.parseInt(address, 16);
        	System.out.println("AddressInt is " + addressInt);
        	Expression2 = Expression2 + addressInt;
        	typeExpression += type;
        	termsCount++;
        	if(type.equals("R"))
            	RCount++;
            else
            	ACount++;
        }
        System.out.println(typeExpression);
        String typeExpressionTemp = "";
        String typeExpressionFinal = "";
        if(typeExpression.contains("R+R") || typeExpression.contains("A-R") || typeExpression.contains("R-A"))
        {
        	//System.out.println("Error in the Expression");
        	errors[index] += " **** illegal expression in statement\r\n";
			errorTable.put(index, errors[index]);
        	labelType = "Error";
        }
        else if(termsCount == ACount)
        {
        	labelType = "A";
        	flag = true;
        }
        else
        {
        	for(int i = 0; i < typeExpression.length(); i++)
        	{
        		if((i % 3) == 0 && (i != 0))
        		{
        			//System.out.println("Hello" + i);
        			if(typeExpressionTemp.contains("R-R") || typeExpressionTemp.contains("A-A") || typeExpressionTemp.contains("A+A"))
        			{
        				typeExpressionFinal+= "A" + typeExpression.charAt(i);
        				//System.out.println(typeExpressionFinal);
        			}
        			else if(typeExpressionTemp.contains("A+R") || typeExpressionTemp.contains("R+A"))
        				typeExpressionFinal+= "R" + typeExpression.charAt(i);
        			typeExpressionTemp = "";
        		}
        		else
        		
        			typeExpressionTemp+= typeExpression.charAt(i);
        	}
        	typeExpressionFinal+= typeExpressionTemp;
        	//System.out.println(typeExpressionTemp);
        	//System.out.println(typeExpressionFinal);
        	if(typeExpressionFinal.contains("R+R") || typeExpressionFinal.contains("A-R") || typeExpressionFinal.contains("R-A"))
            {
            	//System.out.println("Error in the Expression");
            	errors[index] += " **** illegal expression in statement\r\n";
    			errorTable.put(index, errors[index]);
            	labelType = "Error";
            }
            else if(typeExpressionFinal.contains("R-R") || typeExpressionFinal.contains("A+A") || typeExpressionFinal.contains("A-A"))
            {
            	labelType = "A";
            	flag = true;
            }
            else
            {
            	labelType = "R";
            	flag = true;
            }
        }
        System.out.println("Label type is " + labelType);
        if(flag)
        {
	        ScriptEngineManager mgr = new ScriptEngineManager();
	        ScriptEngine engine = mgr.getEngineByName("JavaScript");
	        String result = "";
	        Object evaluation = null;
	        try {
				evaluation = engine.eval(Expression2);
			} catch (ScriptException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
	        //System.out.println(evaluation.toString());
	        result = evaluation.toString();
	        int x = Integer.parseInt(result, 10);
	        result = Integer.toHexString(x);
	        System.out.println("Result in hexa " + result);
	        //System.out.println(result);
	        
	        flag = false;
	        return result;
	        
        }
		return "";
	}

}

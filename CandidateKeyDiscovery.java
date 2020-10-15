import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;

public class CandidateKeyDiscovery
{
	public static String relation;
	public static Set<String> attributes;
	public static Set<String> lhsSetG;
	public static Set<String> rhsSetG;
	public static Map<String,Set<String>> map;
	
	public static void readAttributes(String[] args)
	{
		String attr[] = args[0].split(",|\\s");
		attributes = new HashSet<>();
		
		attributes.add(attr[0].substring(2));
		for(int i = 1; i<attr.length-1;i++)
		{
			if(attr.equals("")||attr.equals(" "))
				continue;
			attributes.add(attr[i]);
		}
		attributes.add(attr[attr.length-1].substring(0,attr[attr.length-1].length()-1));
		
	}
	
	public static void readFDs(String[] args)
	{
		lhsSetG = new HashSet<>();
		rhsSetG = new HashSet<>();
		map = new HashMap<>();
		
		String fds[] = args[1].split(";");
		//System.out.println(fds.length);
		for(int i =0; i < fds.length;i++)
		{
			Set<String> rhsSet = new HashSet<>();
			String arg[] = fds[i].split(",|\\s");
			boolean left = true;
			String key = "";
			String value = "";
			for(int j = 0; j < arg.length; j++)
			{
				if(arg[j].equals("") || arg[j].equals(" "))
					continue;
				if(arg[j].equals("->"))
				{
					left = false;
					if(!map.containsKey(value))
						map.put(value, rhsSet);
					else
						rhsSet = map.get(rhsSet);
					value = "";
					continue;
				}
				if(arg[j].contains("->"))
				{
					left = false;
					arg[j]=arg[j].replace("->", " ");
					//System.out.println(arg[j]);
					String a[] = arg[j].split(" ");
					
					if(j==0)
						value =value + a[0];
					else
						value = value + "," + a[0];
					
					lhsSetG.add(a[0]);
					//key = new String(value);
					if(!map.containsKey(value))
						map.put(value, rhsSet);
					else
					{
						rhsSet = map.get(value);
					}
					
					value = a[1];
					rhsSet.add(a[1]);
					rhsSetG.add(a[1]);
					continue;
				}
				if(left)
				{
					if(j!=0)
						value = value + "," + arg[j];
					else
						value = value + arg[j];
					lhsSetG.add(arg[j]);
				}
					
				else
				{
					rhsSet.add(arg[j]);
					rhsSetG.add(arg[j]);
				}
			}
		
		}
		
	}
	public static void main(String[] args)
	{
		try
		{
			readAttributes(args);
			System.out.println("1");
			readFDs(args);
			System.out.println("1");
			Set<String> partOfCandidateKey = new HashSet<>();
			Set<String> both = new HashSet<>();
			
			for (String attribute : attributes)
			{
				System.out.println(attribute);
				if (!lhsSetG.contains(attribute) && !rhsSetG.contains(attribute))
				{
					partOfCandidateKey.add(attribute);
				} else if (lhsSetG.contains(attribute) && !rhsSetG.contains(attribute)) 
				{
					partOfCandidateKey.add(attribute);
				} else if (lhsSetG.contains(attribute) && rhsSetG.contains(attribute)) 
				{
					both.add(attribute);
				}
			}
			System.out.println("1");
			FileWriter fileWriter = new FileWriter(args[2]);
			PrintWriter printWriter = new PrintWriter(fileWriter);
			System.out.println("1");
			System.out.println(partOfCandidateKey);
			if(checkClosure(partOfCandidateKey))
			{
				boolean fileEmpty = true;
				for(String key : partOfCandidateKey)
				{
					if(fileEmpty)
					{
						//System.out.println(key);
						printWriter.write(key);
						fileEmpty = false;
					}
					else
					{
						//System.out.println(key);
						printWriter.write(","+key);
					}
				}
			}
			
			else
			{
				boolean fileEmpty = true;
				for(int i = 1;i<=both.size();i++)
				{
					Set<Set<String>> combSet = Sets.combinations(both, i);
					
					for(Set<String> sset : combSet)
					{
							partOfCandidateKey.addAll(sset);
							if(checkClosure(partOfCandidateKey))
							{
								//System.out.println("true");
								if(!fileEmpty)
									printWriter.append(";");
								fileEmpty = true;
								for(String key : partOfCandidateKey)
								{
									if(fileEmpty)
									{
										printWriter.append(key);
										fileEmpty = false;
									}
									else
									{
										printWriter.append(","+key);
									}
								}
								fileEmpty = false;
								both.removeAll(sset);
							}
							partOfCandidateKey.removeAll(sset);
					}
				}
				
			}
			System.out.println("1");
			printWriter.close();
			fileWriter.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
		
	
	public static boolean checkClosure(Set<String> set)
	{
		//System.out.println("set        "+set);
		Set<String> result = new HashSet<>(set);
		boolean change = true;
		while(change)
		{
			change = false;
			for(int i = 1 ; i<=result.size();i++)
			{
				Set<Set<String>> setset = Sets.combinations(result, i);
				int size = result.size();
				for(Set<String> set1 : setset)
				{
					//System.out.print(set1);
					
					String ans = "";
					int count = 0;
					for(String ss:set1)
					{
						if(count ==0)
						ans = ans +ss;
						else
							ans = ans + ","+ss;
						
						if(map.containsKey(ans))
						{
							result.addAll(map.get(ans));
						}
						
						
							
						count++;
					}
				}
				if(size!= result.size())
					change = true;

			}

		}
		Set<String> totalSet =  new HashSet<>(attributes);
		totalSet.removeAll(result);
		if(totalSet.size()==0)
			return true;
		else
			return false;
		
	}

}

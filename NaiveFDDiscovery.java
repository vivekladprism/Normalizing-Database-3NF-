import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.google.common.collect.Sets;
import com.mysql.jdbc.Connection;
import com.mysql.jdbc.PreparedStatement;
import com.mysql.jdbc.ResultSetMetaData;
import com.mysql.jdbc.Statement;

public class NaiveFDDiscovery
{
	public static String url;
	public static String userName;
	public static String password;
	public static String relations;
	public static String outputFileNames;
	public static String outputFileName[];
	public static Connection connection;
	public static String relationName;
	public static int numberOfAttributes;
	public static String[] attributes;
	public static ResultSet rs;
	public static PreparedStatement ps;
	public static Set<String> setOfAttributes;
	public static int i;
	public static String relation[];
	
	public static void ReadRelations()
	{
		relation = relations.split(",");
		outputFileName = outputFileNames.split(",");
	}

	public static void naiveFDDApproach(int m) throws SQLException, IOException
	{
		String query = "";
		String fields = "";
		
		FileWriter fileWriter = new FileWriter(outputFileName[m]);
		PrintWriter printWriter = new PrintWriter(fileWriter);
		Map<String,Set<String>> dependency = new HashMap<>();
		boolean fileEmpty = true;
		for(i = 1;i<=numberOfAttributes;i++)
		{
			Set<Set<String>> combSet = Sets.combinations(setOfAttributes, i);
			
			for(Set<String> set : combSet)
			{
				for(String currentAttribute: setOfAttributes)
				{
					boolean toProcess= true;
					query+="Select * From "+relation[m]+" As t1 JOIN "+relation[m] + " t2 ON ";
					int count = 1;
					String setString ="";
					for(String setElement : set)
					{
						if(set.size()==1 && setElement.equals(currentAttribute))
						{	
							query = "";
							toProcess = false;
							continue;
						}
						query+= ("t1."+setElement+ " = t2."+ setElement);
						if(count!=set.size())
						{
							query+= " AND ";
							setString = setString + setElement + ",";
						}
						else
							setString = setString + setElement;
						
						count++;
							
					}
					if(setString.contains(currentAttribute))
						toProcess = false;
					for(String key : dependency.keySet())
					{
						if(setString.contains(key))
						{
							if(dependency.get(key).contains(currentAttribute))
								toProcess = false;
						}
					}
					if(toProcess)
					{
						query+= (" WHERE t1." + currentAttribute + " <> " + "t2."+currentAttribute+";");
						rs = connection.prepareStatement(query).executeQuery();
						if(!rs.next())
						{
							if(fileEmpty)
								printWriter.write(setString+ "->" + currentAttribute);
							else
								printWriter.write("\n"+setString+ "->" + currentAttribute);
							fileEmpty = false;
							
							Set<String> rhs;
							if(dependency.containsKey(setString))
							{
								rhs = dependency.get(setString);
							}
							else
								rhs = new HashSet<>();
							
							rhs.add(currentAttribute);
							dependency.put(setString,rhs);	
						}
						rs.close();
					}
					query = "";
				}
			}
		}
		printWriter.close();
		fileWriter.close();
	}

	public static void readInputFromCommandLine(String args[])
	{
		url = args[0];
		userName = args[1];
		password = args[2];
		relations = args[3];
		outputFileNames = args[4];
	}

	public static void connectToSQL() throws SQLException
	{
		connection = (Connection) DriverManager
				.getConnection(url, userName, password);
	}

	public static void setupDBConnection()
	{
		String sql = "USE" + "";
	}
	
	public static void findAttributes(int relationNumber) throws SQLException
	{
		String query ="SELECT * FROM "+relation[relationNumber]+" limit 1;";
		rs = connection.prepareStatement(query).executeQuery();
		ResultSetMetaData rsmd = (ResultSetMetaData) rs.getMetaData();
		//Set<String> att = new HashSet<>();
		setOfAttributes = new HashSet<>();
		for(int i = 1 ; i <=rsmd.getColumnCount();i++)
		{
			setOfAttributes.add(rsmd.getColumnName(i));
		}
		int i = 0;
		attributes = new String[setOfAttributes.size()];
		numberOfAttributes = setOfAttributes.size();
		System.out.println(setOfAttributes);

		for(String column : setOfAttributes)
		{
			attributes[i] = column;
			i++;
		}		
	}

	public static void main(String[] args) throws SQLException, IOException
	{
		readInputFromCommandLine(args);
		connectToSQL();
		
		ReadRelations();
		
		for(int i = 0 ; i < relation.length;i++)
		{
			findAttributes(i);
			naiveFDDApproach(i);
		}
		connection.close();
	}
	
}

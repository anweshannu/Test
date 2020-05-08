// Generic framework program.
import java.sql.*;
import java.util.Scanner;
import java.util.ArrayList;

public class Framework
{
	public static void main(String argv[])
	{
		String table_name;
		if (argv.length == 1 )
		{
			table_name = argv[0];
		}
		else
		{
			Scanner scan = new Scanner(System.in);
			System.out.print("Please enter table name: ");
			table_name = scan.next();
		}
		CRUD crud = new CRUD(table_name);
		if (crud.con != null)
		{
			crud.displayMenu();
		}
	}
}

class CRUD
{
	private static String url = "jdbc:mysql://165.22.14.77/dbAnwesh?autoReconnect=true&useSSL=false";
	private static String user = "root";
	private static String password = "pwd";

	public static Connection con = null;
	public static Statement st = null;
	public static ResultSet rs = null;
	public static ResultSetMetaData metadata = null;
	public static String user_input = null;
	static int choice, columnCount, counter;
	static String sql_query, input_id, table_name, select_query, heading;
	static ArrayList<String> columnNames = new ArrayList<String>();
	static Scanner scan = new Scanner(System.in);
	static boolean no_status_column = true;

	public CRUD(String table_name)
	{
		try
		 {
		 	this.table_name = table_name;
			con = DriverManager.getConnection(url, user, password);
			getColumnNames();
		 }

		catch (SQLException e)
		 {
		 	System.out.println("Database Connection failed.");
		 	System.out.println("Reason: " + e.getMessage());
		 }
	}

	public static void getColumnNames()
	{
		try
		{
			select_query = "select * from " + table_name;
			executeSqlQuery(select_query, 'S');
			metadata = rs.getMetaData();
			columnCount = metadata.getColumnCount();
			for (int i = 0; i < columnCount; i ++) 
			{
				columnNames.add(i, metadata.getColumnName(i+1));
			}
			if(columnNames.get(columnNames.size() - 1).contains("tatus"))
			{
				if(columnCount > 1)
				{
					no_status_column = false;
				}

			}
			if (no_status_column)
			{
				System.out.println("Status column not found in table! please add status column at the end of the table column.");
				con = null;
			}
		}
		catch(Exception e)
		{
			displayExceptionMessage();
		}
	}

	public static void displayMenu()
	{
		while(true)
		{
			String welcome = "Welcome to " + table_name + " database";
			drawLine(welcome, "-");
			System.out.print("1. Add " + table_name + " details.\n2. Display all " + table_name + " details.\n3. Search an " + table_name + ".\n4. Update " + table_name + " details.\n5. Delete " + table_name + " details.\n6. Exit.\nEnter your choice: ");
			
			switch (takeChoiceFromUser('M'))
			{
				case 1: create();
						break;
				case 2: read();
						break;
				case 3: search('S');
						break;
				case 4: update();
						break;
				case 5: delete();
						break;
				case 6: terminateProgram();
				default: displayInvalidEntryMessage();
			}
			System.out.println();
		}
	}

	public static void terminateProgram()
	{
		try
		{
			con.close();
			scan.close();
			System.out.print("\033[H\033[2J");
			System.out.println("Exited Successfully.");
			System.exit(0);
		}
		catch(Exception e)
		{
			displayExceptionMessage();
		}
	} 

	public static void drawLine(String message, String symbol)
	{
		System.out.println(message);
		for(int i = 0; i < message.length(); i++)
		{
			System.out.print(symbol);
		}
		System.out.println();
	}

	public static void create()
	{
		scan.nextLine();
		sql_query = "insert into " + table_name + " values(";
		for (int i = 0; i < columnNames.size() - 1; i++)
		{
			sql_query = sql_query + "'" + takeStringInputFromUser("Enter " + columnNames.get(i) + ": ") + "'";
			if(i < columnNames.size() - 2)
			{
				sql_query = sql_query + "," ;
			}
		}
		sql_query = sql_query + ", 'A')";
		// System.out.println(sql_query);
		if (executeSqlQuery(sql_query, 'C') > 0)
		{
			System.out.println("\n-- Details added successfully. --");
		}
	}

	public static void read()
	{	
		executeSqlQuery(select_query, 'S');
		printDetails();
	}

	public static void printDetails()
	{
		counter = 0;
		try
		{
			counter = 0;
			heading = "";
			System.out.println();
			while(rs.next())
			{
				if(rs.getString(columnNames.size()).equals("A"))
				{
					counter++;
					if (counter == 1)
					{
						for (counter = 0; counter < columnCount - 1; counter ++)
						{
							heading = heading + String.format("%-20s ", columnNames.get(counter));
						}
						drawLine(heading, "-");
					}
					for (counter = 1; counter < columnCount; counter ++)
					{
						System.out.print(String.format("%-20s ", rs.getString(counter)));
					}
					System.out.println();
				}
			}
			if (counter == 0)
			{
				displayNoRecordFoundMessage();
			}
		}
		catch(Exception e)
		{
			displayNoRecordFoundMessage();
		}
	}

	public static int executeSqlQuery(String sql_query, char mode)
	{
		int rows_effected = 0;
		try
		{
			st = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
			if(mode == 'S')
			{
				rs = st.executeQuery(sql_query);
				if(rs.next())
				{
					rs.beforeFirst();
					rows_effected = 1;
				}

			}
			else
			{
				rows_effected = st.executeUpdate(sql_query);
			}
		}
		catch(SQLException e)
		{
			// e.printStackTrace();
			if (e.getMessage().contains("doesn't exist") || e.getMessage().contains("check the manual"))
			{
				System.out.println("Please enter a valid table name.");
				con = null;
				System.exit(0);
			}
			else if(e.getMessage().contains("Duplicate entry"))
			{
				counter = 0;
				String msg = (String) e.getMessage();
				for(int i = 0; i < msg.length() - 1; i++)
				{
					if(msg.charAt(i) == ' ')
					{
						counter++;
						if(counter == 2)
						{
							break;
						}
						System.out.print(" ");
					}
					else
					{
						System.out.print(msg.charAt(i));
					}
				}
				System.out.println(" found please try again with unique entry.");
			}
			else if(e.getMessage().contains("Communication"))
			{
				System.out.println("Connection with database has been lost please try again.");
			}
			else
			{
				System.out.println(e.getMessage());
			}
		}
		return rows_effected;
	}

	public static int search(char mode)
	{
		scan.nextLine();
		input_id = takeStringInputFromUser("Enter " + table_name + " Id: ");
		sql_query = "select * from " + table_name + " where " + columnNames.get(0) + " = '" + input_id + "'";
			if (executeSqlQuery(sql_query, 'S') == 1)
			{
				if(mode == 'S')
				{
					printDetails();
				}
				else
				{
					return 1;
				}
			}
		return 0;
	}

	public static void update()
	{
		if(search('U') == 1)
		{
			drawLine("Select a field to update:", "-");
			for(int index = 1; index < columnCount - 1; index++)
			{
				System.out.println((index) + ". " + columnNames.get(index) );
			}
			System.out.print("Enter your choice: ");
			int update_choice = takeChoiceFromUser('U');
			if(update_choice > 0 && update_choice < columnCount - 1)
			{
				scan.nextLine();
				sql_query = "update " + table_name + " set " + columnNames.get(update_choice) + " = '"+ takeStringInputFromUser("Enter " + columnNames.get(update_choice) + " to update: ") + "' where " + columnNames.get(0) + " = '" + input_id + "'";
				if (executeSqlQuery(sql_query, 'U') > 0)
				{
					System.out.println("\n" + columnNames.get(update_choice) + " updated.");
				}
			}
			else
			{
				displayInvalidEntryMessage();
			}
		}
	}

	public static void delete()
	{
		if(search('D') == 1)
		{
			sql_query = "update " + table_name + " set " + columnNames.get(columnCount - 1) + " = 'I' where "+ columnNames.get(0) +" = '" + input_id + "'";
			if (executeSqlQuery(sql_query, 'D') > 0)
			{
				System.out.println("\n" + table_name + " details deleted.");
			}
			
		}
	}

	public static int takeChoiceFromUser(char mode)
	{
		try
		{
			choice = scan.nextInt();
		}
		catch (Exception e)
		{
			System.out.println("\nEnter only numbers.\n");
			if(mode == 'M')
			{
				scan.nextLine();
				displayMenu();
			}
		}
		return choice;
	}

	public static void displayNoRecordFoundMessage()
	{
		System.out.println("\n-- No " + table_name + " details found. --");
	}

	public static void displayExceptionMessage()
	{
		System.out.println("\nException occured.");
	}

	public static String takeStringInputFromUser(String message)
	{
		System.out.print(message);
		user_input = scan.nextLine();
		return user_input;
	}

	public static void displayInvalidEntryMessage()
	{
		System.out.println("\nInvalid choice.");
	}
}
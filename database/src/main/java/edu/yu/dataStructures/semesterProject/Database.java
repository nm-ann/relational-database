package edu.yu.dataStructures.semesterProject;

import java.util.*;

import edu.yu.cs.dataStructures.fall2016.SimpleSQLParser.*;
/**
 * @author nannenbe@mail.yu.edu
 * Represents a database, which holds tables
 * Uses a sql parser to parse sql queries
 * Takes the query object the sql parser calls the corresponding method on the appropriate table
 */
public class Database {
	private Map<String, Integer> map; //maps table names to their respective indices
	private List<Table> tables;       //the list of tables in the database
	
	public Database() {
		this.map = new HashMap<String, Integer>();
		this.tables = new ArrayList<Table>();
	}
	//executes the sql query by creating a object that inherits from SQL Query 
	//then it calls an appropriate method, depending on the specific class of the object
	//will return a false result set with an error message if the query cannot be parsed
	public ResultSet<?> execute(String SQL) {
		SQLParser parser = new SQLParser();
		ResultSet<?> resultSet;
		SQLQuery query;
		try {
			query = parser.parse(SQL);
		}
		catch(Exception e) {
			resultSet = new ResultSet<Boolean>(false);
			resultSet.setErrorMsg("invalid sql query");
			return resultSet;
		}
		
		if(query.getClass().getSimpleName().equals("CreateTableQuery")){ resultSet = createTable((CreateTableQuery)query); }
		else if(query.getClass().getSimpleName().equals("InsertQuery")){ resultSet = insert((InsertQuery)query); }
		else if(query.getClass().getSimpleName().equals("SelectQuery")){ resultSet = select((SelectQuery)query); }
		else if(query.getClass().getSimpleName().equals("UpdateQuery")){ resultSet = update((UpdateQuery)query); }
		else if(query.getClass().getSimpleName().equals("DeleteQuery")){ resultSet = delete((DeleteQuery)query); }
		else if(query.getClass().getSimpleName().equals("CreateIndexQuery")){ resultSet = createIndex((CreateIndexQuery)query); }
		else{
			resultSet = new ResultSet<Boolean>(false); 
			resultSet.setErrorMsg("invalid sql query");
		}
		
		return resultSet;
	}
	//creates a new table, which is added to the list of tables and the map of table names to table
	//then returns a result set
	private ResultSet<?> createTable(CreateTableQuery query) {
		Table newTable = new Table(query);
		ResultSet<?> resultSet = newTable.loadColumns(query);
		if(resultSet.getResults() != null){
			tables.add(newTable);
			map.put(newTable.getName(), tables.size()-1);		
		}
		
		return resultSet;
	}
	//inserts a new row in a given table and returns a result set that shows whether or not it was successful
	private ResultSet<Boolean> insert(InsertQuery query) {
		int index;
		try {
			index = map.get(query.getTableName());
		}
		catch(Exception e) {
			ResultSet<Boolean> resultSet = new ResultSet<Boolean>(false);
			resultSet.setErrorMsg("table: " + query.getTableName() + " does not exist");
			return resultSet;
		}
		
		Table table = tables.get(index);
		return table.insert(query);
	}
	//selects specific rows in the table and returns them as a result set
	private ResultSet<?> select(SelectQuery query) {
		int index;
		try {
			//only works for one table
			index = map.get(query.getFromTableNames()[0]);
		}
		catch(Exception e) {
			ResultSet<Boolean> resultSet = new ResultSet<Boolean>(false);
			resultSet.setErrorMsg("table: " + query.getFromTableNames()[0] + " does not exist");
			return resultSet;
		}
		
		Table table = tables.get(index);
		return table.select(query);
	}
	//updates specific rows in the table and returns them as a result set
	private ResultSet<Boolean> update(UpdateQuery query) {
		int index;
		try {
			index = map.get(query.getTableName());
		}
		catch(Exception e) {
			ResultSet<Boolean> resultSet = new ResultSet<Boolean>(false);
			resultSet.setErrorMsg("table: " + query.getTableName() + " does not exist");
			return resultSet;
		}
		
		Table table = tables.get(index);
		return table.update(query);
	}
	//deletes specific rows in the table and returns whether or not it was successful
	private ResultSet<Boolean> delete(DeleteQuery query) {
		int index;
		try {
			index = map.get(query.getTableName());
		}
		catch(Exception e) {
			ResultSet<Boolean> resultSet = new ResultSet<Boolean>(false);
			resultSet.setErrorMsg("table: " + query.getTableName() + " does not exist");
			return resultSet;
		}
		
		Table table = tables.get(index);
		return table.delete(query);
	}
	//creates an index on a specific row to make look-up quicker
	private ResultSet<Boolean> createIndex(CreateIndexQuery query) {
		int index;
		try {
			index = map.get(query.getTableName());
		}
		catch(Exception e) {
			ResultSet<Boolean> resultSet = new ResultSet<Boolean>(false);
			resultSet.setErrorMsg("table: " + query.getTableName() + " does not exist");
			return resultSet;
		}
		
		Table table = tables.get(index);
		return table.createIndex(query);
	}
	
	List<Table> getTables() {
		return tables;
	}
}
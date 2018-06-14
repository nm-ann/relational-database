package edu.yu.dataStructures.semesterProject;

import java.util.*;

import edu.yu.cs.dataStructures.fall2016.SimpleSQLParser.*;
/**
 * @author nannenbe@mail.yu.edu
 * represents a table in a database
 */
public class Table {
	private String name;               				//name of the table
	private Map<String, Integer> map;  				//maps column names to their respective indices
	private List<List<Cell<?>>> table; 				//the table itself, represented as a list of lists
	private BTree<String, List<Cell<?>>>[] btrees;	//array of BTrees whose indices correspond to the rows they index
	
	Table(CreateTableQuery query) {
		map = new HashMap<String, Integer>();
		table = new ArrayList<List<Cell<?>>>();
	}
	
	//adds the columns from the query to the table as null-valued Cell objects in the first row
	ResultSet<?> loadColumns(CreateTableQuery query) {
		List<List<Cell<?>>> resultTable = null;
		try {
			this.name = query.getTableName();
	
			ColumnDescription[] descriptions = query.getColumnDescriptions();
			btrees = new BTree[descriptions.length];
			table.add(new ArrayList<Cell<?>>());
			for(int i = 0; i < descriptions.length; i++) {
				//if the query's primary key column equals the description's column, this column is the primary key
				table.get(0).add(new Cell<Object>(null, descriptions[i], true, query.getPrimaryKeyColumn().equals(descriptions[i])));
				if(query.getPrimaryKeyColumn().equals(descriptions[i])) {
					btrees[i] = new BTree<String, List<Cell<?>>>();
				}
				map.put(descriptions[i].getColumnName(), i);
			}
			
			resultTable = table;
		}
		catch(Exception e) {
			ResultSet<Boolean> resultSet = new ResultSet<Boolean>(false);
			resultSet.setErrorMsg(e.getMessage());
			return resultSet;
		}
		
		return new ResultSet<Cell<?>>(resultTable);
	}
	
	//inserts a new row of values, based on the query, and then returns a result set
	//if any new cell is given invalid data for its column description, it will throw an InputMismatchException and 
	//the ResultSet will be false
	ResultSet<Boolean> insert(InsertQuery query) {
		Cell<?>[] cells = new Cell[table.get(0).size()];
		//adds the values to an array-this way, whatever was left out of the insert query can be inserted as an null-valued Cell
		try {
			createCellArray(cells, query);
		}
		catch(InputMismatchException e) {
			ResultSet<Boolean> resultSet = new ResultSet<Boolean>(false);
			resultSet.setErrorMsg(e.getMessage());
			return resultSet;
		}
		for(int i = 0; i < cells.length; i ++) {
			if(cells[i] == null) {
				Cell<?> column = table.get(0).get(i);
				try {
					cells[i] = new Cell<Object>(null, column.getDescription(), false, column.isPrimaryKey());
				}
				catch(InputMismatchException e) {
					ResultSet<Boolean> resultSet = new ResultSet<Boolean>(false);
					resultSet.setErrorMsg(e.getMessage());
					return resultSet;
				}
			}
		}
		insertBTree(cells);
		return new ResultSet<Boolean>(true);
	}
	//adds all cells in indexed columns to their appropriate btree
	private void insertBTree(Cell<?>[] cells) {
		table.add(new ArrayList<Cell<?>>(Arrays.asList(cells)));
		for(Cell<?> cell : table.get(table.size() - 1)) {
			if(btrees[map.get(cell.getColumnName())] != null) {
				btrees[map.get(cell.getColumnName())].put(table.get(table.size() - 1).get(map.get(cell.getColumnName())).getValueToString(), 
						table.get(table.size() - 1)); 
			}
		}
	}
	//creates an array of Cells which will then be inserted into the table
	//if a cell cannot be created properly, it will throw an InputMismatchException
	private void createCellArray(Cell<?>[] cells, InsertQuery query) throws InputMismatchException {
		for(ColumnValuePair cvp : query.getColumnValuePairs()) {
			if(map.get(cvp.getColumnID().getColumnName()) == null) {
				throw new InputMismatchException("column: " + cvp.getColumnID().getColumnName() + " does not exist");
			}
			
			String columnName = cvp.getColumnID().getColumnName();
			String value = cvp.getValue();
			int index = map.get(columnName);
			
			Cell<?> column = table.get(0).get(index);
			//creates a new Cell
			//converts the String "value" into an object of the appropriate type that contains that value
			Cell<?> newCell = new Cell<Object>(convertValue(value, column), column.getDescription(), false, column.isPrimaryKey());
			//if the given cell does not have to be unique, or must be unique and is unique, it's added to the cells array
			isUnique(newCell, cells, index, table.size());
		}
	}
	//converts a value from a string to an object of the appropriate type
	private Object convertValue(String value, Cell<?> column) throws InputMismatchException{
		try{
			switch(column.getDataType()) {
				case "Integer":
					return Integer.parseInt(value);
				case "Varchar":
					return value;
				case "Decimal":
					return Double.parseDouble(value);
				case "Boolean":
					return Boolean.parseBoolean(value);
				default: 
					return null;
			}
		}
		catch(Exception e) {
			throw new InputMismatchException("cannot convert " + "string \"" + value + "\" to " + column.getDataType() + " for column: " + column.getColumnName());
		}
	}
	//only allows the new cell to be added to the table if does not have to be unique, or must be unique and is unique
	//otherwise, it will throw an InputMismatchException
	private void isUnique(Cell<?> newCell, Cell<?>[] cells, int index, int row) throws InputMismatchException {
		for(int i = 1; i < table.size(); i++) {
			Cell<?> oldCell = table.get(i).get(index);
			if(row < table.size() && row == i) {
				continue;
			}
			if(newCell.isPrimaryKey() || newCell.getDescription().isUnique()) {
				if((newCell.getValue() != null && oldCell.getValue() != null)) {
					if(newCell.getDataType().equals("Varchar")) {
						if(((String) newCell.getValue()).toLowerCase().equals(((String) oldCell.getValue()).toLowerCase())) {
							throw new InputMismatchException("the value " + newCell.getValue() + " is not a unique value and the "+ newCell.getColumnName() +
									" column must be unique");
						}
					}
					else if(newCell.getValue().equals(oldCell.getValue())) {
						throw new InputMismatchException("the value " + newCell.getValue() + " is not a unique value and the "+ newCell.getColumnName() +
								" column must be unique");
					}
				}
			}
		}
		cells[index] = newCell;
	}
	
	//this selects specific columns in the table and returns them as a result set
	ResultSet<?> select(SelectQuery query) {
		Selector selector = new Selector(query); 
		return selector.select(table, map, btrees);
	}
	
	//this updates a specific entries in the table
	//returns a results set that's true or false, depending on whether or not it was successful
	ResultSet<Boolean> update(UpdateQuery query) {
		List<Integer> rowIndices = new ArrayList<Integer>();
		try { 
			rowIndices = getWhereRows(query); 
			updateCells(query, rowIndices);
		}
		catch(Exception e) {
			ResultSet<Boolean> resultSet = new ResultSet<Boolean>(false);
			resultSet.setErrorMsg(e.getMessage());
			return resultSet;
		}
		
		return new ResultSet<Boolean>(true);
	}
	//get the indices of the rows to be affected by update
	private List<Integer> getWhereRows(SQLQuery query) {
		List<Integer> rowIndices = new ArrayList<Integer>();
		Condition condition;
		try { 
			condition = ((UpdateQuery) query).getWhereCondition();
		}
		catch(Exception e) {
			try {
				condition = ((DeleteQuery) query).getWhereCondition();
			}
			catch(Exception f) {
				throw new IllegalArgumentException("invalid sql query");
			}
		}
		//if there's no where, all rows of the given columns should be added to the result set
		if(condition == null) {
			for(int i = 1; i < table.size(); i++) {
				rowIndices.add(i);
			}
		}
		else {
			rowIndices.addAll(new Where().where(condition, table, map, btrees));
		}
		
		return rowIndices;
	}
	//updates the values in the cells
	private void updateCells(UpdateQuery query, List<Integer> rowIndices) throws Exception {
		for(ColumnValuePair cvp : query.getColumnValuePairs()) {
			for(int row : rowIndices) {
				if(map.get(cvp.getColumnID().getColumnName()) == null) {
					throw new InputMismatchException("column: " + cvp.getColumnID().getColumnName() + " does not exist");
				}
				String columnName = cvp.getColumnID().getColumnName();
				String value = cvp.getValue();
				int index = map.get(columnName);
				Cell<?> column = table.get(0).get(index);
				if(column.isUnique()) {
					//makes a mock new cell to see if the value is unique
					Cell<?> newCell = new Cell<Object>(convertValue(value, column), column.getDescription(), false, column.isPrimaryKey());
					Cell<?>[] cells = new Cell<?>[table.get(0).size()];
					if(value.equals(table.get(row).get(index).getValue())) {
						return;
					}
					isUnique(newCell, cells, index, row);
				}
				//if the given cell does not have to be unique, or must be unique and is unique, it's updated
				table.get(row).get(index).setValue(value);
			}
		}
	}
	
	//deletes specified rows in the database
	ResultSet<Boolean> delete(DeleteQuery query) {
		List<Integer> rowIndices = new ArrayList<Integer>();
		try { 
			rowIndices = getWhereRows(query); 
		}
		catch(Exception e) {
			ResultSet<Boolean> resultSet = new ResultSet<Boolean>(false);
			resultSet.setErrorMsg(e.getMessage());
			return resultSet;
		}
		deleteCells(rowIndices);
		return new ResultSet<Boolean>(true);
	}
	//goes through each relevant row and deletes them
	private void deleteCells(List<Integer> rowIndices) {
		for(int row : rowIndices) {
			for(int i = 0; i < btrees.length; i++) {
				if(btrees[i] == null) {
					continue;
				}
				btrees[i].deleteValue(table.get(row).get(i).getValueToString(), table.get(row));
			}
		}
		//every time the smallest index is removed, the rows will shift down. That shift needs to be compensated for
		int removeOffset = 0;
		for(int row : rowIndices) {
			table.remove(row - removeOffset);
			removeOffset++;
		}
	}
	
	//creates a BTree associated to this column to index its values for fast look-up speed
	ResultSet<Boolean> createIndex(CreateIndexQuery query) { 
		if(map.get(query.getColumnName()) == null) {
			ResultSet<Boolean> resultSet = new ResultSet<Boolean>(false);
			resultSet.setErrorMsg("column: " + query.getColumnName() + " does not exist");
			return resultSet;
		}
		int columnIndex = map.get(query.getColumnName());
		if(btrees[columnIndex] != null) {
			ResultSet<Boolean> resultSet = new ResultSet<Boolean>(false);
			resultSet.setErrorMsg("this column is already indexed");
			return resultSet;
		}
		btrees[columnIndex] = new BTree();
		try{
			loadIndices(columnIndex);
		}
		catch(IllegalArgumentException e) {
			ResultSet<Boolean> resultSet = new ResultSet<Boolean>(false);
			resultSet.setErrorMsg(e.getMessage());
			return resultSet;
		}
		
		return new ResultSet<Boolean>(true);
	}
	//loads all of the appropriate cell values into the BTree
	private void loadIndices(int columnIndex) {
		for(int i = 1; i < table.size(); i++) {
			btrees[columnIndex].put(table.get(i).get(columnIndex).getValueToString(), table.get(i));
		}
	}
	
	String getName() {
		return name;
	}
	BTree<String, List<Cell<?>>>[] getBTrees() {
		return btrees;
	}
	List<List<Cell<?>>> getRows() {
		return table;
	}
}

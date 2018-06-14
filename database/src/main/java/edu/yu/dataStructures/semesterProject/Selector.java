package edu.yu.dataStructures.semesterProject;

import java.util.*;

import net.sf.jsqlparser.JSQLParserException;
import edu.yu.cs.dataStructures.fall2016.SimpleSQLParser.ColumnDescription;
import edu.yu.cs.dataStructures.fall2016.SimpleSQLParser.ColumnID;
import edu.yu.cs.dataStructures.fall2016.SimpleSQLParser.CreateTableQuery;
import edu.yu.cs.dataStructures.fall2016.SimpleSQLParser.SQLParser;
import edu.yu.cs.dataStructures.fall2016.SimpleSQLParser.SelectQuery;
import edu.yu.cs.dataStructures.fall2016.SimpleSQLParser.SelectQuery.FunctionInstance;
import edu.yu.cs.dataStructures.fall2016.SimpleSQLParser.SelectQuery.FunctionName;

public class Selector {
	private final SelectQuery query;          	//the SQLQuery that was passed to the selector
	private List<Integer> columnIndices;     	//list of the indices of the columns selected by the normal select
	private List<Integer> nonFunctionColumns;	//list of the indices of columns that are not select functions. Used for distinct()
	private List<Integer> rowIndices;         	//list of the indices of the rows selected
	private Map<ColumnID, Integer> functionMap; //maps the function instance selected by the select functions to their 
												//indices in the table
	
	Selector(SelectQuery query) {
		this.query = query;
		columnIndices = new ArrayList<Integer>();
		nonFunctionColumns = new ArrayList<Integer>();
		functionMap = new HashMap<ColumnID, Integer>();
		rowIndices = new ArrayList<Integer>();
		rowIndices.add(0);
	}
	
	//this selects specific columns in the table and returns them as a result set
	ResultSet<?> select(List<List<Cell<?>>> table, Map<String, Integer> map, BTree<String, List<Cell<?>>>[] btrees) {
		try {
			getFunctionColumns(table, map);
			getSelectColumns(table, map);
			if(columnIndices.size() == 0) {
				throw new InputMismatchException("no columns were found");
			}
			rowIndices.addAll(new Where().where(query.getWhereCondition(), table, map, btrees));
		}
		catch(InputMismatchException e) {
			ResultSet<Boolean> resultSet = new ResultSet<Boolean>(false);
			resultSet.setErrorMsg(e.getMessage());
			return resultSet;
		}
		//if there's no where, all rows of the given columns should be added to the result set
		if(query.getWhereCondition() == null) {
			for(int i = 1; i < table.size(); i++) {
				rowIndices.add(i);
			}
		}
		if(query.isDistinct()) {
			rowIndices = distinct(table, map, nonFunctionColumns);
		}
		if(query.getOrderBys().length != 0) {
			orderBy(table, map);
		}
		//addColumns(columnIndices, rowIndices)
		return new ResultSet<Cell<?>>(selectFunctions(table, map));
	}
	
	//find which columns of the select query are used for select functions and adds them to a map
	private void getFunctionColumns(List<List<Cell<?>>> table, Map<String, Integer> map) throws InputMismatchException {
		for(FunctionInstance function : query.getFunctions()) {
			int index;
			try {
				index = map.get(function.column.getColumnName());
			}
			catch(Exception e) {
				throw new InputMismatchException("column: " + function.column.getColumnName()  + " does not exist");
			}
			functionMap.put(function.column, index);
		}
	}
	//find which columns are selected as part of the select query and adds their indices to an arraylist
	//updates the function map to reflect which index in that arraylist contains the column of the function
	private void getSelectColumns(List<List<Cell<?>>> table, Map<String, Integer> map) throws InputMismatchException {
		for(ColumnID columnId : query.getSelectedColumnNames()) {
			if(columnId.getColumnName().equals("*")) {
				for(int i = 0; i < table.get(0).size(); i++) {
					columnIndices.add(i);
					nonFunctionColumns.add(i);
				}
				break;
			}
			int index;
			try {
				index = map.get(columnId.getColumnName());
			}
			catch(Exception e) {
				throw new InputMismatchException("column: " + columnId.getColumnName()  + " does not exist");
			}
			columnIndices.add(index);
			if(functionMap.containsKey(columnId)) {
				functionMap.put(columnId, columnIndices.size()-1);
			}
			else {
				nonFunctionColumns.add(index);
			}
		}
	}
	
	//narrows down the selected row indices to only those rows whose selected columns are distinct or are the 
	//first appearances of non-distinct rows
	private List<Integer> distinct(List<List<Cell<?>>> table, Map<String, Integer> map, List<Integer> columns) {
		Map<String, Integer> distinctMap = new HashMap<String, Integer>();
		List<Integer> distinctRows = new ArrayList<Integer>();
		distinctRows.add(0);
		for(int i = 1; i < rowIndices.size(); i++) {
			String valuesAsString = "";
			for(Integer column : columns) {
				valuesAsString += table.get(i).get(column).getValueToString();
			}
			if(!distinctMap.containsKey(valuesAsString)) {
				distinctMap.put(valuesAsString, i);
				distinctRows.add(i);
			}
		}
		return distinctRows;
	}
	
	//orders the rows in the resultSet either in ascending or descending order, based on the value in a specified column
	private void orderBy(List<List<Cell<?>>> table, Map<String, Integer> map) {
		String[] values = new String[rowIndices.size()-1];
		int index = map.get(query.getOrderBys()[0].getColumnID().getColumnName());
		for(int i = 0; i < rowIndices.size()-1; i++) {
			//i + 1 is going to be used for the following calculations because rowIndices' first row is for the column cells
			values[i] = table.get(rowIndices.get(i + 1)).get(index).getValueToString();
		}
		mergeSort(values, 0, values.length - 1);
		if(query.getOrderBys()[0].isDescending()) {
			reverseRows(values, 0, values.length - 1);
		}
		multiOrderBy(values, 1, table, map);
	}
	//orders the rows in the resultsSet, based on subsequent order by's
	private void multiOrderBy(String[] prevValues, int curOrderBy, List<List<Cell<?>>> table, Map<String, Integer> map) {
		if(curOrderBy >= query.getOrderBys().length) {
			return;
		}
		String[] values = new String[rowIndices.size() - 1];
		int index = map.get(query.getOrderBys()[curOrderBy].getColumnID().getColumnName());
		int slow = 0;
		int fast = 1;
		boolean match = false;
		while(fast < prevValues.length) {
			if(prevValues[slow] != null && prevValues[fast] != null & prevValues[slow].equals(prevValues[fast])) {
				if(match == false) {
					values[slow] = table.get(rowIndices.get(slow + 1)).get(index).getValueToString();
				}
				values[fast] = table.get(rowIndices.get(fast + 1)).get(index).getValueToString();
				match = true;
				if(fast + 1 == prevValues.length) {
					match = false;
					mergeSort(values, slow, fast);
					if(query.getOrderBys()[curOrderBy].isDescending()) {
						reverseRows(values, slow, fast);
					}
				}
			}
			else {
				if(match) {
					match = false;
					mergeSort(values, slow, fast - 1);
					if(query.getOrderBys()[curOrderBy].isDescending()) {
						reverseRows(values, slow, fast - 1);
					}
				}
				slow = fast;
			}
			fast++;
		}
		multiOrderBy(values, curOrderBy + 1, table, map);
	}
	//based on the code from http://www.vogella.com/tutorials/JavaAlgorithmsMergesort/article.html
	//takes an array of values whose indices correspond with their row index
	//then sorts them using a merge sort so the values will be put in order and the array list of row indices 
	//will change to reflect that order
	private void mergeSort(String[] values, int left, int right) {
		if(left < right) {
			int middle = left + (right - left)/2;
			mergeSort(values, left, middle);
			mergeSort(values, middle + 1, right);
			merge(values, left, middle, right);
		}
	}
	private void merge(String[] values, int left, int middle, int right) {
		String[] helpers = new String[right + 1];
		int[] indices = new int[right + 1];
		for(int i = left; i <= right; i++) {
			helpers[i] = values[i];
		}
		for(int i = left; i <= right; i++) {
			indices[i] = rowIndices.get(i+1);
		}
		int i = left;
		int j = middle + 1;
		int k = left;
		while(i <= middle && j <= right) {
			if(helpers[i].compareTo(helpers[j]) <= 0) {
				values[k] = helpers[i];
				rowIndices.set(k + 1, indices[i]);
				i++;
			}
			else {
				values[k] = helpers[j];
				rowIndices.set(k + 1, indices[j]);
				j++;
			}
			k++;
		}
		while(i <= middle) {
			values[k] = helpers[i];
			rowIndices.set(k + 1, indices[i]);
			k++;
			i++;
		}
	}
	//reverses the row indices and values array so the values are in descending order
	private void reverseRows(String[] values, int left, int right) {
		for(int i = left; i < (right/2) + 1; i++) {
			int tempInt = rowIndices.get(i + 1);
			String tempString = values[i];
			rowIndices.set(i + 1, rowIndices.get(right - i + 1));
			rowIndices.set(right - i + 1, tempInt);
			values[i] = values[right - i];
			values[right - i] = tempString;
		}
	}
	
	//returns a table of columns created by the select functions in the query
	private List<List<Cell<?>>> selectFunctions(List<List<Cell<?>>> table, Map<String, Integer> map) throws InputMismatchException {
		ArrayList<FunctionInstance> functions = query.getFunctions();
		List<List<Cell<?>>> resultTable = new ArrayList<List<Cell<?>>>();
		int rowsInResultTable = rowIndices.size();
		if(columnIndices.size() == 1 && functionMap.size() == 1) {
			rowsInResultTable = 2;
		}
		for(int i = 0; i < rowsInResultTable; i++) {
			resultTable.add(new ArrayList<Cell<?>>());
			for(int j = 0; j < columnIndices.size(); j++) {
				resultTable.get(i).add(null);
			}
		}
		for(int i = 0; i < columnIndices.size(); i++) {
			resultTable.get(0).set(i, table.get(0).get(columnIndices.get(i)));
		}
		for (FunctionInstance function : functions) {
			int column = functionMap.get(function.column);
			if(function.function == FunctionName.AVG) { avg(table, map, resultTable, column, function.isDistinct); }
			else if(function.function == FunctionName.COUNT) {
				//this should not ever throw a JSQLParserException, as the sql query is hard coded. None the less this here
				//in case something gets changed
				try { count(table, map, resultTable, column, function.isDistinct); } 
				catch (JSQLParserException e) { e.printStackTrace(); }
			}
			else if(function.function == FunctionName.MAX) { max(table, map, resultTable, column, function.isDistinct); }
			else if(function.function == FunctionName.MIN) { min(table, map, resultTable, column, function.isDistinct); }
			else if(function.function == FunctionName.SUM) { sum(table, map, resultTable, column, function.isDistinct); }
		}
		return addColumns(resultTable, table);
	}
	//adds a column of cells, each containing the average value of selected the cells in the row, to the result table
	private void avg(List<List<Cell<?>>> table, Map<String, Integer> map, List<List<Cell<?>>> resultTable, int column, boolean distinct) {
		int index = columnIndices.get(column);
		if(table.get(0).get(index).getDataType().equals("Boolean") || table.get(0).get(index).getDataType().equals("String")){
			throw new InputMismatchException("cannot apply AVG function to " + table.get(0).get(index).getDataType());
		}
		int numRows = 1;
		double avg = 0;
		List<Integer> rows = new ArrayList<Integer>(rowIndices);
		List<Integer> columns = new ArrayList<Integer>();
		columns.add(index);
		if(distinct) { rows = distinct(table, map, columns); }
		while(numRows < rows.size()) {
			avg += (Double) table.get(rows.get(numRows)).get(index).getValue();
			numRows++;
		}
		avg = avg/(numRows-1);
		Cell<?> avgCell;
		if(table.get(0).get(index).getDataType().equals("Decimal")) {
			//in case the avg surpasses the allowed decimal length, it's given an initial value of 0.0, then its properties are 
			//modified to allow for a longer number
			avgCell = new Cell<Double>(0.0, table.get(0).get(index).getDescription(), false, false);
			avgCell.setDecimalLength(Double.toString(avg).split("\\.")[0].length(), Double.toString(avg).split("\\.")[1].length());
			avgCell.setValue(Double.toString(avg));
		}
		else {
			avgCell = new Cell<Integer>((int) avg, table.get(0).get(index).getDescription(), false, false);
		}
		avgCell.setColumnName("AVG(" + avgCell.getColumnName() + ")");
		for (int i = 1; i < resultTable.size(); i++) {
			resultTable.get(i).set(column, avgCell);
		}
		Cell<?> columnCell = table.get(0).get(index).copyOf();
		columnCell.setColumnName(avgCell.getColumnName());
		resultTable.get(0).set(column, columnCell);
	}
	//adds a column of cells, each containing the number of selected the cells in the row, to the result table
	private void count(List<List<Cell<?>>> table, Map<String, Integer> map, List<List<Cell<?>>> resultTable, int column, boolean distinct) throws JSQLParserException {
		int index = columnIndices.get(column);
		Cell<?> countCell = table.get(0).get(index);
		//to make a cell that contains the number of cells selected, that cell must be an integer cell
		//however, the column in question may be of a non-integer type. To deal with this, an artificial column description 
		//must be created to "pretend" the column type is integer
		ColumnDescription description = ((CreateTableQuery)new SQLParser().parse("CREATE TABLE YCStudent(PrimaryKey int, " + 
				countCell.getColumnName() + " int,  PRIMARY KEY (PrimaryKey));")).getColumnDescriptions()[0];
		int countNum = 0;
		List<Integer> rows = new ArrayList<Integer>(rowIndices);
		List<Integer> columns = new ArrayList<Integer>();
		columns.add(index);
		if(distinct) { rows = distinct(table, map, columns); }
		while(countNum < rows.size()-1) {
			countNum++;
		}
		countCell = new Cell<Integer>(countNum, description, false, false);
		countCell.setColumnName("COUNT(" + countCell.getColumnName() + ")");
		for (int i = 1; i < resultTable.size(); i++) {
			resultTable.get(i).set(column, countCell);
		}
		Cell<?> columnCell = table.get(0).get(index).copyOf();
		columnCell.setColumnName(countCell.getColumnName());
		resultTable.get(0).set(column, columnCell);
	}
	//adds a column of cells, each containing the maximum value of selected the cells in the row, to the result table
	private void max(List<List<Cell<?>>> table, Map<String, Integer> map, List<List<Cell<?>>> resultTable, int column, boolean distinct) {
		int index = columnIndices.get(column);
		String max = "";
		String type = table.get(0).get(index).getDataType();
		if(type.equals("Boolean")){ throw new InputMismatchException("cannot apply MAX function to " + type); }
		List<Integer> rows = new ArrayList<Integer>(rowIndices);
		List<Integer> columns = new ArrayList<Integer>();
		columns.add(index);
		if(distinct) { rows = distinct(table, map, columns); }
		for(int i = 1; i < rows.size(); i++) {
			Cell<?> cell = table.get(rows.get(i)).get(index);
			int compare = cell.compareValue(max);
			if(compare > 0) { max = cell.getValue().toString(); }
		}
		Cell<?> maxCell = null;
		if(type.equals("Varchar")) { maxCell = new Cell<String>(max, table.get(0).get(index).getDescription(), false, false); }
		else if(type.equals("Decimal")) { maxCell = new Cell<Double>(Double.parseDouble(max), table.get(0).get(index).getDescription(), false, false); }
		else if(type.equals("Integer")){ maxCell = new Cell<Integer>(Integer.parseInt(max), table.get(0).get(index).getDescription(), false, false); }
		maxCell.setColumnName("MAX(" + maxCell.getColumnName() + ")");
		for (int i = 1; i < resultTable.size(); i++) {
			resultTable.get(i).set(column, maxCell);
		}
		Cell<?> columnCell = table.get(0).get(index).copyOf();
		columnCell.setColumnName(maxCell.getColumnName());
		resultTable.get(0).set(column, columnCell);
	}
	//adds a column of cells, each containing the minimum value of selected the cells in the row, to the result table
	private void min(List<List<Cell<?>>> table, Map<String, Integer> map, List<List<Cell<?>>> resultTable, int column, boolean distinct) {
		int index = columnIndices.get(column);
		String min = "" + (char) 127;
		String type = table.get(0).get(index).getDataType();
		if(type.equals("Boolean")){ throw new InputMismatchException("cannot apply MAX function to " + type); }
		List<Integer> rows = new ArrayList<Integer>(rowIndices);
		List<Integer> columns = new ArrayList<Integer>();
		columns.add(index);
		if(distinct) { rows = distinct(table, map, columns); }
		for(int i = 1; i < rows.size(); i++) {
			Cell<?> cell = table.get(rows.get(i)).get(index);
			int compare = cell.compareValue(min);
			if(compare < 0) {
				min = cell.getValue().toString();
			}
		}
		Cell<?> minCell = null;
		if(type.equals("Varchar")) { minCell = new Cell<String>(min, table.get(0).get(index).getDescription(), false, false); }
		else if(type.equals("Decimal")) { minCell = new Cell<Double>(Double.parseDouble(min), table.get(0).get(index).getDescription(), false, false); }
		else if(type.equals("Integer")){ minCell = new Cell<Integer>(Integer.parseInt(min), table.get(0).get(index).getDescription(), false, false); }
		minCell.setColumnName("MIN(" + minCell.getColumnName() + ")");
		for (int i = 0; i < resultTable.size(); i++) {
			resultTable.get(i).set(column, minCell);
		}
		Cell<?> columnCell = table.get(0).get(index).copyOf();
		columnCell.setColumnName(minCell.getColumnName());
		resultTable.get(0).set(column, columnCell);
	}
	//adds a column of cells, each containing the sum of selected the cells in the row, to the result table
	private void sum(List<List<Cell<?>>> table, Map<String, Integer> map, List<List<Cell<?>>> resultTable, int column, boolean distinct) {
		int index = columnIndices.get(column);
		if(table.get(0).get(index).getDataType().equals("Boolean") || table.get(0).get(index).getDataType().equals("String")){
			throw new InputMismatchException("cannot apply SUM function to " + table.get(0).get(index).getDataType());
		}
		int numRows = 1;
		double sum = 0;
		List<Integer> rows = new ArrayList<Integer>(rowIndices);
		List<Integer> columns = new ArrayList<Integer>();
		columns.add(index);
		if(distinct) { rows = distinct(table, map, columns); }
		while(numRows < rows.size()) {
			try {sum += ( Double) table.get(rows.get(numRows)).get(index).getValue(); }
			catch(Exception e) { sum += (Integer) table.get(rows.get(numRows)).get(index).getValue(); }
			numRows++;
		}
		Cell<?> sumCell;
		if(table.get(0).get(index).getDataType().equals("Decimal")) {
			//in case the sum surpasses the allowed decimal length, it's given an initial value of 0.0, then its properties are 
			//modified to allow for a longer number
			sumCell = new Cell<Double>(0.0, table.get(0).get(index).getDescription(), false, false);
			sumCell.setDecimalLength(Double.toString(sum).split("\\.")[0].length(), 
					Double.toString(sum).split("\\.")[1].length());
			sumCell.setValue(Double.toString(sum));
		}
		else {
			sumCell = new Cell<Integer>((int) sum, table.get(0).get(index).getDescription(), false, false);
		}
		sumCell.setColumnName("SUM(" + sumCell.getColumnName() + ")");
		for (int i = 0; i < resultTable.size(); i++) {
			resultTable.get(i).set(column, sumCell);
		}
		Cell<?> columnCell = table.get(0).get(index).copyOf();
		columnCell.setColumnName(sumCell.getColumnName());
		resultTable.get(0).set(column, columnCell);
	}
	//adds the correct columns to the result set, given a list of columns and a list of rows
	private List<List<Cell<?>>> addColumns(List<List<Cell<?>>> resultTable, List<List<Cell<?>>> table) {
		if(rowIndices.size() == 0) {
			for(int i = 0; i < table.get(0).size(); i++) {
				resultTable.add(new ArrayList<Cell<?>>());
				resultTable.get(0).add(table.get(0).get(i));
			}
		}
		//if the only column is a select function column, there's only one cell, which all ready exists, 
		//so there's no need to fill the table in
		if(!(columnIndices.size() == 1 && functionMap.size() == 1))
			for(int i = 0; i < rowIndices.size(); i++) {
				for(int j = 0; j < columnIndices.size(); j++) {
					if(resultTable.get(i).get(j) == null) {
						resultTable.get(i).set(j, table.get(rowIndices.get(i)).get(columnIndices.get(j)));
					}
				}
			}
		return resultTable;
	}
}
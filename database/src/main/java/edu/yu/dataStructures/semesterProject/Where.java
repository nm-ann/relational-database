package edu.yu.dataStructures.semesterProject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.yu.cs.dataStructures.fall2016.SimpleSQLParser.ColumnID;
import edu.yu.cs.dataStructures.fall2016.SimpleSQLParser.Condition;
import edu.yu.cs.dataStructures.fall2016.SimpleSQLParser.Condition.Operator;

public class Where {
	
	Where() {
		
	}
	//accommodates the where sql function within select
	//returns a list of the rows where the given condition is true
	List<Integer> where(Condition condition, List<List<Cell<?>>> table, Map<String, Integer> map,
			BTree<String, List<Cell<?>>>[] btrees) throws InputMismatchException {
		List<Integer> rowIndices = new ArrayList<Integer>();
		if(condition == null) {
			return rowIndices;
		}
		//if the operator is and or or, the left and right conditions must be evaluated as where conditions and will return lists of qualifiying rows
		if(condition.getOperator().equals(Operator.AND) || condition.getOperator().equals(Operator.OR)) {
			//going to need to try and catch by this cast in case the user screwed something up
			List<Integer> leftCondition = where((Condition)condition.getLeftOperand(), table, map, btrees);
			List<Integer> rightCondition = where((Condition)condition.getRightOperand(), table, map, btrees);
			//if the operation is and, all overlapping rows must be returned
			if(condition.getOperator().equals(Operator.AND)) {
				leftCondition = retainOverlap(leftCondition, rightCondition);
				return leftCondition;
			}
			//if the operation is or, all rows must be returned
			else if(condition.getOperator().equals(Operator.OR)) {
				leftCondition = addWithoutDoubles(leftCondition, rightCondition);
				return leftCondition;
			}
		}
		//if the operation isn't an and or an or, the left operand must be a column and the right must be a value
		return whereCondition(condition, table, map, btrees);
	}
	//NEED TO DO DATA TYPE VALIDATION
	//iterates through each row in the table to see if it matches the condition
	//returns a list of all row indexes where the given column matches the condition
	private List<Integer> whereCondition(Condition condition, List<List<Cell<?>>> table, Map<String, 
			Integer> map, BTree<String, List<Cell<?>>>[] btrees) throws InputMismatchException {
		int columnIndex;
		try {
			columnIndex = map.get(((ColumnID) condition.getLeftOperand()).getColumnName());
		}
		catch(Exception e) {
			throw new InputMismatchException("column: " + ((ColumnID) condition.getLeftOperand()).getColumnName() + "does not exist");
		}
		if(btrees[columnIndex] != null) {
			return withBTree(condition, table, btrees[columnIndex]);
		}
		else {
			return withoutBTree(condition, table, columnIndex);
		}
	}
	//finds the appropriate rows, when the column is indexed
	//DOESN'T WORK WHEN THE WHERE CONDITION HAS DOUBLE QUOTES AROUND IT. CAN'T PROPERLY CAST TO STRING
	private List<Integer> withBTree(Condition condition, List<List<Cell<?>>> table, BTree<String, List<Cell<?>>> btree) throws InputMismatchException {
		List<List<Cell<?>>> rowObjects = null;
		List<Integer> rowIndices = new ArrayList<Integer>();
		if(condition.getOperator() == Operator.EQUALS) { rowObjects = btree.get((String)condition.getRightOperand()); }
		else if(condition.getOperator() == Operator.NOT_EQUALS) { 
			rowObjects = btree.getLessThan((String)condition.getRightOperand()); 
			rowObjects.addAll(btree.getGreaterThan((String)condition.getRightOperand()));
		}
		else if(condition.getOperator() == Operator.LESS_THAN) { 
			rowObjects = btree.getLessThan(((String)condition.getRightOperand())); }
		else if(condition.getOperator() == Operator.LESS_THAN_OR_EQUALS) { 
			rowObjects = btree.getLessThanOrEqual((((String)condition.getRightOperand()))); }
		else if(condition.getOperator() == Operator.GREATER_THAN) { 
			rowObjects = btree.getGreaterThan((((String)condition.getRightOperand()))); }
		else { 
			rowObjects = btree.getGreaterThanOrEqual((((String)condition.getRightOperand()))); }
		
		for(List<Cell<?>> row : rowObjects) {
			rowIndices.add(table.indexOf(row));
		}
		Integer[] rowIndicesArray = new Integer[rowIndices.size()];
		rowIndices.toArray(rowIndicesArray);
		//to ensure they're in the order they appear in the table, not simply in increasing order
		mergeSort(rowIndicesArray, 0, rowIndicesArray.length -1);
		return Arrays.asList(rowIndicesArray);
	}
	//finds the appropriate rows, when the column is not indexed
	private List<Integer> withoutBTree(Condition condition, List<List<Cell<?>>> table, int columnIndex) throws InputMismatchException {
		List<Integer> rows = new ArrayList<Integer>();
		for(int i = 1; i < table.size(); i++) {
			Cell<?> cell = table.get(i).get(columnIndex);
			//will return 0 if equal, negative if the cell's value is less than the given value, and positive if it's greater 
			int compare = cell.compareValue((String) condition.getRightOperand());
			if(condition.getOperator() == Operator.EQUALS) {
				if(compare == 0) { rows.add(i); }
			}
			else if(condition.getOperator() == Operator.NOT_EQUALS) {
				if(compare != 0) { rows.add(i); }
			}
			else if(condition.getOperator() == Operator.LESS_THAN) {
				if(compare < 0) { rows.add(i); }
			}
			else if(condition.getOperator() == Operator.LESS_THAN_OR_EQUALS) {
				if(compare <= 0) { rows.add(i); }
			}
			else if(condition.getOperator() == Operator.GREATER_THAN) {
				if(compare > 0) { rows.add(i); }
			}
			else if(condition.getOperator() == Operator.GREATER_THAN_OR_EQUALS) {
				if(compare >= 0) { rows.add(i); }
			}
		}
		return rows;
	}
	//removes all elements in one arraylist that do not appear in another
	private List<Integer> retainOverlap(List<Integer> leftCondition, List<Integer> rightCondition) {
		int i = 0;
		while(i < leftCondition.size()) {
			boolean overlap = false;
			for(int j = 0; j < rightCondition.size(); j++) {
				if(leftCondition.get(i).equals(rightCondition.get(j))) {
					overlap = true;
					i++;
					break;
				}
			}
			if(!overlap) {
				leftCondition.remove(i);
			}
		}
		return leftCondition;
	}
	//adds the elements of one list to the other, while ensuring there aren't doubles
	private List<Integer> addWithoutDoubles(List<Integer> leftCondition, List<Integer> rightCondition) {
		Set<Integer> set = new HashSet<Integer>();
		for(int i = 0; i < leftCondition.size(); i++) {
			set.add(leftCondition.get(i));
		}
		for(int i = 0; i < rightCondition.size(); i++) {
			set.add(rightCondition.get(i));
		}
		
		return new ArrayList<Integer>(set);
	}
	//based on the code from http://www.vogella.com/tutorials/JavaAlgorithmsMergesort/article.html
	//sorts rowIndices into the proper order
	private void mergeSort(Integer[] values, int left, int right) {
		if(left < right) {
			int middle = left + (right - left)/2;
			mergeSort(values, left, middle);
			mergeSort(values, middle + 1, right);
			merge(values, left, middle, right);
		}
	}
	private void merge(Integer[] values, int left, int middle, int right) {
		Integer[] helpers = new Integer[right + 1];
		Integer[] indices = new Integer[right + 1];
		for(int i = left; i <= right; i++) {
			helpers[i] = values[i];
		}
		int i = left;
		int j = middle + 1;
		int k = left;
		while(i <= middle && j <= right) {
			if(helpers[i].compareTo(helpers[j]) <= 0) {
				values[k] = helpers[i];
				i++;
			}
			else {
				values[k] = helpers[j];
				j++;
			}
			k++;
		}
		while(i <= middle) {
			values[k] = helpers[i];
			k++;
			i++;
		}
	}
}

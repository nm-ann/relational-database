package edu.yu.dataStructures.semesterProject;

import java.util.InputMismatchException;

import edu.yu.cs.dataStructures.fall2016.SimpleSQLParser.ColumnDescription;
/**
 * @author nannenbe@mail.yu.edu
 * represents a cell in a table, which is also attributed to a column
 */
public class Cell<T> {
	private T value;                       //the value the cell contains
	private String dataType;               //the data type of the column the cell is in
	private String columnName;             //the name of the column the cell is in
	private ColumnDescription description; //the description/metadata of the column the cell is in
	private boolean isColumnCell;          //describes whether this cell is an empty cell that only represents the column data 
										   //or is a real cell
	private boolean isPrimaryKey;          //describes whether or not this cell is in a primary key column
	
	private int[] decimalLength;		   //describes the maximum length of a decimal number, before and after the decimal

	@SuppressWarnings("unchecked")
	Cell(T value, ColumnDescription description, boolean isColumnCell, boolean isPrimaryKey) throws InputMismatchException {
		this.isColumnCell = isColumnCell;
		this.description = description;
		this.columnName = description.getColumnName();
		this.isPrimaryKey = isPrimaryKey;
		
		findDataType();
		validateType();
		
		if(description.getHasDefault()) {
			if(isPrimaryKey) {
				throw new IllegalArgumentException("a primary key column cannot have a default value");
			}
			//may want to see if can refactor this, as well as finding the data type
			this.value = (T) convertValue(description.getDefaultValue(), true);
		}
		this.value = value;
		
		notNull();
		
		decimalLength = new int[2];
		decimalLength[0] = description.getWholeNumberLength();
		decimalLength[1] = description.getFractionLength();
		validateLength();
	}
	//discovers the appropriate name of the dataType, based on description's columnType
	private void findDataType() {
		switch(description.getColumnType()) {
		case INT:
			dataType = "Integer";
			break;
		case VARCHAR:
			dataType = "Varchar";
			break;
		case DECIMAL:
			dataType = "Decimal";
			break;
		case BOOLEAN:
			dataType = "Boolean";
			break;
		}
	}
	//repeat from table-may want to do something about that
	private Object convertValue(String value, boolean isDefaultValue) throws InputMismatchException {
		try{
			switch(dataType) {
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
			if(isDefaultValue && !value.equals("null") ) {
				throw new InputMismatchException("cannot convert " + "string \"" + value + "\" to " + dataType + " for column: " + columnName);
			}
			return null;
		}
	}
	//if the value's type doesn't match the column's type, throw an InputMismatchException
	private void validateType() throws InputMismatchException{
		if(value != null) {
			if(!value.getClass().getSimpleName().equals(dataType)) {
				if(!value.getClass().getSimpleName().equals("String") && dataType.equals("Varchar")) {
					if(!value.getClass().getSimpleName().equals("Double") && dataType.equals("Decimal")) {
						throw new InputMismatchException("new value's data type doesn't match the column's data type");
					}
				}
			}
		}
	}
	//if it's not the isColumnCell, and it's notNull, and the value is null, throw an InputMismatchException
	private void notNull() throws InputMismatchException {
		if(isColumnCell) {
			return;
		}
		
		if((description.isNotNull() || isPrimaryKey) && value == null) {
			throw new InputMismatchException("a cell in " + columnName + " cannot be null");
		}
	}
	//if the value is a string or a decimal and it's longer than their respective lengths, throw an InputMismatchException
	private void validateLength() throws InputMismatchException {
		if(dataType.equals("Varchar") && value != null) {
			if(((String) value).length() > description.getVarCharLength()) {
				throw new InputMismatchException("a cell in " + columnName + " cannot be longer than " + description.getVarCharLength() + " characters");
			}
		}
		else if(dataType.equals("Decimal") && value != null) {
			String[] valueString = ((Double) value).toString().split("\\.");
			if(valueString[0].length() > decimalLength[0] || valueString[1].length() > decimalLength[1]) {
				throw new InputMismatchException("a cell in " + columnName + " cannot have a whole number longer than " + 
						description.getWholeNumberLength() + " digits and a decimal longer than " + 
						description.getFractionLength() + " digits");
			}
		}
	}
	
	public T getValue() {
		return value;
	}
	//returns the cell's value as a string
	public String getValueToString() {
		if(value == null) {
			return "null";
		}
		return value.toString();
	}
	@SuppressWarnings("unchecked")
	public void setValue(String value) {
		this.value = (T) convertValue(value, false);
		validateType();
		notNull();
		validateLength();
	}
	public String getDataType() {
		return dataType;
	}
	//may not want this
	public ColumnDescription getDescription() {
		return description;
	}
	public String getColumnName() {
		return columnName;
	}
	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}
	public boolean isPrimaryKey() {
		return isPrimaryKey;
	}
	public boolean isUnique() {
		return description.isUnique() || isPrimaryKey();
	}
	public int[] getDecimalLength() {
		return decimalLength;
	}
	//only relevant to the sum and avg select functions to allow for unexpected number generated by them
	void setDecimalLength(int wholeNumber, int decimalNumber) {
		decimalLength[0] = wholeNumber;
		decimalLength[1] = decimalNumber;
 	}
	@SuppressWarnings("unchecked")
	public Cell<T> copyOf() {
		String valueString;
		if(value == null) {
			valueString = "null";
		}
		else {
			valueString = value.toString();
		}
		T valueCopy = (T) convertValue(valueString, false);
		Cell<T> newCell;
		if(dataType.equals("Decimal")) {
			newCell = new Cell<T>((T)Double.valueOf(0.0), description, isColumnCell, isPrimaryKey);
			newCell.setDecimalLength(decimalLength[0], decimalLength[1]);
			newCell.setValue(valueString);
		}
		else {
			newCell = new Cell<T>(valueCopy, description, isColumnCell, isPrimaryKey);
		}
		//in case the length has been changed
		return newCell;
	}
	//checks to see if a given value is equal to the value that the cell holds
	@SuppressWarnings("unchecked")
	public int compareValue(String that) throws InputMismatchException {
		T thatValue;
		thatValue = (T) convertValue(that, false);
		//if the cell's type is numeric and the given string cannot be converted to a number, the above will return null
		//in that case, you need to make an artificial number depending on the value of the string
		if(thatValue == null){
			if(that.compareTo("0") > 0) {
				that = String.valueOf(Integer.MAX_VALUE);
			}
			else {
				that = String.valueOf(Integer.MIN_VALUE);
			}
			thatValue = (T) convertValue(that, false);
		}
		return ((Comparable<T>) value).compareTo(thatValue);
	}
	
	@Override
	public String toString() {
		if(isColumnCell) {
			return columnName + ", " + dataType;
		}
		
		return value.toString() + ", " + columnName ;
	}
}

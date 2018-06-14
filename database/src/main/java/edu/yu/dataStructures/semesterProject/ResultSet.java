package edu.yu.dataStructures.semesterProject;

import java.util.*;
/**
 * @author nannenbe@mail.yu.edu
 * represents the result of a sql query
 * CreateTable: returns the newly creates columns and true if successful, false, with an error message, if not successful
 * CreateIndex, Insert, Update, Delete: returns true or false, depending on whether it was successful or not
 * Select: returns the columns, the selected rows, and true if successful, false, with an error message, if not successful
 * 
 */

public class ResultSet<T> {
	private final List<List<T>> results; //a table containing the results of a sql query, represented by a list of lists
	private String errorMsg;			 //the error message, if the query failed
	//for successful create table and select queries
	@SuppressWarnings("unchecked")
	public ResultSet(List<List<T>> results) {
		if(results == null) {
			this.results = results;
		}
		else {
			this.results = new ArrayList<List<T>>();
			for(int i = 0; i < results.size(); i++) {
				this.results.add(new ArrayList<T>());
				for(int j = 0; j < results.get(i).size(); j++) {
					this.results.get(i).add((T) ((Cell<?>)results.get(i).get(j)).copyOf());
					//the above makes a copy of each cell, but cells from a select query with a select function will have
					//a modified column name that reflects the method, which would be transfered over, so it must be set manually
					((Cell<?>)this.results.get(i).get(j)).setColumnName(((Cell<?>) results.get(i).get(j)).getColumnName());
				}
			}
		}
		this.errorMsg = null;
	}
	//for create index, insert, update, and delete queries, as well as any queries that fail
	public ResultSet(T result) {
		this.results = new ArrayList<List<T>>();
		results.add(new ArrayList<T>());
		results.get(0).add(result);
	}
	
	public String valuesToString() {
		StringBuilder builder = new StringBuilder();
		if(results == null) {
			builder.append("null");
		}
		else{
			for(List<T> row : results) {
				for(T element : row) {
					builder.append(element.toString());
					builder.append(" | ");
				}
				builder.append("\n");
			}
		}
		
		return builder.toString();
	}
	
	public List<List<T>> getResults() {
		return results;
	}
	public boolean isSuccess() {
		return errorMsg == null;
	}
	public String getErrorMsg() {
		return errorMsg;
	}
	void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}
}

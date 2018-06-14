package edu.yu.dataStructures.semesterProject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import net.sf.jsqlparser.JSQLParserException;
/**
 * @author nannenbe@mail.yu.edu
 * Demos the database, going through queries such as create table, insert, select, create index, update, and delete
 * Prints out information relevant to each query
 */
public class DBTest {
	//reads a series of queries from a text file
	//prints out each query, the result sets, and the values in the table after the query has been made
	public static void main(String[] args) throws JSQLParserException, IOException {
		Database db = new Database();
		InputStream input = DBTest.class.getResourceAsStream("/queries.txt");
        BufferedReader queriesText = new BufferedReader(new InputStreamReader(input));
		
        ResultSet<?> resultSet = null;
        String line;
        while((line =  queriesText.readLine()) != null) {
        	System.out.println(line);
        	resultSet = db.execute(line);
        	System.out.println("\nResultSet:");
        	System.out.println(resultSet.valuesToString());
        	System.out.println();
        	if((line =  queriesText.readLine()) != null) {
        		resultSet = db.execute(line);
	        	System.out.println("\nTable:");
	        	System.out.println(resultSet.valuesToString());
	        	System.out.println();
        	}
        	System.out.println("____________________________________________________________");
        }
        
		queriesText.close();
	}
}
package edu.yu.dataStructures.semesterProject;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import net.sf.jsqlparser.JSQLParserException;

import org.junit.Before;
import org.junit.Test;

import edu.yu.dataStructures.semesterProject.*;
/**
 * @author nannenbe@mail.yu.edu
 * tests to ensure the insert query fails when it should and when it is successful, it adds a row to the table
 * the correctness of that row will be checked within the select tests
 */
public class InsertTest {
	Database db;
	List<String> insertQueries;
	
	@Before
	public void createTable() throws JSQLParserException, IOException {
		db = new Database();
		InputStream ctInput = DBTest.class.getResourceAsStream("/CreateTableTest.txt");
        BufferedReader ctReader = new BufferedReader(new InputStreamReader(ctInput));
        db.execute(ctReader.readLine());
        ctReader.close();
        
        InputStream insertInput = DBTest.class.getResourceAsStream("/InsertTest.txt");
        BufferedReader insertReader = new BufferedReader(new InputStreamReader(insertInput));
        insertQueries = new ArrayList<String>();
        String line;
        while((line = insertReader.readLine()) != null) {
        	insertQueries.add(line);
        }
        insertReader.close();
	}

	@Test
	public void normalInsert() throws JSQLParserException {
		ResultSet<?> resultSet = db.execute(insertQueries.get(0));
		assertNotEquals(resultSet.getResults().get(0).get(0), false);
		assertEquals(db.getTables().get(0).getRows().size(), 2);
 	}
	
	@Test
	public void nullNonNull() throws JSQLParserException {
		ResultSet<?> resultSet = db.execute(insertQueries.get(1));
		assertEquals(resultSet.getResults().get(0).get(0), false);
		assertEquals(db.getTables().get(0).getRows().size(), 1);
 	}
	//should add a test to check non unique strings when one is upper case and one is lower case
	@Test
	public void duplicateUnique() throws JSQLParserException {
		db.execute(insertQueries.get(0));
		ResultSet<?> resultSet = db.execute(insertQueries.get(2));
		assertEquals(resultSet.getResults().get(0).get(0), false);
		assertEquals(db.getTables().get(0).getRows().size(), 2);
 	}
	
	@Test
	public void duplicatePrimaryKey() throws JSQLParserException {
		db.execute(insertQueries.get(0));
		ResultSet<?> resultSet = db.execute(insertQueries.get(3));
		assertEquals(resultSet.getResults().get(0).get(0), false);
		assertEquals(db.getTables().get(0).getRows().size(), 2);
 	}
	
	@Test
	public void noPrimaryKey() throws JSQLParserException {
		ResultSet<?> resultSet = db.execute(insertQueries.get(4));
		assertEquals(resultSet.getResults().get(0).get(0), false);
		assertEquals(db.getTables().get(0).getRows().size(), 1);
 	}
	
	@Test
	public void unknownColumn() throws JSQLParserException {
		ResultSet<?> resultSet = db.execute(insertQueries.get(5));
		assertEquals(resultSet.getResults().get(0).get(0), false);
		assertEquals(db.getTables().get(0).getRows().size(), 1);
 	}
	
	@Test
	public void wrongDataType() throws JSQLParserException {
		ResultSet<?> resultSet = db.execute(insertQueries.get(6));
		assertEquals(resultSet.getResults().get(0).get(0), false);
		assertEquals(db.getTables().get(0).getRows().size(), 1);
 	}
	
	@Test
	public void wrongDataTypeReturnedAsNull() throws JSQLParserException {
		ResultSet<?> resultSet = db.execute(insertQueries.get(7));
		assertEquals(resultSet.getResults().get(0).get(0), false);
		assertEquals(db.getTables().get(0).getRows().size(), 1);
 	}
	@Test
	public void nonExistentTable() throws JSQLParserException {
		ResultSet<?> resultSet = db.execute(insertQueries.get(8));
		assertEquals(resultSet.getResults().get(0).get(0), false);
		assertEquals(db.getTables().get(0).getRows().size(), 1);
 	}

}

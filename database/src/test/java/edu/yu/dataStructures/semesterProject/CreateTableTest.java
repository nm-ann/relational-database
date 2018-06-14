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

import edu.yu.dataStructures.semesterProject.DBTest;
import edu.yu.dataStructures.semesterProject.Database;
import edu.yu.dataStructures.semesterProject.ResultSet;
/**
 * @author nannenbe@mail.yu.edu
 * tests to ensure the create table query fails when it should and when it is successful, it creates a table with a single row
 * the correctness of that row will be checked within the insert and select tests
 */
public class CreateTableTest {
	Database db;
	List<String> createQueries;
	
	@Before
	public void createTable() throws JSQLParserException, IOException {
		db = new Database();
		InputStream ctInput = DBTest.class.getResourceAsStream("/CreateTableTest.txt");
        BufferedReader ctReader = new BufferedReader(new InputStreamReader(ctInput));
        createQueries = new ArrayList<String>();
        String line;
        while((line = ctReader.readLine()) != null) {
        	createQueries.add(line);
        }
        ctReader.close();
   	}
	
	@Test
	public void normalCreate() throws JSQLParserException {
		ResultSet<?> resultSet = db.execute(createQueries.get(0));
		assertNotEquals(resultSet.isSuccess(), false);
		assertEquals(db.getTables().get(0).getRows().size(), 1);
	}
	
	@Test
	public void primaryKeyHasDefault() throws JSQLParserException {
		ResultSet<?> resultSet = db.execute(createQueries.get(1));
		assertEquals(resultSet.isSuccess(), false);
	}
	
	@Test
	public void wrongDefaultType() throws JSQLParserException {
		ResultSet<?> resultSet = db.execute(createQueries.get(2));
		assertEquals(resultSet.isSuccess(), false);
	}
}

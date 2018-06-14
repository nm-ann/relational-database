package edu.yu.dataStructures.semesterProject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

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

public class DeleteTest {
	Database db;
	List<String> deleteQueries;
	String[][] resultStrings = new String[9][6];
	
	@Before
	public void createTable() throws JSQLParserException, IOException {
		db = new Database();
		InputStream ctInput = DBTest.class.getResourceAsStream("/CreateTableTest.txt");
        BufferedReader ctReader = new BufferedReader(new InputStreamReader(ctInput));
        db.execute(ctReader.readLine());
        ctReader.close();
        
        InputStream inInput = DBTest.class.getResourceAsStream("/SelectInserts.txt");
        BufferedReader inReader = new BufferedReader(new InputStreamReader(inInput));
        String line;
        while((line =  inReader.readLine()) != null) {
        	db.execute(line);
        }
        inReader.close();
        
        InputStream selectInput = DBTest.class.getResourceAsStream("/DeleteTest.txt");
        BufferedReader selectReader = new BufferedReader(new InputStreamReader(selectInput));
        deleteQueries = new ArrayList<String>();

        while((line = selectReader.readLine()) != null) {
        	deleteQueries.add(line);
        }
        selectReader.close();
	}
	@Before 
	public void createResultStrings() throws IOException {
		InputStream resultInput = DBTest.class.getResourceAsStream("/SelectResultStrings.txt");
        BufferedReader resultReader = new BufferedReader(new InputStreamReader(resultInput));
        for(int j = 0; j < resultStrings[0].length; j++) {
        	for(int i = 0; i < resultStrings.length; i++) {
        		resultStrings[i][j] = resultReader.readLine();
        	}
        }
        resultReader.close();
	}
	
	@Test
	public void deleteWhere() throws JSQLParserException {
		ResultSet<?> resultSet = db.execute(deleteQueries.get(0));
		StringBuilder builder = new StringBuilder();
		builder.append(resultStrings[0][2]);
		builder.append("\n");
		builder.append(resultStrings[2][2]);
		builder.append("\n");
		builder.append(resultStrings[5][2]);
		builder.append("\n");
		builder.append(resultStrings[6][2]);
		builder.append("\n");
		assertEquals(resultSet.valuesToString(), builder.toString());
		resultSet = db.execute(deleteQueries.get(1));
		assertNotEquals(resultSet.isSuccess(), false);
		resultSet = db.execute(deleteQueries.get(0));
		builder = new StringBuilder();
		builder.append(resultStrings[0][2]);
		builder.append("\n");
		assertEquals(resultSet.valuesToString(), builder.toString());
	}
	/*/
	THIS TEST DOES NOT WORK BECAUSE OF A BUG IN THE PARSER
	@Test
	public void deleteAll() throws JSQLParserException {
		ResultSet<?> resultSet = db.execute(deleteQueries.get(3));
		StringBuilder builder = new StringBuilder();
		for(int i = 0; i < resultStrings.length; i++) {
			builder.append(resultStrings[i][2]);
			builder.append("\n");
		}
		assertEquals(resultSet.valuesToString(), builder.toString());
		resultSet = db.execute(deleteQueries.get(4));
		assertNotEquals(resultSet.isSuccess(), false);
		resultSet = db.execute(deleteQueries.get(5));
		builder = new StringBuilder();
		builder.append(resultStrings[0][2]);
		builder.append("\n");
		assertEquals(resultSet.valuesToString(), builder.toString());
	} /*/
}

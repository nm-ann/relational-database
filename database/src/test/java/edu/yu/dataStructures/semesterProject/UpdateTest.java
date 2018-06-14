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

public class UpdateTest {
	Database db;
	List<String> updateQueries;
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
        
        InputStream selectInput = DBTest.class.getResourceAsStream("/UpdateTest.txt");
        BufferedReader selectReader = new BufferedReader(new InputStreamReader(selectInput));
        updateQueries = new ArrayList<String>();

        while((line = selectReader.readLine()) != null) {
        	updateQueries.add(line);
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
	public void updateAll() throws JSQLParserException {
		ResultSet<?> resultSet = db.execute(updateQueries.get(0));
		StringBuilder builder = new StringBuilder();
		builder.append(resultStrings[0][2]);
		builder.append("\n");
		builder.append(resultStrings[1][2]);
		builder.append("\n");
		assertEquals(resultSet.valuesToString(), builder.toString());
		resultSet = db.execute(updateQueries.get(1));
		assertNotEquals(resultSet.isSuccess(), false);
		resultSet = db.execute(updateQueries.get(2));
		builder = new StringBuilder();
		for(int i = 0; i < resultStrings.length; i++) {
			builder.append(resultStrings[i][2]);
			builder.append("\n");
		}
		assertEquals(resultSet.valuesToString(), builder.toString());
	}
	@Test
	public void updateAllUnique() throws JSQLParserException {
		ResultSet<?> resultSet = db.execute(updateQueries.get(3));
		StringBuilder builder = new StringBuilder();
		builder.append(resultStrings[0][2]);
		builder.append("\n");
		builder.append(resultStrings[2][2]);
		builder.append("\n");
		assertEquals(resultSet.valuesToString(), builder.toString());
		resultSet = db.execute(updateQueries.get(4));
		assertEquals(resultSet.isSuccess(), false);
		resultSet = db.execute(updateQueries.get(5));
		builder = new StringBuilder();
		builder.append("false | ");
		builder.append("\n");
		assertEquals(resultSet.valuesToString(), builder.toString());
	}
	@Test
	public void updateWhere() throws JSQLParserException {
		ResultSet<?> resultSet = db.execute(updateQueries.get(6));
		StringBuilder builder = new StringBuilder();
		builder.append(resultStrings[0][4]);
		builder.append("\n");
		builder.append(resultStrings[1][4]);
		builder.append("\n");
		assertEquals(resultSet.valuesToString(), builder.toString());
		resultSet = db.execute(updateQueries.get(7));
		assertNotEquals(resultSet.isSuccess(), false);
		resultSet = db.execute(updateQueries.get(8));
		builder = new StringBuilder();
		builder.append(resultStrings[0][4]);
		builder.append("\n");
		builder.append("3.44, GPA | ");
		builder.append("\n");
		builder.append("3.44, GPA | ");
		builder.append("\n");
		builder.append("3.44, GPA | ");
		builder.append("\n");
		builder.append("3.44, GPA | ");
		builder.append("\n");
		builder.append("3.44, GPA | ");
		builder.append("\n");
		assertEquals(resultSet.valuesToString(), builder.toString());
	}
	@Test
	public void updateWhereUnique() throws JSQLParserException {
		ResultSet<?> resultSet = db.execute(updateQueries.get(9));
		StringBuilder builder = new StringBuilder();
		builder.append(resultStrings[0][1]);
		builder.append("\n");
		builder.append(resultStrings[1][1]);
		builder.append("\n");
		assertEquals(resultSet.valuesToString(), builder.toString());
		resultSet = db.execute(updateQueries.get(10));
		assertEquals(resultSet.isSuccess(), false);
		resultSet = db.execute(updateQueries.get(11));
		assertEquals(resultSet.valuesToString(), builder.toString());
	}
	@Test
	public void updateMultipleColumns() throws JSQLParserException {
		ResultSet<?> resultSet = db.execute(updateQueries.get(12));
		StringBuilder builder = new StringBuilder();
		builder.append(resultStrings[0][4]);
		builder.append(resultStrings[0][5]);
		builder.append("\n");
		builder.append(resultStrings[1][4]);
		builder.append(resultStrings[1][5]);
		builder.append("\n");
		assertEquals(resultSet.valuesToString(), builder.toString());
		resultSet = db.execute(updateQueries.get(13));
		assertNotEquals(resultSet.isSuccess(), false);
		resultSet = db.execute(updateQueries.get(14));
		builder = new StringBuilder();
		builder.append(resultStrings[0][4]);
		builder.append(resultStrings[0][5]);
		builder.append("\n");
		for(int i = 0; i < resultStrings.length - 1; i++) {
			builder.append("3.44, GPA | ");
			builder.append("true, CurrentStudent | ");
			builder.append("\n");
		}
		assertEquals(resultSet.valuesToString(), builder.toString());
	}
}

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

public class BTreeTest {
	Database db;
	List<String> btreeQueries;
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
        
        InputStream selectInput = DBTest.class.getResourceAsStream("/BTreeTest.txt");
        BufferedReader selectReader = new BufferedReader(new InputStreamReader(selectInput));
        btreeQueries = new ArrayList<String>();

        while((line = selectReader.readLine()) != null) {
        	btreeQueries.add(line);
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
	public void loadBTree() throws JSQLParserException {
		BTree[] btrees = db.getTables().get(0).getBTrees();
		List<List<Cell<?>>> rows = null;
		for(int i = 0; i < btrees.length; i++) {
			if(btrees[i] != null) {
				rows = btrees[i].getGreaterThanOrEqual("2112207");
			}
		}
		assertEquals(rows.size(), 8);
	}
	@Test
	public void WhereEqual() {
		ResultSet<?> resultSet = db.execute(btreeQueries.get(0));
		assertNotEquals(resultSet.isSuccess(), false);
		StringBuilder builder = new StringBuilder();
		builder.append(resultStrings[0][0]);
		builder.append("\n");
		builder.append(resultStrings[8][0]);
		builder.append("\n");
		assertEquals(resultSet.valuesToString(), builder.toString());
	}
	@Test
	public void WhereNotEqual() {
		ResultSet<?> resultSet = db.execute(btreeQueries.get(1));
		assertNotEquals(resultSet.isSuccess(), false);
		StringBuilder builder = new StringBuilder();
		builder.append(resultStrings[0][0]);
		builder.append("\n");
		builder.append(resultStrings[1][0]);
		builder.append("\n");
		builder.append(resultStrings[2][0]);
		builder.append("\n");
		builder.append(resultStrings[3][0]);
		builder.append("\n");
		builder.append(resultStrings[4][0]);
		builder.append("\n");
		builder.append(resultStrings[5][0]);
		builder.append("\n");
		builder.append(resultStrings[6][0]);
		builder.append("\n");
		builder.append(resultStrings[7][0]);
		builder.append("\n");
		assertEquals(resultSet.valuesToString(), builder.toString());
	}
	@Test
	public void WhereGreaterThan() {
		ResultSet<?> resultSet = db.execute(btreeQueries.get(2));
		assertNotEquals(resultSet.isSuccess(), false);
		StringBuilder builder = new StringBuilder();
		builder.append(resultStrings[0][0]);
		builder.append("\n");
		builder.append(resultStrings[1][0]);
		builder.append("\n");
		builder.append(resultStrings[2][0]);
		builder.append("\n");
		builder.append(resultStrings[7][0]);
		builder.append("\n");
		assertEquals(resultSet.valuesToString(), builder.toString());
	}
	@Test
	public void WhereGreaterThanOrEqual() {
		ResultSet<?> resultSet = db.execute(btreeQueries.get(3));
		assertNotEquals(resultSet.isSuccess(), false);
		StringBuilder builder = new StringBuilder();
		builder.append(resultStrings[0][0]);
		builder.append("\n");
		builder.append(resultStrings[1][0]);
		builder.append("\n");
		builder.append(resultStrings[2][0]);
		builder.append("\n");
		builder.append(resultStrings[4][0]);
		builder.append("\n");
		builder.append(resultStrings[7][0]);
		builder.append("\n");
		assertEquals(resultSet.valuesToString(), builder.toString());
	}
	@Test
	public void WhereLessThan() {
		ResultSet<?> resultSet = db.execute(btreeQueries.get(4));
		assertNotEquals(resultSet.isSuccess(), false);
		StringBuilder builder = new StringBuilder();
		builder.append(resultStrings[0][0]);
		builder.append("\n");
		builder.append(resultStrings[3][0]);
		builder.append("\n");
		builder.append(resultStrings[5][0]);
		builder.append("\n");
		builder.append(resultStrings[6][0]);
		builder.append("\n");
		builder.append(resultStrings[8][0]);
		builder.append("\n");
		assertEquals(resultSet.valuesToString(), builder.toString());
	}
	@Test
	public void WhereLessThanOrEqual() {
		ResultSet<?> resultSet = db.execute(btreeQueries.get(5));
		assertNotEquals(resultSet.isSuccess(), false);
		StringBuilder builder = new StringBuilder();
		builder.append(resultStrings[0][0]);
		builder.append("\n");
		builder.append(resultStrings[3][0]);
		builder.append("\n");
		builder.append(resultStrings[4][0]);
		builder.append("\n");
		builder.append(resultStrings[5][0]);
		builder.append("\n");
		builder.append(resultStrings[6][0]);
		builder.append("\n");
		builder.append(resultStrings[8][0]);
		builder.append("\n");
		assertEquals(resultSet.valuesToString(), builder.toString());
	}
	@Test
	public void CreateIndex() {
		ResultSet<?> resultSet = db.execute(btreeQueries.get(6));
		assertNotEquals(resultSet.isSuccess(), false);
		resultSet = db.execute(btreeQueries.get(7));
		StringBuilder builder = new StringBuilder();
		builder.append(resultStrings[0][2]);
		builder.append("\n");
		builder.append(resultStrings[3][2]);
		builder.append("\n");
		assertEquals(resultSet.valuesToString(), builder.toString());
	}
	@Test
	public void CreateIndexFakeColumn() {
		ResultSet<?> resultSet = db.execute(btreeQueries.get(8));
		assertEquals(resultSet.isSuccess(), false);
	}
	@Test
	public void alreadyIndexed() {
		ResultSet<?> resultSet = db.execute(btreeQueries.get(6));
		assertNotEquals(resultSet.isSuccess(), false);
		resultSet = db.execute(btreeQueries.get(6));
		assertEquals(resultSet.isSuccess(), false);
	}
}

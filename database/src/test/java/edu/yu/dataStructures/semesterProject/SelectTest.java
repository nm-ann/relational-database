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

public class SelectTest {
	Database db;
	List<String> selectQueries;
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
        
        InputStream selectInput = DBTest.class.getResourceAsStream("/SelectTest.txt");
        BufferedReader selectReader = new BufferedReader(new InputStreamReader(selectInput));
        selectQueries = new ArrayList<String>();

        while((line = selectReader.readLine()) != null) {
        	selectQueries.add(line);
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
	public void selectOneColumn() throws JSQLParserException {
		ResultSet<?> resultSet = db.execute(selectQueries.get(0));
		assertNotEquals(resultSet.isSuccess(), false);
		StringBuilder builder = new StringBuilder();
		for(int i = 0; i < resultStrings.length; i++) {
			builder.append(resultStrings[i][2]);
			builder.append("\n");
		}
		assertEquals(resultSet.valuesToString(), builder.toString());
	}
	@Test
	public void selectTwoColumns() throws JSQLParserException {
		ResultSet<?> resultSet = db.execute(selectQueries.get(1));
		assertNotEquals(resultSet.isSuccess(), false);
		StringBuilder builder = new StringBuilder();
		for(int i = 0; i < resultStrings.length; i++) {
			builder.append(resultStrings[i][2]);
			builder.append(resultStrings[i][3]);
			builder.append("\n");
		}
		assertEquals(resultSet.valuesToString(), builder.toString());
	}
	@Test
	public void selectThreeColumns() throws JSQLParserException {
		ResultSet<?> resultSet = db.execute(selectQueries.get(2));
		assertNotEquals(resultSet.isSuccess(), false);
		StringBuilder builder = new StringBuilder();
		for(int i = 0; i < resultStrings.length; i++) {
			builder.append(resultStrings[i][2]);
			builder.append(resultStrings[i][3]);
			builder.append(resultStrings[i][4]);
			builder.append("\n");
		}
		assertEquals(resultSet.valuesToString(), builder.toString());
	}
	@Test
	public void selectFakeColumn() throws JSQLParserException {
		ResultSet<?> resultSet = db.execute(selectQueries.get(3));
		assertEquals(resultSet.isSuccess(), false);	
	}
	@Test
	public void selectAllColumns() throws JSQLParserException {
		ResultSet<?> resultSet = db.execute(selectQueries.get(4));
		assertNotEquals(resultSet.isSuccess(), false);
		StringBuilder builder = new StringBuilder();
		for(int i = 0; i < resultStrings.length; i++) {
			builder.append(resultStrings[i][5]);
			builder.append(resultStrings[i][4]);
			builder.append(resultStrings[i][2]);
			builder.append(resultStrings[i][0]);
			builder.append(resultStrings[i][3]);
			builder.append(resultStrings[i][1]);
			builder.append("\n");
		}
		assertEquals(resultSet.valuesToString(), builder.toString());
	}
	@Test
	public void whereBooleanEquals() throws JSQLParserException {
		ResultSet<?> resultSet = db.execute(selectQueries.get(5));
		assertNotEquals(resultSet.isSuccess(), false);
		StringBuilder builder = new StringBuilder();
		builder.append(resultStrings[0][2]);
		builder.append("\n");
		builder.append(resultStrings[1][2]);
		builder.append("\n");
		builder.append(resultStrings[3][2]);
		builder.append("\n");
		builder.append(resultStrings[4][2]);
		builder.append("\n");
		builder.append(resultStrings[7][2]);
		builder.append("\n");
		builder.append(resultStrings[8][2]);
		builder.append("\n");
		assertEquals(resultSet.valuesToString(), builder.toString());
	}
	@Test
	public void whereStringEquals() throws JSQLParserException {
		ResultSet<?> resultSet = db.execute(selectQueries.get(6));
		assertNotEquals(resultSet.isSuccess(), false);
		StringBuilder builder = new StringBuilder();
		builder.append(resultStrings[0][2]);
		builder.append("\n");
		builder.append(resultStrings[5][2]);
		builder.append("\n");
		assertEquals(resultSet.valuesToString(), builder.toString());
	}
	@Test
	public void whereDecimalEquals() throws JSQLParserException {
		ResultSet<?> resultSet = db.execute(selectQueries.get(7));
		assertNotEquals(resultSet.isSuccess(), false);	
		StringBuilder builder = new StringBuilder();
		builder.append(resultStrings[0][2]);
		builder.append("\n");
		assertEquals(resultSet.valuesToString(), builder.toString());
	}
	@Test
	public void whereBooleanNotEquals() throws JSQLParserException {
		ResultSet<?> resultSet = db.execute(selectQueries.get(8));
		assertNotEquals(resultSet.isSuccess(), false);
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
	}
	@Test
	public void whereStringNotEquals() throws JSQLParserException {
		ResultSet<?> resultSet = db.execute(selectQueries.get(9));
		assertNotEquals(resultSet.isSuccess(), false);
		StringBuilder builder = new StringBuilder();
		for(int i = 0; i < resultStrings.length; i++) {
			if(i == 5) {
				continue;
			}
			builder.append(resultStrings[i][2]);
			builder.append("\n");
		}
		assertEquals(resultSet.valuesToString(), builder.toString());
	}
	@Test
	public void whereDecimalNotEquals() throws JSQLParserException {
		ResultSet<?> resultSet = db.execute(selectQueries.get(10));
		assertNotEquals(resultSet.isSuccess(), false);
		StringBuilder builder = new StringBuilder();
		for(int i = 0; i < resultStrings.length; i++) {
			if(i == 4) {
				continue;
			}
			builder.append(resultStrings[i][2]);
			builder.append("\n");
		}
		assertEquals(resultSet.valuesToString(), builder.toString());
	}
	@Test
	public void whereBooleanGreater() throws JSQLParserException {
		ResultSet<?> resultSet = db.execute(selectQueries.get(11));
		assertNotEquals(resultSet.isSuccess(), false);	
		StringBuilder builder = new StringBuilder();
		builder.append(resultStrings[0][2]);
		builder.append("\n");
		builder.append(resultStrings[1][2]);
		builder.append("\n");
		builder.append(resultStrings[3][2]);
		builder.append("\n");
		builder.append(resultStrings[4][2]);
		builder.append("\n");
		builder.append(resultStrings[7][2]);
		builder.append("\n");
		builder.append(resultStrings[8][2]);
		builder.append("\n");
		assertEquals(resultSet.valuesToString(), builder.toString());
	}
	@Test
	public void whereStringGreater() throws JSQLParserException {
		ResultSet<?> resultSet = db.execute(selectQueries.get(12));
		assertNotEquals(resultSet.isSuccess(), false);
		StringBuilder builder = new StringBuilder();
		for(int i = 0; i < resultStrings.length; i++) {
			if(i == 5 || i == 8) {
				continue;
			}
			builder.append(resultStrings[i][2]);
			builder.append("\n");
		}
		assertEquals(resultSet.valuesToString(), builder.toString());
	}
	@Test
	public void whereDecimalGreater() throws JSQLParserException {
		ResultSet<?> resultSet = db.execute(selectQueries.get(13));
		assertNotEquals(resultSet.isSuccess(), false);	
		StringBuilder builder = new StringBuilder();
		builder.append(resultStrings[0][2]);
		builder.append("\n");
		builder.append(resultStrings[1][2]);
		builder.append("\n");
		builder.append(resultStrings[2][2]);
		builder.append("\n");
		builder.append(resultStrings[3][2]);
		builder.append("\n");
		builder.append(resultStrings[6][2]);
		builder.append("\n");
		builder.append(resultStrings[7][2]);
		builder.append("\n");
		builder.append(resultStrings[8][2]);
		builder.append("\n");
		assertEquals(resultSet.valuesToString(), builder.toString());
	}
	@Test
	public void whereBooleanGreaterEquals() throws JSQLParserException {
		ResultSet<?> resultSet = db.execute(selectQueries.get(14));
		assertNotEquals(resultSet.isSuccess(), false);
		StringBuilder builder = new StringBuilder();
		builder.append(resultStrings[0][2]);
		builder.append("\n");
		builder.append(resultStrings[1][2]);
		builder.append("\n");
		builder.append(resultStrings[3][2]);
		builder.append("\n");
		builder.append(resultStrings[4][2]);
		builder.append("\n");
		builder.append(resultStrings[7][2]);
		builder.append("\n");
		builder.append(resultStrings[8][2]);
		builder.append("\n");
		assertEquals(resultSet.valuesToString(), builder.toString());
	}
	@Test
	public void whereStringGreaterEquals() throws JSQLParserException {
		ResultSet<?> resultSet = db.execute(selectQueries.get(15));
		assertNotEquals(resultSet.isSuccess(), false);	
		StringBuilder builder = new StringBuilder();
		for(int i = 0; i < resultStrings.length; i++) {
			if(i == 8) {
				continue;
			}
			builder.append(resultStrings[i][2]);
			builder.append("\n");
		}
		assertEquals(resultSet.valuesToString(), builder.toString());
	}
	@Test
	public void whereDecimalGreaterEquals() throws JSQLParserException {
		ResultSet<?> resultSet = db.execute(selectQueries.get(16));
		assertNotEquals(resultSet.isSuccess(), false);
		StringBuilder builder = new StringBuilder();
		for(int i = 0; i < resultStrings.length; i++) {
			if(i == 5) {
				continue;
			}
			builder.append(resultStrings[i][2]);
			builder.append("\n");
		}
		assertEquals(resultSet.valuesToString(), builder.toString());
	}
	@Test
	public void whereBooleanLess() throws JSQLParserException {
		ResultSet<?> resultSet = db.execute(selectQueries.get(17));
		assertNotEquals(resultSet.isSuccess(), false);
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
	}
	@Test
	public void whereStringLess() throws JSQLParserException {
		ResultSet<?> resultSet = db.execute(selectQueries.get(18));
		assertNotEquals(resultSet.isSuccess(), false);
		StringBuilder builder = new StringBuilder();
		builder.append(resultStrings[0][2]);
		builder.append("\n");
		builder.append(resultStrings[8][2]);
		builder.append("\n");
		assertEquals(resultSet.valuesToString(), builder.toString());
	}
	@Test
	public void whereDecimalLess() throws JSQLParserException {
		ResultSet<?> resultSet = db.execute(selectQueries.get(19));
		assertNotEquals(resultSet.isSuccess(), false);
		StringBuilder builder = new StringBuilder();
		builder.append(resultStrings[0][2]);
		builder.append("\n");
		builder.append(resultStrings[5][2]);
		builder.append("\n");
		assertEquals(resultSet.valuesToString(), builder.toString());
	}
	@Test
	public void whereBooleanLessEquals() throws JSQLParserException {
		ResultSet<?> resultSet = db.execute(selectQueries.get(20));
		assertNotEquals(resultSet.isSuccess(), false);
		StringBuilder builder = new StringBuilder();
		for(int i = 0; i < resultStrings.length; i++) {
			builder.append(resultStrings[i][2]);
			builder.append("\n");
		}
		assertEquals(resultSet.valuesToString(), builder.toString());
	}
	@Test
	public void whereStringLessEquals() throws JSQLParserException {
		ResultSet<?> resultSet = db.execute(selectQueries.get(21));
		assertNotEquals(resultSet.isSuccess(), false);
		StringBuilder builder = new StringBuilder();
		builder.append(resultStrings[0][2]);
		builder.append("\n");
		builder.append(resultStrings[5][2]);
		builder.append("\n");
		builder.append(resultStrings[8][2]);
		builder.append("\n");
		assertEquals(resultSet.valuesToString(), builder.toString());
	}
	@Test
	public void whereDecimalLessEquals() throws JSQLParserException {
		ResultSet<?> resultSet = db.execute(selectQueries.get(22));
		assertNotEquals(resultSet.isSuccess(), false);
		StringBuilder builder = new StringBuilder();
		builder.append(resultStrings[0][2]);
		builder.append("\n");
		builder.append(resultStrings[4][2]);
		builder.append("\n");
		builder.append(resultStrings[5][2]);
		builder.append("\n");
		assertEquals(resultSet.valuesToString(), builder.toString());
	}
	@Test
	public void whereAnd() throws JSQLParserException {
		ResultSet<?> resultSet = db.execute(selectQueries.get(23));
		assertNotEquals(resultSet.isSuccess(), false);
		StringBuilder builder = new StringBuilder();
		builder.append(resultStrings[0][2]);
		builder.append(resultStrings[0][3]);
		builder.append("\n");
		assertEquals(resultSet.valuesToString(), builder.toString());
	}
	@Test
	public void whereOr() throws JSQLParserException {
		ResultSet<?> resultSet = db.execute(selectQueries.get(24));
		assertNotEquals(resultSet.isSuccess(), false);
		StringBuilder builder = new StringBuilder();
		builder.append(resultStrings[0][2]);
		builder.append(resultStrings[0][3]);
		builder.append("\n");
		builder.append(resultStrings[1][2]);
		builder.append(resultStrings[1][3]);
		builder.append("\n");
		builder.append(resultStrings[3][2]);
		builder.append(resultStrings[3][3]);
		builder.append("\n");
		builder.append(resultStrings[6][2]);
		builder.append(resultStrings[6][3]);
		builder.append("\n");
		builder.append(resultStrings[7][2]);
		builder.append(resultStrings[7][3]);
		builder.append("\n");
		builder.append(resultStrings[8][2]);
		builder.append(resultStrings[8][3]);
		builder.append("\n");
		assertEquals(resultSet.valuesToString(), builder.toString());
	}
	@Test
	public void whereAndOr() throws JSQLParserException {
		ResultSet<?> resultSet = db.execute(selectQueries.get(25));
		assertNotEquals(resultSet.isSuccess(), false);
		StringBuilder builder = new StringBuilder();
		builder.append(resultStrings[0][2]);
		builder.append(resultStrings[0][3]);
		builder.append("\n");
		builder.append(resultStrings[1][2]);
		builder.append(resultStrings[1][3]);
		builder.append("\n");
		builder.append(resultStrings[3][2]);
		builder.append(resultStrings[3][3]);
		builder.append("\n");
		builder.append(resultStrings[7][2]);
		builder.append(resultStrings[7][3]);
		builder.append("\n");
		builder.append(resultStrings[8][2]);
		builder.append(resultStrings[8][3]);
		builder.append("\n");
		assertEquals(resultSet.valuesToString(), builder.toString());
	}
	@Test
	public void whereFakeColumn() throws JSQLParserException {
		ResultSet<?> resultSet = db.execute(selectQueries.get(26));
		assertEquals(resultSet.isSuccess(), false);	
	}
	@Test
	public void avgFunction() throws JSQLParserException {
		ResultSet<?> resultSet = db.execute(selectQueries.get(27));
		assertNotEquals(resultSet.isSuccess(), false);
		assertEquals(resultSet.valuesToString(), "AVG(GPA), Decimal | " + "\n"  + "2.9762500000000003, AVG(GPA) | " + "\n");
	}
	@Test
	public void sumFunction() throws JSQLParserException {
		ResultSet<?> resultSet = db.execute(selectQueries.get(28));
		assertNotEquals(resultSet.isSuccess(), false);
		assertEquals(resultSet.valuesToString(), "SUM(GPA), Decimal | " + "\n"  + "23.810000000000002, SUM(GPA) | " + "\n");
		
	}
	@Test
	public void maxFunctionString() throws JSQLParserException {
		ResultSet<?> resultSet = db.execute(selectQueries.get(29));
		assertNotEquals(resultSet.isSuccess(), false);
		assertEquals(resultSet.valuesToString(), "MAX(FirstName), Varchar | " + "\n"  + "'Yoko', MAX(FirstName) | " + "\n");
	}
	@Test
	public void maxFunctionInt() throws JSQLParserException {
		ResultSet<?> resultSet = db.execute(selectQueries.get(30));
		assertNotEquals(resultSet.isSuccess(), false);
		assertEquals(resultSet.valuesToString(), "MAX(BannerID), Integer | " + "\n"  + "948813634, MAX(BannerID) | " + "\n");
	}
	@Test
	public void maxFunctionDouble() throws JSQLParserException {
		ResultSet<?> resultSet = db.execute(selectQueries.get(31));
		assertNotEquals(resultSet.isSuccess(), false);	
		assertEquals(resultSet.valuesToString(), "MAX(GPA), Decimal | " + "\n"  + "4.0, MAX(GPA) | " + "\n");
	}
	@Test
	public void minFunctionString() throws JSQLParserException {
		ResultSet<?> resultSet = db.execute(selectQueries.get(32));
		assertNotEquals(resultSet.isSuccess(), false);
		assertEquals(resultSet.valuesToString(), "MIN(FirstName), Varchar | " + "\n"  + "'Aleen', MIN(FirstName) | " + "\n");
	}
	@Test
	public void minFunctionInt() throws JSQLParserException {
		ResultSet<?> resultSet = db.execute(selectQueries.get(33));
		assertNotEquals(resultSet.isSuccess(), false);
		assertEquals(resultSet.valuesToString(), "MIN(BannerID), Integer | " + "\n"  + "2112207, MIN(BannerID) | " + "\n");
	}
	@Test
	public void minFunctionDouble() throws JSQLParserException {
		ResultSet<?> resultSet = db.execute(selectQueries.get(34));
		assertNotEquals(resultSet.isSuccess(), false);
		assertEquals(resultSet.valuesToString(), "MIN(GPA), Decimal | " + "\n"  + "0.97, MIN(GPA) | " + "\n");
	}
	@Test
	public void countFunction() throws JSQLParserException {
		ResultSet<?> resultSet = db.execute(selectQueries.get(35));
		assertNotEquals(resultSet.isSuccess(), false);
		assertEquals(resultSet.valuesToString(), "COUNT(CurrentStudent), Boolean | " + "\n"  + "8, COUNT(CurrentStudent) | " + "\n");
	}
	@Test
	public void functionAndColumn() throws JSQLParserException {
		ResultSet<?> resultSet = db.execute(selectQueries.get(36));
		assertNotEquals(resultSet.isSuccess(), false);
		StringBuilder builder = new StringBuilder();
		for(int i = 0; i < resultStrings.length; i++) {
			builder.append(resultStrings[i][2]);
			builder.append(resultStrings[i][3]);
			if(i == 0) {
				builder.append("MAX(GPA), Decimal | ");
			}
			else {
				builder.append("4.0, MAX(GPA) | ");
			}
			builder.append("\n");
		}
		assertEquals(resultSet.valuesToString(), builder.toString());
	}
	@Test
	public void functionAndColumnOutofOrder() throws JSQLParserException {
		ResultSet<?> resultSet = db.execute(selectQueries.get(37));
		assertNotEquals(resultSet.isSuccess(), false);
		StringBuilder builder = new StringBuilder();
		for(int i = 0; i < resultStrings.length; i++) {
			builder.append(resultStrings[i][2]);
			if(i == 0) {
				builder.append("MAX(GPA), Decimal | ");
			}
			else {
				builder.append("4.0, MAX(GPA) | ");
			}
			builder.append(resultStrings[i][3]);
			builder.append("\n");
		}
		assertEquals(resultSet.valuesToString(), builder.toString());
	}
	@Test
	public void functionAndWhere() throws JSQLParserException {
		ResultSet<?> resultSet = db.execute(selectQueries.get(38));
		assertNotEquals(resultSet.isSuccess(), false);
		assertEquals(resultSet.valuesToString(), "COUNT(GPA), Decimal | " + "\n"  + "5, COUNT(GPA) | " + "\n");
	}
	@Test
	public void distict() throws JSQLParserException {
		ResultSet<?> resultSet = db.execute(selectQueries.get(39));
		assertNotEquals(resultSet.isSuccess(), false);
		StringBuilder builder = new StringBuilder();
		for(int i = 0; i < resultStrings.length - 3; i++) {
			builder.append(resultStrings[i][4]);
			builder.append("\n");
		}
		assertEquals(resultSet.valuesToString(), builder.toString());
	}
	@Test
	public void distictCount() throws JSQLParserException {
		ResultSet<?> resultSet = db.execute(selectQueries.get(40));
		assertNotEquals(resultSet.isSuccess(), false);
		StringBuilder builder = new StringBuilder();
		for(int i = 0; i < resultStrings.length; i++) {
			builder.append(resultStrings[i][2]);
			if(i == 0) {
				builder.append("COUNT(CurrentStudent), Boolean | ");
			}
			else {
				builder.append("2, COUNT(CurrentStudent) | ");
			}
			builder.append("\n");
		}
		assertEquals(resultSet.valuesToString(), builder.toString());
	}
	@Test
	public void distictMultipleColumns() throws JSQLParserException {
		ResultSet<?> resultSet = db.execute(selectQueries.get(41));
		assertNotEquals(resultSet.isSuccess(), false);
		StringBuilder builder = new StringBuilder();
		for(int i = 0; i < resultStrings.length - 2; i++) {
			builder.append(resultStrings[i][4]);
			builder.append(resultStrings[i][5]);
			builder.append("\n");
		}
		assertEquals(resultSet.valuesToString(), builder.toString());
	}
	@Test
	public void distictSelectFunction() throws JSQLParserException {
		ResultSet<?> resultSet = db.execute(selectQueries.get(42));
		assertNotEquals(resultSet.isSuccess(), false);
		StringBuilder builder = new StringBuilder();
		builder.append(resultStrings[0][5]);
		builder.append("MAX(GPA), Decimal | ");
		builder.append("\n");
		builder.append(resultStrings[1][5]);
		builder.append("3.44, MAX(GPA) | ");
		builder.append("\n");
		builder.append(resultStrings[2][5]);
		builder.append("3.44, MAX(GPA) | ");
		builder.append("\n");
		assertEquals(resultSet.valuesToString(), builder.toString());
	}
	@Test
	public void orderByASC() throws JSQLParserException {
		ResultSet<?> resultSet = db.execute(selectQueries.get(43));
		assertNotEquals(resultSet.isSuccess(), false);
		StringBuilder builder = new StringBuilder();
		builder.append(resultStrings[0][2]);
		builder.append(resultStrings[0][4]);
		builder.append("\n");
		builder.append(resultStrings[5][2]);
		builder.append(resultStrings[5][4]);
		builder.append("\n");
		builder.append(resultStrings[4][2]);
		builder.append(resultStrings[4][4]);
		builder.append("\n");
		builder.append(resultStrings[2][2]);
		builder.append(resultStrings[2][4]);
		builder.append("\n");
		builder.append(resultStrings[1][2]);
		builder.append(resultStrings[1][4]);
		builder.append("\n");
		builder.append(resultStrings[3][2]);
		builder.append(resultStrings[3][4]);
		builder.append("\n");
		builder.append(resultStrings[6][2]);
		builder.append(resultStrings[6][4]);
		builder.append("\n");
		builder.append(resultStrings[7][2]);
		builder.append(resultStrings[7][4]);
		builder.append("\n");
		builder.append(resultStrings[8][2]);
		builder.append(resultStrings[8][4]);
		builder.append("\n");
		assertEquals(resultSet.valuesToString(), builder.toString());
	}
	@Test
	public void orderByDESC() throws JSQLParserException {
		ResultSet<?> resultSet = db.execute(selectQueries.get(44));
		assertNotEquals(resultSet.isSuccess(), false);
		StringBuilder builder = new StringBuilder();
		builder.append(resultStrings[0][2]);
		builder.append(resultStrings[0][4]);
		builder.append("\n");
		builder.append(resultStrings[8][2]);
		builder.append(resultStrings[8][4]);
		builder.append("\n");
		builder.append(resultStrings[7][2]);
		builder.append(resultStrings[7][4]);
		builder.append("\n");
		builder.append(resultStrings[6][2]);
		builder.append(resultStrings[6][4]);
		builder.append("\n");
		builder.append(resultStrings[3][2]);
		builder.append(resultStrings[3][4]);
		builder.append("\n");
		builder.append(resultStrings[1][2]);
		builder.append(resultStrings[1][4]);
		builder.append("\n");
		builder.append(resultStrings[2][2]);
		builder.append(resultStrings[2][4]);
		builder.append("\n");
		builder.append(resultStrings[4][2]);
		builder.append(resultStrings[4][4]);
		builder.append("\n");
		builder.append(resultStrings[5][2]);
		builder.append(resultStrings[5][4]);
		builder.append("\n");
		assertEquals(resultSet.valuesToString(), builder.toString());
	}
	@Test
	public void multiOrderByASC() throws JSQLParserException {
		ResultSet<?> resultSet = db.execute(selectQueries.get(45));
		assertNotEquals(resultSet.isSuccess(), false);
		StringBuilder builder = new StringBuilder();
		builder.append(resultStrings[0][2]);
		builder.append(resultStrings[0][4]);
		builder.append(resultStrings[0][5]);
		builder.append("\n");
		builder.append(resultStrings[5][2]);
		builder.append(resultStrings[5][4]);
		builder.append(resultStrings[5][5]);
		builder.append("\n");
		builder.append(resultStrings[4][2]);
		builder.append(resultStrings[4][4]);
		builder.append(resultStrings[4][5]);
		builder.append("\n");
		builder.append(resultStrings[2][2]);
		builder.append(resultStrings[2][4]);
		builder.append(resultStrings[2][5]);
		builder.append("\n");
		builder.append(resultStrings[1][2]);
		builder.append(resultStrings[1][4]);
		builder.append(resultStrings[1][5]);
		builder.append("\n");
		builder.append(resultStrings[6][2]);
		builder.append(resultStrings[6][4]);
		builder.append(resultStrings[6][5]);
		builder.append("\n");
		builder.append(resultStrings[8][2]);
		builder.append(resultStrings[8][4]);
		builder.append(resultStrings[8][5]);
		builder.append("\n");
		builder.append(resultStrings[7][2]);
		builder.append(resultStrings[7][4]);
		builder.append(resultStrings[7][5]);
		builder.append("\n");
		builder.append(resultStrings[3][2]);
		builder.append(resultStrings[3][4]);
		builder.append(resultStrings[3][5]);
		builder.append("\n");
		assertEquals(resultSet.valuesToString(), builder.toString());
	}
	@Test
	public void multiOrderByDESC() throws JSQLParserException {
		ResultSet<?> resultSet = db.execute(selectQueries.get(46));
		assertNotEquals(resultSet.isSuccess(), false);
		StringBuilder builder = new StringBuilder();
		builder.append(resultStrings[0][2]);
		builder.append(resultStrings[0][4]);
		builder.append(resultStrings[0][5]);
		builder.append("\n");
		builder.append(resultStrings[3][2]);
		builder.append(resultStrings[3][4]);
		builder.append(resultStrings[3][5]);
		builder.append("\n");
		builder.append(resultStrings[7][2]);
		builder.append(resultStrings[7][4]);
		builder.append(resultStrings[7][5]);
		builder.append("\n");
		builder.append(resultStrings[8][2]);
		builder.append(resultStrings[8][4]);
		builder.append(resultStrings[8][5]);
		builder.append("\n");
		builder.append(resultStrings[6][2]);
		builder.append(resultStrings[6][4]);
		builder.append(resultStrings[6][5]);
		builder.append("\n");
		builder.append(resultStrings[1][2]);
		builder.append(resultStrings[1][4]);
		builder.append(resultStrings[1][5]);
		builder.append("\n");
		builder.append(resultStrings[2][2]);
		builder.append(resultStrings[2][4]);
		builder.append(resultStrings[2][5]);
		builder.append("\n");
		builder.append(resultStrings[4][2]);
		builder.append(resultStrings[4][4]);
		builder.append(resultStrings[4][5]);
		builder.append("\n");
		builder.append(resultStrings[5][2]);
		builder.append(resultStrings[5][4]);
		builder.append(resultStrings[5][5]);
		builder.append("\n");
		assertEquals(resultSet.valuesToString(), builder.toString());
	}
}

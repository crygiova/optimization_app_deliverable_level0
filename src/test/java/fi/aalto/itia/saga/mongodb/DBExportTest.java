package fi.aalto.itia.saga.mongodb;

import org.junit.Test;

public class DBExportTest {

	@Test
	public void dropAllTest() {
		DBExport dbe = new DBExport();
		dbe.dropAll();
	}
	
	@Test
	public void queryAllTest() {
		DBExport dbe = new DBExport();
		dbe.queryAll();
	}
}

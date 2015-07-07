package fi.aalto.itia.saga.data;

import java.util.Date;

import org.junit.Test;

import static org.junit.Assert.*;

public class TimeEnergyTupleTest {

	private Date start;

	@Test
	public void timeEnergyTupleTest() {
		start = UtilityTest.getMidnight();
		TimeUnitTuple<Date, Double> tet = new TimeUnitTuple<Date, Double>(start, 0d);
		assertTrue("Date: ", start.compareTo(tet.getDate())==0);
		assertTrue("Value: ", 0d == tet.getUnit());
		assertTrue(tet.equals(new TimeUnitTuple<Date, Double>(start, 0d)));
		assertTrue(!tet.equals(new TimeUnitTuple<Date, Double>(start, 10d)));
	}

}

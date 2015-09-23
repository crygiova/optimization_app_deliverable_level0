package fi.aalto.itia.saga.data;

import java.math.BigDecimal;
import java.util.Date;

import org.junit.Test;

import static org.junit.Assert.*;

public class EnergyPlanTest {
	Date start;
	TimeSequencePlan ep;

	private TimeSequencePlan fillEnergyPlan() {
		start = UtilityTest.getMidnight();
		TimeSequencePlan enerP = new TimeSequencePlan(start);
		assertTrue("Start Date ", enerP.getStart().compareTo(start) == 0);
		for (int i = 0; i < 24; i++) {
			enerP.addTimeEnergyTuple(new TimeUnitTuple<Date, BigDecimal>(start,
					new BigDecimal(i * 1000d)));
			start = UtilityTest.calculateNextHour(start, 1);
		}
		return enerP;
	}

	@Test
	public void testEnergyPlan() {
		ep = fillEnergyPlan();
		for (int i = 0; i < 24; i++) {
			assertTrue(ep.getTimeEnergyTuple(i).getUnit().doubleValue() == i * 1000d);
		}
		assertNull(ep.getTimeEnergyTuple(25));
	}

	@Test
	public void testEnergyPlanIndexOf() {
		ep = fillEnergyPlan();
		start = UtilityTest.getMidnight();
		assertTrue(ep.getStart().compareTo(start) == 0);
		assertTrue(ep.indexOf(ep.getStart()) == 0);
		for (int i = 0; i < 24; i++) {
			assertTrue(ep.indexOf(UtilityTest.calculateNextHour(ep.getStart(),
					i)) == i);
		}
		// out of bound
		assertTrue(ep.indexOf(UtilityTest.calculateNextHour(ep.getStart(), 30)) == -1);
		assertTrue(ep.indexOf(UtilityTest.calculateNextHour(ep.getStart(), -2)) == -1);
	}

	@Test
	public void testupdateTimeEnergyTuple() {
		ep = fillEnergyPlan();
		start = UtilityTest.getMidnight();
		// assert update
		assertTrue(ep.updateTimeEnergyTuple(start, new BigDecimal(100d)));
		// assert it has been updated
		assertTrue(ep.getTimeEnergyTuple(0).getUnit().doubleValue() == 100d);
	}
}

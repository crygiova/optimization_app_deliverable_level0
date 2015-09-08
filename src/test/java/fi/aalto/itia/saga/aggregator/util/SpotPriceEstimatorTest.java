package fi.aalto.itia.saga.aggregator.util;

import java.util.Calendar;

import org.junit.Test;

import static org.junit.Assert.*;
import fi.aalto.itia.saga.data.TimeSequencePlan;

public class SpotPriceEstimatorTest {

	// TODO needs to be extensively tested

	@Test
	public void testGetSpotPrice() {
		TimeSequencePlan sp = SpotPriceEstimator.getInstance().getSpotPrice(
				Calendar.getInstance().getTime());
		assertNotNull(sp);
		System.out.println(sp.toString());
	}
}
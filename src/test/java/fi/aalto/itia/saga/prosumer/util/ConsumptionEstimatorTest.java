package fi.aalto.itia.saga.prosumer.util;

import org.junit.Test;

import fi.aalto.itia.saga.data.TimeSequencePlan;
import fi.aalto.itia.saga.data.UtilityTest;

public class ConsumptionEstimatorTest {

	@Test
	public void testGetConsumption() {
		TimeSequencePlan ep = ConsumptionEstimator.getConsumption(UtilityTest
				.getMidnight());
		for (int i = 0; i < ep.getTimEnergy().size(); i++) {
			System.out.println("Date:"+ep.getTimeEnergyTuple(i).getDate()+" Unit:"+ep.getTimeEnergyTuple(i).getUnit());
		}
		
		System.out.println("Consumption MEAN: " + ep.getMeanValue(1));
	}
}

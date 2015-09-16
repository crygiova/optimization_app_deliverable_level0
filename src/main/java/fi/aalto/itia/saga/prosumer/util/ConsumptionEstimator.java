/**
 * 
 */
package fi.aalto.itia.saga.prosumer.util;

import java.util.Date;
import java.util.Properties;

import fi.aalto.itia.saga.data.TimeSequencePlan;
import fi.aalto.itia.saga.simulation.SimulationCalendarUtils;
import fi.aalto.itia.saga.util.MathUtility;
import fi.aalto.itia.saga.util.Utility;

import org.apache.commons.math3.distribution.NormalDistribution;

/**
 * This class estimates the consumption for a single house using a normal
 * distribution where the mean and the variance are specified in the
 * consumptionEstimator.properties file
 * 
 * @author giovanc1
 */
public class ConsumptionEstimator {

	private static final int H = 24;
	private static final String FILE_NAME_PROPERTIES = "consumptionEstimator.properties";
	private static final String MEAN = "mean";
	private static final String VARIANCE = "variance";

	private static Properties properties;
	private static NormalDistribution nd;

	private static Double mean;
	private static Double variance;

	/** Load Properties from Property File */
	static {
		properties = Utility.getProperties(FILE_NAME_PROPERTIES);
		mean = Double.parseDouble(properties.getProperty(MEAN));
		variance = Double.parseDouble(properties.getProperty(VARIANCE));
	};

	/**
	 * 
	 */
	public static TimeSequencePlan getConsumption(Date start) {
		TimeSequencePlan ep = new TimeSequencePlan(start);
		nd = new NormalDistribution(mean, variance);
		for (int i = 0; i < H; i++) {
			double value = MathUtility.roundDoubleTo(nd.sample(),6);//Math.round(nd.sample());
			ep.addTimeEnergyTuple(start, value);
			start = SimulationCalendarUtils.calculateNextHour(start, 1);
		}
		return ep;
	}
}

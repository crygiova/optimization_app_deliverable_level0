/**
 * 
 */
package fi.aalto.itia.saga.prosumer.util;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Properties;
import java.util.Random;

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
	private static final String DEVIATION = "maxDeviation";
	private static final String DB_CONS = "consumptionDB";

	private static Properties properties;
	private static NormalDistribution nd;

	private static Double mean;
	private static Double variance;
	private static Double deviation;

	private static ImportConsumptions importConsumption = new ImportConsumptions();
	private static boolean dbConsumption;

	/** Load Properties from Property File */
	static {
		properties = Utility.getProperties(FILE_NAME_PROPERTIES);
		mean = Double.parseDouble(properties.getProperty(MEAN));
		variance = Double.parseDouble(properties.getProperty(VARIANCE));
		deviation = Double.parseDouble(properties.getProperty(DEVIATION));
		dbConsumption = Boolean.parseBoolean((properties.getProperty(DB_CONS)));
	};

	/**
	 * 
	 */
	public static TimeSequencePlan getConsumption(Date start) {
		TimeSequencePlan ep = new TimeSequencePlan(start);
		if (dbConsumption) {
			// Check the period of the year from the db
			String key = importConsumption.getPeriodName(start);
			// get the consumption
			Double[] cons = importConsumption.getConsumption(key);
			for (int i = 0; i < H; i++) {
				BigDecimal value = MathUtility.roundDoubleTo(cons[i], 6);// Math.round(nd.sample());
				ep.addTimeEnergyTuple(start, value);
				start = SimulationCalendarUtils.calculateNextHour(start, 1);
			}

		} else {

			nd = new NormalDistribution(mean, variance);
			for (int i = 0; i < H; i++) {
				BigDecimal value = MathUtility.roundDoubleTo(nd.sample(), 6);// Math.round(nd.sample());
				ep.addTimeEnergyTuple(start, value);
				start = SimulationCalendarUtils.calculateNextHour(start, 1);
			}
		}
		return ep;
	}

	public static TimeSequencePlan getConsumptionDeviated(
			TimeSequencePlan estimatedConsumption) {
		TimeSequencePlan deviatedConsumption = new TimeSequencePlan(
				estimatedConsumption.getStart());
		Random rnd = new Random();
		boolean sign;
		BigDecimal percent;
		BigDecimal unit;
		for (int i = 0; i < estimatedConsumption.size(); i++) {
			sign = rnd.nextBoolean();
			percent = BigDecimal.valueOf(deviation * rnd.nextDouble());
			unit = estimatedConsumption.getTimeEnergyTuple(i).getUnit();
			if (sign) {
				unit = unit.add(unit.multiply(percent));
			} else {
				unit = unit.subtract(unit.multiply(percent));
			}
			deviatedConsumption.addTimeEnergyTuple(estimatedConsumption
					.getIndex(i).getDate(), MathUtility.roundBigDecimalTo(unit,
					6));
		}
		return deviatedConsumption;
	}
}

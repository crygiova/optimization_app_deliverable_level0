/**
 * 
 */
package fi.aalto.itia.saga.prosumer.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Properties;

import fi.aalto.itia.saga.data.TimeSequencePlan;

import org.apache.commons.math3.distribution.NormalDistribution;


/**
 * @author giovanc1
 *
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
		properties = new Properties();
		ClassLoader classLoader = Thread.currentThread()
				.getContextClassLoader();
		try (InputStream resourceStream = classLoader
				.getResourceAsStream(FILE_NAME_PROPERTIES)) {
			properties.load(resourceStream);
		} catch (IOException e) {
			System.out.println("Property file not Found: "
					+ FILE_NAME_PROPERTIES);
		}
		mean = Double.parseDouble(properties.getProperty(MEAN));
		variance = Double.parseDouble(properties.getProperty(VARIANCE));
	};

	/**
	 * 
	 */
	// TODO 
	public static TimeSequencePlan getConsumption(Date start) {
		TimeSequencePlan ep = new TimeSequencePlan(start);
		nd = new NormalDistribution(mean, variance);
		for (int i = 0; i < H; i++) {
			double value = Math.round(nd.sample());
			ep.addTimeEnergyTuple(start, value);
			start = TimeSequencePlan.calculateNextHour(start, 1);
		}
		return ep;
	}
}

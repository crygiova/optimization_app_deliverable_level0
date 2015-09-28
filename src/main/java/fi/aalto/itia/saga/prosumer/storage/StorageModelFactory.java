/**
 * 
 */
package fi.aalto.itia.saga.prosumer.storage;

import java.math.BigDecimal;
import java.util.Properties;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.log4j.Logger;

import fi.aalto.itia.saga.util.MathUtility;
import fi.aalto.itia.saga.util.Utility;

/**
 * @author giovanc1
 *
 *         Factory which generates the storages
 */

public class StorageModelFactory {

	private static final String FILE_NAME_PROPERTIES = "storageModelFactory.properties";
	private static final String FACTORY_LOGIC = "factoryLogic";
	private static final String AVG_DISCHARGING_RATE = "avgDischargingRate";
	private static final String MIN_STORAGE_CAPACITY = "minStorageCapacity";
	private static final String MAX_STORAGE_CAPACITY = "maxStorageCapacity";
	private static final String ID_STORAGE = "idStorage";
	private static final String AVG_STATE_OF_CHARGE = "avgStateOfCharge";

	private static final String MIN_CHARGING_RATE = "minChargingRate";
	private static final String MAX_CHARGING_RATE = "maxChargingRate";
	private static final Logger log = Logger
			.getLogger(StorageModelFactory.class);

	public enum FactoryTypeLogic {
		CONSTANT, VARIABLE,
	};

	private static Double avgDischargingRate;

	private static Double minStorageCapacity;

	private static Double maxStorageCapacity;

	private static FactoryTypeLogic factoryLogic;

	private static Integer id;

	private static Double avgStateOfCharge;

	private static Double minChargingRate;

	private static Double maxChargingRate;

	private static Properties properties;

	/** Load Properties from Property File */
	static {
		properties = Utility.getProperties(FILE_NAME_PROPERTIES);
		avgDischargingRate = Double.parseDouble(properties
				.getProperty(AVG_DISCHARGING_RATE));
		minStorageCapacity = Double.parseDouble(properties
				.getProperty(MIN_STORAGE_CAPACITY));
		maxStorageCapacity = Double.parseDouble(properties
				.getProperty(MAX_STORAGE_CAPACITY));
		factoryLogic = FactoryTypeLogic.valueOf(properties.getProperty(
				FACTORY_LOGIC).toUpperCase());
		id = Integer.parseInt(properties.getProperty(ID_STORAGE));
		avgStateOfCharge = Double.parseDouble(properties
				.getProperty(AVG_STATE_OF_CHARGE));
		minChargingRate = Double.parseDouble(properties
				.getProperty(MIN_CHARGING_RATE));
		maxChargingRate = Double.parseDouble(properties
				.getProperty(MAX_CHARGING_RATE));
		// TODO normal distribution nd = new NormalDistribution(0,
		// varianceStateOfCharge);
	};

	public static StorageModel getStorageModel() {
		log.debug("Generating Storage of Type: " + factoryLogic);
		switch (factoryLogic) {
		case CONSTANT:
			return new StorageModel(
					Integer.toString(id++),
					BigDecimal.valueOf(avgStateOfCharge),
					BigDecimal.valueOf(minStorageCapacity),
					BigDecimal.valueOf((maxChargingRate + minChargingRate) / 2),
					BigDecimal.valueOf(avgDischargingRate));
		case VARIABLE:

			BigDecimal storageCapacity = getValueWithinRange(
					minStorageCapacity, maxStorageCapacity);// BigDecimal.valueOf(avgStorageCapacity);
			BigDecimal stateOfCharge = getValueWithinRange(0d,
					storageCapacity.doubleValue());
			BigDecimal chargingRate = MathUtility.roundBigDecimalTo(
					getValueWithinRange(minChargingRate, maxChargingRate), 1);// MathUtility.roundDoubleTo(
			return new StorageModel(Integer.toString(id++), stateOfCharge,
					storageCapacity, chargingRate,
					BigDecimal.valueOf(avgDischargingRate));
		}
		return null;
	}

	public static Properties getProperties() {
		return properties;
	}

	public static FactoryTypeLogic getFactoryLogic() {
		return factoryLogic;
	}

	public static BigDecimal getValueWithinRange(double min, double max) {
		double mean = (max + min) / 2;
		double variance = Math.abs(mean - min);
		NormalDistribution nd = new NormalDistribution(mean, variance);
		double value;
		do {
			value = nd.sample();
		} while (value <= min || value >= max);
		return MathUtility.roundDoubleTo(value, 6);
	}
}

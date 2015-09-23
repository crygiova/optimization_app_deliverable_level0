/**
 * 
 */
package fi.aalto.itia.saga.prosumer.storage;

import java.math.BigDecimal;
import java.util.Properties;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.log4j.Logger;

import fi.aalto.itia.saga.util.Utility;

/**
 * @author giovanc1
 *
 *         Factory which generates the storages
 */

public class StorageModelFactory {

	private static final String FILE_NAME_PROPERTIES = "storageModelFactory.properties";
	private static final String FACTORY_LOGIC = "factoryLogic";
	private static final String AVG_CHARGING_RATE = "avgChargingRate";
	private static final String AVG_DISCHARGING_RATE = "avgDischargingRate";
	private static final String AVG_STORAGE_CAPACITY = "avgStorageCapacity";
	private static final String ID_STORAGE = "idStorage";
	private static final String AVG_STATE_OF_CHARGE = "avgStateOfCharge";
	private static final String VARIANCE_STORAGE_CAPACITY = "varianceStorageCapacity";
	private static final Logger log = Logger
			.getLogger(StorageModelFactory.class);

	public enum FactoryTypeLogic {
		CONSTANT, VARIABLE,
	};

	private static Double avgChargingRate;

	private static Double avgDischargingRate;

	private static Double avgStorageCapacity;

	private static FactoryTypeLogic factoryLogic;

	private static Integer id;

	private static Double avgStateOfCharge;

	@SuppressWarnings("unused")
	private static Double varianceStateOfCharge;

	@SuppressWarnings("unused")
	private static NormalDistribution nd;

	private static Properties properties;

	/** Load Properties from Property File */
	static {
		properties = Utility.getProperties(FILE_NAME_PROPERTIES);
		avgChargingRate = Double.parseDouble(properties
				.getProperty(AVG_CHARGING_RATE));
		avgDischargingRate = Double.parseDouble(properties
				.getProperty(AVG_DISCHARGING_RATE));
		avgStorageCapacity = Double.parseDouble(properties
				.getProperty(AVG_STORAGE_CAPACITY));
		factoryLogic = FactoryTypeLogic.valueOf(properties.getProperty(
				FACTORY_LOGIC).toUpperCase());
		id = Integer.parseInt(properties.getProperty(ID_STORAGE));
		avgStateOfCharge = Double.parseDouble(properties
				.getProperty(AVG_STATE_OF_CHARGE));
		varianceStateOfCharge = Double.parseDouble(properties
				.getProperty(VARIANCE_STORAGE_CAPACITY));
		// TODO normal distribution nd = new NormalDistribution(0,
		// varianceStateOfCharge);
	};

	public static StorageModel getStorageModel() {
		log.debug("Generating Storage of Type: " + factoryLogic);
		switch (factoryLogic) {
		case CONSTANT:
			return new StorageModel(Integer.toString(id++),
					BigDecimal.valueOf(avgStateOfCharge),
					BigDecimal.valueOf(avgStorageCapacity),
					BigDecimal.valueOf(avgChargingRate),
					BigDecimal.valueOf(avgDischargingRate));
		case VARIABLE:
			// TODO generate using a distribution the storages
			break;
		}
		return null;
	}

	public static Properties getProperties() {
		return properties;
	}

	public static FactoryTypeLogic getFactoryLogic() {
		return factoryLogic;
	}
}

/**
 * 
 */
package fi.aalto.itia.saga.storage;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author giovanc1
 *
 */
public class StorageModelFactoryTest {

	private StorageModel storageModel;
	private Properties prop;

	@Test
	public void getStorageModelTest() throws FileNotFoundException, IOException {
		storageModel = StorageModelFactory.getStorageModel();
		
		prop = StorageModelFactory.getProperties();

		if (StorageModelFactory.FactoryTypeLogic.CONSTANT == StorageModelFactory
				.getFactoryLogic()) {
			assertTrue("Charging Rate Assert", Double.parseDouble(prop
					.getProperty("avgChargingRate")) == storageModel
					.getMaxChargingRateWh());
			assertTrue("DisCharging Rate Assert", Double.parseDouble(prop
					.getProperty("avgDischargingRate")) == storageModel
					.getMaxDischargingRateWh());
			assertTrue("Storage Capacity Assert", Double.parseDouble(prop
					.getProperty("avgStorageCapacity")) == storageModel
					.getMaxCapacityW());
			assertTrue("State Of Charge", Double.parseDouble(prop
					.getProperty("avgStateOfCharge")) == storageModel
					.getStateOfChargeW());
		}
		if (StorageModelFactory.FactoryTypeLogic.VARIABLE == StorageModelFactory
				.getFactoryLogic()) {
			// TODO
		}
	}
}

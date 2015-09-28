/**
 * 
 */
package fi.aalto.itia.saga.prosumer.storage;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.junit.Test;

import fi.aalto.itia.saga.prosumer.storage.StorageModel;
import fi.aalto.itia.saga.prosumer.storage.StorageModelFactory;
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
					.getMaxChargingRateWh().doubleValue());
			assertTrue("DisCharging Rate Assert", Double.parseDouble(prop
					.getProperty("avgDischargingRate")) == storageModel
					.getMaxDischargingRateWh().doubleValue());
			assertTrue("Storage Capacity Assert", Double.parseDouble(prop
					.getProperty("avgStorageCapacity")) == storageModel
					.getMaxCapacityW().doubleValue());
			assertTrue("State Of Charge", Double.parseDouble(prop
					.getProperty("avgStateOfCharge")) == storageModel
					.getStateOfChargeW().doubleValue());
		}
		if (StorageModelFactory.FactoryTypeLogic.VARIABLE == StorageModelFactory
				.getFactoryLogic()) {
			// TODO
		}
	}
	
	@Test
	public void getValueWithinRange()
	{
		System.out.println(StorageModelFactory.getValueWithinRange(0, 15));
		System.out.println(StorageModelFactory.getValueWithinRange(0, 15));
		System.out.println(StorageModelFactory.getValueWithinRange(0, 15));
	}
}

/**
 * 
 */
package fi.aalto.itia.saga.prosumer.storage;

import java.math.BigDecimal;

/**
 * Class which represent a controller of a storage
 * @author giovanc1
 *
 */
public class StorageController {
	private StorageModel storage;

	/**
	 * 
	 */
	public StorageController() {
		super();
		this.storage = StorageModelFactory.getStorageModel();
	}

	/**
	 * @param storage
	 */
	public StorageController(StorageModel storage) {
		super();
		this.storage = storage;
	}

	public BigDecimal getStorageStatusW() {
		return storage.getStateOfChargeW();
	}

	public BigDecimal getStorageMaxChargingRateWh() {
		return storage.getMaxChargingRateWh();
	}

	public BigDecimal getStorageMaxDischargingRateWh() {
		return storage.getMaxDischargingRateWh();
	}

	public BigDecimal getStorageCapacityW() {
		return storage.getMaxCapacityW();
	}

	/*
	 * TODO implement a solution that knows what is the real charge of the
	 * Storage and returns the exceeded charge or discharge of the battery need
	 * also to implement a solution for receiving the dayly plan and charge the
	 * battery
	 */
	public void chargeAndDischargeStorageWh(BigDecimal chargeWh, BigDecimal dischargeWh)
			throws Exception {
		storage.chargeAndDischarge(chargeWh, dischargeWh);
	}
}

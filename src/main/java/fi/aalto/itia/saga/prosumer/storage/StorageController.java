/**
 * 
 */
package fi.aalto.itia.saga.prosumer.storage;

/**
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

	public Double getStorageStatusW() {
		return storage.getStateOfChargeW();
	}

	public Double getStorageMaxChargingRateWh() {
		return storage.getMaxChargingRateWh();
	}

	public Double getStorageMaxDischargingRateWh() {
		return storage.getMaxDischargingRateWh();
	}

	public Double getStorageCapacityW() {
		return storage.getMaxCapacityW();
	}

	/*
	 * TODO implement a solution that knows what is the real charge of the
	 * Storage and returns the exceeded charge or discharge of the battery need
	 * also to implement a solution for receiving the dayly plan and charge the
	 * battery
	 */
	public void chargeAndDischargeStorageWh(Double chargeWh, Double dischargeWh)
			throws Exception {
		storage.chargeAndDischarge(chargeWh, dischargeWh);
	}
}

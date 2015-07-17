	package fi.aalto.itia.saga.storage;

import org.apache.log4j.Logger;

/**
 * @author giovanc1
 *
 */
public class StorageModel {
	private final String EX_STORAGE_CAPACITY = "Maximum or minimum capacity storage exceeded\n";
	private final String EX_STORAGE_CHARGING_RATE = "Charging or DisCharging Rate has an inappropaiate value\n";
	/***/
	private String id;
	/***/
	private Double stateOfChargeW;
	/***/
	private Double maxCapacityW;
	/***/
	private Double maxChargingRateWh;
	/***/
	private Double maxDischargingRateWh;
	/***/
	private final Double minCapacityW = 0d;

	private final Logger log = Logger.getLogger(StorageModel.class);

	/**
	 * @param id
	 * @param stateOfChargeW
	 * @param maxCapacityW
	 * @param maxChargingRateWh
	 * @param maxDischargingRateWh
	 */
	public StorageModel(String id, Double stateOfChargeW, Double maxCapacityW,
			Double maxChargingRateWh, Double maxDischargingRateWh) {
		super();
		this.id = id;
		this.stateOfChargeW = stateOfChargeW;
		this.maxCapacityW = maxCapacityW;
		this.maxChargingRateWh = maxChargingRateWh;
		this.maxDischargingRateWh = maxDischargingRateWh;

		log.debug("StorageModel Constructor" + this.toString());
	}

	/***/
	public String getId() {
		return id;
	}

	/**
	 * @param id
	 */
	public void setId(String id) {
		this.id = id;
	}

	/***/
	public Double getStateOfChargeW() {
		return stateOfChargeW;
	}

	/**
	 * @param stateOfChargeW
	 * @throws Exception
	 */
	protected void setStateOfChargeW(Double stateOfChargeW) throws Exception {
		if (this.checkChargingRate(stateOfChargeW, this.maxCapacityW)) {
			this.stateOfChargeW = stateOfChargeW;
		} else {
			throw new Exception("Set Value of out Bound");
		}
	}

	/***/
	public Double getMaxCapacityW() {
		return maxCapacityW;
	}

	/***/
	public Double getMaxChargingRateWh() {
		return maxChargingRateWh;
	}

	/***/
	public Double getMaxDischargingRateWh() {
		return maxDischargingRateWh;
	}

	/**
	 * @param wh
	 */
	public void charge(Double wh) {
		// if (this.checkChargingRate(wh, this.maxChargingRateWh)) {
		// Double nextStateOfCharge = this.stateOfChargeW + wh;
		// if (nextStateOfCharge <= this.maxCapacityW) {
		// this.setStateOfChargeW(nextStateOfCharge);
		// } else {
		// this.setStateOfChargeW(this.maxCapacityW);
		// // TODO Exception
		// }
		// } else {
		// // TODO exception or whateverElse
		// }
	}

	/**
	 * @param wh
	 * @throws Exception
	 */
	public void discharge(Double wh) throws Exception {
		// if (this.checkChargingRate(wh, this.maxDischargingRateWh)) {
		// Double nextStateOfCharge = this.stateOfChargeW - wh;
		// if ( nextStateOfCharge >= this.minCapacityW) {
		// this.setStateOfChargeW(nextStateOfCharge);
		// } else {
		// this.setStateOfChargeW(this.minCapacityW);
		// throw new
		// Exception("Required discharging is bigger than the current status");//
		// TODO Exception
		// }
		// } else {
		// throw new
		// Exception("DisCharging Rage Bigger than Max DisCharging Rate");//
		// TODO Exception
		// }
	}

	/**
	 * @param chargeWh
	 * @param dischargeWh
	 * @throws Exception
	 */
	public void chargeAndDischarge(Double chargeWh, Double dischargeWh)
			throws Exception {
		boolean exception = false;
		String exceptionMsg = "";
		if (!this.checkChargingRate(chargeWh, this.maxChargingRateWh)
				|| !this.checkChargingRate(dischargeWh,
						this.maxDischargingRateWh)) {
			exceptionMsg += EX_STORAGE_CHARGING_RATE;
			exception = true;
			chargeWh = (chargeWh <= this.maxChargingRateWh) ? Math
					.abs(chargeWh) : this.maxChargingRateWh;
			dischargeWh = (dischargeWh <= this.maxDischargingRateWh) ? Math
					.abs(dischargeWh) : this.maxChargingRateWh;

			log.debug("Charging rates out of bounds: " + this.toString());
		}
		Double nextStateOfCharge = this.stateOfChargeW + chargeWh - dischargeWh;
		if (nextStateOfCharge >= this.minCapacityW
				&& nextStateOfCharge <= this.maxCapacityW) {
			this.setStateOfChargeW(nextStateOfCharge);
			log.debug("Charging Storage: " + this.toString());
		} else {
			nextStateOfCharge = (nextStateOfCharge < this.minCapacityW) ? this.minCapacityW
					: this.maxCapacityW;
			this.setStateOfChargeW(nextStateOfCharge);
			exceptionMsg += EX_STORAGE_CAPACITY;
			exception = true;
			log.debug("Storage capacity out of bounds: " + this.toString());
		}
		if (exception)
			throw new Exception(exceptionMsg);
	}

	/**
	 * @param wh
	 * @param chargingRate
	 * @return
	 */
	private boolean checkChargingRate(Double wh, Double chargingRate) {
		if (wh <= chargingRate && wh >= 0) {
			return true;
		}
		return false;
	}

	@Override
	public String toString() {
		return "StorageModel [id=" + id + ", stateOfChargeW=" + stateOfChargeW
				+ ", maxCapacityW=" + maxCapacityW + ", maxChargingRateWh="
				+ maxChargingRateWh + ", maxDischargingRateWh="
				+ maxDischargingRateWh + ", minCapacityW=" + minCapacityW + "]";
	}
}

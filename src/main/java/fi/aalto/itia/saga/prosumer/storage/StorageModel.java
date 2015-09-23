package fi.aalto.itia.saga.prosumer.storage;

import java.math.BigDecimal;

import org.apache.log4j.Logger;

import fi.aalto.itia.saga.util.MathUtility;

/**
 * @author giovanc1 
 * 
 * Simple model of a storage! 
 * Values are in Kw
 */
public class StorageModel {
	private final String EX_STORAGE_CAPACITY = "Maximum or minimum capacity storage exceeded\n";
	private final String EX_STORAGE_CHARGING_RATE = "Charging or DisCharging Rate has an inappropaiate value\n";
	/***/
	private String id;
	/***/
	private BigDecimal stateOfChargeW;
	/***/
	private BigDecimal maxCapacityW;
	/***/
	private BigDecimal maxChargingRateWh;
	/***/
	private BigDecimal maxDischargingRateWh;
	/***/
	private final BigDecimal minCapacityW = new BigDecimal(0d);

	private final Logger log = Logger.getLogger(StorageModel.class);

	/**
	 * @param id
	 * @param stateOfChargeW
	 * @param maxCapacityW
	 * @param maxChargingRateWh
	 * @param maxDischargingRateWh
	 */
	public StorageModel(String id, BigDecimal stateOfChargeW,
			BigDecimal maxCapacityW, BigDecimal maxChargingRateWh,
			BigDecimal maxDischargingRateWh) {
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
	public BigDecimal getStateOfChargeW() {
		return stateOfChargeW;
	}

	/**
	 * @param stateOfChargeW
	 * @throws Exception
	 */
	protected void setStateOfChargeW(BigDecimal stateOfChargeW)
			throws Exception {
		if (this.checkChargingRate(stateOfChargeW, this.maxCapacityW)) {
			this.stateOfChargeW = stateOfChargeW;
		} else {
			throw new Exception("Set Value of out Bound");
		}
	}

	/***/
	public BigDecimal getMaxCapacityW() {
		return maxCapacityW;
	}

	/***/
	public BigDecimal getMaxChargingRateWh() {
		return maxChargingRateWh;
	}

	/***/
	public BigDecimal getMaxDischargingRateWh() {
		return maxDischargingRateWh;
	}

	/**
	 * @param chargeWh
	 * @param dischargeWh
	 * @throws Exception
	 */
	public void chargeAndDischarge(BigDecimal chargeWh, BigDecimal dischargeWh)
			throws Exception {
		boolean exception = false;
		String exceptionMsg = "";
		if (!this.checkChargingRate(chargeWh, this.maxChargingRateWh)
				|| !this.checkChargingRate(dischargeWh,
						this.maxDischargingRateWh)) {
			exceptionMsg += EX_STORAGE_CHARGING_RATE;
			exception = true;
			chargeWh = (chargeWh.compareTo(this.maxChargingRateWh) <= 0) ? chargeWh
					.abs() : this.maxChargingRateWh;
			dischargeWh = (dischargeWh.compareTo(this.maxDischargingRateWh) <= 0) ? dischargeWh
					.abs() : this.maxChargingRateWh;

			log.debug("Charging rates out of bounds: " + this.toString());
		}
		BigDecimal nextStateOfCharge = MathUtility.roundBigDecimalTo(
				this.stateOfChargeW.add(chargeWh.subtract(dischargeWh)), 6);

		if (nextStateOfCharge.compareTo(this.minCapacityW) >= 0
				&& nextStateOfCharge.compareTo(this.maxCapacityW) <= 0) {
			this.setStateOfChargeW(nextStateOfCharge);
			log.debug("Charging Storage: " + this.toString());
		} else {
			nextStateOfCharge = (nextStateOfCharge.compareTo(this.minCapacityW) < 0) ? this.minCapacityW
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
	private boolean checkChargingRate(BigDecimal wh, BigDecimal chargingRate) {

		if (wh.compareTo(chargingRate) <= 0
				&& wh.compareTo(BigDecimal.ZERO) >= 0) {
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

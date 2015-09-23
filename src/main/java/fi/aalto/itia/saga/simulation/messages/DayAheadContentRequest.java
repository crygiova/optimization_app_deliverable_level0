package fi.aalto.itia.saga.simulation.messages;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;

/**
 * @author giovanc1
 *
 *         DayAheadContentRequest represents the content of the day ahead
 *         message request from Aggregator and Prosumers
 *
 */
public class DayAheadContentRequest implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * Spot Prices
	 */
	private BigDecimal[] spotPrice;
	/**
	 * Target Flex Up
	 */
	private BigDecimal[] tUp;
	/**
	 * Target Flex Down
	 */
	private BigDecimal[] tDw;
	/**
	 * Tmod
	 */
	private BigDecimal tsize;
	/**
	 * w parameter
	 */
	private BigDecimal w;
	/**
	 * 
	 */
	private Date startOfDay;

	public DayAheadContentRequest(BigDecimal[] spotPrice, BigDecimal[] tUp,
			BigDecimal[] tDw, BigDecimal tsize, BigDecimal w, Date startOfDay) {
		super();
		this.spotPrice = spotPrice;
		this.tUp = tUp;
		this.tDw = tDw;
		this.tsize = tsize;
		this.w = w;
		this.startOfDay = startOfDay;
	}

	public BigDecimal[] getSpotPrice() {
		return spotPrice;
	}

	public void setSpotPrice(BigDecimal[] spotPrice) {
		this.spotPrice = spotPrice;
	}

	public BigDecimal[] gettUp() {
		return tUp;
	}

	public void settUp(BigDecimal[] tUp) {
		this.tUp = tUp;
	}

	public BigDecimal[] gettDw() {
		return tDw;
	}

	public void settDw(BigDecimal[] tDw) {
		this.tDw = tDw;
	}

	public BigDecimal getTsize() {
		return tsize;
	}

	public void setTsize(BigDecimal tsize) {
		this.tsize = tsize;
	}

	public BigDecimal getW() {
		return w;
	}

	public void setW(BigDecimal w) {
		this.w = w;
	}

	public Date getStartOfDay() {
		return startOfDay;
	}

	public void setStartOfDay(Date startOfDay) {
		this.startOfDay = startOfDay;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	@Override
	public String toString() {
		return "DayAheadContentRequest [spotPrice="
				+ Arrays.toString(spotPrice) + ", tUp=" + Arrays.toString(tUp)
				+ ", tDw=" + Arrays.toString(tDw) + ", tsize=" + tsize + ", w="
				+ w + ", startOfDay=" + startOfDay + "]";
	}

}

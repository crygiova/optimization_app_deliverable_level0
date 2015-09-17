package fi.aalto.itia.saga.simulation.messages;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;

public class DayAheadContentRequest implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private double[] spotPrice;
	private double[] tUp;
	private double[] tDw;
	private double tsize;
	private double w;
	private Date startOfDay;

	public DayAheadContentRequest(double[] spotPrice, double[] tUp,
			double[] tDw, double tsize, double w, Date startOfDay) {
		super();
		this.spotPrice = spotPrice;
		this.tUp = tUp;
		this.tDw = tDw;
		this.tsize = tsize;
		this.w = w;
		this.startOfDay = startOfDay;
	}

	public double[] getSpotPrice() {
		return spotPrice;
	}

	public void setSpotPrice(double[] spotPrice) {
		this.spotPrice = spotPrice;
	}

	public double[] gettUp() {
		return tUp;
	}

	public void settUp(double[] tUp) {
		this.tUp = tUp;
	}

	public double[] gettDw() {
		return tDw;
	}

	public void settDw(double[] tDw) {
		this.tDw = tDw;
	}

	public double getTsize() {
		return tsize;
	}

	public void setTsize(double tsize) {
		this.tsize = tsize;
	}

	public double getW() {
		return w;
	}

	public void setW(double w) {
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

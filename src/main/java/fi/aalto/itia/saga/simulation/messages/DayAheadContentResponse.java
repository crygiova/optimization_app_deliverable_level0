package fi.aalto.itia.saga.simulation.messages;

import java.io.Serializable;
import java.util.Arrays;

import fi.aalto.itia.saga.data.TimeSequencePlan;

public class DayAheadContentResponse implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2L;
	private int id;
	private TimeSequencePlan p;
	private double[] dpUp;
	private double[] dpDown;
	private double j;

	/**
	 * 
	 */
	public DayAheadContentResponse() {
		super();
	}

	public DayAheadContentResponse(int id, TimeSequencePlan p, double[] dpUp,
			double[] dpDown, double j) {
		super();
		this.id = id;
		this.p = p;
		this.dpUp = dpUp;
		this.dpDown = dpDown;
		this.j = j;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public TimeSequencePlan getP() {
		return p;
	}

	public void setP(TimeSequencePlan p) {
		this.p = p;
	}

	public double[] getDpUp() {
		return dpUp;
	}

	public void setDpUp(double[] dpUp) {
		this.dpUp = dpUp;
	}

	public void setDp(double[] dpUp, double[] dpDown) {
		this.dpUp = dpUp;
		this.dpDown = dpDown;
	}

	public double[] getDpDown() {
		return dpDown;
	}

	public void setDpDown(double[] dpDown) {
		this.dpDown = dpDown;
	}

	public double getJ() {
		return j;
	}

	public void setJ(double j) {
		this.j = j;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	@Override
	public String toString() {
		return "DayAheadContentResponse [id=" + id + ", p=" + p + ", dpUp="
				+ Arrays.toString(dpUp) + ", dpDown=" + Arrays.toString(dpDown)
				+ ", j=" + j + "]";
	}

}

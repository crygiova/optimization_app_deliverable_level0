package fi.aalto.itia.saga.prosumer.util;

import java.util.Arrays;

import fi.aalto.itia.saga.data.TimeSequencePlan;

public class OptimizationResult {

	private int id;
	private TimeSequencePlan p;
	private double [] dpUp;
	private double [] dpDown;
	
	private double j;
	
	public OptimizationResult() {
		// TODO Auto-generated constructor stub
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

	@Override
	public String toString() {
		return "OptimizationResult [id=" + id + ", p=" + p + ", dpUp="
				+ Arrays.toString(dpUp) + ", dpDown=" + Arrays.toString(dpDown)
				+ ", j=" + j + "]";
	}
	
	
}

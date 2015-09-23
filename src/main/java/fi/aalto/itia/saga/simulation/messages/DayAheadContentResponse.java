package fi.aalto.itia.saga.simulation.messages;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Arrays;

import fi.aalto.itia.saga.data.TimeSequencePlan;

/**
 * @author giovanc1
 *
 *         This class represents the content of the dayahead messsage response
 */
public class DayAheadContentResponse implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2L;
	private int id;
	private TimeSequencePlan p;
	private BigDecimal[] dpUp;
	private BigDecimal[] dpDown;
	private BigDecimal j;

	/**
	 * 
	 */
	public DayAheadContentResponse() {
		super();
	}

	public DayAheadContentResponse(int id, TimeSequencePlan p,
			BigDecimal[] dpUp, BigDecimal[] dpDown, BigDecimal j) {
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

	public BigDecimal[] getDpUp() {
		return dpUp;
	}

	public void setDpUp(BigDecimal[] dpUp) {
		this.dpUp = dpUp;
	}

	public void setDp(BigDecimal[] dpUp, BigDecimal[] dpDown) {
		this.dpUp = dpUp;
		this.dpDown = dpDown;
	}

	public BigDecimal[] getDpDown() {
		return dpDown;
	}

	public void setDpDown(BigDecimal[] dpDown) {
		this.dpDown = dpDown;
	}

	public BigDecimal getJ() {
		return j;
	}

	public void setJ(BigDecimal j) {
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

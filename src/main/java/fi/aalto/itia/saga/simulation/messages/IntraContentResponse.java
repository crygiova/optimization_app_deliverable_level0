package fi.aalto.itia.saga.simulation.messages;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Arrays;

import fi.aalto.itia.saga.data.TimeSequencePlan;

public class IntraContentResponse implements Serializable {

	/**
	 * 
	 */
	private BigDecimal[] dPd;
	private BigDecimal[] dPu;
	private int id;
	private TimeSequencePlan p;

	private static final long serialVersionUID = -6562291994435475966L;

	public IntraContentResponse() {

	}
	
	public IntraContentResponse(int id, TimeSequencePlan p, BigDecimal[] dPd,
			BigDecimal[] dPu) {
		this.setId(id);
		this.setP(p);
		this.setdPd(dPd);
		this.setdPu(dPu);
	}

	public BigDecimal[] getdPd() {
		return dPd;
	}

	public void setdPd(BigDecimal[] dPd) {
		this.dPd = dPd;
	}

	public BigDecimal[] getdPu() {
		return dPu;
	}

	public void setdPu(BigDecimal[] dPu) {
		this.dPu = dPu;
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

	@Override
	public String toString() {
		return "IntraContentResponse [dPd=" + Arrays.toString(dPd) + ", dPu="
				+ Arrays.toString(dPu) + ", id=" + id + ", p=" + p + "]";
	}

}

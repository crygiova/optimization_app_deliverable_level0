package fi.aalto.itia.saga.simulation.messages;

import java.io.Serializable;
import java.math.BigDecimal;

public class IntraChangeConsumptionRequest implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5425197604581108136L;
	private boolean isUp;
	private BigDecimal rPercent;
	
	public IntraChangeConsumptionRequest() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param isUp
	 * @param rPercent
	 */
	public IntraChangeConsumptionRequest(boolean isUp, BigDecimal rPercent) {
		super();
		this.isUp = isUp;
		this.rPercent = rPercent;
	}

	public boolean isUp() {
		return isUp;
	}
	
	public boolean isDown() {
		return !isUp;
	}

	public void setUp(boolean isUp) {
		this.isUp = isUp;
	}

	public BigDecimal getRPercent() {
		return rPercent;
	}

	public void setrPercent(BigDecimal rPercent) {
		this.rPercent = rPercent;
	}
	
	

}

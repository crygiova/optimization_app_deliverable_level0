package fi.aalto.itia.saga.data;

/**
 * Tuple class which contains a Date type and a Double value called unit which
 * can represent different units as KW/h or €/KWh etc
 * 
 * @author giovanc1
 *
 * @param <Date>
 * @param <BigDecimal>
 */
public class TimeUnitTuple<Date, BigDecimal> {

	private final Date date;
	private final BigDecimal unit;

	public TimeUnitTuple(Date date, BigDecimal unit) {
		this.date = date;
		this.unit = unit;
	}

	public Date getDate() {
		return date;
	}

	public BigDecimal getUnit() {
		return unit;
	}

	// public v addToUnit(Double value) {
	// this.unit = this.unit.doubleValue + value;
	// }

	@Override
	public int hashCode() {
		return date.hashCode() ^ unit.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof TimeUnitTuple))
			return false;
		@SuppressWarnings({ "rawtypes", "unchecked" })
		TimeUnitTuple<Date, Double> pairo = (TimeUnitTuple) o;
		return this.date.equals(pairo.getDate())
				&& this.unit.equals(pairo.getUnit());
	}

	@Override
	public String toString() {
		return "TU[ " + date + ", " + unit + "]";
	}

}
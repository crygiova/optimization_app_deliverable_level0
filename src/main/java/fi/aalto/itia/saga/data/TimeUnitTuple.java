package fi.aalto.itia.saga.data;

@SuppressWarnings("hiding")
public class TimeUnitTuple<Date, Double> {

	private final Date date;
	private final Double unit;

	public TimeUnitTuple(Date date, Double unit) {
		this.date = date;
		this.unit = unit;
	}

	public Date getDate() {
		return date;
	}

	public Double getUnit() {
		return unit;
	}

	@Override
	public int hashCode() {
		return date.hashCode() ^ unit.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof TimeUnitTuple))
			return false;
		TimeUnitTuple<Date, Double> pairo = (TimeUnitTuple) o;
		return this.date.equals(pairo.getDate())
				&& this.unit.equals(pairo.getUnit());
	}

	@Override
	public String toString() {
		return "TimeEnergyTuple [date=" + date + ", units=" + unit + "]";
	}

}
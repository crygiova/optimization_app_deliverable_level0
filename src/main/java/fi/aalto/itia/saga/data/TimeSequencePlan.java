package fi.aalto.itia.saga.data;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;



public class TimeSequencePlan {

	private Date start;
	private List<TimeUnitTuple<Date, Double>> timEnergy = new ArrayList<TimeUnitTuple<Date, Double>>();

	public TimeSequencePlan(Date start) {
		this.start = start;
	}

	public Date getStart() {
		return start;
	}

	public void setStart(Date start) {
		this.start = start;
	}

	public void addTimeEnergyTuple(Date date, Double unit) {
		this.addTimeEnergyTuple(new TimeUnitTuple<Date, Double>(date, unit));
	}

	public void addTimeEnergyTuple(TimeUnitTuple<Date, Double> tet) {
		// log.debug("Adding hourly plan, t:" + tet.getDate() + " e:"
		// + tet.getWatts());
		this.timEnergy.add(tet);
	}

	public boolean updateTimeEnergyTuple(Date date, Double unit) {
		int index;
		index = indexOf(date);
		if (index == -1)
			return false;
		else {
			this.timEnergy.set(index, new TimeUnitTuple<Date, Double>(date,
					unit));
			return true;
		}
	}

	public int indexOf(Date time) {
		int index = -1;
		// TODO you can optimize when time
		while (++index < timEnergy.size()) {
			if (timEnergy.get(index).getDate().compareTo(time) == 0)
				return index;
		}
		return -1;
	}

	public TimeUnitTuple<Date, Double> getTimeEnergyTuple(int index) {

		if (index < 0 || index >= timEnergy.size())
			return null;
		return timEnergy.get(index);
	}

	public List<TimeUnitTuple<Date, Double>> getTimEnergy() {
		return timEnergy;
	}

	public void setTimEnergy(List<TimeUnitTuple<Date, Double>> timEnergy) {
		// TODOthis.timEnergy = timEnergy;
	}

	public static Date calculateNextHour(Date start, int add) {
		GregorianCalendar gc = (GregorianCalendar) GregorianCalendar
				.getInstance();
		gc.setTime(start);
		gc.add(Calendar.HOUR_OF_DAY, add);
		return (Date) gc.getTime().clone();
	}
	

}

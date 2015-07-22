package fi.aalto.itia.saga.simulation;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class SimulationCalendarUtils {

	private static GregorianCalendar gc = (GregorianCalendar) GregorianCalendar
			.getInstance();
	
	public static boolean isMidnight(SimulationCalendar sc) {
		if (sc.get(Calendar.HOUR_OF_DAY) == 0)
			return true;
		return false;
	}

	public static Date calculateNextHour(Date start, int add) {
		gc.setTime(start);
		gc.add(Calendar.HOUR_OF_DAY, add);
		return (Date) gc.getTime().clone();
	}

	public static Date getMidnight(Date d) {
		gc.setTime(d);
		gc.set(Calendar.HOUR_OF_DAY, 0);
		gc.set(Calendar.MINUTE, 0);
		gc.set(Calendar.SECOND, 0);
		return gc.getTime();
	}
}

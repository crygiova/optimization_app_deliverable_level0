package fi.aalto.itia.saga.data;

import java.util.Comparator;

//TODO not in use yet
public class TaskScheduleComparator implements Comparator<TaskSchedule> {

	public TaskScheduleComparator() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public int compare(TaskSchedule o1, TaskSchedule o2) {
		if (o1.getHour() == o2.getHour()) {
			if (o1.getPriority() < o2.getPriority())
				return -1;
			else if (o1.getPriority() > o2.getPriority())
				return 1;
			else
				return 0;

		} else if (o1.getHour() < o2.getHour()) {
			return -1;
		} else {
			return 1;
		}
	}

}

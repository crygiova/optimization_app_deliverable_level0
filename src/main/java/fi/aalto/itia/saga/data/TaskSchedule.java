package fi.aalto.itia.saga.data;

public class TaskSchedule implements Comparable<TaskSchedule> {

	private String taskName;
	private Integer hour;
	private Integer priority;

	public TaskSchedule(String taskName, Integer hour, Integer priority) {
		super();
		this.taskName = taskName;
		this.hour = hour;
		this.priority = priority;
	}

	public Integer getPriority() {
		return priority;
	}

	public String getTaskName() {
		return taskName;
	}

	public Integer getHour() {
		return hour;
	}

	@Override
	public int compareTo(TaskSchedule o2) {
		if (this.getHour() == o2.getHour()) {
			if (this.getPriority() < o2.getPriority())
				return -1;
			else if (this.getPriority() > o2.getPriority())
				return 1;
			else
				return 0;

		} else if (this.getHour() < o2.getHour()) {
			return -1;
		} else {
			return 1;
		}
	}
}

package fi.aalto.itia.saga.util;

public class MathUtility {

	public static double roundDoubleTo(double value, int decimal) {
		double power = Math.pow(10d, decimal);
		return Math.round(value * power) / power;
	}

	public static double convertMWhtoKWh(double price) {
		return price / 1000d;
	}
}

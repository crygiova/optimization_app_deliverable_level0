package fi.aalto.itia.saga.util;

import java.math.BigDecimal;

public class MathUtility {

	public static BigDecimal roundBigDecimalTo(BigDecimal value, int decimal) {
		double power = Math.pow(10d, decimal);
		return BigDecimal.valueOf(Math.round(value.doubleValue() * power)
				/ power);
	}

	public static BigDecimal roundDoubleTo(double value, int decimal) {
		double power = Math.pow(10d, decimal);
		return BigDecimal.valueOf(Math.round(value * power) / power);
	}

	public static BigDecimal convertMWhtoKWh(double price) {
		return BigDecimal.valueOf(price).divide(new BigDecimal(1000d));
	}
}

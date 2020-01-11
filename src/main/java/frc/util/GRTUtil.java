package frc.util;

public class GRTUtil {

	// GRTUtil because there are too many other Util classes
	private GRTUtil() {
	}

	public static final double TWO_PI = 2 * Math.PI;

	public static double clamp(double min, double x, double max) {
		return Math.min(Math.max(min, x), max);
	}

	public static double positiveMod(double x, double mod) {
		return (((x % mod) + mod) % mod);
	}

	/** Returns whether x is between min and max, inclusive */
	public static boolean inRange(double min, double x, double max) {
		return x >= min && x <= max;
	}

	public static double distanceToAngle(double from, double to) {
		from = positiveMod(from, TWO_PI);
		to = positiveMod(to, TWO_PI);
		double error = to - from;
		if (Math.abs(error) > Math.PI) {
			error -= Math.signum(error) * TWO_PI;
		}
		return error;
	}

	/**
	 * Takes an original range, a new range, and a number to stretch (or shrink).
	 * See
	 * https://math.stackexchange.com/questions/914823/shift-numbers-into-a-different-range
	 * 
	 * @param origMin
	 *                    The minimum of the original range
	 * @param origMax
	 *                    The maximum of the original range
	 * @param newMin
	 *                    The minimum of the range to stretch/shrink to
	 * @param newMax
	 *                    The maximum of the range to stretch/shrink to
	 * @param x
	 *                    The number to stretch
	 */
	public static double toRange(double origMin, double origMax, double newMin, double newMax, double x) {
		return newMin + ((newMax - newMin) / (origMax - origMin)) * (x - origMin);
	}
}

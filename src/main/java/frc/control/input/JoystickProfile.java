package frc.control.input;

import frc.gen.BIGData;
import frc.util.GRTUtil;

public class JoystickProfile {
	private static final double DEFAULT_DEADBAND = 0.1;

	/** Array of [original, new] mappings used to define the joystick profile. */
	private static double[][] profilingPoints;

	private JoystickProfile() {
	}

	public static void init() {
		profilingPoints = new double[2][2];
		updateProfilingPoints();
	}

	public static void updateProfilingPoints() {
		profilingPoints[0][0] = BIGData.getJoystickX1();
		profilingPoints[0][1] = BIGData.getJoystickY1();
		profilingPoints[1][0] = BIGData.getJoystickX2();
		profilingPoints[1][1] = BIGData.getJoystickY2();
	}

	public static double applyProfile(double x) {
		double signum = Math.signum(x);
		// first apply deadband, then scale back to original range
		double ans = applyDeadband(Math.abs(x));
		if (ans != 0) {
			ans -= DEFAULT_DEADBAND;
		}
		ans = GRTUtil.transformation(0, 1 - DEFAULT_DEADBAND, 0, 1, ans);
		// apply profiling
		if (GRTUtil.inRange(0, ans, profilingPoints[0][0])) {
			ans = GRTUtil.transformation(0, profilingPoints[0][0], 0, profilingPoints[0][1], ans);
		} else if (GRTUtil.inRange(profilingPoints[0][0], ans, profilingPoints[1][0])) {
			ans = GRTUtil.transformation(profilingPoints[0][0], profilingPoints[1][0], profilingPoints[0][1],
					profilingPoints[1][1], ans);
		} else {
			ans = GRTUtil.transformation(profilingPoints[1][0], 1.01, profilingPoints[1][1], .99, ans);
		}
		return ans * signum;
	}

	/** applies the requested deadband to x. */
	public static double applyDeadband(double x, double deadband) {
		return (Math.abs(x) > deadband ? x : 0);
	}

	/** Applies the default deadband to the value passed in */
	public static double applyDeadband(double x) {
		return applyDeadband(x, DEFAULT_DEADBAND);
	}

	/** squares x while keeping the original sign. */
	public static double signedSquare(double x) {
		return Math.copySign(x * x, x);
	}

	/** applies the deadband to x and returns the signed square of the result */
	public static double clipAndSquare(double x) {
		return signedSquare(applyDeadband(x));
	}

}
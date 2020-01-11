package frc.input;

import java.util.ArrayList;
import java.util.Arrays;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.config.Config;
import frc.util.GRTUtil;

public class JoystickProfile {
	private static final double DEFAULT_DEADBAND = 0.1;

	/** Array of [original, new] mappings used to define the joystick profile. */
	private static double[][] profilingPoints;

	private static String dashboardProfileStr0 = "DB/String 5";
	private static String dashboardProfileStr1 = "DB/String 6";

	private JoystickProfile() {
	}

	public static void init() {
		profilingPoints = new double[2][2];
		profilingPoints[0][0] = Config.getDouble("joystick_x1");
		profilingPoints[0][1] = Config.getDouble("joystick_y1");
		SmartDashboard.putString(dashboardProfileStr0, profilingPoints[0][0] + ", " + profilingPoints[0][1]);
		profilingPoints[1][0] = Config.getDouble("joystick_x2");
		profilingPoints[1][1] = Config.getDouble("joystick_y2");
		SmartDashboard.putString(dashboardProfileStr1, profilingPoints[1][0] + ", " + profilingPoints[1][1]);
	}

	public static void updateProfilingPoints() {
		String[][] profilingPointsStr = new String[2][];
		profilingPointsStr[0] = SmartDashboard.getString(dashboardProfileStr0, "not found").split(",");
		profilingPointsStr[1] = SmartDashboard.getString(dashboardProfileStr1, "not found").split(",");
		try {
			double[][] profilingPointsTemp = new double[2][2];
			for (int r = 0; r < profilingPointsTemp.length; r++) {
				for (int c = 0; c < profilingPointsTemp[r].length; c++) {
					profilingPointsTemp[r][c] = Double.parseDouble(profilingPointsStr[r][c]);
				}
			}
			profilingPoints = profilingPointsTemp;
			System.out.println("Successfully set the new joystick profiling points");
		} catch (Exception e) {
			// put the current ones on the dashboard instead
			SmartDashboard.putString(dashboardProfileStr0, profilingPoints[0][0] + ", " + profilingPoints[0][1]);
			SmartDashboard.putString(dashboardProfileStr1, profilingPoints[1][0] + ", " + profilingPoints[1][1]);
		}
		Config.put("joystick_x1", profilingPoints[0][0]);
		Config.put("joystick_y1", profilingPoints[0][1]);
		Config.put("joystick_x2", profilingPoints[1][0]);
		Config.put("joystick_y2", profilingPoints[1][1]);
		Config.updateConfigFile();
	}

	public static double applyProfile(double x) {
		double signum = Math.signum(x);
		// first apply deadband, then scale back to original range
		double ans = applyDeadband(Math.abs(x));
		if (ans != 0) {
			ans -= DEFAULT_DEADBAND;
		}
		ans = GRTUtil.toRange(0, 1-DEFAULT_DEADBAND, 0, 1, ans);
		// apply profiling
		if (GRTUtil.inRange(0, ans, profilingPoints[0][0])) {
			ans = GRTUtil.toRange(0, profilingPoints[0][0], 0, profilingPoints[0][1], ans);
		} else if (GRTUtil.inRange(profilingPoints[0][0], ans, profilingPoints[1][0])) {
			ans = GRTUtil.toRange(profilingPoints[0][0], profilingPoints[1][0], profilingPoints[0][1],
					profilingPoints[1][1], ans);
		} else {
			ans = GRTUtil.toRange(profilingPoints[1][0], 1.01, profilingPoints[1][1], .99, ans);
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
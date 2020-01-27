package frc.gen;

import java.util.HashMap;
import java.util.Map;

import frc.positiontracking.fieldmap.geometry.Vector;
import frc.swerve.SwerveData;

public class BIGData {

	private static Map<String, String> map;

	public static final int FR_WHEEL = 0;
	public static final int BR_WHEEL = 1;
	public static final int BL_WHEEL = 2;
	public static final int FL_WHEEL = 3;

	public static void start() {
		map = new HashMap<String, String>();
		Config.start(map);
	}

	private static void existenceCheck(String key, String type) {
		if (!map.containsKey(key)) {
			switch (type) {
			case "boolean":
				put(key, false);
				break;
			case "double":
				put(key, 0.0);
				break;
			case "int":
				put(key, 0);
				break;
			case "String":
				put(key, "");
				break;
			}
		}
	}

	/**
	 * Get the boolean config value corresponding to the key passed in.
	 * 
	 * @return The corresponding boolean value, or false if the key was invalid
	 */
	public static boolean getBoolean(String key) {
		existenceCheck(key, "boolean");
		return Boolean.parseBoolean(map.get(key));
	}

	/**
	 * Get the double config value corresponding to the key passed in.
	 * 
	 * @return The corresponding double value, or 0.0 if the key was invalid
	 */
	public static double getDouble(String key) {
		existenceCheck(key, "double");
		try {
			return Double.parseDouble(map.get(key));
		} catch (Exception e) {
			return 0.0;
		}
	}

	/**
	 * Get the int config value corresponding to the key passed in.
	 * 
	 * @return The corresponding integer value, or -1 if the key was not
	 *         found/invalid
	 */
	public static int getInt(String key) {
		existenceCheck(key, "int");
		try {
			return Integer.parseInt(map.get(key));
		} catch (Exception e) {
			return -1;
		}
	}

	/**
	 * Get the string value corresponding to the key passed in.
	 * 
	 * @return The corresponding string value, or the empty string if the key was
	 *         not found/invalid
	 */
	public static String getString(String key) {
		existenceCheck(key, "String");
		return map.get(key);
	}

	/**
	 * Pass in one of four constants: FR_WHEEL, BR_WHEEL, BL_WHEEL, FL_WHEEL to get
	 * the wheel's name "fr", "br", "bl", "fl"
	 */
	public static String getWheelName(int wheelNum) {
		switch (wheelNum) {
		case FR_WHEEL:
			return "fr";
		case BR_WHEEL:
			return "br";
		case BL_WHEEL:
			return "bl";
		case FL_WHEEL:
			return "fl";
		default:
			return "unknown_wheel";
		}
	}

	/**
	 * Request translational and angular velocity of the robot
	 * 
	 * @param vx
	 *               requested x velocity from -1.0 to 1.0
	 * @param vy
	 *               requested y velocity from -1.0 to 1.0
	 * @param w
	 *               requested angular velocity
	 */
	public static void requestDrive(double vx, double vy, double w) {
		put("requested_vx", vx);
		put("requested_vy", vy);
		put("requested_w", w);
	}

	/** get the requested x velocity of the robot */
	public static double getRequestedVX() {
		return getDouble("requested_vx");
	}

	/** get the requested y velocity of the robot */
	public static double getRequestedVY() {
		return getDouble("requested_vy");
	}

	/** get the requested angular velocity of the robot */
	public static double getRequestedW() {
		return getDouble("requested_w");
	}

	/** set swerve data */
	public static void setSwerveData(SwerveData data) {
		put("gyro_ang", data.gyroAngle);
		put("gyro_w", data.gyroW);
		put("enc_vx", data.encoderVX);
		put("enc_vy", data.encoderVY);
		put("enc_w", data.encoderW);
	}

	/** get swerve data */
	public static SwerveData getSwerveData() {
		return new SwerveData(getDouble("gyro_ang"), getDouble("gyro_w"), getDouble("enc_vx"), getDouble("enc_vy"),
				getDouble("enc_w"));
	}

	/** set current position */
	public static void setPosition(Vector pos, String s) {
		put(s + "_pos_x", pos.x);
		put(s + "_pos_y", pos.y);
	}

	/** get current position */
	public static Vector getPosition(String s) {
		Vector vec = new Vector(getDouble(s + "_pos_x"), getDouble(s + "_pos_y"));
		return vec;
	}

	public static void setTarget(double x, double y) {
		put("tar_x", x);
		put("tar_y", y);
	}

	public static Vector getTarget() {
		return new Vector(getDouble("tar_x"), getDouble("tar_y"));
	}

	/** set the gyro's angle */
	public static void putGyroAngle(double angle) {
		put("gyro_ang", angle);
	}

	/** set the gyro's rate of rotation */
	public static void putGyroW(double w) {
		put("gyro_w", w);
	}

	/** get the gyro's current angle */
	public static double getGyroAngle() {
		return getDouble("gyro_ang");
	}

	/** get the gyro's angular velocity */
	public static double getGyroW() {
		return getDouble("gyro_w");
	}

	/** Request that swerve be zeroed. */
	public static void putZeroSwerveRequest(boolean request) {
		put("zero_swerve", request);
	}

	/** Get whether swerve has been requested to be zeroed. */
	public static boolean getZeroSwerveRequest() {
		return getBoolean("zero_swerve");
	}

	/** Request that the gyro be zeroed. */
	public static void putZeroGyroRequest(boolean request) {
		put("zero_gyro", request);
	}

	/** Get whether the gyro has been requested to be zeroed. */
	public static boolean getZeroGyroRequest() {
		return getBoolean("zero_gyro");
	}

	public static void putMechs(double one, double two_a, double two_b) {
		// TODO: add the rest of the mechs
		put("one_wheel_shooter", one);
		put("wheel_a_rpm", two_a);
		put("wheel_b_rpm", two_b);
	}

	/** set the original value of the first joystick profile point */
	public static void setJoystickX1(double x1) {
		put("joystick_x1", x1);
	}

	/** set the new value of the first joystick profile point */
	public static void setJoystickY1(double y1) {
		put("joystick_y1", y1);
	}

	/** set the original value of the second joystick profile point */
	public static void setJoystickX2(double x2) {
		put("joystick_x2", x2);
	}

	/** set the new value of the second joystick profile point */
	public static void setJoystickY2(double y2) {
		put("joystick_y2", y2);
	}

	/** get the original value of the first joystick profile point */
	public static double getJoystickX1() {
		return getDouble("joystick_x1");
	}

	/** get the new value of the first joystick profile point */
	public static double getJoystickY1() {
		return getDouble("joystick_y1");
	}

	/** get the original value of the second joystick profile point */
	public static double getJoystickX2() {
		return getDouble("joystick_x2");
	}

	/** get the new value of the second joystick profile point */
	public static double getJoystickY2() {
		return getDouble("joystick_y2");
	}

	/** put a wheel's zero position. only Swerve should call this function */
	public static void putWheelZero(String name, int zeroPos) {
		put(name + "_offset", zeroPos);
	}

	/** get a wheel's zero position from a name "fr", "br", "bl", "fl". */
	public static int getWheelZero(String wheelName) {
		return getInt(wheelName + "_offset");
	}

	/** get a wheel's zero position from a wheel number */
	public static int getWheelZero(int wheelNum) {
		return getInt(getWheelName(wheelNum) + "_offset");
	}

	/** put a wheel's current raw drive speed */
	public static void putWheelRawDriveSpeed(String name, double driveSpeed) {
		put(name + "_raw_drive", driveSpeed);
	}

	/** get a wheel's current raw drive speed from a name "fr", "br", "bl", "fl". */
	public static double getWheelRawDriveSpeed(String name) {
		return getDouble(name + "_raw_drive");
	}

	/** get a wheel's current raw drive speed from a wheel number */
	public static double getWheelRawDriveSpeed(int wheelNum) {
		return getDouble(getWheelName(wheelNum) + "_raw_drive");
	}

	/** put a wheel's current raw rotate speed */
	public static void putWheelRawRotateSpeed(String name, double rotateSpeed) {
		put(name + "_raw_rotate", rotateSpeed);
	}

	/**
	 * get a wheel's current raw rotate speed from a name "fr", "br", "bl", "fl".
	 */
	public static double getWheelRawRotateSpeed(String wheelName) {
		return getDouble(wheelName + "_raw_rotate");
	}

	/** get a wheel's current raw rotate speed from a wheel number */
	public static double getWheelRawRotateSpeed(int wheelNum) {
		return getDouble(getWheelName(wheelNum) + "_raw_rotate");
	}

	/** set the config file message to display to drivers */
	public static void putConfigFileMsg(String msg) {
		put("config_msg", msg);
	}

	/** get the config file message to display to drivers */
	public static String getConfigFileMsg() {
		return getString("config_msg");
	}

	/** put (or update) a key/value mapping into the map */
	public static void put(String key, String val) {
		map.put(key, val);
	}

	/** put (or update) a key/value mapping into the map */
	public static void put(String key, double val) {
		map.put(key, "" + val);
	}

	/** put (or update) a key/value mapping into the map */
	public static void put(String key, int val) {
		map.put(key, "" + val);
	}

	/** put (or update) a key/value mapping into the map */
	public static void put(String key, boolean val) {
		map.put(key, "" + val);
	}

	/** calls Config.updateConfigFile */
	public static void updateConfigFile() {
		Config.updateConfigFile();
	}

	/** calls Config.resetTempConfigFile() */
	public static void resetTempConfigFile() {
		Config.resetTempConfigFile();
	}

	/**
	 * Change whether we use the deploy time config file or the temporary config
	 * file ON STARTUP. This function does not modify current program state.
	 */
	public static void changeStartupConfigFile(boolean useDeploy) {
		Config.changeStartupConfigFile(useDeploy);
	}

	/** print the current config mappings to the console */
	public static void printConfigMappings() {
		Config.printConfigMappings();
	}
}
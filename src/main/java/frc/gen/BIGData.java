package frc.gen;

import java.util.HashMap;
import java.util.Map;

import frc.swerve.SwerveData;
import frc.gen.Config;

public class BIGData {

	private static Map<String, String> map;

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

	public static void setSwerveData(SwerveData data) {
		put("gyro_ang", data.gyroAngle);
		put("gyro_w", data.gyroW);
		put("enc_vx", data.encoderVX);
		put("enc_vy", data.encoderVY);
		put("enc_w", data.encoderW);
	}

	/** set the gyro's angle */
	public static void setGyroAngle(double angle) {
		put("gyro_ang", angle);
	}

	/** set the gyro's rate of rotation */
	public static void setGyroW(double w) {
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
	public static void setZeroSwerveRequest(boolean request) {
		put("zero_swerve", request);
	}

	/** Get whether swerve has been requested to be zeroed. */
	public static boolean getZeroSwerveRequest() {
		return getBoolean("zero_swerve");
	}

	/** Request that the gyro be zeroed. */
	public static void setZeroGyroRequest(boolean request) {
		put("zero_gyro", request);
	}

	/** Get whether the gyro has been requested to be zeroed. */
	public static boolean getZeroGyroRequest() {
		return getBoolean("zero_gyro");
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

	/**
	 * set the front right wheel's zero position. Only Swerve should call this
	 * function.
	 */
	public static void setFrZero(int frzero) {
		put("fr_offset", frzero);
	}

	/**
	 * set the back right wheel's zero position. Only Swerve should call this
	 * function.
	 */
	public static void setBrZero(int brzero) {
		put("br_offset", brzero);
	}

	/**
	 * set the back left wheel's zero position. Only Swerve should call this
	 * function.
	 */
	public static void setBlZero(int blzero) {
		put("bl_offset", blzero);
	}

	/**
	 * set the front left wheel's zero position. Only Swerve should call this
	 * function.
	 */
	public static void setFlZero(int flzero) {
		put("fl_offset", flzero);
	}

	/** get the front right wheel's zero position */
	public static int getFrZero() {
		return getInt("fr_offset");
	}

	/** get the back right wheel's zero position */
	public static int getBrZero() {
		return getInt("br_offset");
	}

	/** get the back left wheel's zero position */
	public static int getBlZero() {
		return getInt("bl_offset");
	}

	/** get the front left wheel's zero position */
	public static int getFlZero() {
		return getInt("fl_offset");
	}

	/** set the config file message to display to drivers */
	public static void setConfigFileMsg(String msg) {
		put("config_msg", msg);
	}

	/** get the config file message to display to drivers */
	public static String getConfigFileMsg() {
		return getString("config_,sg");
	}

	/** put (or update) a key/value mapping into the map */
	public static void put(String key, String val) {
		map.put(key, val);
	}

	public static void updateCamera(double r, double a) {
		put("camera_azimuth", a);
		put("camera_distance", r);
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
package frc.gen;

import java.util.HashMap;
import java.util.Map;

import frc.swerve.SwerveData;
import frc.util.GRTUtil;

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

	public static boolean getDisabled(int i) {
		switch (i) {
		case 1:
			return getBoolean("stage_1_disabled");
		case 2:
			return getBoolean("stage_2_disabled");
		case 3:
			return getBoolean("stage_3_disabled");
		}
		return false;
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

	public static void setAngle(double theta) {
		setPIDTrue();
		put("requested_angle", theta);
	}

	public static void setPIDTrue() {
		put("PID?", true);
	}

	public static void setPIDFalse() {
		put("PID?", false);
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

	// TODO ADD MECH BIGDATA STUFF HERE!
	/** Set the state of the linkage; true=on, false=off */
	public static void requestLinkageState(boolean state) {
		put("linkage_state", state);
	}

	/** Get the state of the linkage; true=on, false=off */
	public static boolean getLinkageState() {
		return getBoolean("linkage_state");
	}

	/** Set the state of intake; true=extended, false=retracted */
	public static void requestIntakeState(boolean state) {
		put("intake_state", state);
	}

	/** Get the state of intake; true=extended, false=retracted */
	public static boolean getIntakeState() {
		return getBoolean("intake_state");
	}

	/** Set the state of shooter; false = manual, true = automatic */
	public static void putShooterState(boolean state) {
		put("shooter_state", state);
	}

	/** Get the state of shooter; false = manual, true = automatic */
	public static boolean getShooterState() {
		return getBoolean("shooter_state");
	}

	/** Set the state of storage; true = automatic, false = manual */
	public static void putStorageState(boolean state) {
		put("storage_state", state);
	}

	/** Get the state of storage; true = automatic, false = manual */
	public static boolean getStorageState() {
		return getBoolean("storage_state");
	}

	/** Set the motor spin speed with percentage motor output */
	public static void requestStorageSpeed(double speed) {
		put("storage_speed", GRTUtil.clamp(-1.0, speed, 1.0));
	}

	/** Get the motor spin speed with percentage motor output */
	public static double getStorageSpeed() {
		return getDouble("storage_speed");
	}

	/** Set the motor spin speed with percentage motor output */
	public static void requestStorageSpeedAuto(double speed) {
		put("storage_speed_auto", GRTUtil.clamp(-1.0, speed, 1.0));
	}

	/** Get the motor spin speed with percentage motor output */
	public static double getStorageSpeedAuto() {
		return getDouble("storage_speed_auto");
	}

	/**
	 * set the output speed of the winch motor, from -1.0 to 1.0 TODO maybe only
	 * make it turn one way
	 */
	public static void requestWinchSpeed(double output) {
		put("winch_speed", GRTUtil.clamp(-1.0, output, 1.0));
	}

	public static double getWinchSpeed() {
		return getDouble("winch_speed");
	}

	public static void putWinchState(boolean state) {
		put("winch_state", state);
	}

	public static boolean getWinchState() {
		return getBoolean("winch_state");
	}

	public static void putSpinnerState(boolean state) {
		put("spinner_state", state);
	}

	public static boolean getSpinnerState() {
		return getBoolean("spinner_state");
	}

	public static void putButtonClick(String click) {
		put("button_click", click);
	}

	public static String getButtonClick() {
		return getString("button_click");
	}

	public static void putCanvasClick(String click) {
		put("canvas_click", click);
	}

	public static String getCanvasClick() {
		return getString("canvas_click");
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
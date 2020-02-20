package frc.gen;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import frc.pathfinding.fieldmap.geometry.*;
import frc.swerve.SwerveData;
import frc.util.GRTUtil;

public class BIGData {

	private static Map<String, String> map;

	// RPM map of <distance (inches), RPM> for when the hood is up
	public static TreeMap<Integer, Integer> upRPMMap;
	// RPM map of <distance (inches), RPM> for when the hood is down
	public static TreeMap<Integer, Integer> downRPMMap;

	public static final int FR_WHEEL = 0;
	public static final int BR_WHEEL = 1;
	public static final int BL_WHEEL = 2;
	public static final int FL_WHEEL = 3;

	public static void start() {
		map = new HashMap<String, String>();
		upRPMMap = new TreeMap<Integer, Integer>();
		downRPMMap = new TreeMap<Integer, Integer>();
		Config.start(map, upRPMMap, downRPMMap);
		put("stage_1_disabled", false);
		put("stage_2_disabled", false);
		put("stage_3_disabled", false);
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

	public static int getWheelNum(String wheelName) {
		wheelName = wheelName.toLowerCase();
		switch (wheelName) {
		case "fr":
			return FR_WHEEL;
		case "br":
			return BR_WHEEL;
		case "bl":
			return BL_WHEEL;
		case "fl":
			return FL_WHEEL;
		default:
			return -1;
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

	/** Set the angle of the robot, in degrees, to turn to. */
	public static void setAngle(double theta) {
		setPIDTrue();
		put("requested_angle", theta);
	}

	public static double getRequestedAngle() {
		return getDouble("requested_angle");
	}

	/** Request that swerve use PID loop to go to the requested angle */
	public static void setPIDTrue() {
		put("PID?", true);
	}

	/** Request that swerve stop using PID loop to go to the requested angle */
	public static void setPIDFalse() {
		put("PID?", false);
	}

	/** @return whether swerve should use PID or not */
	public static boolean isPID() {
		return getBoolean("PID?");
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

	/** manually set what the current position is */
	public static void setManualPos(double x, double y) {
		put("manual_x", x);
		put("manual_y", y);
		put("manual_change_pos", true);
	}

	/** get the manually set position */
	public static Vector getManualPos() {
		put("manual_change_pos", false);
		return new Vector(getDouble("manual_x"), getDouble("manual_y"));
	}

	/** set the gyro's angle */
	public static void putGyroAngle(double angle) {
		put("gyro_ang", angle);
	}

	/** set the gyro's rate of rotation */
	public static void putGyroW(double w) {
		put("gyro_w", w);
	}

	/** get the gyro's current angle in degrees */
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

	/** request to zero a single swerve module */
	public static void putZeroIndivSwerveRequest(int wheelNum, boolean setTo) {
		put("zero_module_" + wheelNum, setTo);
	}

	/** get whether a single swerve module has been requested to be zeroed */
	public static boolean getZeroIndivSwerveRequest(int wheelNum) {
		return getBoolean("zero_module_" + wheelNum);
	}

	/** Request that the gyro be zeroed. */
	public static void putZeroGyroRequest(boolean request) {
		put("zero_gyro", request);
	}

	/** Get whether the gyro has been requested to be zeroed. */
	public static boolean getZeroGyroRequest() {
		return getBoolean("zero_gyro");
	}

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

	/** Get the motor spin speed with percentage motor output */
	public static double getStorageSpeedAuto() {
		return getDouble("storage_speed_load");
	}

	/**
	 * set the output speed of the winch motor, from -1.0 to 1.0 TODO maybe only
	 * make winch turn one way
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

	public static void setSpinnerState(boolean state) {
		put("spinner_state", state);
	}

	public static boolean getSpinnerState() {
		return getBoolean("spinner_state");
	}

	public static double getSpinnerSpeed() {
		return getDouble("spinner_manual_speed");
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

	/**
	 * update lidar values
	 * 
	 * @param azimuth
	 *                     angle in radians between the 0 rad line of lidar and the
	 *                     line from lidar to center of target. the plane of this
	 *                     angle is parallel to the floor
	 * @param range
	 *                     distance from lidar to center of the target in the plane
	 *                     parallel to the floor
	 * @param relAngle
	 *                     angle of the robot relative to target, where 0 rad is
	 *                     right in front of target, and right of target are
	 *                     positive angles, and left of target are negative angles.
	 * @param quality
	 *                     the quality of the data, lower is better
	 */
	public static void updateLidar(double azimuth, double range, double relAngle, double quality) {
		put("lidar_range", range);
		put("lidar_azimuth", azimuth);
		put("lidar_rel_angle", relAngle);
		put("lidar_quality", quality);
	}

	/** put (or update) a key/value mapping into the map */
	public static void put(String key, String val) {
		map.put(key, val);
	}

	public static void putJetsonCameraConnected(boolean connected) {
		put("jetson_camera_connected", connected);
	}

	public static boolean getJetsonCameraConnected() {
		return getBoolean("jetson_camera_connected");
	}

	/**
	 * @param r
	 *              the range (distance from the target horizontally, in inches)
	 * @param a
	 *              the azimuth (in degrees, where positive means camera is pointed
	 *              to the left)
	 * @param x
	 *              TODO TELL ME WHAT THIS IS!
	 * @param y
	 *              TODO TELL ME WHAT THIS IS!!!!!!!!!!!!!!
	 */
	public static void updateCamera(double r, double a, double x, double y) {
		put("camera_azimuth", a);
		put("camera_range", r);
		put("relative_x", x);
		put("relative_y", y);
	}

	/** TODO COMMENT CODE PLS! */
	public static Vector getCameraPos() {
		if (getDouble("camera_range") != 0) {
			Vector v = new Vector(getDouble("relative_x"), getDouble("relative_y"));
			return v;
		}
		return null;
	}

	/**
	 * @param r
	 *              distance in inches
	 */
	public static void updateLidar(double r) {
		put("lidar_range", r);
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

	/**
	 * resets the local RPM config file with the corresponding deploy-file RPM
	 * config file
	 */
	public static void resetLocalRPMConfigFile() {
		Config.resetLocalRPMConfigFile();
	}

	/** resets the local config file (that contains the swerve zeros) */
	public static void resetLocalConfigFile() {
		Config.resetLocalConfigFile();
	}

	/**
	 * Writes the current local mappings to the local config file in home/lvuser.
	 * (updates swerve zeroes in local file)
	 */
	public static void updateLocalConfigFile() {
		Config.updateLocalConfigFile();
	}

	public static void updateLocalRPMConfigFile() {
		Config.updateLocalRPMConfigFile();
	}

	public static String getMechToRun() {
		return getString("mech_to_run");
	}
}
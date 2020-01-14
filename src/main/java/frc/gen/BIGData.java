package frc.gen;

import java.util.Map;

import frc.swerve.SwerveData;
import frc.gen.Config;

public class BIGData {

    private static Map<String, String> map;

    public static void start() {
        Config.start();
		map = Config.getMap();
	}

	private static void existenceCheck(String key, String type) {
		if ( !map.containsKey(key) ) {
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
			}
		}
	}

    /** Get the boolean config value corresponding to the key passed in.
	 * @return The corresponding boolean value, or false if the key was invalid
	 */
	public static boolean getBoolean(String key) {
		existenceCheck(key, "boolean");
		return Boolean.parseBoolean(map.get(key));
    }

    /** Get the double config value corresponding to the key passed in.
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
    
    /** Get the int config value corresponding to the key passed in.
	 * @return The corresponding integer value, or -1 if the key was not found/invalid
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
	 * Set the translational and angular velocity of the robot
	 * @param vx requested x velocity from -1.0 to 1.0
	 * @param vy requested y velocity from -1.0 to 1.0
	 * @param w requested angular velocity
	 */
	public static void setDrive(double vx, double vy, double w) {
		put("drive_vx", vx);
		put("drive_vy", vy);
		put("drive_vw", w);
	}

	public static void setSwerveData(SwerveData data) {
		put("gyro_ang", data.gyroAngle);
		put("gyro_w", data.gyroW);
		put("enc_vx", data.encoderVX);
		put("enc_vy", data.encoderVY);
		put("enc_w", data.encoderW);
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

	/** put (or update) a key/value mapping into the map */
	public static void put(String key, double val) {
		map.put(key, "" + val);
	}

	/** put (or update) a key/value mapping into the map  */
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

	/** Change whether we use the deploy time config file or the temporary config file 
	 * ON STARTUP. This function does not modify current program state. */
	public static void changeStartupConfigFile(boolean useDeploy) {
		Config.changeStartupConfigFile(useDeploy);
	}

	/** print the current config mappings to the console */
	public static void printConfigMappings() {
		Config.printConfigMappings();
	}
}
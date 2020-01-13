package frc.gen;

import java.util.Map;

import frc.gen.Config;

public class BIGData {

    private static Map<String, String> map;

    public static void start() {
        Config.start();
		map = Config.getMap();
		put("data_x", 0);
		put("data_y", 0);
		put("data_w", 0);
    }

    /** Get the boolean config value corresponding to the key passed in.
	 * @return The corresponding boolean value, or false if the key was invalid
	 */
	public static boolean getBoolean(String key) {
		return Boolean.parseBoolean(map.get(key));
    }

    /** Get the double config value corresponding to the key passed in.
	 * @return The corresponding double value, or 0.0 if the key was invalid
	 */
	public static double getDouble(String key) {
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
	public static void setDrive(double vx, double vy, double w){
		put("drive_vx", vx);
		put("drive_vy", vy);
		put("drive_vw", w);
	}

	public static void put(String key, double val) {
		map.put(key, "" + val);
	}

	public static void put(String key, int val) {
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
}
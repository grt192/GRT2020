package frc.gen;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Scanner;
import java.util.TreeMap;

/**
 * This class stores its configuration information in 3 files:
 * <p>
 * Deploy time config file, located in /home/lvuser/deploy/. Robot programs
 * don't have permission to write to this directory.
 * </p>
 * <p>
 * "Temporary" config file, located in /home/lvuser/. This file is where changes
 * to configuration values while the program is running are stored.
 * </p>
 * <p>
 * Config state file, located in /home/lvuser/. This file contains a single line
 * that determines which config file to load into the program on startup. If the
 * file contains "deploy", the deploy time config file will be used, and if the
 * file contains "temp", the temporary config file will be used to start the
 * program.
 * </p>
 */
class Config {
	private static Map<String, String> map;

	/**
	 * The name of the deploy time main config file in home/lvuser/deploy (e.g.
	 * "omega2020.txt")
	 */
	private static String deployConfigFileName;
	/**
	 * The name of the local config file in home/lvuser. (e.g. "omega_local.txt")
	 * This file contains configuration values like swerve zeroes
	 */
	private static String localConfigFileName;
	/**
	 * The name of the local config file for shooter RPMs in home/lvuser (e.g.
	 * "omega_rpms.txt") This file contains shooter RPMs formatted in a way that
	 * ShooterMech knows how to read
	 */
	private static String RPMConfigFileName;

	/** config values that must be contained in the local config file */
	private static String[] VALUES_REQUIRED_FOR_LOCAL = { "fr_offset", "br_offset", "bl_offset", "fl_offset" };

	public static Map<String, String> getMap() {
		return map;
	}

	/** initialize the main map and shooter rpm maps from the configuration files */
	public static void start(Map<String, String> givenMap, TreeMap<Integer, Integer> upRPMMap,
			TreeMap<Integer, Integer> downRPMMap) {
		Arrays.sort(VALUES_REQUIRED_FOR_LOCAL);
		map = givenMap;
		try {
			// get deploy config file name
			Scanner nameScanner = new Scanner(new File("/home/lvuser/name.192"));
			deployConfigFileName = nameScanner.nextLine() + ".txt";
			nameScanner.close();
			// load deploy config file
			String directory = "/home/lvuser/deploy";
			System.out.println("reading from deploy config file " + deployConfigFileName);
			loadFromFile(new File(directory, deployConfigFileName));

			// load local config file from "home/lvuser"
			localConfigFileName = map.getOrDefault("local_config_file", "local_config.txt");
			directory = "/home/lvuser";
			File f = new File(directory, localConfigFileName);
			if (!f.exists()) {
				resetLocalConfigFile();
			}
			loadFromFile(new File(directory, localConfigFileName));

			// initialize RPMConfigFileName
			RPMConfigFileName = map.getOrDefault("shooter_rpm_file", "shooter_rpms.txt");
			initRPMTable(upRPMMap, downRPMMap);
		} catch (FileNotFoundException e) {
			System.out.println("UNABLE TO FIND FILE - either name.192 or deploy config file is missing!");
		}
		// System.out.println(Arrays.asList(upRPMMap));
	}

	/** load config values from the given file into the map */
	public static void loadFromFile(File f) throws FileNotFoundException {
		Scanner in = new Scanner(f);

		// add configs to map
		while (in.hasNextLine()) {
			String line = in.nextLine().trim();

			if (line.length() > 0 && line.charAt(0) != '#') {
				String[] splitted = line.split("=", 2);
				if (splitted.length == 2)
					map.put(splitted[0].trim(), splitted[1].trim());
			}
		}
		in.close();
	}

	/**
	 * reads the rpms from the file specified by the "shooter_rpm_file" key in the
	 * map
	 */
	private static void initRPMTable(TreeMap<Integer, Integer> upRPMMap, TreeMap<Integer, Integer> downRPMMap) {
		boolean loadingDown = true;

		String directory = "/home/lvuser";
		try {
			File f = new File(directory, RPMConfigFileName);
			if (!f.exists()) {
				resetLocalRPMConfigFile();
			}
			Scanner in = new Scanner(new File(directory, RPMConfigFileName));
			while (in.hasNextLine()) {
				String line = in.nextLine().trim();
				if (line.equalsIgnoreCase("down")) {
					loadingDown = true;
					continue;
				} else if (line.equalsIgnoreCase("up")) {
					loadingDown = false;
					continue;
				}
				if (line.length() > 0 && line.charAt(0) != '#') {
					String[] split = line.split(",");
					try {
						int a = Integer.parseInt(split[0].trim());
						int b = Integer.parseInt(split[1].trim());
						System.out
								.println("loaded shooter point: dist(in)=" + a + ",rpm=" + b + ", down=" + loadingDown);
						if (loadingDown) {
							downRPMMap.put(a, b);
						} else {
							// System.out.println(a + " " + b);
							upRPMMap.put(a, b);
						}
					} catch (ArrayIndexOutOfBoundsException e) {
						System.out.println("unable to parse line: " + line);
					} catch (NumberFormatException e) {
						System.out.println("unable to parse line: " + line);
					}
				}
			}
			// System.out.println(Arrays.asList(upRPMMap));
			in.close();
		} catch (FileNotFoundException e) {
			System.out.println("UNABLE TO LOAD SHOOTER VALUES! SHOOTER WILL BE BAD!");
		} catch (Exception e) {
			System.out.println("something bad happened in initRPMTable!");
		}
		BIGData.putConfigFileMsg(deployConfigFileName + "," + localConfigFileName + "," + RPMConfigFileName);
	}

	/**
	 * Writes the current local mappings to the local config file in home/lvuser.
	 * (updates swerve zeroes in local file)
	 */
	public static void updateLocalConfigFile() {
		File f = new File("/home/lvuser", localConfigFileName);
		if (!f.exists()) {
			System.out.println("creating the local config file...");
			resetLocalConfigFile();
		}
		// read local config file, store the formatting
		// queue of all the lines in the file
		Queue<String> configLines = new LinkedList<String>();
		try {
			Scanner scanner = new Scanner(f);
			String input;
			while (scanner.hasNext()) {
				input = scanner.nextLine().trim();
				configLines.add(input);
			}
			scanner.close();
		} catch (FileNotFoundException e) {
			System.out.println("unable to update the config file, could not find file");
			return;
		}

		// put new config file at "configlocaltemp.txt", then atomically rename
		// it later to replace old config file
		File tempFile = new File("/home/lvuser", "configlocaltemp.txt");
		FileWriter writer;

		// boolean array parallel to VALUES_REQUIRED_FOR_LOCAL so all the required
		// values make it into local config
		// by default, all values are initialized to false.
		boolean[] requiredValsExist = new boolean[VALUES_REQUIRED_FOR_LOCAL.length];

		try {
			writer = new FileWriter(tempFile);
			String line, key;
			while (!configLines.isEmpty()) {
				// get the next line from the queue
				line = configLines.remove();
				if (line.isEmpty()) {
					writer.write("\n");
				} else if (line.charAt(0) != '#') {
					// if this line is not a comment, it is a command
					key = line.split("=", 2)[0].trim();
					// if it is one of the required values, note that it has been put into the
					// updated config file
					int index = Arrays.binarySearch(VALUES_REQUIRED_FOR_LOCAL, key);
					if (index >= 0) {
						requiredValsExist[index] = true;
					}
					if (map.containsKey(key)) {
						writer.write(key + "=" + map.get(key) + "\n");
					} else {
						System.out.println(
								"could not find corresponding value for " + key + ", writing '" + line + "' to file");
						writer.write(line);
					}
				} else if (line.charAt(0) == '#') {
					// write comments to the file (# denotes a comment)
					writer.write(line + "\n");
				}
			}

			// write the required values to the file
			for (int i = 0; i < VALUES_REQUIRED_FOR_LOCAL.length; i++) {
				if (!requiredValsExist[i]) {
					writer.write(
							VALUES_REQUIRED_FOR_LOCAL[i] + "=" + map.getOrDefault(VALUES_REQUIRED_FOR_LOCAL[i], ""));
				}
			}
			writer.close();
			// rename file to replace old file
			Files.move(tempFile.toPath(), f.toPath(), StandardCopyOption.ATOMIC_MOVE);
			BIGData.putConfigFileMsg("updated the local config file");
		} catch (IOException e) {
			BIGData.putConfigFileMsg("could not update config file");
			e.printStackTrace();
			return;
		}
	}

	/** update the local RPM config file with the current values in the rpm maps */
	public static void updateLocalRPMConfigFile() {
		File f = new File("/home/lvuser", RPMConfigFileName);
		try {
			FileWriter writer = new FileWriter(f);
			// load down rpms
			writer.write("down\n");
			for (Map.Entry<Integer, Integer> entry : BIGData.downRPMMap.entrySet()) {
				// write in format "distance,RPM"
				writer.write(entry.getKey() + "," + entry.getValue() + "\n");
			}
			// load up rpms
			writer.write("up\n");
			for (Map.Entry<Integer, Integer> entry : BIGData.upRPMMap.entrySet()) {
				// write in format "distance,RPM"
				writer.write(entry.getKey() + "," + entry.getValue() + "\n");
			}
			writer.close();
			BIGData.putConfigFileMsg("updated the local RPM config file");
		} catch (IOException e) {
			BIGData.putConfigFileMsg("unable to update local rpm config file");
		}
	}

	/** resets the local config file that contains the swerve zeros */
	public static void resetLocalConfigFile() {
		File localConfigFile = new File("/home/lvuser", localConfigFileName);
		try {
			FileWriter writer = new FileWriter(localConfigFile);
			for (String key : VALUES_REQUIRED_FOR_LOCAL) {
				writer.write(key + "=\n");
			}
			writer.close();
			BIGData.putConfigFileMsg("reset the local config file (swerve zeros)");
		} catch (IOException e) {
			BIGData.putConfigFileMsg("unable to reset local config file");
		}
	}

	/**
	 * resets the local RPM config file with the corresponding deploy-time config
	 * file
	 */
	public static void resetLocalRPMConfigFile() {
		System.out.println("resetting local RPM config file to deploy time config file");
		File localRPMConfigFile = new File("/home/lvuser", RPMConfigFileName);
		File deployRPMConfigFile = new File("home/lvuser/deploy", RPMConfigFileName);
		try {
			if (!deployRPMConfigFile.exists()) {
				System.out.println("Deploy time RPM file does not exist, aborting reset");
				return;
			}
			Files.copy(deployRPMConfigFile.toPath(), localRPMConfigFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
			BIGData.putConfigFileMsg("reset the RPM local config file");
		} catch (IOException e) {
			BIGData.putConfigFileMsg("unable to reset RPM local config file");
		}
	}
}

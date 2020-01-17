package frc.control;

import edu.wpi.first.wpilibj.command.Command;
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;
import edu.wpi.first.wpilibj.shuffleboard.ShuffleboardLayout;
import edu.wpi.first.wpilibj.shuffleboard.ShuffleboardTab;
import frc.gen.BIGData;

/**
 * Miscellaneous commands from the shuffleboard
 */
public class ShuffleboardCommands {

    private static ShuffleboardTab settingsTab;
    private static ShuffleboardLayout configCommands;
    private static ShuffleboardLayout joystickCommands;

    public static void init() {
        settingsTab = Shuffleboard.getTab("Settings");
        //configCommands = settingsTab.getLayout("config_file");
        joystickCommands = settingsTab.getLayout("joystick");

        settingsTab.add("zero swerve", new Command() {
            private boolean done = false;
            protected void execute() {
                BIGData.setZeroSwerveRequest(true);
                done = true;
            }
            protected boolean isFinished() {
                return done;
            }
        }); // TODO put somewhere else

        configCommands.add("reset temp config file", new Command() {
            private boolean done = false;
            protected void execute() {
                BIGData.resetTempConfigFile();
                done = true;
            }
            protected boolean isFinished() {
                return done;
            }
        });
        configCommands.add("use deploy time config file", new Command() {
            private boolean done = false;
            protected void execute() {
                BIGData.changeStartupConfigFile(true);
                done = true;
            }
            protected boolean isFinished() {
                return done;
            }
        });
        configCommands.add("use temporary config file", new Command() {
            private boolean done = false;
            protected void execute() {
                BIGData.changeStartupConfigFile(false);
                done = true;
            }
            protected boolean isFinished() {
                return done;
            }
        });
        
        joystickCommands.add("original pt1", 2); // TODO
    }


}
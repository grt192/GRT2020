/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018 FIRST. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.control;

public abstract class Mode {
    private static DriverControl driverControl;
    private static PathfindingControl pathfindingControl;
    private static Mode[] modes;

    public static void initModes() {
        driverControl = new DriverControl();
        pathfindingControl = new PathfindingControl();
        modes = new Mode[2];
        modes[0] = driverControl;
        modes[1] = pathfindingControl;
    }

    public abstract boolean loop();

    public static Mode getMode(int i) {
        return modes[i];
    }

}

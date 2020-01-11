/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018 FIRST. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.modes;

public abstract class Mode {

    public static Mode DRIVER_CONTROL;
    private static Mode[] modes;

    public static void initModes() {
        DRIVER_CONTROL = new DriverControl();
        modes = new Mode[1];
        modes[0] = DRIVER_CONTROL;
    }

    public abstract boolean loop();

    public static Mode getMode(int i) {
        return modes[i];
    }

}

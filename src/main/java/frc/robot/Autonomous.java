/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018 FIRST. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

import edu.wpi.first.wpilibj.Filesystem;
import frc.gen.BIGData;
import frc.pathfinding.fieldmap.geometry.*;
import frc.pathfinding.*;

public class Autonomous {

    private Robot robot;
    private Queue<String> lines;
    private boolean done;

    private boolean finishedFlag;
    private long delayTime;

    public Autonomous(Robot robot) {
        this.robot = robot;
        done = true;
    }

    public void init(String filename) {
        lines = new LinkedList<>();
        try {
            BufferedReader reader = new BufferedReader(
                    new FileReader(new File(Filesystem.getDeployDirectory(), "auton/" + filename)));
            String line = reader.readLine();
            while (line != null) {
                if (!line.startsWith("#"))
                    lines.add(line);
                line = reader.readLine();
            }
            reader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        done = false;
        finishedFlag = true;
        delayTime = 0;
    }

    public void loop() {
        long time = System.currentTimeMillis();
        while (true) {
            if (done || !finishedFlag || time < delayTime)
                return;
            String line = lines.poll();
            if (line == null) {
                done = true;
                return;
            }
            // System.out.println(line);
            String[] cmd = line.trim().split(" ");
            switch (cmd[0]) {
            case "delay":
                delayTime = time + Integer.parseInt(cmd[1]);
                break;
            case "wait":
                finishedFlag = false;
                break;
            case "pos":
                BIGData.setManualPos(Double.parseDouble(cmd[1]), Double.parseDouble(cmd[2]));
                break;
            case "swerve":
                robot.setMode(0);
                BIGData.requestDrive(Double.parseDouble(cmd[1]), Double.parseDouble(cmd[2]),
                        cmd.length > 3 ? Double.parseDouble(cmd[3]) : 0);
                break;
            case "path":
                Target.putAction(Target.Actions.PATH);
                Target.setTarget(new Vector(Double.parseDouble(cmd[1]), Double.parseDouble(cmd[2])));
                robot.setMode(1);
                break;
            case "bez":
                Target.putAction(Target.Actions.BEZ);
                Target.setBezier(new Vector(Double.parseDouble(cmd[1]), Double.parseDouble(cmd[2])),
                        new Vector(Double.parseDouble(cmd[3]), Double.parseDouble(cmd[4])),
                        new Vector(Double.parseDouble(cmd[5]), Double.parseDouble(cmd[6])));
                robot.setMode(2);
                break;
            case "score":
                Target.putAction(Target.Actions.SCORE);
                robot.setMode(3);
                break;
            case "turn":
                Target.setAngle(Double.parseDouble(cmd[1]));
                Target.putAction(Target.Actions.TURN);
                robot.setMode(3);
                break;
            case "intake":
                Target.setIntakeState(Boolean.parseBoolean(cmd[1]));
                Target.putAction(Target.Actions.INTAKE);
                robot.setMode(3);
                break;
            case "driveto":
                Target.setTarget(new Vector(Double.parseDouble(cmd[1]), Double.parseDouble(cmd[2])));
                Target.putAction(Target.Actions.DRIVETO);
                robot.setMode(3);
                break;
            }
        }
    }

    public void modeFinished() {
        finishedFlag = true;
    }

    public void kill() {
        done = true;
    }
}

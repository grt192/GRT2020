package frc.swerve;

import com.kauailabs.navx.frc.AHRS;

import edu.wpi.first.wpilibj.SPI;
import edu.wpi.first.wpilibj.interfaces.Gyro;

public class NavXGyro extends AHRS implements Gyro {

	public NavXGyro() {
		super(SPI.Port.kMXP, (byte) 100);
	}

	@Override
	public void calibrate() {
		reset();
	}

}

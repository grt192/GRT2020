package frc.swerve;

public class SwerveData {

	public final double gyroAngle, gyroW, encoderW, encoderVX, encoderVY;

	public SwerveData(double gyroAngle, double gyroRate, double encVX, double encVY, double encAngVel) {
		this.gyroAngle = gyroAngle;
		gyroW = gyroRate;
		encoderW = encAngVel;
		encoderVX = encVX;
		encoderVY = encVY;
	}

}

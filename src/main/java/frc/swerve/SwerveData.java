package frc.swerve;

public class SwerveData {

	public final double enc_w, enc_vx, enc_vy;

	public SwerveData(double encVX, double encVY, double encAngVel) {
		enc_w = encAngVel;
		enc_vx = encVX;
		enc_vy = encVY;
	}
}

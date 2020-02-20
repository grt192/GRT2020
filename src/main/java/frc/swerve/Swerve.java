package frc.swerve;

import static frc.gen.BIGData.BL_WHEEL;
import static frc.gen.BIGData.BR_WHEEL;
import static frc.gen.BIGData.FL_WHEEL;
import static frc.gen.BIGData.FR_WHEEL;

import frc.gen.BIGData;
import frc.util.GRTUtil;

public class Swerve {

	private final double SWERVE_WIDTH;
	private final double SWERVE_HEIGHT;
	private final double RADIUS;
	private final double WHEEL_ANGLE;
	private final double ROTATE_SCALE;
	/** proportional scaling constant */
	private final double kP;
	/** derivative scaling constant */
	private final double kD;
	private final double kF;
	private NavXGyro gyro;
	/** wheels[0]=fr, wheels[1]=br, wheels[2]=bl, wheels[3]=fl */
	private Wheel[] wheels;

	/** requested x velocity, y velocity, angular velocity(rad/s), and angle */
	private volatile double userVX, userVY, userW, angle;
	/** determines if robot centric control or field centric control is used */
	private volatile boolean robotCentric;
	private volatile boolean withPID;
	private volatile SwerveData swerveData;

	public Swerve() {
		this.gyro = new NavXGyro();
		gyro.reset();
		angle = 0.0;
		robotCentric = false;
		BIGData.setPIDFalse();

		wheels = new Wheel[4];
		wheels[FR_WHEEL] = new Wheel(BIGData.getWheelName(FR_WHEEL));
		wheels[BR_WHEEL] = new Wheel(BIGData.getWheelName(BR_WHEEL));
		wheels[BL_WHEEL] = new Wheel(BIGData.getWheelName(BL_WHEEL));
		wheels[FL_WHEEL] = new Wheel(BIGData.getWheelName(FL_WHEEL));

		SWERVE_WIDTH = BIGData.getDouble("swerve_width");
		SWERVE_HEIGHT = BIGData.getDouble("swerve_height");
		kP = BIGData.getDouble("swerve_kp");
		kD = BIGData.getDouble("swerve_kd");
		kF = BIGData.getDouble("swerve_kf");
		RADIUS = Math.sqrt(SWERVE_WIDTH * SWERVE_WIDTH + SWERVE_HEIGHT * SWERVE_HEIGHT) / 2;
		WHEEL_ANGLE = Math.atan2(SWERVE_WIDTH, SWERVE_HEIGHT);
		ROTATE_SCALE = 1 / RADIUS;
		calcSwerveData();
		// TODO: test swerve PID
		setAngle(0.0);
	}

	public void update() {

		refreshVals();
		double w = userW;
		if (withPID) {
			w = calcPID();
		}
		changeMotors(userVX, userVY, w);
		calcSwerveData();
	}

	private void refreshVals() {
		withPID = BIGData.isPID();
		angle = BIGData.getRequestedAngle();

		userVX = BIGData.getRequestedVX();
		userVY = BIGData.getRequestedVY();
		userW = BIGData.getRequestedW();
		if (userW != 0 || Math.abs(gyro.getAngle() % 360 - angle) < .5) {
			BIGData.setPIDFalse();
		}
		if (BIGData.getZeroSwerveRequest()) {
			System.out.println("zeroing ALL wheels");
			zeroRotate();
			BIGData.putZeroSwerveRequest(false);
			BIGData.updateLocalConfigFile();
		}
		if (BIGData.getZeroGyroRequest()) {
			System.out.println("zeroing gyro");
			gyro.zeroYaw();
			BIGData.putZeroGyroRequest(false);
		}
		BIGData.putGyroAngle(gyro.getAngle());

		boolean zeroesUpdated = false;

		for (int i = 0; i < wheels.length; i++) {
			BIGData.putWheelRawDriveSpeed(wheels[i].getName(), wheels[i].getRawDriveSpeed());
			BIGData.putWheelRawRotateSpeed(wheels[i].getName(), wheels[i].getRawRotateSpeed());
			if (BIGData.getZeroIndivSwerveRequest(i)) {
				zeroRotateIndiv(i);
				zeroesUpdated = true;
				BIGData.putZeroIndivSwerveRequest(i, false);
			}
		}
		if (zeroesUpdated) {
			BIGData.updateLocalConfigFile();
		}
	}

	/**
	 * calculates angle correction for robot based on current angle, requested
	 * angle, kP, and kD
	 */
	private double calcPID() {
		// System.out.println("kF: " + kF);
		double error = GRTUtil.angularDifference(Math.toRadians(gyro.getAngle()), Math.toRadians(angle),
				Math.toRadians(kF));
		double w = error * kP - Math.toRadians(gyro.getRate()) * kD;
		// System.out.print("W: " + w);%
		return -w;
	}

	/**
	 * sets the angle of the robot
	 * 
	 * @param angle
	 *                  the angle to turn the robot to, in radians
	 */
	private void setAngle(double angle) {
		withPID = true;
		this.angle = angle;
	}

	/** sets whether we use robot centric or field centric control */
	public void setRobotCentric(boolean mode) {
		robotCentric = mode;
	}

	/**
	 * change the motors to reach the requested values
	 * 
	 * @param vx
	 *               the requested x velocity from -1.0 to 1.0
	 * @param vy
	 *               the requested y velocity from -1.0 to 1.0
	 * @param w
	 *               the requested angular velocity
	 */
	private void changeMotors(double vx, double vy, double w) {
		w *= ROTATE_SCALE;
		double gyroAngle = (robotCentric ? 0 : Math.toRadians(gyro.getAngle()));
		for (int i = 0; i < wheels.length; i++) {
			// angle in radians
			double wheelAngle = getRelativeWheelAngle(i) - gyroAngle;
			// x component of tangential velocity
			double wx = (w * RADIUS) * Math.cos(Math.PI / 2 + wheelAngle);
			// y component of tangential velocity
			double wy = (w * RADIUS) * Math.sin(Math.PI / 2 + wheelAngle);
			double wheelVX = vx + wx;
			double wheelVY = vy + wy;
			double wheelPos = Math.atan2(wheelVY, wheelVX) + gyroAngle - Math.PI / 2;
			double power = Math.sqrt(wheelVX * wheelVX + wheelVY * wheelVY);
			wheels[i].set(wheelPos, power);
		}
	}

	private double getRelativeWheelAngle(int i) {
		double angle = WHEEL_ANGLE;
		switch (i) {
		case BR_WHEEL:
			angle = GRTUtil.TWO_PI - WHEEL_ANGLE;
			break;
		case BL_WHEEL:
			angle = Math.PI + WHEEL_ANGLE;
			break;
		case FL_WHEEL:
			angle = Math.PI - WHEEL_ANGLE;
			break;
		}
		return angle;
	}

	public SwerveData getSwerveData() {
		return swerveData;
	}

	private void calcSwerveData() {
		double gyroAngle = Math.toRadians(gyro.getAngle());
		double gyroRate = Math.toRadians(gyro.getRate());
		double vx = 0;
		double vy = 0;
		double w = 0;
		for (int i = 0; i < wheels.length; i++) {
			double wheelAngle = getRelativeWheelAngle(i);
			double wheelPos = wheels[i].getCurrentPosition();
			double speed = wheels[i].getDriveSpeed();
			w += Math.sin(wheelPos - wheelAngle) * speed / RADIUS;
			wheelPos += gyroAngle;
			vx += Math.cos(wheelPos) * speed;
			vy += Math.sin(wheelPos) * speed;
		}
		w /= 4.0;
		vx /= 4.0;
		vy /= 4.0;
		swerveData = new SwerveData(gyroAngle, gyroRate, vx, vy, w);
		BIGData.setSwerveData(swerveData);
	}

	/**
	 * Takes the current position of the wheels and sets them as zero in the
	 * currently running program and adds them to BIGData
	 */
	private void zeroRotate() {
		for (int i = 0; i < wheels.length; i++) {
			wheels[i].zero();
			BIGData.putWheelZero(wheels[i].getName(), wheels[i].getOffset());
		}
	}

	private void zeroRotateIndiv(int wheelNum) {
		if (wheelNum < wheels.length) {
			wheels[wheelNum].zero();
			BIGData.putWheelZero(wheels[wheelNum].getName(), wheels[wheelNum].getOffset());
		}
	}

}

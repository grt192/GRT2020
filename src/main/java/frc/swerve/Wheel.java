package frc.swerve;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.StatusFrameEnhanced;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import com.revrobotics.CANEncoder;
import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkMax.IdleMode;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;

import frc.util.GRTUtil;
import frc.gen.BIGData;

class Wheel {
	/* encoder ticks per rotation of the rotateMotor */
	private final double TICKS_PER_ROTATION;
	/* offset of the encoder that is the zero value of the angle of the wheel. */
	private int OFFSET;
	private final double DRIVE_TICKS_TO_METERS;

	/* 2*pi=6.28ish */
	private static final double TWO_PI = Math.PI * 2;

	/* proportional gain constant for turning swerve to an angle */
	private static final double kP = 9000.0;
	/* derivative gain constant for turning swerve to an angle */
	private static final double kD = 0.0;

	/* talon srx to control rotate motor */
	private TalonSRX rotateMotor;
	/* spark max to control drive motor */
	private CANSparkMax driveMotor;
	/* encoder for the drive motor */
	private CANEncoder driveEncoder;

	/** Name of the wheel "fr", "br", "bl", "fl" */
	private String name;

	/* whether the wheel is currently in its reversed position. */
	private boolean reversed;
	/* true if this Wheel is enabled, false if it is disabled. call Wheel.enable() or Wheel.disable() to change */
	private boolean enabled;

	public Wheel(String name) {
		this.name = name;

		rotateMotor = new TalonSRX(BIGData.getInt(name + "_rotate"));
		driveMotor = new CANSparkMax(BIGData.getInt(name + "_drive"), MotorType.kBrushless);
		driveEncoder = driveMotor.getEncoder();
		TICKS_PER_ROTATION = BIGData.getDouble("ticks_per_rotation");
		OFFSET = BIGData.getInt(name + "_offset");
		DRIVE_TICKS_TO_METERS = BIGData.getDouble("drive_encoder_scale");
		configRotateMotor();
		configDriveMotor();
		enabled = true;
	}

	/** Zeroes the wheel by updating the offset, and returns the new offset */
	public int zero() {
		System.out.println("Zeroing " + name + "module");
		OFFSET = rotateMotor.getSelectedSensorPosition(0);
		return OFFSET;
	}

	/** set whether this swerve module should be enabled or not.
	 * @param enabled true if this module should be enabled, false if it should be disabled
	 */
	public void setEnabled(boolean enabled) {
		System.out.println("wheel" + name + "=" + enabled);
		if (enabled) {
			this.enabled = true;
			rotateMotor.set(ControlMode.Disabled, 0);
			set(0, 0);
		} else {
			this.enabled = false;
			rotateMotor.set(ControlMode.Disabled, 0);
			driveMotor.disable();
		}
		System.out.println("realenable:" + this.enabled);
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void set(double radians, double speed) {
		System.out.println(enabled);
		if (!enabled) {
			rotateMotor.set(ControlMode.PercentOutput, 0);
			driveMotor.set(0);
			return;
		}
		if (speed != 0.0) {
			double targetPosition = radians / TWO_PI;
			targetPosition = GRTUtil.positiveMod(targetPosition, 1.0);

			int encoderPosition = rotateMotor.getSelectedSensorPosition(0) - OFFSET;
			double currentPosition = encoderPosition / TICKS_PER_ROTATION;
			double rotations = Math.floor(currentPosition);
			currentPosition -= rotations;
			double delta = currentPosition - targetPosition;
			if (Math.abs(delta) > 0.5) {
				targetPosition += Math.signum(delta);
			}
			delta = currentPosition - targetPosition;
			boolean newReverse = false;
			if (Math.abs(delta) > 0.25) {
				targetPosition += Math.signum(delta) * 0.5;
				newReverse = true;
			}
			targetPosition += rotations;
			reversed = newReverse;
			double encoderPos = targetPosition * TICKS_PER_ROTATION + OFFSET;
			rotateMotor.set(ControlMode.Position, encoderPos);
			
			speed *= (reversed ? -1 : 1);
		}
		driveMotor.set(speed);
	}

	public int getEncoderPosition() {
		return rotateMotor.getSelectedSensorPosition(0) - OFFSET;
	}

	public double getDriveSpeed() {
		//TODO possible wrong calculation because getVelocity() is in RPM
		return driveEncoder.getVelocity() * DRIVE_TICKS_TO_METERS * (reversed ? -1 : 1) / 60.0;
	}

	public double getCurrentPosition() {
		return GRTUtil.positiveMod((((rotateMotor.getSelectedSensorPosition(0) - OFFSET) * TWO_PI / TICKS_PER_ROTATION)
				+ (reversed ? Math.PI : 0)), TWO_PI);
	}

	/** return the name of this wheel "fr", "br", "bl", "fl" */
	public String getName() {
		return name;
	}

	/** Return the rotationally zero position of the module in encoder ticks */
	public int getOffset() {
		return OFFSET;
	}

	/** get the drive motor speed in rotations/second */
	public double getRawDriveSpeed() {
		// (rotations/minute) * (1 min/60 sec)
		return driveEncoder.getVelocity() / 60;
	}

	/** get the rotate motor speed in rotations/sec */
	public double getRawRotateSpeed() {
		// (ticks/100ms) / (ticks/rotation) * (10 (100ms)/1s) 
		return (rotateMotor.getSelectedSensorVelocity() / TICKS_PER_ROTATION) * 10;
	}

	private void configRotateMotor() {
		GRTUtil.defaultConfigTalon(rotateMotor);

		boolean inverted = BIGData.getBoolean("swerve_inverted");
		rotateMotor.setInverted(inverted);
		rotateMotor.setSensorPhase((!inverted) ^ BIGData.getBoolean("sensor_phase"));
		rotateMotor.configSelectedFeedbackSensor(FeedbackDevice.Analog, 0, 0);
		rotateMotor.setStatusFramePeriod(StatusFrameEnhanced.Status_2_Feedback0, 10, 0);

		rotateMotor.config_kP(0, kP / TICKS_PER_ROTATION, 0);
		rotateMotor.config_kI(0, 0, 0);
		rotateMotor.config_kD(0, kD / TICKS_PER_ROTATION, 0);
		rotateMotor.config_kF(0, 0, 0);
		rotateMotor.configMaxIntegralAccumulator(0, 0, 0);
		rotateMotor.configAllowableClosedloopError(0, 0, 0);
	}

	private void configDriveMotor() {
		driveMotor.restoreFactoryDefaults();
		driveMotor.setIdleMode(IdleMode.kBrake);
		driveMotor.setOpenLoopRampRate(0.1);
	}
}

package org.firstinspires.ftc.teamcode.techno.teleops;

import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.IMU;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;

@TeleOp(name = "Teste 20 Horas")
public class Teste20Horas extends OpMode {
    // This declares the four motors needed
    DcMotor frontLeftDrive;
    DcMotor frontRightDrive;
    DcMotor backLeftDrive;
    DcMotor backRightDrive;
    DcMotor shooter, intake;

    // This declares the IMU needed to get the current direction the robot is facing
    IMU imu;

    @Override
    public void init() {
        frontLeftDrive = hardwareMap.get(DcMotor.class, "leftFront");
        frontRightDrive = hardwareMap.get(DcMotor.class, "rightFront");
        backLeftDrive = hardwareMap.get(DcMotor.class, "leftBack");
        backRightDrive = hardwareMap.get(DcMotor.class, "rightBack");
        shooter = hardwareMap.get(DcMotor.class, "shooter");
        intake = hardwareMap.get(DcMotor.class, "intake");

        // We set the left motors in reverse which is needed for drive trains where the left
        // motors are opposite to the right ones.

        backLeftDrive.setDirection(DcMotorSimple.Direction.FORWARD);
        frontLeftDrive.setDirection(DcMotorSimple.Direction.REVERSE);
        backRightDrive.setDirection(DcMotorSimple.Direction.REVERSE);

        imu = hardwareMap.get(IMU.class, "imu");
        // This needs to be changed to match the orientation on your robot
        RevHubOrientationOnRobot.LogoFacingDirection logoDirection =
                RevHubOrientationOnRobot.LogoFacingDirection.RIGHT;
        RevHubOrientationOnRobot.UsbFacingDirection usbDirection =
                RevHubOrientationOnRobot.UsbFacingDirection.BACKWARD;

        RevHubOrientationOnRobot orientationOnRobot = new
                RevHubOrientationOnRobot(logoDirection, usbDirection);
        imu.initialize(new IMU.Parameters(orientationOnRobot));
    }

    @Override
    public void loop() {

        // If you press the A button, then you reset the Yaw to be zero from the way
        // the robot is currently pointing
        if (gamepad1.x) {
            imu.resetYaw();
        }

        //driveFieldRelative(-gamepad1.left_stick_y, gamepad1.left_stick_x, gamepad1.right_stick_x);
        shooter.setPower(0.65);

        if (gamepad1.a){
            intake.setPower(0.7);
        }
        else{
            intake.setPower(0);
        }

        double y  = -gamepad1.left_stick_y; // Invertido porque o joystick para cima é negativo
        double x  =  gamepad1.left_stick_x;
        double rx =  gamepad1.right_stick_x;

        // 1. Ler o ângulo atual do robô pelo IMU
        double robotHeading = imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.RADIANS);

// 2. Ajustar a rotação do joystick baseado no ângulo do robô
// (Aqui você adiciona ou subtrai os 90° do campo se a sua posição inicial exigir)
        double headingOffset = Math.toRadians(0); // Mude para Math.toRadians(90) se sua partida for rotacionada
        double totalHeading = -robotHeading + headingOffset;

        double rotX = x * Math.cos(totalHeading) - y * Math.sin(totalHeading);
        double rotY = x * Math.sin(totalHeading) + y * Math.cos(totalHeading);

// 3. Multiplicador para garantir que o Strafe (lateral) não perca força devido ao atrito do Mecanum
        rotX = rotX * 1.1;

// 4. Denominador para normalizar as potências abaixo de 1.0
        double denominator = Math.max(Math.abs(rotY) + Math.abs(rotX) + Math.abs(rx), 1.0);

// 5. Distribuição idêntica para os motores (a física já foi corrigida no setDirection!)
        double frontLeftPower  = (rotY + rotX + rx) / denominator;
        double backLeftPower   = (rotY - rotX + rx) / denominator;
        double frontRightPower = (rotY - rotX - rx) / denominator;
        double backRightPower  = (rotY + rotX - rx) / denominator;

// Enviar comandos para o hardware
        frontLeftDrive.setPower(frontLeftPower);
        backLeftDrive.setPower(backLeftPower);
        frontRightDrive.setPower(frontRightPower);
        backRightDrive.setPower(backRightPower);

    }

    // This routine drives the robot field relative
    private void driveFieldRelative(double forward, double right, double rotate) {
        // First, convert direction being asked to drive to polar coordinates
        double theta = Math.atan2(forward, right);
        double r = Math.hypot(right, forward);

        // Second, rotate angle by the angle the robot is pointing
        theta = AngleUnit.normalizeRadians(theta -
                imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.RADIANS));

        // Third, convert back to cartesian
        double newForward = r * Math.sin(theta);
        double newRight = r * Math.cos(theta);

        // Finally, call the drive method with robot relative forward and right amounts
        drive(newForward, newRight, rotate);
    }

    // Thanks to FTC16072 for sharing this code!!
    public void drive(double forward, double right, double rotate) {
        // This calculates the power needed for each wheel based on the amount of forward,
        // strafe right, and rotate
        double frontLeftPower = forward + right + rotate;
        double frontRightPower = forward - right - rotate;
        double backRightPower = forward + right - rotate;
        double backLeftPower = forward - right + rotate;

        double maxPower = 1.0;
        double maxSpeed = 1.0;  // make this slower for outreaches

        // This is needed to make sure we don't pass > 1.0 to any wheel
        // It allows us to keep all of the motors in proportion to what they should
        // be and not get clipped
        maxPower = Math.max(maxPower, Math.abs(frontLeftPower));
        maxPower = Math.max(maxPower, Math.abs(frontRightPower));
        maxPower = Math.max(maxPower, Math.abs(backRightPower));
        maxPower = Math.max(maxPower, Math.abs(backLeftPower));

        // We multiply by maxSpeed so that it can be set lower for outreaches
        // When a young child is driving the robot, we may not want to allow full
        // speed.
        frontLeftDrive.setPower(maxSpeed * (frontLeftPower / maxPower));
        frontRightDrive.setPower(maxSpeed * (frontRightPower / maxPower));
        backLeftDrive.setPower(maxSpeed * (backLeftPower / maxPower));
        backRightDrive.setPower(maxSpeed * (backRightPower / maxPower));
    }
}

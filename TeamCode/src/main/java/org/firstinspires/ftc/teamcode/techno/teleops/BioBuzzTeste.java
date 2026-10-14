package org.firstinspires.ftc.teamcode.techno.teleops;

import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.IMU;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.YawPitchRollAngles;

@TeleOp(name = "BioBuzzTeste")
public class BioBuzzTeste extends LinearOpMode {

    // =========================
    // MOTORES
    // =========================

    private DcMotor leftFront;
    private DcMotor rightFront;
    private DcMotor leftRear;
    private DcMotor rightRear;
    private DcMotor intake;

    // =========================
    // IMU
    // =========================

    private IMU imu;

    @Override
    public void runOpMode() {

        // =========================
        // HARDWARE
        // =========================

        leftFront = hardwareMap.get(DcMotor.class, "fl");
        rightFront = hardwareMap.get(DcMotor.class, "fr");
        leftRear = hardwareMap.get(DcMotor.class, "bl");
        rightRear = hardwareMap.get(DcMotor.class, "br");
        intake = hardwareMap.get(DcMotor.class, "intake");
        imu = hardwareMap.get(IMU.class, "imu");


        // =========================
        // DIREÇÃO DOS MOTORES
        // =========================

        leftFront.setDirection(DcMotor.Direction.REVERSE);
        leftRear.setDirection(DcMotor.Direction.REVERSE);

        intake.setDirection(DcMotorSimple.Direction.FORWARD);
        rightFront.setDirection(DcMotor.Direction.FORWARD);
        rightRear.setDirection(DcMotor.Direction.FORWARD);


        // =========================
        // BRAKE
        // =========================

        leftFront.setZeroPowerBehavior(
                DcMotor.ZeroPowerBehavior.BRAKE
        );

        rightFront.setZeroPowerBehavior(
                DcMotor.ZeroPowerBehavior.BRAKE
        );

        leftRear.setZeroPowerBehavior(
                DcMotor.ZeroPowerBehavior.BRAKE
        );

        rightRear.setZeroPowerBehavior(
                DcMotor.ZeroPowerBehavior.BRAKE
        );


        // =========================
        // CONFIGURAÇÃO DO IMU
        // =========================

        RevHubOrientationOnRobot.LogoFacingDirection logoDirection =
                RevHubOrientationOnRobot.LogoFacingDirection.LEFT;

        RevHubOrientationOnRobot.UsbFacingDirection usbDirection =
                RevHubOrientationOnRobot.UsbFacingDirection.BACKWARD;

        RevHubOrientationOnRobot orientation =
                new RevHubOrientationOnRobot(
                        logoDirection,
                        usbDirection
                );

        imu.initialize(
                new IMU.Parameters(orientation)
        );


        // =========================
        // TELEMETRIA INICIAL
        // =========================

        telemetry.addLine("FIELD ORIENTED MECANUM");
        telemetry.addLine("Inicializando...");
        telemetry.update();


        // =========================
        // ESPERA START
        // =========================

        waitForStart();


        // =========================
        // ZERA O FIELD AO INICIAR
        // =========================

        imu.resetYaw();


        // =========================
        // VARIÁVEL DO BOTÃO Y
        // =========================

        boolean lastY = false;


        // =========================
        // LOOP PRINCIPAL
        // =========================

        while (opModeIsActive()) {

            // =================================
            // RESET DO FIELD ORIENTED
            // Y = RESET
            // =================================

            boolean currentY = gamepad1.y;

            // Detecta somente quando o botão é apertado
            if (currentY && !lastY) {
                imu.resetYaw();
            }

            lastY = currentY;


            // =================================
            // JOYSTICKS
            // =================================

            // Frente / trás
            double y = gamepad1.left_stick_y;

            // Esquerda / direita
            double x = -gamepad1.left_stick_x;

            // Rotação
            double rotation = -gamepad1.right_stick_x;


            // =================================
            // PEGA O HEADING DO IMU
            // =================================

            YawPitchRollAngles orientationAngles =
                    imu.getRobotYawPitchRollAngles();

            double heading =
                    orientationAngles.getYaw(AngleUnit.RADIANS);


            // =================================
            // FIELD ORIENTED
            // =================================

            double cosA = Math.cos(-heading);
            double sinA = Math.sin(-heading);

            double fieldX =
                    x * cosA - y * sinA;

            double fieldY =
                    x * sinA + y * cosA;


            // =================================
            // MECANUM
            // =================================

            double frontLeftPower =
                    fieldY + fieldX + rotation;

            double frontRightPower =
                    fieldY - fieldX - rotation;

            double rearLeftPower =
                    fieldY - fieldX + rotation;

            double rearRightPower =
                    fieldY + fieldX - rotation;


            // =================================
            // NORMALIZAÇÃO
            // =================================

            double maxPower = Math.max(
                    1.0,
                    Math.max(
                            Math.abs(frontLeftPower),
                            Math.max(
                                    Math.abs(frontRightPower),
                                    Math.max(
                                            Math.abs(rearLeftPower),
                                            Math.abs(rearRightPower)
                                    )
                            )
                    )
            );

            frontLeftPower /= maxPower;
            frontRightPower /= maxPower;
            rearLeftPower /= maxPower;
            rearRightPower /= maxPower;


            // =================================
            // ENVIA POTÊNCIA PARA OS MOTORES
            // =================================

            leftFront.setPower(frontLeftPower);
            rightFront.setPower(frontRightPower);

            leftRear.setPower(rearLeftPower);
            rightRear.setPower(rearRightPower);

            //========
            //BOTÕES
            //=======
            while (gamepad1.rightBumperWasPressed()){
                intake.setPower(0.7);
            }
            while(gamepad1.rightBumperWasReleased()){
                intake.setPower(0);
            }
            while (gamepad1.aWasPressed()){
                intake.setPower(-0.7);
            }
            while (gamepad1.aWasReleased()){
                intake.setPower(0);
            }


            // =================================
            // TELEMETRIA
            // =================================

            telemetry.addData(
                    "Heading",
                    "%.1f°",
                    Math.toDegrees(heading)
            );

            telemetry.addData(
                    "FL",
                    "%.2f",
                    frontLeftPower
            );

            telemetry.addData(
                    "FR",
                    "%.2f",
                    frontRightPower
            );

            telemetry.addData(
                    "RL",
                    "%.2f",
                    rearLeftPower
            );

            telemetry.addData(
                    "RR",
                    "%.2f",
                    rearRightPower
            );

            telemetry.addLine("");
            telemetry.addLine("Y = RESET FIELD");

            telemetry.update();
        }
    }
}

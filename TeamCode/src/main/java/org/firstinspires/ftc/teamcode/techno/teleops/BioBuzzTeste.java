package org.firstinspires.ftc.teamcode.techno.teleops;

import com.bylazar.configurables.annotations.Configurable;
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.IMU;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.YawPitchRollAngles;

@Configurable
@TeleOp(name = "BioBuzzTeste")
public class BioBuzzTeste extends LinearOpMode {

    // =========================
    // MOTORES
    // =========================

    private DcMotor leftFront;
    private DcMotor rightFront;
    private DcMotor leftRear;
    private DcMotor rightRear;
    private DcMotor intake, intake2, shooter;
    private Servo fdireita, fesquerda, hood;
    private CRServo storreta, storreta2;

    double powerT;

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
        fdireita  = hardwareMap.get(Servo.class, "f1");
        fesquerda = hardwareMap.get(Servo.class, "f2");
        intake2 = hardwareMap.get(DcMotor.class, "intake2");
        shooter = hardwareMap.get(DcMotor.class, "shooter");
        storreta = hardwareMap.get(CRServo.class, "storreta");
        storreta2 = hardwareMap.get(CRServo.class, "storreta2");
        hood = hardwareMap.get(Servo.class, "hood");


        // =========================
        // DIREÇÃO DOS MOTORES
        // =========================

        leftFront.setDirection(DcMotor.Direction.REVERSE);
        leftRear.setDirection(DcMotor.Direction.REVERSE);
        shooter.setDirection(DcMotorSimple.Direction.REVERSE);
        hood.setDirection(Servo.Direction.REVERSE);

        intake2.setDirection(DcMotorSimple.Direction.FORWARD);
        intake.setDirection(DcMotorSimple.Direction.REVERSE);
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
                RevHubOrientationOnRobot.LogoFacingDirection.RIGHT;

        RevHubOrientationOnRobot.UsbFacingDirection usbDirection =
                RevHubOrientationOnRobot.UsbFacingDirection.FORWARD;

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
        fdireita.setPosition(0.3);
        fesquerda.setPosition(0.3);

        // =========================
        // ZERA O FIELD AO INICIAR
        // =========================

        imu.resetYaw();


        // =========================
        // VARIÁVEIS DOS BOTÕES
        // =========================

        boolean lastY = false;
        boolean fState = false;
        boolean fState1 = false;
        boolean fState2 = false;


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

            //Torreta plano B
            powerT = gamepad2.right_stick_x;


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

            double cosA = Math.cos(heading);
            double sinA = Math.sin(heading);

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

            // Intake (sem loops 'while' bloqueantes)
            if (gamepad1.right_bumper) {
                intake.setPower(1);
                intake2.setPower(1);

            } else if (gamepad1.a) {
                intake.setPower(-0.5);
                intake2.setPower(-0.5);
            } else {
                intake.setPower(0);
                intake2.setPower(0);
            }

            // Alternar servos f1 e f2 (ON/OFF com o botão X)
            if (gamepad1.xWasPressed()) {
                fState = !fState;
                if (fState) {
                    fdireita.setPosition(1); // Posição ON // direita
                    fesquerda.setPosition(-1);//esquerda
                } else {
                    fdireita.setPosition(0.3); // Posição OFF
                    fesquerda.setPosition(0.3);

                }
            }

            storreta.setPower(powerT);
            storreta2.setPower(powerT);

            if (gamepad1.bWasPressed()) {
                fState1 = !fState1;
                if (fState1) {
                    shooter.setPower(0.85);
                } else {
                    shooter.setPower(0);
                }
            }

            if (gamepad1.dpadUpWasPressed()) {
                fState2 = !fState2;
                if (fState2) {
                    hood.setPosition(1);
                } else {
                    hood.setPosition(0);

                }
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
            telemetry.addData("Torreta: ", powerT);


            telemetry.addLine("");
            telemetry.addLine("Y = RESET FIELD");

            telemetry.update();
        }
    }
}

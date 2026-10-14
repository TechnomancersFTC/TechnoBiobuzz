package org.firstinspires.ftc.teamcode.techno.autos;

import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.IMU;

import java.util.Objects;


@Autonomous (name = "teste20horas")
public class Path20horas extends LinearOpMode {

    private DcMotor leftFront, rightFront, leftBack, rightBack, shooter, intake;
    private IMU imu;

    @Override
    public void runOpMode() {
        leftFront = hardwareMap.get(DcMotor.class, "leftFront");
        rightFront = hardwareMap.get(DcMotor.class, "rightFront");
        leftBack = hardwareMap.get(DcMotor.class, "leftBack");
        rightBack = hardwareMap.get(DcMotor.class, "rightBack");
        shooter = hardwareMap.get(DcMotor.class, "shooter");
        intake = hardwareMap.get(DcMotor.class, "intake");

        leftBack.setDirection(DcMotorSimple.Direction.FORWARD);
        leftFront.setDirection(DcMotorSimple.Direction.REVERSE);
        rightBack.setDirection(DcMotorSimple.Direction.REVERSE);

        RevHubOrientationOnRobot.LogoFacingDirection logoDirection = RevHubOrientationOnRobot.LogoFacingDirection.RIGHT;
        RevHubOrientationOnRobot.UsbFacingDirection  usbDirection  = RevHubOrientationOnRobot.UsbFacingDirection.BACKWARD;
        RevHubOrientationOnRobot orientationOnRobot = new RevHubOrientationOnRobot(logoDirection, usbDirection);

        // Now initialize the IMU with this mounting orientation
        // This sample expects the IMU to be in a REV Hub and named "imu".
        imu = hardwareMap.get(IMU.class, "imu");
        imu.initialize(new IMU.Parameters(orientationOnRobot));

        waitForStart();

        andar("e", 250);
        sleep(50);
        andar("t", 200);
        shoot();

    }

    private void andar(String direcao, long tempo){
        if (Objects.equals(direcao, "f")){
            leftFront.setPower(1);
            rightFront.setPower(1);
            leftBack.setPower(1);
            rightBack.setPower(1);
            sleep(tempo);
            leftFront.setPower(0);
            rightFront.setPower(0);
            leftBack.setPower(0);
            rightBack.setPower(0);

        }
        else if (Objects.equals(direcao, "t")){
            leftFront.setPower(-1);
            rightFront.setPower(-1);
            leftBack.setPower(-1);
            rightBack.setPower(-1);
            sleep(tempo);
            leftFront.setPower(0);
            rightFront.setPower(0);
            leftBack.setPower(0);
            rightBack.setPower(0);
        }
        else if (Objects.equals(direcao, "e")){
            leftFront.setPower(-1);
            rightFront.setPower(1);
            leftBack.setPower(1);
            rightBack.setPower(-1);
            sleep(tempo);
            leftFront.setPower(0);
            rightFront.setPower(0);
            leftBack.setPower(0);
            rightBack.setPower(0);
        }
        else if (Objects.equals(direcao, "d")){
            leftFront.setPower(1);
            rightFront.setPower(-1);
            leftBack.setPower(-1);
            rightBack.setPower(1);
            sleep(tempo);
            leftFront.setPower(0);
            rightFront.setPower(0);
            leftBack.setPower(0);
            rightBack.setPower(0);
        }
    }

    private void virar(String direcao, long tempo){
        if (Objects.equals(direcao, "e")){
            leftFront.setPower(-1);
            rightFront.setPower(1);
            leftBack.setPower(-1);
            rightBack.setPower(1);
            sleep(tempo);
            leftFront.setPower(0);
            rightFront.setPower(0);
            leftBack.setPower(0);
            rightBack.setPower(0);
        }
        else if (Objects.equals(direcao, "d")){
            leftFront.setPower(1);
            rightFront.setPower(-1);
            leftBack.setPower(1);
            rightBack.setPower(-1);
            sleep(tempo);
            leftFront.setPower(0);
            rightFront.setPower(0);
            leftBack.setPower(0);
            rightBack.setPower(0);
        }
        else {
            leftFront.setPower(0);
            rightFront.setPower(0);
            leftBack.setPower(0);
            rightBack.setPower(0);
        }
    }

    private void shoot(){
        shooter.setPower(0.65);
        sleep(1900);
        intake.setPower(1);
        sleep(135);
        intake.setPower(0);
        shooter.setPower(0.8);
        sleep(800);
        intake.setPower(1);
        sleep(100);
        intake.setPower(0);
        sleep(600);
        intake.setPower(1);
        sleep(200);
        intake.setPower(0);
        sleep(700);
        intake.setPower(1);
        sleep(200);
        intake.setPower(0);
    }
}
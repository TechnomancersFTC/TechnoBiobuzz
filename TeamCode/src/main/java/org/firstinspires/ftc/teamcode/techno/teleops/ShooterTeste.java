package org.firstinspires.ftc.teamcode.techno.teleops;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;

@TeleOp (name  = "ShooterTeste")
public class ShooterTeste extends LinearOpMode {

    private DcMotor shooter;

    double pot = 0;


    @Override
    public void runOpMode() {

        shooter = hardwareMap.get(DcMotor.class, "shooter");

        telemetry.addLine("Aperte B pra ligar o shooter");
        telemetry.addLine("");

        waitForStart();

        boolean fState1 = false;

        while (opModeIsActive()) {
            if (gamepad1.bWasPressed()) {
                fState1 = !fState1;
                if (fState1) {
                    shooter.setPower(pot);
                } else {
                    shooter.setPower(pot);
                }
            }

            telemetry.addData("potência do shooter: ", pot);

        }
    }
}


package org.firstinspires.ftc.teamcode.techno.teleops;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.techno.storage.Torreta;

@TeleOp(name = "BIOBUZZ - Torreta Automatica")
public class TesteTorreta extends LinearOpMode {

    @Override
    public void runOpMode() {

        Torreta torreta = new Torreta(hardwareMap);

        // ======================================
        // SELECAO DE ALIANCA NO INIT
        // ======================================

        Torreta.Alliance selectedAlliance =
                Torreta.Alliance.BLUE;

        boolean previousA = false;
        boolean confirmed = false;

        telemetry.addLine("===== BIOBUZZ =====");
        telemetry.addLine("SELECIONE A ALIANCA");
        telemetry.addLine("D-PAD ESQUERDO: BLUE");
        telemetry.addLine("D-PAD DIREITO: RED");
        telemetry.addLine("A: CONFIRMAR");
        telemetry.update();

        // ======================================
        // INIT LOOP
        // ======================================

        while (opModeInInit()) {

            if (gamepad1.dpad_left) {
                selectedAlliance = Torreta.Alliance.BLUE;
            }

            if (gamepad1.dpad_right) {
                selectedAlliance = Torreta.Alliance.RED;
            }

            boolean a = gamepad1.a;

            if (a && !previousA) {
                confirmed = true;
            }

            previousA = a;

            telemetry.addLine("===== BIOBUZZ =====");

            telemetry.addData(
                    "ALIANCA SELECIONADA",
                    selectedAlliance
            );

            telemetry.addData(
                    "CONFIRMADA",
                    confirmed ? "SIM" : "NAO"
            );

            telemetry.addLine(
                    "D-PAD ESQUERDO = BLUE"
            );

            telemetry.addLine(
                    "D-PAD DIREITO = RED"
            );

            telemetry.addLine(
                    "A = CONFIRMAR"
            );

            telemetry.update();

        }

        // ======================================
        // CONFIGURACAO FINAL
        // ======================================

        if (!confirmed) {
            telemetry.addLine("Selecione e confirme a alianca!");
            telemetry.update();
            return;
        }

        torreta.setAlliance(selectedAlliance);

        // POSICAO INICIAL PROVISORIA
        torreta.setInitialPose(
                0.0,
                0.0,
                0.0
        );

        // ======================================
        // TELEOP
        // ======================================

        boolean previousTest = false;
        boolean previousB = false;

        waitForStart();

        while (opModeIsActive()) {

            boolean a = gamepad1.a;
            boolean b = gamepad1.b;

            // MODO TESTE - TOGGLE

            if (a && !previousTest) {

                if (!torreta.isTestMode()) {
                    torreta.setRelativeTestTarget(24.0);
                } else {
                    torreta.setAutomaticAim();
                }
            }

            // RETORNO A MIRA AUTOMATICA

            if (b && !previousB) {
                torreta.setAutomaticAim();
            }

            previousTest = a;
            previousB = b;

            torreta.update();

            // TELEMETRIA

            telemetry.addLine("===== BIOBUZZ =====");

            telemetry.addData(
                    "Alliance",
                    torreta.getAlliance()
            );

            telemetry.addData(
                    "Celula",
                    torreta.getUpperCell()
            );

            telemetry.addData(
                    "Modo teste",
                    torreta.isTestMode()
            );

            telemetry.addLine("===== PINPOINT =====");

            telemetry.addData(
                    "Robot X",
                    "%.2f",
                    torreta.getRobotX()
            );

            telemetry.addData(
                    "Robot Y",
                    "%.2f",
                    torreta.getRobotY()
            );

            telemetry.addData(
                    "Heading",
                    "%.2f",
                    torreta.getRobotHeading()
            );

            telemetry.addLine("===== TORRETA =====");

            telemetry.addData(
                    "Angulo atual",
                    "%.2f",
                    torreta.getCurrentAngleValue()
            );

            telemetry.addData(
                    "Angulo desejado",
                    "%.2f",
                    torreta.getTargetAngle()
            );

            telemetry.addData(
                    "Alvo X",
                    "%.2f",
                    torreta.getTargetX()
            );

            telemetry.addData(
                    "Alvo Y",
                    "%.2f",
                    torreta.getTargetY()
            );

            telemetry.update();

        }

        torreta.stop();
    }
}

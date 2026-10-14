//package org.firstinspires.ftc.teamcode.techno.storage;
//
//import static org.firstinspires.ftc.robotcore.external.BlocksOpModeCompanion.telemetry;
//
//import com.pedropathing.ivy.pedro.PedroCommands;
//import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
//import com.qualcomm.robotcore.hardware.IMU;
//
//import java.math.MathContext;
//
//import dev.nextftc.core.subsystems.Subsystem;
//import dev.nextftc.functionalInterfaces.ConfiguratorMarker;
//import dev.nextftc.hardware.impl.CRServoEx;
//import dev.nextftc.hardware.impl.MotorEx;
//
///**
// * Subsistema da Torreta.
// * Responsável por controlar o movimento, rotação e mira automática da torreta.
// */
//
//public class Torreta implements Subsystem {
//
//
//    private IMU imu;
//    private final CRServoEx turret = new CRServoEx("torreta");
//    private final MotorEx encoder = new MotorEx("encoder");
//
//    // Encoder diretamente conectado à torreta.
//    // 8192 counts = 360°.
//    public double scalingFactor = 360.0 / 8192.0;
//
//    /**
//     * Converte uma leitura em ticks do encoder
//     * para o ângulo correspondente da torreta.
//     */
//    public double encoderTicksToAngle(int ticks) {
//        return ticks * scalingFactor;
//    }
//
//    /**
//     * Calcula a direção e rotaciona a torreta
//     * automaticamente em direção ao alvo.
//     */
//
//    public void aimToObject() {
//
//        // Posição atual do robô
//        double robotYPosition = PedroComponent.follower.getY();
//        double robotXPosition = PedroComponent.follower.getX();
//
//        // Ângulo do robô até o alvo
//        double destinationAngle = Math.toDegrees(
//                Math.atan2(
//                        poses.upperBlueHive.y() - robotYPosition,
//                        poses.upperBlueHive.x() - robotXPosition
//                )
//        );
//
//        // Ângulo atual da torreta
//        double turretAngle = encoderTicksToAngle(
//                (int) encoder.getCurrentPosition()
//        );
//
//        // Orientação atual do robô
//        double robotAngle = Math.toDegrees(PedroComponent.follower.getHeading());
//
//        // Ângulo que a torreta precisa ter em relação ao robô
//        double desiredTurretAngle = destinationAngle - robotAngle;
//
//        // Erro entre a posição desejada e a posição atual
//        double toTurn = desiredTurretAngle - turretAngle;
//
//        // CONTROLE DO CR SERVO ENTRA AQUI DEPOIS
//
//
//        // TELEMETRIA
//        telemetry.addData("Robot X", robotXPosition);
//        telemetry.addData("Robot Y", robotYPosition);
//        telemetry.addData("Robot Angle", robotAngle);
//
//        telemetry.addData("Destination Angle", destinationAngle);
//
//        telemetry.addData("Encoder Ticks", encoder.getCurrentPosition());
//        telemetry.addData("Turret Angle", turretAngle);
//
//        telemetry.addData("Desired Turret Angle", desiredTurretAngle);
//        telemetry.addData("Turret Error", toTurn);
//
//        telemetry.update();
//    }
//}
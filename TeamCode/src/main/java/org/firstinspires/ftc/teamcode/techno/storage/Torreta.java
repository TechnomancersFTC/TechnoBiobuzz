
package org.firstinspires.ftc.teamcode.techno.storage;

import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;

public class Torreta {

    // ==========================================
    // HARDWARE
    // ==========================================

    private final GoBildaPinpointDriver pinpoint;
    private final CRServo servoA;
    private final CRServo servoB;
    private final DcMotorEx encoder;

    // ==========================================
    // ALIANCA E CELULAS
    // ==========================================

    public enum Alliance {
        BLUE, RED
    }

    public enum HiveCell {
        GARDEN_SIDE,
        OPPOSITE_GARDEN
    }

    private Alliance alliance = Alliance.BLUE;

    private HiveCell upperCell = HiveCell.GARDEN_SIDE;

    // ==========================================
    // COORDENADAS DAS CELULAS
    // ==========================================

    // AZUL
    public static double BLUE_CELL_A_X = 83.8;
    public static double BLUE_CELL_A_Y = 56.6;

    public static double BLUE_CELL_B_X = 83.8;
    public static double BLUE_CELL_B_Y = 87.4;

    // VERMELHO
    public static double RED_CELL_A_X = 58.05;
    public static double RED_CELL_A_Y = 60.21;

    public static double RED_CELL_B_X = 58.05;
    public static double RED_CELL_B_Y = 83.4;

    // ==========================================
    // SELECAO AUTOMATICA POR POSICAO Y
    // ==========================================

    public static double FIELD_CENTER_Y = 72.0;

    public static double HIVE_SWITCH_MARGIN = 2.0;

    // ==========================================
    // OFFSET DA TORRETA
    // ==========================================

    public static double TURRET_OFFSET_X = -0.11811;
    public static double TURRET_OFFSET_Y = 0.0;

    // ==========================================
    // ENCODER
    // ==========================================

    public static double ENCODER_TICKS_PER_REV = 8192.0;

    public static boolean ENCODER_REVERSED = false;

    private double tickOffset;

    // ==========================================
    // CONTROLE
    // ==========================================

    public static double kP = 0.015;
    public static double kD = 0.0008;
    public static double kS = 0.05;

    public static double DEADBAND = 0.5;

    public static double MAX_POWER = 1.0;

    public static double MIN_ANGLE = -180.0;
    public static double MAX_ANGLE = 180.0;

    // ==========================================
    // ESTADO
    // ==========================================

    private double robotX;
    private double robotY;
    private double robotHeading;

    private double currentAngle;
    private double targetAngle;
    private double error;

    private double targetX;
    private double targetY;

    private boolean testMode = false;

    // ==========================================
    // CONSTRUTOR
    // ==========================================

    public Torreta(HardwareMap hw) {

        pinpoint = hw.get(
                GoBildaPinpointDriver.class,
                "pinpoint"
        );

        servoA = hw.get(CRServo.class, "storreta");

        servoB = hw.get(CRServo.class, "storreta2");

        encoder = hw.get(DcMotorEx.class, "encoder");

        servoA.setDirection(DcMotorSimple.Direction.FORWARD);
        servoB.setDirection(DcMotorSimple.Direction.FORWARD);

        encoder.setMode(
                DcMotor.RunMode.RUN_USING_ENCODER
        );

        // CONFIGURACAO DO PINPOINT

        pinpoint.setEncoderResolution(
                GoBildaPinpointDriver.GoBildaOdometryPods.goBILDA_4_BAR_POD
        );

        // Ambos os pods a 1.69291 polegadas para tras.
        // Confira os sinais conforme a convencao fisica
        // de offsets do Pinpoint.

        pinpoint.setOffsets(
                3.9328480517770363,
                0.6159939352921613,
                DistanceUnit.INCH
        );

        pinpoint.setEncoderDirections(
                GoBildaPinpointDriver.EncoderDirection.FORWARD,
                GoBildaPinpointDriver.EncoderDirection.REVERSED
        );

        pinpoint.resetPosAndIMU();

        tickOffset = readTicks();

        setAlliance(Alliance.BLUE);
    }

    // ==========================================
    // POSICAO INICIAL
    // ==========================================

    public void setInitialPose(
            double x,
            double y,
            double headingDegrees
    ) {

        pinpoint.setPosition(
                new Pose2D(
                        DistanceUnit.INCH,
                        x,
                        y,
                        AngleUnit.DEGREES,
                        headingDegrees
                )
        );
    }

    // ==========================================
    // ALIANCA
    // ==========================================

    public void setAlliance(Alliance alliance) {

        this.alliance = alliance;

        upperCell = HiveCell.GARDEN_SIDE;

        updateTargetCoordinates();
    }

    // ==========================================
    // COORDENADAS DA CELULA
    // ==========================================

    private void updateTargetCoordinates() {

        if (alliance == Alliance.BLUE) {

            if (upperCell == HiveCell.GARDEN_SIDE) {

                targetX = BLUE_CELL_A_X;
                targetY = BLUE_CELL_A_Y;

            } else {

                targetX = BLUE_CELL_B_X;
                targetY = BLUE_CELL_B_Y;

            }

        } else {

            if (upperCell == HiveCell.GARDEN_SIDE) {

                targetX = RED_CELL_A_X;
                targetY = RED_CELL_A_Y;

            } else {

                targetX = RED_CELL_B_X;
                targetY = RED_CELL_B_Y;

            }
        }
    }

    // ==========================================
    // SELECAO AUTOMATICA
    // ==========================================

    private void updateAutomaticHiveSelection() {

        if (robotY > 74.0) {

            upperCell = HiveCell.GARDEN_SIDE;

        }
        else if (robotY < 70.0) {

            upperCell = HiveCell.OPPOSITE_GARDEN;

        }

        updateTargetCoordinates();
    }

    // ==========================================
    // MODO TESTE
    // ==========================================

    public void setRelativeTestTarget(double distance) {

        double heading = robotHeading;

        targetX = robotX + distance * Math.cos(heading);

        targetY = robotY + distance * Math.sin(heading);

        testMode = true;
    }

    public void setAutomaticAim() {

        testMode = false;

        updateTargetCoordinates();
    }

    public boolean isTestMode() {
        return testMode;
    }

    // ==========================================
    // ENCODER
    // ==========================================

    private double readTicks() {

        double ticks = encoder.getCurrentPosition();

        return ENCODER_REVERSED ? -ticks : ticks;
    }

    public double getCurrentAngle() {

        return (readTicks() - tickOffset)
                * 360.0 / ENCODER_TICKS_PER_REV;
    }

    private double getVelocity() {

        double velocity = encoder.getVelocity();

        if (ENCODER_REVERSED) {
            velocity = -velocity;
        }

        return velocity * 360.0 / ENCODER_TICKS_PER_REV;
    }

    // ==========================================
    // ATUALIZACAO PRINCIPAL
    // ==========================================

    public void update() {

        pinpoint.update();

        Pose2D pose = pinpoint.getPosition();

        robotX = pose.getX(DistanceUnit.INCH);
        robotY = pose.getY(DistanceUnit.INCH);

        robotHeading = pose.getHeading(
                AngleUnit.RADIANS
        );

        if (!testMode) {
            updateAutomaticHiveSelection();
        }

        // Posicao da torreta no campo.

        double turretX =
                robotX
                        + TURRET_OFFSET_X * Math.cos(robotHeading)
                        - TURRET_OFFSET_Y * Math.sin(robotHeading);

        double turretY =
                robotY
                        + TURRET_OFFSET_X * Math.sin(robotHeading)
                        + TURRET_OFFSET_Y * Math.cos(robotHeading);

        // Direcao do alvo.

        double dx = targetX - turretX;

        double dy = targetY - turretY;

        double fieldAngle = Math.toDegrees(
                Math.atan2(dy, dx)
        );

        double relativeAngle = normalize180(
                fieldAngle - Math.toDegrees(robotHeading)
        );

        currentAngle = getCurrentAngle();

        targetAngle = chooseReachableTarget(
                relativeAngle,
                currentAngle
        );

        error = targetAngle - currentAngle;

        driveTo(targetAngle);
    }

    // ==========================================
    // CONTROLE DOS SERVOS
    // ==========================================

    private void driveTo(double target) {

        double e = target - currentAngle;

        if (Math.abs(e) < DEADBAND) {

            stop();
            return;
        }

        double power =
                kP * e
                        - kD * getVelocity()
                        + Math.signum(e) * kS;

        power = Math.max(
                -MAX_POWER,
                Math.min(MAX_POWER, power)
        );

        if (currentAngle >= MAX_ANGLE && power > 0) {
            power = 0;
        }

        if (currentAngle <= MIN_ANGLE && power < 0) {
            power = 0;
        }

        servoA.setPower(power);
        servoB.setPower(power);
    }

    // ==========================================
    // MENOR GIRO POSSIVEL
    // ==========================================

    private double chooseReachableTarget(
            double desired,
            double current
    ) {

        double best = Double.NaN;

        for (int k = -2; k <= 2; k++) {

            double candidate = desired + 360.0 * k;

            if (candidate < MIN_ANGLE ||
                    candidate > MAX_ANGLE) {
                continue;
            }

            if (Double.isNaN(best) ||
                    Math.abs(candidate - current)
                            < Math.abs(best - current)) {

                best = candidate;
            }
        }

        if (Double.isNaN(best)) {

            best = Math.max(
                    MIN_ANGLE,
                    Math.min(MAX_ANGLE, desired)
            );
        }

        return best;
    }

    // ==========================================
    // NORMALIZACAO
    // ==========================================

    private static double normalize180(double angle) {

        angle %= 360.0;

        if (angle > 180) angle -= 360;

        if (angle <= -180) angle += 360;

        return angle;
    }

    // ==========================================
    // TELEMETRIA
    // ==========================================

    public double getRobotX() {
        return robotX;
    }

    public double getRobotY() {
        return robotY;
    }

    public double getRobotHeading() {
        return Math.toDegrees(robotHeading);
    }

    public double getCurrentAngleValue() {
        return currentAngle;
    }

    public double getTargetAngle() {
        return targetAngle;
    }

    public double getError() {
        return error;
    }

    public double getTargetX() {
        return targetX;
    }

    public double getTargetY() {
        return targetY;
    }

    public Alliance getAlliance() {
        return alliance;
    }

    public HiveCell getUpperCell() {
        return upperCell;
    }

    public void stop() {

        servoA.setPower(0);
        servoB.setPower(0);
    }
}

//package org.firstinspires.ftc.teamcode.techno.storage;
//
//import com.pedropathing.follower.Follower;
//import com.pedropathing.math.Pose;
//import com.qualcomm.robotcore.hardware.CRServo;
//import com.qualcomm.robotcore.hardware.DcMotorEx;
//import com.qualcomm.robotcore.hardware.DcMotorSimple;
//import com.qualcomm.robotcore.hardware.HardwareMap;
//
//import org.firstinspires.ftc.teamcode.pedro.Constants;
//
//import dev.nextftc.core.components.BindingsComponent;
//import dev.nextftc.core.components.SubsystemComponent;
//import dev.nextftc.core.subsystems.Subsystem;
//import dev.nextftc.ftc.components.BulkReadComponent;
//import dev.nextftc.functionalInterfaces.ConfiguratorMarker;
//
//
//public class Torreta implements Subsystem {
//
//    public Torreta() {
//        addComponents(
//                new PedroComponent(Constants::createFollower)
//        );
//    }
//
//
//    public enum Alliance { BLUE, RED }
//
//    // ====== ALVOS (coordenadas do Pedro, polegadas) ======
//    public static final double BLUE_HIVE_X = 83.8;
//    public static final double BLUE_HIVE_Y = 87.4;
//    // Espelhamento em X (campo 144x144). Confirme no visualizer!
//    public static final double RED_HIVE_X  = 58;
//    public static final double RED_HIVE_Y  = 83.5;
//
//    // ====== MECÂNICA ======
//    // Ticks do encoder por volta COMPLETA da torreta.
//    // = 8192 * (voltas do encoder por volta da torreta). Se o encoder está
//    // direto no eixo da torreta, é 8192. Se passa por engrenagem, multiplique pela relação.
//    public static double ENCODER_TICKS_PER_REV = 8192.0 * 1.0;
//    public static boolean ENCODER_REVERSED = false;
//
//
//    // Posição do centro da torreta em relação ao ponto de rastreio do Pinpoint (referencial do robô)
//    public static double TURRET_OFFSET_X = 0.0; // pol, + = frente
//    public static double TURRET_OFFSET_Y = 0.0; // pol, + = esquerda
//
//
//    // Limites de giro (evita enrolar fios). Graus, relativos à frente do robô.
//    public static double MIN_ANGLE = -180.0;
//    public static double MAX_ANGLE =  180.0;
//
//
//    // ====== CONTROLE (ajustar no robô) ======
//    public static double kP = 0.015;          // potência por grau de erro
//    public static double kD = 0.0008;         // amortecimento (usa velocidade medida)
//    public static double kS = 0.05;           // potência mínima p/ vencer o atrito
//    public static double kHeadingFF = 0.0;    // feedforward da rotação do robô (comece em 0)
//    public static double DEADBAND_DEG = 0.5;
//    public static double MAX_POWER = 1.0;
//
//    private CRServo servoA, servoB;
//    private DcMotorEx encoder;
//    private double tickOffset;          // zera o encoder na inicialização
//
//    private Alliance alliance = Alliance.BLUE;
//    private boolean autoAimEnabled = true;
//    private double manualTargetDeg = 0.0;
//
//    private double lastHeadingRad = Double.NaN;
//    private long lastTimeNs = 0;
//    private double robotOmegaDegPerSec = 0.0;
//
//    public Torreta(HardwareMap hw) {
//        servoA  = hw.get(CRServo.class, "storreta");
//        servoB  = hw.get(CRServo.class, "storreta1");
//        encoder = hw.get(DcMotorEx.class, "encoder"); // nome da porta do Through Bore
//
//        // Os dois servos giram a mesma engrenagem: normalmente um fica invertido.
//        servoA.setDirection(DcMotorSimple.Direction.FORWARD);
//        servoB.setDirection(DcMotorSimple.Direction.FORWARD);
//
//        // A torreta deve estar fisicamente no 0° (virada p/ frente) ao iniciar.
//        tickOffset = readRawTicks();
//    }
//
//    // ---------- conversões ----------
//    public double ticksToDegrees(double ticks) {
//        return ticks * 360.0 / ENCODER_TICKS_PER_REV;
//    }
//
//    public double degreesToTicks(double degrees) {
//        return degrees * ENCODER_TICKS_PER_REV / 360.0;
//    }
//
//    private double readRawTicks() {
//        double t = encoder.getCurrentPosition();
//        return ENCODER_REVERSED ? -t : t;
//    }
//
//    public double getAngleDeg() {
//        return ticksToDegrees(readRawTicks() - tickOffset);
//    }
//
//    private double getVelocityDegPerSec() {
//        double v = encoder.getVelocity();
//        if (ENCODER_REVERSED) v = -v;
//        return ticksToDegrees(v);
//    }
//
//    // ---------- API ----------
//    public void setAlliance(Alliance a) { alliance = a; }
//    public void setAutoAim(boolean on) { autoAimEnabled = on; }
//    public void setManualTarget(double deg) { manualTargetDeg = deg; }
//
//    /** Chame UMA vez por loop, depois de follower.update(). */
//    public void update(Pose robotPose) {
//        double headingRad = PedroComponent.follower.getHeading(); // radianos (ajuste se a v3 usar outra unidade)
//        updateRobotOmega(headingRad);
//
//        double desiredDeg;
//        if (autoAimEnabled) {
//            desiredDeg = computeRelativeAimAngle(robotPose);
//        } else {
//            desiredDeg = manualTargetDeg;
//        }
//
//        double target = chooseReachableTarget(desiredDeg, getAngleDeg());
//        driveTo(target);
//    }
//
//    public void stop() {
//        servoA.setPower(0);
//        servoB.setPower(0);
//    }
//
//    /** Ângulo (graus) que a torreta precisa ter relativo à frente do robô para mirar na Hive. */
//    public double computeRelativeAimAngle(Pose robotPose) {
//        double h = PedroComponent.follower.getHeading();
//
//        // Posição da torreta no campo (offset do referencial do robô -> campo)
//        double turretX = PedroComponent.follower.getX() + TURRET_OFFSET_X * Math.cos(h) - TURRET_OFFSET_Y * Math.sin(h);
//        double turretY = PedroComponent.follower.getY() + TURRET_OFFSET_X * Math.sin(h) + TURRET_OFFSET_Y * Math.cos(h);
//
//        double targetX = (alliance == Alliance.BLUE) ? BLUE_HIVE_X : RED_HIVE_X;
//        double targetY = (alliance == Alliance.BLUE) ? BLUE_HIVE_Y : RED_HIVE_Y;
//
//        // Ângulo no campo até a Hive
//        double fieldAngleDeg = Math.toDegrees(Math.atan2(targetY - turretY, targetX - turretX));
//
//        // Ângulo relativo ao robô
//        double relative = fieldAngleDeg - Math.toDegrees(h);
//        return normalize180(relative);
//    }
//
//    // ---------- internos ----------
//    private void driveTo(double targetDeg) {
//        double current = getAngleDeg();
//        double error = targetDeg - current;
//
//        if (Math.abs(error) < DEADBAND_DEG) {
//            stop();
//            return;
//        }
//
//        double power = kP * error
//                - kD * getVelocityDegPerSec()      // derivada na medição
//                - kHeadingFF * robotOmegaDegPerSec // robô gira p/ um lado, torreta compensa p/ o outro
//                + Math.signum(error) * kS;
//
//        power = Math.max(-MAX_POWER, Math.min(MAX_POWER, power));
//
//        // Proteção nos limites físicos
//        if ((current >= MAX_ANGLE && power > 0) || (current <= MIN_ANGLE && power < 0)) power = 0;
//
//        servoA.setPower(power);
//        servoB.setPower(power);
//    }
//
//    /** Entre as opções equivalentes (±360°), escolhe a mais próxima que cabe nos limites. */
//    private double chooseReachableTarget(double desired, double current) {
//        double best = Double.NaN;
//        for (int k = -1; k <= 1; k++) {
//            double cand = desired + 360.0 * k;
//            if (cand < MIN_ANGLE || cand > MAX_ANGLE) continue;
//            if (Double.isNaN(best) || Math.abs(cand - current) < Math.abs(best - current)) best = cand;
//        }
//        if (Double.isNaN(best)) {
//            best = Math.max(MIN_ANGLE, Math.min(MAX_ANGLE, normalize180(desired)));
//        }
//        return best;
//    }
//
//    private void updateRobotOmega(double headingRad) {
//        long now = System.nanoTime();
//        if (!Double.isNaN(lastHeadingRad)) {
//            double dt = (now - lastTimeNs) / 1e9;
//            if (dt > 1e-3) {
//                double dh = Math.toDegrees(normalize180Rad(headingRad - lastHeadingRad));
//                robotOmegaDegPerSec = dh / dt;
//            }
//        }
//        lastHeadingRad = headingRad;
//        lastTimeNs = now;
//    }
//
//    private static double normalize180(double deg) {
//        deg = deg % 360.0;
//        if (deg > 180.0) deg -= 360.0;
//        if (deg <= -180.0) deg += 360.0;
//        return deg;
//    }
//
//    private static double normalize180Rad(double rad) {
//        rad = rad % (2 * Math.PI);
//        if (rad > Math.PI) rad -= 2 * Math.PI;
//        if (rad <= -Math.PI) rad += 2 * Math.PI;
//        return rad;
//    }
//}
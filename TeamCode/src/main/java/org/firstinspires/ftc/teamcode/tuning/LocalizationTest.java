package org.firstinspires.ftc.teamcode.tuning;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.PoseVelocity2d;
import com.acmerobotics.roadrunner.Vector2d;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.teamcode.Drawing;
import org.firstinspires.ftc.teamcode.MecanumDrive;
import org.firstinspires.ftc.teamcode.TankDrive;

public class LocalizationTest extends LinearOpMode {
    @Override
    public void runOpMode() throws InterruptedException {
        telemetry = new MultipleTelemetry(telemetry, FtcDashboard.getInstance().getTelemetry());

        if (TuningOpModes.DRIVE_CLASS.equals(MecanumDrive.class)) {
            MecanumDrive drive = new MecanumDrive(hardwareMap, new Pose2d(0, 0, 0));

            waitForStart();
            drive.leftFront.setPower(0);
            drive.rightFront.setPower(0);
            while (opModeIsActive()) {
                drive.setDrivePowers(new PoseVelocity2d(
                        new Vector2d(
                                -gamepad1.left_stick_y,
                                -gamepad1.right_stick_x
                        ),
                        -gamepad1.left_stick_x
                ));

                drive.updatePoseEstimate();

                telemetry.addData("x", drive.pose.position.x);
                telemetry.addData("y", drive.pose.position.y);
                telemetry.addData("heading (deg)", Math.toDegrees(drive.pose.heading.toDouble()));
                telemetry.update();

                TelemetryPacket packet = new TelemetryPacket();
                packet.fieldOverlay().setStroke("#3F51B5");
                Drawing.drawRobot(packet.fieldOverlay(), drive.pose);
                FtcDashboard.getInstance().sendTelemetryPacket(packet);

                if (gamepad1.left_trigger > 0.2){
                    while(gamepad1.left_trigger > 0.2){
                        drive.linearMove(gamepad1.left_trigger);
                    }
                    drive.linearMove(0);
                }
                if (gamepad1.right_trigger > 0.2){
                    while(gamepad1.right_trigger > 0.2){
                        drive.linearMove(-gamepad1.right_trigger);
                    }
                    drive.linearMove(0);
                }

                if (gamepad1.a){
                    drive.intakeMove(0.7);
                    while(gamepad1.a){
                    }
                    drive.intakeMove(0);
                }
                if (gamepad1.b){
                    drive.intakeMove(-0.7);
                    while(gamepad1.b){
                    }
                    drive.intakeMove(0);
                }


            }
        } else {
            throw new RuntimeException();
        }
    }
}

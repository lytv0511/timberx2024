package org.firstinspires.ftc.teamcode;

import androidx.annotation.NonNull;

import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.acmerobotics.roadrunner.Action;
import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.SequentialAction;
import com.acmerobotics.roadrunner.Vector2d;
import com.acmerobotics.roadrunner.ftc.Actions;
import com.acmerobotics.roadrunner.TrajectoryActionBuilder;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

@Autonomous(name = "ScoringRoutine", group = "Autonomous")
public class ScoringRoutine extends LinearOpMode {
    @Override
    public void runOpMode() {
        // Initialize hardware and drive system
        Pose2d initialPose = new Pose2d(0, 0, Math.toRadians(90));
        MecanumDrive drive = new MecanumDrive(hardwareMap, initialPose);

        // Define sample block and basket positions
        Vector2d samplePosition = new Vector2d(48, 48); // Sample block position
        Vector2d basketPosition = new Vector2d(60, 60); // Basket position

        // Trajectories for navigation
        TrajectoryActionBuilder grabSampleTrajectory = drive.actionBuilder(initialPose)
                .splineTo(samplePosition, Math.toRadians(0)) // Navigate to the block
                .endTrajectory();

        TrajectoryActionBuilder deliverToBasketTrajectory = drive.actionBuilder(new Pose2d(samplePosition, Math.toRadians(90)))
                .splineTo(basketPosition, Math.toRadians(90)) // Navigate to the basket
                .endTrajectory();

        waitForStart();

        if (isStopRequested()) return;

        // Action sequence for scoring
        Actions.runBlocking(
                new SequentialAction(
                        // Navigate to sample
                        grabSampleTrajectory.build(),

                        // Lower grabber to grab the block
                        new Action() {
                            @Override
                            public boolean run(@NonNull TelemetryPacket packet) {
                                drive.grabberMove(0); // Move grabber to grabbing position
                                return false;
                            }
                        },

                        // Activate intake servo to suck in the block
                        new Action() {
                            private long startTime = -1;

                            @Override
                            public boolean run(@NonNull TelemetryPacket packet) {
                                if (startTime == -1) {
                                    startTime = System.currentTimeMillis();
                                    drive.intakeMove(1.0); // Start intake
                                }
                                if (System.currentTimeMillis() - startTime > 2000) { // Stop after 2 seconds
                                    drive.intakeMove(0);
                                    return false;
                                }
                                return true;
                            }
                        },

                        // Raise grabber to transport position
                        new Action() {
                            @Override
                            public boolean run(@NonNull TelemetryPacket packet) {
                                drive.grabberMove(500); // Raise grabber
                                return false;
                            }
                        },

                        // Navigate to the basket
                        deliverToBasketTrajectory.build(),

                        // Lift the linear slides
                        new Action() {
                            @Override
                            public boolean run(@NonNull TelemetryPacket packet) {
                                drive.linearMove(0.8); // Lift the slides
                                try {
                                    Thread.sleep(1500); // Wait for the slides to fully extend
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                drive.linearMove(0); // Stop slides
                                return false;
                            }
                        },

                        // Tilt tray servo to deposit the block
                        new Action() {
                            @Override
                            public boolean run(@NonNull TelemetryPacket packet) {
                                drive.intakeMove(1.0); // Adjust tray servo to pour
                                try {
                                    Thread.sleep(500); // Allow servo time to pour
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                drive.intakeMove(0);
                                return false;
                            }
                        },

                        // Lower the linear slides
                        new Action() {
                            @Override
                            public boolean run(@NonNull TelemetryPacket packet) {
                                drive.linearMove(-0.8); // Lower slides
                                try {
                                    Thread.sleep(1500); // Wait for the slides to fully retract
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                drive.linearMove(0); // Stop slides
                                return false;
                            }
                        }
                )
        );
    }
}
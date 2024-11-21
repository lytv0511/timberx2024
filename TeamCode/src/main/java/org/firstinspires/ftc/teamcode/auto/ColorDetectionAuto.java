@Autonomous(name = "Color Detection Auto")
public class ColorDetectionAuto extends LinearOpMode {
    private MecanumDrive drive;
    private AprilTagProcessor aprilTag;
    private VisionPortal visionPortal;
    private DcMotor armMotor;
    private DcMotor linearLeft;
    private DcMotor linearRight;
    private Servo intakeServo;
    private Servo tiltTray;

    @Override
    public void runOpMode() throws InterruptedException {
        drive = new MecanumDrive(hardwareMap, new Pose2d(0, 0, 0));
        armMotor = hardwareMap.get(DcMotor.class, "armMotor");
        linearLeft = hardwareMap.get(DcMotor.class, "linearLeft");
        linearRight = hardwareMap.get(DcMotor.class, "linearRight");
        intakeServo = hardwareMap.get(Servo.class, "intakeServo");
        tiltTray = hardwareMap.get(Servo.class, "tiltTray");
        
        // Initialize vision system
        aprilTag = new AprilTagProcessor.Builder()
            .build();
        visionPortal = new VisionPortal.Builder()
            .addProcessor(aprilTag)
            .build();

        waitForStart();

        if (isStopRequested()) return;

        // First move to y = 0 (middle of field)
        drive.actionBuilder()
            .strafeRight(60)
            .build();

        // Vision detection loop while moving toward center
        boolean targetFound = false;
        while (!targetFound && opModeIsActive()) {
            List<AprilTagDetection> currentDetections = aprilTag.getDetections();
            
            for (AprilTagDetection detection : currentDetections) {
                if (detection.metadata != null) {
                    targetFound = true;
                    
                    // Stop movement
                    drive.setDrivePowers(new PoseVelocity2d(new Vector2d(0, 0), 0));
                    
                    // Execute pickup sequence
                    drive.controlArm(-0.8);
                    sleep(1000);
                    
                    drive.intakeMove(1.0);
                    sleep(500);
                    
                    drive.intakeMove(0);
                    
                    drive.controlArm(0.8);
                    sleep(1000);
                    
                    drive.controlArm(0);
                    break;
                }
            }
            
            drive.setDrivePowers(new PoseVelocity2d(new Vector2d(0.2, 0), 0));
        }
        
        // Return to starting position
        drive.actionBuilder()
            .strafeLeft(60)
            .build();

        // Scoring sequence with dual linear slides
        drive.linearMove(0.8);  // Extend both slides
        sleep(1500);
        drive.linearMove(0);

        drive.tiltTray(1.0, 500);

        // Retract both linear slides
        drive.linearMove(-0.8);
        sleep(1500);
        drive.linearMove(0);

        // Return to initial position
        drive.actionBuilder()
            .back(30)
            .build();
    }
}
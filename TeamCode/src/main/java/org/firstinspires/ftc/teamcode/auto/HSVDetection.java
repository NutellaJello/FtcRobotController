package org.firstinspires.ftc.teamcode.auto;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.openftc.easyopencv.OpenCvPipeline;
import org.openftc.easyopencv.OpenCvWebcam;

public class HSVDetection extends OpenCvPipeline {
    /*
    GREEN  = Parking Left
    ORANGE    = Parking Middle
    PURPLE = Parking Right
     */

    private OpenCvWebcam webcam;
    boolean viewportPaused;
    public enum ParkingPosition {
        LEFT,
        CENTER,
        RIGHT
    }

    // TOPLEFT anchor point for the bounding box
    private static Point SLEEVE_TOPLEFT_ANCHOR_POINT = new Point(145, 168);

    // Width and height for the bounding box

    static final Point LEFT_REGION_TOPLEFT_POINT = new Point(0,98);
    static final Point CENTER_REGION_TOPLEFT_POINT = new Point(150,98);
    static final Point RIGHT_REGION_TOPLEFT_POINT = new Point(300,98);
    static final int REGION_WIDTH = 40;
    static final int REGION_HEIGHT = 60;

    // Lower and upper boundaries for colors
    private static final Scalar



            lower_red_bounds = new Scalar(0,50,50),

            upper_red_bounds = new Scalar(15,255,255),

            lower_red_bounds_2 = new Scalar(170,50,50),

            upper_red_bounds_2 = new Scalar(180,255,255),

            lower_blue_bounds = new Scalar(100,50,50),

            upper_blue_bounds = new Scalar(120,255,255);


            // old colors from 2021-2022s game
//            lower_green_bounds  = new Scalar(42, 50, 50),
//            upper_green_bounds  = new Scalar(76, 255, 255),
//            lower_orange_bounds    = new Scalar(13, 50,50),
//            upper_orange_bounds    = new Scalar(20, 255, 255),
//            lower_purple_bounds = new Scalar(138, 50, 50),
//            upper_purple_bounds = new Scalar(155, 255, 255);


    // Color definitions
//    public final Scalar BLUE = new Scalar(0, 0, 255);
//    public final Scalar GREEN = new Scalar(0, 255, 0);
    private final Scalar
            GREEN  = new Scalar(255, 255, 0),
            ORANGE    = new Scalar(0, 255, 255),
            PURPLE = new Scalar(255, 0, 255);



    // Percent and mat definitions
    private double grePercent, oraPercent, purPercent;
    private Mat leftMat = new Mat(), rightMat = new Mat(), centerMat = new Mat();
    private Mat leftBlurredMat = new Mat(), centerBlurredMat = new Mat(), rightBlurredMat = new Mat();
    private Mat leftKernel = new Mat(), centerKernel = new Mat(), rightKernel = new Mat();
    Point left_region_pointA = new Point(
            LEFT_REGION_TOPLEFT_POINT.x,
            LEFT_REGION_TOPLEFT_POINT.y);
    Point left_region1_pointB = new Point(
            LEFT_REGION_TOPLEFT_POINT.x + REGION_WIDTH,
            LEFT_REGION_TOPLEFT_POINT.y + REGION_HEIGHT);
    Point center_region_pointA = new Point(
            CENTER_REGION_TOPLEFT_POINT.x,
            CENTER_REGION_TOPLEFT_POINT.y);
    Point center_region_pointB = new Point(
            CENTER_REGION_TOPLEFT_POINT.x + REGION_WIDTH,
            CENTER_REGION_TOPLEFT_POINT.y + REGION_HEIGHT);
    Point right_region_pointA = new Point(
            RIGHT_REGION_TOPLEFT_POINT.x,
            RIGHT_REGION_TOPLEFT_POINT.y);
    Point right_region_pointB = new Point(
            RIGHT_REGION_TOPLEFT_POINT.x + REGION_WIDTH,
            RIGHT_REGION_TOPLEFT_POINT.y + REGION_HEIGHT);
    // Anchor point definitions


    // Running variable storing the parking position
    private volatile ParkingPosition position = ParkingPosition.LEFT;

    private Telemetry telemetry;
    public HSVDetection(OpenCvWebcam webcam, Telemetry telemetry) {
        this.webcam = webcam;
        this.telemetry = telemetry;

    }

    @Override
    public Mat processFrame(Mat input) {
        // Noise reduction
        Imgproc.cvtColor(input, input, Imgproc.COLOR_RGB2HSV);
        // left
        Imgproc.blur(input, leftBlurredMat, new Size(5, 5));
        leftBlurredMat = leftBlurredMat.submat(new Rect(left_region_pointA, left_region1_pointB));

        // Apply Morphology
        leftKernel= Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));
        Imgproc.morphologyEx(leftBlurredMat, leftBlurredMat, Imgproc.MORPH_CLOSE, leftKernel);



        // center
        Imgproc.blur(input, centerBlurredMat, new Size(5, 5));
        centerBlurredMat = centerBlurredMat.submat(new Rect(center_region_pointA, center_region_pointB));

        // Apply Morphology
        centerKernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));
        Imgproc.morphologyEx(centerBlurredMat, centerBlurredMat, Imgproc.MORPH_CLOSE, centerKernel);



        // right
        Imgproc.blur(input, rightBlurredMat, new Size(5, 5));
        rightBlurredMat = rightBlurredMat.submat(new Rect(right_region_pointA, right_region_pointB));

        // Apply Morphology
        rightKernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));
        Imgproc.morphologyEx(centerBlurredMat, centerBlurredMat, Imgproc.MORPH_CLOSE, centerKernel);



        // Gets channels from given source mat
        Core.inRange(leftBlurredMat, lower_red_bounds, upper_red_bounds, leftMat);
        Core.inRange(centerBlurredMat, lower_red_bounds, upper_red_bounds, centerMat);
        Core.inRange(rightBlurredMat, lower_red_bounds, upper_red_bounds, rightMat);

        // Gets color specific values
        int leftPercent = Core.countNonZero(leftMat);
        int centerPercent = Core.countNonZero(centerMat);
        int rightPercent = Core.countNonZero(rightMat);

        // Calculates the highest amount of pixels being covered on each side
        double maxPercent = Math.max(leftPercent, Math.max(centerPercent, rightPercent));

        // Checks all percentages, will highlight bounding box in camera preview
        // based on what color is being detected
        if (maxPercent == leftPercent) {
            position = ParkingPosition.LEFT;
            Imgproc.rectangle(
                    input,
                    left_region_pointA,
                    left_region1_pointB,
                    GREEN,
                    -1
            );
        } else if (maxPercent == centerPercent) {
            position = ParkingPosition.CENTER;
            Imgproc.rectangle(
                    input,
                    center_region_pointA,
                    center_region_pointB,
                    GREEN,
                    -1
            );
        } else if (maxPercent == rightPercent) {
            position = ParkingPosition.RIGHT;
            Imgproc.rectangle(
                    input,
                    right_region_pointA,
                    right_region_pointB,
                    GREEN,
                    -1
            );
        }

        // Memory cleanup
        leftBlurredMat.release();
        centerBlurredMat.release();
        rightBlurredMat.release();
        leftMat.release();
        centerMat.release();
        rightMat.release();
        leftKernel.release();
        centerKernel.release();
        rightKernel.release();

        return input;
    }

    // Returns an enum being the current position where the robot will park
    public ParkingPosition getPosition() {
        return position;
    }

    public void onViewportTapped() {
        this.viewportPaused = !this.viewportPaused;
        if (this.viewportPaused) {
            this.webcam.pauseViewport();
        } else {
            this.webcam.resumeViewport();
        }
    }
}
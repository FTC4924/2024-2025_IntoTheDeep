@file:Suppress("MemberVisibilityCanBePrivate")

package org.firstinspires.ftc.teamcodekt.opmodes.auto

import com.outoftheboxrobotics.photoncore.PhotonCore
import ftc.rogue.blacksmith.BlackOp
import ftc.rogue.blacksmith.util.kt.LateInitVal
import ftc.rogue.blacksmith.util.kt.invoke
import ftc.rogue.blacksmith.util.kt.toCm
import org.firstinspires.ftc.teamcode.AutoData
import org.firstinspires.ftc.teamcode.pipelines.AprilTagDetectionPipeline
import org.firstinspires.ftc.teamcode.pipelines.BasePoleDetector
import org.firstinspires.ftc.teamcodekt.components.*
import org.openftc.apriltag.AprilTagDetection
import org.openftc.easyopencv.OpenCvCamera
import org.openftc.easyopencv.OpenCvCamera.AsyncCameraOpenListener
import org.openftc.easyopencv.OpenCvCameraFactory
import org.openftc.easyopencv.OpenCvCameraRotation
import kotlin.math.max

abstract class RogueBaseAuto : BlackOp() {
    protected val frontSensor by createOnGo<ShortRangeSensor> { hwMap }

    protected val bot by evalOnGo {
        createAutoBotComponents(hwMap, VoltageScaler(hwMap))
    }

    protected var camera by LateInitVal<OpenCvCamera>()

    protected var aprilTagDetectionPipeline = AprilTagDetectionPipeline(tagsize, fx, fy, cx, cy);
    protected var poleDetector = BasePoleDetector(telemetry)
    protected var numFramesWithoutDetection = 0

    // Left and right is from orientation of the front of the bot
    protected val shortLeftSensor by createOnGo<ShortRangeSensor>({ hwMap }, { DeviceNames.SHORT_RANGE_SENSOR_LEFT })
    protected val shortRightSensor by createOnGo<ShortRangeSensor>({ hwMap }, { DeviceNames.SHORT_RANGE_SENSOR_RIGHT })
//    protected val longLeftSensor by createOnGo<LongRangeSensor>({ hwMap }, { DeviceNames.LONG_RANGE_SENSOR_LEFT })
//    protected val longRightSensor by createOnGo<LongRangeSensor>({ hwMap }, { DeviceNames.LONG_RANGE_SENSOR_RIGHT })

    abstract fun execute()

    final override fun go() {
        PhotonCore.enable()
        initHardware()
        execute()
    }

    private fun initHardware() {


        //***************************
        // Set up camera and pipeline
        //***************************
        val cameraMonitorViewId = hardwareMap.appContext.resources.getIdentifier(
            "cameraMonitorViewId",
            "id",
            hardwareMap.appContext.packageName,
        )

        camera = OpenCvCameraFactory.getInstance().createWebcam(
            hardwareMap(DeviceNames.WEBCAM1),
            cameraMonitorViewId,
        )

        camera.setPipeline(aprilTagDetectionPipeline)

        camera.openCameraDeviceAsync(object : AsyncCameraOpenListener {
            override fun onOpened() {
                camera.startStreaming(1280, 720, OpenCvCameraRotation.UPRIGHT)
            }

            override fun onError(errorCode: Int) {
                throw RuntimeException("Error opening camera! Error code $errorCode");
            }
        })
    }
    fun waitForStartWithVision(): Int {
        var lastIntID = -1

        while (!opModeIsActive()) {
            val detections: ArrayList<AprilTagDetection> = aprilTagDetectionPipeline.detectionsUpdate

            telemetry.addData("FPS",         camera.fps)
            telemetry.addData("Overhead ms", camera.overheadTimeMs)
            telemetry.addData("Pipeline ms", camera.pipelineTimeMs)

            if (detections.size == 0) {
                numFramesWithoutDetection++

                if (numFramesWithoutDetection >= THRESHOLD_NUM_FRAMES_NO_DETECTION_BEFORE_LOW_DECIMATION) {
                    aprilTagDetectionPipeline.setDecimation(DECIMATION_LOW)
                }
            } else {
                numFramesWithoutDetection = 0

                if (detections[0].pose.z < THRESHOLD_HIGH_DECIMATION_RANGE_METERS) {
                    aprilTagDetectionPipeline.setDecimation(DECIMATION_HIGH)
                }

                for (detection in detections) {
                    lastIntID = detection.id
                    telemetry.addLine("\nDetected tag ID=${detection.id}")
                }
            }

            telemetry.update()
        }

        return lastIntID
    }

    open fun setPoleDetectorAsPipeline() {
        camera.setPipeline(poleDetector)
    }

    // Do not move more than 15 cm in any one direction!
    open val polePosition: DoubleArray
        get() {
            val (x, y, heading) = bot.drive.localizer.poseEstimate

            val pos: DoubleArray = poleDetector.getRepositionCoord(
                x.toCm(), y.toCm(), heading, frontSensor.distance,
            )

            // Do not move more than 15 cm in any one direction!
            if (pos[0] > 15 || pos[1] > 15) {
                telemetry.addData("Uh oh! Tried to return a pole position greater than 15 cm away!", max(pos[0], pos[1]))
                return doubleArrayOf(0.0, 0.0)
            }

            return pos
        }

    companion object {
        // Lens intrinsics
        // Units are in pixels
        private const val fx = 578.272
        private const val fy = 578.272
        private const val cx = 402.145
        private const val cy = 221.506

        // UNITS ARE METERS
        private const val tagsize = 0.166
        private const val DECIMATION_HIGH = 3f
        private const val DECIMATION_LOW = 2f
        private const val THRESHOLD_HIGH_DECIMATION_RANGE_METERS = 1.0f
        private const val THRESHOLD_NUM_FRAMES_NO_DETECTION_BEFORE_LOW_DECIMATION = 4

        const val MAX_CYCLES = 4

        @JvmStatic
        protected val liftOffsets = intArrayOf(
            AutoData.AUTO_INTAKE_LIFT_HEIGHT_1,
            AutoData.AUTO_INTAKE_LIFT_HEIGHT_2,
            AutoData.AUTO_INTAKE_LIFT_HEIGHT_3,
            AutoData.AUTO_INTAKE_LIFT_HEIGHT_4,
            AutoData.AUTO_INTAKE_LIFT_HEIGHT_5,
        )
    }
}
package org.firstinspires.ftc.teamcode.robots.taubot.subsystem;

import static org.firstinspires.ftc.teamcode.robots.reachRefactor.util.Constants.TRACK_WIDTH;
import static org.firstinspires.ftc.teamcode.robots.reachRefactor.util.Utils.wrapAngleRad;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.acmerobotics.roadrunner.drive.Drive;
import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.localization.Localizer;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Mahesh Natamai
 */

public abstract class DiffyDrive extends Drive {

    private Localizer localizer;

    public DiffyDrive(boolean simulated) {
        localizer = simulated ? new DiffyLocalizer(this, false) : new DiffyLocalizer(this, true);
    }

    static class DiffyLocalizer implements Localizer {

        private DiffyDrive drive;
        private Pose2d poseEstimate;
        private Pose2d poseVelocity;
        private List<Double> lastWheelPositions;
        private double lastExternalHeading;
        private boolean useExternalHeading;

        public DiffyLocalizer(DiffyDrive drive, boolean useExternalHeading) {
            this.drive = drive;
            this.useExternalHeading = useExternalHeading;
            lastWheelPositions = new ArrayList<>();
            poseEstimate = new Pose2d(0,0,0);
        }

        public void setHeading(double imuHeading){
            poseEstimate = new Pose2d(poseEstimate.getX(), poseEstimate.getY(), imuHeading);
        }

        @Override
        public void update() {
            List<Double> wheelPositions = drive.getWheelPositions();
            double externalHeading = useExternalHeading ? drive.getExternalHeading() : Double.NaN;
            if(!lastWheelPositions.isEmpty()) {
                List<Double> wheelDeltas = new ArrayList<>();
                for(int i = 0; i < wheelPositions.size(); i++) {
                    wheelDeltas.add(wheelPositions.get(i) - lastWheelPositions.get(i));
                }

                double displacement = (wheelDeltas.get(0) + wheelDeltas.get(1)) / 2;
                double heading = useExternalHeading ?

                        //Vance, I changed this!! 
                        //wrapAngleRad(poseEstimate.getHeading() + wrapAngleRad(externalHeading - lastExternalHeading))
                        externalHeading :
                        wrapAngleRad(poseEstimate.getHeading() + wrapAngleRad((-wheelDeltas.get(0) + wheelDeltas.get(1)) / TRACK_WIDTH));
                poseEstimate = new Pose2d(
                        poseEstimate.getX() + displacement * Math.cos(heading),
                        poseEstimate.getY() + displacement * Math.sin(heading),
                        heading
                );
            }

            List<Double> wheelVelocities = drive.getWheelVelocities();
            double externalHeadingVel = drive.getExternalHeadingVelocity();
            if(wheelVelocities != null) {
                    poseVelocity = useExternalHeading ?
                            new Pose2d((wheelVelocities.get(0) + wheelVelocities.get(1)) / 2, 0, externalHeadingVel) :
                            new Pose2d((wheelVelocities.get(0) + wheelVelocities.get(1)) / 2, 0, (-wheelVelocities.get(0) + wheelVelocities.get(1)) / TRACK_WIDTH);
            }

            lastWheelPositions = wheelPositions;
            lastExternalHeading = externalHeading;
        }

        @NonNull
        @Override
        public Pose2d getPoseEstimate() {
            return poseEstimate;
        }

        public void setPoseEstimate(Pose2d poseEstimate) {
            lastWheelPositions = new ArrayList<>();
            lastExternalHeading = Double.NaN;
            if(useExternalHeading)
                drive.setExternalHeading(poseEstimate.getHeading());
            this.poseEstimate = poseEstimate;


        }



        @Nullable
        @Override
        public Pose2d getPoseVelocity() {
            return poseVelocity;
        }


    }

    @NonNull
    @Override
    public Localizer getLocalizer() {
        return localizer;
    }

    @Override
    public void setLocalizer(@NonNull Localizer localizer) {
        this.localizer = localizer;
    }

    public abstract List<Double> getWheelPositions();

    public abstract List<Double> getWheelVelocities();

    public abstract void setMotorVelocities(double left, double right);
}
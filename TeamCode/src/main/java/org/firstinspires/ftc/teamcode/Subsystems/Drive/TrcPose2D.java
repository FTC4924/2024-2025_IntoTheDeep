package org.firstinspires.ftc.teamcode.Subsystems.Drive;

import androidx.annotation.NonNull;

import java.util.Locale;

/**
 * This class implements a 2D pose object that represents the positional state of an object.
 */
public class TrcPose2D
{

    public double x;
    public double y;
    public double angle;
    private double direction;
    private double movementAngle;

    /**
     * Constructor: Create an instance of the object.
     *
     * @param x specifies the x component of the position.
     * @param y specifies the y component of the position.
     * @param angle specifies the angle.
     */
    public TrcPose2D(double x, double y, double angle)
    {
        this.x = x;
        this.y = y;
        this.angle = angle;
    }   //TrcPose2D

    /**
     * Constructor: Create an instance of the object.
     *
     * @param x specifies the x coordinate of the position.
     * @param y specifies the y coordinate of the position.
     */
    public TrcPose2D(double x, double y)
    {
        this(x, y, 0.0);
    }   //TrcPose2D

    /**
     * Constructor: Create an instance of the object.
     */
    public TrcPose2D()
    {
        this(0.0, 0.0, 0.0);
    }   //TrcPose2D

    /**
     * This method returns the string representation of the pose.
     *
     * @return string representation of the pose.
     */
    @Override
    public String toString()
    {
        return String.format(Locale.US, "(x=%.1f,y=%.1f,angle=%.1f)", x, y, angle);
    }

    public boolean equals(TrcPose2D other) {
        return (other.x == this.x) && (other.y == this.y) && (other.angle == this.angle);
    }

    /**
     * This method creates and returns a copy of this pose.
     *
     * @return a copy of this pose.
     */
    @NonNull
    public TrcPose2D clone()
    {
        return new TrcPose2D(this.x, this.y, this.angle);
    }   //clone

    /**
     * This method sets this pose to be the same as the given pose.
     *
     * @param pose specifies the pose to make this pose equal to.
     */
    public void setAs(TrcPose2D pose)
    {
        this.x = pose.x;
        this.y = pose.y;
        this.angle = pose.angle;
    }   //setAs

    /**
     * This method translates this pose with the x and y offset in reference to the angle of the pose.
     *
     * @param xOffset specifies the x offset in reference to the angle of the pose.
     * @param yOffset specifies the y offset in reference to the angle of the pose.
     * @return translated pose.
     */
    public TrcPose2D translatePose(double xOffset, double yOffset)
    {
        final String funcName = "translatePose";
        TrcPose2D newPose = clone();
        double angleRadians = Math.toRadians(newPose.angle);
        double cosAngle = Math.cos(angleRadians);
        double sinAngle = Math.sin(angleRadians);

        newPose.x += xOffset*cosAngle + yOffset*sinAngle;
        newPose.y += -xOffset*sinAngle + yOffset*cosAngle;

        return newPose;
    }

}

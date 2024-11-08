package org.firstinspires.ftc.teamcode.teleops

import com.acmerobotics.dashboard.telemetry.TelemetryPacket
import com.acmerobotics.roadrunner.Action
import com.qualcomm.robotcore.hardware.DcMotorEx
import com.qualcomm.robotcore.hardware.HardwareMap


class CollectionArm(hardwareMap: HardwareMap) {
    private val motor: DcMotorEx = hardwareMap.get(DcMotorEx::class.java, "collectionArmMotor")

    inner class SpinUp : Action {
        private var initialized = false
        override fun run(packet: TelemetryPacket): Boolean {
            if (!initialized) {
                motor.power = 0.8
                initialized = true
            }
            val vel = motor.velocity
            packet.put("shooterVelocity", vel)
            return vel < 10000.0
        }
    }
    fun spinUp(): Action {
        return SpinUp()
    }
}
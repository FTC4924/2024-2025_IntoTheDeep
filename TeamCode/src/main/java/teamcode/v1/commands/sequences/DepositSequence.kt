package teamcode.v1.commands.sequences

import com.asiankoala.koawalib.command.commands.InstantCmd
import com.asiankoala.koawalib.command.commands.WaitCmd
import com.asiankoala.koawalib.command.group.ParallelGroup
import com.asiankoala.koawalib.command.group.SequentialGroup
import org.firstinspires.ftc.teamcode.koawalib.commands.subsystems.ClawCmds
import teamcode.v1.subsystems.Arm
import org.firstinspires.ftc.teamcode.koawalib.subsystems.Claw
import org.firstinspires.ftc.teamcode.koawalib.subsystems.Lift
import teamcode.v1.constants.ClawConstants

class DepositSequence(
    lift: Lift,
    arm : Arm,
    claw: Claw,
    armAngle : Double,
    LiftHeight : Double,
    clawPos : Double? = null
) : SequentialGroup(
    InstantCmd({
        if (clawPos != null) {
            ClawConstants.openPos = clawPos
        }
    }),
    ClawCmds.ClawCloseCmd(claw),
    InstantCmd({arm.setPos(armAngle)}, arm),
    WaitCmd(0.3),
    InstantCmd({lift.setPos(LiftHeight)}, lift),
)
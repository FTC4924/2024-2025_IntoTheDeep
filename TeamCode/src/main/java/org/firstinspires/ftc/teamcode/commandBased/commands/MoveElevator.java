package org.firstinspires.ftc.teamcode.commandBased.commands;

import com.arcrobotics.ftclib.command.CommandBase;
import com.arcrobotics.ftclib.command.SubsystemBase;

import org.firstinspires.ftc.teamcode.commandBased.Robot;
import org.firstinspires.ftc.teamcode.commandBased.subsystems.ElevatorSubsystem;

public class MoveElevator extends CommandBase {

    private double target;
    private ElevatorSubsystem m_elevatorSubsystem;

    public MoveElevator(ElevatorSubsystem elevatorSubsystem, double target) {
        m_elevatorSubsystem = elevatorSubsystem;
        addRequirements(m_elevatorSubsystem);
        this.target = target;
    }

    @Override
    public void initialize() {
        Robot.elevatorSubsystem.setHeight(target);
    }

    @Override
    public boolean isFinished() {
        return true;
    }
}
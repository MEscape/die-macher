/**
 * Provides command implementations for controlling the Dobot robotic arm.
 * <p>
 * This package contains command classes that interact with the Dobot using
 * serial communication. Each command represents a specific action or query
 * to be sent to the Dobot, such as moving to a position, clearing the queue,
 * or setting device parameters.
 * <p>
 * All commands implement the {@link com.die_macher.pick_and_place.dobot.command.DobotCommand}
 * interface, ensuring consistency and simplicity when executing commands
 * through a {@link com.die_macher.pick_and_place.dobot.config.DobotSerialConnector} instance.
 * <p>
 *
 * <h2>Command List:</h2>
 * <ul>
 *   <li>{@link com.die_macher.pick_and_place.dobot.command.impl.ClearQueueCommand} - Clears the execution queue.</li>
 *   <li>{@link com.die_macher.pick_and_place.dobot.command.impl.ExecuteQueueCommand} - Starts executing the queued commands.</li>
 *   <li>{@link com.die_macher.pick_and_place.dobot.command.impl.GetDeviceNameCommand} - Retrieves the device name.</li>
 *   <li>{@link com.die_macher.pick_and_place.dobot.command.impl.GetDeviceSNCommand} - Retrieves the device serial number.</li>
 *   <li>{@link com.die_macher.pick_and_place.dobot.command.impl.GoHomeCommand} - Sends the Dobot arm back to the home position.</li>
 *   <li>{@link com.die_macher.pick_and_place.dobot.command.impl.MoveToPositionCommand} - Moves the arm to a specified XYZ position.</li>
 *   <li>{@link com.die_macher.pick_and_place.dobot.command.impl.SetDefaultHomeCommand} - Sets the default home parameters.</li>
 *   <li>{@link com.die_macher.pick_and_place.dobot.command.impl.SetDeviceNameCommand} - Sets the name of the Dobot device.</li>
 *   <li>{@link com.die_macher.pick_and_place.dobot.command.impl.SetMovementConfigCommand} - Configures movement parameters.</li>
 *   <li>{@link com.die_macher.pick_and_place.dobot.command.impl.SetVacuumStateCommand} - Enables or disables the vacuum gripper.</li>
 *   <li>{@link com.die_macher.pick_and_place.dobot.command.impl.StopExecuteQueueCommand} - Stops the execution of the command queue.</li>
 * </ul>
 *
 * <h2>Author:</h2>
 * Marvin Eschenbach
 *
 * <h2>License:</h2>
 * (c) 2025 Marvin Eschenbach. All rights reserved.
 * This code is licensed for private use. Redistribution
 * or modification without explicit permission from the author is prohibited.
 */

@org.springframework.lang.NonNullApi
package com.die_macher.pick_and_place.dobot;
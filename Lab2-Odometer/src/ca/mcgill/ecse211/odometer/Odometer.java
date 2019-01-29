/**
 * This class is meant as a skeleton for the odometer class to be used.
 * 
 * @author Rodrigo Silva
 * @author Dirk Dubois
 * @author Derek Yu
 * @author Karim El-Baba
 * @author Michael Smith
 */

package ca.mcgill.ecse211.odometer;

import lejos.hardware.motor.EV3LargeRegulatedMotor;

public class Odometer extends OdometerData implements Runnable {

	private OdometerData odoData;
	private static Odometer odo = null; // Returned as singleton

	// Motors and related variables
	private int leftMotorTachoCount; // total degrees turned
	private int rightMotorTachoCount;
	private EV3LargeRegulatedMotor leftMotor;
	private EV3LargeRegulatedMotor rightMotor;
	private double Theta; // heading in radians

	private final double TRACK; // distance between left and right wheels
	private final double WHEEL_RAD; // wheel radius

	private static final long ODOMETER_PERIOD = 25; // odometer update period in ms
	private static final double toRad = Math.PI / 180.0; // degrees -> radians conversion
	private static final double toDeg = 180.0 / Math.PI; // radians -> degrees conversion

	/**
	 * This is the default constructor of this class. It initiates all motors and
	 * variables once.It cannot be accessed externally.
	 * 
	 * @param leftMotor
	 * @param rightMotor
	 * @throws OdometerExceptions
	 */
	private Odometer(EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor, final double TRACK,
			final double WHEEL_RAD) throws OdometerExceptions {
		odoData = OdometerData.getOdometerData(); // Allows access to x,y,z
													// manipulation methods
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;

		// Reset the values of x, y and z to 0
		odoData.setXYT(0, 0, 0);

		this.leftMotorTachoCount = 0;
		this.rightMotorTachoCount = 0;
		this.Theta = 0.0;

		this.TRACK = TRACK;
		this.WHEEL_RAD = WHEEL_RAD;

	}

	/**
	 * This method is meant to ensure only one instance of the odometer is used
	 * throughout the code.
	 * 
	 * @param leftMotor
	 * @param rightMotor
	 * @return new or existing Odometer Object
	 * @throws OdometerExceptions
	 */
	public synchronized static Odometer getOdometer(EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor,
			final double TRACK, final double WHEEL_RAD) throws OdometerExceptions {
		if (odo != null) { // Return existing object
			return odo;
		} else { // create object and return it
			odo = new Odometer(leftMotor, rightMotor, TRACK, WHEEL_RAD);
			return odo;
		}
	}

	/**
	 * This class is meant to return the existing Odometer Object. It is meant to be
	 * used only if an odometer object has been created
	 * 
	 * @return error if no previous odometer exists
	 */
	public synchronized static Odometer getOdometer() throws OdometerExceptions {

		if (odo == null) {
			throw new OdometerExceptions("No previous Odometer exits.");

		}
		return odo;
	}

	/**
	 * This method is where the logic for the odometer will run. Use the methods
	 * provided from the OdometerData class to implement the odometer.
	 */
	// run method (required for Thread)
	public void run() {
		long updateStart, updateEnd;

		while (true) {

			// get current time
			updateStart = System.currentTimeMillis();

			// get current TachoCount in degrees
			int leftMotorTachoCountNew = leftMotor.getTachoCount();
			int rightMotorTachoCountNew = rightMotor.getTachoCount();

			// degrees rotated for left and right
			int leftPhi = leftMotorTachoCountNew - leftMotorTachoCount;
			int rightPhi = rightMotorTachoCountNew - rightMotorTachoCount;

			// save counts for next iteration
			leftMotorTachoCount = leftMotorTachoCountNew;
			rightMotorTachoCount = rightMotorTachoCountNew;

			// convert angular displacement to linear displacement
			double leftDistance = WHEEL_RAD * leftPhi * toRad;
			double rightDistance = WHEEL_RAD * rightPhi * toRad;

			// change in displacement of vehicle
			double dDisp = 0.5 * (leftDistance + rightDistance);

			// change in heading in radians
			double radTheta = (leftDistance - rightDistance) / TRACK;

			// update heading
			Theta += radTheta;

			// compute x, y component of displacement
			// sin and cos uses radian
			double dX = Math.sin(Theta) * dDisp;
			double dY = Math.cos(Theta) * dDisp;

			// TODO Update odometer values with new calculated values
			// theta is in degrees
			odo.update(dX, dY, radTheta * toDeg);

			// this ensures that the odometer only runs once every period
			updateEnd = System.currentTimeMillis();
			if (updateEnd - updateStart < ODOMETER_PERIOD) {
				try {
					Thread.sleep(ODOMETER_PERIOD - (updateEnd - updateStart));
				} catch (InterruptedException e) {
					// there is nothing to be done
				}
			}
		}
	}

}

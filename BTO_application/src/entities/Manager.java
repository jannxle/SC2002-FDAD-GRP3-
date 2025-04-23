package entities;

import enums.Role;

/**
 * Represents an HDB Manager user in the BTO Management System.
 * A Manager extends the base User class.
 * This class primarily serves to instantiate a User.
 * Manager-specific logic is handled in the HDBManagerUI and relevant control classes.
 */
public class Manager extends User{
	/**
     * Constructs a new Manager object.
     * Initializes the manager with basic user details and explicitly sets the role to MANAGER
     *
     * @param name      The name of the manager.
     * @param NRIC      The NRIC of the manager.
     * @param age       The age of the manager.
     * @param isMarried The marital status of the manager (true if married, false if single).
     * @param password  The login password for the manager.
     */
	public Manager(String name, String NRIC, int age, boolean isMarried,String password) {
		super(name, NRIC, age, isMarried, password, Role.MANAGER);
	}
}

package entities;

import enums.Role;

/**
 * Represents an abstract base class for all users within the BTO Management System.
 * It defines common attributes and functionalities shared by all user types,
 * such as name, NRIC, age, marital status, password, and role.
 * Applicant, Officer, Manager extend this class.
 */
public abstract class User {
	private String name;
	private String NRIC;
	private int age;
	private boolean isMarried;
	private String password;
	private Role role;

	/**
	 * Constructs a new User object.
	 *
	 * @param name      The name of the user.
	 * @param NRIC      The NRIC of the user.
	 * @param age       The age of the user.
	 * @param married   The marital status of the user (true for married, false for single).
	 * @param password  The login password for the user.
	 * @param role      The Role of the user in the system.
	 */
	public User(String name, String NRIC, int age, boolean married, String password, Role role) {
		this.name = name;
		this.NRIC = NRIC;
		this.age = age;
		this.isMarried = married;
		this.role = role;
		this.password = password;
	}

	/**
	 * Gets the name of the user.
	 * @return The user's name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets the NRIC of the user.
	 * @return The user's NRIC.
	 */
	public String getNRIC() {
		return NRIC;
	}

	/**
	 * Gets the age of the user.
	 * @return The user's age.
	 */
	public int getAge() {
		return age;
	}

	/**
	 * Gets the marital status of the user.
	 * @return true if the user is married, false otherwise.
	 */
	public boolean isMarried() {
		return isMarried;
	}

	/**
     * Gets the user's password.
     * @return The user's password.
     */
    public String getPassword() {
        return password;
    }

	/**
	 * Gets the role of the user within the system.
	 * @return The user's Role.
	 */
	public Role getRole() {
		return role;
	}

	/**
	 * Sets the user's password.
	 * @param newPass The new password string.
	 */
	public void setPassword(String newPass) {
		this.password = newPass;
	}

	/**
	 * Changes the user's password if the new password is valid (not null or empty).
	 *
	 * @param newPass The new password to set.
	 * @return true if the password was successfully changed, false otherwise.
	 */
	public boolean changePass(String newPass) {
		if (newPass != null && !newPass.isEmpty()) {
			setPassword(newPass);
			return true;
		}
		System.err.println("Password change failed: New password cannot be empty.");
		return false;
	}

	/**
	 * Verifies if the provided password matches the user's stored password.
	 *
	 * @param pass The password to verify.
	 * @return true if the provided password matches the stored password, false otherwise.
	 */
	public boolean verifyPassword(String pass) {
		return this.password != null && this.password.equals(pass);
	}
}

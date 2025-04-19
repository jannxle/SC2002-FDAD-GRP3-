package entities;

import enums.Role;

public abstract class User {
	private String name;
	private String NRIC;
	private int age;
	private boolean isMarried;
	private String password;

	private Role role;

	public User(String name, String NRIC, int age, boolean married, String password, Role role) {
		this.name = name;
		this.NRIC = NRIC;
		this.age = age;
		this.isMarried = married;
		this.role = role;
		this.password = password;
	}

	public String getName() {
		return name;
	}
	public String getNRIC() {
		return NRIC;
	}
	public int getAge() {
		return age;
	}
	public boolean isMarried() {
		return isMarried;
	}
    public String getPassword() {
        return password;
    }
	public Role getRole() {
		return role;
	}

	public void setPassword(String newPass) {
		this.password = newPass;
	}

	public void logout() {
		System.out.println("Logging out user " + getName() + "...");
	}

	public boolean changePass(String newPass) {
		if (newPass != null && !newPass.isEmpty()) {
			setPassword(newPass);
			return true;
		}
		System.err.println("Password change failed: New password cannot be empty.");
		return false;
	}

	public boolean verifyPassword(String pass) {
		return this.password != null && this.password.equals(pass);
	}
}

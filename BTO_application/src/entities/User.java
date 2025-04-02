package entities;

import enums.Role;

public class User {
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
	
	//getter methods
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
    // Add the getPassword() method so that subclasses can inherit it.
    public String getPassword() {
        return password;
    }
	public Role getRole() {
		return role;
	}
	
	//setter methods
	public void setPassword(String newPass) {
		this.password = newPass;
	}
	
	
	//actions
	public void logout() {
		System.out.println("Logging out of System...");
	}
	
	public boolean changePass(String newPass) {
		if (newPass != null && !newPass.isEmpty()) {
			setPassword(newPass);
			return true;
		}
		return false;
	}
	
	public boolean verifyPassword(String pass) {
		return password.equals(pass);
		
	}
}

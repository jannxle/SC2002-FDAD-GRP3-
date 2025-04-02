package entities;

import enums.Role;

public class Manager extends User{
	public Manager(String name, String NRIC, int age, boolean isMarried,String password) {
		super(name, NRIC, age, isMarried, password, Role.MANAGER);
	}
}

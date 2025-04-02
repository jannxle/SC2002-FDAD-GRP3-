package boundary;

import entities.Manager;
import java.util.Scanner;

public class HDBManagerUI {
	private Manager manager;
	private Scanner scanner = new Scanner(System.in);
	
	public HDBManagerUI(Manager manager) {
		this.manager = manager;
	}
	
	public void showMenu() {
		System.out.println("====BTO Management Main Page====");
		System.out.println("Hello Manager!");
	}
}

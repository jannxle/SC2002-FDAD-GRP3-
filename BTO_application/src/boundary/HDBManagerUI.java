package boundary;

import control.ProjectManager;
import control.EnquiryManager;
import control.ApplicationManager;
import control.UserManager;
import entities.Manager;
import entities.Project;
import entities.Enquiry;
import java.util.List;
import java.util.Scanner;

import auth.LoginManager;

public class HDBManagerUI {
	private Manager manager;
    private ProjectManager projectManager;
    private EnquiryManager enquiryManager;
    private ApplicationManager applicationManager;
    private UserManager userManager;
    private LoginManager loginManager;
	private Scanner scanner = new Scanner(System.in);
	
	public HDBManagerUI(Manager manager, ProjectManager projectManager, EnquiryManager enquiryManager,
            ApplicationManager applicationManager, UserManager userManager, LoginManager loginManager) {
		this.manager = manager;
		this.projectManager = projectManager;
        this.enquiryManager = enquiryManager;
        this.applicationManager = applicationManager;
        this.userManager = userManager;
        this.loginManager = loginManager;
	}
	
	public void showMenu() {
		boolean logout = false;
		
		while (!logout) {
			System.out.println("========HDB Manager Dashboard========");
			System.out.println("1. View all Projects");
			System.out.println("2. View My current Projects"); //filter based on manager column and view list of projects they have created 
			System.out.println("3. HDB Officer Registration Site"); //view pending and approved HDB Officer Registration
			System.out.println("4. All Applications Site"); //approve or reject applicant BTO application + approve/reject withdrawal
			System.out.println("5. Create BTO Project Listing");
			System.out.println("6. Generate Report ");
			System.out.println("7. View Enquiries");
			System.out.println("8. Reply to Enquries under your Project");
			System.out.println("9. Change Password");
			System.out.println("10. Log out");
			System.out.println("-------------------------------------");
			
			int choice = 0;
			boolean validInput = false;
	        while (!validInput) {
	            System.out.print("Enter your choice: ");
	            if (scanner.hasNextInt()) {
	                choice = scanner.nextInt();
	                scanner.nextLine(); // consume the rest of the line
	                validInput = true;
	            } else {
	                System.out.println("Invalid input. Please enter a valid number.");
	                scanner.nextLine(); // discard the invalid input
	            }
	        }
	        
	        System.out.println("----------------------------------------");
			
			switch(choice) {
			case 1: 
				viewAllProjects();
				break;
				
			case 2:
				viewMyProjects();
				//shows list of Projects under Manager's name and allows manager to choose if he wqants to delete and edit 
				//can also toggle visibility for the projects
				break;
				
			case 3:
				officerRegistration();
				break;
			
			case 4:
				//applicationSite();
				break;
				
			case 5:
				//createBTOListing();
				break;
				
			case 6:
				//generateReport();
				break;
			
			case 7:
				//viewEnquiries();
				break;
				
			case 8:
				//replyEnquiries();
				break;
				
			case 9:
				//changePassword();
				break;
				
			case 10:
				logout = true;
				System.out.println();
				System.out.println("Logging out...");
				System.out.println();
				loginManager.login();
				break;
			}
		}
	}
	private void viewAllProjects() {
		System.out.println("========All Projects========");
		List <Project> projects = projectManager.getProjects();
		if (projects.isEmpty()) {
			System.out.println("There are no available projects");
		}
		else {
			for (Project p :projects) {
				System.out.println(p);
				System.out.println("----------------------------");
			}
		}
	}
	
	private void viewMyProjects() {
		
		System.out.println("=== My Current Project ===");
	    Project currentProject = projectManager.getCurrentProjectForManager(manager.getName());
	    if (currentProject == null) {
	        System.out.println("You are not currently handling any project within the active application period.");
	    } else {
	        System.out.println("Currently Handling Project:");
	        // Display the project's details. This calls the project's toString() method.
	        System.out.println(currentProject);
	    }
	    
	    
		System.out.println("========My Projects========");
		List <Project> projects = projectManager.getProjects();
		
		boolean found = false;
		for (Project p : projects) {
			if(p.getManager().equalsIgnoreCase(manager.getName())) {
				found = true;
				System.out.println(p);
				System.out.println("----------------------------");
				System.out.println("Option for project " + p.getName());
				System.out.println("1. Edit Listing");
				System.out.println("2. Delete Listing");
				System.out.println("3. Toggle Visibility");
				System.out.println("0. Exit");
				System.out.println("----------------------------");
				System.out.print("Enter option:");
				int option = scanner.nextInt();
				System.out.println("----------------------------");
				
				switch(option) {
				case 1:
					editProjectListing(p);
					break;
				case 2:
					System.out.print("Are you sure you want to delete this project? (Y/N)");
					String response = scanner.next();
			        if (response.equalsIgnoreCase("Y")) {
			        	if(projectManager.deleteProject(p.getName())) {
			        		System.out.println("Project deleted.");
			        		System.out.println();
			        	}else {
			        		System.out.println("Deletion failed.");
			        		System.out.println();
			        	}
			        } else {
			        	System.out.println("Deletion cancelled.");
			        	System.out.println();
			        }
			        break;
				case 3:
					boolean newVisibility = !p.isVisibility();
					projectManager.toggleProjectVisibility(p.getName(), newVisibility);
					System.out.println("Visibility set to " + (newVisibility ? "On" : "Off"));
					break;
				
				}
			}
			if (!found) {
				System.out.println("You have not created any projects.");
			}
		}
	}
    private void editProjectListing(Project p) {
        System.out.println("=== Edit Project Listing: " + p.getName() + " ===");
        System.out.print("Enter new Neighbourhood (current: " + p.getNeighbourhood() + "): ");
        String newNeighbourhood = scanner.nextLine();
        p.setNeighbourhood(newNeighbourhood);
        // You can extend this to allow editing additional fields as needed.
        System.out.println("Project listing updated successfully.");
    }
    
    
	
	private void officerRegistration() {
		System.out.println("====Manage HDB Officer Registration====");
		System.out.println("1. View Pending HDB Officer Registration");
		System.out.println("2. View Approved HDB Officer Registration");
		System.out.println("----------------------------------------");
		System.out.println("Your selection: ");
		int selection = scanner.nextInt();
		
		switch (selection) {
		case 1: 
		}
	}
}

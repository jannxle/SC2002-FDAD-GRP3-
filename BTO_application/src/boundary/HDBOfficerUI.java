package boundary;

import java.util.List;
import java.util.Scanner;

import auth.LoginManager;
import control.ApplicantManager;
import control.ApplicationManager;
import control.EnquiryManager;
import control.OfficerRegistrationManager;
import control.ProjectManager;
import control.UserManager;
import entities.Applicant;
import entities.Enquiry;
import entities.Officer;
import entities.Project;
import enums.ApplicationStatus;
import enums.OfficerRegistrationStatus;

public class HDBOfficerUI extends ApplicantUI {
	private Officer officer;
	private Scanner scanner = new Scanner(System.in);
	private OfficerRegistrationManager officerRegistrationManager;
	
	public HDBOfficerUI(Officer officer, ApplicantManager applicantManager, UserManager userManager, 
            LoginManager loginManager, EnquiryManager enquiryManager, 
            ApplicationManager applicationManager, ProjectManager projectManager, OfficerRegistrationManager officerRegistrationManager) {
        // Call the super constructor, treating the officer as an applicant to reuse those features
        super(officer, applicantManager, userManager, loginManager, enquiryManager, applicationManager, projectManager);
        this.officer = officer;
        this.officerRegistrationManager = officerRegistrationManager;
	}
	
	public void showMenu() {
		boolean logout = false;
		
		while (!logout) {
			System.out.println("===========HDB Officer Dashboard===========");
			System.out.println("1. Change Password");
			System.out.println("2. View Available List of Projects");
			System.out.println("3. Apply for a project");
			System.out.println("4. View Application Status");
			System.out.println("5. Request withdrawal from Application");
			System.out.println("6. Submit an Enquiry");
			System.out.println("7. View, Edit and Delete your enquires");
			//Officer-specific options
			System.out.println("--------------Officer Features--------------");
			System.out.println("8. Register to join a Project as an HDBOfficer");
			System.out.println("9. View status of HDB Officer Registration ");
			System.out.println("10. View My Projects Details"); //regardless of visibility
			System.out.println("11. View and reply to Enquiries for your Projects");
			System.out.println("12. Generate receipt for applicants");
			System.out.println("13. View Profile");
			System.out.println("14. Log out");
			System.out.println("----------------------------------------------");

			
	        // Validate integer input
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
			
            switch (choice) {
            case 1:
            	changePassword();
            	break;
            case 2: 
            	viewAvailableProjects(); 
            	break;
            case 3: 
            	applyForProject(); 
            	break;
            case 4: 
            	viewApplicationStatus(); 
            	break;
            case 5: 
            	requestWithdrawal(); 
            	break;
            case 6: 
            	submitEnquiry(); 
            	break;
            case 7: 
            	editEnquiry(); 
            	break;
            case 8: 
            	registerForProject(); 
            	break;
            case 9: 
            	viewOfficerRegistrationStatus(); 
            	break;
            case 10: 
            	viewProjectDetails(); 
            	break;
            case 11: 
            	viewAndReplyEnquiries(); 
            	enquiryManager.saveEnquiries("data/enquiries.csv");
            	break;
            case 12: 
            	generateReceipt(); 
            	break;
            case 13: 
            	viewOfficerProfile(); 
            	break;
            case 14:
                logout = true;
                System.out.println("Logging out...");
                System.out.println();
                loginManager.login();
                break;
            default:
                System.out.println("Invalid option. Please try again.");
                System.out.println();
        }
		}
	}
    // Officer-specific methods:
    private void registerForProject() {
    	List<Project> allProjects = projectManager.getProjects();
    	if (allProjects.isEmpty()) {
    		System.out.println("There are no project available.");
    		System.out.println();
    		return;
    	}
    	System.out.println("Available projects to join: ");
    	for (int i = 0; i < allProjects.size(); i++) {
    		System.out.println((i + 1) + ". " + allProjects.get(i).getName());
    	}
    	
    	System.out.println("----------------------------------------------");
    	System.out.print("Select a project to join (or 0 to return to main page): ");
    	int selection = scanner.nextInt();
    	scanner.nextLine();
    	
    	if (selection == 0) {
    		System.out.println("Registration cancelled. Returning to main page...");
    		System.out.println();
    		return;
    	}
    	if (selection < 1 || selection > allProjects.size()) {
    		System.out.println("Invalid input. Returning to main page...");
    		System.out.println();
    		return;
    	}
    	
    	Project selectedProject = allProjects.get(selection -1);
    	officerRegistrationManager.registerOfficerForProject(officer, selectedProject);
    	System.out.println();
    }
    
	
    private void viewOfficerRegistrationStatus() {
        Project regProject = officer.getRegisteredProject();
        if (regProject == null) {
            System.out.println("Access Denide! You are not registered for any project as an HDB Officer.");
        } else {
            System.out.println("You are registered for project: " + regProject.getName());
            System.out.println("Registration Status: " + officer.getRegistrationStatus());
            System.out.println();
        }
    }
    
    private void viewProjectDetails() {
        Project project = officer.getRegisteredProject(); // Use registered project, not applicant's applied project.
        if (project == null || officer.getRegistrationStatus() != OfficerRegistrationStatus.SUCCESSFUL) {
            System.out.println("No project assigned to you.");
        } else {
            System.out.println("===========Project Details===========");
            System.out.println(project);
        }
    }
    
    private void viewAndReplyEnquiries() {
        Project project = officer.getRegisteredProject();
        if (project == null) {
            System.out.println("No project assigned.");
            return;
        }
        
        List<Enquiry> enquiries = enquiryManager.getEnquiriesByProject(project.getName());
        if (enquiries.isEmpty()) {
            System.out.println("No enquiries for your project.");
            return;
        }
        
        System.out.println("Enquiries for Project: " + project.getName());
        System.out.println("----------------------------------------------");
        for (int i = 0; i < enquiries.size(); i++) {
            System.out.println((i + 1) + ". " + enquiries.get(i).toString());
            System.out.println("------------------------------------------------");
        }
        
        System.out.print("Select an enquiry number to reply (Enter 0 to cancel): ");
        int selection = scanner.nextInt();
        scanner.nextLine(); // consume newline
        
        if (selection <= 0 || selection > enquiries.size()) {
            System.out.println("Reply cancelled.");
            return;
        }
        Enquiry selectedEnquiry = enquiries.get(selection - 1);
        System.out.print("Enter your reply: ");
        String reply = scanner.nextLine();
		enquiryManager.replyToEnquiry(selectedEnquiry, reply, officer.getName());
		selectedEnquiry.setReply(reply, officer.getName());
        System.out.println("Reply submitted successfully.");
        System.out.println();
    }
    
    private void generateReceipt() {
        List<Applicant> applicants = userManager.getApplicants();
        boolean found = false;
        for (Applicant a : applicants) {
            if (a.getStatus() == ApplicationStatus.BOOKED) {
                found = true;
                System.out.println("---- Receipt for " + a.getName() + " ----");
                System.out.println("Applicant Name: " + a.getName());
                System.out.println("NRIC: " + a.getNRIC());
                System.out.println("Age: " + a.getAge());
                System.out.println("Marital Status: " + (a.isMarried() ? "Married" : "Single"));
                System.out.println("Flat Type Booked: " + a.getRoomChosen());
                if (a.getAppliedProject() != null) {
                    System.out.println("Project Name: " + a.getAppliedProject().getName());
                }
                System.out.println("---- End Receipt ----");
                System.out.println();
            }
        }
        if (!found) {
            System.out.println("No applicant has booked a flat.");
        }    	
    	}

    
	private void viewOfficerProfile() {
		//enter password to view profile as a security feature
    	System.out.print("Please enter your password to view your profile details: ");
    	String autpassword = scanner.next();
    	System.out.println();
    	if (autpassword.equals(applicant.getPassword())){ 
	        System.out.println("==== Officer Profile ====");
	        System.out.println("Name: " + officer.getName());
	        System.out.println("NRIC: " + officer.getNRIC());
	        System.out.println("Age: " + officer.getAge());
	        System.out.println("Marital Status: " + (officer.isMarried() ? "Married" : "Single"));
	        System.out.println("Application Status for Projects as Applicant: " + officer.getAppliedProject());
	        Project regProject = officer.getRegisteredProject();
	        if (regProject != null) {
	            System.out.println("Registered for Project as Officer: " + regProject.getName());
	            System.out.println("Registration Status for other Projects: " + officer.getRegistrationStatus());
	        } else {
	            System.out.println("Not registered for any project as an HDB Officer.");
	        }
        System.out.println();
    	}
    }
}



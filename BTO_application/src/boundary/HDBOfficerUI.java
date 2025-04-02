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

public class HDBOfficerUI extends ApplicantUI {
	private Officer officer;
	private ProjectManager projectManager;
	private OfficerRegistrationManager officerRegistrationManager;
	private Scanner scanner = new Scanner(System.in);
	
	public HDBOfficerUI(Officer officer, ApplicantManager applicantManager, UserManager userManager, 
            LoginManager loginManager, EnquiryManager enquiryManager, 
            ApplicationManager applicationManager, ProjectManager projectManager, OfficerRegistrationManager officerRegistrationManager) {
        // Call the super constructor, treating the officer as an applicant to reuse those features
        super(officer, applicantManager, userManager, loginManager, enquiryManager, applicationManager);
        this.officer = officer;
        this.projectManager = projectManager;
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
			System.out.println("8. Register to join a Project");
			System.out.println("9. View status of HDB Officer Registration ");
			System.out.println("10. View My Projects Details"); //regardless of visibility
			System.out.println("11. View and reply to Enquiries");
			System.out.println("12. Generate receipt for applicants");
			System.out.println("13. View Profile");
			System.out.println("14. Log out");
			System.out.println("----------------------------------------------");
			System.out.print("Enter your choice: ");
			int choice = scanner.nextInt();
			System.out.println();
			
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
            	//registerForProject(); 
            	break;
            case 9: 
            	viewOfficerRegistrationStatus(); 
            	break;
            case 10: 
            	//viewProjectDetails(); 
            	break;
            case 11: 
            	viewAndReplyEnquiries(); 
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
    
	
    private void viewOfficerRegistrationStatus() {
        Project regProject = officer.getRegisteredProject();
        if (regProject == null) {
            System.out.println("You are not registered for any project as an HDB Officer.");
        } else {
            System.out.println("You are registered for project: " + regProject.getName());
            System.out.println("Registration Status: " + officer.getRegistrationStatus());
        }
    }
    
    private void viewAndReplyEnquiries() {
        Project project = officer.getAppliedProject();
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
        System.out.println("-------------------------------------------------");
        for (int i = 0; i < enquiries.size(); i++) {
            System.out.println((i + 1) + ". " + enquiries.get(i).toString());
            System.out.println("-------------------------------------------------");
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
		enquiryManager.replyToEnquiry(selectedEnquiry, reply);
        System.out.println("Reply submitted successfully.");
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
        System.out.println("==== Officer Profile ====");
        System.out.println("Name: " + officer.getName());
        System.out.println("NRIC: " + officer.getNRIC());
        System.out.println("Age: " + officer.getAge());
        System.out.println("Marital Status: " + (officer.isMarried() ? "Married" : "Single"));
        Project regProject = officer.getAppliedProject();
        if (regProject != null) {
            System.out.println("Registered for Project: " + regProject.getName());
            System.out.println("Registration Status: " + officer.getRegistrationStatus());
        } else {
            System.out.println("Not registered for any project as an HDB Officer.");
        }
        System.out.println();
    }
}



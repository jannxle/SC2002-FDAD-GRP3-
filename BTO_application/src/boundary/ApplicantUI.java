package boundary;

import auth.LoginManager;
import control.ApplicantManager;
import control.ApplicationManager;
import control.EnquiryManager;
import control.ProjectManager;
import control.UserManager;
import entities.Applicant;
import entities.Enquiry;
import entities.Project;
import entities.Room;
import enums.RoomType;
import java.util.List;
import java.util.Scanner;

public class ApplicantUI {
	protected Applicant applicant;
	private ApplicantManager applicantManager;
	private ApplicationManager applicationManager;
	protected UserManager userManager;
	protected LoginManager loginManager;
	protected EnquiryManager enquiryManager;
	protected ProjectManager projectManager;
	private Scanner scanner = new Scanner(System.in);
	
	
	public ApplicantUI(Applicant applicant, ApplicantManager applicantManager, UserManager userManager, LoginManager loginManager, EnquiryManager enquiryManager, ApplicationManager applicationManager, ProjectManager projectManager) {
		this.applicant = applicant;
		this.applicantManager = applicantManager;
		this.userManager = userManager;
		this.loginManager = loginManager;
		this.enquiryManager = enquiryManager;
		this.applicationManager = applicationManager;
		this.projectManager = projectManager;
	}


	public void showMenu() {
		boolean logout = false;
		
		while(!logout) {
			System.out.println("========BTO Management Main Page========");
			System.out.println("1. Change Password");
			System.out.println("2. View Available List of Projects");
			System.out.println("3. Apply for a project");
			System.out.println("4. View Application Status");
			System.out.println("5. Request withdrawal from Application");
			System.out.println("6. Submit an Enquiry");
			System.out.println("7. View, Edit and Delete your enquires");
			System.out.println("8. View Profile");
			System.out.println("9. Log out");
			System.out.println("----------------------------------------");


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
		
			switch(choice) {
			case 1: 
				changePassword();
				System.out.println();
				break;
			case 2:
				//can only view list of projects that are open to their user group (single/married)
				viewAvailableProjects();
				break;
			case 3:
				applyForProject();
				applicationManager.saveApplications("data/Applications.csv", userManager.getApplicants());
				break;
			case 4:
				viewApplicationStatus();
				break;
			case 5:
				requestWithdrawal();
				applicationManager.saveApplications("data/Applications.csv", userManager.getApplicants());
				break;
			case 6:
				System.out.println();
				submitEnquiry();
				enquiryManager.saveEnquiries("data/enquiries.csv");

				break;
			case 7:
				System.out.println();
				editEnquiry();
				enquiryManager.saveEnquiries("data/enquiries.csv");
				break;
			case 8:
				//implement profile
				viewApplicantProfile();
				break;
			case 9:
				logout=true;
				System.out.println();
				System.out.println("Logging out...");
				System.out.println();
				//enquiryManager.saveEnquiries("data/enquiries.csv");
				//applicationManager.saveApplications("data/Applications.csv", userManager.getApplicants());
				loginManager.login();
				break;
			}
	}
	}
    protected void changePassword() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter your new password:");
        String newPassword = scanner.nextLine();
        
        // (Optional: Add validations for password strength here)
        
        boolean success = userManager.changeApplicantPassword(applicant.getNRIC(), newPassword);
        if (success) {
            System.out.println("Password changed successfully.");
        } else {
            System.out.println("Error: Password change failed. Please try again.");
        }
    }
    
    protected void viewAvailableProjects() {
    	List<Project> availableProjects = applicantManager.getAvaliableProjects(applicant); //each applicant can view based on isMarried and age
    	if (availableProjects.isEmpty()) {
    		System.out.println("There are not available projects under your egibility.");
    	} else {
    		System.out.println("These are the following available projects: ");
    		for (int i=0; i<availableProjects.size();i++) {
    			System.out.println((i+1) + ". " + availableProjects.get(i).getName());
    		}
    	}
    	System.out.println();
    }
    protected void applyForProject() {
    	//first check if user has applied
		//Single, and above 35 can only apply 2 room
		//married and 21 and above can apply for any flat types
    	List<Project> availableProjects = applicantManager.getAvaliableProjects(applicant);
    	if (availableProjects.isEmpty()) {
    		System.out.println("There are no available projects you can apply for.");
    		return;
    	}
    	System.out.println("These are the projects you can apply for:");
    	for (int i=0; i<availableProjects.size();i++) {
			System.out.println((i+1) + ". " + availableProjects.get(i).getName());
		}
    	System.out.println("-----------------------------------------------------------------------------");
    	System.out.println("Which project would you like to apply for? (press 0 to exit application page)");
    	int selection = scanner.nextInt();
    	if (selection == 0) {
    		System.out.println("Exiting application page. Redirecting to Main Page...");
    		System.out.println();
    		return;
    	}
    	if (selection <1 || selection > availableProjects.size()) {
    		System.out.println("Invalid selection");
    		System.out.println();
    		return;
    	}
    	
    	Project selectedProject = availableProjects.get(selection -1);
    	System.out.println("Available room types for project " + selectedProject.getName() + ": ");
    	
    	//checking eligibility
    	if (!applicant.isMarried() && applicant.getAge()>= 35) {
    		Room twoRoom = selectedProject.getRooms().stream()
    				.filter(r-> r.getRoomType() == RoomType.TwoRoom)
    				.findFirst().orElse(null);
            if (twoRoom != null) {
                System.out.println("1. 2-Room (Available units: " + twoRoom.getAvailableRooms() 
                        + ", Price: " + twoRoom.getPrice() + ")");
                System.out.print("Enter 1 to apply for 2-Room: ");
                int flatChoice = scanner.nextInt();
                scanner.nextLine(); // consume newline
                if (flatChoice == 1) {
                    if (applicationManager.apply(applicant, selectedProject, RoomType.TwoRoom)) {
                        System.out.println("Application submitted successfully!");
                    } else {
                        System.out.println("Application submission failed.");
                    }
                } else {
                    System.out.println("Invalid selection.");
                }
            } else {
                System.out.println("No 2-Room flats available in this project.");
            }
    	}else if (applicant.isMarried() && applicant.getAge()>=21) {
    		Room twoRoom = selectedProject.getRooms().stream()
                    .filter(r -> r.getRoomType() == RoomType.TwoRoom)
                    .findFirst().orElse(null);
            Room threeRoom = selectedProject.getRooms().stream()
                    .filter(r -> r.getRoomType() == RoomType.ThreeRoom)
                    .findFirst().orElse(null);
            if (twoRoom != null) {
                System.out.println("1. 2-Room (Available units: " + twoRoom.getAvailableRooms() 
                        + ", Selling Price: " + twoRoom.getPrice() + ")");
            }
            if (threeRoom != null) {
                System.out.println("2. 3-Room (Available units: " + threeRoom.getAvailableRooms() 
                        + ", Selling Price: " + threeRoom.getPrice() + ")");
            }
            
            System.out.print("Enter your choice (1 or 2): ");
            int flatChoice = scanner.nextInt();
            scanner.nextLine(); // consume newline
            
            RoomType chosenType = null;
            if (flatChoice == 1) {
                chosenType = RoomType.TwoRoom;
            } else if (flatChoice == 2) {
                chosenType = RoomType.ThreeRoom;
            } else {
                System.out.println("Invalid selection.");
                return;
            }
            
            if (applicationManager.apply(applicant, selectedProject, chosenType)) {
                System.out.println("Application submitted successfully!");
            } else {
                System.out.println("Application submission failed.");
            }
        } else {
            System.out.println("You are not eligible to apply for any flats in this project.");
    	}
    	
    }
    protected void viewApplicationStatus() {
        // TODO: Retrieve and display the applicant's application details and status (Pending, Successful, Unsuccessful, Booked).  
    	if (applicant.getStatus() == null) {
    		System.out.println("You have not submitted an application yet.");
    		System.out.println();
    	}else {
    		System.out.println("Your application status: " + applicant.getStatus());
    		System.out.println();
    	}
    }
    protected void requestWithdrawal(){
    	//can request before/after flat booking 
        if (applicant.getAppliedProject() == null) {
            System.out.println("No application to withdraw from.");
            return;
        }
        System.out.print("Are you sure you want to withdraw your application? (Y/N): ");
        String response = scanner.next();
        if (response.equalsIgnoreCase("Y")) {
        	System.out.println();
            if (applicationManager.withdraw(applicant)) {
                System.out.println("Requested for Withdrawal. HDB Manager will be looking into your request shortly.");
                System.out.println();
            } else {
                System.out.println("Request Withdrawal failed.");
                System.out.println();
            }
        } else {
        	System.out.println();
            System.out.println("Withdrawal cancelled.");
            System.out.println();
        }
    }    	
    
    protected void submitEnquiry() {   
        System.out.println("==========Submit an Enquiry==========");
        //get list of projects available
        List<Project> availableProjects = projectManager.getProjects();
        
        if (availableProjects.isEmpty()) {
        	System.out.println("There are no projects available at the moment.");
        	System.out.println();
        	return;
        }
        
        System.out.println("Select the project you want to submit and enquiry: (Press 0 to exit)");
        for (int i = 0; i < availableProjects.size(); i++) {
            System.out.println((i + 1) + ". " + availableProjects.get(i).getName());
        }
        
        System.out.print("Project choice: ");
        int projectChoice = scanner.nextInt();
        
        if (projectChoice == 0) {
            System.out.println("Enquiry submission cancelled.");
            System.out.println();
            return;
        }
        
        if (projectChoice < 1 || projectChoice > availableProjects.size()) {
            System.out.println("Invalid selection.");
            System.out.println();
            return;
        }
        
        Project chosenProject = availableProjects.get(projectChoice - 1);
            	
    	System.out.println("Enter your enquiry: ");
    	String message = scanner.next();
    	
        Enquiry e = new Enquiry(applicant.getNRIC(), applicant.getName(), chosenProject.getName(), message);
        enquiryManager.submitEnquiry(e);
        System.out.println("Enquiry submitted successfully!");
        System.out.println();
    }
    
    protected void editEnquiry() {
    	// TODO: Display a list of enquiries, and then prompt for edit or deletion.
    	List<Enquiry> myEnquiries = enquiryManager.getEnquiriesByApplicant(applicant.getNRIC());
    	
    	if(myEnquiries.isEmpty()) {
    		System.out.println("You have no enquiries.");
    		System.out.println("----------------------------------------");
    		return;
    	}
        System.out.println("Your Enquiries:");
        System.out.println("----------------------------------------");
        for (int i = 0; i < myEnquiries.size(); i++) {
            // Display using the applicant-friendly view (hides NRIC)
            System.out.println((i + 1) + ". " + myEnquiries.get(i).toStringForApplicant());
            System.out.println("----------------------------------------");
        }
        
    	System.out.print("Choose an enquiry to edit/delete (press 0 to exit): ");
        int index = scanner.nextInt();
        scanner.nextLine();
        
        if (index == 0) {
        	System.out.println("Request Cancelled. Redirecting to Main Page...");
        	System.out.println();
        	return;
        }
        
        if (index < 0 || index > myEnquiries.size()) {
            System.out.println("Invalid option. Redirecting to Main Page...");
            System.out.println();
            return;
        }
        
        Enquiry selected = myEnquiries.get(index - 1);
        
        System.out.println("Would you like to Edit or Delete the enquiry?");
        System.out.println("1. Edit Message");
        System.out.println("2. Delete Enquiry");
        int action = scanner.nextInt();
        scanner.nextLine(); // consume newline
        
        
        switch (action) {
        //have to edit such that it shows a change in the csv
            case 1:
                System.out.print("Enter new enquiry message: ");
                String newMessage = scanner.nextLine();
                enquiryManager.updateEnquiryMessage(selected, newMessage);
                System.out.println();
                break;
            case 2:
                enquiryManager.deleteEnquiry(selected);
                System.out.println();
                break;
            default:
                System.out.println("Invalid option.");
                System.out.println();
        }
    }    
    
    private void viewApplicantProfile() {
    	//added security - user inputs password before seeing personal details
    	System.out.println("Please enter your password to view your profile details: ");
    	String autpassword = scanner.next();
    	if (autpassword.equals(applicant.getPassword())){ 
	        System.out.println("==== Applicant Profile ====");
	        System.out.println("Name: " + applicant.getName());
	        System.out.println("NRIC: " + applicant.getNRIC());
	        System.out.println("Age: " + applicant.getAge());
	        System.out.println("Marital Status: " + (applicant.isMarried() ? "Married" : "Single"));
	        System.out.println();
    	}else {
    		System.out.println("Wrong password!");
    	}
    	System.out.println();
    }
}



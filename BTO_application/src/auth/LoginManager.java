package auth;

import java.util.Scanner;

import boundary.ApplicantUI;
import boundary.HDBManagerUI;
import boundary.HDBOfficerUI;
import control.UserManager;
import control.ApplicantManager;
import control.ApplicationManager;
import control.EnquiryManager;
import control.OfficerRegistrationManager;
import control.ProjectManager;
import entities.*;


public class LoginManager {
	private ApplicantManager applicantManager;
	private ApplicationManager applicationManager;
	private UserManager userManager;
	private EnquiryManager enquiryManager;
	private ProjectManager projectManager;
	private OfficerRegistrationManager officerRegistrationManager;
	
	public LoginManager(ApplicantManager applicantManager, UserManager userManager, EnquiryManager enquiryManager, ApplicationManager applicationManager, ProjectManager projectManager, OfficerRegistrationManager officerRegistrationManager) {
		this.applicantManager = applicantManager;
		this.userManager = userManager;
		this.enquiryManager = enquiryManager;
		this.applicationManager = applicationManager;
		this.projectManager = projectManager;
	}
	
	/**
     * Validates NRIC format.
     * NRIC must start with S or T, followed by 7 digits and ending with a letter.
     *
     * @param nric The NRIC string to validate.
     * @return true if valid, false otherwise.
     */
    private boolean isValidNRIC(String nric) {
        return nric.matches("^[ST]\\d{7}[A-Za-z]$");
    }
	
	public void login() {
		Scanner scanner = new Scanner(System.in);
		boolean loggedIn = false;
		
		while (!loggedIn) {
			System.out.println("----BTO Management System Login Page----");
			System.out.print("Enter NRIC: ");
			// make sure input matches NRIC format
			String NRIC = scanner.nextLine().trim().toUpperCase();
			
			//Validate NRIC format
			if(!isValidNRIC(NRIC)) {
				System.out.println("Invalid NRIC format. Please try again.");
				System.out.println();
				continue; //reset to login inputs
			}
			
			System.out.print("Enter Password: ");
			String password = scanner.nextLine();
			System.out.println();
			boolean found = false;
			
			//Check Applicants
			for(Applicant a:userManager.getApplicants()) {    //iterates through all applicants in system
				// checks if NRIC and password entered by individual matches the current applicant
				if(a.getNRIC().equalsIgnoreCase(NRIC)&& a.verifyPassword(password)) {
					System.out.println("Welcome "+a.getName() + "!");
					System.out.println("Signed in as Applicant.");
					System.out.println();
					
					//creates new object and passes an Applicant to it
					//calls constructor of Applicant UI
					ApplicantUI applicantUI = new ApplicantUI(a, applicantManager, userManager, this, enquiryManager, applicationManager);
					applicantUI.showMenu();
					found = true;
					loggedIn = true;
					break;
				}
			}
			
			if(found) {
				break;
			}
			
			//Check Officers
			for(Officer o: userManager.getOfficers()){
				if (o.getNRIC().equalsIgnoreCase(NRIC) && o.verifyPassword(password)) {
					System.out.println("Welcome " + o.getName() + "!");
					System.out.println("Signed in as HDB Officer.");
					System.out.println();
					HDBOfficerUI officerUI = new HDBOfficerUI(o, applicantManager, userManager, this, enquiryManager, applicationManager, projectManager, officerRegistrationManager);
					officerUI.showMenu();
					found = true;
					loggedIn = true;
					break;
				}
			}
			
			if(found) {
				break;
			}
			
			//check for Manager
			for(Manager m: userManager.getManagers()){
				if (m.getNRIC().equalsIgnoreCase(NRIC) && m.verifyPassword(password)) {
					System.out.println("Welcome " + m.getName() + "!");
					System.out.println("Signed in as HDB Manager.");
					System.out.println();
					HDBManagerUI managerUI = new HDBManagerUI(m);
					managerUI.showMenu();
					found = true;
					loggedIn = true;
					break;
				}
			}
			
			if(found) {
				break;
			}
		
			//if NRIC or password no match
			if (!found) {
				System.out.println("Unsuccessful Login. Invalid NRIC or Password.");
			}
			
		}
	}
}

package auth;

import java.util.Scanner;

import boundary.ApplicantUI;
import boundary.HDBManagerUI;
import boundary.HDBOfficerUI;

import control.UserManager;
import control.ApplicantManager;
import control.ApplicationManager;
import control.BookingManager;
import control.EnquiryManager;
import control.FilterManager;
import control.OfficerRegistrationManager;
import control.ProjectManager;
import control.ReportManager;

import entities.*;


public class LoginManager {
	private final ApplicantManager applicantManager;
	private final ApplicationManager applicationManager;
	private final UserManager<Applicant> applicantUserManager;
	private final UserManager<Officer> officerUserManager;
	private final UserManager<Manager> managerUserManager;
	private final EnquiryManager enquiryManager;
	private final ProjectManager projectManager;
	private final OfficerRegistrationManager officerRegistrationManager;
    private final BookingManager bookingManager;
    private final ReportManager reportManager;
    private final FilterManager filterManager;

	public LoginManager(ApplicantManager applicantManager,
                        UserManager<Applicant> applicantUserManager,
                        UserManager<Officer> officerUserManager,
                        UserManager<Manager> managerUserManager,
                        EnquiryManager enquiryManager,
                        ApplicationManager applicationManager,
                        ProjectManager projectManager,
                        OfficerRegistrationManager officerRegistrationManager,
                        BookingManager bookingManager,
                        ReportManager reportManager,
                        FilterManager filterManager
                        ) {
		this.applicantManager = applicantManager;
		this.applicantUserManager = applicantUserManager;
		this.officerUserManager = officerUserManager;
		this.managerUserManager = managerUserManager;
		this.enquiryManager = enquiryManager;
		this.applicationManager = applicationManager;
		this.projectManager = projectManager;
        this.officerRegistrationManager = officerRegistrationManager;
        this.bookingManager = bookingManager;
        this.reportManager = reportManager;
        this.filterManager = filterManager;
	}
	
	public static void welcomeBanner() {
		System.out.println(" #######  #########  #######       #######   #######   #######  ");
		System.out.println(" ##    ##    ##     ##     ##     ##     ## ##     ## ##     ## ");
		System.out.println(" ##    ##    ##     ##     ##     ##     ## ##     ## ##     ## ");
		System.out.println(" #######     ##     ##     ##     ##     ## ##     ## ##     ## ");
		System.out.println(" ##    ##    ##     ##     ##     ## ### ## ########  ########  ");
		System.out.println(" ##    ##    ##     ##     ##     ##     ## ##        ##        ");
		System.out.println(" #######     ##      #######      ##     ## ##        ##        ");
		System.out.println("-------------------------------------------------------------------");
	}

    private boolean isValidNRIC(String nric) {
        if (nric == null) return false;
        return nric.matches("^[ST]\\d{7}[A-Za-z]$");
    }

	public void login() {
		Scanner scanner = new Scanner(System.in);
		boolean loggedIn = false;
        User authenticatedUser = null;

		while (!loggedIn) {
			System.out.println("\n");
			System.out.println("=============== Login Page ===============");
			System.out.print("Enter NRIC: ");
			String inputNRIC = scanner.nextLine().trim().toUpperCase();

			if(!isValidNRIC(inputNRIC)) {
				System.out.println("Invalid NRIC format. Please try again (e.g., S1234567A).");
				continue;
			}
			
			System.out.println();
			System.out.print("Enter Password: ");
			String inputPassword = scanner.nextLine();
			System.out.println();

			authenticatedUser = null;
			boolean found = false;

            for(Officer o: officerUserManager.getUsers()){
                if (o.getNRIC().equalsIgnoreCase(inputNRIC) && o.verifyPassword(inputPassword)) {
                    authenticatedUser = o;
                    found = true;
                    break;
                }
            }			

			if(!found) {
                for(Manager m: managerUserManager.getUsers()){
                    if (m.getNRIC().equalsIgnoreCase(inputNRIC) && m.verifyPassword(inputPassword)) {
                         authenticatedUser = m;
                         found = true;
                         break;
                    }
                }
			}

			if(!found) {
				for(Applicant a : applicantUserManager.getUsers()) {
					if(a.getNRIC().equalsIgnoreCase(inputNRIC) && a.verifyPassword(inputPassword)) {
	                    authenticatedUser = a;
						found = true;
						break;
					}
				}
			}

			// Process Login Result
			if (found && authenticatedUser != null) {
                loggedIn = true;
                System.out.println("Login Successful!");
                System.out.println("Welcome " + authenticatedUser.getName() + "!");

                // Launch the appropriate UI based on the user's Role or Type
                if (authenticatedUser instanceof Officer) {
                    System.out.println("Signed in as HDB Officer.");
                    System.out.println();
                    HDBOfficerUI officerUI = new HDBOfficerUI(
                        (Officer) authenticatedUser,
                        applicantManager,
                        applicantUserManager,
                        officerUserManager,
                        this,
                        enquiryManager,
                        applicationManager,
                        projectManager,
                        officerRegistrationManager,
                        bookingManager,
                        filterManager
                     );
                    officerUI.showMenu();

                } else if (authenticatedUser instanceof Applicant) {
                    System.out.println("Signed in as Applicant.");
                    System.out.println();
                    ApplicantUI applicantUI = new ApplicantUI(
                        (Applicant) authenticatedUser,
                        applicantManager,
                        applicantUserManager,
                        this,
                        enquiryManager,
                        applicationManager,
                        projectManager,
                        filterManager
                    );
                    applicantUI.showMenu();

                } else if (authenticatedUser instanceof Manager) {
                     System.out.println("Signed in as HDB Manager.");
                     System.out.println();
                     HDBManagerUI managerUI = new HDBManagerUI(
                         (Manager) authenticatedUser,
                         projectManager,
                         applicationManager,
                         officerRegistrationManager,
                         enquiryManager,
                         reportManager,
                         applicantUserManager,
                         officerUserManager,
                         managerUserManager,
                         this,
                         filterManager
                     );
                     managerUI.showMenu();
                } else {
                     System.err.println("Error: Authenticated user type unknown.");
                }

			} else {
				System.out.println("Login Unsuccessful. Invalid NRIC or Password.");
			}
		}
	}

    public void logout(User user) {
         if (user != null) {
             System.out.println("\nLogging out " + user.getName() + "...");
         } else {
             System.out.println("\nLogging out...");
         }
         login();
    }
}
package boundary;

import java.util.List;
import java.util.Scanner;
import java.time.LocalDate;

import auth.LoginManager;
import control.UserManager;
import control.ApplicantManager;
import control.ApplicationManager;
import control.EnquiryManager;
import control.OfficerRegistrationManager;
import control.ProjectManager;
import control.BookingManager;

import entities.Applicant;
import entities.Enquiry;
import entities.Manager;
import entities.Officer;
import entities.Project;
import entities.Room;
import enums.ApplicationStatus;
import enums.OfficerRegistrationStatus;
import enums.RoomType;

public class HDBOfficerUI extends ApplicantUI {

    private Officer officer;
    private ProjectManager projectManager;
    private OfficerRegistrationManager officerRegistrationManager;
    private BookingManager bookingManager; 
    private UserManager<Officer> officerUserManager;
    
    public HDBOfficerUI(Officer officer,
                        ApplicantManager applicantManager,
                        UserManager<Applicant> applicantUserManager,
                        UserManager<Officer> officerUserManager,
                        LoginManager loginManager,
                        EnquiryManager enquiryManager,
                        ApplicationManager applicationManager,
                        ProjectManager projectManager,
                        OfficerRegistrationManager officerRegistrationManager,
                        BookingManager bookingManager) {
        // Call the super constructor (ApplicantUI)
        super(officer,
              applicantManager,
              applicantUserManager,
              loginManager,
              enquiryManager,
              applicationManager,
              projectManager);

        this.officer = officer;
        this.officerUserManager = officerUserManager;
        this.projectManager = projectManager;
        this.officerRegistrationManager = officerRegistrationManager;
        this.bookingManager = bookingManager;
    }

    @Override
    public void showMenu() {
        boolean logout = false;

        while (!logout) {
            System.out.println("\n============ HDB Officer Dashboard ============");
            System.out.println(" User: " + officer.getName() + " (" + officer.getNRIC() + ")");
            System.out.println("------------------------------------------------");
            System.out.println("-------- Applicant Actions (As Officer) --------");
            System.out.println(" 1. Change Password");
            System.out.println(" 2. View Available Projects (as Applicant)");
            System.out.println(" 3. Apply for a Project (as Applicant)");
            System.out.println(" 4. View My Application Status");
            System.out.println(" 5. Withdraw My Application");
            System.out.println(" 6. Submit an Enquiry");
            System.out.println(" 7. View/Edit/Delete My Enquiries");
            System.out.println("--- Officer Actions ---");
            System.out.println(" 8. Register to Handle a Project");
            System.out.println(" 9. View My HDB Officer Registration Status");
            System.out.println("10. View Details of Project I Handle");
            System.out.println("11. View and Reply to Project Enquiries");
            System.out.println("12. Book Flat for Successful Applicant");
            System.out.println("13. Generate Booking Receipt for Applicant");
            System.out.println("14. View My Profile");
            System.out.println("15. Logout");
            System.out.println("================================================");
            System.out.print("Enter your choice: ");

            int choice = -1;
            try {
                 choice = scanner.nextInt();
            } catch (java.util.InputMismatchException e) {
                 System.out.println("Invalid input. Please enter a number.");
            } finally {
                 scanner.nextLine();
            }
            System.out.println();

            switch (choice) {
                case 1:
                    changePassword(); // Use overridden method below
                    break;
                case 2:
                    viewAvailableProjects(); // Inherited from ApplicantUI
                    break;
                case 3:
                    applyForProject(); // Inherited from ApplicantUI
                    break;
                case 4:
                    viewApplicationStatus(); // Inherited from ApplicantUI
                    break;
                case 5:
                    withdrawApplication(); // Inherited from ApplicantUI
                    break;
                case 6:
                    submitEnquiry(); // Inherited from ApplicantUI
                    break;
                case 7:
                    viewEditDeleteMyEnquiries(); // Inherited from ApplicantUI
                    break;
                // Officer Actions
                case 8:
                    registerToHandleProject();
                    break;
                case 9:
                    viewOfficerRegistrationStatus();
                    break;
                case 10:
                    viewHandledProjectDetails();
                    break;
                case 11:
                    viewAndReplyToProjectEnquiries();
                    enquiryManager.saveEnquiries("data/enquiries.csv");
                    break;
                case 12:
                    bookFlatForApplicant();
                    break;
                 case 13:
                    generateApplicantReceipt();
                    break;
                case 14:
                    viewOfficerProfile();
                    break;
                case 15:
                    logout = true;
                    loginManager.logout(this.officer);
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
             if (!logout) {
                 System.out.println("\nPress Enter to return to the menu...");
                 scanner.nextLine();
             }
        }
        System.out.println("Exiting Officer Dashboard.");
    }

    @Override
    protected void changePassword() {
        System.out.print("Enter current password: ");
        String currentPassword = scanner.nextLine();

        if (!officer.verifyPassword(currentPassword)) {
            System.out.println("Incorrect current password.");
            return;
        }

        System.out.print("Enter new password: ");
        String newPassword = scanner.nextLine();
        System.out.print("Confirm new password: ");
        String confirmPassword = scanner.nextLine();

        if (!newPassword.equals(confirmPassword)) {
            System.out.println("New passwords do not match.");
            return;
        }

        boolean success = officerUserManager.changePassword(officer.getNRIC(), newPassword);

        if (success) {
            System.out.println("Password changed successfully.");
        } else {
            System.out.println("Password change failed. Please check requirements or try again.");
        }
    }

    // Officer-Specific Method Implementations

    private void registerToHandleProject() {
        System.out.println("--- Register to Handle Project ---");
        System.out.println("Available Projects:");
        List<Project> allProjects = projectManager.getProjects();
        if (allProjects.isEmpty()) {
            System.out.println("There are currently no projects in the system.");
            return;
        }

        displayProjectListBasic(allProjects);

        System.out.print("Enter the exact name of the project you want to register for (or leave blank to cancel): ");
        String projectName = scanner.nextLine();

        if (projectName.trim().isEmpty()) {
             System.out.println("Registration cancelled.");
             return;
        }

        Project selectedProject = projectManager.findProjectByName(projectName);

        if (selectedProject == null) {
            System.out.println("Project '" + projectName + "' not found.");
            return;
        }

        boolean success = officerRegistrationManager.requestRegistration(this.officer, selectedProject);

        if (success) {
            System.out.println("Registration request submitted for '" + selectedProject.getName() + "'. Status: PENDING.");
        } else {
            System.out.println("Failed to submit registration request.");
        }
    }


    private void viewOfficerRegistrationStatus() {
        System.out.println("--- My HDB Officer Registration Status ---");
        OfficerRegistrationStatus status = officer.getRegistrationStatus();
        Project regProject = officer.getRegisteredProject();

        if (status == null || regProject == null) {
            System.out.println("You have no pending or active HDB Officer registration.");
        } else {
            System.out.println("Project Registered For: " + regProject.getName());
            System.out.println("Registration Status:    " + status);
            if (status == OfficerRegistrationStatus.REJECTED) {
                 System.out.println("Your registration request for this project was rejected.");
            }
        }
    }

    private void viewHandledProjectDetails() {
        System.out.println("--- Details of Project I Handle ---");
        Project handledProject = null;
        if (officer.getRegistrationStatus() == OfficerRegistrationStatus.APPROVED && officer.getRegisteredProject() != null) {
            handledProject = projectManager.findProjectByName(officer.getRegisteredProject().getName());
        }

        if (handledProject == null) {
            System.out.println("You are not currently approved to handle any project.");
            return;
        }
        displayProjectDetails(handledProject);
    }

    private void viewAndReplyToProjectEnquiries() {
        System.out.println("--- View and Reply to Project Enquiries ---");
        Project handledProject = null;
        if (officer.getRegistrationStatus() == OfficerRegistrationStatus.APPROVED && officer.getRegisteredProject() != null) {
            handledProject = projectManager.findProjectByName(officer.getRegisteredProject().getName());
        }

        if (handledProject == null) {
            System.out.println("You are not currently approved to handle any project, cannot view enquiries.");
            return;
        }

        System.out.println("Fetching enquiries for Project: " + handledProject.getName());
        List<Enquiry> enquiries = enquiryManager.getEnquiriesByProject(handledProject.getName());

        if (enquiries.isEmpty()) {
            System.out.println("No enquiries found for this project.");
            return;
        }

        System.out.println("-------------------------------------------------");
        for (int i = 0; i < enquiries.size(); i++) {
            Enquiry e = enquiries.get(i);
             System.out.println("Enquiry #" + (i + 1));
             System.out.println(" From NRIC: " + e.getApplicantNRIC());
             System.out.println(" From Name: " + e.getApplicantName());
             System.out.println(" Message:   " + e.getMessage());
             System.out.println(" Reply:     " + (e.getReply() == null || e.getReply().isEmpty() ? "<No Reply Yet>" : e.getReply()));
            System.out.println("-------------------------------------------------");
        }

        System.out.print("Select an enquiry number to reply (Enter 0 to cancel): ");
        int selection = -1;
        try {
            selection = scanner.nextInt();
        } catch (java.util.InputMismatchException e) {}
        finally {
             scanner.nextLine();
        }

        if (selection <= 0 || selection > enquiries.size()) {
            System.out.println("Reply cancelled or invalid selection.");
            return;
        }

        Enquiry selectedEnquiry = enquiries.get(selection - 1);

        // Check if already replied
        if (selectedEnquiry.getReply() != null && !selectedEnquiry.getReply().isEmpty()) {
            System.out.println("This enquiry has already been replied to.");
            System.out.print("Do you want to overwrite the existing reply? (yes/no): ");
            String confirmation = scanner.nextLine().trim().toLowerCase();
            if (!confirmation.equals("yes")) {
                System.out.println("Reply cancelled.");
                return;
            }
        }

        System.out.print("Enter your reply: ");
        String reply = scanner.nextLine();

		enquiryManager.replyToEnquiry(selectedEnquiry, reply, officer.getName());
		selectedEnquiry.setReply(reply, officer.getName());
        enquiryManager.saveEnquiries();

        System.out.println("Reply submitted successfully for Enquiry #" + selection);
    }


     private void bookFlatForApplicant() {
         System.out.println("--- Book Flat for Successful Applicant ---");
         System.out.print("Enter the NRIC of the applicant whose application status is SUCCESSFUL: ");
         String applicantNRIC = scanner.nextLine().trim().toUpperCase();

         if(!isValidNRIC(applicantNRIC)) {
              System.out.println("Invalid NRIC format entered.");
              return;
         }

         // Use BookingManager to handle the booking logic
         boolean success = bookingManager.bookFlat(this.officer, applicantNRIC);

         if (success) {
              System.out.println("Booking process completed.");
         } else {
              System.out.println("Booking process failed.");
         }
     }

     private void generateApplicantReceipt() {
        System.out.println("--- Generate Booking Receipt ---");
        System.out.print("Enter the NRIC of the applicant whose status is BOOKED: ");
        String applicantNRIC = scanner.nextLine().trim().toUpperCase();

         if(!isValidNRIC(applicantNRIC)) {
              System.out.println("Invalid NRIC format entered.");
              return;
         }

        Applicant applicant = super.applicantUserManager.findByNRIC(applicantNRIC);

        if (applicant == null) {
             System.out.println("Applicant with NRIC '" + applicantNRIC + "' not found.");
             return;
        }

        // Check status
        if (applicant.getStatus() != ApplicationStatus.BOOKED) {
             System.out.println("Applicant '" + applicantNRIC + "' has not booked a flat (Status: " + applicant.getStatus() + ").");
             return;
        }

        // Generate receipt using BookingManager
        String receipt = bookingManager.generateBookingReceipt(applicant);
        System.out.println("\n" + receipt);
    }


	private void viewOfficerProfile() {
        System.out.println("========== Officer Profile ==========");
        System.out.println(" Name:           " + officer.getName());
        System.out.println(" NRIC:           " + officer.getNRIC());
        System.out.println(" Age:            " + officer.getAge());
        System.out.println(" Marital Status: " + (officer.isMarried() ? "Married" : "Single"));

        // Display registration details
        OfficerRegistrationStatus regStatus = officer.getRegistrationStatus();
        Project regProject = officer.getRegisteredProject();

        System.out.println("--- HDB Officer Role ---");
        if (regStatus == OfficerRegistrationStatus.APPROVED && regProject != null) {
            System.out.println(" Handling Project: " + regProject.getName());
            System.out.println(" Status:           APPROVED");
        } else if (regStatus == OfficerRegistrationStatus.PENDING && regProject != null) {
             System.out.println(" Pending Registration For: " + regProject.getName());
             System.out.println(" Status:                 PENDING");
        } else if (regStatus == OfficerRegistrationStatus.REJECTED && regProject != null) {
             System.out.println(" Registration Rejected For: " + regProject.getName());
             System.out.println(" Status:                    REJECTED");
        }
        else {
            System.out.println(" Not currently registered to handle any project.");
        }

         // Display Applicant application details
         System.out.println("--- Applicant Role ---");
         super.displayApplicantApplicationStatus(this.officer);

        System.out.println("===================================");
    }


    // Helper Methods (Could be moved or shared)

    // Basic display for project selection
    private void displayProjectListBasic(List<Project> projects) {
        System.out.println("-------------------------------------");
        System.out.printf(" %-30s | %s%n", "Project Name", "Status");
        System.out.println("-------------------------------------");
        if (projects.isEmpty()) {
             System.out.println("         <No Projects Found>         ");
        } else {
            for (int i = 0; i < projects.size(); i++) {
                 Project p = projects.get(i);
                 String status = p.isVisibility() ? "Visible" : "Hidden";
                 LocalDate today = LocalDate.now();
                 if(p.getOpenDate() != null && p.getCloseDate() != null) {
                      if (today.isBefore(p.getOpenDate())) status += ", Opens Soon";
                      else if (today.isAfter(p.getCloseDate())) status += ", Closed";
                      else status += ", Open";
                 } else {
                     status += ", Dates Invalid";
                 }
                 System.out.printf(" %-30s | %s%n", p.getName(), status);
            }
        }
        System.out.println("-------------------------------------");
    }

    // Detailed project display
    private void displayProjectDetails(Project p) {
         if (p == null) {
              System.out.println("Project details cannot be displayed (Project is null).");
              return;
         }
         System.out.println("--- Project Details: " + p.getName() + " ---");
         System.out.println(" Neighbourhood: " + p.getNeighbourhood());
         System.out.println(" Visibility:    " + (p.isVisibility() ? "ON" : "OFF"));
         System.out.println(" Application Opens:  " + (p.getOpenDate() != null ? p.getOpenDate().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE) : "N/A"));
         System.out.println(" Application Closes: " + (p.getCloseDate() != null ? p.getCloseDate().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE) : "N/A"));
         System.out.println(" Manager NRIC:  " + p.getManager());
         System.out.println(" Officer Slots Available: " + p.getOfficerSlot());
         System.out.println(" Assigned Officer Name: " + (p.getOfficer() == null || p.getOfficer().isEmpty() ? "<None>" : p.getOfficer()));
         System.out.println(" Room Details:");
         if (p.getRooms() == null || p.getRooms().isEmpty()) {
              System.out.println("  <No room information available>");
         } else {
              for (Room r : p.getRooms()) {
                   System.out.printf("  - %-8s | Total Units: %-4d | Available: %-4d | Price: $%.2f%n",
                                     r.getRoomType(), r.getTotalRooms(), r.getAvailableRooms(), r.getPrice());
              }
         }
         System.out.println("----------------------------------------");
    }

     private boolean isValidNRIC(String nric) {
        if (nric == null) return false;
        return nric.matches("^[ST]\\d{7}[A-Za-z]$");
    }
}
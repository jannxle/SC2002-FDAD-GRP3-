package boundary;

import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

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
            System.out.println("\n========== HDB Officer Dashboard =========");
            System.out.println(" User: " + officer.getName() + " (" + officer.getNRIC() + ")");
            System.out.println("------------------------------------------");
            System.out.println("----- Applicant Actions (As Officer) -----");
            System.out.println(" 1. Change Password");
            System.out.println(" 2. View Projects");
            System.out.println(" 3. Apply for a Project");
            System.out.println(" 4. View My Application Status");
            System.out.println(" 5. Withdraw My Application");
            System.out.println(" 6. Submit an Enquiry");
            System.out.println(" 7. View/Edit/Delete My Enquiries");
            System.out.println();
            System.out.println("------------- Officer Actions -------------");
            System.out.println(" 8. Register to Handle a Project");
            System.out.println(" 9. View My HDB Officer Registration Status");
            System.out.println("10. View Details of Project I Handle");
            System.out.println("11. View and Reply to Project Enquiries");
            System.out.println("12. Book Flat for Successful Applicant");
            System.out.println("13. Generate Booking Receipt for Applicant");
            System.out.println("14. View My Profile");
            System.out.println("15. Logout");
            System.out.println("===========================================");
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
                    applyForProject(); // Override to check if officer is officer for any projects in the same period of time
                    List<Applicant> allApplicants = new ArrayList<>();
                    allApplicants.addAll(applicantUserManager.getUsers());
                    allApplicants.addAll(officerUserManager.getUsers());
                    applicationManager.saveApplications("data/applications.csv", allApplicants);
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
        
        if (newPassword.length() < 8) {
            System.out.println("New password must be at least 8 characters long.");
            return;
        }
        
        System.out.print("Confirm new password: ");
        String confirmPassword = scanner.nextLine();
        
        if (!newPassword.equals(confirmPassword)) {
            System.out.println("New passwords do not match.");
            return;
        }

        boolean success = officerUserManager.changePassword(officer.getNRIC(), newPassword);

        if (success) {
            System.out.println("Password changed successfully.");
            loginManager.login();
        } else {
            System.out.println("Password change failed. Please check requirements or try again.");
        }
    }
    
    protected void viewAvailableProjects() {
        System.out.println("========== All BTO Projects (Officer View) ===============================================");
        // Use the same applicantManager but pass a flag indicating this is an officer
        List<Project> projects = projectManager.getProjects(); // true indicates officer access
        
        if (projects.isEmpty()) {
            System.out.println("There are currently no BTO projects in the system.");
            return;
        }
        displayProjectListWithRooms(projects);
    }
    //override to show the visibility of the project to the officer only
    protected void displayProjectListWithRooms(List<Project> projects) {
        // Format the header with consistent column widths
        System.out.println("=".repeat(115));  // Adjust based on total width
        System.out.printf(" %-20s | %-15s | %-30s | %-12s | %-12s | %-10s%n", 
                          "Project Name", "Neighbourhood", "Room Types (Units Available)", "Open Date", "Close Date", "Visibility");
        System.out.println("-".repeat(115));  // Adjust based on total width
        
        if (projects.isEmpty()) {
            System.out.println(" < No Projects Found >");
        } else {
            // Format dates with DateTimeFormatter for consistent display
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-YY");
            
            for (Project p : projects) {
                StringBuilder roomInfo = new StringBuilder();
                if (p.getRooms() != null && !p.getRooms().isEmpty()) {
                    boolean firstRoom = true;
                    for (Room r : p.getRooms()) {
                        if (!firstRoom) roomInfo.append(", ");
                        roomInfo.append(r.getRoomType().name())
                            .append(" (")
                            .append(r.getAvailableRooms())
                            .append(")");
                        firstRoom = false;
                    }
                } else {
                    roomInfo.append("<No room info>");
                }

                // Format dates
                String openDate = p.getOpenDate().format(dateFormatter);
                String closeDate = p.getCloseDate().format(dateFormatter);
                
                // Add visibility status
                String visibilityStatus = p.isVisibility() ? "ON" : "OFF";

                System.out.printf(" %-20s | %-15s | %-30s | %-12s | %-12s | %-10s%n",
                    p.getName(),
                    p.getNeighbourhood(),
                    roomInfo.toString(),
                    openDate,
                    closeDate,
                    visibilityStatus);
            }
        }
        System.out.println("=".repeat(115));  // Adjust based on total width
    }
    
    protected void applyForProject() {
        // First check if this officer is assigned to any active projects
        List<Project> officerProjects = officer.getRegisteredProjects();
        
        // Filter to only include projects with overlapping application periods (current date is between open and close dates)
        List<Project> activeOfficerProjects = new ArrayList<>();
        LocalDate currentDate = LocalDate.now();
        
        for (Project p : officerProjects) {
            // Check if current date is between open and close dates (inclusive)
            if ((p.getOpenDate().isBefore(currentDate) || p.getOpenDate().isEqual(currentDate)) && 
                (p.getCloseDate().isAfter(currentDate) || p.getCloseDate().isEqual(currentDate))) {
                activeOfficerProjects.add(p);
            }
        }
        
        // If officer is currently assigned to any active projects, prevent application
        if (!activeOfficerProjects.isEmpty()) {
        	System.out.println();
            System.out.println("As an HDB Officer currently assigned to active projects, you cannot apply for BTO projects.");
            System.out.println("Your officer assignments during the current application period:");
            for (Project p : activeOfficerProjects) {
                System.out.println("- " + p.getName() + " (" + p.getOpenDate() + " to " + p.getCloseDate() + ")");
            }
            return;
        }
        
        // If not currently assigned as an officer, proceed with regular application process
        super.applyForProject();
    }

    // Officer-Specific Method Implementations
    private void registerToHandleProject() {
        System.out.println("======= Register to Handle Project =======");
        System.out.println("Available Projects:");
        List<Project> allProjects = projectManager.getProjects();
        
        if (allProjects.isEmpty()) {
            System.out.println("There are currently no projects in the system.");
            return;
        }
        
        LocalDate today = LocalDate.now();
        List<Project> openProjects = new ArrayList<>();
        List<Project> futureProjects = new ArrayList<>();
        
        // Sort projects into current and future
        for (Project p : allProjects) {
                if (p.getOpenDate() != null && p.getCloseDate() != null) {
                    // Check if project is currently open
                    if (!today.isBefore(p.getOpenDate()) && !today.isAfter(p.getCloseDate())) {
                        openProjects.add(p);
                    } 
                    // Check if project is in the future
                    else if (today.isBefore(p.getOpenDate())) {
                        futureProjects.add(p);
                    }
                }
        }
        
        // Display available projects with dates
        System.out.println("\n-------- CURRENTLY OPEN PROJECTS --------");
        if (openProjects.isEmpty()) {
            System.out.println("There are no currently open projects available for registration.");
        } else {
            displayProjectListWithDates(openProjects);
        }
        
        System.out.println("\n-------- UPCOMING PROJECTS --------");
        if (futureProjects.isEmpty()) {
            System.out.println("There are no upcoming projects available for registration.");
        } else {
            displayProjectListWithDates(futureProjects);
        }
        
        if (openProjects.isEmpty() && futureProjects.isEmpty()) {
            System.out.println("No projects are available for registration.");
            return;
        }
        
        // Combine lists for selection
        List<Project> availableProjects = new ArrayList<>();
        availableProjects.addAll(openProjects);
        availableProjects.addAll(futureProjects);
        
        System.out.print("\nEnter the exact name of the project you want to register for (or leave blank to cancel): ");
        String projectName = scanner.nextLine();
        
        if (projectName.trim().isEmpty()) {
            System.out.println("Registration cancelled.");
            return;
        }
        
        Project selectedProject = null;
        for (Project p : availableProjects) {
            if (p.getName().equalsIgnoreCase(projectName)) {
                selectedProject = p;
                break;
            }
        }
        
        if (selectedProject == null) {
            System.out.println("Project '" + projectName + "' not found.");
            return;
        }
        
        // Additional confirmation for future projects
        if (today.isBefore(selectedProject.getOpenDate())) {
            System.out.println("Note: This project will only open on " + formatDate(selectedProject.getOpenDate()));
            System.out.print("Do you still want to register for this future project? (Y/N): ");
            String confirm = scanner.nextLine();
            if (!confirm.toUpperCase().startsWith("Y")) {
                System.out.println("Registration cancelled.");
                return;
            }
        }
        
        boolean success = officerRegistrationManager.requestRegistration(this.officer, selectedProject);
        
        if (success) {
            System.out.println("Registration request submitted for '" + selectedProject.getName() + "'. Status: PENDING.");
        } else {
            System.out.println("Failed to submit registration request.");
        }
    }

    // Helper method to display projects with dates
    private void displayProjectListWithDates(List<Project> projects) {
        String formatStr = " %-25s | %-15s | %-12s | %-12s%n";
        System.out.println("------------------------------------------------------------------------------");
        System.out.printf(formatStr, "Project Name", "Neighbourhood", "Open Date", "Close Date");
        System.out.println("------------------------------------------------------------------------------");
        
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yy");
        
        for (Project p : projects) {
            String openDate = p.getOpenDate() != null ? formatDate(p.getOpenDate()) : "N/A";
            String closeDate = p.getCloseDate() != null ? formatDate(p.getCloseDate()) : "N/A";
            
            System.out.printf(formatStr, 
                             p.getName(), 
                             p.getNeighbourhood(), 
                             openDate,
                             closeDate);
        }
        System.out.println("------------------------------------------------------------------------------");
    }

    // Helper method to format dates consistently
    private String formatDate(LocalDate date) {
        if (date == null) return "N/A";
        return date.format(DateTimeFormatter.ofPattern("dd-MM-yy"));
    }


    private void viewOfficerRegistrationStatus() {
        System.out.println("===== You Officer Registration Status =====");
        List<Project> projects = officer.getRegisteredProjects();
        if (projects.isEmpty()) {
            System.out.println("You have no pending or active HDB Officer registrations.");
            return;
        }
        for (Project p : projects) {
            OfficerRegistrationStatus status = officer.getRegistrationStatusForProject(p);
            System.out.println(" Project: " + p.getName());
            System.out.println(" Status:  " + (status != null ? status : "<Unknown>"));
            System.out.println("-------------------------------------------");
        }
    }

    private void viewHandledProjectDetails() {
        System.out.println("============ Details of My Projects =========");
        boolean found = false;
        for (Project p : officer.getRegisteredProjects()) {
            if (officer.getRegistrationStatusForProject(p) == OfficerRegistrationStatus.APPROVED) {
                displayProjectDetails(p);
                found = true;
            }
        }
        if (!found) {
            System.out.println("You are not currently approved to handle any project.");
        }
    }

    private void viewAndReplyToProjectEnquiries() {
        System.out.println("============= Reply My Enquiries ==========");
        List<Enquiry> replyableEnquiries = new ArrayList<>();
        List<Project> handledProjects = officer.getRegisteredProjects().stream()
            .filter(p -> officer.getRegistrationStatusForProject(p) == OfficerRegistrationStatus.APPROVED)
            .collect(Collectors.toList());

        if (handledProjects.isEmpty()) {
            System.out.println("You are not currently approved to handle any project.");
            return;
        }
        
        int globalEnquiryCounter = 1;

        // Display all enquiries, but only collect replyable ones
        for (Project project : handledProjects) {
            List<Enquiry> projectEnquiries = enquiryManager.getEnquiriesByProject(project.getName());

            if (projectEnquiries.isEmpty()) continue;

            System.out.println("Project: " + project.getName());

            for (Enquiry e : projectEnquiries) {
                System.out.println("Enquiry #" + globalEnquiryCounter);
                System.out.println(" From NRIC:  " + e.getApplicantNRIC());
                System.out.println(" From Name:  " + e.getApplicantName());
                System.out.println(" Message:    " + e.getMessage());

                if (e.getReply() == null || e.getReply().isEmpty()) {
                    System.out.println(" Reply:      <No Reply Yet>");
                    replyableEnquiries.add(e); // track only those without reply
                } else {
                    System.out.println(" Reply:      " + e.getReply());
                    System.out.println(" Replied By: " + (e.getReplyingOfficer() == null ? "<Unknown>" : e.getReplyingOfficer()));
                }
                System.out.println("-------------------------------------------");
                globalEnquiryCounter++;
            }
        }

        if (replyableEnquiries.isEmpty()) {
            System.out.println("No enquiries awaiting replies in your approved projects.");
            return;
        }

        System.out.print("Select enquiry number to reply (0 to cancel): ");
        int selection = -1;
        try {
            selection = scanner.nextInt();
        } catch (Exception ignored) {}
        scanner.nextLine();

        if (selection <= 0 || selection > replyableEnquiries.size()) {
            System.out.println("Reply cancelled or invalid selection.");
            return;
        }

        Enquiry selected = replyableEnquiries.get(selection - 1);
        System.out.print("Enter your reply: ");
        String reply = scanner.nextLine();

        enquiryManager.replyToEnquiry(selected, reply, officer.getName());
        selected.setReply(reply, officer.getName());
        enquiryManager.saveEnquiries("data/enquiries.csv");

        System.out.println("Reply submitted successfully.");
    }


     private void bookFlatForApplicant() {
         System.out.println("=========== Book Flat for Successful Applicants ===========");
         List<Applicant> successfulApplicants = new ArrayList<>();
         successfulApplicants.addAll(applicantUserManager.getUsers());
         successfulApplicants.addAll(officerUserManager.getUsers());

         successfulApplicants = successfulApplicants.stream()
             .filter(a -> a.getStatus() == ApplicationStatus.SUCCESSFUL)
             .collect(Collectors.toList());
         
         if (successfulApplicants.isEmpty()) {
        	    System.out.println("No applicants with SUCCESSFUL status available for booking.");
        	    return;
        	}

        	System.out.println("========== Applicants Eligible for Booking ==========");
        	for (int i = 0; i < successfulApplicants.size(); i++) {
        	    Applicant a = successfulApplicants.get(i);
        	    System.out.printf("%d. %s (%s) | Project: %s | Room: %s%n", i + 1, a.getName(), a.getNRIC(),
        	                      a.getAppliedProject().getName(), a.getRoomChosen());
        	}
        	
        System.out.println("-------------------------------------------------------------");
         System.out.print("Enter NRIC of the applicant whose application status is SUCCESSFUL: ");
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
    	    System.out.println("========== Generate Booking Receipt ==========");

    	    // Get officer's approved projects
    	    List<Project> handledProjects = officer.getRegisteredProjects().stream()
    	        .filter(p -> officer.getRegistrationStatusForProject(p) == OfficerRegistrationStatus.APPROVED)
    	        .collect(Collectors.toList());

    	    if (handledProjects.isEmpty()) {
    	        System.out.println("You are not currently handling any projects.");
    	        return;
    	    }

    	    // Find all booked applicants in these projects
    	    List<Applicant> bookedApplicants = applicantUserManager.getUsers().stream()
    	        .filter(a -> a.getStatus() == ApplicationStatus.BOOKED &&
    	                     a.getAppliedProject() != null &&
    	                     handledProjects.contains(a.getAppliedProject()))
    	        .collect(Collectors.toList());

    	    if (bookedApplicants.isEmpty()) {
    	        System.out.println("No applicants with status BOOKED under your projects.");
    	        return;
    	    }

    	    // Display list
    	    System.out.println("\nBooked Applicants:");
    	    for (int i = 0; i < bookedApplicants.size(); i++) {
    	        Applicant a = bookedApplicants.get(i);
    	        System.out.printf("%d. %s (%s) - %s\n", i + 1, a.getName(), a.getNRIC(), a.getAppliedProject().getName());
    	    }

    	    // Prompt selection
    	    System.out.print("Select an applicant to generate receipt (Enter 0 to cancel): ");
    	    int choice = -1;
    	    try {
    	        choice = scanner.nextInt();
    	    } catch (InputMismatchException e) {
    	        System.out.println("Invalid input.");
    	        scanner.nextLine(); // clear
    	        return;
    	    }
    	    scanner.nextLine(); // consume newline

    	    if (choice <= 0 || choice > bookedApplicants.size()) {
    	        System.out.println("Invalid input. Action cancelled.");
    	        return;
    	    }

    	    // Generate receipt
    	    Applicant selectedApplicant = bookedApplicants.get(choice - 1);
    	    String receipt = bookingManager.generateBookingReceipt(selectedApplicant);
    	    System.out.println("\n" + receipt);
    	}



     private void viewOfficerProfile() {
         System.out.println("============= Officer Profile =============");
         System.out.println(" Name:           " + officer.getName());
         System.out.println(" NRIC:           " + officer.getNRIC());
         System.out.println(" Age:            " + officer.getAge());
         System.out.println(" Marital Status: " + (officer.isMarried() ? "Married" : "Single"));

         System.out.println("============ HDB Officer Role =============");
         List<Project> projects = officer.getRegisteredProjects();
         if (projects.isEmpty()) {
             System.out.println(" Not currently registered to handle any project.");
         } else {
             for (Project p : projects) {
                 OfficerRegistrationStatus status = officer.getRegistrationStatusForProject(p);
                 System.out.println(" Project: " + p.getName());
                 System.out.println(" Status : " + status);
                 System.out.println();
             }
         }

         System.out.println("============== Applicant Role =============");
         super.displayApplicantApplicationStatus(this.officer);
         System.out.println("===========================================");
     }


    // Helper Methods (Could be moved or shared)

    // Basic display for project selection
    private void displayProjectListBasic(List<Project> projects) {
        System.out.println("-------------------------------------------");
        System.out.printf(" %-20s | %s%n", "Project Name", "Status");
        System.out.println("-------------------------------------------");
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
                 System.out.printf(" %-20s | %s%n", p.getName(), status);
            }
        }
        System.out.println("-------------------------------------------");
    }

    // Detailed project display
    private void displayProjectDetails(Project p) {
         if (p == null) {
              System.out.println("Project details cannot be displayed (Project is null).");
              return;
         }
         System.out.println("====== Project Details: " + p.getName() + " ================================================");
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
         System.out.println("=====================================================================================");
    }

     private boolean isValidNRIC(String nric) {
        if (nric == null) return false;
        return nric.matches("^[ST]\\d{7}[A-Za-z]$");
    }
}
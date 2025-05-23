package boundary;

import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import auth.LoginManager;
import control.*;
import entities.*;
import enums.*;

/**
 * Provides the command-line user interface for users logged in as Applicants.
 * Displays a menu of available actions and interacts with various manager classes
 * to perform operations like viewing projects, applying, managing enquiries, etc.
 * This class serves as the base class for HDBOfficerUI, which inherits
 * applicant functionalities.
 */
public class ApplicantUI {

    protected Applicant applicant;
    protected Scanner scanner;
    protected ApplicantManager applicantManager;
    protected ApplicationManager applicationManager;
    protected EnquiryManager enquiryManager;
    protected UserManager<Applicant> applicantUserManager;
    protected ProjectManager projectManager;
    protected LoginManager loginManager;
    protected Filter filter;
    protected FilterManager filterManager;

    /**
     * Constructs an ApplicantUI instance.
     * Initializes the UI with the logged-in applicant and necessary manager dependencies.
     *
     * @param applicant           The Applicant user for this UI session.
     * @param applicantManager    Manager for applicant-specific project viewing logic.
     * @param applicantUserManager Manager for applicant user data operations.
     * @param loginManager        Manager to handle logout.
     * @param enquiryManager      Manager for enquiries.
     * @param applicationManager  Manager for BTO applications.
     * @param projectManager      Manager for general project data.
     * @param filterManager       Manager for user view filters.
     * @throws IllegalArgumentException if any required manager or the applicant is null.
     */
    public ApplicantUI(Applicant applicant,
                       ApplicantManager applicantManager,
                       UserManager<Applicant> applicantUserManager,
                       LoginManager loginManager,
                       EnquiryManager enquiryManager,
                       ApplicationManager applicationManager,
                       ProjectManager projectManager,
                       FilterManager filterManager) {
        if (applicant == null || applicantManager == null || applicantUserManager == null ||
            loginManager == null || enquiryManager == null || applicationManager == null ||
            projectManager == null || filterManager == null) {
             throw new IllegalArgumentException("All manager dependencies and applicant must be non-null.");
        }
        this.applicant = applicant;
        this.applicantManager = applicantManager;
        this.applicantUserManager = applicantUserManager;
        this.loginManager = loginManager;
        this.enquiryManager = enquiryManager;
        this.applicationManager = applicationManager;
        this.projectManager = projectManager;
        this.filterManager = filterManager;
        this.filter = filterManager.getFilter(applicant.getNRIC());
        this.scanner = new Scanner(System.in);
    }

    /**
     * Displays the main menu for the Applicant dashboard and handles user input.
     * Loops until the user chooses to log out. Calls corresponding helper methods
     * based on user selection.
     */
    public void showMenu() {
        boolean logout = false;
        while (!logout) {
            System.out.println("\n=========== Applicant Dashboard ===========");
            System.out.println(" User: " + applicant.getName() + " (" + applicant.getNRIC() + ")");
            System.out.println("-------------------------------------------");
            System.out.println(" 1. Change Password");
            System.out.println(" 2. View Available BTO Projects");
            System.out.println(" 3. Apply for a Project");
            System.out.println(" 4. View My Application Status");
            System.out.println(" 5. Withdraw My Application");
            System.out.println(" 6. Submit an Enquiry");
            System.out.println(" 7. View/Edit/Delete My Enquiries");
            System.out.println(" 8. View My Profile");
            System.out.println(" 9. Set/View/Remove Filters for Viewing Projects");
            System.out.println(" 10. Logout");
            System.out.println("===========================================");
            System.out.print("Enter your choice: ");

            int choice = -1;
             try {
                 choice = scanner.nextInt();
            } catch (InputMismatchException e) {
                 System.out.println("Invalid input. Please enter a number.");
            } finally {
                 scanner.nextLine();
            }
            System.out.println();

            switch (choice) {
                case 1: changePassword(); break;
                case 2: viewAvailableProjects(); break;
                case 3: 
                	applyForProject(); 
                	applicationManager.saveApplications("data/Applications.csv", applicantUserManager.getUsers());
                	projectManager.saveProjects("data/ProjectList.csv");
                	break;
                case 4: viewApplicationStatus(); break;
                case 5: withdrawApplication(); break;
                case 6: 
                	submitEnquiry(); 
	                enquiryManager.saveEnquiries("data/enquiries.csv");
	                break;
                case 7: 
                	viewEditDeleteMyEnquiries(); 
                	enquiryManager.saveEnquiries("data/enquiries.csv");
                	break;
                case 8: viewApplicantProfile(); break;
                case 9: manageFilters(); break;
                case 10: logout = true; loginManager.logout(this.applicant); break;
                default: System.out.println("Invalid option. Please try again.");
            }
            if (!logout) {
                 System.out.println("\nPress Enter to return to the menu...");
                 scanner.nextLine();
             }
        }
        System.out.println("Exiting Applicant Dashboard.");
    }

    // --- Helper Methods for Menu Actions ---

    /**
     * Handles the process of changing the applicant's password.
     * Prompts for current and new passwords, validates, and calls UserManager.
     * Redirects to login upon successful change.
     */
    protected void changePassword() {
         System.out.println("============= Change Password =============");
         System.out.print("Enter current password: ");
         String currentPassword = scanner.nextLine();

        if (!applicant.verifyPassword(currentPassword)) {
            System.out.println("Incorrect current password.");
            return;
        }

        System.out.print("Enter new password: ");
        String newPassword = scanner.nextLine();
        
        if (newPassword.length() < 8) {
        	System.out.println();
            System.out.println("Password must be at least 8 characters long.");
            return;
        }
        
        System.out.print("Confirm new password: ");
        String confirmPassword = scanner.nextLine();


        
        if (!newPassword.equals(confirmPassword)) {
            System.out.println("New passwords do not match.");
            return;
        }

        boolean success = applicantUserManager.changePassword(applicant.getNRIC(), newPassword);

        if (success) {
             System.out.println("Password changed successfully.");
             loginManager.login();
        } else {
             System.out.println("Password change failed. Please check requirements or try again.");
        }
    }

    /**
     * Retrieves and displays the list of BTO projects available to the current applicant,
     * applying any active filters.
     */
    protected void viewAvailableProjects() {
        System.out.println("========== Available BTO Projects for You ============================================================");
        List<Project> projects = ViewProjectFilter.apply(applicantManager.getAvailableProjects(applicant), filter);

        if (projects.isEmpty()) {
            System.out.println("There are currently no BTO projects available for you based on your eligibility or project availability.");
            return;
        }
        displayProjectListWithRooms(projects);
    }

    /**
     * Displays a formatted list of projects, including details about available room types and units.
     * Used by viewAvailableProjects and potentially applyForProject.
     *
     * @param projects The list of Project objects to display.
     */
    protected void displayProjectListWithRooms(List<Project> projects) {

        String divider = "------------------------------------------------------------------------------------------------------";
        String formatStr = " %-20s | %-15s | %-30s | %-10s | %-10s%n";
        
        System.out.println(divider);
        System.out.printf(formatStr,
                "Project Name", "Neighbourhood", "Room Types (Units Available)", "Open Date", "Close Date");
        System.out.println(divider);
        
        if (projects.isEmpty()) {
            System.out.println(" < No Projects Found >");
        } else {
            // Format dates with DateTimeFormatter for consistent display
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yy"); // Changed YY to yy for standard format
            RoomType filteredRoomType = filter.getRoomType();
            
            for (Project p : projects) {
                StringBuilder roomInfo = new StringBuilder();
                if (p.getRooms() != null && !p.getRooms().isEmpty()) {
                    boolean firstRoom = true;
                   
                    for (Room r : p.getRooms()) {
                    	if (filteredRoomType != null && r.getRoomType() != filteredRoomType) continue;
                        if (!firstRoom) roomInfo.append(", ");
                        roomInfo.append(r.getRoomType().name())
                               .append(" (")
                               .append(r.getAvailableRooms())
                               .append(")");
                        firstRoom = false;
                    }
                    if (roomInfo.length() == 0) {
                        roomInfo.append("<No matching room types>");
                    }
                    
                } else {
                    roomInfo.append("<No room info>");
                }
                
                // Format dates
                String openDate = p.getOpenDate() != null ? p.getOpenDate().format(dateFormatter) : "N/A";
                String closeDate = p.getCloseDate() != null ? p.getCloseDate().format(dateFormatter) : "N/A";
                
                System.out.printf(formatStr,
                        p.getName(),
                        p.getNeighbourhood(),
                        roomInfo.toString(),
                        openDate,
                        closeDate);
            }
        }
        System.out.println("======================================================================================================");
    }


    /**
     * Handles the process for an applicant to apply for a BTO project.
     * Checks eligibility, displays available projects, prompts for selection,
     * validates room choice and availability, and calls ApplicationManager.
     */
    protected void applyForProject() {
        System.out.println("========== Apply for BTO Projects ====================================================================");
        // Cannot apply if PENDING, SUCCESSFUL, BOOKED
        if (applicant.getStatus() != null && applicant.getStatus() != ApplicationStatus.UNSUCCESSFUL) {
            System.out.println("You already have an active or successful application (Status: " + applicant.getStatus() + ").");
            System.out.println("Please withdraw it first if you wish to apply for a different project.");
            return;
        }

        List<Project> projects = applicantManager.getAvailableProjects(this.applicant);
        if (projects.isEmpty()) {
            System.out.println("There are currently no projects available for you to apply for.");
            return;
        }
        displayProjectListWithRooms(projects);

        System.out.print("Enter the exact name of the project you wish to apply for (or leave blank to cancel): ");
        String projectName = scanner.nextLine().trim();

        if (projectName.isEmpty()) {
            System.out.println("Application cancelled.");
            return;
        }

        Project selectedProject = null;
        for (Project p : projects) {
            if (p.getName().equalsIgnoreCase(projectName)) {
                selectedProject = p;
                break;
            }
        }

        if (selectedProject == null) {
            System.out.println("Project '" + projectName + "' not found in the list of projects available to you.");
            return;
        }

        List<RoomType> eligibleRoomTypes = getEligibleRoomTypesForProject(applicant, selectedProject);

        if (eligibleRoomTypes.isEmpty()) {
             System.out.println("There are no eligible room types for you in project '" + selectedProject.getName() + "'.");
             return;
        }

        System.out.println("Eligible Room Types for Project '" + selectedProject.getName() + "':");
        for (int i = 0; i < eligibleRoomTypes.size(); i++) {
            System.out.println((i + 1) + ". " + eligibleRoomTypes.get(i).name());
        }
        System.out.print("Select the number for the room type you want to apply for (0 to cancel): ");
        int roomChoice = -1;
        try {
            roomChoice = scanner.nextInt();
        } catch (InputMismatchException e) {}
        finally {
            scanner.nextLine();
        }

        if (roomChoice <= 0 || roomChoice > eligibleRoomTypes.size()) {
            System.out.println("Application cancelled or invalid choice.");
            return;
        }

        RoomType chosenRoom = eligibleRoomTypes.get(roomChoice - 1);

        // Check availability before submitting
        Room selectedRoom = null;
        for (Room r : selectedProject.getRooms()) {
            if (r.getRoomType() == chosenRoom) {
                selectedRoom = r;
                break;
            }
        }

        if (selectedRoom == null || selectedRoom.getAvailableRooms() <= 0) {
            System.out.println("Sorry, there are no more available units for " + chosenRoom + " in project '" + selectedProject.getName() + "'.");
            return;
        }

        // Proceed with application
        boolean success = applicationManager.apply(this.applicant, selectedProject, chosenRoom);

        if (success) {
            System.out.println("Application submitted successfully. Status is PENDING.");
        } else {
            System.out.println("Application submission failed.");
        }
    }

    /**
      * Determines the room types within a project for which the applicant is eligible
      * based on age, marital status, and room availability.
      *
      * @param applicant The applicant whose eligibility is being checked.
      * @param project   The project being considered.
      * @return A List of RoomType the applicant can apply for in this project.
      */
    protected List<RoomType> getEligibleRoomTypesForProject(Applicant applicant, Project project) {
        List<RoomType> eligibleTypes = new ArrayList<>();
        if (project == null || project.getRooms() == null) return eligibleTypes;

        for (Room room : project.getRooms()) {
            RoomType type = room.getRoomType();
            boolean canApply = false;
            if (!applicant.isMarried() && applicant.getAge() >= 35) {
                if (type == RoomType.TwoRoom) canApply = true;
            } else if (applicant.isMarried() && applicant.getAge() >= 21) {
                canApply = true;
            }
            if (canApply) eligibleTypes.add(type);
        }
        return eligibleTypes;
    }

    /**
     * Displays the applicant's current BTO application status.
     */
    protected void viewApplicationStatus() {
        System.out.println("========== My Application Status ==========");
        displayApplicantApplicationStatus(this.applicant);
    }

    /**
      * Helper method to display the application status details for a given applicant.
      * Shows project name, chosen room, status, and relevant messages based on the status.
      *
      * @param applicantForStatus The applicant whose status needs to be displayed.
      */
     protected void displayApplicantApplicationStatus(Applicant applicantForStatus) {
         Project appliedProject = applicantForStatus.getAppliedProject();
         ApplicationStatus status = applicantForStatus.getStatus();

         if (appliedProject != null && status != null && status != ApplicationStatus.UNSUCCESSFUL) {
        	 System.out.println("-------------------------------------------");
             System.out.println("Current Application Details:");
             System.out.println("Project:     " + appliedProject.getName());
             System.out.println("Chosen Room: " + (applicantForStatus.getRoomChosen() != null ? applicantForStatus.getRoomChosen() : "N/A"));
             System.out.println("Status:      " + status);
             System.out.println();
             switch (status) {
                 case PENDING: System.out.println(">>> Your application is pending review by HDB."); break;
                 case SUCCESSFUL: System.out.println(">>> Congratulations! Your application is successful.\n     Please contact an HDB Officer to book your flat."); break;
                 case BOOKED: System.out.println(">>> You have successfully booked a flat for this application."); break;
                 default: break;
             }
         } else if (status == ApplicationStatus.UNSUCCESSFUL) {
              if (appliedProject != null) {
                    System.out.println(" Your application for project '" + appliedProject.getName() + "' was unsuccessful or withdrawn.");
              } else {
                   System.out.println(" Your last application was unsuccessful or withdrawn.");
              }
              System.out.println(" You may apply for other available projects.");
         }
          else {
             System.out.println(" You have no active BTO application.");
         }
    }

    /**
     * Handles the process for an applicant to request withdrawal of their application.
     * Confirms the action and calls ApplicationManager to set status to PENDING_WITHDRAWAL.
     */
    protected void withdrawApplication() {
        System.out.println("=========== Withdraw Application ==========");

        ApplicationStatus currentStatus = applicant.getStatus();
        Project currentProject = applicant.getAppliedProject();
        
        if (currentStatus == null || currentStatus == ApplicationStatus.UNSUCCESSFUL || currentStatus == ApplicationStatus.PENDING_WITHDRAWAL) {
             System.out.println("You do not have an application that can be withdrawn.");
             return;
         }
        
        System.out.println("You are requesting to withdraw your application for project: " +
                (currentProject != null ? currentProject.getName() : "<Unknown>"));
            System.out.println("Current Status: " + currentStatus);
            System.out.println();
            System.out.println(">>> IMPORTANT: Submitting this request will set your application status to PENDING_WITHDRAWAL.");
            System.out.println(">>> The request will be reviewed by an HDB Manager.");

            System.out.print("Are you sure you want to request withdrawal? (Y/N): ");
            String confirmation = scanner.nextLine().trim().toUpperCase();

            if (confirmation.equals("Y") || confirmation.equals("yes")) {
                boolean success = applicationManager.withdrawApplication(this.applicant);
                if (success) {
                    System.out.println();
                    System.out.println("Withdrawal request submitted. HDB Manager will be reviewing your request.");

                    applicationManager.saveApplications("data/applications.csv", applicationManager.getAllApplicants());
                } else {
                    System.out.println("Withdrawal process failed. Please check your current application status or contact HDB.");
                }
            } else {
                System.out.println("Withdrawal cancelled.");
            }
    }

    /**
     * Handles the submission of a new enquiry by the applicant.
     * Displays available projects, prompts for selection and message, then calls EnquiryManager.
     */
    protected void submitEnquiry() {
        System.out.println("============= Submit Enquiry ==============");
        
        List<Project> allProjects = projectManager.getProjects();
        List<Project> visibleProjects = allProjects != null?
        		allProjects.stream()
        				   .filter(Project::isVisibility)
        				   .collect(Collectors.toList())
        				   :new ArrayList<>();
        				   
        				   
        if (visibleProjects == null || visibleProjects.isEmpty()) {
        	System.out.println("There are no projects available for you to submit an enquiry.");
        	return;
        }
        
        System.out.println("Available Projects:");
        for (int i = 0; i < visibleProjects.size(); i++) {
            System.out.printf(" %d. %s (%s)\n", i + 1, visibleProjects.get(i).getName(), visibleProjects.get(i).getNeighbourhood());
        }
        System.out.println("-------------------------------------------");
        System.out.print("Select the project you want to enquiry about (Enter 0 to exit): ");
        int projectChoice = scanner.nextInt();
        scanner.nextLine();
        
        
        if (projectChoice < 1 || projectChoice > visibleProjects.size()) {
            System.out.println("Invalid selection.");
            System.out.println();
            return;
        }
       
        Project chosenProject = visibleProjects.get(projectChoice - 1);
            	
    	System.out.println("Enter your enquiry: ");
    	String message = scanner.nextLine();
    	
        Enquiry e = new Enquiry(applicant.getNRIC(), applicant.getName(), chosenProject.getName(), message);
        enquiryManager.submitEnquiry(e);
        System.out.println("Enquiry submitted successfully!");
        System.out.println();
    }
 
    /**
     * Handles viewing, editing, and deleting the applicant's own enquiries.
     * Displays enquiries, prompts for action, and calls EnquiryManager.
     * Editing/deleting is only allowed if the enquiry has not been replied to.
     */
    protected void viewEditDeleteMyEnquiries() {
        System.out.println("--- View/Edit/Delete My Enquiries ---");
        List<Enquiry> myEnquiries = enquiryManager.getEnquiriesByApplicant(applicant.getNRIC());

        if (myEnquiries.isEmpty()) {
            System.out.println("You have not submitted any enquiries.");
            return;
        }

        System.out.println("Your Submitted Enquiries:");
        System.out.println("-------------------------------------------");
        for (int i = 0; i < myEnquiries.size(); i++) {
            Enquiry e = myEnquiries.get(i);
            System.out.println("Enquiry #" + (i + 1));
            System.out.println("    Project: " + e.getProjectName());
            System.out.println("    Message: " + e.getMessage());
            if (e.getReply() == null || e.getReply().isEmpty()) {
                System.out.println("    Reply:     <No Reply Yet>");
            } else {
                System.out.println(" Reply:     " + e.getReply());
                System.out.println(" Replied By:" + (e.getReplyingOfficer() != null && !e.getReplyingOfficer().isEmpty()
                    ? " " + e.getReplyingOfficer()
                    : " <Unknown>"));
            }
            System.out.println("-------------------------------------------");
        }

        System.out.print("Select enquiry # to Edit or Delete (Enter 0 to cancel): ");
        int choice = -1;
        try {
             choice = scanner.nextInt();
        } catch (InputMismatchException e) {}
        finally {
             scanner.nextLine();
        }

        if (choice <= 0 || choice > myEnquiries.size()) {
            System.out.println("Action cancelled or invalid selection.");
            return;
        }

        Enquiry selectedEnquiry = myEnquiries.get(choice - 1);

        if (selectedEnquiry.getReply() != null && !selectedEnquiry.getReply().isEmpty()) {
            System.out.println("This enquiry has already been replied to and cannot be edited or deleted.");
            return;
        }

        System.out.println();
        System.out.println("Actions for Enquiry" + choice + " :");
        System.out.println("-------------------------------------------");
        System.out.println("1. Edit Message");
        System.out.println("2. Delete Enquiry");
        System.out.println("0. Cancel");
        System.out.print("What would you like to do for your enquiry? : ");
        int actionChoice = -1;
        try {
             actionChoice = scanner.nextInt();
        } catch (InputMismatchException e) {}
        finally {
             scanner.nextLine();
        }

        if (actionChoice == 1) {
            System.out.print("Enter the new enquiry message: ");
            String newMessage = scanner.nextLine().trim();
            if (!newMessage.isEmpty()) {
                boolean edited = enquiryManager.editEnquiry(selectedEnquiry, newMessage, applicant.getNRIC());
                if (edited) {
                    enquiryManager.saveEnquiries();
                    System.out.println("Enquiry message updated successfully.");
                } else { System.out.println("Failed to edit enquiry (perhaps it was replied to recently?)."); }
            } else { System.out.println("New message cannot be empty. Edit cancelled."); }
        } else if (actionChoice == 2) {
            System.out.print("Are you sure you want to delete Enquiry #" + choice + "? (Y/N): ");
            String confirmation = scanner.nextLine().trim().toUpperCase();
            if (confirmation.equals("Y")) {
                 boolean deleted = enquiryManager.deleteEnquiry(selectedEnquiry, applicant.getNRIC());
                 if (deleted) {
                     enquiryManager.saveEnquiries();
                     System.out.println("Enquiry deleted successfully.");
                 } else { System.out.println("Failed to delete enquiry (perhaps it was replied to recently or not found?)."); }
            } else { System.out.println("Deletion cancelled."); }
        } else { System.out.println("Action cancelled."); }
    }

    /**
      * Displays the applicant's profile information, including basic details and application status.
      */
    protected void viewApplicantProfile() {
        System.out.println("=========== Applicant Profile ==============");
        System.out.println(" Name:           " + applicant.getName());
        System.out.println(" NRIC:           " + applicant.getNRIC());
        System.out.println(" Age:            " + applicant.getAge());
        System.out.println(" Marital Status: " + (applicant.isMarried() ? "Married" : "Single"));
        System.out.println(" Role:           " + applicant.getRole());
        displayApplicantApplicationStatus(applicant);
        System.out.println("============================================");
    }

    /**
      * Private helper method to validate NRIC format.
      * @param nric The NRIC string to validate.
      * @return true if the format is valid (S/T + 7 digits + letter), false otherwise.
      */
    private boolean isValidNRIC(String nric) {
        if (nric == null) return false;
        return nric.matches("^[ST]\\d{7}[A-Za-z]$");
    }
    
    /**
      * Manages user interaction for setting, viewing, and removing project view filters.
      * Calls the static method in FilterUI to handle the filter settings menu
      * and updates the applicant's filter settings via FilterManager.
      */
    private void manageFilters() {
    	    FilterUI.promptFilterSettings(scanner, filter);
    	    filterManager.setFilter(applicant.getNRIC(), filter);
    	    filterManager.saveFilters();
     }
}
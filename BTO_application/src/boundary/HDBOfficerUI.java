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
 * Provides the command-line user interface for users logged in as HDB Officers.
 * Extends ApplicantUI to include all applicant functionalities, and adds
 * specific actions for officers, such as registering to handle projects,
 * managing project enquiries, booking flats for applicants, and generating receipts.
 */
public class HDBOfficerUI extends ApplicantUI {

    private Officer officer;
    private ProjectManager projectManager;
    private OfficerRegistrationManager officerRegistrationManager;
    private BookingManager bookingManager; 
    private UserManager<Officer> officerUserManager;
    
    /**
     * Constructs an HDBOfficerUI instance.
     * Initializes the UI with the logged-in officer and necessary manager dependencies,
     * calling the superclass constructor for shared managers.
     *
     * @param officer                   The Officer user for this UI session.
     * @param applicantManager          Manager for applicant-specific project viewing logic.
     * @param applicantUserManager      Manager for base applicant user data operations.
     * @param officerUserManager        Manager for officer user data operations.
     * @param loginManager              Manager to handle logout.
     * @param enquiryManager            Manager for enquiries.
     * @param applicationManager        Manager for BTO applications.
     * @param projectManager            Manager for general project data.
     * @param officerRegistrationManager Manager for officer project registrations.
     * @param bookingManager            Manager for flat booking.
     * @param filterManager             Manager for user view filters.
     * @throws IllegalArgumentException if any required manager or the officer is null.
     */
    public HDBOfficerUI(Officer officer,
                        ApplicantManager applicantManager,
                        UserManager<Applicant> applicantUserManager,
                        UserManager<Officer> officerUserManager,
                        LoginManager loginManager,
                        EnquiryManager enquiryManager,
                        ApplicationManager applicationManager,
                        ProjectManager projectManager,
                        OfficerRegistrationManager officerRegistrationManager,
                        BookingManager bookingManager,
                        FilterManager filterManager) {
        // Call the super constructor (ApplicantUI)
        super(officer,
              applicantManager,
              applicantUserManager,
              loginManager,
              enquiryManager,
              applicationManager,
              projectManager,
              filterManager);

        this.officer = officer;
        this.officerUserManager = officerUserManager;
        this.projectManager = projectManager;
        this.officerRegistrationManager = officerRegistrationManager;
        this.bookingManager = bookingManager;
    }

    /**
     * Displays the main menu for the HDB Officer dashboard and handles user input.
     * Includes both inherited Applicant actions and specific Officer actions.
     * Loops until the user chooses to log out.
     */
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
            System.out.println(" 8. Set/View/Remove Filters for viewing Projects");
            System.out.println();
            System.out.println("------------- Officer Actions -------------");
            System.out.println(" 9. Register to Handle a Project");
            System.out.println(" 10. View My HDB Officer Registration Status");
            System.out.println(" 11. View Details of Project I Handle");
            System.out.println(" 12. View and Reply to Project Enquiries");
            System.out.println(" 13. Book Flat for Successful Applicant");
            System.out.println(" 14. Generate Booking Receipt for Applicant");
            System.out.println(" 15. View My Profile");
            System.out.println(" 16. Logout");
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
                    projectManager.saveProjects("data/ProjectList.csv");
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
                case 8:
                	manageFilters();
                	break;
                // Officer Actions
                case 9:
                    registerToHandleProject();
                    break;
                case 10:
                    viewOfficerRegistrationStatus();
                    break;
                case 11:
                    viewHandledProjectDetails();
                    break;
                case 12:
                    viewAndReplyToProjectEnquiries();
                    enquiryManager.saveEnquiries("data/enquiries.csv");
                    break;
                case 13:
                    bookFlatForApplicant();
                    break;
                 case 14:
                    generateApplicantReceipt();
                    break;
                case 15:
                    viewOfficerProfile();
                    break;
                case 16:
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

    /**
     * Overrides the changePassword method to use the OfficerUserManager for saving.
     * Handles changing the officer's password after verification.
     */
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
    
    /**
     * Overrides viewAvailableProjects to clarify its purpose for officers (viewing projects to apply for).
     * The underlying logic for filtering based on applicant eligibility remains the same.
     */
    protected void viewAvailableProjects() {
        System.out.println("========== Available BTO Projects for You ============================================================");
        // Use the same applicantManager but pass a flag indicating this is an officer
        List<Project> projects = ViewProjectFilter.apply(applicantManager.getAvailableProjectsForOfficer(officer), filter); // true indicates officer access
        
        if (projects.isEmpty()) {
            System.out.println("There are currently no BTO projects in the system.");
            return;
        }
        displayProjectListWithRooms(projects);
    }
   
    /**
     * Overrides the applicant's applyForProject method to add checks specific to HDB Officers.
     * Prevents application if the officer is registered (PENDING/APPROVED) for the same project
     * or for another project with an overlapping application period.
     */
    @Override
    protected void applyForProject() {
        System.out.println("========== Apply for BTO Projects (Officer Check) ================================================");

        // Cannot apply if already have an active/successful application
        if (applicant.getStatus() != null && applicant.getStatus() != ApplicationStatus.UNSUCCESSFUL) {
            System.out.println("You already have an active or successful application (Status: " + applicant.getStatus() + ").");
            System.out.println("Please withdraw it first if you wish to apply for a different project.");
            return;
        }

        List<Project> projects = applicantManager.getAvailableProjects(this.applicant);
        if (projects.isEmpty()) {
            System.out.println("There are currently no projects available for you to apply for based on eligibility.");
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

        // --- Officer-Specific Checks ---
        List<Project> officerRegisteredProjects = officer.getRegisteredProjects();
        LocalDate targetOpen = selectedProject.getOpenDate();
        LocalDate targetClose = selectedProject.getCloseDate();

        if (targetOpen == null || targetClose == null) {
            System.err.println("Application failed: The selected project '" + selectedProject.getName() + "' has invalid application dates.");
            return;
        }

        for (Project registeredProject : officerRegisteredProjects) {
            OfficerRegistrationStatus regStatus = officer.getRegistrationStatusForProject(registeredProject);

            // Check only against PENDING or APPROVED registrations
            if (regStatus == OfficerRegistrationStatus.PENDING || regStatus == OfficerRegistrationStatus.APPROVED) {

                // Check: Is it the SAME project?
                if (registeredProject.equals(selectedProject)) {
                    System.out.println("\nApplication failed: You cannot apply for project '" + selectedProject.getName() +
                                       "' because you have a " + regStatus + " registration to handle it.");
                    return;
                }

                // Check: Does the application period of ANOTHER registered project overlap?
                LocalDate existingOpen = registeredProject.getOpenDate();
                LocalDate existingClose = registeredProject.getCloseDate();

                if (existingOpen != null && existingClose != null) {
                    // Check for overlap: !(targetEnd < existingStart || targetStart > existingEnd)
                    boolean overlaps = !(targetClose.isBefore(existingOpen) || targetOpen.isAfter(existingClose));
                    if (overlaps) {
                        System.out.println("\nApplication failed: The application period for '" + selectedProject.getName() +
                                           "' (" + formatDate(targetOpen) + " to " + formatDate(targetClose) +
                                           ") overlaps with your " + regStatus + " registration for project '" +
                                           registeredProject.getName() + "' (" + formatDate(existingOpen) + " to " + formatDate(existingClose) + ").");
                        return;
                    }
                }
            }
        }

        System.out.println("Officer checks passed. Proceeding with application...");

        // Get eligible room types for the selected project based on applicant criteria
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

        // Check room availability before submitting
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

        // Proceed with application via ApplicationManager
        boolean success = applicationManager.apply(this.applicant, selectedProject, chosenRoom);

        if (success) {
            System.out.println("Application submitted successfully. Status is PENDING.");
        } else {
            System.out.println("Application submission failed (check ApplicationManager logs or previous messages).");
        }
    }

    // --- Officer-Specific Method Implementations ---

    /**
     * Handles the process for an officer to register to handle a BTO project.
     * Displays only projects that are currently visible ('on') and meet other criteria.
     * Prompts the officer to select a project and submits the registration request
     * via OfficerRegistrationManager. Only shows projects with visibility 'true'.
     */
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

    /**
     * Displays a list of projects with their names, neighbourhoods, open and close dates.
     * Uses a formatted string for better readability.
     *
     * @param projects The list of projects to display.
     */
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

    /** 
     * Formats a LocalDate to a string in the format "dd-MM-yy".
     * If the date is null, returns "N/A".
     *
     * @param date The LocalDate to format.
     * @return A formatted string representation of the date.
     */
    private String formatDate(LocalDate date) {
        if (date == null) return "N/A";
        return date.format(DateTimeFormatter.ofPattern("dd-MM-yy"));
    }

    /**
     * Displays the officer's registration status for each project they have registered for.
     */
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

    /**
     * Displays detailed information for projects the officer is currently APPROVED to handle.
     */
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

    /**
     * Allows the officer to view and reply to enquiries for the projects they are APPROVED to handle.
     */
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

    /**
     * Handles the process for an officer to book a flat for an applicant with SUCCESSFUL status.
     * Validates officer authority and applicant status, then calls BookingManager.
     */
    private void bookFlatForApplicant() {
        System.out.println("=========== Book Flat for Successful Applicants ===========");

        // 1. Get projects the current officer handles and is approved for
        List<Project> handledProjects = this.officer.getRegisteredProjects().stream()
            .filter(p -> this.officer.getRegistrationStatusForProject(p) == OfficerRegistrationStatus.APPROVED)
            .collect(Collectors.toList());

        if (handledProjects.isEmpty()) {
            System.out.println("You are not currently approved to handle any projects.");
            System.out.println("Therefore, you cannot book flats for any applicants.");
            return;
        }
        System.out.println("You can book flats for applicants in the following projects you handle:");
        handledProjects.forEach(p -> System.out.println("- " + p.getName()));
        System.out.println("-------------------------------------------------------------");

        // 2. Get all successful applicants from ApplicationManager
        List<Applicant> allSuccessfulApplicants = applicationManager.getAllApplicants().stream()
            .filter(a -> a.getStatus() == ApplicationStatus.SUCCESSFUL)
            .collect(Collectors.toList());

        // 3. Filter successful applicants to only those in projects handled by this officer
        List<Applicant> eligibleApplicants = allSuccessfulApplicants.stream()
            .filter(a -> a.getAppliedProject() != null && handledProjects.contains(a.getAppliedProject()))
            .collect(Collectors.toList());

        if (eligibleApplicants.isEmpty()) {
           System.out.println("No applicants with SUCCESSFUL status found for the projects you handle.");
           return;
       }

       // 4. Display only the eligible applicants
       System.out.println("======= Applicants Eligible for Booking (Your Projects) =======");
       for (int i = 0; i < eligibleApplicants.size(); i++) {
           Applicant a = eligibleApplicants.get(i);
           System.out.printf("%d. %s (%s) | Project: %s | Room: %s%n",
                             i + 1,
                             a.getName(),
                             a.getNRIC(),
                             a.getAppliedProject().getName(),
                             a.getRoomChosen() != null ? a.getRoomChosen() : "N/A");
       }
       System.out.println("-------------------------------------------------------------");

       // 5. Prompt for NRIC from the filtered list
        System.out.print("Enter NRIC of the applicant from the list above: ");
        String applicantNRIC = scanner.nextLine().trim().toUpperCase();

        if(!isValidNRIC(applicantNRIC)) {
             System.out.println("Invalid NRIC format entered.");
             return;
        }

        boolean nricInList = eligibleApplicants.stream().anyMatch(a -> a.getNRIC().equalsIgnoreCase(applicantNRIC));
        if (!nricInList) {
            System.out.println("The entered NRIC does not belong to an eligible applicant in the list shown.");
            return;
        }

        // 6. Call BookingManager
        boolean success = bookingManager.bookFlat(this.officer, applicantNRIC);

        if (success) {
             System.out.println("Booking process completed successfully.");
        } else {
             System.out.println("Booking process failed (check previous messages for reason).");
        }
    }

    /**
     * Generates and displays a booking receipt for a selected applicant with BOOKED status.
     * Only shows applicants from projects handled by the current officer.
     */
    private void generateApplicantReceipt() {
        System.out.println("========== Generate Booking Receipt ==========");

        List<Project> handledProjects = officer.getRegisteredProjects().stream()
            .filter(p -> officer.getRegistrationStatusForProject(p) == OfficerRegistrationStatus.APPROVED)
            .collect(Collectors.toList());

        if (handledProjects.isEmpty()) {
            System.out.println("You are not currently handling any projects.");
            return;
        }

        List<Applicant> bookedApplicants = applicationManager.getAllApplicants().stream()
            .filter(a -> a.getStatus() == ApplicationStatus.BOOKED &&
                         a.getAppliedProject() != null &&
                         handledProjects.contains(a.getAppliedProject()))
            .collect(Collectors.toList());


        if (bookedApplicants.isEmpty()) {
            System.out.println("No applicants with status BOOKED under your projects.");
            return;
        }

        System.out.println("\nBooked Applicants:");
        for (int i = 0; i < bookedApplicants.size(); i++) {
            Applicant a = bookedApplicants.get(i);
            System.out.printf("%d. %s (%s) - %s\n", i + 1, a.getName(), a.getNRIC(), a.getAppliedProject().getName());
        }

        System.out.print("Select an applicant to generate receipt (Enter 0 to cancel): ");
        int choice = -1;
        try {
            choice = scanner.nextInt();
        } catch (InputMismatchException e) {
            System.out.println("Invalid input.");
            scanner.nextLine();
            return;
        }
        scanner.nextLine();

        if (choice <= 0 || choice > bookedApplicants.size()) {
            System.out.println("Invalid input. Action cancelled.");
            return;
        }

        Applicant selectedApplicant = bookedApplicants.get(choice - 1);
        Receipt receipt = bookingManager.generateBookingReceipt(selectedApplicant);

        if (receipt != null) {
            System.out.println("\n" + receipt.toFormattedString());
        } else {
            System.out.println("Failed to generate receipt for applicant " + selectedApplicant.getNRIC() + ".");
        }
    }

    /**
      * Displays the officer's profile, including basic user details,
      * their HDB Officer registration statuses, and their applicant status (if any).
      */
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
         System.out.println();
         System.out.println("============== Applicant Role =============");
         super.displayApplicantApplicationStatus(this.officer);
         System.out.println("===========================================");
     }


    // Helper Methods

    /**
     * Helper method to display a basic list of projects (Name and Status).
     * Used internally, potentially for simpler project selection scenarios.
     * @param projects List of projects to display.
     */
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

    /**
     * Helper method to display detailed information about a single project.
     * Used when viewing details of handled projects.
     * @param p The Project object to display details for.
     */
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

    /**
     * Helper method to validate NRIC format. Inherited from ApplicantUI but potentially used here.
     * @param nric The NRIC string to validate.
     * @return true if the format is valid, false otherwise.
     */
    private boolean isValidNRIC(String nric) {
        if (nric == null) return false;
        return nric.matches("^[ST]\\d{7}[A-Za-z]$");
    }
    
    /**
      * Manages user interaction for setting/viewing/removing project filters.
      * Calls FilterUI and saves changes via FilterManager. Inherited from ApplicantUI.
      */
     private void manageFilters() {
 	    FilterUI.promptFilterSettings(scanner, filter);
 	    filterManager.setFilter(officer.getNRIC(), filter);
 	    filterManager.saveFilters();
  }
}
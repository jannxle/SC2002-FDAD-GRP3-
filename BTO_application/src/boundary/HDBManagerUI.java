package boundary;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.InputMismatchException;

import auth.LoginManager;
import control.*;
import entities.*;
import enums.*;

/**
 * Provides the command-line user interface for users logged in as HDB Managers.
 * Displays a menu of available actions specific to managers, such as managing
 * projects, approving/rejecting applications and registrations, handling enquiries,
 * generating reports, and managing their own profile and password.
 */
public class HDBManagerUI {

    private final Manager manager;
    private final Scanner scanner;
    private final ProjectManager projectManager;
    private final ApplicationManager applicationManager;
    private final OfficerRegistrationManager officerRegistrationManager;
    private final EnquiryManager enquiryManager;
    private final ReportManager reportManager;
    private final UserManager<Applicant> applicantUserManager;
    private final UserManager<Officer> officerUserManager;
    private final UserManager<Manager> managerUserManager;
    private final LoginManager loginManager;
    private final Filter filter;
    private final FilterManager filterManager;
 
    /**
     * Constructs an HDBManagerUI instance.
     * Initializes the UI with the logged-in manager and necessary manager dependencies.
     *
     * @param manager                   The Manager user for this UI session.
     * @param projectManager            Manager for project data.
     * @param applicationManager        Manager for BTO applications.
     * @param officerRegistrationManager Manager for officer registrations.
     * @param enquiryManager            Manager for enquiries.
     * @param reportManager             Manager for report generation.
     * @param applicantUserManager      Manager for applicant user data.
     * @param officerUserManager        Manager for officer user data.
     * @param managerUserManager        Manager for manager user data.
     * @param loginManager              Manager to handle logout.
     * @param filterManager             Manager for user view filters.
     */
    public HDBManagerUI(Manager manager,
                        ProjectManager projectManager,
                        ApplicationManager applicationManager,
                        OfficerRegistrationManager officerRegistrationManager,
                        EnquiryManager enquiryManager,
                        ReportManager reportManager,
                        UserManager<Applicant> applicantUserManager,
                        UserManager<Officer> officerUserManager,
                        UserManager<Manager> managerUserManager,
                        LoginManager loginManager,
                        FilterManager filterManager) {
        this.manager = manager;
        this.projectManager = projectManager;
        this.applicationManager = applicationManager;
        this.officerRegistrationManager = officerRegistrationManager;
        this.enquiryManager = enquiryManager;
        this.reportManager = reportManager;
        this.applicantUserManager = applicantUserManager;
        this.officerUserManager = officerUserManager;
        this.managerUserManager = managerUserManager;
        this.loginManager = loginManager;
        this.filterManager = filterManager;
        this.filter = filterManager.getFilter(manager.getNRIC());
        this.scanner = new Scanner(System.in);
    }

    /**
     * Displays the main menu for the HDB Manager dashboard and handles user input.
     * Loops until the user chooses to log out, calling appropriate methods for each action.
     */
    public void showMenu() {
        boolean logout = false;
        while (!logout) {
            System.out.println("\n============== HDB Manager Dashboard =============");
            System.out.println(" User: " + manager.getName() + " (" + manager.getNRIC() + ")");
            System.out.println("--------------------------------------------------");
            System.out.println("--------------- Project Management ---------------");
            System.out.println(" 1. Create New BTO Project Listing");
            System.out.println(" 2. View/Edit/Delete Project Listings");
            System.out.println(" 3. Toggle Project Visibility");
            System.out.println(" 4. Set/View/Remove Filters for Viewing Project Listings");
            System.out.println();
            System.out.println("------ Registration & Application Management ------");
            System.out.println(" 5. View/Approve/Reject Officer Registrations");
            System.out.println(" 6. View/Approve/Reject BTO Applications");
            System.out.println(" 7. Process Application Withdrawals");
            System.out.println();
            System.out.println("--------------- Enquiries & Reports ---------------");
            System.out.println(" 8. View All Enquiries");
            System.out.println(" 9. Reply to Enquiries (for projects handled)");
            System.out.println(" 10. Generate Applicant Booking Report");
            System.out.println();
            System.out.println("--------------------- Account ---------------------");
            System.out.println(" 11. Change Password");
            System.out.println(" 12. View My Profile");
            System.out.println(" 13. Logout");
            System.out.println("===================================================");
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
                case 1: createProject(); break;
                case 2: manageProjects(); break;
                case 3: toggleProjectVisibility(); break;
                case 4: manageFilters(); break;
                case 5: manageOfficerRegistrations(); break;
                case 6: manageBTOApplications(); break;
                case 7: manageApplicationWithdrawals(); break;
                case 8: viewAllEnquiries(); break;
                case 9: replyToMyProjectEnquiries();break;
                case 10: generateBookingReport(); break;
                case 11: changePassword(); break;
                case 12: viewManagerProfile(); break;
                case 13: logout = true; loginManager.logout(this.manager); break;
                default: System.out.println("Invalid option. Please try again.");
            }
             if (!logout) {
                 System.out.println("\nPress Enter to return to the menu...");
                 scanner.nextLine();
             }
        }
         System.out.println("Exiting Manager Dashboard.");
    }

    // --- Private Helper Methods for Menu Options ---

    /**
     * Guides the manager through creating a new BTO project listing.
     * Prompts for project details, validates input (name uniqueness, dates, slots),
     * checks for manager date conflicts, and adds the project via ProjectManager.
     */
    private void createProject() {
        System.out.println("============== Create New BTO Listing =============");
        try {
            System.out.print("Enter Project Name: ");
            String name = scanner.nextLine().trim();
            if (name.isEmpty() || projectManager.findProjectByName(name) != null) {
                System.out.println("Project name cannot be empty or already exists. Creation cancelled.");
                return;
            }

            System.out.print("Enter Neighbourhood: ");
            String neighbourhood = scanner.nextLine().trim();

            Room room2 = createRoomInput(RoomType.TwoRoom);
            Room room3 = createRoomInput(RoomType.ThreeRoom);
            List<Room> rooms = new ArrayList<>();
            if (room2 != null) rooms.add(room2);
            if (room3 != null) rooms.add(room3);
            if (rooms.isEmpty()) {
                 System.out.println("At least one room type must be specified. Creation cancelled.");
                 return;
            }

            LocalDate openDate = promptForDate("Enter Application Opening Date (dd/MM/yy): ");
            LocalDate closeDate = promptForDate("Enter Application Closing Date (dd/MM/yy): ");
             if (openDate == null || closeDate == null || closeDate.isBefore(openDate)) {
                 System.out.println("Invalid date range. Closing date must be on or after opening date. Creation cancelled.");
                 return;
             }

            if (isManagerHandlingOverlappingProject(openDate, closeDate)) {
                System.out.println("You are already managing a project with an overlapping application period. Cannot create this project.");
                return;
            }

            System.out.print("Enter Max Number of HDB Officer Slots (max 10): ");
            int slots = -1;
             try {
                 slots = scanner.nextInt();
            } catch (InputMismatchException e) {}
             finally {
                 scanner.nextLine();
            }

            if (slots < 0 || slots > 10) {
                System.out.println("Invalid number of slots (must be 0-10). Creation cancelled.");
                return;
            }

            Project newProject = new Project(
                name, neighbourhood, openDate, closeDate, this.manager.getName(),
                slots, rooms, false, null
            );

            if (projectManager.addProject(newProject)) {
                projectManager.saveProjects("data/ProjectList.csv");
                System.out.println("Project '" + name + "' created successfully with visibility OFF.");
            }

        } catch (Exception e) {
            System.err.println("An error occurred during project creation: " + e.getMessage());
            if (scanner.hasNextLine()) scanner.nextLine();
        }
    }

    /**
     * Helper method to prompt for and create a Room object based on user input.
     * @param type The RoomType to get details for.
     * @return A Room object, or null if input is invalid or user enters 0 units.
     */
    private Room createRoomInput(RoomType type) {
         System.out.println("=============== Details for " + type.name() + " ===============");
         System.out.print("Enter Total Number of Units (0 if none): ");
         int totalUnits = -1;
         double price = -1.0;
         try {
             totalUnits = scanner.nextInt();
             if (totalUnits <= 0) {
                  System.out.println("Skipping " + type.name());
                  return null;
             }
             System.out.print("Enter Selling Price per Unit: ");
             price = scanner.nextDouble();
             if (price < 0) {
                  System.out.println("Price cannot be negative. Skipping " + type.name());
                  return null;
             }
         } catch (InputMismatchException e) {
              System.err.println("Invalid number format. Skipping " + type.name());
              return null;
         } finally {
              scanner.nextLine();
         }
         return new Room(type, totalUnits, totalUnits, price);
    }

    /**
      * Helper method to prompt the user for a date and parse it.
      * @param prompt The message to display to the user.
      * @return The parsed LocalDate, or null if the format is invalid.
      */
     private LocalDate promptForDate(String prompt) {
         System.out.print(prompt);
         String dateStr = scanner.nextLine();
         try {
             DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/M/yy");
             return LocalDate.parse(dateStr, formatter);
         } catch (DateTimeParseException e) {
             System.err.println("Invalid date format. Please use dd/MM/yy.");
             return null;
         }
     }

     /**
     * Checks if the current manager is already managing another project
     * whose application period overlaps with the given new period.
     * @param newOpen The start date of the new period.
     * @param newClose The end date of the new period.
     * @return true if an overlap exists, false otherwise.
     */
    private boolean isManagerHandlingOverlappingProject(LocalDate newOpen, LocalDate newClose) {
        if (newOpen == null || newClose == null) return false;

        List<Project> allProjects = projectManager.getProjects();
        for (Project p : allProjects) {
            if (p.getManager() != null && p.getManager().equalsIgnoreCase(this.manager.getName())) {
                LocalDate existingOpen = p.getOpenDate();
                LocalDate existingClose = p.getCloseDate();
                // Check overlap
                if (existingOpen != null && existingClose != null &&
                    !newOpen.isAfter(existingClose) && !newClose.isBefore(existingOpen))
                {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Manages viewing, editing, and deleting project listings.
     * Allows filtering view by 'All Projects' or 'My Projects'.
     * Applies user-defined filters. Prompts for action on a selected project.
     */
    private void manageProjects() {
        System.out.println("============ View/Edit/Delete Projects ============");
        System.out.println("Choose an option:");
        System.out.println("1. View All Projects");
        System.out.println("2. View Only My Projects");
        System.out.println();
        System.out.print("Enter choice: ");
        int viewChoice = -1;
        try { viewChoice = scanner.nextInt(); } catch (InputMismatchException e) {}
        scanner.nextLine();

        List<Project> projectsToDisplay;
        if (viewChoice == 2) {
            projectsToDisplay = projectManager.getProjects().stream()
                                     .filter(p -> p.getManager() != null && p.getManager().equalsIgnoreCase(this.manager.getName()))
                                     .collect(Collectors.toList());
             System.out.println("\n========== Projects Managed By You ==========");
        } else {
            projectsToDisplay = projectManager.getProjects();
            System.out.println("\n================ All Projects ===============");
        }
        
        projectsToDisplay = ViewProjectFilter.apply(projectsToDisplay, filter);

        if (projectsToDisplay.isEmpty()) {
            System.out.println("No projects found matching your criteria.");
            return;
        }

        displayProjectListDetailed(projectsToDisplay);

        System.out.print("\nEnter the name of the project to Edit or Delete (or press Enter to cancel): ");
        String projectName = scanner.nextLine().trim();

        if (projectName.isEmpty()) { System.out.println("Action cancelled."); return; }

        Project projectToManage = projectManager.findProjectByName(projectName);

        if (projectToManage == null) { System.out.println("Project '" + projectName + "' not found."); return; }

        // Check ownership for Edit/Delete
        if (projectToManage.getManager() == null || !projectToManage.getManager().equalsIgnoreCase(this.manager.getName())) {
             System.out.println("You can only Edit or Delete projects created by you.");
             return;
        }

        System.out.println("Selected Project: " + projectToManage.getName());
        System.out.println("Choose action:");
        System.out.println("1. Edit Project");
        System.out.println("2. Delete Project");
        System.out.println("0. Cancel");
        System.out.println();
        System.out.print("Enter choice: ");
        int actionChoice = -1;
        try { actionChoice = scanner.nextInt(); } catch (InputMismatchException e) {}
        scanner.nextLine();

        if (actionChoice == 1) { editProject(projectToManage); }
        else if (actionChoice == 2) { deleteProject(projectToManage); }
        else { System.out.println("Action cancelled."); }
    }

    /**
      * Helper method to display a detailed list of projects.
      * @param projects The list of Project objects to display.
      */
     private void displayProjectListDetailed(List<Project> projects) {
        System.out.println("---------------------------------------------------------------------------------------------------------------------------");
        System.out.printf(" %-25s | %-15s | %-10s | %-10s | %-10s | %-5s | %-15s | %s%n",
                          "Project Name", "Neighbourhood", "Open Date", "Close Date", "Manager", "Slots", "Officer", "Visibility");
        System.out.println("---------------------------------------------------------------------------------------------------------------------------");
        if (projects.isEmpty()) {
             System.out.println(" < No Projects >");
        } else {
             DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yy");
             for(Project p : projects) {
                  System.out.printf(" %-25s | %-15s | %-10s | %-10s | %-10s | %-5d | %-15s | %s%n",
                      p.getName(), p.getNeighbourhood(),
                      p.getOpenDate() != null ? p.getOpenDate().format(dtf) : "N/A",
                      p.getCloseDate() != null ? p.getCloseDate().format(dtf) : "N/A",
                      p.getManager() != null ? p.getManager() : "N/A",
                      p.getOfficerSlot(), p.getOfficer() != null ? p.getOfficer() : "<None>",
                      p.isVisibility() ? "ON" : "OFF");
                  if (p.getRooms() != null) {
                      for (Room r : p.getRooms()) {
                           System.out.printf("   -> %-8s | Units: %-4d | Avail: %-4d | Price: $%.2f%n", r.getRoomType(), r.getTotalRooms(), r.getAvailableRooms(), r.getPrice());
                      }
                  }
             }
        }
         System.out.println("---------------------------------------------------------------------------------------------------------------------------");
    }

    /**
     * Handles editing specific fields of a project managed by the current manager.
     * @param project The Project object to edit.
     */
    private void editProject(Project project) {
    	System.out.println();
        System.out.println("=========== Edit Project: " + project.getName() + " ===========");
        System.out.println("Current project details:");
        System.out.println("1. Neighbourhood: " + project.getNeighbourhood());
        System.out.println("2. Open Date: " + (project.getOpenDate() != null ? 
            project.getOpenDate().format(DateTimeFormatter.ofPattern("dd/MM/yy")) : "N/A"));
        System.out.println("3. Close Date: " + (project.getCloseDate() != null ? 
            project.getCloseDate().format(DateTimeFormatter.ofPattern("dd/MM/yy")) : "N/A"));
        System.out.println("4. Officer Slots: " + project.getOfficerSlot());
        System.out.println("5. Number of Two Room Flats: " + project.getRoomCount(RoomType.TwoRoom));
        System.out.println("6. Number of Three Room Flats: " + project.getRoomCount(RoomType.ThreeRoom));
        System.out.println("--------------------------------------------------");
        System.out.print("Select field to edit (Press 0 to exit): ");
        int field = -1;
        try { field = scanner.nextInt(); } catch (InputMismatchException e) {}
        scanner.nextLine();
        
        switch(field) {
            case 1:
                System.out.print("Enter new Neighbourhood: ");
                String newNeighbourhood = scanner.nextLine().trim();
                if (!newNeighbourhood.isEmpty()) {
                    project.setNeighbourhood(newNeighbourhood);
                    System.out.println("Neighbourhood updated.");
                }
                break;
            case 2:
                LocalDate newOpenDate = promptForDate("Enter new Open Date (dd/MM/yy): ");
                if (newOpenDate != null) {
                    if (projectManager.hasDateConflict(project, project.getManager(), newOpenDate, project.getCloseDate())) {
                        System.out.println("Date conflict: Another project by you overlaps with the new open date.");
                    } else {
                        project.setOpenDate(newOpenDate);
                        System.out.println("Open Date updated.");
                    }
                }
                break;
            case 3:
                LocalDate newCloseDate = promptForDate("Enter new Close Date (dd/MM/yy): ");
                if (newCloseDate != null) {
                    if (projectManager.hasDateConflict(project, project.getManager(), project.getOpenDate(), newCloseDate)) {
                        System.out.println("Date conflict: Another project by you overlaps with the new close date.");
                    } else {
                        project.setCloseDate(newCloseDate);
                        System.out.println("Close Date updated.");
                    }
                }
                break;
            case 4:
                System.out.print("Enter new Officer Slots (0-10): ");
                int newSlots = -1;
                try { newSlots = scanner.nextInt(); } catch (InputMismatchException e) {}
                scanner.nextLine();
                
                if (newSlots >= 0 && newSlots <= 10) {
                    project.setOfficerSlot(newSlots);
                    System.out.println("Officer Slots updated.");
                } else {
                    System.out.println("Invalid number of slots. Must be between 0 and 10.");
                }
                break;
            case 5:
            	System.out.print("Enter number of additional Two Room Flats to add: ");
                int addTwoRoom = -1;
                try {
                    addTwoRoom = scanner.nextInt();
                } catch (InputMismatchException e) {}
                scanner.nextLine();

                if (addTwoRoom > 0) {
                    Room twoRoom = project.getRoom(RoomType.TwoRoom);
                    if (twoRoom != null) {
                        twoRoom.increaseRoomSupply(addTwoRoom);
                        System.out.println("Two Room flat supply updated (+" + addTwoRoom + ").");
                        projectManager.saveProjects("data/ProjectList.csv"); // persist changes
                    } else {
                        System.out.println("Two Room type not found in this project.");
                    }
                } else {
                    System.out.println("Invalid input. Must be a positive number.");
                }
                break;
            case 6:
               	System.out.print("Enter number of additional Three Room Flats to add: ");
                int addThreeRoom = -1;
                try {
                    addThreeRoom = scanner.nextInt();
                } catch (InputMismatchException e) {}
                scanner.nextLine();

                if (addThreeRoom > 0) {
                    Room twoRoom = project.getRoom(RoomType.ThreeRoom);
                    if (twoRoom != null) {
                        twoRoom.increaseRoomSupply(addThreeRoom);
                        System.out.println("Three Room flat supply updated (+" + addThreeRoom + ").");
                        projectManager.saveProjects("data/ProjectList.csv"); // persist changes
                    } else {
                        System.out.println("Three Room type not found in this project.");
                    }
                } else {
                    System.out.println("Invalid input. Must be a positive number.");
                }
                break;
            case 0:
                System.out.println("Edit cancelled.");
                return;
            default:
                System.out.println("Invalid option.");
                return;
        }
        
        // Save changes
        System.out.println();
        projectManager.saveProjects("data/ProjectList.csv");
    }

    /**
     * Handles deleting a project managed by the current manager.
     * Confirms deletion and updates related data (applications, officer registrations).
     * @param project The Project object to delete.
     */
    private void deleteProject(Project project) {
        System.out.println("=========== Delete Project: " + project.getName() + " ===========");
        System.out.print("Are you sure you want to permanently delete project '" + project.getName() + "'? (Y/N): ");
        String confirmation = scanner.nextLine().trim().toUpperCase();
        if (confirmation.equals("Y")) {
        	if (projectManager.deleteProject(project.getName())) {
        	    projectManager.saveProjects("data/ProjectList.csv");

        	    // Combine all applicants (including officers)
        	    List<Applicant> allApplicants = new ArrayList<>();
        	    allApplicants.addAll(applicantUserManager.getUsers());
        	    allApplicants.addAll(officerUserManager.getUsers());

        	    // Remove applications associated with the deleted project so that it will reflect in applicant
        	    for (Applicant a : allApplicants) {
        	        if (a.getAppliedProject() != null &&
        	            a.getAppliedProject().getName().equalsIgnoreCase(project.getName())) {
        	            a.setAppliedProject(null);
        	            a.setRoomChosen(null);
        	            a.setStatus(null);
        	        }
        	    }

        	    // Save updated applications.csv
        	    applicationManager.saveApplications("data/applications.csv", allApplicants);
        	    
        	    List<Officer> allOfficers = officerUserManager.getUsers();

        	    for (Officer officer : allOfficers) {
        	        List<Project> toRemove = new ArrayList<>();

        	        for (Project registered : officer.getRegisteredProjects()) {
        	            if (registered.getName().equalsIgnoreCase(project.getName())) {
        	                toRemove.add(registered);  // avoid concurrent modification
        	            }
        	        }

        	        for (Project p : toRemove) {
        	            officer.removeRegisteredProject(p);  // custom method we added earlier
        	        }
        	    }

        	    // Save the updated OfficerList.csv
        	    officerUserManager.saveUsers();

        	    System.out.println("Project deleted successfully.");
        	}
        } else {
            System.out.println("Deletion cancelled.");
        }
    }

    /**
      * Handles toggling the visibility of a project managed by the current manager.
      */
     private void toggleProjectVisibility() {
        System.out.println("============ Toggle Project Visibility ============");
        List<Project> myProjects = projectManager.getProjects().stream()
                                     .filter(p -> p.getManager() != null && p.getManager().equalsIgnoreCase(this.manager.getName()))
                                     .collect(Collectors.toList());

        if (myProjects.isEmpty()) { System.out.println("You are not managing any projects."); return; }

        System.out.println("Projects Managed By You:");
        displayProjectListDetailed(myProjects);

        System.out.print("Enter the name of the project to toggle visibility: ");
        String projectName = scanner.nextLine().trim();
        Project project = projectManager.findProjectByName(projectName);

        if (project == null || !myProjects.contains(project)) {
             System.out.println("Project '" + projectName + "' not found or you are not the manager.");
             return;
        }

        boolean newVisibility = !project.isVisibility();

        if (projectManager.setProjectVisibility(projectName, newVisibility)) {
             projectManager.saveProjects("data/ProjectList.csv");
             System.out.println("Visibility for '" + projectName + "' changed to " + (newVisibility ? "ON" : "OFF") + ".");
        }
    }

    /**
     * Manages viewing and approving/rejecting pending HDB officer registration requests
     * for projects handled by the current manager.
     */
    private void manageOfficerRegistrations() {
        System.out.println("========== Manage Officer Registrations ===========");
        List<Project> myProjects = projectManager.getProjects().stream()
                                     .filter(p -> p.getManager() != null && p.getManager().equalsIgnoreCase(this.manager.getName()))
                                     .collect(Collectors.toList());

        if (myProjects.isEmpty()) { System.out.println("You are not managing any projects."); return; }

        System.out.println("Select a project to view registrations:");
        for (int i = 0; i < myProjects.size(); i++) { System.out.println((i + 1) + ". " + myProjects.get(i).getName()); }
        System.out.println("0. Cancel");
        System.out.print("Enter choice: ");
        int choice = -1;
        try { choice = scanner.nextInt(); } catch (InputMismatchException e) {}
        scanner.nextLine();

        if (choice <= 0 || choice > myProjects.size()) { System.out.println("Action cancelled."); return; }
        Project selectedProject = myProjects.get(choice - 1);

        List<Officer> pendingOfficers = officerRegistrationManager.getPendingRegistrationsForProject(selectedProject);

        if (pendingOfficers.isEmpty()) { System.out.println("No pending officer registrations for project '" + selectedProject.getName() + "'."); return; }

        System.out.println("\n--------- Pending Registrations for: " + selectedProject.getName() + " ---------");
        for (int i = 0; i < pendingOfficers.size(); i++) {
              Officer officer = pendingOfficers.get(i);
              System.out.println("Registration #" + (i + 1));
              System.out.println(" Officer Name: " + officer.getName());
              System.out.println(" Officer NRIC: " + officer.getNRIC());
              System.out.println("--------------------------------------------------");
        }

        System.out.print("Select registration # to Approve/Reject (Enter 0 to cancel): ");
        int regChoice = -1;
        try { regChoice = scanner.nextInt(); } catch (InputMismatchException e) {}
        scanner.nextLine();

        if (regChoice <= 0 || regChoice > pendingOfficers.size()) { System.out.println("Action cancelled."); return; }
        Officer officerToProcess = pendingOfficers.get(regChoice - 1);

        System.out.println("Action for Officer " + officerToProcess.getNRIC() + ":");
        System.out.println("1. Approve Registration");
        System.out.println("2. Reject Registration");
        System.out.println("0. Cancel");
        System.out.print("Enter choice: ");
        int actionChoice = -1;
        try { actionChoice = scanner.nextInt(); } catch (InputMismatchException e) {}
        scanner.nextLine();

        boolean success = false;
        if (actionChoice == 1) {
              success = officerRegistrationManager.approveRegistration(this.manager, officerToProcess);
              if (success) { System.out.println("Registration approved."); } else { System.out.println("Approval failed."); }
        } else if (actionChoice == 2) {
              success = officerRegistrationManager.rejectRegistration(this.manager, officerToProcess);
               if (success) { System.out.println("Registration rejected."); } else { System.out.println("Rejection failed."); }
        } else {
              System.out.println("Action cancelled.");
        }
    }

    /**
     * Manages viewing and approving/rejecting PENDING BTO applications
     * for projects handled by the current manager.
     */
    private void manageBTOApplications() {
        System.out.println("============= Manage BTO Applications =============");
         List<Project> myProjects = projectManager.getProjects().stream()
                                     .filter(p -> p.getManager() != null && p.getManager().equalsIgnoreCase(this.manager.getName()))
                                     .collect(Collectors.toList());

         if (myProjects.isEmpty()) { System.out.println("You are not managing any projects."); return; }

         System.out.println("Select a project to view applications:");
          for (int i = 0; i < myProjects.size(); i++) { System.out.println((i + 1) + ". " + myProjects.get(i).getName()); }
         System.out.println("0. Cancel");
         System.out.print("Enter choice: ");
         int choice = -1;
         try { choice = scanner.nextInt(); } catch (InputMismatchException e) {}
         scanner.nextLine();

          if (choice <= 0 || choice > myProjects.size()) { System.out.println("Action cancelled."); return; }
         Project selectedProject = myProjects.get(choice - 1);

        List<Applicant> allApplicants = applicantUserManager.getUsers();

        List<Applicant> pendingApplicants = allApplicants.stream()
            .filter(a -> a.getStatus() == ApplicationStatus.PENDING &&
                         a.getAppliedProject() != null &&
                         a.getAppliedProject().getName().equalsIgnoreCase(selectedProject.getName()))
            .collect(Collectors.toList());

         if (pendingApplicants.isEmpty()) { System.out.println("No pending BTO applications for project '" + selectedProject.getName() + "'."); return; }

         System.out.println("\n------ Pending BTO Applications for: " + selectedProject.getName() + " ------");
          for (int i = 0; i < pendingApplicants.size(); i++) {
              Applicant app = pendingApplicants.get(i);
              System.out.println("Application #" + (i + 1));
              System.out.println(" Applicant Name: " + app.getName());
              System.out.println(" Applicant NRIC: " + app.getNRIC());
              System.out.println(" Chosen Room:    " + app.getRoomChosen());
              System.out.println("--------------------------------------------------");
         }

         System.out.print("Select application # to Approve/Reject (Enter 0 to cancel): ");
         int appChoice = -1;
         try { appChoice = scanner.nextInt(); } catch (InputMismatchException e) {}
         scanner.nextLine();

         if (appChoice <= 0 || appChoice > pendingApplicants.size()) { System.out.println("Action cancelled."); return; }
         Applicant applicantToProcess = pendingApplicants.get(appChoice - 1);

        System.out.println("--------------------------------------------------");
        System.out.println("Action for Applicant " + applicantToProcess.getNRIC() + ":");
        System.out.println("1. Approve Application");
        System.out.println("2. Reject Application");
        System.out.println("0. Cancel");
        System.out.print("Enter choice: ");
        int actionChoice = -1;
        try { actionChoice = scanner.nextInt(); } catch (InputMismatchException e) {}
        scanner.nextLine();

        boolean success = false;
        if (actionChoice == 1) {
             success = applicationManager.approveApplication(applicantToProcess);
             if (success) System.out.println("Application approved action completed.");
             else System.out.println("Approval action failed or had issues (check logs).");
        } else if (actionChoice == 2) {
             success = applicationManager.rejectApplication(applicantToProcess);
             if (success) System.out.println("Application rejected action completed.");
             else System.out.println("Rejection action failed.");
        } else {
            System.out.println("Action cancelled.");
        }
    }

    /**
     * Manages processing application withdrawal requests (status PENDING_WITHDRAWAL)
     * for projects handled by the current manager. Allows approving or rejecting the withdrawal.
     */
    private void manageApplicationWithdrawals() {
        System.out.println("========= Process Application Withdrawals =========");

        //get names of projects managed by this manager
         List<Applicant> applicantsToReview = new ArrayList<>();
         List<Project> myProjects = projectManager.getProjects().stream()
                                     .filter(p -> p.getManager() != null && p.getManager().equalsIgnoreCase(this.manager.getName()))
                                     .collect(Collectors.toList());
         List<String> myProjectNames = myProjects.stream().map(Project::getName).collect(Collectors.toList());

         
         //combine all applicants including officers
         Stream.concat(applicantUserManager.getUsers().stream(), officerUserManager.getUsers().stream())
               .map(user -> (Applicant) user)
               .filter(app -> app.getAppliedProject() != null &&
                              myProjectNames.contains(app.getAppliedProject().getName()) &&
                              app.getStatus() == ApplicationStatus.PENDING_WITHDRAWAL)
               .forEach(applicantsToReview::add);


        if (applicantsToReview.isEmpty()) {
            System.out.println("No applications with PENDING_WITHDRAWAL status for your projects requiring withdrawal management.");
            return;
        }

        System.out.println("\n====== Applications Eligible for Withdrawal Processing ======");
        for (int i = 0; i < applicantsToReview.size(); i++) {
             Applicant app = applicantsToReview.get(i);
             System.out.printf("%d. NRIC: %s | Name: %s | Project: %s | Status: %s%n",
                              (i + 1), app.getNRIC(), app.getName(), app.getAppliedProject().getName(), app.getStatus());
        }

        System.out.print("Select application # to process withdrawal for (Sets status to SUCCESSFUL_WITHDRAWAL) (Enter 0 to cancel): ");
        int choice = -1;
        try { choice = scanner.nextInt(); } catch (InputMismatchException e) {}
        scanner.nextLine();

        if (choice <= 0 || choice > applicantsToReview.size()) {
             System.out.println("Action cancelled.");
             return;
        }
        Applicant applicantToProcess = applicantsToReview.get(choice - 1);
        Project project = applicantToProcess.getAppliedProject();
        RoomType room = applicantToProcess.getRoomChosen();
        
        System.out.println("--------------------------------------------------");
        System.out.println("Selected Applicant: " + applicantToProcess.getNRIC());
        System.out.println("1. Approve Withdrawal");
        System.out.println("2. Reject Withdrawal");
        System.out.println("--------------------------------------------------");
        System.out.print("Enter your choice: ");

        int decision = -1;
        try { decision = scanner.nextInt(); } catch (InputMismatchException e) {}
        scanner.nextLine();

        boolean success = false;
        if (decision == 1) {
            success = applicationManager.approveWithdrawal(applicantToProcess);
            if (success) {
                System.out.println("Withdrawal approval processed.");
            } else {
                System.out.println("Withdrawal approval failed (check logs).");
            }
        } else if (decision == 2) {
             success = applicationManager.rejectWithdrawal(applicantToProcess);
             if (success) {
                 System.out.println("Withdrawal rejection processed.");
             } else {
                  System.out.println("Withdrawal rejection failed (check logs).");
             }
        } else {
            System.out.println("Invalid choice. Action cancelled.");
            return;
        }
    }

    /**
     * Displays all enquiries submitted across all projects in the system.
     */
    private void viewAllEnquiries() {
        System.out.println("================ View All Enquiries ================");
        List<Enquiry> allEnquiries = enquiryManager.getAllEnquiries();
        if (allEnquiries.isEmpty()) { System.out.println("No enquiries found in the system."); return; }

        System.out.println("--------------------------------------------------");
        for (int i = 0; i < allEnquiries.size(); i++) {
             Enquiry e = allEnquiries.get(i);
             System.out.println("Enquiry #" + (i + 1));
             System.out.println(" Project:   " + e.getProjectName());
             System.out.println(" Name: " + e.getApplicantName() +" ("+e.getApplicantNRIC()+")");
             System.out.println(" Message:   " + e.getMessage());
             System.out.println(" Reply:     " + (e.getReply() == null || e.getReply().isEmpty() ? "<No Reply Yet>" : e.getReply()));
             System.out.println("--------------------------------------------------");
        }
    }

    /**
     * Allows the manager to reply to enquiries specifically for the projects they handle.
     * Displays relevant enquiries and prompts for selection and reply message.
     */
    private void replyToMyProjectEnquiries() {
         System.out.println("====== Reply to Enquiries for Projects You Handle ======");
         List<Project> myProjects = projectManager.getProjects().stream()
                                     .filter(p -> p.getManager() != null && p.getManager().equalsIgnoreCase(this.manager.getName()))
                                     .collect(Collectors.toList());

         if (myProjects.isEmpty()) { System.out.println("You are not managing any projects."); return; }

         List<Enquiry> relevantEnquiries = new ArrayList<>();
         System.out.println("Enquiries for projects you handle:");
         System.out.println("--------------------------------------------------");
         int count = 0;
         for (Project p : myProjects) {
              List<Enquiry> projectEnquiries = enquiryManager.getEnquiriesByProject(p.getName());
              for (Enquiry e : projectEnquiries) {
                  count++;
                  relevantEnquiries.add(e);
                   System.out.println("Enquiry #" + count + " (Project: " + e.getProjectName() + ")");
                   System.out.println(" Name: " + e.getApplicantName() +" ("+e.getApplicantNRIC()+")");
                   System.out.println(" Message:   " + e.getMessage());
                   System.out.println(" Reply:     " + (e.getReply() == null || e.getReply().isEmpty() ? "<No Reply Yet>" : e.getReply()));
                   System.out.println("--------------------------------------------------");
              }
         }

          if (relevantEnquiries.isEmpty()) { System.out.println("No enquiries found for the projects you handle."); return; }

         System.out.print("Select enquiry # to Reply (Enter 0 to cancel): ");
         int choice = -1;
         try { choice = scanner.nextInt(); } catch (InputMismatchException e) {}
         scanner.nextLine();

         if (choice <= 0 || choice > relevantEnquiries.size()) { System.out.println("Action cancelled."); return; }
         Enquiry enquiryToReply = relevantEnquiries.get(choice - 1);

         if (enquiryToReply.getReply() != null && !enquiryToReply.getReply().isEmpty()) {
            System.out.println("This enquiry has already been replied to.");
            System.out.print("Do you want to overwrite the existing reply? (Y/N): ");
            String confirmation = scanner.nextLine().trim().toUpperCase();
            if (!confirmation.equals("Y")) { System.out.println("Reply cancelled."); return; }
        }

         System.out.print("Enter your reply: ");
         String reply = scanner.nextLine();

         enquiryManager.replyToEnquiry(enquiryToReply, reply, manager.getName());
         enquiryToReply.setReply(reply, manager.getName());
         enquiryManager.saveEnquiries();
         System.out.println("Reply submitted successfully.");

    }

    /**
     * Generates and displays a report of booked applicants based on selected filters.
     * Uses ReportManager to generate the report data.
     */
    private void generateBookingReport() {
        System.out.println("========== Generate Applicant Booking Report ==========");
        if (reportManager != null) {
            System.out.println("Available Filters (Example):");
            System.out.println("1. All Booked Applicants");
            System.out.println("2. Filter by Marital Status");
            System.out.println("3. Filter by Flat Type");
            System.out.print("Choose filter type (or 1 for all): ");
            int filterChoice = -1;
            try { filterChoice = scanner.nextInt(); } catch (InputMismatchException e) {}
            scanner.nextLine();

            ReportManager.FilterCriteria criteria = new ReportManager.FilterCriteria();

            if (filterChoice == 2) {
                System.out.print("Enter Marital Status to filter (Married/Single): ");
                String status = scanner.nextLine().trim();
                if (status.equalsIgnoreCase("Married")) { criteria.setMaritalStatusFilter(true); }
                else if (status.equalsIgnoreCase("Single")) { criteria.setMaritalStatusFilter(false); }
                else { System.out.println("Invalid status, showing all."); }
            } else if (filterChoice == 3) {
                 System.out.print("Enter Flat Type to filter (TwoRoom/ThreeRoom): ");
                 String typeStr = scanner.nextLine().trim();
                 try { criteria.setRoomTypeFilter(RoomType.valueOf(typeStr)); }
                 catch (IllegalArgumentException e) { System.out.println("Invalid flat type, showing all."); }
            }

            Report report = reportManager.generateBookingReport(criteria);

            System.out.println("\n--- Booking Report ---");
            if (report != null) {
                System.out.println(report.toFormattedString());
            } else {
                 System.out.println("Failed to generate report.");
            }
            System.out.println("--- End of Report ---");
        } else {
            System.out.println("Report generation functionality is not available (ReportManager not configured).");
        }
    }

    /**
     * Handles changing the manager's password.
     * Prompts for current and new passwords, validates, and calls ManagerUserManager.
     * Redirects to login upon successful change.
     */
    private void changePassword() {
         System.out.println("=========== Change Password ==========");
         System.out.print("Enter current password: ");
         String currentPassword = scanner.nextLine();

        if (!manager.verifyPassword(currentPassword)) {
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

        boolean success = managerUserManager.changePassword(manager.getNRIC(), newPassword);

        if (success) { 
        	System.out.println("Password changed successfully."); 
        	loginManager.login();
        }
        else { 
        	System.out.println("Password change failed."); 
        }
    }

    /**
     * Displays the manager's profile information, including basic details and projects managed.
     */
    private void viewManagerProfile() {
        System.out.println("========== Manager Profile ==========");
        System.out.println(" Name:           " + manager.getName());
        System.out.println(" NRIC:           " + manager.getNRIC());
        System.out.println(" Age:            " + manager.getAge());
        System.out.println(" Marital Status: " + (manager.isMarried() ? "Married" : "Single"));
        System.out.println(" Role:           " + manager.getRole());

        List<Project> myProjects = projectManager.getProjects().stream()
                                     .filter(p -> p.getManager() != null && p.getManager().equalsIgnoreCase(this.manager.getName()))
                                     .collect(Collectors.toList());
        if (!myProjects.isEmpty()) {
             System.out.println("\n--- Projects Currently Managed ---");
             for (Project p : myProjects) {
                  System.out.println(" - " + p.getName() + " (Opens: " + p.getOpenDate() + ", Closes: " + p.getCloseDate() + ")");
             }
        } else {
             System.out.println("\nNot currently managing any active projects.");
        }
        System.out.println("===================================");
    }
    
    /**
     * Manages user interaction for setting/viewing/removing project view filters for the manager.
     * Calls FilterUI and saves changes via FilterManager.
     */
    private void manageFilters() {
        FilterUI.promptFilterSettings(scanner, filter);
        filterManager.setFilter(manager.getNRIC(), filter);
        filterManager.saveFilters();
    }
}
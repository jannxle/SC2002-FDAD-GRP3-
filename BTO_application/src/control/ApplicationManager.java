package control;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import entities.Applicant;
import entities.Officer;
import entities.Project;
import entities.Room;
import enums.RoomType;
import utils.FileManager;
import enums.ApplicationStatus;

/**
 * Manages the lifecycle of BTO applications within the system.
 * This includes handling application submission, approval, rejection, withdrawal requests,
 * and persisting application data to/from a CSV file. It interacts with ProjectManager
 * to update room availability and UserManagers to update applicant statuses.
 */
public class ApplicationManager {

    private static final String APPLICATIONS_FILE_PATH = "data/Applications.csv";
    private final ProjectManager projectManager;
    private final UserManager<Applicant> applicantUserManager;
    private UserManager<Officer> officerUserManager;

    /**
     * Constructs an ApplicationManager.
     * Requires instances of ProjectManager, ApplicantUserManager, and OfficerUserManager.
     *
     * @param projectManager       The manager for project data.
     * @param applicantUserManager The manager for applicant user data.
     * @param officerUserManager   The manager for officer user data.
     * @throws IllegalArgumentException if any manager dependency is null.
     */
    public ApplicationManager(ProjectManager projectManager, UserManager<Applicant> applicantUserManager, UserManager<Officer> officerUserManager) {
        if (projectManager == null || applicantUserManager == null) {
             throw new IllegalArgumentException("ProjectManager and ApplicantUserManager cannot be null.");
        }
        this.projectManager = projectManager;
        this.applicantUserManager = applicantUserManager;
        this.officerUserManager = officerUserManager;
    }

    /**
     * Retrieves a combined list of all users who can potentially have applications
     *
     * @return A new List containing all Applicant and Officer users.
     */
    public List<Applicant> getAllApplicants() {
        List<Applicant> all = new ArrayList<>();
        all.addAll(applicantUserManager.getUsers());
        all.addAll(officerUserManager.getUsers());
        return all;
    }
    
    /**
     * Processes a BTO application submission.
     * Validates inputs, checks for existing active applications, and verifies room type availability.
     * If valid, sets applicant status to PENDING and saves the application details.
     *
     * @param applicant  The Applicant submitting the application.
     * @param project    The Project being applied for.
     * @param chosenRoom The RoomType selected by the applicant.
     * @return true if the application was successfully submitted, false otherwise.
     */
    public boolean apply(Applicant applicant, Project project, RoomType chosenRoom) {
        if (applicant == null || project == null || chosenRoom == null) {
             System.err.println("Application failed: Applicant, project, or chosen room cannot be null.");
             return false;
        }

        // Rule: Each applicant can only apply for one project at a time unless previous is UNSUCCESSFUL.
        if (applicant.getStatus() != null && applicant.getStatus() != ApplicationStatus.UNSUCCESSFUL) {
            System.out.println("Application failed: You already have an active or successful application (Status: " + applicant.getStatus() + ").");
            System.out.println("Please withdraw your existing application before applying for a new one.");
            return false;
        }

        // Check if the chosenRoom type exists in the project.
        boolean roomTypeExists = false;
        for (Room room : project.getRooms()) { //
            if (room.getRoomType() == chosenRoom) { //
                roomTypeExists = true;
                break;
            }
        }
        if (!roomTypeExists) {
             System.out.println("Application failed: The chosen room type (" + chosenRoom + ") is not available in project '" + project.getName() + "'.");
             return false;
        }

        applicant.setAppliedProject(project);
        applicant.setRoomChosen(chosenRoom);
        applicant.setStatus(ApplicationStatus.PENDING);

        // Trigger saving of the applicant's state
        if (applicantUserManager != null) {
             applicantUserManager.saveUsers();
        } else {
             System.err.println("Warning: ApplicantUserManager not set. Cannot save applicant state automatically.");
        }

        System.out.println("Application for project '" + project.getName() + "' submitted successfully. Status is now PENDING."); //
        return true;
    }

   /**
     * Initiates the withdrawal process for an applicant's application.
     * This method sets the application status to PENDING_WITHDRAWAL.
     * The actual withdrawal (clearing details, potentially returning room unit) happens
     * upon manager approval
     * Withdrawal can only be requested if the status is PENDING, SUCCESSFUL, or BOOKED.
     *
     * @param applicant The applicant requesting to withdraw their application.
     * @return true if the withdrawal request was successfully submitted (status set to PENDING_WITHDRAWAL), false otherwise.
     */
    public boolean withdrawApplication(Applicant applicant) {
        if (applicant == null) {
             System.err.println("Withdrawal failed: Applicant cannot be null.");
             return false;
        }

        ApplicationStatus currentStatus = applicant.getStatus();
        Project currentProject = applicant.getAppliedProject();
        RoomType currentRoom = applicant.getRoomChosen();

        // Check if withdrawal is allowed based on current status
        if (currentStatus == ApplicationStatus.PENDING ||
            currentStatus == ApplicationStatus.SUCCESSFUL ||
            currentStatus == ApplicationStatus.BOOKED) {

            // If status was BOOKED, increment room availability first
            if (currentStatus == ApplicationStatus.BOOKED) { //
                if (currentProject == null || currentRoom == null) {
                    System.err.println("Withdrawal Error: Cannot process withdrawal from BOOKED status.");
                    return false;
                }
            }

            // Set status to UNSUCCESSFUL and clear details
            applicant.setStatus(ApplicationStatus.PENDING_WITHDRAWAL);

            // Trigger saving of the applicant's state
            if (applicant instanceof Officer) {
				if (officerUserManager != null) {
                    officerUserManager.saveUsers();
                }
            } else {
                if (applicantUserManager != null) {
                    applicantUserManager.saveUsers();
                }
            }

            System.out.println("Application for project '" + (currentProject != null ? currentProject.getName() : "Unknown") + "' withdrawn. Status set to PENDING_WITHDRAWAL."); //
            return true;

        } else {
            // Status is null, UNSUCCESSFUL, or something else - cannot withdraw
            System.out.println("Withdrawal failed: No active application found, or application status ("+ currentStatus +") does not allow withdrawal."); //
            return false;
        }
    }

    /**
     * Approves a PENDING BTO application.
     * Changes the applicant's status to SUCCESSFUL.
     * Decrements the available room count for the chosen room type in the project.
     * Saves the updated applicant state and application list.
     *
     * @param applicant The applicant whose PENDING application is to be approved.
     * @return true if the application was successfully approved, false otherwise (e.g., applicant null, status not PENDING, room update failed).
     */
    public boolean approveApplication(Applicant applicant) {
        if (applicant == null || applicant.getStatus() != ApplicationStatus.PENDING) {
             System.err.println("Approval failed: Applicant is null or status is not PENDING.");
             return false;
        }

        Project project = applicant.getAppliedProject();
        RoomType chosenRoom = applicant.getRoomChosen();
        if (project == null || chosenRoom == null) {
            System.err.println("Approval failed: Project or chosen room data missing for applicant.");
            return false;
        }

        boolean updated = projectManager.updateRoomAvailability(project, chosenRoom, -1);
        if (!updated) {
             System.err.println("CRITICAL: Failed to decrement available room count for " + chosenRoom + " in project " + project.getName() + " during approval.");
        }

        applicant.setStatus(ApplicationStatus.SUCCESSFUL);
        saveApplicantUserState(applicant);

        System.out.println("Application approved for Applicant " + applicant.getNRIC() + ". Status set to SUCCESSFUL.");

        return true;
   }

    /**
     * Rejects a PENDING BTO application.
     * Changes the applicant's status to UNSUCCESSFUL.
     * Clears the applicant's applied project and chosen room details.
     * Saves the updated applicant state and application list.
     *
     * @param applicant The applicant whose PENDING application is to be rejected.
     * @return true if the application was successfully rejected, false otherwise (e.g., applicant null or status not PENDING).
     */
    public boolean rejectApplication(Applicant applicant) {
          if (applicant == null || applicant.getStatus() != ApplicationStatus.PENDING) {
              System.err.println("Rejection failed: Applicant is null or status is not PENDING.");
              return false;
          }

         applicant.setStatus(ApplicationStatus.UNSUCCESSFUL);
         applicant.setAppliedProject(null);
         applicant.setRoomChosen(null);

         saveApplicantUserState(applicant);

         System.out.println("Application rejected for Applicant " + applicant.getNRIC() + ". Status set to UNSUCCESSFUL.");

         return true;
    }

    /**
     * Approves an applicant's request to withdraw their application (status PENDING_WITHDRAWAL).
     * If the original application status involved a booked/allocated unit (implicitly, as withdrawal is allowed from BOOKED),
     * this method attempts to increment the room availability count in the project.
     * Clears the applicant's application details (project, room) and sets their status to null or UNSUCCESSFUL.
     * Saves the updated applicant state and application list.
     *
     * @param applicant The applicant whose PENDING_WITHDRAWAL request is being approved.
     * @return true if the withdrawal was successfully approved, false otherwise.
     */
    public boolean approveWithdrawal(Applicant applicant) {
        if (applicant == null || applicant.getStatus() != ApplicationStatus.PENDING_WITHDRAWAL) {
            System.err.println("Withdrawal approval failed: Applicant is null or status is not PENDING_WITHDRAWAL.");
            return false;
        }

        Project project = applicant.getAppliedProject();
        RoomType room = applicant.getRoomChosen();
        boolean unitReturned = false;

        if (project != null && room != null) {
            unitReturned = projectManager.updateRoomAvailability(project, room, +1);
            if (!unitReturned) {
                System.err.println("Warning: Failed to increment room availability during withdrawal approval for NRIC " + applicant.getNRIC());
            }
        }

        applicant.setAppliedProject(null);
        applicant.setRoomChosen(null);
        applicant.setStatus(null);

        saveApplicantUserState(applicant);

        System.out.println("Withdrawal approved for NRIC " + applicant.getNRIC() + ". Application details cleared.");
        return true;
    }

    /**
     * Rejects an applicant's request to withdraw their application (status PENDING_WITHDRAWAL).
     * Reverts the applicant's status back to what it likely was before the withdrawal request
     * (assumed to be SUCCESSFUL, as withdrawal from PENDING/BOOKED might lead to approval).
     * Saves the updated applicant state.
     *
     * @param applicant The applicant whose PENDING_WITHDRAWAL request is being rejected.
     * @return true if the withdrawal rejection was successful, false otherwise.
     */
    public boolean rejectWithdrawal(Applicant applicant) {
         if (applicant == null || applicant.getStatus() != ApplicationStatus.PENDING_WITHDRAWAL) {
            System.err.println("Withdrawal rejection failed: Applicant is null or status is not PENDING_WITHDRAWAL.");
            return false;
        }
        applicant.setStatus(ApplicationStatus.SUCCESSFUL);
        saveApplicantUserState(applicant);
        System.out.println("Withdrawal rejected for NRIC " + applicant.getNRIC() + ". Status reverted to " + applicant.getStatus() + ".");
        return true;
    }

    /**
     * Saves the current state of the applicant and their application details.
     * This method is called after any changes to the applicant's status or application details.
     *
     * @param applicant The applicant whose state is to be saved.
     */
    private void saveApplicantUserState(Applicant applicant) {
         if (applicant instanceof Officer) {
             if (officerUserManager != null) {
                 officerUserManager.saveUsers();
             } else {
                  System.err.println("Warning: OfficerUserManager not set. Cannot save officer state.");
             }
        } else {
             if (applicantUserManager != null) {
                 applicantUserManager.saveUsers();
             } else {
                 System.err.println("Warning: ApplicantUserManager not set. Cannot save applicant state.");
             }
        }
        saveApplications(APPLICATIONS_FILE_PATH, getAllApplicants());
    }

    /**
     * Updates the status of an applicant and saves their state.
     * This method is called when an officer or manager updates the status of an applicant.
     *
     * @param applicant The applicant whose status is to be updated.
     * @param newStatus The new status to set for the applicant.
     * @return true if the status was successfully updated and saved, false otherwise.
     */
    public boolean updateAndSaveApplicantStatus(Applicant applicant, ApplicationStatus newStatus) {
        if (applicant == null) {
             System.err.println("Cannot update status for null applicant.");
             return false;
        }
        applicant.setStatus(newStatus);
        saveApplicantUserState(applicant);
        System.out.println("Applicant " + applicant.getNRIC() + " status updated to " + newStatus + " and state saved.");
        return true;
    }

    /**
     * Saves the current application data for all relevant applicants to a CSV file.
     * Only includes applicants who have an active application (status is not null and not UNSUCCESSFUL).
     * Writes a header row followed by data rows.
     * Format: NRIC,Name,ProjectName,RoomType,Status
     *
     * @param filePath   The path to the CSV file to write to.
     * @param applicants The list of all applicants (including officers) whose applications should be considered for saving.
     */
    public void saveApplications(String filePath, List<Applicant> applicants) {
        List<String> lines = new ArrayList<>();
        lines.add("NRIC,Name,ProjectName,RoomType,Status");

        for (Applicant a : applicants) {
            if (a.getAppliedProject() != null && a.getStatus() != null && a.getStatus() != ApplicationStatus.UNSUCCESSFUL) {
                String line = a.getNRIC() + "," +
                              a.getName() + "," +
                              a.getAppliedProject().getName() + "," +
                              (a.getRoomChosen() != null ? a.getRoomChosen().name() : "") + "," +
                              (a.getStatus() != null ? a.getStatus().name() : "");
                lines.add(line);
            }
        }
        FileManager.writeFile(filePath, lines);
    }

    /**
     * Loads application data from a specified CSV file and links it to the provided lists
     * of applicants and projects.
     * Skips the header row and parses each subsequent line.
     * Finds the corresponding applicant and project objects based on NRIC and project name.
     * Sets the `appliedProject`, `chosenRoom`, and `status` on the applicant object.
     * Handles potential parsing errors (e.g., invalid enum values, missing objects).
     * Format: NRIC,Name,ProjectName,RoomType,Status
     *
     * @param filePath   The path to the CSV file containing application data.
     * @param applicants A list of all potential applicants (including officers) to link data to.
     * @param projects   A list of all projects to link data to.
     */
    public void loadApplications(String filePath, List<Applicant> applicants, List<Project> projects) {
        List<String> lines = FileManager.readFile(filePath);
        if (lines == null || lines.size() <= 1) {
            System.out.println("No application data found in " + filePath + " or file is empty.");
            return;
        }

        for (String line : lines.subList(1, lines.size())) {
            try {
                String[] parts = line.split(",", 5);
                if (parts.length >= 5) {
                    String nric = parts[0].trim().toUpperCase();
                    String projectName = parts[2].trim();
                    String roomTypeStr = parts[3].trim();
                    String statusStr = parts[4].trim();

                    Applicant targetApplicant = null;
                    for (Applicant a : applicants) {
                        if (a.getNRIC().equalsIgnoreCase(nric)) {
                            targetApplicant = a;
                            break;
                        }
                    }
                    if (targetApplicant == null) continue;

                    Project targetProject = null;
                    for (Project p : projects) {
                        if (p.getName().equalsIgnoreCase(projectName)) {
                            targetProject = p;
                            break;
                        }
                    }
                    if (targetProject == null) continue;

                    targetApplicant.setAppliedProject(targetProject);
                    try {
                        targetApplicant.setRoomChosen(!roomTypeStr.isEmpty() ? RoomType.valueOf(roomTypeStr) : null);
                    } catch (IllegalArgumentException e) { targetApplicant.setRoomChosen(null); }
                     try {
                        targetApplicant.setStatus(!statusStr.isEmpty() ? ApplicationStatus.valueOf(statusStr) : null);
                    } catch (IllegalArgumentException e) { targetApplicant.setStatus(null); }

                }
            } catch (Exception e) {
                 System.err.println("Error processing line in " + filePath + ": " + line + " - " + e.getMessage());
            }
        }
         System.out.println("Application data loaded from " + filePath + " and linked.");
    }
}
package control;

import entities.Manager;
import entities.Officer;
import entities.Project;
import enums.OfficerRegistrationStatus;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages the registration process for HDB Officers wanting to handle specific BTO projects.
 * Handles registration requests, approvals, and rejections, ensuring rules like
 * slot availability, non-application as applicant, and non-overlapping project periods are met.
 * Added check: Ensures officers can only register for projects with visibility 'on'.
 */
public class OfficerRegistrationManager {

    private final ProjectManager projectManager;
    private final UserManager<Officer> officerUserManager;

    /**
     * Constructs an OfficerRegistrationManager.
     *
     * @param projectManager     The manager for accessing project data.
     * @param officerUserManager The manager for accessing and saving officer data.
     */
    public OfficerRegistrationManager(ProjectManager projectManager, OfficerUserManager officerUserManager) {
        if (projectManager == null || officerUserManager == null) {
            throw new IllegalArgumentException("ProjectManager and OfficerUserManager cannot be null.");
        }
        this.projectManager = projectManager;
        this.officerUserManager = officerUserManager;
    }

    /**
     * Processes a request from an officer to register for a specific project.
     * Validates several conditions:
     * - Officer and project are not null.
     * - Project visibility is 'on'.
     * - Officer has not applied to this project as an applicant.
     * - Officer does not already have a PENDING or REJECTED registration for this project.
     * - Project has available officer slots.
     * - Project's application period does not overlap with other projects the officer
     * has PENDING or APPROVED registrations for.
     * If all checks pass, the officer's registration status for the project is set to PENDING.
     *
     * @param officer The Officer requesting registration.
     * @param project The Project the officer wants to register for.
     * @return true if the registration request was successfully submitted (status set to PENDING), false otherwise.
     */
    public boolean requestRegistration(Officer officer, Project project) {
        if (officer == null || project == null) {
            System.err.println("Registration failed: Officer or Project cannot be null.");
            return false;
        }

        // --- Check Registration Rules ---

        // 1. Check if the visibility of the project is 'on'
        if (!project.isVisibility()) {
            System.out.println("Registration failed: Project '" + project.getName() + "' is currently not visible and cannot be registered for.");
            return false;
        }


        // 2. Officer cannot have applied as an applicant to this project
        if (officer.getAppliedProject() != null && officer.getAppliedProject().equals(project)) {
            System.out.println("Registration failed: You have already applied to this project as an Applicant.");
            return false;
        }

        // 3. Officer cannot already have a pending registration for this project
        OfficerRegistrationStatus existingStatus = officer.getRegistrationStatusForProject(project);
        if (existingStatus == OfficerRegistrationStatus.PENDING) {
            System.out.println("Registration failed: Already pending for project '" + project.getName() + "'.");
            return false;
        }

        // 4. Officer cannot already have a rejected registration for this project
        if (existingStatus == OfficerRegistrationStatus.REJECTED) {
            System.out.println("Registration failed: Your previous registration request for project '" + project.getName() + "' was REJECTED.");
            return false;
        }

        // 5. Check available slots
        if (project.getOfficerSlot() <= 0) {
            System.out.println("Registration failed: No slots left for '" + project.getName() + "'.");
            return false;
        }

        // 6. Check for overlaps with OTHER PENDING or APPROVED projects
        LocalDate newOpen = project.getOpenDate();
        LocalDate newClose = project.getCloseDate();

        // Check for valid dates before proceeding with overlap check
        if (newOpen == null || newClose == null) {
             System.err.println("Registration failed: The target project '" + project.getName() + "' has invalid application dates.");
             return false;
        }

        for (Project existingProject : officer.getRegisteredProjects()) {
            if (existingProject.equals(project)) {
                continue;
            }
            OfficerRegistrationStatus statusOfExisting = officer.getRegistrationStatusForProject(existingProject);
            if (statusOfExisting == OfficerRegistrationStatus.PENDING || statusOfExisting == OfficerRegistrationStatus.APPROVED) {
                LocalDate existOpen = existingProject.getOpenDate();
                LocalDate existClose = existingProject.getCloseDate();

                // Check if the other project has valid dates
                if (existOpen != null && existClose != null) {
                    // Check for date overlap:
                    boolean overlaps = !(newClose.isBefore(existOpen) || newOpen.isAfter(existClose));

                    if (overlaps) {
                        System.out.println("Registration failed: The application period for '" + project.getName() +
                                           "' (" + newOpen + " to " + newClose + ") overlaps with your " + statusOfExisting +
                                           " registration for project '" + existingProject.getName() +
                                           "' (" + existOpen + " to " + existClose + ").");
                        return false;
                    }
                } else {
                     System.err.println("Warning: Skipping overlap check with project '" + existingProject.getName() + "' due to its invalid dates.");
                }
            }
        }

        // --- Register ---
        officer.addRegisteredProject(project, OfficerRegistrationStatus.PENDING);
        officerUserManager.saveUsers();
        System.out.println("Registration request submitted. Status: PENDING.");
        return true;
    }

    /**
     * Approves a pending registration request for an officer by the project manager.
     * Checks if the manager is indeed in charge of the project and if slots are available.
     * Updates the officer's status, decrements project slots, adds the officer's name
     * to the project, and saves changes.
     *
     * @param approver The Manager approving the registration.
     * @param officer  The Officer whose registration is being approved.
     * @return true if the approval was successful, false otherwise.
     */
    public boolean approveRegistration(Manager approver, Officer officer) {
        for (Project project : officer.getRegisteredProjects()) {
            OfficerRegistrationStatus status = officer.getRegistrationStatusForProject(project);
            if (status == OfficerRegistrationStatus.PENDING && project.getManager().equalsIgnoreCase(approver.getName())) {
                if (project.getOfficerSlot() <= 0) {
                    System.out.println("Approval failed: No slots left for project '" + project.getName() + "'.");
                    return false;
                }
                officer.updateRegistrationStatus(project, OfficerRegistrationStatus.APPROVED);
                project.setOfficerSlot(project.getOfficerSlot() - 1);
                if (officerUserManager instanceof OfficerUserManager) {
                    ((OfficerUserManager) officerUserManager).updateProjectListCSV(project, officer.getName());
                }
                project.addOfficer(officer.getName());
                officerUserManager.saveUsers();
                projectManager.saveProjects("data/ProjectList.csv");
                System.out.println("Officer " + officer.getName() + " approved for project '" + project.getName() + "'.");
                return true;
            }
        }
        System.out.println("Approval failed: No matching PENDING registration for this manager.");
        return false;
    }

    /**
     * Approves a pending registration request for an officer by the project manager.
     * Checks if the manager is indeed in charge of the project and if slots are available.
     * Updates the officer's status, decrements project slots, adds the officer's name
     * to the project, and saves changes.
     *
     * @param approver The Manager approving the registration.
     * @param officer  The Officer whose registration is being approved.
     * @return true if the approval was successful, false otherwise.
     */
    public boolean rejectRegistration(Manager rejector, Officer officer) {
        for (Project project : officer.getRegisteredProjects()) {
            OfficerRegistrationStatus status = officer.getRegistrationStatusForProject(project);
            if (status == OfficerRegistrationStatus.PENDING && project.getManager().equalsIgnoreCase(rejector.getName())) {
                officer.updateRegistrationStatus(project, OfficerRegistrationStatus.REJECTED);
                officerUserManager.saveUsers();
                System.out.println("Officer " + officer.getName() + " rejected for project '" + project.getName() + "'.");
                return true;
            }
        }
        System.out.println("Rejection failed: No matching PENDING registration for this manager.");
        return false;
    }

    /**
     * Retrieves a list of officers with pending registrations for a specific project.
     * If the project is null, an empty list is returned.
     *
     * @param project The Project to check for pending registrations.
     * @return A List of Officers with pending registrations for the specified project.
     */
    public List<Officer> getPendingRegistrationsForProject(Project project) {
        List<Officer> pending = new ArrayList<>();
        if (project == null) return pending;
        for (Officer officer : officerUserManager.getUsers()) {
            if (OfficerRegistrationStatus.PENDING.equals(officer.getRegistrationStatusForProject(project))) {
                pending.add(officer);
            }
        }
        return pending;
    }
    
}


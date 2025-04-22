package control;

import entities.Manager;
import entities.Officer;
import entities.Project;
import enums.OfficerRegistrationStatus;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class OfficerRegistrationManager {

    private final ProjectManager projectManager;
    private final UserManager<Officer> officerUserManager;

    public OfficerRegistrationManager(ProjectManager projectManager, OfficerUserManager officerUserManager) {
        if (projectManager == null || officerUserManager == null) {
            throw new IllegalArgumentException("ProjectManager and OfficerUserManager cannot be null.");
        }
        this.projectManager = projectManager;
        this.officerUserManager = officerUserManager;
    }

    public boolean requestRegistration(Officer officer, Project project) {
        if (officer == null || project == null) {
            System.err.println("Registration failed: Officer or Project cannot be null.");
            return false;
        }

        // 1. Officer cannot have applied as an applicant to this project
        if (officer.getAppliedProject() != null && officer.getAppliedProject().equals(project)) {
            System.out.println("Registration failed: You have already applied to this project as an Applicant.");
            return false;
        }

        // 2. Officer cannot already have a pending registration for this project
        OfficerRegistrationStatus existingStatus = officer.getRegistrationStatusForProject(project);
        if (existingStatus == OfficerRegistrationStatus.PENDING) {
            System.out.println("Registration failed: Already pending for project '" + project.getName() + "'.");
            return false;
        }

        // 3. Officer cannot already have a rejected registration for this project
        if (existingStatus == OfficerRegistrationStatus.REJECTED) {
            System.out.println("Registration failed: Your previous registration request for project '" + project.getName() + "' was REJECTED.");
            return false;
        }

        // 4. Check available slots
        if (project.getOfficerSlot() <= 0) {
            System.out.println("Registration failed: No slots left for '" + project.getName() + "'.");
            return false;
        }

        // 5. Check for overlaps with OTHER PENDING or APPROVED projects
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


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
    // Optional: May need access to all projects for cross-project checks
    // private final List<Project> allProjects; // Get via projectManager

    public OfficerRegistrationManager(ProjectManager projectManager, UserManager<Officer> officerUserManager) {
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

        // --- Eligibility Checks ---

        // 1. Check if Officer has already applied for this project as an Applicant
        if (officer.getAppliedProject() != null && officer.getAppliedProject().equals(project)) {
             System.out.println("Registration failed: You have already submitted an application for project '" + project.getName() + "' as an Applicant.");
             return false;
        }

        // 2. Check if the *requesting* officer is already APPROVED for another project with an overlapping application period.
        if (officer.getRegistrationStatus() == OfficerRegistrationStatus.APPROVED && officer.getRegisteredProject() != null) {
            Project currentProject = officer.getRegisteredProject();
            LocalDate projectOpenDate = project.getOpenDate();
            LocalDate projectCloseDate = project.getCloseDate();
            LocalDate currentOpen = currentProject.getOpenDate();
            LocalDate currentClose = currentProject.getCloseDate();

            // Check dates are valid and perform overlap check
            // Overlap exists if (StartA <= EndB) and (EndA >= StartB)
            if (projectOpenDate != null && projectCloseDate != null && currentOpen != null && currentClose != null &&
                !projectOpenDate.isAfter(currentClose) && // Target start <= Current end
                !projectCloseDate.isBefore(currentOpen))  // Target end >= Current start
            {
                // If BOTH conditions are true, the periods overlap
                System.out.println("Registration failed: You are already approved for project '" + currentProject.getName() + "' which has an overlapping application period with '" + project.getName() + "'.");
                return false; // Prevent registration because of overlap
            }
       }

        // 3. Check if Officer already has a PENDING registration for *any* project
        if (officer.getRegistrationStatus() == OfficerRegistrationStatus.PENDING && officer.getRegisteredProject() != null) {
              System.out.println("Registration failed: You already have a pending registration for project '" + officer.getRegisteredProject().getName() + "'. Please wait for approval or withdraw it.");
              return false;
        }

        // 4. Check if project has available officer slots
        if (project.getOfficerSlot() <= 0) {
            System.out.println("Registration failed: Project '" + project.getName() + "' has no available HDB Officer slots.");
            return false;
        }


        // --- All checks passed: Set status to PENDING ---
        officer.setRegisteredProject(project);
        officer.setRegistrationStatus(OfficerRegistrationStatus.PENDING);
        officerUserManager.saveUsers();

        System.out.println("Registration request for project '" + project.getName() + "' submitted successfully. Status is now PENDING.");
        return true;
    }


    public boolean approveRegistration(Manager approver, Officer officerToApprove) {
        if (approver == null || officerToApprove == null) {
            System.err.println("Approval failed: Approver or Officer cannot be null.");
            return false;
        }

        Project project = officerToApprove.getRegisteredProject();

        // 1. Check if a project is actually assigned for registration
        if (project == null) {
            System.err.println("Approval failed: Officer " + officerToApprove.getNRIC() + " has no project pending registration.");
            return false;
        }

        // 2. Verify the Officer's status is PENDING
        if (officerToApprove.getRegistrationStatus() != OfficerRegistrationStatus.PENDING) {
            System.err.println("Approval failed: Officer " + officerToApprove.getNRIC() + "'s registration status is not PENDING (Current: " + officerToApprove.getRegistrationStatus() + ").");
            return false;
        }

        // 3. Verify the approver is the Manager in charge of the project
        if (!project.getManager().equalsIgnoreCase(approver.getNRIC())) {
            System.err.println("Approval failed: Manager " + approver.getNRIC() + " is not authorized to approve registrations for project '" + project.getName() + "'.");
            return false;
        }

        // 4. Check project still has slots
        if (project.getOfficerSlot() <= 0) {
             System.err.println("Approval failed: Project '" + project.getName() + "' has no available HDB Officer slots remaining.");
             return false;
        }

        officerToApprove.setRegistrationStatus(OfficerRegistrationStatus.APPROVED);
        project.setOfficerSlot(project.getOfficerSlot() - 1);
        officerUserManager.saveUsers();
        projectManager.saveProjects("data/ProjectList.csv");

        System.out.println("Registration for Officer " + officerToApprove.getNRIC() + " approved for project '" + project.getName() + "'.");
        return true;
    }



    public boolean rejectRegistration(Manager rejector, Officer officerToReject) {
        if (rejector == null || officerToReject == null) {
            System.err.println("Rejection failed: Rejector or Officer cannot be null.");
            return false;
        }

        Project project = officerToReject.getRegisteredProject();

        // 1. Check if a project is actually assigned for registration
        if (project == null) {
            System.err.println("Rejection failed: Officer " + officerToReject.getNRIC() + " has no project pending registration.");
            return false;
        }

        // 2. Verify the Officer's status is PENDING
        if (officerToReject.getRegistrationStatus() != OfficerRegistrationStatus.PENDING) {
            System.err.println("Rejection failed: Officer " + officerToReject.getNRIC() + "'s registration status is not PENDING (Current: " + officerToReject.getRegistrationStatus() + ").");
            return false;
        }

        // 3. Verify the rejector is the Manager in charge of the project
        if (!project.getManager().equalsIgnoreCase(rejector.getNRIC())) {
            System.err.println("Rejection failed: Manager " + rejector.getNRIC() + " is not authorized to reject registrations for project '" + project.getName() + "'.");
            return false;
        }

        officerToReject.setRegistrationStatus(OfficerRegistrationStatus.REJECTED);
        officerUserManager.saveUsers();
        System.out.println("Registration for Officer " + officerToReject.getNRIC() + " rejected for project '" + project.getName() + "'.");
        return true;
    }


    public List<Officer> getPendingRegistrationsForProject(Project project) {
         List<Officer> pendingOfficers = new ArrayList<>();
         if (project == null) return pendingOfficers;

         List<Officer> allOfficers = officerUserManager.getUsers();
         for (Officer officer : allOfficers) {
              if (officer.getRegistrationStatus() == OfficerRegistrationStatus.PENDING &&
                  officer.getRegisteredProject() != null &&
                  officer.getRegisteredProject().equals(project)) {
                  pendingOfficers.add(officer);
              }
         }
         return pendingOfficers;
    }
}
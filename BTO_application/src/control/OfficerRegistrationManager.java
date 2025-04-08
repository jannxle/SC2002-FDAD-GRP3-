package control;

import java.time.LocalDate;

import entities.Officer;
import entities.Project;
import enums.OfficerRegistrationStatus;

public class OfficerRegistrationManager {

    public boolean registerOfficerForProject(Officer officer, Project project) {
        // Check criteria (e.g., ensure officer hasn't applied as applicant, or isn't registered for overlapping project).
        if (officer.getAppliedProject() != null) {
            System.out.println("Registration failed: You have applied for this project as an applicant.");
            return false;
        }
        
        // Check overlapping project if needed…
        Project currentProject = officer.getRegisteredProject();
        if ( currentProject != null) {
        	if (isOverlapping(currentProject, project)) {
        		System.out.println("Registration failed - You are already registered (or pending) for a project" +
        							" with overlapping application period");
        		return false;
        	}
        	
        }
        // If criteria are met, assign the project and set status to PENDING.
        officer.setRegisteredProject(project);
        officer.setRegistrationStatus(OfficerRegistrationStatus.PENDING);
        
        System.out.println("Registration submitted. Your registration status is now PENDING approval from HDB Manager.");
        return true;
    }
    
    /**
     * Checks whether the application periods of two projects overlap.
     * Two projects overlap if p1.openDate <= p2.closeDate and p2.openDate <= p1.closeDate.
     *
     * @param p1 The first project.
     * @param p2 The second project.
     * @return true if the application periods overlap, false otherwise.
     */
    private boolean isOverlapping(Project p1, Project p2) {
        LocalDate p1Open = p1.getOpenDate();
        LocalDate p1Close = p1.getCloseDate();
        LocalDate p2Open = p2.getOpenDate();
        LocalDate p2Close = p2.getCloseDate();
        
        // Check if p1's open date is on or before p2's close date and
        // p2's open date is on or before p1's close date.
        return (!p1Open.isAfter(p2Close)) && (!p2Open.isAfter(p1Close));
    }
}

package entities;

import enums.OfficerRegistrationStatus;

/**
 * Represents an HDB Officer.
 * An Officer is a type of Applicant with additional fields for officer registration.
 */
public class Officer extends Applicant {
    
    // The project for which the officer is registered as an HDB Officer.
    private Project registeredProject;
    
    // The registration status (e.g., PENDING, APPROVED, REJECTED).
    private OfficerRegistrationStatus registrationStatus;
    
    /**
     * Constructs a new Officer.
     * @param name The officer's name.
     * @param NRIC The officer's NRIC.
     * @param age The officer's age.
     * @param isMarried Whether the officer is married.
     * @param password The officer's password.
     */
    public Officer(String name, String NRIC, int age, boolean isMarried, String password) {
        super(name, NRIC, age, isMarried, password);
        this.registeredProject = null;
        this.registrationStatus = null; // No registration by default.
    }
    
    /**
     * Returns the project for which the officer is registered.
     * @return the registered project.
     */
    public Project getRegisteredProject() {
        return registeredProject;
    }
    
    /**
     * Sets the project for which the officer is registered.
     * @param registeredProject the project to set.
     */
    public void setRegisteredProject(Project registeredProject) {
        this.registeredProject = registeredProject;
    }
    
    /**
     * Returns the registration status of the officer.
     * @return the registration status.
     */
    public OfficerRegistrationStatus getRegistrationStatus() {
        return registrationStatus;
    }
    
    /**
     * Sets the registration status of the officer.
     * @param registrationStatus the registration status to set.
     */
    public void setRegistrationStatus(OfficerRegistrationStatus registrationStatus) {
        this.registrationStatus = registrationStatus;
    }
    
    public void setAppliedProject(Project newProject) {
    	super.setAppliedProject(newProject);
        this.registeredProject = newProject;
    }
}

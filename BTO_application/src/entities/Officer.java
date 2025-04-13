package entities;

import enums.OfficerRegistrationStatus;

import java.util.*;

/**
 * Represents an HDB Officer.
 * An Officer is a type of Applicant with additional fields for officer registration.
 */
public class Officer extends Applicant {

    // A list of projects the officer is registered to handle.
    private List<Project> registeredProjects;

    // A map of registration status per project.
    private Map<Project, OfficerRegistrationStatus> registrationStatuses;

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
        this.registeredProjects = new ArrayList<>();
        this.registrationStatuses = new HashMap<>();
    }

    /**
     * Adds a project the officer is registered for.
     * @param project the project.
     * @param status the registration status.
     */
    public void addRegisteredProject(Project project, OfficerRegistrationStatus status) {
        if (!registeredProjects.contains(project)) {
            registeredProjects.add(project);
            registrationStatuses.put(project, status);
        }
    }

    /**
     * Returns the list of projects the officer is handling.
     * @return list of registered projects.
     */
    public List<Project> getRegisteredProjects() {
        return registeredProjects;
    }

    /**
     * Gets the registration status for a specific project.
     * @param project the project.
     * @return the registration status, or null if not found.
     */
    public OfficerRegistrationStatus getRegistrationStatusForProject(Project project) {
        return registrationStatuses.get(project);
    }

    /**
     * Updates the registration status for a specific project.
     * @param project the project.
     * @param status the new status.
     */
    public void updateRegistrationStatus(Project project, OfficerRegistrationStatus status) {
        if (registeredProjects.contains(project)) {
            registrationStatuses.put(project, status);
        }
    }

    /**
     * Overrides applied project for officer (used by Applicant part).
     * @param newProject the applied project.
     */
    public void setAppliedProject(Project newProject) {
        super.setAppliedProject(newProject);
    }
}
package entities;

import enums.OfficerRegistrationStatus;

import java.util.*;

/**
 * Represents an HDB Officer user in the BTO Management System.
 * An Officer extends the Applicant class, inheriting all applicant capabilities,
 * but also has additional responsibilities related to managing specific BTO projects.
 * Officers can register to handle projects, view project details, and manage enquiries
 * and bookings for the projects they are assigned to.
 */
public class Officer extends Applicant {

    private List<Project> registeredProjects;

    private Map<Project, OfficerRegistrationStatus> registrationStatuses;

    /**
     * Constructs a new Officer object.
     * Initializes the officer with basic user details and sets the role to OFFICER.
     * Also initializes the lists/maps for managing project registrations.
     *
     * @param name      The name of the officer.
     * @param NRIC      The NRIC of the officer.
     * @param age       The age of the officer.
     * @param isMarried The marital status of the officer (true if married, false if single).
     * @param password  The login password for the officer.
     */
    public Officer(String name, String NRIC, int age, boolean isMarried, String password) {
        super(name, NRIC, age, isMarried, password);
        this.registeredProjects = new ArrayList<>();
        this.registrationStatuses = new HashMap<>();
    }

    /**
     * Adds a project to the officer's list of registered projects with a specific status.
     *
     * @param project The Project to register for.
     * @param status  The initial OfficerRegistrationStatus (e.g., PENDING).
     */
    public void addRegisteredProject(Project project, OfficerRegistrationStatus status) {
        if (!registeredProjects.contains(project)) {
            registeredProjects.add(project);
            registrationStatuses.put(project, status);
        }
    }

    /**
     * Gets the list of projects the officer is registered to handle (regardless of status).
     *
     * @return A List of Project objects.
     */
    public List<Project> getRegisteredProjects() {
        return registeredProjects;
    }

    /**
     * Gets the registration status for a specific project the officer is registered for.
     *
     * @param project The Project to check the status for.
     * @return The OfficerRegistrationStatus for the given project, or null if the officer is not registered for it.
     */
    public OfficerRegistrationStatus getRegistrationStatusForProject(Project project) {
        return registrationStatuses.get(project);
    }

    /**
     * Updates the registration status for a specific project the officer is registered for.
     *
     * @param project The Project whose status needs updating.
     * @param status  The new OfficerRegistrationStatus (e.g., APPROVED, REJECTED).
     */
    public void updateRegistrationStatus(Project project, OfficerRegistrationStatus status) {
        if (registeredProjects.contains(project)) {
            registrationStatuses.put(project, status);
        }
    }

    /**
     * Overrides the setAppliedProject method from the Applicant superclass.
     * Ensures that when an Officer applies for a project (as an applicant),
     * the application details are set correctly in the inherited fields.
     *
     * @param newProject The Project that the officer is applying for as an applicant.
     */
    @Override
    public void setAppliedProject(Project newProject) {
        super.setAppliedProject(newProject);
    }
    
    /**
     * Removes a project from the officer's registration list and status map.
     *
     * @param project The Project to remove from the registration records.
     */
    public void removeRegisteredProject(Project project) {
        registeredProjects.remove(project);
        registrationStatuses.remove(project);
    }
}
package entities;

import enums.OfficerRegistrationStatus;

import java.util.*;


public class Officer extends Applicant {

    private List<Project> registeredProjects;

    private Map<Project, OfficerRegistrationStatus> registrationStatuses;

    public Officer(String name, String NRIC, int age, boolean isMarried, String password) {
        super(name, NRIC, age, isMarried, password);
        this.registeredProjects = new ArrayList<>();
        this.registrationStatuses = new HashMap<>();
    }

    public void addRegisteredProject(Project project, OfficerRegistrationStatus status) {
        if (!registeredProjects.contains(project)) {
            registeredProjects.add(project);
            registrationStatuses.put(project, status);
        }
    }

    public List<Project> getRegisteredProjects() {
        return registeredProjects;
    }

    public OfficerRegistrationStatus getRegistrationStatusForProject(Project project) {
        return registrationStatuses.get(project);
    }


    public void updateRegistrationStatus(Project project, OfficerRegistrationStatus status) {
        if (registeredProjects.contains(project)) {
            registrationStatuses.put(project, status);
        }
    }

    public void setAppliedProject(Project newProject) {
        super.setAppliedProject(newProject);
    }
    
    public void removeRegisteredProject(Project project) {
        registeredProjects.remove(project);
        registrationStatuses.remove(project);
    }
}
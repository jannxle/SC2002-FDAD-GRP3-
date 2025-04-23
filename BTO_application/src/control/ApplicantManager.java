package control;

import entities.Applicant;
import entities.Officer;
import entities.Project;
import entities.Room;
import enums.RoomType;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;

/**
 * Manages logic related to applicants viewing BTO projects.
 * This class filters the list of all available BTO projects based on
 * applicant eligibility criteria (age, marital status, eligible room types)
 * and project status (visibility 'on', within application period).
 * It also provides specific filtering logic for HDB Officers viewing projects
 * for potential registration.
 */
public class ApplicantManager {

    private ProjectManager projectManager;

    /**
     * Constructs an ApplicantManager.
     * Requires a ProjectManager instance to retrieve project data.
     *
     * @param projectManager The ProjectManager instance used to access project information.
     * @throws IllegalArgumentException if projectManager is null.
     */
    public ApplicantManager(ProjectManager projectManager) {
        if (projectManager == null) {
            throw new IllegalArgumentException("ProjectManager cannot be null.");
        }
        this.projectManager = projectManager;
    }

    /**
     * Retrieves a list of BTO projects available for the given applicant to apply for.
     * Filters projects based on visibility ('on'), current application period, and
     * applicant eligibility (Single >=35 for 2-Room only; Married >=21 for any type).
     *
     * @param applicant The applicant for whom to find available projects.
     * @return A List of eligible, open, and visible Project objects, or an empty list if none match.
     */
    public List<Project> getAvailableProjects(Applicant applicant) {
        List<Project> allProjects = projectManager.getProjects();
        List<Project> availableProjects = new ArrayList<>();
        LocalDate today = LocalDate.now();

        if (applicant == null) {
            System.err.println("Cannot get available projects for a null applicant.");
            return availableProjects;
        }

        for (Project p : allProjects) {
            // 1. Check Project Visibility
            if (!p.isVisibility()) {
                continue;
            }

            // 2. Check Application Period
            // Assuming Project has getOpenDate() and getCloseDate() returning LocalDate
            LocalDate openDate = p.getOpenDate();
            LocalDate closeDate = p.getCloseDate();
            if (openDate == null || closeDate == null || today.isBefore(openDate) || today.isAfter(closeDate)) {
                 continue;
            }


            // 3. Check Applicant Eligibility based on available rooms in the project
            boolean eligible = false;
            if (!applicant.isMarried() && applicant.getAge() >= 35) {
                // Single, >= 35: Can ONLY apply for 2-Room. Check if project HAS 2-Room.
                for (Room room : p.getRooms()) {
                    if (room.getRoomType() == RoomType.TwoRoom) {
                        eligible = true;
                        break;
                    }
                }
            } else if (applicant.isMarried() && applicant.getAge() >= 21) {
                // Married, >= 21: Can apply for ANY flat type (2-Room or 3-Room).
                // Just need to ensure the project has at least one room type listed.
                if (p.getRooms() != null && !p.getRooms().isEmpty()) {
                     eligible = true;
                }
            } else {
                 // Applicant does not meet any eligibility criteria (e.g., single < 35, married < 21)
                 eligible = false;
            }

            // 4. Add to list if all checks pass
            if (eligible) {
                availableProjects.add(p);
            }
        }
        return availableProjects;
    }
    
    /**
     * Retrieves a list of BTO projects potentially available for an HDB Officer to register for.
     * This method filters projects based on criteria relevant for officer registration,
     * which might differ slightly from applicant eligibility.
     * 
     * Filters the projects based on:
     * Project visibility must be 'on'.
     * Current date must be within the project's application open and close dates.
     * Officer must not have already applied for this project as an applicant.
     * Officer must not already be registered (any status) for this specific project.
     * Project's application period must not overlap with other projects the officer is registered for.
     *
     * @param officer The Officer for whom to find potentially available projects for registration.
     * @return A List of Project objects that are currently open and visible, and meet basic criteria for potential registration.
     */
    public List<Project> getAvailableProjectsForOfficer(Officer officer) {
        List<Project> allProjects = projectManager.getProjects();
        List<Project> availableProjects = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (Project p : allProjects) {
            // Officers must not have applied for the project before
            if (officer.getAppliedProject() != null &&
                officer.getAppliedProject().getName().equalsIgnoreCase(p.getName())) {
                continue;
            }

            // Officers can't register for projects they're already handling
            if (officer.getRegisteredProjects().stream()
                    .anyMatch(registered -> registered.getName().equalsIgnoreCase(p.getName()))) {
                continue;
            }

            // Must not overlap with existing officer project (date-wise)
            boolean overlaps = officer.getRegisteredProjects().stream().anyMatch(registered -> {
                return !(p.getCloseDate().isBefore(registered.getOpenDate()) ||
                         p.getOpenDate().isAfter(registered.getCloseDate()));
            });
            
            // Project must be visible and within application period
            if (!p.isVisibility()) continue;
            if (p.getOpenDate() == null || p.getCloseDate() == null) continue;
            if (today.isBefore(p.getOpenDate()) || today.isAfter(p.getCloseDate())) continue;

            availableProjects.add(p);
        }

        return availableProjects;
    }

}
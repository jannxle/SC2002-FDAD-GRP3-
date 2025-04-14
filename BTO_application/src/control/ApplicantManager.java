package control;

import entities.Applicant;
import entities.Project;
import entities.Room;
import enums.RoomType;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;

/**
 * This class filters the list of available BTO projects based on
 * applicant eligibility criteria (age, marital status, needed room types)
 * and project visibility/application period.
 */

// Could this class just been stream expressions? - Shrey

public class ApplicantManager {

    private ProjectManager projectManager;

    public ApplicantManager(ProjectManager projectManager) {
        if (projectManager == null) {
            throw new IllegalArgumentException("ProjectManager cannot be null.");
        }
        this.projectManager = projectManager;
    }

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

}
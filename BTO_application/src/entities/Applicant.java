package entities;


import enums.ApplicationStatus;
import enums.Role;
import enums.RoomType;

/**
 * Represents an Applicant user in the BTO Management System.
 * Extends the base User class and adds attributes specific to
 * BTO applications, such as the project applied for, the chosen room type,
 * and the current application status.
 */
public class Applicant extends User{
	private Project appliedProject;
	private RoomType chosenRoom;
	private ApplicationStatus status;
	
    /**
     * Constructs a new Applicant object.
     * Initializes the applicant with basic user details and sets the role to APPLICANT.
     * Application-specific details (project, room, status) are initially null.
     *
     * @param name      The name of the applicant.
     * @param NRIC      The NRIC of the applicant.
     * @param age       The age of the applicant.
     * @param isMarried The marital status of the applicant (true if married, false if single).
     * @param password  The login password for the applicant.
     */
	public Applicant(String name, String NRIC, int age, boolean isMarried, String password) {
		super(name, NRIC, age, isMarried, password, Role.APPLICANT);
	}

    /**
     * Gets the project the applicant has applied for.
     *
     * @return The Project object representing the applied project, or null if none.
     */
    public Project getAppliedProject() {
        return appliedProject;
    }

    /**
     * Sets the project the applicant has applied for.
     *
     * @param appliedProject The Project the applicant is applying for or has applied to.
     */
    public void setAppliedProject(Project appliedProject) {
        this.appliedProject = appliedProject;
    }

    /**
     * Gets the room type chosen by the applicant in their application.
     *
     * @return The RoomType chosen, or null if no application or room type selected.
     */
    public RoomType getRoomChosen() {
        return chosenRoom;
    }

    /**
     * Sets the room type chosen by the applicant.
     *
     * @param flatTypeChosen The RoomType selected by the applicant.
     */
    public void setRoomChosen(RoomType flatTypeChosen) {
        this.chosenRoom = flatTypeChosen;
    }

    /**
     * Gets the current status of the applicant's BTO application.
     *
     * @return The ApplicationStatus of the application (e.g., PENDING, SUCCESSFUL, BOOKED), or null if no active application.
     */
    public ApplicationStatus getStatus() {
        return status;
    }

    /**
     * Sets the current status of the applicant's BTO application.
     *
     * @param status The new ApplicationStatus for the application.
     */
    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }

    /**
     * Returns a string representation of the Applicant object, including basic user info
     * and current application details (project name and status).
     *
     * @return A string summarizing the Applicant's details.
     */
    @Override
    public String toString() {
        return "Applicant{" +
                "name='" + getName() + '\'' +
                ", NRIC='" + getNRIC() + '\'' +
                ", age=" + getAge() +
                ", maritalStatus=" + (isMarried() ? "Married" : "Single") +
                ", appliedProject=" + (appliedProject != null ? appliedProject.getName() : "None") +
                ", status=" + (status != null ? status : "None") +
                '}';
    }
}

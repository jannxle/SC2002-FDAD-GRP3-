package entities;


import enums.ApplicationStatus;
import enums.Role;
import enums.RoomType;

public class Applicant extends User{
	private Project appliedProject;
	private RoomType chosenRoom;
	private ApplicationStatus status;
	
	public Applicant(String name, String NRIC, int age, boolean isMarried, String password) {
		super(name, NRIC, age, isMarried, password, Role.APPLICANT);
	}

    // Getters and Setters
    public Project getAppliedProject() {
        return appliedProject;
    }

    public void setAppliedProject(Project appliedProject) {
        this.appliedProject = appliedProject;
    }

    public RoomType getRoomChosen() {
        return chosenRoom;
    }

    public void setRoomChosen(RoomType flatTypeChosen) {
        this.chosenRoom = flatTypeChosen;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }


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

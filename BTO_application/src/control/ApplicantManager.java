package control;

import entities.Applicant;
import entities.Project;
import entities.Room;
import enums.RoomType;

import java.util.ArrayList;
import java.util.List;
//this class filters the list of projects based on applicant eligibility
public class ApplicantManager {
	private List<Project> allProjects;
	
    public ApplicantManager(List<Project> allProjects) {
        this.allProjects = allProjects;
    }
    
    public List<Project> getAvaliableProjects(Applicant applicant){
    	List<Project> availableProjects = new ArrayList<>();
    	//Can only view the list of projects that are open to their user group (Single
    	// or Married) and if their visibility has been toggled “on”.
    	for (Project p:allProjects) {
    		if(!p.isVisibility()) {
    			continue; //project is hidden, applicant cannot access
    		}
    	//Eligibility Checking
    	// if singles (isMarried==false) && age >= 35, can only apply 2-room
    	// if married (isMarried == true) && age >= 21, can apply any rooms
    		if (!applicant.isMarried() && applicant.getAge()>=35) {
    			for (Room room:p.getRooms()) {
    				if (room.getRoomType() == RoomType.TwoRoom) {
    					availableProjects.add(p);
    					break;
    				}
    			}
    		} else if (applicant.isMarried() && applicant.getAge()>=21) {
    			availableProjects.add(p);
    		}
    	}
    	return availableProjects;
    }
    
}

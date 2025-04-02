package entities;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import enums.RoomType;

public class Project {
	private String projectName;
	private String neighbourhood;
	private LocalDate openDate; //need to parse as it is yyyy/mm/dd format
	private LocalDate closeDate;
	private String manager;
	private int officerSlot;
	private List<Room> rooms; //List holding room objects
	private boolean visibility;
	private String officer;

	private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("d/M/yy");
	
	//Constructor
	public Project(String projectName, String neighbourhood, LocalDate openDate,
            LocalDate closeDate, String manager, int officerSlot,
            List<Room> rooms, boolean visibility, String officer) {
		this.projectName = projectName;
		this.neighbourhood = neighbourhood;
		this.openDate = openDate;
		this.closeDate = closeDate;
		this.manager = manager;
		this.officerSlot = officerSlot;
		this.rooms = rooms;
		this.visibility = visibility;
		this.officer = officer;
	}
	
	public String getName() {
		return projectName;
	}
	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}
	
	public String getNeighbourhood() {
		return neighbourhood;
	}
	public void setNeighbourhood(String neighbourhood) {
		this.neighbourhood = neighbourhood;
	}
	public LocalDate getOpenDate() {
		return openDate;
	}
	public void setOpenDate(LocalDate newOpenDate) {
		this.openDate = newOpenDate;
	}
	public LocalDate getCloseDate() {
		return closeDate;
	}
	public void setCloseDate(LocalDate newCloseDate) {
		this.closeDate = newCloseDate;
	}
	public String getManager() {
		return manager;
	}
	public String getOfficer() {
		return officer;
	}
	public void setOfficer(String officer) {
	    this.officer = officer;
	}

	public int getOfficerSlot() {
		return officerSlot;
	}
	public void setOfficerSlot(int officerSlot) {
	    this.officerSlot = officerSlot;
	}
	public List<Room> getRooms() {
	    return rooms;
	}
	public void setRooms(List<Room> rooms) {
	    this.rooms = rooms;
	}	
	public boolean isVisibility() {
		return visibility;
	}
	public void setVisibility(boolean visibility) {
		this.visibility = visibility;
	}
	
	
    public static Project fromCSV(String csvLine) {
        // CSV format:
        // Project Name, Neighbourhood, Type 1, Number of units for Type 1, 
        // Selling price for Type 1, Type 2, Number of units for Type 2, 
        // Selling price for Type 2, Application opening date, 
        // Application closing date, Manager, Officer Slot, Officer
        String[] parts = csvLine.split(",");
        if (parts.length < 13) {
            throw new IllegalArgumentException("Not enough columns in CSV line.");
        }

        String projectName = parts[0].trim();
        String neighborhood = parts[1].trim();
        // parse room1
        String type1Str = parts[2].trim();
        RoomType roomType1 = type1Str.equalsIgnoreCase("2-Room") ? RoomType.TwoRoom : RoomType.ThreeRoom;
        int numUnitsType1 = Integer.parseInt(parts[3].trim());
        double priceType1 = Double.parseDouble(parts[4].trim());
        // parse room2
        String type2Str = parts[5].trim();
        RoomType roomType2 = type2Str.equalsIgnoreCase("2-Room") ? RoomType.TwoRoom : RoomType.ThreeRoom;
        int numUnitsType2 = Integer.parseInt(parts[6].trim());
        double priceType2 = Double.parseDouble(parts[7].trim());
        // parse dates
        LocalDate openDate = LocalDate.parse(parts[8].trim(), dtf);
        LocalDate closeDate = LocalDate.parse(parts[9].trim(), dtf);
        
        String manager = parts[10].trim();
        int officerSlot = Integer.parseInt(parts[11].trim());
        String officer = parts[12].trim();

        // create two Room objects
        Room room1 = new Room(roomType1, numUnitsType1, priceType1);
        Room room2 = new Room(roomType2, numUnitsType2, priceType2);
        List<Room> rooms = List.of(room1, room2);

        boolean visibility = true; // default or parse from CSV if you have a column for it.
        
        
        return new Project(projectName, neighborhood, openDate, closeDate, manager, officerSlot, rooms, visibility, officer);
  
    }

	
}

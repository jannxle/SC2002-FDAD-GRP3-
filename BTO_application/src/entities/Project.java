package entities;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.ArrayList;

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
	public int getRoomCount(RoomType type) {
	    for (Room room : rooms) {
	        if (room.getRoomType() == type) {
	            return room.getAvailableRooms();
	        }
	    }
	    return 0; // If room type not found
	}
	public void setRoomCount(RoomType type, int count) {
	    for (Room room : rooms) {
	        if (room.getRoomType() == type) {
	            room.setAvailableRooms(count);
	            return;
	        }
	    }
	}
	public boolean isVisibility() {
		return visibility;
	}
	public void setVisibility(boolean visibility) {
		this.visibility = visibility;
	}
	
	public void addOfficer(String name) {
	    if (officer == null || officer.isEmpty()) {
	        officer = name;
	    } else if (!officer.contains(name)) {
	        officer += ";" + name;
	    }
	}
	
    public static Project fromCSV(String csvLine) {
        String[] parts = csvLine.split(",");
        if (parts.length < 16) {
            System.err.println("Skipping line: Not enough columns in CSV line. Expected 16, got " + parts.length + ". Line: " + csvLine);
            return null;
        }

        try {
            String projectName = parts[0].trim();
            String neighborhood = parts[1].trim();

            List<Room> rooms = new ArrayList<>();

            String type1Str = parts[2].trim();
            if (!type1Str.isEmpty()) {
                 RoomType roomType1 = RoomType.valueOf(type1Str);
                 int numUnitsType1 = Integer.parseInt(parts[3].trim());
                 int availUnitsType1 = Integer.parseInt(parts[4].trim());
                 double priceType1 = Double.parseDouble(parts[5].trim());
                 rooms.add(new Room(roomType1, numUnitsType1, availUnitsType1, priceType1));
            }

             String type2Str = parts[6].trim();
            if (!type2Str.isEmpty()) {
                RoomType roomType2 = RoomType.valueOf(type2Str);
                int numUnitsType2 = Integer.parseInt(parts[7].trim());
                int availUnitsType2 = Integer.parseInt(parts[8].trim());
                double priceType2 = Double.parseDouble(parts[9].trim());
                rooms.add(new Room(roomType2, numUnitsType2, availUnitsType2, priceType2));
            }

            LocalDate openDate = LocalDate.parse(parts[10].trim(), dtf);
            LocalDate closeDate = LocalDate.parse(parts[11].trim(), dtf);

            String manager = parts[12].trim();
            int officerSlot = Integer.parseInt(parts[13].trim());
            String officer = parts[14].trim();

            boolean visibility = Boolean.parseBoolean(parts[15].trim());

            return new Project(projectName, neighborhood, openDate, closeDate, manager, officerSlot, rooms, visibility, officer);

        } catch (DateTimeParseException e) {
            System.err.println("Error parsing date in project from line: " + csvLine + " - " + e.getMessage());
        } catch (NumberFormatException e) {
            System.err.println("Error parsing number (units, price, slots) in project from line: " + csvLine + " - " + e.getMessage());
        } catch (IllegalArgumentException e) {
             System.err.println("Error parsing enum (RoomType) in project from line: " + csvLine + " - " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error parsing project from line: " + csvLine);
            e.printStackTrace();
        }
        return null;
    }

	
}

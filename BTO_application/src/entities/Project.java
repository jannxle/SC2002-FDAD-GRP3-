package entities;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

import control.ProjectManager;

import java.util.ArrayList;

import enums.RoomType;

/**
 * Represents a Build-To-Order (BTO) housing project listing in the system.
 * Contains details such as project name, location, application dates, managing HDB staff,
 * available room types with their units and prices, and visibility status.
 */
public class Project {
	private String projectName;
	private String neighbourhood;
	private LocalDate openDate; //need to parse as it is yyyy/mm/dd format
	private LocalDate closeDate;
	private String manager;
	private int officerSlot;
	private List<Room> rooms;
	private boolean visibility;
	private String officer;

	private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("d/M/yy");
	
	/**
     * Constructs a new Project object.
     *
     * @param projectName   The name of the project.
     * @param neighbourhood The neighbourhood of the project.
     * @param openDate      The application opening date.
     * @param closeDate     The application closing date.
     * @param manager       The name of the manager in charge.
     * @param officerSlot   The number of available officer slots.
     * @param rooms         A list of Room objects representing available flat types.
     * @param visibility    The initial visibility status for applicants.
     * @param officer       The name(s) of assigned officer(s), can be null or empty initially.
     */
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
	
	/**
	 * Gets the name of the project.
	 * @return The project name.
	 */
	public String getName() {
		return projectName;
	}

	/**
	 * Sets the name of the project.
	 * @param projectName The new project name.
	 */
	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}
	
	/**
	 * Gets the neighbourhood of the project.
	 * @return The neighbourhood name.
	 */
	public String getNeighbourhood() {
		return neighbourhood;
	}

	/**
	 * Sets the neighbourhood of the project.
	 * @param neighbourhood The new neighbourhood name.
	 */
	public void setNeighbourhood(String neighbourhood) {
		this.neighbourhood = neighbourhood;
	}

	/**
	 * Gets the application opening date.
	 * @return The opening date as a LocalDate.
	 */
	public LocalDate getOpenDate() {
		return openDate;
	}

	/**
	 * Sets the application opening date.
	 * @param newOpenDate The new opening date.
	 */
	public void setOpenDate(LocalDate newOpenDate) {
		this.openDate = newOpenDate;
	}

	/**
	 * Gets the application closing date.
	 * @return The closing date as a LocalDate.
	 */
	public LocalDate getCloseDate() {
		return closeDate;
	}

	/**
	 * Sets the application closing date.
	 * @param newCloseDate The new closing date.
	 */
	public void setCloseDate(LocalDate newCloseDate) {
		this.closeDate = newCloseDate;
	}

	/**
	 * Gets the name of the HDB Manager in charge.
	 * @return The manager's name.
	 */
	public String getManager() {
		return manager;
	}

	/**
	 * Gets the name(s) of the assigned HDB Officer(s).
	 * @return A string containing officer names, possibly semicolon-separated, or null/empty if none.
	 */
	public String getOfficer() {
		return officer;
	}

	/**
	 * Sets the name(s) of the assigned HDB Officer(s). Overwrites existing names.
	 * @param officer The string containing the new officer name(s).
	 */
	public void setOfficer(String officer) {
	    this.officer = officer;
	}

	/**
	 * Gets the number of available HDB Officer slots for this project.
	 * @return The number of slots.
	 */
	public int getOfficerSlot() {
		return officerSlot;
	}

	/**
	 * Sets the number of available HDB Officer slots.
	 * @param officerSlot The new number of slots.
	 */
	public void setOfficerSlot(int officerSlot) {
	    this.officerSlot = officerSlot;
	}

	/**
	 * Gets the list of Room objects associated with this project.
	 * @return A List of Room objects. Returns an empty list if no rooms are defined.
	 */
	public List<Room> getRooms() {
	    return rooms;
	}

	/**
	 * Sets the list of Room objects for this project. Replaces the existing list.
	 * @param rooms A new List of Room objects.
	 */
	public void setRooms(List<Room> rooms) {
	    this.rooms = rooms;
	}	
	
	/**
	 * Gets the number of currently available units for a specific room type.
	 *
	 * @param type The RoomType to check.
	 * @return The number of available units for the specified type, or 0 if the type is not found in this project.
	 */
	public int getRoomCount(RoomType type) {
	    for (Room room : rooms) {
	        if (room.getRoomType() == type) {
	            return room.getAvailableRooms();
	        }
	    }
	    return 0;
	}
	
	/**
	 * Sets the number of available units for a specific room type.
	 * Finds the matching room in the project's list and updates its available count.
	 *
	 * @param type The RoomType to update.
	 * @param count The new number of available units.
	 */
	public void setRoomCount(RoomType type, int count) {
	    for (Room room : rooms) {
	        if (room.getRoomType() == type) {
	            room.setAvailableRooms(count);
	            return;
	        }
	    }
	}
	
	/**
	 * Gets the Room object corresponding to a specific room type within this project.
	 *
	 * @param type The RoomType to retrieve.
	 * @return The matching Room object, or null if the type is not found in this project.
	 */
	public Room getRoom(RoomType type) {
	    for (Room room : rooms) {
	        if (room.getRoomType() == type) {
	            return room;
	        }
	    }
	    return null;
	}
	
	/**
	 * Checks if the project is currently visible to applicants.
	 * @return true if visible, false otherwise.
	 */
	public boolean isVisibility() {
		return visibility;
	}

	/**
	 * Sets the visibility status of the project for applicants.
	 * @param visibility The new visibility status (true for visible, false for hidden).
	 */
	public void setVisibility(boolean visibility) {
		this.visibility = visibility;
	}
	
	/**
	 * Adds an officer's name to the list of assigned officers.
	 * If no officers are currently assigned, the name is set directly.
	 * If officers are already assigned, the name is appended using a semicolon separator,
	 * only if the name is not already present.
	 *
	 * @param name The name of the officer to add.
	 */
	public void addOfficer(String name) {
	    if (officer == null || officer.isEmpty()) {
	        officer = name;
	    } else if (!officer.contains(name)) {
	        officer += ";" + name;
	    }
	}
	
	/**
     * Parses a CSV string line and creates a Project object from it.
     * Handles potential errors during parsing (e.g., incorrect number format, invalid date format, missing columns).
     * Assumes a specific CSV structure as defined in `ProjectList.csv`.
     *
     * @param csvLine A single line string from the ProjectList.csv file.
     * @return A Project object parsed from the line, or null if parsing fails.
     */
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

package entities;

import enums.RoomType;

/**
 * Represents a set of filter criteria that users can apply when viewing lists of BTO projects.
 * Filters can be set based on neighbourhood and/or room type.
 * These filter settings are typically saved per user.
 */
public class Filter {
	private String neighbourhood;
	private RoomType roomType;
	
	/**
	 * Default constructor. Creates a Filter object with no criteria set (all filters off).
	 */
	public Filter() {}
	
	/**
	 * Constructs a Filter object with initial criteria.
	 *
	 * @param neighbourhood The neighbourhood to filter by (can be null).
	 * @param roomType      The RoomType to filter by (can be null).
	 */
	public Filter(String neighbourhood, RoomType roomType) {
		this.neighbourhood = neighbourhood;
		this.roomType= roomType;
	}
	
	/**
	 * Gets the currently set neighbourhood filter.
	 * @return The neighbourhood string, or null if no neighbourhood filter is set.
	 */
	public String getNeighbourhood() {
		return neighbourhood;
	}
	
	/**
	 * Sets the neighbourhood filter.
	 * @param neighbourhood The neighbourhood string to filter by. Set to null or empty string to remove the filter.
	 */
	public void setNeighbourhood(String neighbourhood) {
		this.neighbourhood = neighbourhood;
	}
	
	/**
	 * Gets the currently set room type filter.
	 * @return The RoomType, or null if no room type filter is set.
	 */
	public RoomType getRoomType() {
		return roomType;
	}
	
	/**
	 * Sets the room type filter.
	 * @param roomType The RoomType to filter by. Set to null to remove the filter.
	 */
	public void setRoomType(RoomType roomType) {
		this.roomType = roomType;
	}

	/**
	 * Resets all filter criteria, effectively turning off all filters.
	 * Sets neighbourhood and roomType back to null.
	 */
	public void reset() {
		this.neighbourhood = null;
		this.roomType = null;
	}
	
	/**
	 * Checks if a given Project matches the currently set filter criteria.
	 * A project matches if:
	 * - No filters are set, OR
	 * - The project's neighbourhood matches the filter, if the neighbourhood filter is set, AND
	 * - The project offers the specified room type, if the room type filter is set.
	 *
	 * @param p The Project to check against the filters.
	 * @return true if the project matches the filter criteria, false otherwise.
	 */
    public boolean matches(Project p) {
        if (neighbourhood != null && !p.getNeighbourhood().equalsIgnoreCase(neighbourhood)) return false;
        if (roomType != null && (p.getRooms().stream().noneMatch(r -> r.getRoomType() == roomType))) return false;
        return true;
    }
}

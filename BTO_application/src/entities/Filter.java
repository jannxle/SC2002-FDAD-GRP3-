package entities;

import enums.RoomType;

public class Filter {
	private String neighbourhood;
	private RoomType roomType;
	
	public Filter() {}
	
	public Filter(String neighbourhood, RoomType roomType) {
		this.neighbourhood = neighbourhood;
		this.roomType= roomType;
	}
	
	public String getNeighbourhood() {
		return neighbourhood;
	}
	
	public void setNeighbourhood(String neighbourhood) {
		this.neighbourhood = neighbourhood;
	}
	
	public RoomType getRoomType() {
		return roomType;
	}
	
	public void setRoomType(RoomType roomType) {
		this.roomType = roomType;
	}

	public void reset() {
		this.neighbourhood = null;
		this.roomType = null;
	}
	
    public boolean matches(Project p) {
        if (neighbourhood != null && !p.getNeighbourhood().equalsIgnoreCase(neighbourhood)) return false;
        if (roomType != null && (p.getRooms().stream().noneMatch(r -> r.getRoomType() == roomType))) return false;
        return true;
    }
}

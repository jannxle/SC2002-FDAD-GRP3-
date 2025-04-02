package entities;

import enums.RoomType;

public class Room {
	private RoomType type;
	private int totalRooms;
	private int availableRooms;
	private double price;
	
	public Room(RoomType type, int totalRooms, double price) {
		this.type = type;
		this.totalRooms = totalRooms;
		this.price = price;
		this.availableRooms = totalRooms; //initially all rooms are available
	}
	
	public RoomType getRoomType() {
		return type;
	}
	public int getTotalRooms() {
		return totalRooms;
	}
	public void setTotalRooms(int totalRooms) {
		this.totalRooms = totalRooms;
	}
	public int getAvailableRooms() {
		return availableRooms;
	}
	public void setAvailableRooms(int availableRooms) {
		this.availableRooms = availableRooms;
	}
	public double getPrice() {
		return price;
	}
	public void setPrice(double price) {
		this.price = price;
	}
	public boolean incrementAvailableRooms() {
		if(availableRooms < totalRooms) {
			availableRooms++;
			return true;
		}
		return false;
	}
	
	public boolean decrementAvailableRooms() {
		if (availableRooms > 0) {
			availableRooms--;
			return true;
		}
		return false;
	}
	
    @Override
    public String toString() {
        return "Room{" +
               "type=" + type +
               ", total Number of Rooms=" + totalRooms +
               ", price=" + price +
               ", availableNumber of Rooms=" + availableRooms +
               '}';
    }
}


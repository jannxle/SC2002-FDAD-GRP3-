package entities;

import enums.RoomType;

public class Room {
	private RoomType type;
	private int totalRooms;
	private int availableRooms;
	private double price;

	public Room(RoomType type, int totalRooms, int availableRooms, double price) {
		this.type = type;
		this.totalRooms = totalRooms;
		this.price = price;
		this.availableRooms = Math.min(availableRooms, totalRooms);
		if (availableRooms > totalRooms) {
		    System.err.println("Warning: Initial available rooms (" + availableRooms +
		                       ") greater than total rooms (" + totalRooms +
		                       ") for " + type + ". Setting available to total.");
		}
	}

	public RoomType getRoomType() {
		return type;
	}
	public int getTotalRooms() {
		return totalRooms;
	}
	public int getAvailableRooms() {
		return availableRooms;
	}
	public void setAvailableRooms(int availableRooms) {
		this.availableRooms = Math.max(0, Math.min(availableRooms, this.totalRooms));
	}
	public double getPrice() {
		return price;
	}
	public void setPrice(double price) {
		this.price = price;
	}
	
	//used for manager to edit project information
	public void increaseRoomSupply(int additionalUnits) {
	    if (additionalUnits > 0) {
	        this.totalRooms += additionalUnits;
	        this.availableRooms += additionalUnits;
	    } else {
	        System.err.println("Cannot increase room supply by non-positive number.");
	    }
	}
	
	public boolean incrementAvailableRooms() {
		if(availableRooms < totalRooms) {
			availableRooms++;
			return true;
		}
		System.err.println("Increment failed: Available rooms already at maximum (" + availableRooms + "/" + totalRooms + ").");
		return false;
	}

	public boolean decrementAvailableRooms() {
		if (availableRooms > 0) {
			availableRooms--;
			return true;
		}
		System.err.println("Decrement failed: No available rooms left (" + availableRooms + "/" + totalRooms + ").");
		return false;
	}

    @Override
    public String toString() {
        return "Room{" +
               "type=" + type +
               ", totalRooms=" + totalRooms +
               ", price=" + price +
               ", availableRooms=" + availableRooms +
               '}';
    }
}
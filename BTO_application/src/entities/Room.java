package entities;

import enums.RoomType;

/**
 * Represents a specific type of flat (room) offered within a BTO Project.
 * Stores details about the room type (e.g., TwoRoom, ThreeRoom), the total number
 * of units, the currently available units, and the selling price.
 * Provides methods to manage the availability count.
 */
public class Room {
	private RoomType type;
	private int totalRooms;
	private int availableRooms;
	private double price;

	/**
	 * Constructs a new Room object.
	 * Initializes the room with its type, total units, initial available units, and price.
	 * Ensures that the initial available units do not exceed the total units.
	 *
	 * @param type           The RoomType of the flat.
	 * @param totalRooms     The total number of units of this type.
	 * @param availableRooms The initial number of available units (will be capped at totalRooms).
	 * @param price          The selling price per unit.
	 */
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

	/**
	 * Gets the type of the room.
	 * @return The RoomType.
	 */
	public RoomType getRoomType() {
		return type;
	}

	/**
	 * Gets the total number of units for this room type.
	 * @return The total number of units.
	 */
	public int getTotalRooms() {
		return totalRooms;
	}

	/**
	 * Gets the currently available number of units for this room type.
	 * @return The number of available units.
	 */
	public int getAvailableRooms() {
		return availableRooms;
	}

	/**
	 * Sets the number of available units for this room type.
	 * The value is automatically capped between 0 and the total number of rooms.
	 *
	 * @param availableRooms The new number of available units.
	 */
	public void setAvailableRooms(int availableRooms) {
		this.availableRooms = Math.max(0, Math.min(availableRooms, this.totalRooms));
	}

	/**
	 * Gets the selling price per unit for this room type.
	 * @return The price.
	 */
	public double getPrice() {
		return price;
	}

	/**
	 * Sets the selling price per unit for this room type.
	 * @param price The new price.
	 */
	public void setPrice(double price) {
		this.price = price;
	}
	
	/**
	 * Increases the total supply and available units of this room type.
	 * This method is intended for use by HDB Managers when editing project details
	 * to add more units after the initial creation.
	 *
	 * @param additionalUnits The number of units to add. Must be a positive number.
	 */
	public void increaseRoomSupply(int additionalUnits) {
	    if (additionalUnits > 0) {
	        this.totalRooms += additionalUnits;
	        this.availableRooms += additionalUnits;
	    } else {
	        System.err.println("Cannot increase room supply by non-positive number.");
	    }
	}
	
	/**
	 * Increments the count of available rooms by one.
	 * This is typically used when an application withdrawal is approved,
	 * making a previously allocated unit available again.
	 * Does not increment if the available count is already equal to the total count.
	 *
	 * @return true if the count was successfully incremented, false otherwise (e.g., already at max).
	 */
	public boolean incrementAvailableRooms() {
		if(availableRooms < totalRooms) {
			availableRooms++;
			return true;
		}
		System.err.println("Increment failed: Available rooms already at maximum (" + availableRooms + "/" + totalRooms + ").");
		return false;
	}

	/**
	 * Decrements the count of available rooms by one.
	 * This is typically used when an application is approved or a flat is booked,
	 * reducing the number of available units.
	 * Does not decrement if the available count is already zero.
	 *
	 * @return true if the count was successfully decremented, false otherwise (e.g., already at zero).
	 */
	public boolean decrementAvailableRooms() {
		if (availableRooms > 0) {
			availableRooms--;
			return true;
		}
		System.err.println("Decrement failed: No available rooms left (" + availableRooms + "/" + totalRooms + ").");
		return false;
	}

	/**
     * Returns a string representation of the Room object, including its type,
     * total units, price, and currently available units.
     *
     * @return A string summarizing the Room's details.
     */
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
package control;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import entities.Filter;
import enums.RoomType;

/**
 * Manages user-specific filter preferences for viewing BTO projects.
 * Handles loading saved filters from a CSV file, retrieving filters for a user,
 * setting filters for a user, and saving all filters back to the CSV file.
 */
public class FilterManager {
	private Map<String, Filter> userFilters = new HashMap<>();
    private final String filePath;
    
    /**
     * Constructs a FilterManager and loads existing filter settings from the specified file path.
     *
     * @param filePath The path to the CSV file used for storing and loading filter settings.
     */
    public FilterManager(String filePath) {
    	this.filePath = filePath;
    	loadFilters();
    }
    
    /**
     * Retrieves the saved filter settings for a specific user.
     * If no filter is saved for the user, a default Filter object (with no criteria set) is returned.
     *
     * @param nric The NRIC of the user whose filter settings are requested.
     * @return The Filter object for the user, or a default Filter if none exists.
     */
    public Filter getFilter(String nric) {
        return userFilters.getOrDefault(nric, new Filter());
    }

    /**
     * Sets or updates the filter settings for a specific user.
     *
     * @param nric   The NRIC of the user whose filter is being set.
     * @param filter The Filter object containing the desired settings.
     */
    public void setFilter(String nric, Filter filter) {
        userFilters.put(nric, filter);
    }

    /**
     * Saves the current filter settings for all users to the CSV file.
     * Overwrites the existing file content.
     * Format: NRIC,Neighbourhood,RoomType
     */
    public void saveFilters() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write("NRIC,Neighbourhood,RoomType\n");
            for (Map.Entry<String, Filter> entry : userFilters.entrySet()) {
                String nric = entry.getKey();
                Filter filter = entry.getValue();
                writer.write(String.format("%s,%s,%s\n",
                        nric,
                        filter.getNeighbourhood() != null ? filter.getNeighbourhood() : "",
                        filter.getRoomType() != null ? filter.getRoomType().name() : ""));
            }
        } catch (IOException e) {
            System.out.println("Failed to save filter settings: " + e.getMessage());
        }
    }

    /**
     * Loads filter settings from the CSV file into memory.
     * Clears existing in-memory filters before loading.
     * Handles file not found errors and potential issues during parsing.
     * Format: NRIC,Neighbourhood,RoomType
     */
    public void loadFilters() {
        File file = new File(filePath);
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine(); // skip header
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", -1);
                if (parts.length != 3) continue;

                String nric = parts[0].trim();
                String neighbourhood = parts[1].trim().isEmpty() ? null : parts[1].trim();
                RoomType roomType = parts[2].trim().isEmpty() ? null : RoomType.valueOf(parts[2].trim());

                userFilters.put(nric, new Filter(neighbourhood, roomType));
            }
        } catch (IOException e) {
            System.out.println("Failed to load filter settings: " + e.getMessage());
        }
    }
}


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

public class FilterManager {
	private Map<String, Filter> userFilters = new HashMap<>();
    private final String filePath;
    
    public FilterManager(String filePath) {
    	this.filePath = filePath;
    	loadFilters();
    }
    
    /**
     * Returns the saved filters for a user by NRIC, or default if not found.
     */
    public Filter getFilter(String nric) {
        return userFilters.getOrDefault(nric, new Filter());
    }

    /**
     * Saves filter settings for a given user.
     */
    public void setFilter(String nric, Filter filter) {
        userFilters.put(nric, filter);
    }

    /**
     * Writes all filter settings to the CSV file.
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
     * Loads all filter settings from the CSV file.
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


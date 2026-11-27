package service;

import model.Room;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

/**
 * Reads data/rooms.txt
 * Format per line:  id|name|pricePerNight|maxGuests|units|summary|description|amenity1;amenity2;...|imagePath
 */
public class RoomFileHandler {

    public static final String ROOMS_FILE = "data/rooms.txt";

    // FILE READING: load every room from the text file
    public static ArrayList<Room> loadRooms() {
        ArrayList<Room> rooms = new ArrayList<>();
        File file = new File(ROOMS_FILE);
        if (!file.exists()) {
            return rooms;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                String[] p = line.split("\\|", -1);
                if (p.length != 9) {
                    continue;
                }
                try {
                    rooms.add(new Room(p[0], p[1], Double.parseDouble(p[2]), Integer.parseInt(p[3]),
                            Integer.parseInt(p[4]), p[5], p[6], p[7].split(";"), p[8]));
                } catch (NumberFormatException e) {
                    System.out.println("Skipping bad room line: " + line);
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading rooms file: " + e.getMessage());
        }
        return rooms;
    }
}

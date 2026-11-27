package service;

import model.Reservation;
import model.Room;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;

/** Reads and writes data/reservations.txt (format is documented in model/Reservation.java) */
public class ReservationFileHandler {

    public static final String RESERVATIONS_FILE = "data/reservations.txt";

    // FILE READING
    public static ArrayList<Reservation> loadAll() {
        ArrayList<Reservation> list = new ArrayList<>();
        File file = new File(RESERVATIONS_FILE);
        if (!file.exists()) {
            return list;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                Reservation r = Reservation.fromLine(line);
                if (r != null) {
                    list.add(r);
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading reservations file: " + e.getMessage());
        }
        return list;
    }

    // FILE WRITING: overwrite the file with the current list
    public static boolean saveAll(ArrayList<Reservation> list) {
        File file = new File(RESERVATIONS_FILE);
        File folder = file.getParentFile();
        if (folder != null && !folder.exists()) {
            folder.mkdirs();
        }
        try (PrintWriter pw = new PrintWriter(new FileWriter(file, StandardCharsets.UTF_8))) {
            for (Reservation r : list) {
                pw.println(r.toFileLine());
            }
            return true;
        } catch (IOException e) {
            System.out.println("Error saving reservations file: " + e.getMessage());
            return false;
        }
    }

    public static boolean addReservation(Reservation r) {
        ArrayList<Reservation> list = loadAll();
        list.add(r);
        return saveAll(list);
    }

    public static ArrayList<Reservation> loadForUser(String username) {
        ArrayList<Reservation> mine = new ArrayList<>();
        for (Reservation r : loadAll()) {
            if (r.getUsername().equalsIgnoreCase(username)) {
                mine.add(r);
            }
        }
        return mine;
    }

    /**
     * How many units of this room type are still free for every night in [checkIn, checkOut).
     * Only CONFIRMED bookings use up a unit; cancelled ones don't.
     */
    public static int roomsLeft(Room room, LocalDate checkIn, LocalDate checkOut) {
        ArrayList<Reservation> all = loadAll();
        int mostUsed = 0;
        for (LocalDate night = checkIn; night.isBefore(checkOut); night = night.plusDays(1)) {
            int used = 0;
            for (Reservation r : all) {
                boolean sameRoom = r.getRoomId().equals(room.getId());
                boolean staying = !night.isBefore(r.getCheckIn()) && night.isBefore(r.getCheckOut());
                if (sameRoom && staying && Reservation.CONFIRMED.equals(r.getStatus())) {
                    used++;
                }
            }
            mostUsed = Math.max(mostUsed, used);
        }
        return Math.max(0, room.getUnits() - mostUsed);
    }

    public static boolean isRoomAvailable(Room room, LocalDate checkIn, LocalDate checkOut) {
        return roomsLeft(room, checkIn, checkOut) > 0;
    }

    /** Next id like TT-2026-0001 */
    public static String nextBookingId() {
        int max = 0;
        for (Reservation r : loadAll()) {
            String id = r.getBookingId();
            try {
                max = Math.max(max, Integer.parseInt(id.substring(id.lastIndexOf('-') + 1)));
            } catch (NumberFormatException e) {
                // ignore ids we can't read
            }
        }
        return String.format("TT-%d-%04d", LocalDate.now().getYear(), max + 1);
    }

    public static boolean cancel(String bookingId) {
        ArrayList<Reservation> list = loadAll();
        for (Reservation r : list) {
            if (r.getBookingId().equals(bookingId)) {
                r.setStatus(Reservation.CANCELLED);
                return saveAll(list);
            }
        }
        return false;
    }
}

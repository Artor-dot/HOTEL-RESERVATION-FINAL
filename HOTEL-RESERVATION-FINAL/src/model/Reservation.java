package model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

/**
 * One saved booking.
 * Line format in data/reservations.txt:
 * bookingId|username|guestName|contact|roomId|roomName|checkIn|checkOut|guests|addOns|payment|total|status
 */
public class Reservation {
    public static final String CONFIRMED = "CONFIRMED";
    public static final String CANCELLED = "CANCELLED";

    private final String bookingId;
    private final String username;
    private final String guestName;
    private final String contact;
    private final String roomId;
    private final String roomName;
    private final LocalDate checkIn;
    private final LocalDate checkOut;
    private final int guests;
    private final String addOns;
    private final String payment;
    private final double total;
    private String status;

    public Reservation(String bookingId, String username, String guestName, String contact,
                       String roomId, String roomName, LocalDate checkIn, LocalDate checkOut,
                       int guests, String addOns, String payment, double total, String status) {
        this.bookingId = bookingId;
        this.username = username;
        this.guestName = guestName;
        this.contact = contact;
        this.roomId = roomId;
        this.roomName = roomName;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.guests = guests;
        this.addOns = addOns;
        this.payment = payment;
        this.total = total;
        this.status = status;
    }

    public String getBookingId() { return bookingId; }
    public String getUsername() { return username; }
    public String getGuestName() { return guestName; }
    public String getContact() { return contact; }
    public String getRoomId() { return roomId; }
    public String getRoomName() { return roomName; }
    public LocalDate getCheckIn() { return checkIn; }
    public LocalDate getCheckOut() { return checkOut; }
    public int getGuests() { return guests; }
    public String getAddOns() { return addOns; }
    public String getPayment() { return payment; }
    public double getTotal() { return total; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getNights() {
        return (int) ChronoUnit.DAYS.between(checkIn, checkOut);
    }

    public String toFileLine() {
        return String.join("|", bookingId, username, guestName, contact, roomId, roomName,
                checkIn.toString(), checkOut.toString(), String.valueOf(guests), addOns, payment,
                String.format(Locale.US, "%.2f", total), status);
    }

    /** Returns null if the line is not a valid reservation. */
    public static Reservation fromLine(String line) {
        String[] p = line.split("\\|", -1);
        if (p.length != 13) {
            return null;
        }
        try {
            return new Reservation(p[0], p[1], p[2], p[3], p[4], p[5],
                    LocalDate.parse(p[6]), LocalDate.parse(p[7]), Integer.parseInt(p[8]),
                    p[9], p[10], Double.parseDouble(p[11]), p[12]);
        } catch (RuntimeException e) { // bad date / number
            return null;
        }
    }
}

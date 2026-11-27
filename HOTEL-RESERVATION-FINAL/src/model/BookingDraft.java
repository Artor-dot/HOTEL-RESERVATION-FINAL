package model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Locale;

/**
 * The booking the user is currently building. One instance is shared by the
 * Browse, Details and Checkout pages so the choices carry over between screens.
 * All the price math lives here so every screen shows the same numbers.
 */
public class BookingDraft {
    // Change these three numbers to change the pricing everywhere
    public static final double AIRPORT_TRANSFER_FEE = 800.00;  // flat, in pesos
    public static final double CLEANING_PER_NIGHT = 300.00;
    public static final double SERVICE_FEE_PER_NIGHT = 250.00;

    public static final String PAY_CASH = "Cash at Front Desk";
    public static final String PAY_CARD = "Credit / Debit Card";
    public static final String PAY_EWALLET = "E-Wallet (GCash / Maya)";

    private Room room;
    private LocalDate checkIn = LocalDate.now().plusDays(1);
    private LocalDate checkOut = LocalDate.now().plusDays(4);
    private int guests = 2;
    private boolean airportTransfer;
    private boolean dailyCleaning;
    private String paymentMethod = PAY_CASH;

    public Room getRoom() { return room; }
    public void setRoom(Room room) { this.room = room; }
    public LocalDate getCheckIn() { return checkIn; }
    public LocalDate getCheckOut() { return checkOut; }
    public void setDates(LocalDate in, LocalDate out) { this.checkIn = in; this.checkOut = out; }
    public int getGuests() { return guests; }
    public void setGuests(int guests) { this.guests = guests; }
    public boolean isAirportTransfer() { return airportTransfer; }
    public void setAirportTransfer(boolean v) { this.airportTransfer = v; }
    public boolean isDailyCleaning() { return dailyCleaning; }
    public void setDailyCleaning(boolean v) { this.dailyCleaning = v; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public int nights() {
        return (int) Math.max(0, ChronoUnit.DAYS.between(checkIn, checkOut));
    }

    public double roomSubtotal() { return room == null ? 0 : room.getPricePerNight() * nights(); }
    public double airportFee() { return airportTransfer ? AIRPORT_TRANSFER_FEE : 0; }
    public double cleaningFee() { return dailyCleaning ? CLEANING_PER_NIGHT * nights() : 0; }
    public double serviceFee() { return SERVICE_FEE_PER_NIGHT * nights(); }

    public double total() {
        return roomSubtotal() + airportFee() + cleaningFee() + serviceFee();
    }

    /** e.g. ["Airport Transfer (₱800.00)", "Daily Room Cleaning (₱900.00)"]; empty if none chosen */
    public ArrayList<String> addOnLines() {
        ArrayList<String> lines = new ArrayList<>();
        if (airportTransfer) {
            lines.add(String.format(Locale.US, "Airport Transfer (\u20B1%,.2f)", airportFee()));
        }
        if (dailyCleaning) {
            lines.add(String.format(Locale.US, "Daily Room Cleaning (\u20B1%,.2f)", cleaningFee()));
        }
        return lines;
    }
}

package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * One payment, linked to a booking.
 * Line format in data/payments.txt:
 * paymentId|bookingId|username|method|reference|amount|status|timestamp
 *
 * Only a masked reference (e.g. "Visa ending 4242") is stored - never a full card number or CVV.
 */
public class Payment {
    public static final String PAID = "PAID";         // money received
    public static final String PENDING = "PENDING";   // cash to be paid at the front desk
    public static final String REFUNDED = "REFUNDED"; // booking cancelled after it was paid
    public static final String VOID = "VOID";         // booking cancelled before it was paid

    private static final DateTimeFormatter SHOW = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final String paymentId;
    private final String bookingId;
    private final String username;
    private final String method;
    private String reference;
    private final double amount;
    private String status;
    private final LocalDateTime timestamp;

    public Payment(String paymentId, String bookingId, String username, String method,
                   String reference, double amount, String status, LocalDateTime timestamp) {
        this.paymentId = paymentId;
        this.bookingId = bookingId;
        this.username = username;
        this.method = method;
        this.reference = reference;
        this.amount = amount;
        this.status = status;
        this.timestamp = timestamp;
    }

    public String getPaymentId() { return paymentId; }
    public String getBookingId() { return bookingId; }
    public String getUsername() { return username; }
    public String getMethod() { return method; }
    public String getReference() { return reference; }
    public double getAmount() { return amount; }
    public String getStatus() { return status; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getDisplayDate() { return timestamp.format(SHOW); }

    public void setStatus(String status) { this.status = status; }
    public void setReference(String reference) { this.reference = reference; }

    public String toFileLine() {
        return String.join("|", paymentId, bookingId, username, method, reference,
                String.format(Locale.US, "%.2f", amount), status, timestamp.toString());
    }

    /** Returns null if the line is not a valid payment. */
    public static Payment fromLine(String line) {
        String[] p = line.split("\\|", -1);
        if (p.length != 8) {
            return null;
        }
        try {
            return new Payment(p[0], p[1], p[2], p[3], p[4], Double.parseDouble(p[5]), p[6],
                    LocalDateTime.parse(p[7]));
        } catch (RuntimeException e) {
            return null;
        }
    }
}

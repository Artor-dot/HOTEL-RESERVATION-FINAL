package service;

import model.Payment;
import model.Reservation;

import java.util.Locale;

/** Actions that touch both reservations and payments, so they always stay in sync. */
public class BookingService {

    /**
     * Cancels a reservation and settles its payment:
     * PAID -> REFUNDED, PENDING (cash not yet collected) -> VOID.
     * Returns a message to show the user, or null if the file could not be updated.
     */
    public static String cancel(Reservation r) {
        if (!ReservationFileHandler.cancel(r.getBookingId())) {
            return null;
        }
        Payment p = PaymentFileHandler.findByBooking(r.getBookingId());
        if (p == null) {
            return "Reservation " + r.getBookingId() + " cancelled.";
        }
        if (Payment.PAID.equals(p.getStatus())) {
            PaymentFileHandler.updateStatus(p.getPaymentId(), Payment.REFUNDED);
            return String.format(Locale.US, "Reservation %s cancelled.%nA refund of \u20B1%,.2f will be returned to %s (%s).",
                    r.getBookingId(), p.getAmount(), p.getMethod(), p.getReference());
        }
        if (Payment.PENDING.equals(p.getStatus())) {
            PaymentFileHandler.updateStatus(p.getPaymentId(), Payment.VOID);
            return "Reservation " + r.getBookingId() + " cancelled.\nNo payment had been collected.";
        }
        return "Reservation " + r.getBookingId() + " cancelled.";
    }
}

package ui;

import model.Payment;
import model.Reservation;
import service.BookingService;
import service.PaymentFileHandler;
import service.ReservationFileHandler;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;

/** Admin: every reservation from every guest, with the option to cancel one (payment is refunded / voided). */
public class AdminReservationsPanel extends JPanel {

    private final ReadOnlyTable table = new ReadOnlyTable(
            new String[]{"Booking ID", "Guest", "Room", "Check-In", "Check-Out", "Total", "Payment", "Status"},
            new int[]{100, 140, 115, 80, 80, 80, 70, 80});
    private final ArrayList<Reservation> shown = new ArrayList<>();
    private final JTextField search = new JTextField();
    private final JLabel infoLabel = new JLabel(" ");

    public AdminReservationsPanel() {
        setLayout(new BorderLayout(0, 10));
        setBackground(Theme.CREAM);
        setBorder(BorderFactory.createEmptyBorder(25, 25, 20, 25));

        JPanel top = new JPanel(new BorderLayout(0, 10));
        top.setOpaque(false);
        top.add(Theme.title("ALL RESERVATIONS"), BorderLayout.NORTH);
        top.add(Theme.searchRow(search), BorderLayout.CENTER);
        add(top, BorderLayout.NORTH);

        table.statusColumn(6);
        table.statusColumn(7);
        table.alignRight(5);
        add(table.inScrollPane(), BorderLayout.CENTER);

        JButton cancel = Theme.dangerButton("CANCEL RESERVATION");
        cancel.setPreferredSize(new java.awt.Dimension(190, 38));
        cancel.addActionListener(e -> cancelSelected());
        JButton refresh = Theme.outlineButton("REFRESH");
        refresh.setPreferredSize(new java.awt.Dimension(110, 38));
        refresh.addActionListener(e -> refresh());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setOpaque(false);
        buttons.add(refresh);
        buttons.add(cancel);
        JPanel south = new JPanel(new BorderLayout());
        south.setOpaque(false);
        infoLabel.setFont(Theme.LABEL);
        south.add(infoLabel, BorderLayout.WEST);
        south.add(buttons, BorderLayout.EAST);
        add(south, BorderLayout.SOUTH);

        Theme.onTextChange(search, this::refresh);
    }

    /** Called by AdminPanel every time this page is shown. FILE READING: reservations + payments */
    public void refresh() {
        String q = search.getText().trim().toLowerCase();
        HashMap<String, Payment> payments = new HashMap<>();
        for (Payment p : PaymentFileHandler.loadAll()) {
            payments.put(p.getBookingId(), p);
        }

        table.clear();
        shown.clear();
        ArrayList<Reservation> all = ReservationFileHandler.loadAll();
        int confirmed = 0;
        for (int i = all.size() - 1; i >= 0; i--) { // newest first
            Reservation r = all.get(i);
            if (Reservation.CONFIRMED.equals(r.getStatus())) {
                confirmed++;
            }
            String haystack = (r.getBookingId() + " " + r.getGuestName() + " " + r.getUsername()
                    + " " + r.getRoomName()).toLowerCase();
            if (!q.isEmpty() && !haystack.contains(q)) {
                continue;
            }
            Payment p = payments.get(r.getBookingId());
            shown.add(r);
            table.model().addRow(new Object[]{r.getBookingId(),
                    r.getGuestName() + " (" + r.getUsername() + ")", r.getRoomName(),
                    r.getCheckIn(), r.getCheckOut(), Theme.money(r.getTotal()),
                    p == null ? "-" : p.getStatus(), r.getStatus()});
        }
        infoLabel.setText(all.size() + " reservations, " + confirmed + " confirmed");
    }

    private void cancelSelected() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= shown.size()) {
            JOptionPane.showMessageDialog(this, "Please select a reservation first.",
                    "No Selection", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Reservation r = shown.get(row);
        if (Reservation.CANCELLED.equals(r.getStatus())) {
            JOptionPane.showMessageDialog(this, "That reservation is already cancelled.",
                    "Already Cancelled", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (r.getCheckOut().isBefore(LocalDate.now())) {
            JOptionPane.showMessageDialog(this, "That stay is already over and cannot be cancelled.",
                    "Cannot Cancel", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int choice = JOptionPane.showConfirmDialog(this,
                "Cancel booking " + r.getBookingId() + " of " + r.getGuestName() + "?\n"
                        + "A paid booking will be refunded.",
                "Cancel Reservation", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (choice != JOptionPane.YES_OPTION) {
            return;
        }
        String message = BookingService.cancel(r); // FILE WRITING
        if (message == null) {
            JOptionPane.showMessageDialog(this, "Could not update the reservations file.",
                    "File Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        refresh();
        JOptionPane.showMessageDialog(this, message, "Reservation Cancelled", JOptionPane.INFORMATION_MESSAGE);
    }
}

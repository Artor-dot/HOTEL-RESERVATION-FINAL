package ui;

import model.Payment;
import model.Reservation;
import model.User;
import service.BookingService;
import service.PaymentFileHandler;
import service.ReservationFileHandler;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.util.ArrayList;

/**
 * "My Reservations": your bookings from data/reservations.txt in a table (center),
 * with the receipt of the selected booking on the right (same layout as the other pages).
 */
public class ReservationsPanel extends JPanel {

    private final AppPanel app;
    private final User user;
    private final ArrayList<Reservation> shown = new ArrayList<>(); // same order as the table rows

    private final ReadOnlyTable table = new ReadOnlyTable(
            new String[]{"Booking ID", "Room", "From", "To", "Status"},
            new int[]{100, 130, 80, 80, 80});
    private final JLabel infoLabel = new JLabel(" ");

    // receipt panel
    private final JLabel idLabel = new JLabel();
    private final JLabel statusBadge = Theme.badge("CONFIRMED", Theme.GREEN);
    private final JLabel guestVal = new JLabel();
    private final JLabel roomVal = new JLabel();
    private final JLabel datesVal = new JLabel();
    private final JLabel addonsVal = new JLabel();
    private final JLabel paymentVal = new JLabel();
    private final JLabel totalVal = new JLabel();
    private final JLabel emptyHint = new JLabel("<html><body style='width:230px;text-align:center'>"
            + "Select a reservation to see its receipt.</body></html>", SwingConstants.CENTER);
    private JPanel receiptBox;
    private JButton cancelBtn;

    public ReservationsPanel(AppPanel app, User user) {
        this.app = app;
        this.user = user;

        setLayout(new BorderLayout());
        setBackground(Theme.CREAM);
        add(buildList(), BorderLayout.CENTER);
        add(buildReceiptSide(), BorderLayout.EAST);

        table.statusColumn(4);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                showSelected();
            }
        });
    }

    // ------------------------------------------------------------ building

    private JPanel buildList() {
        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(25, 20, 20, 20));
        p.add(Theme.title("MY RESERVATIONS"), BorderLayout.NORTH);
        p.add(table.inScrollPane(), BorderLayout.CENTER);
        infoLabel.setFont(Theme.LABEL);
        p.add(infoLabel, BorderLayout.SOUTH);
        return p;
    }

    private JPanel buildReceiptSide() {
        JPanel side = new JPanel(new BorderLayout(0, 12));
        side.setBackground(Theme.SIDEBAR);
        side.setPreferredSize(new Dimension(290, 0));
        side.setBorder(BorderFactory.createEmptyBorder(25, 20, 25, 20));

        receiptBox = new JPanel(new GridBagLayout());
        receiptBox.setBackground(Theme.CARD);
        receiptBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.GOLD),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));

        JLabel title = new JLabel("BOOKING DETAILS", SwingConstants.CENTER);
        title.setFont(Theme.HEADING.deriveFont(18f));
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 0, 8, 0);
        receiptBox.add(title, c);

        idLabel.setFont(Theme.LABEL.deriveFont(14f));
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(idLabel, BorderLayout.CENTER);
        JPanel badgeHolder = new JPanel(new BorderLayout());
        badgeHolder.setOpaque(false);
        badgeHolder.add(statusBadge, BorderLayout.NORTH);
        header.add(badgeHolder, BorderLayout.EAST);
        c.gridy = 1;
        c.insets = new Insets(0, 0, 6, 0);
        receiptBox.add(header, c);

        c.gridy = 2;
        c.insets = new Insets(0, 0, 4, 0);
        receiptBox.add(new JSeparator(), c);

        addRow(3, "GUEST:", guestVal);
        addRow(4, "ROOM:", roomVal);
        addRow(5, "DATES:", datesVal);
        addRow(6, "ADD-ONS:", addonsVal);
        addRow(7, "PAYMENT:", paymentVal);

        JLabel totalCaption = new JLabel("TOTAL:");
        totalCaption.setFont(Theme.LABEL.deriveFont(14f));
        totalVal.setFont(Theme.HEADING.deriveFont(20f));
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 8;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(8, 0, 0, 8);
        receiptBox.add(totalCaption, c);
        c.gridx = 1;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(8, 0, 0, 0);
        receiptBox.add(totalVal, c);

        // the box and the "nothing selected" hint share the same spot
        emptyHint.setFont(Theme.LABEL.deriveFont(14f));
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(receiptBox, BorderLayout.NORTH);
        top.add(emptyHint, BorderLayout.CENTER);
        side.add(top, BorderLayout.CENTER);

        cancelBtn = Theme.outlineButton("CANCEL RESERVATION");
        cancelBtn.addActionListener(e -> cancelSelected());
        JButton browse = Theme.goldButton("BOOK ANOTHER ROOM");
        browse.addActionListener(e -> app.showPage("browse"));
        JPanel buttons = new JPanel();
        buttons.setOpaque(false);
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.Y_AXIS));
        browse.setAlignmentX(Component.LEFT_ALIGNMENT);
        cancelBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        buttons.add(browse);
        buttons.add(Box.createVerticalStrut(8));
        buttons.add(cancelBtn);
        side.add(buttons, BorderLayout.SOUTH);
        return side;
    }

    private void addRow(int y, String caption, JComponent value) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = y;
        c.gridx = 0;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.insets = new Insets(4, 0, 4, 8);
        JLabel cap = new JLabel(caption);
        cap.setFont(Theme.LABEL.deriveFont(14f));
        cap.setPreferredSize(new Dimension(70, 18));
        receiptBox.add(cap, c);

        c.gridx = 1;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(4, 0, 4, 0);
        value.setFont(Theme.LABEL.deriveFont(14f));
        receiptBox.add(value, c);
    }

    private String wrap(String... lines) {
        StringBuilder sb = new StringBuilder("<html><body style='width:128px'>");
        for (int i = 0; i < lines.length; i++) {
            if (i > 0) {
                sb.append("<br>");
            }
            sb.append(Theme.html(lines[i]));
        }
        return sb.append("</body></html>").toString();
    }

    // ------------------------------------------------------------ data

    /** Called by AppPanel every time this page is shown. FILE READING: data/reservations.txt */
    public void refresh() {
        table.clear();
        shown.clear();

        ArrayList<Reservation> mine = ReservationFileHandler.loadForUser(user.getUsername());
        for (int i = mine.size() - 1; i >= 0; i--) { // newest first
            Reservation r = mine.get(i);
            shown.add(r);
            table.model().addRow(new Object[]{
                    r.getBookingId(), r.getRoomName(), r.getCheckIn(), r.getCheckOut(), r.getStatus()});
        }
        infoLabel.setText(mine.isEmpty()
                ? "No reservations yet. Book a room from Browse Rooms."
                : mine.size() + (mine.size() == 1 ? " reservation" : " reservations"));

        if (shown.isEmpty()) {
            showSelected();
        } else {
            table.setRowSelectionInterval(0, 0);
        }
    }

    private Reservation selected() {
        int row = table.getSelectedRow();
        return row < 0 || row >= shown.size() ? null : shown.get(row);
    }

    private void showSelected() {
        Reservation r = selected();
        receiptBox.setVisible(r != null);
        cancelBtn.setEnabled(r != null);
        emptyHint.setVisible(r == null);
        if (r == null) {
            return;
        }

        boolean cancelled = Reservation.CANCELLED.equals(r.getStatus());
        statusBadge.setText(r.getStatus());
        statusBadge.setBackground(cancelled ? Theme.RED : Theme.GREEN);
        idLabel.setText("<html>BOOKING ID:<br>" + Theme.html(r.getBookingId()) + "</html>");
        guestVal.setText(wrap(r.getGuestName(), r.getContact()));
        roomVal.setText(wrap(r.getRoomName(), r.getGuests() + (r.getGuests() == 1 ? " guest" : " guests")));
        int n = r.getNights();
        datesVal.setText(wrap(r.getCheckIn() + " to", r.getCheckOut().toString(),
                "(" + n + (n == 1 ? " Night)" : " Nights)")));
        addonsVal.setText(wrap(r.getAddOns().split("; ")));

        Payment p = PaymentFileHandler.findByBooking(r.getBookingId());
        paymentVal.setText(p == null
                ? wrap(r.getPayment())
                : wrap(p.getMethod(), p.getReference(), "Status: " + p.getStatus()));
        totalVal.setText(Theme.money(r.getTotal()));
        cancelBtn.setEnabled(!cancelled);
    }

    // ------------------------------------------------------------ actions

    private void cancelSelected() {
        Reservation r = selected();
        if (r == null) {
            JOptionPane.showMessageDialog(this, "Please select a reservation first.",
                    "No Selection", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (Reservation.CANCELLED.equals(r.getStatus())) {
            JOptionPane.showMessageDialog(this, "That reservation is already cancelled.",
                    "Already Cancelled", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (r.getCheckIn().isBefore(LocalDate.now())) {
            JOptionPane.showMessageDialog(this, "Reservations that already started cannot be cancelled.",
                    "Cannot Cancel", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int choice = JOptionPane.showConfirmDialog(this,
                "Cancel booking " + r.getBookingId() + " (" + r.getRoomName() + ")?",
                "Cancel Reservation", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (choice != JOptionPane.YES_OPTION) {
            return;
        }
        String message = BookingService.cancel(r); // FILE WRITING (reservation + payment)
        if (message == null) {
            JOptionPane.showMessageDialog(this, "Could not update the reservations file.",
                    "File Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        refresh();
        JOptionPane.showMessageDialog(this, message, "Reservation Cancelled", JOptionPane.INFORMATION_MESSAGE);
    }
}

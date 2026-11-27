package ui;

import model.Reservation;
import model.User;
import service.BookingService;
import service.FileHandler;
import service.ReservationFileHandler;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
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
 * Admin: every guest account (admins are hidden), with search, booking counts and the blocklist.
 * Center = guest table, right = details of the selected guest + BLOCK / UNBLOCK.
 */
public class AdminGuestsPanel extends JPanel {

    private final ReadOnlyTable table = new ReadOnlyTable(
            new String[]{"Full Name", "Username", "Stays", "Cancels", "Status"},
            new int[]{140, 100, 65, 70, 80});
    private final ArrayList<User> shown = new ArrayList<>(); // same order as the table rows
    private final JTextField search = new JTextField();
    private final JLabel infoLabel = new JLabel(" ");

    // details panel
    private JPanel box;
    private final JLabel emptyHint = new JLabel("Select a guest to see their details.", SwingConstants.CENTER);
    private final JLabel nameVal = new JLabel();
    private final JLabel emailVal = new JLabel();
    private final JLabel userVal = new JLabel();
    private final JLabel bookingsVal = new JLabel();
    private final JLabel cancelledVal = new JLabel();
    private final JLabel bookedVal = new JLabel();
    private final JLabel statusBadge = Theme.badge("ACTIVE", Theme.GREEN);
    private final JLabel reasonVal = new JLabel();
    private JLabel reasonCaption;
    private JButton toggleBtn;
    private JButton viewBtn;

    public AdminGuestsPanel() {
        setLayout(new BorderLayout());
        setBackground(Theme.CREAM);
        add(buildList(), BorderLayout.CENTER);
        add(buildDetailsSide(), BorderLayout.EAST);

        table.statusColumn(4);
        table.alignCenter(2);
        table.alignCenter(3);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                showSelected();
            }
        });
        Theme.onTextChange(search, this::refresh);
    }

    // ------------------------------------------------------------ building

    private JPanel buildList() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(25, 20, 20, 20));

        JPanel top = new JPanel(new BorderLayout(0, 10));
        top.setOpaque(false);
        top.add(Theme.title("GUEST ACCOUNTS"), BorderLayout.NORTH);
        top.add(Theme.searchRow(search), BorderLayout.CENTER);
        p.add(top, BorderLayout.NORTH);

        p.add(table.inScrollPane(), BorderLayout.CENTER);
        infoLabel.setFont(Theme.LABEL);
        p.add(infoLabel, BorderLayout.SOUTH);
        return p;
    }

    private JPanel buildDetailsSide() {
        JPanel side = new JPanel(new BorderLayout(0, 12));
        side.setBackground(Theme.SIDEBAR);
        side.setPreferredSize(new Dimension(290, 0));
        side.setBorder(BorderFactory.createEmptyBorder(25, 20, 25, 20));

        box = new JPanel(new GridBagLayout());
        box.setBackground(Theme.CARD);
        box.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.GOLD),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));

        JLabel title = new JLabel("GUEST DETAILS", SwingConstants.CENTER);
        title.setFont(Theme.HEADING.deriveFont(18f));
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 0, 8, 0);
        box.add(title, c);
        c.gridy = 1;
        c.insets = new Insets(0, 0, 4, 0);
        box.add(new JSeparator(), c);

        Theme.addDetailRow(box, 2, "NAME:", nameVal);
        Theme.addDetailRow(box, 3, "EMAIL:", emailVal);
        Theme.addDetailRow(box, 4, "USERNAME:", userVal);
        Theme.addDetailRow(box, 5, "BOOKINGS:", bookingsVal);
        Theme.addDetailRow(box, 6, "CANCELLED:", cancelledVal);
        Theme.addDetailRow(box, 7, "BOOKED:", bookedVal);
        JPanel badgeHolder = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 0));
        badgeHolder.setOpaque(false);
        badgeHolder.add(statusBadge);
        Theme.addDetailRow(box, 8, "STATUS:", badgeHolder);
        reasonCaption = Theme.addDetailRow(box, 9, "REASON:", reasonVal);

        emptyHint.setFont(Theme.LABEL.deriveFont(14f));
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(box, BorderLayout.NORTH);
        top.add(emptyHint, BorderLayout.CENTER);
        side.add(top, BorderLayout.CENTER);

        viewBtn = Theme.outlineButton("VIEW BOOKINGS");
        viewBtn.addActionListener(e -> viewBookings());
        toggleBtn = Theme.dangerButton("BLOCK GUEST");
        toggleBtn.addActionListener(e -> toggleBlock());
        JPanel buttons = new JPanel();
        buttons.setOpaque(false);
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.Y_AXIS));
        toggleBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        viewBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        buttons.add(toggleBtn);
        buttons.add(Box.createVerticalStrut(8));
        buttons.add(viewBtn);
        side.add(buttons, BorderLayout.SOUTH);
        return side;
    }

    // ------------------------------------------------------------ data

    /** Called by AdminPanel every time this page is shown. FILE READING: users + reservations */
    public void refresh() {
        String selectedUsername = selected() == null ? null : selected().getUsername();
        String q = search.getText().trim().toLowerCase();
        ArrayList<Reservation> all = ReservationFileHandler.loadAll();

        table.clear();
        shown.clear();
        int guests = 0;
        int blocked = 0;
        for (User u : FileHandler.loadUsers()) {
            if (u.isAdmin()) {
                continue;
            }
            guests++;
            if (u.isBlocked()) {
                blocked++;
            }
            String haystack = (u.getFullName() + " " + u.getEmail() + " " + u.getUsername()).toLowerCase();
            if (!q.isEmpty() && !haystack.contains(q)) {
                continue;
            }
            shown.add(u);
            table.model().addRow(new Object[]{u.getFullName(), u.getUsername(),
                    countOf(all, u, true), countOf(all, u, false), u.isBlocked() ? "BLOCKED" : "ACTIVE"});
        }
        infoLabel.setText(guests + (guests == 1 ? " guest" : " guests") + ", " + blocked + " blocked");

        // keep the same guest selected after a refresh
        int keep = 0;
        for (int i = 0; i < shown.size(); i++) {
            if (shown.get(i).getUsername().equals(selectedUsername)) {
                keep = i;
            }
        }
        if (shown.isEmpty()) {
            showSelected();
        } else {
            table.setRowSelectionInterval(keep, keep);
        }
    }

    private int countOf(ArrayList<Reservation> all, User u, boolean confirmedOnes) {
        int n = 0;
        for (Reservation r : all) {
            boolean mine = r.getUsername().equalsIgnoreCase(u.getUsername());
            boolean isConfirmed = Reservation.CONFIRMED.equals(r.getStatus());
            if (mine && isConfirmed == confirmedOnes) {
                n++;
            }
        }
        return n;
    }

    private User selected() {
        int row = table.getSelectedRow();
        return row < 0 || row >= shown.size() ? null : shown.get(row);
    }

    private void showSelected() {
        User u = selected();
        box.setVisible(u != null);
        emptyHint.setVisible(u == null);
        toggleBtn.setEnabled(u != null);
        viewBtn.setEnabled(u != null);
        if (u == null) {
            return;
        }

        ArrayList<Reservation> mine = ReservationFileHandler.loadForUser(u.getUsername());
        int confirmed = 0;
        int cancelled = 0;
        double booked = 0;
        for (Reservation r : mine) {
            if (Reservation.CONFIRMED.equals(r.getStatus())) {
                confirmed++;
                booked += r.getTotal();
            } else {
                cancelled++;
            }
        }

        nameVal.setText(Theme.wrapHtml(135, u.getFullName()));
        emailVal.setText(Theme.wrapHtml(135, u.getEmail()));
        userVal.setText(Theme.wrapHtml(135, u.getUsername()));
        bookingsVal.setText(String.valueOf(confirmed));
        cancelledVal.setText(String.valueOf(cancelled));
        bookedVal.setText(Theme.money(booked));
        statusBadge.setText(u.isBlocked() ? "BLOCKED" : "ACTIVE");
        statusBadge.setBackground(u.isBlocked() ? Theme.RED : Theme.GREEN);
        reasonCaption.setVisible(u.isBlocked());
        reasonVal.setVisible(u.isBlocked());
        reasonVal.setText(Theme.wrapHtml(135, u.getBlockReason()));

        toggleBtn.setText(u.isBlocked() ? "UNBLOCK GUEST" : "BLOCK GUEST");
        toggleBtn.setBackground(u.isBlocked() ? Theme.GREEN : Theme.RED);
    }

    // ------------------------------------------------------------ actions

    private void toggleBlock() {
        User u = selected();
        if (u == null) {
            return;
        }
        if (u.isBlocked()) {
            unblock(u);
        } else {
            block(u);
        }
    }

    private void block(User u) {
        String reason = JOptionPane.showInputDialog(this,
                "Why are you blocking " + u.getUsername() + "?\n(e.g. fake bookings, trolling)",
                "Block Guest", JOptionPane.QUESTION_MESSAGE);
        if (reason == null) {
            return; // pressed Cancel
        }
        reason = reason.trim().replace("|", "/");
        if (reason.isEmpty()) {
            reason = "No reason given";
        }

        String[] options = {"Block only", "Block + cancel upcoming bookings", "Cancel"};
        int choice = JOptionPane.showOptionDialog(this,
                "Block " + u.getFullName() + " (" + u.getUsername() + ")?\n"
                        + "They will not be able to log in, make bookings, or register again with this email.",
                "Confirm Block", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                null, options, options[1]);
        if (choice != 0 && choice != 1) {
            return;
        }

        if (!FileHandler.setBlocked(u.getUsername(), true, reason)) { // FILE WRITING
            JOptionPane.showMessageDialog(this, "Could not update the users file.",
                    "File Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int cancelled = 0;
        if (choice == 1) {
            // free up the rooms this troll was holding (payments are refunded / voided too)
            for (Reservation r : ReservationFileHandler.loadForUser(u.getUsername())) {
                boolean upcoming = !r.getCheckIn().isBefore(LocalDate.now());
                if (Reservation.CONFIRMED.equals(r.getStatus()) && upcoming && BookingService.cancel(r) != null) {
                    cancelled++;
                }
            }
        }
        refresh();
        JOptionPane.showMessageDialog(this,
                u.getUsername() + " has been blocked."
                        + (choice == 1 ? "\n" + cancelled + " upcoming booking(s) cancelled." : ""),
                "Guest Blocked", JOptionPane.INFORMATION_MESSAGE);
    }

    private void unblock(User u) {
        int choice = JOptionPane.showConfirmDialog(this,
                "Unblock " + u.getFullName() + " (" + u.getUsername() + ")?\nThey will be able to log in again.",
                "Unblock Guest", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (choice != JOptionPane.YES_OPTION) {
            return;
        }
        if (FileHandler.setBlocked(u.getUsername(), false, "")) {
            refresh();
            JOptionPane.showMessageDialog(this, u.getUsername() + " has been unblocked.",
                    "Guest Unblocked", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Could not update the users file.",
                    "File Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void viewBookings() {
        User u = selected();
        if (u == null) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (Reservation r : ReservationFileHandler.loadForUser(u.getUsername())) {
            sb.append(String.format("%-13s %-20s %s to %s  %10s  %s%n", r.getBookingId(), r.getRoomName(),
                    r.getCheckIn(), r.getCheckOut(), Theme.money(r.getTotal()), r.getStatus()));
        }
        if (sb.length() == 0) {
            sb.append("This guest has no bookings yet.");
        }
        JTextArea text = new JTextArea(sb.toString());
        text.setEditable(false);
        text.setFont(new java.awt.Font(java.awt.Font.MONOSPACED, java.awt.Font.PLAIN, 12));
        JScrollPane sp = new JScrollPane(text);
        sp.setPreferredSize(new Dimension(620, 200));
        JOptionPane.showMessageDialog(this, sp, "Bookings of " + u.getUsername(), JOptionPane.PLAIN_MESSAGE);
    }
}

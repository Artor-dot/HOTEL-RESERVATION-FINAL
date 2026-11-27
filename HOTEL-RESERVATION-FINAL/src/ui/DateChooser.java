package ui;

import javax.swing.JComboBox;
import java.time.LocalDate;

/**
 * A check-in + check-out pair of dropdowns (yyyy-MM-dd).
 * Check-in offers today .. 1 year ahead; check-out offers 1..30 nights after the check-in.
 * Pick the two boxes up with getCheckInBox()/getCheckOutBox() and place them in any layout.
 */
public class DateChooser {
    private static final int DAYS_AHEAD = 365;
    private static final int MAX_NIGHTS = 30;

    private final JComboBox<String> checkInBox = new JComboBox<>();
    private final JComboBox<String> checkOutBox = new JComboBox<>();
    private final Runnable onChange;
    private boolean updating = false;

    /** onChange runs whenever the USER changes either box (not when setDates is used) */
    public DateChooser(Runnable onChange) {
        this.onChange = onChange;

        LocalDate today = LocalDate.now();
        for (int i = 0; i <= DAYS_AHEAD; i++) {
            checkInBox.addItem(today.plusDays(i).toString());
        }
        fillCheckOut(today);

        Theme.styleCombo(checkInBox);
        Theme.styleCombo(checkOutBox);

        checkInBox.addActionListener(e -> {
            if (updating) {
                return;
            }
            // keep the same length of stay when the check-in moves
            int nights = Math.max(1, checkOutBox.getSelectedIndex() + 1);
            updating = true;
            fillCheckOut(getCheckIn());
            checkOutBox.setSelectedIndex(Math.min(nights, MAX_NIGHTS) - 1);
            updating = false;
            onChange.run();
        });
        checkOutBox.addActionListener(e -> {
            if (!updating) {
                onChange.run();
            }
        });
    }

    private void fillCheckOut(LocalDate checkIn) {
        checkOutBox.removeAllItems();
        for (int i = 1; i <= MAX_NIGHTS; i++) {
            checkOutBox.addItem(checkIn.plusDays(i).toString());
        }
    }

    public JComboBox<String> getCheckInBox() { return checkInBox; }
    public JComboBox<String> getCheckOutBox() { return checkOutBox; }

    public LocalDate getCheckIn() {
        return LocalDate.parse((String) checkInBox.getSelectedItem());
    }

    public LocalDate getCheckOut() {
        return LocalDate.parse((String) checkOutBox.getSelectedItem());
    }

    /** Set both boxes without triggering onChange */
    public void setDates(LocalDate in, LocalDate out) {
        updating = true;
        checkInBox.setSelectedItem(in.toString());
        if (checkInBox.getSelectedItem() == null || !in.toString().equals(checkInBox.getSelectedItem())) {
            checkInBox.setSelectedIndex(0);
        }
        fillCheckOut(getCheckIn());
        checkOutBox.setSelectedItem(out.toString());
        if (!out.toString().equals(checkOutBox.getSelectedItem())) {
            checkOutBox.setSelectedIndex(0);
        }
        updating = false;
    }
}

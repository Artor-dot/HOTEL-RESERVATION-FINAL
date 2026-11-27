package ui;

import model.Payment;
import service.PaymentFileHandler;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ArrayList;

/** Admin: every payment in the system, revenue totals, and MARK AS PAID for cash collected at the desk. */
public class AdminPaymentsPanel extends JPanel {

    private final ReadOnlyTable table = new ReadOnlyTable(
            new String[]{"Booking ID", "Guest", "Method", "Reference", "Amount", "Status", "Date"},
            new int[]{105, 75, 135, 120, 80, 70, 115});
    private final ArrayList<Payment> shown = new ArrayList<>();
    private final JTextField search = new JTextField();
    private final JLabel collectedVal = new JLabel("\u20B10.00", SwingConstants.CENTER);
    private final JLabel pendingVal = new JLabel("\u20B10.00", SwingConstants.CENTER);
    private final JLabel refundedVal = new JLabel("\u20B10.00", SwingConstants.CENTER);

    public AdminPaymentsPanel() {
        setLayout(new BorderLayout(0, 10));
        setBackground(Theme.CREAM);
        setBorder(BorderFactory.createEmptyBorder(25, 25, 20, 25));

        JPanel stats = new JPanel(new GridLayout(1, 3, 12, 0));
        stats.setOpaque(false);
        stats.add(PaymentsPanel.statCard("COLLECTED", collectedVal, Theme.GREEN));
        stats.add(PaymentsPanel.statCard("PENDING AT FRONT DESK", pendingVal, Theme.GOLD));
        stats.add(PaymentsPanel.statCard("REFUNDED", refundedVal, Theme.RED));

        JPanel top = new JPanel(new BorderLayout(0, 10));
        top.setOpaque(false);
        top.add(Theme.title("PAYMENTS"), BorderLayout.NORTH);
        JPanel mid = new JPanel(new BorderLayout(0, 10));
        mid.setOpaque(false);
        mid.add(stats, BorderLayout.NORTH);
        mid.add(Theme.searchRow(search), BorderLayout.CENTER);
        top.add(mid, BorderLayout.CENTER);
        add(top, BorderLayout.NORTH);

        table.statusColumn(5);
        table.alignRight(4);
        add(table.inScrollPane(), BorderLayout.CENTER);

        JButton markPaid = Theme.goldButton("MARK AS PAID");
        markPaid.setPreferredSize(new java.awt.Dimension(160, 38));
        markPaid.addActionListener(e -> markSelectedPaid());
        JButton refresh = Theme.outlineButton("REFRESH");
        refresh.setPreferredSize(new java.awt.Dimension(110, 38));
        refresh.addActionListener(e -> refresh());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setOpaque(false);
        buttons.add(refresh);
        buttons.add(markPaid);
        add(buttons, BorderLayout.SOUTH);

        Theme.onTextChange(search, this::refresh);
    }

    /** Called by AdminPanel every time this page is shown. FILE READING: data/payments.txt */
    public void refresh() {
        String q = search.getText().trim().toLowerCase();
        table.clear();
        shown.clear();

        double collected = 0;
        double pending = 0;
        double refunded = 0;
        ArrayList<Payment> all = PaymentFileHandler.loadAll();
        for (int i = all.size() - 1; i >= 0; i--) { // newest first
            Payment p = all.get(i);
            if (Payment.PAID.equals(p.getStatus())) {
                collected += p.getAmount();
            } else if (Payment.PENDING.equals(p.getStatus())) {
                pending += p.getAmount();
            } else if (Payment.REFUNDED.equals(p.getStatus())) {
                refunded += p.getAmount();
            }
            String haystack = (p.getPaymentId() + " " + p.getBookingId() + " " + p.getUsername()
                    + " " + p.getMethod() + " " + p.getReference()).toLowerCase();
            if (!q.isEmpty() && !haystack.contains(q)) {
                continue;
            }
            shown.add(p);
            table.model().addRow(new Object[]{p.getBookingId(), p.getUsername(),
                    p.getMethod(), p.getReference(), Theme.money(p.getAmount()), p.getStatus(),
                    p.getDisplayDate()});
        }
        collectedVal.setText(Theme.money(collected));
        pendingVal.setText(Theme.money(pending));
        refundedVal.setText(Theme.money(refunded));
    }

    private void markSelectedPaid() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= shown.size()) {
            JOptionPane.showMessageDialog(this, "Please select a payment first.",
                    "No Selection", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Payment p = shown.get(row);
        if (!Payment.PENDING.equals(p.getStatus())) {
            JOptionPane.showMessageDialog(this, "Only PENDING payments can be marked as paid.",
                    "Cannot Mark as Paid", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int choice = JOptionPane.showConfirmDialog(this,
                "Confirm that " + Theme.money(p.getAmount()) + " was received at the front desk\n"
                        + "for booking " + p.getBookingId() + " (" + p.getUsername() + ")?",
                "Mark as Paid", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (choice != JOptionPane.YES_OPTION) {
            return;
        }
        if (PaymentFileHandler.updateStatus(p.getPaymentId(), Payment.PAID)) { // FILE WRITING
            refresh();
            JOptionPane.showMessageDialog(this, "Payment " + p.getPaymentId() + " marked as paid.",
                    "Payment Updated", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Could not update the payments file.",
                    "File Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

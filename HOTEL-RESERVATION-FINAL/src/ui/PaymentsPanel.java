package ui;

import model.Payment;
import model.User;
import service.PaymentFileHandler;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.ArrayList;

/** "Payments": the logged-in guest's payment history from data/payments.txt */
public class PaymentsPanel extends JPanel {

    private final User user;
    private final ReadOnlyTable table = new ReadOnlyTable(
            new String[]{"Payment ID", "Booking ID", "Method", "Reference", "Amount", "Status", "Date"},
            new int[]{95, 95, 125, 130, 80, 80, 115});
    private final JLabel paidVal = new JLabel("\u20B10.00", SwingConstants.CENTER);
    private final JLabel dueVal = new JLabel("\u20B10.00", SwingConstants.CENTER);
    private final JLabel refundVal = new JLabel("\u20B10.00", SwingConstants.CENTER);

    public PaymentsPanel(User user) {
        this.user = user;

        setLayout(new BorderLayout(0, 14));
        setBackground(Theme.CREAM);
        setBorder(BorderFactory.createEmptyBorder(25, 25, 20, 25));

        JPanel top = new JPanel(new BorderLayout(0, 14));
        top.setOpaque(false);
        top.add(Theme.title("PAYMENTS"), BorderLayout.NORTH);
        JPanel stats = new JPanel(new GridLayout(1, 3, 12, 0));
        stats.setOpaque(false);
        stats.add(statCard("PAID", paidVal, Theme.GREEN));
        stats.add(statCard("DUE AT FRONT DESK", dueVal, Theme.GOLD));
        stats.add(statCard("REFUNDED", refundVal, Theme.RED));
        top.add(stats, BorderLayout.CENTER);
        add(top, BorderLayout.NORTH);

        table.statusColumn(5);
        table.alignRight(4);
        add(table.inScrollPane(), BorderLayout.CENTER);
    }

    /** A small summary box: big number on top, caption below (also used by the admin payments page) */
    static JPanel statCard(String caption, JLabel value, java.awt.Color color) {
        value.setFont(Theme.HEADING);
        value.setForeground(color);
        JLabel cap = new JLabel(caption, SwingConstants.CENTER);
        cap.setFont(Theme.LABEL.deriveFont(12f));
        JPanel card = new JPanel(new BorderLayout(0, 2));
        card.setBackground(Theme.CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        card.add(value, BorderLayout.CENTER);
        card.add(cap, BorderLayout.SOUTH);
        return card;
    }

    /** Called by AppPanel every time this page is shown. FILE READING: data/payments.txt */
    public void refresh() {
        table.clear();
        double paid = 0;
        double due = 0;
        double refunded = 0;

        ArrayList<Payment> mine = PaymentFileHandler.loadForUser(user.getUsername());
        for (int i = mine.size() - 1; i >= 0; i--) { // newest first
            Payment p = mine.get(i);
            table.model().addRow(new Object[]{p.getPaymentId(), p.getBookingId(), p.getMethod(),
                    p.getReference(), Theme.money(p.getAmount()), p.getStatus(), p.getDisplayDate()});
            if (Payment.PAID.equals(p.getStatus())) {
                paid += p.getAmount();
            } else if (Payment.PENDING.equals(p.getStatus())) {
                due += p.getAmount();
            } else if (Payment.REFUNDED.equals(p.getStatus())) {
                refunded += p.getAmount();
            }
        }
        paidVal.setText(Theme.money(paid));
        dueVal.setText(Theme.money(due));
        refundVal.setText(Theme.money(refunded));
    }
}

package ui;

import model.BookingDraft;
import model.Payment;
import model.Reservation;
import model.Room;
import model.User;
import service.FileHandler;
import service.PaymentFileHandler;
import service.PaymentService;
import service.ReservationFileHandler;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 * Checkout & Booking Confirmation: payment method + guest info on the left,
 * live reservation receipt on the right.
 * CONFIRM RESERVATION takes the payment (PaymentService), then saves the booking
 * to data/reservations.txt and the payment to data/payments.txt.
 */
public class CheckoutPanel extends JPanel {

    private final AppPanel app;
    private final BookingDraft draft;
    private final User user;

    // payment method
    private final JRadioButton cashBtn = new JRadioButton(BookingDraft.PAY_CASH);
    private final JRadioButton cardBtn = new JRadioButton(BookingDraft.PAY_CARD);
    private final JRadioButton ewalletBtn = new JRadioButton(BookingDraft.PAY_EWALLET);

    // card details (shown only for Credit / Debit Card)
    private final JTextField cardNumber = new JTextField();
    private final JTextField cardName = new JTextField();
    private final JTextField cardExpiry = new JTextField();
    private final JPasswordField cardCvv = new JPasswordField();
    private JPanel cardPanel;

    // e-wallet details (shown only for E-Wallet)
    private final JComboBox<String> walletProvider = new JComboBox<>(new String[]{"GCash", "Maya"});
    private final JTextField walletNumber = new JTextField();
    private JPanel walletPanel;

    // guest info
    private final JTextField guestName = new JTextField();
    private final JTextField contactNumber = new JTextField();
    private final JButton confirmBtn = Theme.goldButton("CONFIRM RESERVATION");

    // receipt
    private final JLabel statusBadge = Theme.badge("PENDING", Theme.GOLD);
    private final JLabel idLabel = new JLabel();
    private final JLabel guestVal = new JLabel();
    private final JLabel roomVal = new JLabel();
    private final JLabel datesVal = new JLabel();
    private final JLabel addonsVal = new JLabel();
    private final JLabel paymentVal = new JLabel();
    private final JLabel totalCaption = new JLabel("TOTAL DUE:");
    private final JLabel totalVal = new JLabel();

    private String previewId = "";
    private Reservation confirmed = null;  // set once the booking is saved
    private Payment confirmedPayment = null;

    public CheckoutPanel(AppPanel app, BookingDraft draft, User user) {
        this.app = app;
        this.draft = draft;
        this.user = user;

        setLayout(new BorderLayout());
        setBackground(Theme.CREAM);
        add(buildForm(), BorderLayout.CENTER);
        add(buildReceiptSide(), BorderLayout.EAST);

        for (JRadioButton b : new JRadioButton[]{cashBtn, cardBtn, ewalletBtn}) {
            b.addActionListener(e -> {
                draft.setPaymentMethod(b.getText());
                showPaymentDetails();
                updateReceipt();
            });
        }
        onType(guestName);
    }

    // ------------------------------------------------------------ building

    private JScrollPane buildForm() {
        JPanel center = new JPanel(new BorderLayout(0, 15));
        center.setOpaque(false);
        center.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        center.add(Theme.title("CHECKOUT & BOOKING CONFIRMATION"), BorderLayout.NORTH);

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Theme.CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER),
                BorderFactory.createEmptyBorder(16, 22, 16, 22)));

        JLabel heading = new JLabel("PAYMENT METHOD");
        heading.setFont(Theme.HEADING);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(heading);
        card.add(Box.createVerticalStrut(4));

        ButtonGroup group = new ButtonGroup();
        for (JRadioButton b : new JRadioButton[]{cashBtn, cardBtn, ewalletBtn}) {
            b.setOpaque(false);
            b.setFont(Theme.LABEL.deriveFont(16f));
            b.setAlignmentX(Component.LEFT_ALIGNMENT);
            group.add(b);
            card.add(b);
        }
        cashBtn.setSelected(true);
        card.add(Box.createVerticalStrut(8));

        // card details: number on top, then name | expiry | cvv
        cardPanel = new JPanel();
        cardPanel.setOpaque(false);
        cardPanel.setLayout(new BoxLayout(cardPanel, BoxLayout.Y_AXIS));
        cardPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        cardPanel.add(labeled("CARD NUMBER (test: 4242 4242 4242 4242)", cardNumber));
        JPanel cardRow = new JPanel(new GridBagLayout());
        cardRow.setOpaque(false);
        cardRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        addToRow(cardRow, labeled("NAME ON CARD", cardName), 0, 0.5);
        addToRow(cardRow, labeled("EXPIRY (MM/YY)", cardExpiry), 1, 0.3);
        addToRow(cardRow, labeled("CVV", cardCvv), 2, 0.2);
        cardPanel.add(cardRow);
        card.add(cardPanel);

        // e-wallet details: provider | mobile number
        walletPanel = new JPanel(new GridBagLayout());
        walletPanel.setOpaque(false);
        walletPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        Theme.styleCombo(walletProvider);
        addToRow(walletPanel, labeled("WALLET", walletProvider), 0, 0.35);
        addToRow(walletPanel, labeled("MOBILE NUMBER", walletNumber), 1, 0.65);
        card.add(walletPanel);

        showPaymentDetails();
        card.add(Box.createVerticalStrut(6));

        // guest info side by side
        JPanel guestRow = new JPanel(new GridLayout(1, 2, 12, 0));
        guestRow.setOpaque(false);
        guestRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        guestRow.add(labeled("GUEST FULL NAME", guestName));
        guestRow.add(labeled("CONTACT NUMBER", contactNumber));
        guestRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));
        card.add(guestRow);

        confirmBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        confirmBtn.addActionListener(e -> doConfirm());
        card.add(Box.createVerticalStrut(6));
        card.add(confirmBtn);

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.add(card, BorderLayout.NORTH); // keep the card its natural height
        center.add(wrap, BorderLayout.CENTER);

        // scrolls only if the window is too small for the card details
        JScrollPane scroll = new JScrollPane(center, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(Theme.CREAM);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    /** Label on top, field below it (used for every input on this page) */
    private JPanel labeled(String labelText, JComponent field) {
        JLabel label = new JLabel(labelText);
        label.setFont(Theme.LABEL.deriveFont(13f));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setFont(Theme.LABEL.deriveFont(14f));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        field.setPreferredSize(new Dimension(80, 32));

        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(label);
        p.add(Box.createVerticalStrut(3));
        p.add(field);
        p.add(Box.createVerticalStrut(8));
        return p;
    }

    private void addToRow(JPanel row, JComponent comp, int col, double weight) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = col;
        c.gridy = 0;
        c.weightx = weight;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, col == 0 ? 0 : 10, 0, 0);
        row.add(comp, c);
    }

    private JPanel buildReceiptSide() {
        JPanel side = new JPanel(new BorderLayout(0, 12));
        side.setBackground(Theme.SIDEBAR);
        side.setPreferredSize(new Dimension(300, 0));
        side.setBorder(BorderFactory.createEmptyBorder(25, 20, 25, 20));

        JPanel box = new JPanel(new GridBagLayout());
        box.setBackground(Theme.CARD);
        box.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.GOLD),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));

        JLabel title = new JLabel("RESERVATION RECEIPT", SwingConstants.CENTER);
        title.setFont(Theme.HEADING.deriveFont(18f));
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 0, 8, 0);
        box.add(title, c);

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
        box.add(header, c);

        c.gridy = 2;
        c.insets = new Insets(0, 0, 4, 0);
        box.add(new JSeparator(), c);

        addReceiptRow(box, 3, "GUEST:", guestVal);
        addReceiptRow(box, 4, "ROOM:", roomVal);
        addReceiptRow(box, 5, "DATES:", datesVal);
        addReceiptRow(box, 6, "ADD-ONS:", addonsVal);
        addReceiptRow(box, 7, "PAYMENT:", paymentVal);

        totalCaption.setFont(Theme.LABEL.deriveFont(14f));
        totalVal.setFont(Theme.HEADING.deriveFont(20f));
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 8;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(8, 0, 0, 8);
        box.add(totalCaption, c);
        c.gridx = 1;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(8, 0, 0, 0);
        box.add(totalVal, c);

        JPanel boxWrap = new JPanel(new BorderLayout());
        boxWrap.setOpaque(false);
        boxWrap.add(box, BorderLayout.NORTH);
        side.add(boxWrap, BorderLayout.CENTER);

        JButton view = Theme.goldButton("VIEW MY RESERVATIONS");
        view.addActionListener(e -> app.showPage("reservations"));
        JButton back = Theme.outlineButton("BACK TO ROOMS");
        back.addActionListener(e -> app.showPage("browse"));
        JPanel buttons = new JPanel();
        buttons.setOpaque(false);
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.Y_AXIS));
        view.setAlignmentX(Component.LEFT_ALIGNMENT);
        back.setAlignmentX(Component.LEFT_ALIGNMENT);
        buttons.add(view);
        buttons.add(Box.createVerticalStrut(8));
        buttons.add(back);
        side.add(buttons, BorderLayout.SOUTH);
        return side;
    }

    private void addReceiptRow(JPanel box, int y, String caption, JComponent value) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = y;
        c.gridx = 0;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.insets = new Insets(4, 0, 4, 8);
        JLabel cap = new JLabel(caption);
        cap.setFont(Theme.LABEL.deriveFont(14f));
        cap.setPreferredSize(new Dimension(70, 18));
        box.add(cap, c);

        c.gridx = 1;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(4, 0, 4, 0);
        value.setFont(Theme.LABEL.deriveFont(14f));
        box.add(value, c);
    }

    private void onType(JTextField field) {
        field.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { updateReceipt(); }
            @Override public void removeUpdate(DocumentEvent e) { updateReceipt(); }
            @Override public void changedUpdate(DocumentEvent e) { updateReceipt(); }
        });
    }

    // ------------------------------------------------------------ updating

    /** Called by AppPanel every time this page is shown: starts a fresh, unconfirmed checkout */
    public void refresh() {
        confirmed = null;
        confirmedPayment = null;
        previewId = ReservationFileHandler.nextBookingId();
        setFormEnabled(true);
        guestName.setText(user.getFullName());
        contactNumber.setText("");
        cardNumber.setText("");
        cardName.setText("");
        cardExpiry.setText("");
        cardCvv.setText("");
        walletNumber.setText("");
        cashBtn.setSelected(true);
        draft.setPaymentMethod(BookingDraft.PAY_CASH);
        showPaymentDetails();
        updateReceipt();
    }

    /** Shows the extra fields that belong to the selected payment method */
    private void showPaymentDetails() {
        cardPanel.setVisible(cardBtn.isSelected());
        walletPanel.setVisible(ewalletBtn.isSelected());
        revalidate();
        repaint();
    }

    private void setFormEnabled(boolean on) {
        for (JComponent c : new JComponent[]{cashBtn, cardBtn, ewalletBtn, cardNumber, cardName, cardExpiry,
                cardCvv, walletProvider, walletNumber, guestName, contactNumber, confirmBtn}) {
            c.setEnabled(on);
        }
    }

    /** Wraps lines of text so the receipt value column breaks nicely */
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

    private void updateReceipt() {
        Room room = draft.getRoom();
        if (room == null) {
            return;
        }
        boolean done = confirmed != null;
        String id = done ? confirmed.getBookingId() : previewId;

        statusBadge.setText(done ? "CONFIRMED" : "PENDING");
        statusBadge.setBackground(done ? Theme.GREEN : Theme.GOLD);
        idLabel.setText("<html>BOOKING ID:<br>" + Theme.html(id) + "</html>");

        String name = guestName.getText().trim();
        guestVal.setText(wrap(name.isEmpty() ? "\u2014" : name));
        roomVal.setText(wrap(room.getName()));
        int n = draft.nights();
        datesVal.setText(wrap(draft.getCheckIn() + " to", draft.getCheckOut().toString(),
                "(" + n + (n == 1 ? " Night)" : " Nights)")));
        ArrayList<String> addOns = draft.addOnLines();
        addonsVal.setText(addOns.isEmpty() ? wrap("None") : wrap(addOns.toArray(new String[0])));

        if (done && confirmedPayment != null) {
            paymentVal.setText(wrap(draft.getPaymentMethod(), confirmedPayment.getReference(),
                    "Status: " + confirmedPayment.getStatus()));
        } else {
            paymentVal.setText(wrap(draft.getPaymentMethod()));
        }

        boolean paid = done && confirmedPayment != null && Payment.PAID.equals(confirmedPayment.getStatus());
        totalCaption.setText(paid ? "TOTAL PAID:" : "TOTAL DUE:");
        totalVal.setText(Theme.money(draft.total()));
    }

    // ------------------------------------------------------------ actions

    private void doConfirm() {
        Room room = draft.getRoom();
        String name = guestName.getText().trim();
        String contact = contactNumber.getText().trim();

        if (FileHandler.isBlocked(user.getUsername())) {
            JOptionPane.showMessageDialog(this,
                    "This account has been blocked by the hotel administrator.\nYou can't make new bookings.",
                    "Account Blocked", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (name.isEmpty() || contact.isEmpty()) {
            showWarning("Please enter the guest's full name and contact number.");
            return;
        }
        if (name.contains("|") || contact.contains("|")) {
            showWarning("The \"|\" character is not allowed.");
            return;
        }
        if (!contact.matches("\\+?[0-9][0-9 \\-]{6,16}")) {
            showWarning("Please enter a valid contact number, e.g. +63 917 555 0194.");
            return;
        }
        if (!ReservationFileHandler.isRoomAvailable(room, draft.getCheckIn(), draft.getCheckOut())) {
            JOptionPane.showMessageDialog(this,
                    "Sorry, this room just sold out for those dates.\nPlease go back and choose other dates.",
                    "Room Unavailable", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String method = draft.getPaymentMethod();
        String chargeNote = cashBtn.isSelected()
                ? "You will pay at the front desk on arrival."
                : "Your " + (cardBtn.isSelected() ? "card" : walletProvider.getSelectedItem())
                + " will be charged " + Theme.money(draft.total()) + " now.";
        int choice = JOptionPane.showConfirmDialog(this,
                "Confirm your reservation for " + room.getName() + "?\nTotal: " + Theme.money(draft.total())
                        + "  (" + method + ")\n" + chargeNote,
                "Confirm Reservation", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        // take the payment first; if it fails, no booking is made
        PaymentService.Result result;
        if (cardBtn.isSelected()) {
            result = PaymentService.payCard(cardName.getText(), cardNumber.getText(),
                    cardExpiry.getText(), new String(cardCvv.getPassword()));
        } else if (ewalletBtn.isSelected()) {
            result = PaymentService.payEWallet((String) walletProvider.getSelectedItem(), walletNumber.getText());
        } else {
            result = PaymentService.payCash();
        }
        if (!result.approved) {
            JOptionPane.showMessageDialog(this, result.message, "Payment Failed", JOptionPane.ERROR_MESSAGE);
            return;
        }

        ArrayList<String> addOns = draft.addOnLines();
        String bookingId = ReservationFileHandler.nextBookingId();
        Reservation r = new Reservation(
                bookingId, user.getUsername(), name, contact,
                room.getId(), room.getName(), draft.getCheckIn(), draft.getCheckOut(),
                draft.getGuests(), addOns.isEmpty() ? "None" : String.join("; ", addOns),
                method, draft.total(), Reservation.CONFIRMED);

        if (!ReservationFileHandler.addReservation(r)) { // FILE WRITING
            JOptionPane.showMessageDialog(this, "Could not save your reservation.",
                    "File Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        Payment p = new Payment(PaymentFileHandler.nextPaymentId(), bookingId, user.getUsername(), method,
                result.reference, draft.total(), result.status, LocalDateTime.now().withNano(0));
        boolean paymentSaved = PaymentFileHandler.add(p); // FILE WRITING

        confirmed = r;
        confirmedPayment = p;
        setFormEnabled(false);
        updateReceipt();

        String done = "Reservation confirmed!\nBooking ID: " + bookingId;
        if (!paymentSaved) {
            done += "\n(Warning: the payment record could not be saved.)";
        } else if (Payment.PAID.equals(result.status)) {
            done += "\nPayment received: " + Theme.money(p.getAmount()) + " (" + result.reference + ")";
        } else {
            done += "\n" + result.message;
        }
        JOptionPane.showMessageDialog(this, done, "Booking Successful", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showWarning(String message) {
        JOptionPane.showMessageDialog(this, message, "Invalid Input", JOptionPane.WARNING_MESSAGE);
    }
}

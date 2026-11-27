package ui;

import model.BookingDraft;
import model.Room;
import service.ReservationFileHandler;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Insets;

/** Room details (left/center) + Stay Calculator (right). BOOK NOW goes to checkout. */
public class DetailsPanel extends JPanel {

    private final AppPanel app;
    private final BookingDraft draft;

    // room info
    private final JLabel imageLabel = new JLabel();
    private final JLabel nameLabel = new JLabel();
    private final JLabel badge = Theme.badge("Available", Theme.GREEN);
    private final JTextArea description = new JTextArea();
    private final JPanel amenityGrid = new JPanel(new GridLayout(0, 2, 12, 8));

    // stay calculator
    private final DateChooser dates;
    private final JCheckBox airport = new JCheckBox(
            "Airport Transfer (+" + Theme.price(BookingDraft.AIRPORT_TRANSFER_FEE) + ")");
    private final JCheckBox cleaning = new JCheckBox(
            "Daily Room Cleaning (+" + Theme.price(BookingDraft.CLEANING_PER_NIGHT) + "/night)");
    private final JLabel roomLeft = new JLabel();
    private final JLabel roomRight = new JLabel();
    private final JLabel airportRight = new JLabel();
    private final JLabel cleaningLeft = new JLabel();
    private final JLabel cleaningRight = new JLabel();
    private final JLabel serviceRight = new JLabel();
    private final JLabel totalRight = new JLabel();
    private JPanel airportRow;
    private JPanel cleaningRow;

    public DetailsPanel(AppPanel app, BookingDraft draft) {
        this.app = app;
        this.draft = draft;
        this.dates = new DateChooser(this::onDatesChanged);

        setLayout(new BorderLayout());
        setBackground(Theme.CREAM);
        add(buildInfo(), BorderLayout.CENTER);
        add(buildCalculator(), BorderLayout.EAST);
    }

    // ------------------------------------------------------------ building

    private JPanel buildInfo() {
        JPanel center = new JPanel(new BorderLayout(0, 12));
        center.setOpaque(false);
        center.setBorder(BorderFactory.createEmptyBorder(25, 25, 15, 25));
        center.add(Theme.title("ROOM DETAILS"), BorderLayout.NORTH);

        // top row: photo | name + badge + description
        nameLabel.setFont(Theme.BUTTON.deriveFont(18f));
        JPanel badgeHolder = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 4));
        badgeHolder.setOpaque(false);
        badgeHolder.add(badge);
        JPanel header = new JPanel(new BorderLayout(8, 0));
        header.setOpaque(false);
        header.add(nameLabel, BorderLayout.CENTER);
        header.add(badgeHolder, BorderLayout.EAST);

        description.setEditable(false);
        description.setLineWrap(true);
        description.setWrapStyleWord(true);
        description.setFont(Theme.LABEL.deriveFont(14f));
        description.setBackground(Theme.CARD);
        description.setMargin(new Insets(6, 8, 6, 8));
        JScrollPane descScroll = new JScrollPane(description);
        descScroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER));

        JPanel info = new JPanel(new BorderLayout(0, 8));
        info.setOpaque(false);
        info.add(header, BorderLayout.NORTH);
        info.add(descScroll, BorderLayout.CENTER);

        imageLabel.setVerticalAlignment(SwingConstants.TOP);
        JPanel topRow = new JPanel(new BorderLayout(15, 0));
        topRow.setOpaque(false);
        topRow.add(imageLabel, BorderLayout.WEST);
        topRow.add(info, BorderLayout.CENTER);

        // amenities box
        JLabel amenTitle = new JLabel("AMENITIES");
        amenTitle.setFont(Theme.BUTTON.deriveFont(15f));
        amenityGrid.setOpaque(false);
        JPanel amenities = new JPanel(new BorderLayout(0, 8));
        amenities.setBackground(Theme.CARD);
        amenities.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        amenities.add(amenTitle, BorderLayout.NORTH);
        amenities.add(amenityGrid, BorderLayout.CENTER);

        JPanel body = new JPanel(new BorderLayout(0, 12));
        body.setOpaque(false);
        body.add(topRow, BorderLayout.CENTER);
        body.add(amenities, BorderLayout.SOUTH);
        center.add(body, BorderLayout.CENTER);
        return center;
    }

    private JPanel buildCalculator() {
        JPanel side = new JPanel();
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setBackground(Theme.SIDEBAR);
        side.setPreferredSize(new Dimension(290, 0));
        side.setBorder(BorderFactory.createEmptyBorder(30, 20, 25, 20));

        side.add(Theme.sideTitle("STAY CALCULATOR"));
        side.add(Box.createVerticalStrut(14));

        addField(side, "Check-In", dates.getCheckInBox());
        addField(side, "Check-Out", dates.getCheckOutBox());

        for (JCheckBox box : new JCheckBox[]{airport, cleaning}) {
            box.setOpaque(false);
            box.setFont(Theme.LABEL.deriveFont(14f));
            box.setAlignmentX(Component.LEFT_ALIGNMENT);
            side.add(box);
        }
        airport.addActionListener(e -> {
            draft.setAirportTransfer(airport.isSelected());
            updateBreakdown();
        });
        cleaning.addActionListener(e -> {
            draft.setDailyCleaning(cleaning.isSelected());
            updateBreakdown();
        });
        side.add(Box.createVerticalStrut(8));
        side.add(separator());
        side.add(Box.createVerticalStrut(6));

        // price breakdown
        JLabel airportLeft = new JLabel("Airport Transfer");
        cleaningLeft.setText("Daily Cleaning");
        airportRow = Theme.row(airportLeft, airportRight);
        cleaningRow = Theme.row(cleaningLeft, cleaningRight);
        JLabel serviceLeft = new JLabel("Service Fee");
        for (JLabel l : new JLabel[]{roomLeft, roomRight, airportLeft, airportRight,
                cleaningLeft, cleaningRight, serviceLeft, serviceRight}) {
            l.setFont(Theme.LABEL.deriveFont(15f));
        }
        side.add(Theme.row(roomLeft, roomRight));
        side.add(airportRow);
        side.add(cleaningRow);
        side.add(Theme.row(serviceLeft, serviceRight));
        side.add(Box.createVerticalStrut(6));
        side.add(thickSeparator());
        side.add(Box.createVerticalStrut(6));

        JLabel totalLeft = new JLabel("TOTAL COST:");
        totalLeft.setFont(Theme.HEADING);
        totalRight.setFont(Theme.HEADING);
        side.add(Theme.row(totalLeft, totalRight));

        side.add(Box.createVerticalGlue());

        JButton book = Theme.goldButton("BOOK NOW");
        book.setAlignmentX(Component.LEFT_ALIGNMENT);
        book.addActionListener(e -> bookNow());
        JButton back = Theme.outlineButton("BACK TO ROOMS");
        back.setAlignmentX(Component.LEFT_ALIGNMENT);
        back.addActionListener(e -> app.showPage("browse"));
        side.add(book);
        side.add(Box.createVerticalStrut(8));
        side.add(back);
        return side;
    }

    private JSeparator separator() {
        JSeparator s = new JSeparator();
        s.setAlignmentX(Component.LEFT_ALIGNMENT);
        s.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));
        return s;
    }

    private JPanel thickSeparator() {
        JPanel line = new JPanel();
        line.setBackground(Color.BLACK);
        line.setAlignmentX(Component.LEFT_ALIGNMENT);
        line.setPreferredSize(new Dimension(100, 2));
        line.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));
        return line;
    }

    private void addField(JPanel side, String labelText, JComboBox<String> box) {
        JLabel label = new JLabel(labelText);
        label.setFont(Theme.LABEL.deriveFont(14f));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        side.add(label);
        side.add(Box.createVerticalStrut(4));
        side.add(box);
        side.add(Box.createVerticalStrut(10));
    }

    // ------------------------------------------------------------ updating

    /** Called by AppPanel every time this page is shown */
    public void refresh() {
        Room room = draft.getRoom();
        if (room == null) {
            return;
        }
        imageLabel.setIcon(Theme.loadImage(room.getImagePath(), 200, 240));
        nameLabel.setText("<html><body style='width:135px'>" + Theme.html(room.getName()) + " \u2014<br>"
                + Theme.price(room.getPricePerNight()) + " / night</body></html>");
        description.setText(room.getDescription());
        description.setCaretPosition(0);

        amenityGrid.removeAll();
        for (String a : room.getAmenities()) {
            JLabel l = new JLabel("\u2713  " + a.trim());
            l.setFont(Theme.LABEL.deriveFont(14f));
            amenityGrid.add(l);
        }
        amenityGrid.revalidate();

        dates.setDates(draft.getCheckIn(), draft.getCheckOut());
        draft.setDates(dates.getCheckIn(), dates.getCheckOut());
        airport.setSelected(draft.isAirportTransfer());
        cleaning.setSelected(draft.isDailyCleaning());
        updateBreakdown();
    }

    private void onDatesChanged() {
        draft.setDates(dates.getCheckIn(), dates.getCheckOut());
        updateBreakdown();
    }

    private int roomsLeft() {
        return ReservationFileHandler.roomsLeft(draft.getRoom(), draft.getCheckIn(), draft.getCheckOut());
    }

    private boolean isAvailable() {
        return roomsLeft() > 0;
    }

    private void updateBreakdown() {
        Room room = draft.getRoom();
        if (room == null) {
            return;
        }
        int n = draft.nights();
        String nightWord = n == 1 ? " Night" : " Nights";

        int left = roomsLeft();
        badge.setText(Theme.availabilityText(left));
        badge.setBackground(Theme.availabilityColor(left));

        roomLeft.setText(Theme.price(room.getPricePerNight()) + " x " + n + nightWord);
        roomRight.setText(Theme.money(draft.roomSubtotal()));
        airportRow.setVisible(draft.isAirportTransfer());
        airportRight.setText(Theme.money(draft.airportFee()));
        cleaningRow.setVisible(draft.isDailyCleaning());
        cleaningLeft.setText("Daily Cleaning (" + n + "x)");
        cleaningRight.setText(Theme.money(draft.cleaningFee()));
        serviceRight.setText(Theme.money(draft.serviceFee()));
        totalRight.setText(Theme.money(draft.total()));
        revalidate();
        repaint();
    }

    private void bookNow() {
        if (!isAvailable()) {
            JOptionPane.showMessageDialog(this,
                    "This room is sold out for those dates.\nPlease pick different dates.",
                    "Room Unavailable", JOptionPane.ERROR_MESSAGE);
            return;
        }
        app.showPage("checkout");
    }
}

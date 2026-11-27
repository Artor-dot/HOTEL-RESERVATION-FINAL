package ui;

import model.BookingDraft;
import model.Room;
import service.ReservationFileHandler;
import service.RoomFileHandler;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * "Browse Rooms" page: room catalog on the left/center, reservation details on the right.
 * Click a room card to select it, pick dates + guests, then PROCEED TO BOOKING.
 */
public class BrowsePanel extends JPanel {

    private static final int CARD_HEIGHT = 108;

    private final AppPanel app;
    private final BookingDraft draft;
    private final ArrayList<Room> rooms = RoomFileHandler.loadRooms(); // FILE READING: data/rooms.txt
    private final HashMap<String, ImageIcon> iconCache = new HashMap<>();

    private final JPanel listPanel = new JPanel();
    private final JScrollPane scroll = new JScrollPane(listPanel,
            ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    private final DateChooser dates;
    private final JComboBox<String> guestsBox = new JComboBox<>();
    private final JLabel selectedLabel = new JLabel(" ");
    private final JLabel totalLabel = new JLabel(" ", SwingConstants.CENTER);
    private boolean updating = false;

    public BrowsePanel(AppPanel app, BookingDraft draft) {
        this.app = app;
        this.draft = draft;
        this.dates = new DateChooser(this::onDatesChanged);

        setLayout(new BorderLayout());
        setBackground(Theme.CREAM);
        add(buildCatalog(), BorderLayout.CENTER);
        add(buildSide(), BorderLayout.EAST);
    }

    // ------------------------------------------------------------ building

    private JPanel buildCatalog() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(25, 25, 15, 25));

        JLabel title = Theme.title("ROOM CATALOG");
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        p.add(title, BorderLayout.NORTH);

        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(Theme.CREAM);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(Theme.CREAM);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        p.add(scroll, BorderLayout.CENTER);
        return p;
    }

    private JPanel buildSide() {
        JPanel side = new JPanel();
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setBackground(Theme.SIDEBAR);
        side.setPreferredSize(new Dimension(290, 0));
        side.setBorder(BorderFactory.createEmptyBorder(30, 20, 25, 20));

        side.add(Theme.sideTitle("RESERVATION DETAILS"));
        side.add(Box.createVerticalStrut(18));

        addField(side, "CHECK-IN DATE:", dates.getCheckInBox());
        addField(side, "CHECK-OUT DATE:", dates.getCheckOutBox());

        for (int i = 1; i <= 6; i++) {
            guestsBox.addItem(i == 1 ? "1 Adult" : i + " Adults");
        }
        Theme.styleCombo(guestsBox);
        guestsBox.addActionListener(e -> {
            if (!updating) {
                draft.setGuests(guestsBox.getSelectedIndex() + 1);
            }
        });
        addField(side, "GUESTS:", guestsBox);

        JSeparator sep = new JSeparator();
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));
        side.add(sep);
        side.add(Box.createVerticalStrut(14));

        selectedLabel.setFont(Theme.LABEL);
        selectedLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        selectedLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        side.add(selectedLabel);
        side.add(Box.createVerticalStrut(6));

        totalLabel.setFont(Theme.HEADING);
        totalLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        totalLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        side.add(totalLabel);

        side.add(Box.createVerticalGlue());

        JButton proceed = Theme.goldButton("PROCEED TO BOOKING");
        proceed.setAlignmentX(Component.LEFT_ALIGNMENT);
        proceed.addActionListener(e -> proceed());
        side.add(proceed);
        return side;
    }

    private void addField(JPanel side, String labelText, JComboBox<String> box) {
        JLabel label = new JLabel(labelText);
        label.setFont(Theme.LABEL.deriveFont(14f));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        side.add(label);
        side.add(Box.createVerticalStrut(4));
        side.add(box);
        side.add(Box.createVerticalStrut(12));
    }

    // ------------------------------------------------------------ catalog

    /** Called by AppPanel every time this page is shown */
    public void refresh() {
        updating = true;
        dates.setDates(draft.getCheckIn(), draft.getCheckOut());
        draft.setDates(dates.getCheckIn(), dates.getCheckOut());
        guestsBox.setSelectedIndex(draft.getGuests() - 1);
        updating = false;
        refreshCards();
        updateSummary();
    }

    private void onDatesChanged() {
        draft.setDates(dates.getCheckIn(), dates.getCheckOut());
        refreshCards();
        updateSummary();
    }

    private void refreshCards() {
        int scrollPos = scroll.getVerticalScrollBar().getValue();
        listPanel.removeAll();

        if (rooms.isEmpty()) {
            JLabel empty = new JLabel("No rooms found. Check that data/rooms.txt exists.");
            empty.setAlignmentX(Component.LEFT_ALIGNMENT);
            listPanel.add(empty);
        }

        for (Room room : rooms) {
            int left = ReservationFileHandler.roomsLeft(room, draft.getCheckIn(), draft.getCheckOut());
            boolean available = left > 0;
            if (!available && draft.getRoom() != null && draft.getRoom().getId().equals(room.getId())) {
                draft.setRoom(null); // the dates changed and the selected room is taken
            }
            boolean selected = draft.getRoom() != null && draft.getRoom().getId().equals(room.getId());
            listPanel.add(buildCard(room, left, selected));
            listPanel.add(Box.createVerticalStrut(10));
        }

        listPanel.revalidate();
        listPanel.repaint();
        SwingUtilities.invokeLater(() -> scroll.getVerticalScrollBar().setValue(scrollPos));
    }

    private JPanel buildCard(Room room, int left, boolean selected) {
        boolean available = left > 0;
        JPanel card = new JPanel(new BorderLayout(12, 0));
        card.setBackground(Theme.CARD);
        int pad = selected ? 7 : 8;
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(selected ? Theme.GREEN : Theme.BORDER, selected ? 2 : 1),
                BorderFactory.createEmptyBorder(pad, pad, pad, pad)));
        // small preferred width so the card stretches to the list width
        card.setPreferredSize(new Dimension(100, CARD_HEIGHT));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, CARD_HEIGHT));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        ImageIcon icon = iconCache.computeIfAbsent(room.getId(),
                id -> Theme.loadImage(room.getImagePath(), 105, 90));
        card.add(new JLabel(icon), BorderLayout.WEST);

        JLabel badge = Theme.badge(Theme.availabilityText(left), Theme.availabilityColor(left));
        JPanel badgeRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        badgeRow.setOpaque(false);
        badgeRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        badgeRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        badgeRow.add(badge);

        JLabel name = new JLabel(room.getName() + " \u2014 " + Theme.price(room.getPricePerNight()) + " / night");
        name.setFont(Theme.BUTTON.deriveFont(15f));
        name.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel summary = new JLabel(room.getSummary());
        summary.setFont(Theme.LABEL.deriveFont(13f));
        summary.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.add(badgeRow);
        text.add(Box.createVerticalStrut(4));
        text.add(name);
        text.add(Box.createVerticalStrut(3));
        text.add(summary);
        text.add(Box.createVerticalGlue());
        card.add(text, BorderLayout.CENTER);

        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selectRoom(room, available);
            }
        });
        return card;
    }

    private void selectRoom(Room room, boolean available) {
        if (!available) {
            JOptionPane.showMessageDialog(this,
                    room.getName() + " is sold out for the selected dates.\nPlease choose other dates or another room.",
                    "Room Unavailable", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        draft.setRoom(room);
        refreshCards();
        updateSummary();
    }

    private void updateSummary() {
        Room room = draft.getRoom();
        if (room == null) {
            selectedLabel.setText("No room selected");
            totalLabel.setText("\u2014");
            return;
        }
        int n = draft.nights();
        selectedLabel.setText("Selected: " + room.getName());
        totalLabel.setText(n + (n == 1 ? " Night: " : " Nights: ") + Theme.money(draft.roomSubtotal()));
    }

    // ------------------------------------------------------------ actions

    private void proceed() {
        Room room = draft.getRoom();
        if (room == null) {
            JOptionPane.showMessageDialog(this, "Please select a room from the catalog first.",
                    "No Room Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (draft.getGuests() > room.getMaxGuests()) {
            JOptionPane.showMessageDialog(this,
                    room.getName() + " fits up to " + room.getMaxGuests() + " guests.\n"
                            + "Please lower the number of guests or choose a bigger room.",
                    "Too Many Guests", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!ReservationFileHandler.isRoomAvailable(room, draft.getCheckIn(), draft.getCheckOut())) {
            JOptionPane.showMessageDialog(this, "That room is no longer available for those dates.",
                    "Room Unavailable", JOptionPane.ERROR_MESSAGE);
            refreshCards();
            return;
        }
        app.showPage("details");
    }
}

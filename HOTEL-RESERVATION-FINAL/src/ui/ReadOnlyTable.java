package ui;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseEvent;

/**
 * The table used on every list screen (My Reservations, Payments and the admin pages)
 * so they all look the same: green header, cream rows, colored status text.
 */
public class ReadOnlyTable extends JTable {

    public ReadOnlyTable(String[] columns, int[] widths) {
        super(new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });

        setRowHeight(32);
        setFont(Theme.LABEL.deriveFont(13f));
        setGridColor(new Color(0xE4DCC8));
        setShowVerticalLines(false);
        setBackground(Color.WHITE);
        setSelectionBackground(new Color(0xE6DDC6));
        setSelectionForeground(Color.BLACK);
        setFillsViewportHeight(true);

        // custom header so the green shows on every OS (macOS ignores header colors otherwise)
        getTableHeader().setReorderingAllowed(false);
        getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel,
                                                           boolean focus, int row, int col) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, false, false, row, col);
                l.setOpaque(true);
                l.setBackground(Theme.GREEN);
                l.setForeground(Color.WHITE);
                l.setFont(Theme.BUTTON.deriveFont(13f));
                l.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                l.setHorizontalAlignment(SwingConstants.LEFT);
                return l;
            }
        });
        getTableHeader().setPreferredSize(new java.awt.Dimension(100, 34));

        for (int i = 0; i < widths.length; i++) {
            getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
    }

    public DefaultTableModel model() {
        return (DefaultTableModel) getModel();
    }

    public void clear() {
        model().setRowCount(0);
    }

    public void alignRight(int col) {
        DefaultTableCellRenderer r = new DefaultTableCellRenderer();
        r.setHorizontalAlignment(SwingConstants.RIGHT);
        getColumnModel().getColumn(col).setCellRenderer(r);
    }

    public void alignCenter(int col) {
        DefaultTableCellRenderer r = new DefaultTableCellRenderer();
        r.setHorizontalAlignment(SwingConstants.CENTER);
        getColumnModel().getColumn(col).setCellRenderer(r);
    }

    /** Colors the text of a status column: green = good, gold = waiting, red = cancelled/blocked */
    public void statusColumn(int col) {
        getColumnModel().getColumn(col).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel,
                                                           boolean focus, int row, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, sel, focus, row, c);
                l.setHorizontalAlignment(SwingConstants.CENTER);
                l.setFont(Theme.BUTTON.deriveFont(12f));
                String s = String.valueOf(v);
                switch (s) {
                    case "PAID": case "CONFIRMED": case "ACTIVE":
                        l.setForeground(Theme.GREEN);
                        break;
                    case "PENDING":
                        l.setForeground(Theme.GOLD);
                        break;
                    case "CANCELLED": case "REFUNDED": case "VOID": case "BLOCKED":
                        l.setForeground(Theme.RED);
                        break;
                    default:
                        l.setForeground(Color.BLACK);
                }
                return l;
            }
        });
    }

    /** The table inside a bordered scroll pane, ready to add to a layout */
    public JScrollPane inScrollPane() {
        JScrollPane sp = new JScrollPane(this);
        sp.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        sp.getViewport().setBackground(Color.WHITE);
        return sp;
    }

    // show the full text of a cell as a tooltip (long add-ons, emails...)
    @Override
    public String getToolTipText(MouseEvent e) {
        int row = rowAtPoint(e.getPoint());
        int col = columnAtPoint(e.getPoint());
        if (row < 0 || col < 0) {
            return null;
        }
        Object v = getValueAt(row, col);
        return v == null ? null : v.toString();
    }
}

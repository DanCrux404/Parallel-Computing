package windows;

import java.awt.Color;
import java.awt.Component;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class MonitorWindow extends javax.swing.JFrame {

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(MonitorWindow.class.getName());

    /**
     * Creates new form MonitorWindow
     */
    // Column indexes — constants so we never use magic numbers
    public static final int COL_NAME = 0;
    public static final int COL_TYPE = 1;
    public static final int COL_COLOR = 2;
    public static final int COL_STATE = 3;
    public static final int COL_COUNT = 4;

    private final DefaultTableModel tableModel = new DefaultTableModel(
    new String[]{"Process", "Type", "Color", "State", "Operations"}, 0
) {
    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }

    @Override
    public Class<?> getColumnClass(int column) {
        if (column == COL_COLOR) return Color.class;
        return String.class;
    }
};

    public MonitorWindow() {
        initComponents();
        setupTable();
    }

    // ── Setup table model and renderers — called after initComponents ──
    private void setupTable() {
        // Replace NetBeans default model with our custom one

        Monitoring.setModel(tableModel);
        Monitoring.setRowHeight(28); // Taller rows so colors are visible

        // ── Color column renderer — paints cell with producer's color ──
        Monitoring.getColumnModel().getColumn(COL_COLOR).setCellRenderer(
                new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    javax.swing.JTable t, Object value, boolean selected,
                    boolean focused, int row, int col) {
                super.getTableCellRendererComponent(t, "", selected, focused, row, col);
                if (value instanceof Color) {
                    setBackground((Color) value); // Paint with producer color
                    setText("");
                } else {
                    setBackground(Color.LIGHT_GRAY); // Consumer has no color
                }
                return this;
            }
        }
        );

        // ── State column renderer — color by state ──
        Monitoring.getColumnModel().getColumn(COL_STATE).setCellRenderer(
                new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    javax.swing.JTable t, Object value, boolean selected,
                    boolean focused, int row, int col) {
                super.getTableCellRendererComponent(t, value, selected, focused, row, col);
                if ("RUNNING".equals(value)) {
                    setBackground(new Color(144, 238, 144)); // Light green
                    setForeground(Color.BLACK);
                } else if ("SLEEPING".equals(value)) {
                    setBackground(new Color(255, 255, 153)); // Light yellow
                    setForeground(Color.BLACK);
                } else if ("WAITING".equals(value)) {
                    setBackground(new Color(255, 102, 102)); // Light red
                    setForeground(Color.WHITE);
                } else {
                    setBackground(Color.WHITE);
                    setForeground(Color.BLACK);
                }
                return this;
            }
        }
        );

        // Column widths
        Monitoring.getColumnModel().getColumn(COL_NAME).setPreferredWidth(80);
        Monitoring.getColumnModel().getColumn(COL_TYPE).setPreferredWidth(90);
        Monitoring.getColumnModel().getColumn(COL_COLOR).setPreferredWidth(60);
        Monitoring.getColumnModel().getColumn(COL_STATE).setPreferredWidth(100);
        Monitoring.getColumnModel().getColumn(COL_COUNT).setPreferredWidth(90);
    }

    // ── Called by SimulationManager when a new process is added ──
    public void addProcess(String name, String type, Color color) {
        javax.swing.SwingUtilities.invokeLater(()
                -> tableModel.addRow(new Object[]{
            name, // "P-1" or "C-1"
            type, // "Producer" or "Consumer"
            color, // Producer color, null for consumer
            "RUNNING", // Always starts running
            0 // Operations counter starts at 0
        })
        );
    }

    // ── Called by SimulationManager when a process is removed ──
    public void removeProcess(String name) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                if (name.equals(tableModel.getValueAt(i, COL_NAME))) {
                    tableModel.removeRow(i);
                    break;
                }
            }
        });
    }

    // ── Called when state changes — RUNNING, SLEEPING, WAITING ──
    public void updateState(String name, String state) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                if (name.equals(tableModel.getValueAt(i, COL_NAME))) {
                    tableModel.setValueAt(state, i, COL_STATE);
                    break;
                }
            }
        });
    }

    // ── Called when production or consumption happens — increments counter ──
    public void incrementCount(String name) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                if (name.equals(tableModel.getValueAt(i, COL_NAME))) {
                    int current = (int) tableModel.getValueAt(i, COL_COUNT);
                    tableModel.setValueAt(current + 1, i, COL_COUNT);
                    break;
                }
            }
        });
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        Monitoring = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        Monitoring.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(Monitoring);

        getContentPane().add(jScrollPane1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new MonitorWindow().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTable Monitoring;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables
}

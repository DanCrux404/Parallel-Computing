package readerswriters.windows;

import controller.SimulationManager;
import java.awt.Color;
import java.awt.Component;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import models.Flight;

public class MainWindow extends javax.swing.JFrame {

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(MainWindow.class.getName());
    private SimulationManager simulationManager;

    // Column indexes for flights table
    private static final int COL_ID = 0;
    private static final int COL_ORIGIN = 1;
    private static final int COL_DEST = 2;
    private static final int COL_PRICE = 3;
    private static final int COL_STATUS = 4;

    // Flights table model — initialized as attribute, never null
    private final DefaultTableModel flightsModel = new DefaultTableModel(
            new String[]{"ID", "Origin", "Destination", "Price", "Status"}, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false; // Read only — only writers can modify via Database
        }
    };

    /**
     * Creates new form MainWindow
     */
    public MainWindow() {
        initComponents();
        setupTable();
        // Create simulation manager — it creates Database and ColorManager
        simulationManager = new SimulationManager(this);

        // Connect buttons to simulation manager
        btnAddReader.addActionListener(e -> simulationManager.addReader());
        btnDeleteReader.addActionListener(e -> simulationManager.removeReader());
        btnAddWriter.addActionListener(e -> simulationManager.addWriter());
        btnDeleteWriter.addActionListener(e -> simulationManager.removeWriter());
    }

    // == Setup flights table — called after initComponents ==========
    private void setupTable() {
        flightsTable.setModel(flightsModel);
        flightsTable.setRowHeight(25);

        // == Status column renderer — color by status =====
        flightsTable.getColumnModel().getColumn(COL_STATUS).setCellRenderer(
                new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    javax.swing.JTable t, Object value, boolean selected,
                    boolean focused, int row, int col) {
                super.getTableCellRendererComponent(t, value, selected, focused, row, col);
                if ("Active".equals(value)) {
                    setBackground(new Color(144, 238, 144)); // Light green
                    setForeground(Color.BLACK);
                } else if ("Delayed".equals(value)) {
                    setBackground(new Color(255, 255, 153)); // Light yellow
                    setForeground(Color.BLACK);
                } else if ("Cancelled".equals(value)) {
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
        flightsTable.getColumnModel().getColumn(COL_ID).setPreferredWidth(40);
        flightsTable.getColumnModel().getColumn(COL_ORIGIN).setPreferredWidth(80);
        flightsTable.getColumnModel().getColumn(COL_DEST).setPreferredWidth(100);
        flightsTable.getColumnModel().getColumn(COL_PRICE).setPreferredWidth(80);
        flightsTable.getColumnModel().getColumn(COL_STATUS).setPreferredWidth(100);
    }

    // == PUBLIC METHODS for SimulationManager to call ================
    // Add a new flight row to the table
    public void addFlightRow(Flight flight) {
        flightsModel.addRow(new Object[]{
            flight.getId(),
            flight.getOrigin(),
            flight.getDest(),
            String.format("$%.0f", flight.getPrice()),
            flight.getStatus()
        });
    }

    // Update an existing flight row
    public void updateFlightRow(int index, Flight flight) {
        if (index >= 0 && index < flightsModel.getRowCount()) {
            flightsModel.setValueAt(flight.getOrigin(), index, COL_ORIGIN);
            flightsModel.setValueAt(flight.getDest(), index, COL_DEST);
            flightsModel.setValueAt(String.format("$%.0f", flight.getPrice()), index, COL_PRICE);
            flightsModel.setValueAt(flight.getStatus(), index, COL_STATUS);
        }
    }

    // Highlight a row yellow while a reader is reading it
    // Uses a separate renderer per row — stored in a boolean array
    public void highlightRow(int index, boolean highlighting) {
        if (index >= 0 && index < flightsModel.getRowCount()) {
            // Store highlight state in a hidden column or use row color trick
            // Simple approach — change the row via repaint trigger
            flightsTable.repaint();
            highlightedRow = highlighting ? index : -1;
        }
    }

    // Currently highlighted row — -1 means none
    private int highlightedRow = -1;

    // Update north counters
    public void setReaderCount(int count) {
        lblReadersNumb.setText(String.valueOf(count));
    }

    public void setWriterCount(int count) {
        lblWritersNumb.setText(String.valueOf(count));
    }

    public void setRecordCount(int count) {
        lblRecordsNumb.setText(String.valueOf(count));
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        lblReaders = new javax.swing.JLabel();
        lblReadersNumb = new javax.swing.JLabel();
        lblWriters = new javax.swing.JLabel();
        lblWritersNumb = new javax.swing.JLabel();
        lblRecords = new javax.swing.JLabel();
        lblRecordsNumb = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        flightsTable = new javax.swing.JTable();
        jPanel2 = new javax.swing.JPanel();
        btnAddReader = new javax.swing.JButton();
        btnDeleteReader = new javax.swing.JButton();
        btnAddWriter = new javax.swing.JButton();
        btnDeleteWriter = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setLayout(new java.awt.GridLayout(1, 6));

        lblReaders.setText("Readers:");
        jPanel1.add(lblReaders);

        lblReadersNumb.setText("0");
        jPanel1.add(lblReadersNumb);

        lblWriters.setText("Writers:");
        jPanel1.add(lblWriters);

        lblWritersNumb.setText("0");
        jPanel1.add(lblWritersNumb);

        lblRecords.setText("Records:");
        jPanel1.add(lblRecords);

        lblRecordsNumb.setText("0");
        jPanel1.add(lblRecordsNumb);

        getContentPane().add(jPanel1, java.awt.BorderLayout.PAGE_START);

        flightsTable.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane1.setViewportView(flightsTable);

        getContentPane().add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jPanel2.setLayout(new java.awt.GridLayout(2, 2));

        btnAddReader.setText("Add Reader");
        jPanel2.add(btnAddReader);

        btnDeleteReader.setText("Delete Reader");
        jPanel2.add(btnDeleteReader);

        btnAddWriter.setText("Add Writer");
        jPanel2.add(btnAddWriter);

        btnDeleteWriter.setText("Delete Writer");
        jPanel2.add(btnDeleteWriter);

        getContentPane().add(jPanel2, java.awt.BorderLayout.PAGE_END);

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
        java.awt.EventQueue.invokeLater(() -> new MainWindow().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddReader;
    private javax.swing.JButton btnAddWriter;
    private javax.swing.JButton btnDeleteReader;
    private javax.swing.JButton btnDeleteWriter;
    private javax.swing.JTable flightsTable;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblReaders;
    private javax.swing.JLabel lblReadersNumb;
    private javax.swing.JLabel lblRecords;
    private javax.swing.JLabel lblRecordsNumb;
    private javax.swing.JLabel lblWriters;
    private javax.swing.JLabel lblWritersNumb;
    // End of variables declaration//GEN-END:variables
}

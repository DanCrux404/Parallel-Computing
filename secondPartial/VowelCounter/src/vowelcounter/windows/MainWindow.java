package vowelcounter.windows;

import controller.CounterManager;
import java.awt.Color;
import java.awt.Component;
import java.io.File;
import java.util.List;
import javax.swing.JFileChooser;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import model.AppStatus;
import model.VowelResult;

/**
 *
 * @author dante
 */
public class MainWindow extends javax.swing.JFrame {

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(MainWindow.class.getName());

    private CounterManager counterManager;
    private StatsWindow statsWindow;

    // Column indexes — constants so we never use magic numbers, again :)
    private static final int COL_NAME = 0;
    private static final int COL_STATUS = 1;
    private static final int COL_VOWELS = 2;
    private static final int COL_TIME = 3;

    // Table model — initialized as attribute, never null (learned from previous projects!)
    private final DefaultTableModel filesModel = new DefaultTableModel(
            new String[]{"File", "Status", "Vowels", "Time(ms)"}, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false; // Read only — user can't edit
        }
    };

    /**
     * Creates new form MainWindow
     */
    public MainWindow() {
        initComponents();

        setupTable();

        // Open stats window alongside main window
        statsWindow = new StatsWindow();
        statsWindow.setVisible(true);

        // Create counter manager — it creates Sequential and Concurrent counters
        counterManager = new CounterManager(this, statsWindow);

        // Connect buttons — same pattern as previous projects
        btnUpload.addActionListener(e -> openFileChooser());
        btnSequential.addActionListener(e -> counterManager.runSequential());
        btnConcurrent.addActionListener(e
                -> counterManager.runConcurrent((int) spnThreads.getValue())
        );

        spnThreads.setModel(new javax.swing.SpinnerNumberModel(
                1, // Initial value
                1, // min
                null, // max, no limit(Until it crashes MUEJEJEJEJEEJ)
                1 // increment
        ));
    }

    // == File chooser — opens dialog to select multiple files ========
    private void openFileChooser() {
        JFileChooser chooser = new JFileChooser();
        // Allow selecting multiple files at once
        chooser.setMultiSelectionEnabled(true);
        // Only show .txt files
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Text files", "txt"
        ));

        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            List<File> files = java.util.Arrays.asList(chooser.getSelectedFiles());
            counterManager.loadFiles(files);
        }
    }

    // == Setup table model and renderers =====================
    private void setupTable() {
        FilesTable.setModel(filesModel);
        FilesTable.setRowHeight(25);

        // == Status column renderer — color by status ==
        FilesTable.getColumnModel().getColumn(COL_STATUS).setCellRenderer(
                new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    javax.swing.JTable t, Object value, boolean selected,
                    boolean focused, int row, int col) {
                super.getTableCellRendererComponent(t, value, selected, focused, row, col);
                if (VowelResult.Status.DONE.name().equals(value)) {
                    setBackground(new Color(144, 238, 144)); // Light green
                    setForeground(Color.BLACK);
                } else if (VowelResult.Status.PROCESSING.name().equals(value)) {
                    setBackground(new Color(255, 255, 153)); // Light yellow
                    setForeground(Color.BLACK);
                } else if (VowelResult.Status.ERROR.name().equals(value)) {
                    setBackground(new Color(255, 102, 102)); // Light red
                    setForeground(Color.WHITE);
                } else {
                    setBackground(Color.WHITE); // PENDING — white
                    setForeground(Color.BLACK);
                }
                return this;
            }
        }
        );

        // Column widths
        FilesTable.getColumnModel().getColumn(COL_NAME).setPreferredWidth(200);
        FilesTable.getColumnModel().getColumn(COL_STATUS).setPreferredWidth(100);
        FilesTable.getColumnModel().getColumn(COL_VOWELS).setPreferredWidth(80);
        FilesTable.getColumnModel().getColumn(COL_TIME).setPreferredWidth(80);
    }

    // == PUBLIC METHODS for CounterManager to call =================
    // Add a new file row — called when files are loaded
    public void addFileRow(String fileName, VowelResult.Status status) {
        filesModel.addRow(new Object[]{
            fileName,
            status.name(), // "PENDING", "PROCESSING", etc.
            "-", // Vowels — not counted yet
            "-" // Time — not measured yet
        });
    }

    // Update status of a file row — PENDING -> PROCESSING -> DONE
    public void updateFileStatus(int index, VowelResult.Status status) {
        if (index >= 0 && index < filesModel.getRowCount()) {
            filesModel.setValueAt(status.name(), index, COL_STATUS);
        }
    }

    // Update file row with results — called when file is done
    public void updateFileResult(int index, VowelResult result) {
        if (index >= 0 && index < filesModel.getRowCount()) {
            filesModel.setValueAt(result.getStatus().name(), index, COL_STATUS);
            filesModel.setValueAt(result.getTotal(), index, COL_VOWELS);
            filesModel.setValueAt(result.getProcessingTime(), index, COL_TIME);
        }
    }

    // Reset all file statuses back to PENDING — before a new run
    public void resetFileStatuses() {
        for (int i = 0; i < filesModel.getRowCount(); i++) {
            filesModel.setValueAt(VowelResult.Status.PENDING.name(), i, COL_STATUS);
            filesModel.setValueAt("-", i, COL_VOWELS);
            filesModel.setValueAt("-", i, COL_TIME);
        }
    }

    // Clear table — called when new files are loaded
    public void clearTable() {
        filesModel.setRowCount(0); // Removes all rows
    }

    // == North counter updates ====================================
    public void setFileCount(int count) {
        lblFilesNumb.setText(String.valueOf(count));
    }

    public void setThreadCount(int count) {
        lblThreadsNumb.setText(String.valueOf(count));
    }

    // AppStatus enum — shown in status label
    public void setStatus(AppStatus status) {
        switch (status) {
            case IDLE ->
                lblStatusValue.setText(AppStatus.IDLE.toString());
            case RUNNING_SEQUENTIAL ->
                lblStatusValue.setText(AppStatus.RUNNING_SEQUENTIAL.toString());
            case RUNNING_CONCURRENT ->
                lblStatusValue.setText(AppStatus.RUNNING_CONCURRENT.toString());
            case DONE ->
                lblStatusValue.setText(AppStatus.DONE.toString());
        }
    }

    // == South time updates =====================================0
    public void setSequentialTime(long ms) {
        lblSequentialNumb.setText(ms + " ms");
    }

    public void setConcurrentTime(long ms) {
        lblConcurrentNumb.setText(ms + " ms");
    }

    public void setSpeedup(String text) {
        lblSpeedupNumb.setText(text);
    }

    public void setEfficiency(String text) {
        lblEfficiencyNumb.setText(text);
    }

    // == Button control — disabled while running ====================
    // No interruption allowed — it's a competition!
    public void setButtonsEnabled(boolean enabled) {
        btnUpload.setEnabled(enabled);
        btnSequential.setEnabled(enabled);
        btnConcurrent.setEnabled(enabled);
        spnThreads.setEnabled(enabled);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        lblFiles = new javax.swing.JLabel();
        lblThreads = new javax.swing.JLabel();
        lblStatus = new javax.swing.JLabel();
        lblFilesNumb = new javax.swing.JLabel();
        lblThreadsNumb = new javax.swing.JLabel();
        lblStatusValue = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        btnUpload = new javax.swing.JButton();
        spnThreads = new javax.swing.JSpinner();
        btnSequential = new javax.swing.JButton();
        btnConcurrent = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        FilesTable = new javax.swing.JTable();
        jPanel3 = new javax.swing.JPanel();
        lblSequiential = new javax.swing.JLabel();
        lblConcurrent = new javax.swing.JLabel();
        lblEfficiency = new javax.swing.JLabel();
        lblSpeedUp = new javax.swing.JLabel();
        lblSequentialNumb = new javax.swing.JLabel();
        lblConcurrentNumb = new javax.swing.JLabel();
        lblEfficiencyNumb = new javax.swing.JLabel();
        lblSpeedupNumb = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setLayout(new java.awt.GridLayout(2, 3));

        lblFiles.setText("Files");
        jPanel1.add(lblFiles);

        lblThreads.setText("Threads");
        jPanel1.add(lblThreads);

        lblStatus.setText("Status");
        jPanel1.add(lblStatus);

        lblFilesNumb.setText("0");
        jPanel1.add(lblFilesNumb);

        lblThreadsNumb.setText("0");
        jPanel1.add(lblThreadsNumb);

        lblStatusValue.setText("Idle");
        jPanel1.add(lblStatusValue);

        getContentPane().add(jPanel1, java.awt.BorderLayout.PAGE_START);

        jPanel2.setLayout(new java.awt.GridLayout(4, 1));

        btnUpload.setText("Upload files");
        jPanel2.add(btnUpload);
        jPanel2.add(spnThreads);

        btnSequential.setText("Run Sequential");
        btnSequential.setActionCommand("Sequential");
        jPanel2.add(btnSequential);

        btnConcurrent.setText("Run Concurrent");
        jPanel2.add(btnConcurrent);

        getContentPane().add(jPanel2, java.awt.BorderLayout.LINE_START);

        FilesTable.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane1.setViewportView(FilesTable);

        getContentPane().add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jPanel3.setLayout(new java.awt.GridLayout(2, 4));

        lblSequiential.setText("Sequential");
        jPanel3.add(lblSequiential);

        lblConcurrent.setText("Concurrent");
        jPanel3.add(lblConcurrent);

        lblEfficiency.setText("Efficiency");
        jPanel3.add(lblEfficiency);

        lblSpeedUp.setText("Speedup");
        jPanel3.add(lblSpeedUp);

        lblSequentialNumb.setText("0");
        jPanel3.add(lblSequentialNumb);

        lblConcurrentNumb.setText("0");
        jPanel3.add(lblConcurrentNumb);

        lblEfficiencyNumb.setText("0");
        jPanel3.add(lblEfficiencyNumb);

        lblSpeedupNumb.setText("0X");
        jPanel3.add(lblSpeedupNumb);

        getContentPane().add(jPanel3, java.awt.BorderLayout.PAGE_END);

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
    private javax.swing.JTable FilesTable;
    private javax.swing.JButton btnConcurrent;
    private javax.swing.JButton btnSequential;
    private javax.swing.JButton btnUpload;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblConcurrent;
    private javax.swing.JLabel lblConcurrentNumb;
    private javax.swing.JLabel lblEfficiency;
    private javax.swing.JLabel lblEfficiencyNumb;
    private javax.swing.JLabel lblFiles;
    private javax.swing.JLabel lblFilesNumb;
    private javax.swing.JLabel lblSequentialNumb;
    private javax.swing.JLabel lblSequiential;
    private javax.swing.JLabel lblSpeedUp;
    private javax.swing.JLabel lblSpeedupNumb;
    private javax.swing.JLabel lblStatus;
    private javax.swing.JLabel lblStatusValue;
    private javax.swing.JLabel lblThreads;
    private javax.swing.JLabel lblThreadsNumb;
    private javax.swing.JSpinner spnThreads;
    // End of variables declaration//GEN-END:variables
}

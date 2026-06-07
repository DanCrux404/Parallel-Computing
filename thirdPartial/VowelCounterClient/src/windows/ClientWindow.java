package windows;

import java.awt.Color;
import java.awt.Component;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import model.VowelResult;

/**
 *
 * @author dante
 */
public class ClientWindow extends javax.swing.JFrame {

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(ClientWindow.class.getName());
    //Yeah, variables so we don't use magic numbers... as always... you know this...
    private static final int COL_NAME = 0;
    private static final int COL_STATUS = 1;
    private static final int COL_VOWELS = 2;
    private static final int COL_TIME = 3;

    private final DefaultTableModel filesModel = new DefaultTableModel(
            new String[]{"File", "Status", "Vowels", "Time(ms)"}, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }
    };

    /**
     * Creates new form ClientWindow
     */
    public ClientWindow() {
        initComponents();
        setupTable();
    }

    private void setupTable() {
        jTable1.setModel(filesModel);
        jTable1.setRowHeight(25);

        jTable1.getColumnModel().getColumn(COL_STATUS).setCellRenderer(
                new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    javax.swing.JTable t, Object value, boolean sel,
                    boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, value, sel, foc, row, col);
                if ("DONE".equals(value)) {
                    setBackground(new Color(144, 238, 144));
                } else if ("PROCESSING".equals(value)) {
                    setBackground(new Color(255, 255, 153));
                } else if ("ERROR".equals(value)) {
                    setBackground(new Color(255, 102, 102));
                } else {
                    setBackground(Color.WHITE);
                }
                setForeground(Color.BLACK);
                return this;
            }
        }
        );
    }

// == PUBLIC METHODS for ClientManager =======================
    public void setServerIp(String ip) {
        lblIp.setText(ip);
    }

    public void setStatus(String status) {
        lblStat.setText(status);
    }

    public void setFileCount(int count) {
        lblFilesNumb.setText(String.valueOf(count));
    }

    public void setThreadCount(int count) {
        lblThreadsCount.setText(String.valueOf(count));
    }

    public void setProcessingTime(long ms) {
        lblTimeCount.setText(ms + " ms");
    }

    public void addFileRow(String name) {
        filesModel.addRow(new Object[]{name, "PENDING", "-", "-"});
    }

    public void updateFileStatus(int index, VowelResult.Status status, String fileName) {
        if (index >= 0 && index < filesModel.getRowCount()) {
            filesModel.setValueAt(fileName, index, COL_NAME);
            filesModel.setValueAt(status.name(), index, COL_STATUS);
        }
    }

    public void updateFileResult(int index, VowelResult result) {
        if (index >= 0 && index < filesModel.getRowCount()) {
            filesModel.setValueAt(result.getFileName(), index, COL_NAME);
            filesModel.setValueAt(result.getStatus().name(), index, COL_STATUS);
            filesModel.setValueAt(result.getTotal(), index, COL_VOWELS);
            filesModel.setValueAt(result.getProcessingTime(), index, COL_TIME);
            updateStats(result);
        }
    }

    private void updateStats(VowelResult result) {
        lblCountA.setText(String.valueOf(
                parseLong(lblCountA.getText()) + result.getCountA()));
        lblCountE.setText(String.valueOf(
                parseLong(lblCountE.getText()) + result.getCountE()));
        lblCountI.setText(String.valueOf(
                parseLong(lblCountI.getText()) + result.getCountI()));
        lblCountO.setText(String.valueOf(
                parseLong(lblCountO.getText()) + result.getCountO()));
        lblCountU.setText(String.valueOf(
                parseLong(lblCountU.getText()) + result.getCountU()));
        lblAccentedCount.setText(String.valueOf(
                parseLong(lblAccentedCount.getText()) + result.getCountAccented()));

        long total = parseLong(lblTotalCount.getText()) + result.getTotal();
        lblTotalCount.setText(String.valueOf(total));
        updateMostFrequent();
    }

    private void updateMostFrequent() {
        long a = parseLong(lblCountA.getText());
        long e = parseLong(lblCountE.getText());
        long i = parseLong(lblCountI.getText());
        long o = parseLong(lblCountO.getText());
        long u = parseLong(lblCountU.getText());
        long max = Math.max(a, Math.max(e, Math.max(i, Math.max(o, u))));
        if (max == 0) {
            lblMostFreqCount.setText("-");
            return;
        }
        if (max == a) {
            lblMostFreqCount.setText("A");
        } else if (max == e) {
            lblMostFreqCount.setText("E");
        } else if (max == i) {
            lblMostFreqCount.setText("I");
        } else if (max == o) {
            lblMostFreqCount.setText("O");
        } else {
            lblMostFreqCount.setText("U");
        }
    }

    private long parseLong(String text) {
        try {
            return Long.parseLong(text);
        } catch (NumberFormatException ex) {
            return 0;
        }
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
        lblServerIp = new javax.swing.JLabel();
        lblIp = new javax.swing.JLabel();
        lblStatus = new javax.swing.JLabel();
        lblStat = new javax.swing.JLabel();
        lblFiles = new javax.swing.JLabel();
        lblFilesNumb = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jPanel2 = new javax.swing.JPanel();
        lblA = new javax.swing.JLabel();
        lblCountA = new javax.swing.JLabel();
        lblE = new javax.swing.JLabel();
        lblCountE = new javax.swing.JLabel();
        lblI = new javax.swing.JLabel();
        lblCountI = new javax.swing.JLabel();
        lblO = new javax.swing.JLabel();
        lblCountO = new javax.swing.JLabel();
        lblU = new javax.swing.JLabel();
        lblCountU = new javax.swing.JLabel();
        lblAccented = new javax.swing.JLabel();
        lblAccentedCount = new javax.swing.JLabel();
        lblTotal = new javax.swing.JLabel();
        lblTotalCount = new javax.swing.JLabel();
        lblMostFreq = new javax.swing.JLabel();
        lblMostFreqCount = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        lblTime = new javax.swing.JLabel();
        lblTimeCount = new javax.swing.JLabel();
        lblThreads = new javax.swing.JLabel();
        lblThreadsCount = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setLayout(new java.awt.GridLayout(1, 6));

        lblServerIp.setText("Server IP:");
        jPanel1.add(lblServerIp);

        lblIp.setText("0.0.0.0");
        jPanel1.add(lblIp);

        lblStatus.setText("Status:");
        jPanel1.add(lblStatus);

        lblStat.setText("-----");
        jPanel1.add(lblStat);

        lblFiles.setText("Files:");
        jPanel1.add(lblFiles);

        lblFilesNumb.setText("0");
        jPanel1.add(lblFilesNumb);

        getContentPane().add(jPanel1, java.awt.BorderLayout.PAGE_START);

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane1.setViewportView(jTable1);

        getContentPane().add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jPanel2.setLayout(new java.awt.GridLayout(4, 4));

        lblA.setText("A:");
        jPanel2.add(lblA);

        lblCountA.setText("0");
        jPanel2.add(lblCountA);

        lblE.setText("E:");
        jPanel2.add(lblE);

        lblCountE.setText("0");
        jPanel2.add(lblCountE);

        lblI.setText("I:");
        jPanel2.add(lblI);

        lblCountI.setText("0");
        jPanel2.add(lblCountI);

        lblO.setText("O:");
        jPanel2.add(lblO);

        lblCountO.setText("0");
        jPanel2.add(lblCountO);

        lblU.setText("U:");
        jPanel2.add(lblU);

        lblCountU.setText("0");
        jPanel2.add(lblCountU);

        lblAccented.setText("Accented:");
        jPanel2.add(lblAccented);

        lblAccentedCount.setText("0");
        jPanel2.add(lblAccentedCount);

        lblTotal.setText("Total:");
        jPanel2.add(lblTotal);

        lblTotalCount.setText("0");
        jPanel2.add(lblTotalCount);

        lblMostFreq.setText("Most freq:");
        jPanel2.add(lblMostFreq);

        lblMostFreqCount.setText("0");
        jPanel2.add(lblMostFreqCount);

        getContentPane().add(jPanel2, java.awt.BorderLayout.LINE_END);

        jPanel3.setLayout(new java.awt.GridLayout(1, 4));

        lblTime.setText("Processing Time:");
        jPanel3.add(lblTime);

        lblTimeCount.setText("0");
        jPanel3.add(lblTimeCount);

        lblThreads.setText("Threads:");
        jPanel3.add(lblThreads);

        lblThreadsCount.setText("0");
        jPanel3.add(lblThreadsCount);

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
        java.awt.EventQueue.invokeLater(() -> new ClientWindow().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JLabel lblA;
    private javax.swing.JLabel lblAccented;
    private javax.swing.JLabel lblAccentedCount;
    private javax.swing.JLabel lblCountA;
    private javax.swing.JLabel lblCountE;
    private javax.swing.JLabel lblCountI;
    private javax.swing.JLabel lblCountO;
    private javax.swing.JLabel lblCountU;
    private javax.swing.JLabel lblE;
    private javax.swing.JLabel lblFiles;
    private javax.swing.JLabel lblFilesNumb;
    private javax.swing.JLabel lblI;
    private javax.swing.JLabel lblIp;
    private javax.swing.JLabel lblMostFreq;
    private javax.swing.JLabel lblMostFreqCount;
    private javax.swing.JLabel lblO;
    private javax.swing.JLabel lblServerIp;
    private javax.swing.JLabel lblStat;
    private javax.swing.JLabel lblStatus;
    private javax.swing.JLabel lblThreads;
    private javax.swing.JLabel lblThreadsCount;
    private javax.swing.JLabel lblTime;
    private javax.swing.JLabel lblTimeCount;
    private javax.swing.JLabel lblTotal;
    private javax.swing.JLabel lblTotalCount;
    private javax.swing.JLabel lblU;
    // End of variables declaration//GEN-END:variables

    public void clearTable() {
        filesModel.setRowCount(0);
    }
}

package AdminPanel;

import DataBase.StaffDataBase;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import parking.MainPage;
import util.WindowUtils;

/**
 * Admin screen for managing staff accounts.
 *
 * FEATURE: this is the missing module described in the "Option 2 -
 * Staff Management Module" request. Previously there was no way for
 * an admin to see, add, change, or remove staff logins - the two
 * accounts were permanently hardcoded in the source code. Now the
 * admin can see every account a staff member has self-registered
 * (see parking.StaffRegister), deactivate one instantly if someone
 * leaves, reactivate one, or reset a forgotten password - all without
 * touching any code.
 *
 * @author Claude (added for user-requested Staff Management feature)
 */
public class StaffManagement extends javax.swing.JFrame {

    private final StaffDataBase staffDb = new StaffDataBase();
    private DefaultTableModel dtm;
    private javax.swing.JTable table;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JButton Activate;
    private javax.swing.JButton Deactivate;
    private javax.swing.JButton ResetPassword;
    private javax.swing.JButton Delete;
    private javax.swing.JButton Refresh;
    private javax.swing.JButton Back;

    public StaffManagement() {
        initComponents();
        staffDb.ensureStaffTable();
        loadStaff();
    }

    private void initComponents() {
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Staff Management");

        jLabel1 = new javax.swing.JLabel("Staff Management", javax.swing.SwingConstants.CENTER);
        jLabel1.setFont(new java.awt.Font("Times New Roman", 1, 28));

        dtm = new DefaultTableModel(new Object[][]{}, new String[]{"Username", "Role", "Status"}) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new javax.swing.JTable(dtm);
        javax.swing.JScrollPane scrollPane = new javax.swing.JScrollPane(table);

        Activate = new javax.swing.JButton("Activate");
        Activate.addActionListener(evt -> ActivateActionPerformed());

        Deactivate = new javax.swing.JButton("Deactivate");
        Deactivate.addActionListener(evt -> DeactivateActionPerformed());

        ResetPassword = new javax.swing.JButton("Reset Password");
        ResetPassword.addActionListener(evt -> ResetPasswordActionPerformed());

        Delete = new javax.swing.JButton("Delete Account");
        Delete.addActionListener(evt -> DeleteActionPerformed());

        Refresh = new javax.swing.JButton("Refresh");
        Refresh.addActionListener(evt -> loadStaff());

        Back = new javax.swing.JButton("Back");
        Back.addActionListener(evt -> BackActionPerformed());

        javax.swing.JPanel buttonPanel = new javax.swing.JPanel(new java.awt.GridLayout(1, 6, 12, 10));
        buttonPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 15, 10, 15));
        buttonPanel.add(Activate);
        buttonPanel.add(Deactivate);
        buttonPanel.add(ResetPassword);
        buttonPanel.add(Delete);
        buttonPanel.add(Refresh);
        buttonPanel.add(Back);

        getContentPane().setLayout(new java.awt.BorderLayout(10, 10));
        getContentPane().add(jLabel1, java.awt.BorderLayout.NORTH);
        getContentPane().add(scrollPane, java.awt.BorderLayout.CENTER);
        getContentPane().add(buttonPanel, java.awt.BorderLayout.SOUTH);

        setSize(760, 420);
        setLocationRelativeTo(null);
    }

    /** Reloads the table from the database. */
    private void loadStaff() {
        dtm.setRowCount(0);
        List<Object[]> rows = staffDb.listStaff();
        for (Object[] row : rows) {
            dtm.addRow(row);
        }
    }

    /** @return the username in the currently selected row, or null if none selected. */
    private String getSelectedUsername() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this,
                    "Please select a staff account from the table first.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return (String) dtm.getValueAt(row, 0);
    }

    private void ActivateActionPerformed() {
        String username = getSelectedUsername();
        if (username == null) {
            return;
        }
        staffDb.setActive(username, true);
        loadStaff();
    }

    private void DeactivateActionPerformed() {
        String username = getSelectedUsername();
        if (username == null) {
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Deactivate account \"" + username + "\"? They will no longer be able to log in.",
                "Confirm Deactivation", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            staffDb.setActive(username, false);
            loadStaff();
        }
    }

    private void ResetPasswordActionPerformed() {
        String username = getSelectedUsername();
        if (username == null) {
            return;
        }
        String newPassword = JOptionPane.showInputDialog(this,
                "Enter a new password for \"" + username + "\":",
                "Reset Password", JOptionPane.PLAIN_MESSAGE);
        if (newPassword != null && !newPassword.trim().isEmpty()) {
            staffDb.updatePassword(username, newPassword.trim());
            JOptionPane.showMessageDialog(this, "Password updated for " + username + ".");
        }
    }

    private void DeleteActionPerformed() {
        String username = getSelectedUsername();
        if (username == null) {
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Permanently delete account \"" + username + "\"? This cannot be undone.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            staffDb.deleteStaff(username);
            loadStaff();
        }
    }

    private void BackActionPerformed() {
        WindowUtils.closeWindow(this);
        AdminPanel a1 = new AdminPanel();
        a1.setVisible(true);
    }

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new StaffManagement().setVisible(true);
            }
        });
    }
}

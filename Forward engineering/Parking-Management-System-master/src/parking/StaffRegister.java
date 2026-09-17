package parking;

import DataBase.StaffDataBase;
import javax.swing.JOptionPane;
import util.WindowUtils;

/**
 * Self-service staff registration screen.
 *
 * FEATURE: previously there was no way for a new staff member to get
 * an account at all - the only two logins ("checkin"/"checkout") were
 * permanently hardcoded in the source code. This screen lets a staff
 * member pick their own username and password and choose which desk
 * they work at (Check-In or Check-Out). The account is created
 * immediately as active, and can later be deactivated or have its
 * password reset by the admin from the Staff Management screen.
 *
 * @author Claude (added for user-requested Staff Management feature)
 */
public class StaffRegister extends javax.swing.JFrame {

    private final StaffDataBase staffDb = new StaffDataBase();

    public StaffRegister() {
        initComponents();
        staffDb.ensureStaffTable();
    }

    @SuppressWarnings("unchecked")
    private void initComponents() {

        title = new javax.swing.JLabel();
        userLabel = new javax.swing.JLabel();
        Username = new javax.swing.JTextField();
        passLabel = new javax.swing.JLabel();
        Password = new javax.swing.JPasswordField();
        confirmLabel = new javax.swing.JLabel();
        ConfirmPassword = new javax.swing.JPasswordField();
        roleLabel = new javax.swing.JLabel();
        Role = new javax.swing.JComboBox();
        Create = new javax.swing.JButton();
        Back = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Staff Registration");
        setResizable(false);

        java.awt.Font labelFont = new java.awt.Font("Times New Roman", java.awt.Font.BOLD, 14);
        java.awt.Font fieldFont = new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 13);

        title.setFont(new java.awt.Font("Times New Roman", java.awt.Font.BOLD, 22));
        title.setText("Create Staff Account");
        title.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        userLabel.setFont(labelFont);
        userLabel.setText("Choose a Username");
        Username.setFont(fieldFont);
        Username.setColumns(18);

        passLabel.setFont(labelFont);
        passLabel.setText("Choose a Password");
        Password.setFont(fieldFont);
        Password.setColumns(18);

        confirmLabel.setFont(labelFont);
        confirmLabel.setText("Confirm Password");
        ConfirmPassword.setFont(fieldFont);
        ConfirmPassword.setColumns(18);

        roleLabel.setFont(labelFont);
        roleLabel.setText("Which Desk Do You Work At?");
        Role.setFont(fieldFont);
        Role.setModel(new javax.swing.DefaultComboBoxModel(new String[]{"checkin", "checkout"}));

        Create.setFont(new java.awt.Font("Times New Roman", java.awt.Font.BOLD, 14));
        Create.setText("Create Account");
        Create.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        Create.setPreferredSize(new java.awt.Dimension(150, 32));
        Create.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CreateActionPerformed(evt);
            }
        });

        Back.setFont(new java.awt.Font("Times New Roman", java.awt.Font.BOLD, 14));
        Back.setText("Back to Login");
        Back.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        Back.setPreferredSize(new java.awt.Dimension(150, 32));
        Back.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BackActionPerformed(evt);
            }
        });

        // ---- form: GridBagLayout gives every label/field pair the same
        // left-aligned, evenly-spaced layout, so nothing is ever centered
        // oddly or cut off regardless of label text length or font metrics.
        javax.swing.JPanel form = new javax.swing.JPanel(new java.awt.GridBagLayout());
        form.setBorder(javax.swing.BorderFactory.createEmptyBorder(25, 35, 20, 35));

        java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        gbc.gridy = 0;
        gbc.insets = new java.awt.Insets(0, 0, 20, 0);
        form.add(title, gbc);

        gbc.gridy = 1;
        gbc.insets = new java.awt.Insets(0, 0, 4, 0);
        form.add(userLabel, gbc);
        gbc.gridy = 2;
        gbc.insets = new java.awt.Insets(0, 0, 14, 0);
        form.add(Username, gbc);

        gbc.gridy = 3;
        gbc.insets = new java.awt.Insets(0, 0, 4, 0);
        form.add(passLabel, gbc);
        gbc.gridy = 4;
        gbc.insets = new java.awt.Insets(0, 0, 14, 0);
        form.add(Password, gbc);

        gbc.gridy = 5;
        gbc.insets = new java.awt.Insets(0, 0, 4, 0);
        form.add(confirmLabel, gbc);
        gbc.gridy = 6;
        gbc.insets = new java.awt.Insets(0, 0, 14, 0);
        form.add(ConfirmPassword, gbc);

        gbc.gridy = 7;
        gbc.insets = new java.awt.Insets(0, 0, 4, 0);
        form.add(roleLabel, gbc);
        gbc.gridy = 8;
        gbc.insets = new java.awt.Insets(0, 0, 22, 0);
        form.add(Role, gbc);

        javax.swing.JPanel buttons = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 16, 0));
        buttons.add(Create);
        buttons.add(Back);
        gbc.gridy = 9;
        gbc.insets = new java.awt.Insets(0, 0, 0, 0);
        form.add(buttons, gbc);

        getContentPane().add(form);

        // pack() sizes the window to fit the content exactly, so the
        // longest label/field always has the room it needs - no more
        // guessed pixel widths, no more truncated text or dead space.
        pack();
        setLocationRelativeTo(null);
    }

    private void CreateActionPerformed(java.awt.event.ActionEvent evt) {
        String username = Username.getText() == null ? "" : Username.getText().trim();
        String password = new String(Password.getPassword());
        String confirm = new String(ConfirmPassword.getPassword());
        String role = (String) Role.getSelectedItem();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter both a username and a password.",
                    "Missing Information", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!password.equals(confirm)) {
            JOptionPane.showMessageDialog(this,
                    "Password and Confirm Password do not match.",
                    "Password Mismatch", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (staffDb.usernameExists(username)) {
            JOptionPane.showMessageDialog(this,
                    "That username is already taken. Please choose another one.",
                    "Username Taken", JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean created = staffDb.addStaff(username, password, role);
        if (created) {
            JOptionPane.showMessageDialog(this,
                    "Account created! You can now log in with your new username and password.",
                    "Account Created", JOptionPane.INFORMATION_MESSAGE);
            WindowUtils.closeWindow(this);
            login l1 = new login();
            l1.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Could not create the account. Please try again.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void BackActionPerformed(java.awt.event.ActionEvent evt) {
        WindowUtils.closeWindow(this);
        login l1 = new login();
        l1.setVisible(true);
    }

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new StaffRegister().setVisible(true);
            }
        });
    }

    private javax.swing.JLabel title;
    private javax.swing.JLabel userLabel;
    private javax.swing.JTextField Username;
    private javax.swing.JLabel passLabel;
    private javax.swing.JPasswordField Password;
    private javax.swing.JLabel confirmLabel;
    private javax.swing.JPasswordField ConfirmPassword;
    private javax.swing.JLabel roleLabel;
    private javax.swing.JComboBox Role;
    private javax.swing.JButton Create;
    private javax.swing.JButton Back;
}

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

public class ContactManager extends JFrame {
    private DefaultTableModel contactTableModel;
    private JTable contactTable;
    private JTextField nameField;
    private JTextField phoneField;
    private JTextField emailField;
    private JButton addButton;
    private JButton editButton;
    private JButton saveButton;
    private JButton deleteButton;
    private File contactsFile = new File("contacts.txt");
    private int selectedRow = -1;

    public ContactManager() {
        setTitle("Contact Manager");
        setSize(520, 400); // Réduction de la taille de la fenêtre
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setLocationRelativeTo(null); // Centrage de la fenêtre

        String[] columnNames = {"Nom", "Numero de telephone", "Adresse email"};
        contactTableModel = new DefaultTableModel(columnNames, 0);
        contactTable = new JTable(contactTableModel);
        contactTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        contactTable.getColumnModel().getColumn(0).setPreferredWidth(150); // Setting column widths
        contactTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        contactTable.getColumnModel().getColumn(2).setPreferredWidth(205);
        contactTable.getTableHeader().setReorderingAllowed(false); // Disable column reordering
        contactTable.setAutoCreateRowSorter(true); // Enable row sorting
        contactTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // Single row selection
        contactTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS); // Allow automatic resizing of columns

        JScrollPane tableScrollPane = new JScrollPane(contactTable);
        add(tableScrollPane, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.LINE_END;
        gbc.insets = new Insets(5, 5, 5, 5);

        inputPanel.add(new JLabel("Nom:"), gbc);
        gbc.gridy++;
        inputPanel.add(new JLabel("Numero de telephone:"), gbc);
        gbc.gridy++;
        inputPanel.add(new JLabel("Adresse email:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        nameField = new JTextField(20);
        inputPanel.add(nameField, gbc);
        gbc.gridy++;
        phoneField = new JTextField(20);
        restrictToNumbers(phoneField); // Appliquer le filtre de chiffres
        inputPanel.add(phoneField, gbc);
        gbc.gridy++;
        emailField = new JTextField(20);
        inputPanel.add(emailField, gbc);

        add(inputPanel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        addButton = new JButton("Ajouter");
        addButton.setBackground(Color.DARK_GRAY);
        addButton.setForeground(Color.WHITE);
        addButton.addActionListener(e -> addContact());
        buttonPanel.add(addButton);

        editButton = new JButton("Modifier");
        editButton.setBackground(Color.DARK_GRAY);
        editButton.setForeground(Color.WHITE);
        editButton.addActionListener(e -> editContact());
        editButton.setEnabled(false);
        buttonPanel.add(editButton);

        saveButton = new JButton("Sauvegarder");
        saveButton.setBackground(Color.DARK_GRAY);
        saveButton.setForeground(Color.WHITE);
        saveButton.addActionListener(e -> saveContactChanges());
        saveButton.setEnabled(false);
        buttonPanel.add(saveButton);

        deleteButton = new JButton("Supprimer");
        deleteButton.setBackground(Color.DARK_GRAY);
        deleteButton.setForeground(Color.WHITE);
        deleteButton.addActionListener(e -> deleteContact());
        deleteButton.setEnabled(false);
        buttonPanel.add(deleteButton);

        add(buttonPanel, BorderLayout.SOUTH);

        loadContacts();
        setVisible(true);
    }

    private void clearFields() {
        nameField.setText("");
        phoneField.setText("");
        emailField.setText("");
    }

    private void setFieldsEditable(boolean editable) {
        nameField.setEditable(editable);
        phoneField.setEditable(editable);
        emailField.setEditable(editable);
    }

    private void addContact() {
        String name = nameField.getText();
        String phone = phoneField.getText();
        String email = emailField.getText();
        if (!name.isEmpty() && !phone.isEmpty() && !email.isEmpty()) {
            contactTableModel.addRow(new Object[]{name, phone, email});
            saveContacts();
            clearFields();
            setFieldsEditable(true); // Set fields to editable for new input
            contactTable.clearSelection(); // Clear the selection to reset buttons
        } else {
            JOptionPane.showMessageDialog(this, "Veuillez remplir tous les champs.", "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void editContact() {
        if (selectedRow != -1) {
            setFieldsEditable(true);
            saveButton.setEnabled(true);
        }
    }

    private void saveContactChanges() {
        if (selectedRow != -1) {
            contactTableModel.setValueAt(nameField.getText(), selectedRow, 0);
            contactTableModel.setValueAt(phoneField.getText(), selectedRow, 1);
            contactTableModel.setValueAt(emailField.getText(), selectedRow, 2);
            saveContacts();
            setFieldsEditable(false);
            saveButton.setEnabled(false);
            contactTable.clearSelection(); // Clear the selection to reset buttons
        }
    }

    private void deleteContact() {
        if (selectedRow != -1) {
            contactTableModel.removeRow(selectedRow);
            saveContacts();
            clearFields();
            setFieldsEditable(true); // Set fields to editable for new input
            contactTable.clearSelection(); // Clear the selection to reset buttons
        }
    }

    private void loadContacts() {
        if (contactsFile.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(contactsFile))) {
                while (true) {
                    try {
                        Contact contact = (Contact) ois.readObject();
                        contactTableModel.addRow(new Object[]{contact.getName(), contact.getPhoneNumber(), contact.getEmail()});
                    } catch (EOFException e) {
                        break;
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private void saveContacts() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(contactsFile))) {
            for (int i = 0; i < contactTableModel.getRowCount(); i++) {
                String name = (String) contactTableModel.getValueAt(i, 0);
                String phone = (String) contactTableModel.getValueAt(i, 1);
                String email = (String) contactTableModel.getValueAt(i, 2);
                oos.writeObject(new Contact(name, phone, email));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void restrictToNumbers(JTextField textField) {
        PlainDocument doc = (PlainDocument) textField.getDocument();
        doc.setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
                if (string == null) return;
                if (containsOnlyNumbers(string)) {
                    super.insertString(fb, offset, string, attr);
                }
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                if (text == null) return;
                if (containsOnlyNumbers(text)) {
                    super.replace(fb, offset, length, text, attrs);
                }
            }

            private boolean containsOnlyNumbers(String text) {
                return text.matches("\\d*"); // Regex to match digits
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ContactManager::new);
    }
}

class Contact implements Serializable {
    private String name;
    private String phoneNumber;
    private String email;

    public Contact(String name, String phoneNumber, String email) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}

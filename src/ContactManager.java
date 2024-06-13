import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.PlainDocument;


public class ContactManager extends JFrame {
    private DefaultTableModel contactTableModel;
    private JTable contactTable;
    private JTextField nameField;
    private JTextField phoneField;
    private JTextField emailField;
    private JComboBox<String> groupComboBox;
    private JButton addButton;
    private JButton editButton;
    private JButton saveButton;
    private JButton deleteButton;
    private File contactsFile = new File("contacts.json");
    private int selectedRow = -1;
    private int selectedColumn = -1;
    private Gson gson;

    public ContactManager() {
        setTitle("Contact Manager");
        setSize(520, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setLocationRelativeTo(null);

        gson = new GsonBuilder().setPrettyPrinting().create(); // Utilisation de Gson avec mise en forme JSON

        String[] columnNames = {"Nom", "Numero de telephone", "Adresse email", "Groupe"};
        contactTableModel = new DefaultTableModel(columnNames, 0);
        contactTable = new JTable(contactTableModel);
        contactTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        contactTable.getTableHeader().setReorderingAllowed(false);
        contactTable.setAutoCreateRowSorter(true);
        contactTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        // Ajout d'un ListSelectionListener pour détecter les changements de sélection dans la JTable
        ListSelectionModel selectionModel = contactTable.getSelectionModel();
        selectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        selectionModel.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                selectedRow = contactTable.getSelectedRow();
                selectedColumn = contactTable.getSelectedColumn();
                updateButtonState();
                loadSelectedContact();
            }
        });

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
        gbc.gridy++;
        inputPanel.add(new JLabel("Groupe:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        nameField = new JTextField(20);
        inputPanel.add(nameField, gbc);
        gbc.gridy++;
        phoneField = new JTextField(20);
        restrictToNumbers(phoneField);
        inputPanel.add(phoneField, gbc);
        gbc.gridy++;
        emailField = new JTextField(20);
        inputPanel.add(emailField, gbc);
        gbc.gridy++;
        groupComboBox = new JComboBox<>(new String[]{"Famille", "Amis", "Travail"});
        inputPanel.add(groupComboBox, gbc);

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
        groupComboBox.setSelectedIndex(0);
    }

    private void setFieldsEditable(boolean editable) {
        nameField.setEditable(editable);
        phoneField.setEditable(editable);
        emailField.setEditable(editable);
        groupComboBox.setEnabled(editable);
    }

    private void addContact() {
        String name = nameField.getText();
        String phone = phoneField.getText();
        String email = emailField.getText();
        String group = (String) groupComboBox.getSelectedItem();
        if (!name.isEmpty() && !phone.isEmpty() && !email.isEmpty() && group != null) {
            contactTableModel.addRow(new Object[]{name, phone, email, group});
            saveContacts();
            clearFields();
            setFieldsEditable(true);
            contactTable.clearSelection();
        } else {
            JOptionPane.showMessageDialog(this, "Veuillez remplir tous les champs.", "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void editContact() {
        if (selectedRow != -1 && selectedColumn != -1) {
            setFieldsEditable(true);
            saveButton.setEnabled(true);
        }
    }

    private void saveContactChanges() {
        if (selectedRow != -1) {
            contactTableModel.setValueAt(nameField.getText(), selectedRow, 0);
            contactTableModel.setValueAt(phoneField.getText(), selectedRow, 1);
            contactTableModel.setValueAt(emailField.getText(), selectedRow, 2);
            contactTableModel.setValueAt(groupComboBox.getSelectedItem(), selectedRow, 3);
            saveContacts();
            setFieldsEditable(false);
            saveButton.setEnabled(false);
            contactTable.clearSelection();
        }
    }

    private void deleteContact() {
        if (selectedRow != -1) {
            contactTableModel.removeRow(selectedRow);
            saveContacts();
            clearFields();
            setFieldsEditable(true);
            contactTable.clearSelection();
        }
    }

    private void loadContacts() {
        if (contactsFile.exists()) {
            try {
                String jsonContent = readJsonFile(contactsFile);
                Contact[] contacts = gson.fromJson(jsonContent, Contact[].class);
                for (Contact contact : contacts) {
                    contactTableModel.addRow(new Object[]{contact.getName(), contact.getPhoneNumber(), contact.getEmail(), contact.getGroup()});
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void saveContacts() {
        List<Contact> contacts = new ArrayList<>();
        for (int i = 0; i < contactTableModel.getRowCount(); i++) {
            String name = (String) contactTableModel.getValueAt(i, 0);
            String phone = (String) contactTableModel.getValueAt(i, 1);
            String email = (String) contactTableModel.getValueAt(i, 2);
            String group = (String) contactTableModel.getValueAt(i, 3);
            Contact contact = new Contact(name, phone, email, group);
            contacts.add(contact);
        }

        String json = gson.toJson(contacts);
        try {
            writeJsonFile(json, contactsFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String readJsonFile(File file) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }
        }
        return content.toString();
    }

    private void writeJsonFile(String content, File file) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(content);
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
                return text.matches("\\d*");
            }
        });
    }

    private void updateButtonState() {
        if (selectedRow != -1 && selectedColumn != -1) {
            editButton.setEnabled(true);
            deleteButton.setEnabled(true);
        } else {
            editButton.setEnabled(false);
            deleteButton.setEnabled(false);
        }
    }

    private void loadSelectedContact() {
        if (selectedRow != -1 && selectedRow < contactTableModel.getRowCount()) {
            String name = (String) contactTableModel.getValueAt(selectedRow, 0);
            String phone = (String) contactTableModel.getValueAt(selectedRow, 1);
            String email = (String) contactTableModel.getValueAt(selectedRow, 2);
            String group = (String) contactTableModel.getValueAt(selectedRow, 3);
            nameField.setText(name);
            phoneField.setText(phone);
            emailField.setText(email);
            groupComboBox.setSelectedItem(group);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ContactManager());
    }
}


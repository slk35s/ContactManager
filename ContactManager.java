import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

public class ContactManager extends JFrame {
    private DefaultListModel<Contact> contactListModel;
    private JList<Contact> contactList;
    private JTextField nameField;
    private JTextField phoneField;
    private JTextField emailField;
    private JButton addButton;
    private JButton editButton;
    private JButton saveButton;
    private File contactsFile = new File("contacts.txt");
    private Contact selectedContact;

    public ContactManager() {
    setTitle("Contact Manager");
    setSize(600, 400);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setLayout(new BorderLayout());

    contactListModel = new DefaultListModel<>();
    contactList = new JList<>(contactListModel);
    contactList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    contactList.addListSelectionListener(e -> {
        selectedContact = contactList.getSelectedValue();
        if (selectedContact != null) {
            nameField.setText(selectedContact.getName());
            phoneField.setText(selectedContact.getPhoneNumber());
            emailField.setText(selectedContact.getEmail());
            editButton.setEnabled(true);
        } else {
            clearFields();
            editButton.setEnabled(false);
        }
    });
    add(new JScrollPane(contactList), BorderLayout.CENTER);

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

    nameField = new JTextField();
    inputPanel.add(nameField, gbc);
    gbc.gridy++;
    phoneField = new JTextField();
    inputPanel.add(phoneField, gbc);
    gbc.gridy++;
    emailField = new JTextField();
    inputPanel.add(emailField, gbc);

    add(inputPanel, BorderLayout.NORTH);

    JPanel buttonPanel = new JPanel();
    addButton = new JButton("Ajouter");
    addButton.addActionListener(e -> addContact());
    buttonPanel.add(addButton);
    editButton = new JButton("Modifier");
    editButton.addActionListener(e -> editContact());
    editButton.setEnabled(false);
    buttonPanel.add(editButton);
    saveButton = new JButton("Sauvegarder");
    saveButton.addActionListener(e -> saveContactChanges());
    saveButton.setEnabled(false);
    buttonPanel.add(saveButton);
    add(buttonPanel, BorderLayout.SOUTH);

    loadContacts();
    setVisible(true);
}


    private void clearFields() {
        nameField.setText("");
        phoneField.setText("");
        emailField.setText("");
    }

    private void addContact() {
        String name = nameField.getText();
        String phone = phoneField.getText();
        String email = emailField.getText();
        if (!name.isEmpty() && !phone.isEmpty() && !email.isEmpty()) {
            Contact contact = new Contact(name, phone, email);
            contactListModel.addElement(contact);
            saveContacts();
            clearFields();
        }
    }

    private void editContact() {
        if (selectedContact != null) {
            nameField.setEditable(true);
            phoneField.setEditable(true);
            emailField.setEditable(true);
            saveButton.setEnabled(true);
        }
    }

    private void saveContactChanges() {
        if (selectedContact != null) {
            selectedContact.setName(nameField.getText());
            selectedContact.setPhoneNumber(phoneField.getText());
            selectedContact.setEmail(emailField.getText());
            contactList.repaint();
            saveContacts();
            nameField.setEditable(false);
            phoneField.setEditable(false);
            emailField.setEditable(false);
            saveButton.setEnabled(false);
        }
    }

    private void loadContacts() {
        if (contactsFile.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(contactsFile))) {
                while (true) {
                    try {
                        Contact contact = (Contact) ois.readObject();
                        contactListModel.addElement(contact);
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
            for (int i = 0; i < contactListModel.getSize(); i++) {
                oos.writeObject(contactListModel.getElementAt(i));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

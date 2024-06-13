import java.io.Serializable;

public class Contact implements Serializable {
    private String name;
    private String phoneNumber;
    private String email;
    private String group; // Nouveau champ pour le groupe du contact

    public Contact(String name, String phoneNumber, String email, String group) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.group = group;
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

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    @Override
    public String toString() {
        return name + " - " + phoneNumber + " - " + email + " - Groupe: " + group;
    }
}

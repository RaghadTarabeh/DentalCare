package Entity;

import java.util.Objects;

public class Patient {

    private int patientID;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String emailAddress;
    private int age;

    public Patient() { }

	public Patient(int patientID, String firstName, String lastName, String phoneNumber, String emailAddress, int age) {
		super();
		this.patientID = patientID;
		this.firstName = firstName;
		this.lastName = lastName;
		this.phoneNumber = phoneNumber;
		this.emailAddress = emailAddress;
		this.age = age;
	}
	public String getFullName() {
	    return firstName + " " + lastName;
	}

	public int getPatientID() {
		return patientID;
	}

	public void setPatientID(int patientID) {
		this.patientID = patientID;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	@Override
	public int hashCode() {
		return Objects.hash(age, emailAddress, firstName, lastName, patientID, phoneNumber);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Patient other = (Patient) obj;
		return age == other.age && Objects.equals(emailAddress, other.emailAddress)
				&& Objects.equals(firstName, other.firstName) && Objects.equals(lastName, other.lastName)
				&& patientID == other.patientID && Objects.equals(phoneNumber, other.phoneNumber);
	}

	@Override
	public String toString() {
		return "Patient [patientID=" + patientID + ", firstName=" + firstName + ", lastName=" + lastName
				+ ", phoneNumber=" + phoneNumber + ", emailAddress=" + emailAddress + ", age=" + age + "]";
	}

    
}

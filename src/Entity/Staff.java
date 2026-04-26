package Entity;

import java.util.Objects;

public class Staff {

    private int staffID;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String emailAddress;
    private int roleID;             
    private int specializationID;   
    private String qualification;
    private String scheduleDetails;
    private boolean isClinicManager;

    
    public Staff() { }    
    
    public Staff(int staffID, String firstName, String lastName, String phoneNumber, String emailAddress, int roleID,
			int specializationID, String qualification, String scheduleDetails, boolean isClinicManager) {
		super();
		this.staffID = staffID;
		this.firstName = firstName;
		this.lastName = lastName;
		this.phoneNumber = phoneNumber;
		this.emailAddress = emailAddress;
		this.roleID = roleID;
		this.specializationID = specializationID;
		this.qualification = qualification;
		this.scheduleDetails = scheduleDetails;
		this.isClinicManager = isClinicManager;
	}





	public int getStaffID() {
		return staffID;
	}

	public void setStaffID(int staffID) {
		this.staffID = staffID;
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

	public int getRoleID() {
		return roleID;
	}

	public void setRoleID(int roleID) {
		this.roleID = roleID;
	}

	public int getSpecializationID() {
		return specializationID;
	}

	public void setSpecializationID(int specializationID) {
		this.specializationID = specializationID;
	}

	public String getQualification() {
		return qualification;
	}

	public void setQualification(String qualification) {
		this.qualification = qualification;
	}

	public String getScheduleDetails() {
		return scheduleDetails;
	}

	public void setScheduleDetails(String scheduleDetails) {
		this.scheduleDetails = scheduleDetails;
	}

	public boolean isClinicManager() {
		return isClinicManager;
	}

	public void setClinicManager(boolean isClinicManager) {
		this.isClinicManager = isClinicManager;
	}

	
    @Override
	public int hashCode() {
		return Objects.hash(emailAddress, firstName, isClinicManager, lastName, phoneNumber, qualification, roleID,
				scheduleDetails, specializationID, staffID);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Staff other = (Staff) obj;
		return Objects.equals(emailAddress, other.emailAddress) && Objects.equals(firstName, other.firstName)
				&& isClinicManager == other.isClinicManager && Objects.equals(lastName, other.lastName)
				&& Objects.equals(phoneNumber, other.phoneNumber) && Objects.equals(qualification, other.qualification)
				&& roleID == other.roleID && Objects.equals(scheduleDetails, other.scheduleDetails)
				&& specializationID == other.specializationID && staffID == other.staffID;
	}

	@Override
	public String toString() {
	    return   firstName + " " + lastName;
	}
	
	
	
}

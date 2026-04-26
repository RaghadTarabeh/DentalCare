package Entity;

import java.sql.Date;
import java.sql.Time;

public class Reminder {
    private int reminderID;
    private int appointmentID;
    private Date reminderDateTime;
    private String reminderType;

    // Constructors
    public Reminder() {}

    public Reminder(int reminderID, int appointmentID, Date reminderDateTime, String reminderType) {
        this.reminderID = reminderID;
        this.appointmentID = appointmentID;
        this.reminderDateTime = reminderDateTime;
        this.reminderType = reminderType;
    }

    // Getters and Setters
    public int getReminderID() {
        return reminderID;
    }

    public void setReminderID(int reminderID) {
        this.reminderID = reminderID;
    }

    public int getAppointmentID() {
        return appointmentID;
    }

    public void setAppointmentID(int appointmentID) {
        this.appointmentID = appointmentID;
    }

    public Date getReminderDateTime() {
        return reminderDateTime;
    }

    public void setReminderDateTime(Date reminderDateTime) {
        this.reminderDateTime = reminderDateTime;
    }

    public String getReminderType() {
        return reminderType;
    }

    public void setReminderType(String reminderType) {
        this.reminderType = reminderType;
    }

    // Helper methods
    public boolean isEmailReminder() {
        return "Email".equalsIgnoreCase(reminderType);
    }

    public boolean isSMSReminder() {
        return "SMS".equalsIgnoreCase(reminderType);
    }

    public boolean isPastDue() {
        return reminderDateTime != null && reminderDateTime.before(new Date(System.currentTimeMillis()));
    }

    @Override
    public String toString() {
        return "Reminder (" + reminderType + ") for Appointment #" + appointmentID + 
               " on " + reminderDateTime;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Reminder reminder = (Reminder) obj;
        return reminderID == reminder.reminderID;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(reminderID);
    }
}
package Entity;

import java.sql.Date;
import java.sql.Time;

public class Appointment {
    private int appointmentID;
    private int patientID;
    private int staffID;
    private Date appointmentDate;
    private Time appointmentTime;
    private int visitReasonID;
    private int appointmentStatusID;

    // Constructors
    public Appointment() {}

    public Appointment(int appointmentID, int patientID, int staffID, Date appointmentDate, 
                      Time appointmentTime, int visitReasonID, int appointmentStatusID) {
        this.appointmentID = appointmentID;
        this.patientID = patientID;
        this.staffID = staffID;
        this.appointmentDate = appointmentDate;
        this.appointmentTime = appointmentTime;
        this.visitReasonID = visitReasonID;
        this.appointmentStatusID = appointmentStatusID;
    }

    // Getters and Setters
    public int getAppointmentID() {
        return appointmentID;
    }

    public void setAppointmentID(int appointmentID) {
        this.appointmentID = appointmentID;
    }

    public int getPatientID() {
        return patientID;
    }

    public void setPatientID(int patientID) {
        this.patientID = patientID;
    }

    public int getStaffID() {
        return staffID;
    }

    public void setStaffID(int staffID) {
        this.staffID = staffID;
    }

    public Date getAppointmentDate() {
        return appointmentDate;
    }

    public void setAppointmentDate(Date appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public Time getAppointmentTime() {
        return appointmentTime;
    }

    public void setAppointmentTime(Time appointmentTime) {
        this.appointmentTime = appointmentTime;
    }

    public int getVisitReasonID() {
        return visitReasonID;
    }

    public void setVisitReasonID(int visitReasonID) {
        this.visitReasonID = visitReasonID;
    }

    public int getAppointmentStatusID() {
        return appointmentStatusID;
    }

    public void setAppointmentStatusID(int appointmentStatusID) {
        this.appointmentStatusID = appointmentStatusID;
    }

    // Helper methods for status checking
    public boolean isScheduled() {
        return appointmentStatusID == 1; // Assuming 1 = Scheduled
    }

    public boolean isCompleted() {
        return appointmentStatusID == 2; // Assuming 2 = Completed
    }

    public boolean isCancelled() {
        return appointmentStatusID == 3; // Assuming 3 = Cancelled
    }

    public boolean isSuspended() {
        return appointmentStatusID == 4; // Assuming 4 = Suspended
    }

    @Override
    public String toString() {
        return "Appointment ID: " + appointmentID + " - Date: " + appointmentDate + 
               " Time: " + appointmentTime + " (Patient ID: " + patientID + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Appointment appointment = (Appointment) obj;
        return appointmentID == appointment.appointmentID;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(appointmentID);
    }
}
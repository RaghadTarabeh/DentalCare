package Entity;

public class AppointmentStatus {
    private int appointmentStatusID;
    private String statusName;

    // Constructors
    public AppointmentStatus() {}

    public AppointmentStatus(int appointmentStatusID, String statusName) {
        this.appointmentStatusID = appointmentStatusID;
        this.statusName = statusName;
    }

    // Getters and Setters
    public int getAppointmentStatusID() {
        return appointmentStatusID;
    }

    public void setAppointmentStatusID(int appointmentStatusID) {
        this.appointmentStatusID = appointmentStatusID;
    }

    public String getStatusName() {
        return statusName;
    }

    public void setStatusName(String statusName) {
        this.statusName = statusName;
    }

    // Helper methods based on your actual data
    public boolean isScheduled() {
        return appointmentStatusID == 1 || "Scheduled".equalsIgnoreCase(statusName);
    }

    public boolean isApproved() {
        return appointmentStatusID == 2 || "Approved".equalsIgnoreCase(statusName);
    }

    public boolean isCancelled() {
        return appointmentStatusID == 3 || "Cancelled".equalsIgnoreCase(statusName);
    }

    public boolean isRescheduled() {
        return appointmentStatusID == 4 || "Rescheduled".equalsIgnoreCase(statusName);
    }

    public boolean isCompleted() {
        return appointmentStatusID == 5 || "Completed".equalsIgnoreCase(statusName);
    }

    // For war-time suspension (you may need to add this status to your database)
    public boolean isSuspended() {
        return "Suspended".equalsIgnoreCase(statusName);
    }

    // Check if appointment is active (scheduled or approved)
    public boolean isActive() {
        return isScheduled() || isApproved();
    }

    @Override
    public String toString() {
        return statusName;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        AppointmentStatus status = (AppointmentStatus) obj;
        return appointmentStatusID == status.appointmentStatusID;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(appointmentStatusID);
    }
}
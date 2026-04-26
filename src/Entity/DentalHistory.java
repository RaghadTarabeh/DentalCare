package Entity;

public class DentalHistory {
    private int dentalHistoryID;
    private String pastTreatments;
    private String xRays;
    private int patientID;

    // Constructors
    public DentalHistory() {}

    public DentalHistory(int dentalHistoryID, String pastTreatments, String xRays, int patientID) {
        this.dentalHistoryID = dentalHistoryID;
        this.pastTreatments = pastTreatments;
        this.xRays = xRays;
        this.patientID = patientID;
    }

    // Getters and Setters
    public int getDentalHistoryID() {
        return dentalHistoryID;
    }

    public void setDentalHistoryID(int dentalHistoryID) {
        this.dentalHistoryID = dentalHistoryID;
    }

    public String getPastTreatments() {
        return pastTreatments;
    }

    public void setPastTreatments(String pastTreatments) {
        this.pastTreatments = pastTreatments;
    }

    public String getXRays() {
        return xRays;
    }

    public void setXRays(String xRays) {
        this.xRays = xRays;
    }

    public int getPatientID() {
        return patientID;
    }

    public void setPatientID(int patientID) {
        this.patientID = patientID;
    }

    // Helper methods
    public boolean hasPastTreatments() {
        return pastTreatments != null && !pastTreatments.trim().isEmpty() && !pastTreatments.equalsIgnoreCase("None");
    }

    public boolean hasXRays() {
        return xRays != null && !xRays.trim().isEmpty() && !xRays.equalsIgnoreCase("None");
    }

    @Override
    public String toString() {
        return "Dental History - Treatments: " + (hasPastTreatments() ? pastTreatments : "None") + 
               ", X-Rays: " + (hasXRays() ? xRays : "None");
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        DentalHistory history = (DentalHistory) obj;
        return dentalHistoryID == history.dentalHistoryID;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(dentalHistoryID);
    }
}
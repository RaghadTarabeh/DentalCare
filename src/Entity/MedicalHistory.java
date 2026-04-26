package Entity;

public class MedicalHistory {
    private int medicalHistoryID;
    private int patientID;
    private String allergies;
    private String preExistingCondition;

    // Constructors
    public MedicalHistory() {}

    public MedicalHistory(int medicalHistoryID, int patientID, String allergies, String preExistingCondition) {
        this.medicalHistoryID = medicalHistoryID;
        this.patientID = patientID;
        this.allergies = allergies;
        this.preExistingCondition = preExistingCondition;
    }

    // Getters and Setters
    public int getMedicalHistoryID() {
        return medicalHistoryID;
    }

    public void setMedicalHistoryID(int medicalHistoryID) {
        this.medicalHistoryID = medicalHistoryID;
    }

    public int getPatientID() {
        return patientID;
    }

    public void setPatientID(int patientID) {
        this.patientID = patientID;
    }

    public String getAllergies() {
        return allergies;
    }

    public void setAllergies(String allergies) {
        this.allergies = allergies;
    }

    public String getPreExistingCondition() {
        return preExistingCondition;
    }

    public void setPreExistingCondition(String preExistingCondition) {
        this.preExistingCondition = preExistingCondition;
    }

    // Helper methods
    public boolean hasAllergies() {
        return allergies != null && !allergies.trim().isEmpty() && !allergies.equalsIgnoreCase("None");
    }

    public boolean hasPreExistingConditions() {
        return preExistingCondition != null && !preExistingCondition.trim().isEmpty() && !preExistingCondition.equalsIgnoreCase("None");
    }

    @Override
    public String toString() {
        return "Medical History - Allergies: " + (hasAllergies() ? allergies : "None") + 
               ", Conditions: " + (hasPreExistingConditions() ? preExistingCondition : "None");
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        MedicalHistory history = (MedicalHistory) obj;
        return medicalHistoryID == history.medicalHistoryID;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(medicalHistoryID);
    }
}
package Entity;

public class Specialization {
    private int specializationID;
    private String specializationName;

    // Constructors
    public Specialization() {}

    public Specialization(int specializationID, String specializationName) {
        this.specializationID = specializationID;
        this.specializationName = specializationName;
    }

    // Getters and Setters
    public int getSpecializationID() {
        return specializationID;
    }

    public void setSpecializationID(int specializationID) {
        this.specializationID = specializationID;
    }

    public String getSpecializationName() {
        return specializationName;
    }

    public void setSpecializationName(String specializationName) {
        this.specializationName = specializationName;
    }

    // Helper methods based on your actual data
    public boolean isOrthodontics() {
        return specializationID == 1 || "Orthodontics".equalsIgnoreCase(specializationName);
    }

    public boolean isPeriodontics() {
        return specializationID == 2 || "Periodontics".equalsIgnoreCase(specializationName);
    }

    // You can add more specializations as needed
    public boolean isGeneralDentistry() {
        return "General Dentistry".equalsIgnoreCase(specializationName) ||
               "General".equalsIgnoreCase(specializationName);
    }

    @Override
    public String toString() {
        return specializationName;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Specialization specialization = (Specialization) obj;
        return specializationID == specialization.specializationID;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(specializationID);
    }
}
package Entity;

public class Insurance {
    private int insuranceID;
    private int patientID;
    private String providerName;
    private String policyNumber;

    // Constructors
    public Insurance() {}

    public Insurance(int insuranceID, int patientID, String providerName, String policyNumber) {
        this.insuranceID = insuranceID;
        this.patientID = patientID;
        this.providerName = providerName;
        this.policyNumber = policyNumber;
    }

    // Getters and Setters
    public int getInsuranceID() {
        return insuranceID;
    }

    public void setInsuranceID(int insuranceID) {
        this.insuranceID = insuranceID;
    }

    public int getPatientID() {
        return patientID;
    }

    public void setPatientID(int patientID) {
        this.patientID = patientID;
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public String getPolicyNumber() {
        return policyNumber;
    }

    public void setPolicyNumber(String policyNumber) {
        this.policyNumber = policyNumber;
    }

    @Override
    public String toString() {
        return providerName + " (Policy: " + policyNumber + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Insurance insurance = (Insurance) obj;
        return insuranceID == insurance.insuranceID;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(insuranceID);
    }
}
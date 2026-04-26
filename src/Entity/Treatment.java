package Entity;

import java.math.BigDecimal;
import java.util.Objects;

public class Treatment {

    private int treatmentID;
    private int treatmentPlanID;   
    private String treatmentType;
    private BigDecimal treatmentCost;
    private int staffID;           
    private String treatmentPhase;

    public Treatment() { }
   
	public Treatment(int treatmentID, int treatmentPlanID, String treatmentType, BigDecimal treatmentCost, int staffID,
			String treatmentPhase) {
		super();
		this.treatmentID = treatmentID;
		this.treatmentPlanID = treatmentPlanID;
		this.treatmentType = treatmentType;
		this.treatmentCost = treatmentCost;
		this.staffID = staffID;
		this.treatmentPhase = treatmentPhase;
	}


	public int getTreatmentID() {
		return treatmentID;
	}

	public void setTreatmentID(int treatmentID) {
		this.treatmentID = treatmentID;
	}

	public int getTreatmentPlanID() {
		return treatmentPlanID;
	}

	public void setTreatmentPlanID(int treatmentPlanID) {
		this.treatmentPlanID = treatmentPlanID;
	}

	public String getTreatmentType() {
		return treatmentType;
	}

	public void setTreatmentType(String treatmentType) {
		this.treatmentType = treatmentType;
	}

	public BigDecimal getTreatmentCost() {
		return treatmentCost;
	}

	public void setTreatmentCost(BigDecimal treatmentCost) {
		this.treatmentCost = treatmentCost;
	}

	public int getStaffID() {
		return staffID;
	}

	public void setStaffID(int staffID) {
		this.staffID = staffID;
	}

	public String getTreatmentPhase() {
		return treatmentPhase;
	}

	public void setTreatmentPhase(String treatmentPhase) {
		this.treatmentPhase = treatmentPhase;
	}

	@Override
	public int hashCode() {
		return Objects.hash(staffID, treatmentCost, treatmentID, treatmentPhase, treatmentPlanID, treatmentType);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Treatment other = (Treatment) obj;
		return staffID == other.staffID && Objects.equals(treatmentCost, other.treatmentCost)
				&& treatmentID == other.treatmentID && Objects.equals(treatmentPhase, other.treatmentPhase)
				&& treatmentPlanID == other.treatmentPlanID && Objects.equals(treatmentType, other.treatmentType);
	}

	@Override
	public String toString() {
		return "Treatment [treatmentID=" + treatmentID + ", treatmentPlanID=" + treatmentPlanID + ", treatmentType="
				+ treatmentType + ", treatmentCost=" + treatmentCost + ", staffID=" + staffID + ", treatmentPhase="
				+ treatmentPhase + "]";
	}


    
    }

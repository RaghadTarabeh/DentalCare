package Entity;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.Objects;

public class TreatmentPlan {

    private int treatmentPlanID;
    private int patientID;                 
    private Date startDate;
    private Date estimatedCompletionDate;
    private String status;
    private BigDecimal totalCost;

    public TreatmentPlan() { }


	public TreatmentPlan(int treatmentPlanID, int patientID, Date startDate, Date estimatedCompletionDate,
			String status, BigDecimal totalCost) {
		super();
		this.treatmentPlanID = treatmentPlanID;
		this.patientID = patientID;
		this.startDate = startDate;
		this.estimatedCompletionDate = estimatedCompletionDate;
		this.status = status;
		this.totalCost = totalCost;
	}



	public int getTreatmentPlanID() {
		return treatmentPlanID;
	}

	public void setTreatmentPlanID(int treatmentPlanID) {
		this.treatmentPlanID = treatmentPlanID;
	}

	public int getPatientID() {
		return patientID;
	}

	public void setPatientID(int patientID) {
		this.patientID = patientID;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEstimatedCompletionDate() {
		return estimatedCompletionDate;
	}

	public void setEstimatedCompletionDate(Date estimatedCompletionDate) {
		this.estimatedCompletionDate = estimatedCompletionDate;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public BigDecimal getTotalCost() {
		return totalCost;
	}

	public void setTotalCost(BigDecimal totalCost) {
		this.totalCost = totalCost;
	}

	@Override
	public int hashCode() {
		return Objects.hash(estimatedCompletionDate, patientID, startDate, status, totalCost, treatmentPlanID);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TreatmentPlan other = (TreatmentPlan) obj;
		return Objects.equals(estimatedCompletionDate, other.estimatedCompletionDate) && patientID == other.patientID
				&& Objects.equals(startDate, other.startDate) && Objects.equals(status, other.status)
				&& Objects.equals(totalCost, other.totalCost) && treatmentPlanID == other.treatmentPlanID;
	}

	@Override
	public String toString() {
		return "TreatmentPlan [treatmentPlanID=" + treatmentPlanID + ", patientID=" + patientID + ", startDate="
				+ startDate + ", estimatedCompletionDate=" + estimatedCompletionDate + ", status=" + status
				+ ", totalCost=" + totalCost + "]";
	}


	
}

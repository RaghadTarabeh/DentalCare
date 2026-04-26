package Entity;

import java.util.Objects;

public class InvoiceTreatment {

	
	private int invoiceID;
    private int treatmentID;

    public InvoiceTreatment() { }

	public InvoiceTreatment(int invoiceID, int treatmentID) {
		super();
		this.invoiceID = invoiceID;
		this.treatmentID = treatmentID;
	}

	public int getInvoiceID() {
		return invoiceID;
	}

	public void setInvoiceID(int invoiceID) {
		this.invoiceID = invoiceID;
	}

	public int getTreatmentID() {
		return treatmentID;
	}

	public void setTreatmentID(int treatmentID) {
		this.treatmentID = treatmentID;
	}

	@Override
	public int hashCode() {
		return Objects.hash(invoiceID, treatmentID);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		InvoiceTreatment other = (InvoiceTreatment) obj;
		return invoiceID == other.invoiceID && treatmentID == other.treatmentID;
	}

	@Override
	public String toString() {
		return "InvoiceTreatment [invoiceID=" + invoiceID + ", treatmentID=" + treatmentID + "]";
	}

    
}

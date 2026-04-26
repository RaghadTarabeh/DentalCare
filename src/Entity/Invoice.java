package Entity;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.Objects;

public class Invoice {
	
	
	private int invoiceID;
    private int patientID;           
    private Date invoiceDate;
    private BigDecimal totalAmount;
    private String paymentStatus;

    public Invoice() { }

	public Invoice(int invoiceID, int patientID, Date invoiceDate, BigDecimal totalAmount, String paymentStatus) {
		super();
		this.invoiceID = invoiceID;
		this.patientID = patientID;
		this.invoiceDate = invoiceDate;
		this.totalAmount = totalAmount;
		this.paymentStatus = paymentStatus;
	}

	public int getInvoiceID() {
		return invoiceID;
	}

	public void setInvoiceID(int invoiceID) {
		this.invoiceID = invoiceID;
	}

	public int getPatientID() {
		return patientID;
	}

	public void setPatientID(int patientID) {
		this.patientID = patientID;
	}

	public Date getInvoiceDate() {
		return invoiceDate;
	}

	public void setInvoiceDate(Date invoiceDate) {
		this.invoiceDate = invoiceDate;
	}

	public BigDecimal getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(BigDecimal totalAmount) {
		this.totalAmount = totalAmount;
	}

	public String getPaymentStatus() {
		return paymentStatus;
	}

	public void setPaymentStatus(String paymentStatus) {
		this.paymentStatus = paymentStatus;
	}

	@Override
	public int hashCode() {
		return Objects.hash(invoiceDate, invoiceID, patientID, paymentStatus, totalAmount);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Invoice other = (Invoice) obj;
		return Objects.equals(invoiceDate, other.invoiceDate) && invoiceID == other.invoiceID
				&& patientID == other.patientID && Objects.equals(paymentStatus, other.paymentStatus)
				&& Objects.equals(totalAmount, other.totalAmount);
	}

	@Override
	public String toString() {
		return "Invoice [invoiceID=" + invoiceID + ", patientID=" + patientID + ", invoiceDate=" + invoiceDate
				+ ", totalAmount=" + totalAmount + ", paymentStatus=" + paymentStatus + "]";
	}

    
    
    
}

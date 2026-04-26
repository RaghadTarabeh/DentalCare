package Entity;

import java.util.Objects;

public class Supplier {
	
	private int supplierID;
    private String supplierName;
    private String contactInformation;

    // קונסטרקטור ריק
    public Supplier() {
    }

    // קונסטרקטור מלא
    public Supplier(int supplierID, String supplierName, String contactInformation) {
        this.supplierID = supplierID;
        this.supplierName = supplierName;
        this.contactInformation = contactInformation;
    }

    // Getters and Setters
    public int getSupplierID() {
        return supplierID; 
    }

    public void setSupplierID(int supplierID) {
        this.supplierID = supplierID;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public String getContactInformation() {
        return contactInformation;
    }

    public void setContactInformation(String contactInformation) {
        this.contactInformation = contactInformation;
    }

    @Override
    public String toString() {
        return "Supplier{" +
                "supplierID=" + supplierID +
                ", supplierName='" + supplierName + '\'' +
                ", contactInformation='" + contactInformation + '\'' +
                '}';
    }

	@Override
	public int hashCode() {
		return Objects.hash(contactInformation, supplierID, supplierName);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Supplier other = (Supplier) obj;
		return Objects.equals(contactInformation, other.contactInformation) && supplierID == other.supplierID
				&& Objects.equals(supplierName, other.supplierName);
	}
    
    

}

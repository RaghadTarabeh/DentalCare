package Entity;

import java.util.Objects;

public class TreatmentInventoryUsage {

	private int treatmentID;
    private int inventoryItemID;
    private int quantityUsed;

    // קונסטרקטור ריק
    public TreatmentInventoryUsage() {
    }

    // קונסטרקטור מלא
    public TreatmentInventoryUsage(int treatmentID, int inventoryItemID, int quantityUsed) {
        this.treatmentID = treatmentID;
        this.inventoryItemID = inventoryItemID;
        this.quantityUsed = quantityUsed;
    }

    // Getters and Setters
    public int getTreatmentID() {
        return treatmentID;
    }

    public void setTreatmentID(int treatmentID) {
        this.treatmentID = treatmentID;
    }

    public int getInventoryItemID() {
        return inventoryItemID;
    }

    public void setInventoryItemID(int inventoryItemID) {
        this.inventoryItemID = inventoryItemID;
    }

    public int getQuantityUsed() {
        return quantityUsed;
    }

    public void setQuantityUsed(int quantityUsed) {
        this.quantityUsed = quantityUsed;
    }

    @Override
    public String toString() {
        return "TreatmentInventoryUsage{" +
                "treatmentID=" + treatmentID +
                ", inventoryItemID=" + inventoryItemID +
                ", quantityUsed=" + quantityUsed +
                '}';
    }

	@Override
	public int hashCode() {
		return Objects.hash(inventoryItemID, quantityUsed, treatmentID);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TreatmentInventoryUsage other = (TreatmentInventoryUsage) obj;
		return inventoryItemID == other.inventoryItemID && quantityUsed == other.quantityUsed
				&& treatmentID == other.treatmentID;
	}
    
    
}

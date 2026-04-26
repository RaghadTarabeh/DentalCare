package Entity;


import java.time.LocalDate;
import java.util.Objects;
 
  public class InventoryItem {

	private int inventoryItemID;
    private String itemName;
    private String description;
    private int categoryID;
    private int quantityInStock;
    private int supplierID;
    private LocalDate expirationDate;
    private String serialNumber;

    // קונסטרקטור ריק
    public InventoryItem() {
    }

    // קונסטרקטור מלא
    public InventoryItem(int inventoryItemID, String itemName, String description, int categoryID,
                         int quantityInStock, int supplierID, LocalDate expirationDate, String serialNumber) {
        this.inventoryItemID = inventoryItemID;
        this.itemName = itemName;
        this.description = description;
        this.categoryID = categoryID;
        this.quantityInStock = quantityInStock;
        this.supplierID = supplierID;
        this.expirationDate = expirationDate;
        this.serialNumber = serialNumber;
    }
    
    
    

    // Getters and Setters
    public int getInventoryItemID() {
        return inventoryItemID;
    }

    public void setInventoryItemID(int inventoryItemID) {
        this.inventoryItemID = inventoryItemID;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getCategoryID() {
        return categoryID;
    }

    public void setCategoryID(int categoryID) {
        this.categoryID = categoryID;
    }

    public int getQuantityInStock() {
        return quantityInStock;
    }

    public void setQuantityInStock(int quantityInStock) {
        this.quantityInStock = quantityInStock;
    }

    public int getSupplierID() {
        return supplierID;
    }

    public void setSupplierID(int supplierID) {
        this.supplierID = supplierID;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    @Override
    public String toString() {
        return "InventoryItem{" +
                "inventoryItemID=" + inventoryItemID +
                ", itemName='" + itemName + '\'' +
                ", description='" + description + '\'' +
                ", categoryID=" + categoryID +
                ", quantityInStock=" + quantityInStock +
                ", supplierID=" + supplierID +
                ", expirationDate=" + expirationDate +
                ", serialNumber='" + serialNumber + '\'' +
                '}';
    }

	@Override
	public int hashCode() {
		return Objects.hash(categoryID, description, expirationDate, inventoryItemID, itemName, quantityInStock,
				serialNumber, supplierID);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		InventoryItem other = (InventoryItem) obj;
		return categoryID == other.categoryID && Objects.equals(description, other.description)
				&& Objects.equals(expirationDate, other.expirationDate) && inventoryItemID == other.inventoryItemID
				&& Objects.equals(itemName, other.itemName) && quantityInStock == other.quantityInStock
				&& Objects.equals(serialNumber, other.serialNumber) && supplierID == other.supplierID;
	}
    
    
}

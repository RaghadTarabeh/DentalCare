package Entity;

import java.util.Objects;

public class InventoryCategory {

	    private int categoryID;
	    private String categoryName;

	    // קונסטרקטור ריק
	    public InventoryCategory() {
	    }

	    // קונסטרקטור מלא
	    public InventoryCategory(int categoryID, String categoryName) {
	        this.categoryID = categoryID;
	        this.categoryName = categoryName;
	    }

	    // Getters and Setters
	    public int getCategoryID() {
	        return categoryID;
	    }

	    public void setCategoryID(int categoryID) {
	        this.categoryID = categoryID;
	    }

	    public String getCategoryName() {
	        return categoryName;
	    }

	    public void setCategoryName(String categoryName) {
	        this.categoryName = categoryName;
	    }

	    @Override
	    public String toString() {
	        return "InventoryCategory{" +
	                "categoryID=" + categoryID +
	                ", categoryName='" + categoryName + '\'' +
	                '}';
	    }

		@Override
		public int hashCode() {
			return Objects.hash(categoryID, categoryName);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			InventoryCategory other = (InventoryCategory) obj;
			return categoryID == other.categoryID && Objects.equals(categoryName, other.categoryName);
		}
	    
	    
}

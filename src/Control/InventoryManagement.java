package Control;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import Entity.InventoryItem;
import Entity.Consts;

public class InventoryManagement {

    // הוספת פריט מלאי
	 public static void addInventoryItem(
	            int inventoryItemID,
	            String itemName,
	            String description,
	            int categoryId,
	            int quantityInStock,
	            int supplierId,
	            java.sql.Date expirationDate,
	            String serialNumber
	    ) throws Exception {
	        // 1) בדיקה מקדימה: האם קיימת רשומה עם אותו inventoryItemID?
	        InventoryItem existing = getInventoryItemById(inventoryItemID);
	        if (existing != null) {
	            // אם כן – נזרוק Exception עם הודעה באנגלית
	            throw new Exception("Cannot add item: an item with this ID already exists.");
	        }

	        // 2) אם לא קיימת כפילות, ממשיכים להכניס למסד הנתונים
	        try {
	            Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
	            try (Connection conn = DriverManager.getConnection(Consts.CONN_STR)) {
	                // קוראים ל־Stored Procedure Add_Inventory
	                CallableStatement cs = conn.prepareCall(Consts.SQL_ADD_INVENTORY);
	                cs.setInt(1, inventoryItemID);
	                cs.setString(2, itemName);
	                cs.setString(3, description);
	                cs.setInt(4, categoryId);
	                cs.setInt(5, quantityInStock);
	                cs.setInt(6, supplierId);
	                cs.setDate(7, expirationDate);
	                cs.setString(8, serialNumber);
	                cs.executeUpdate();
	                cs.close();
	            }
	        } catch (SQLException sqlEx) {
	            // אם אירעה שגיאת JDBC אחרת, נארוז אותה ב־Exception כללי
	            throw new Exception("Error adding inventory item to database: " + sqlEx.getMessage(), sqlEx);
	        } catch (ClassNotFoundException cnf) {
	            throw new Exception("UCanAccess driver not found: " + cnf.getMessage(), cnf);
	        }
	    }

    // עדכון פריט מלאי
    public static void updateInventoryItem(int itemId, String itemName, String description, int categoryId,
                                           int quantityInStock, int supplierId, java.sql.Date expirationDate, String serialNumber) {
        try {
            Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
            try (Connection conn = DriverManager.getConnection(Consts.CONN_STR)) {
                String sql = "UPDATE [Inventory Item] SET ItemName = ?, Description = ?, CategoryID = ?, QuantityInStock = ?, SupplierID = ?, ExpirationDate = ?, SerialNumber = ? WHERE InventoryItemID = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, itemName);
                stmt.setString(2, description);
                stmt.setInt(3, categoryId);
                stmt.setInt(4, quantityInStock);
                stmt.setInt(5, supplierId);
                stmt.setDate(6, expirationDate);
                stmt.setString(7, serialNumber);
                stmt.setInt(8, itemId);
                stmt.executeUpdate();
                System.out.println("Inventory item updated successfully.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // מחיקת פריט מלאי
    public static void removeInventoryItem(int itemId) {
        try {
            Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
            try (Connection conn = DriverManager.getConnection(Consts.CONN_STR)) {
                String sql = "DELETE FROM [Inventory Item] WHERE InventoryItemID = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, itemId);
                stmt.executeUpdate();
                System.out.println("Inventory item removed successfully.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    //החזרת הרשימה של כל פריטי המלאי
    public static List<InventoryItem> getAllInventoryItems() {
        List<InventoryItem> items = new ArrayList<>();

        try {
            Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
            try (Connection conn = DriverManager.getConnection(Consts.CONN_STR)) {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT * FROM [Inventory Item]");

                while (rs.next()) {
                    int id = rs.getInt("InventoryItemID");
                    String name = rs.getString("ItemName");
                    String desc = rs.getString("Description");
                    int categoryID = rs.getInt("CategoryID");
                    int qty = rs.getInt("QuantityInStock");
                    int supplierID = rs.getInt("SupplierID");
                    String serial = rs.getString("SerialNumber");

                    // טיפול ב־null בתאריך
                    java.sql.Date sqlDate = rs.getDate("ExpirationDate");
                    LocalDate expirationDate = (sqlDate != null) ? sqlDate.toLocalDate() : null;

                    // צור את האובייקט והוסף לרשימה
                    InventoryItem item = new InventoryItem(id, name, desc, categoryID, qty, supplierID, expirationDate, serial);
                    items.add(item);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return items;
    }

    
    
 // חיפוש פריט מלאי לפי ID
    public static Entity.InventoryItem getInventoryItemById(int itemId) {
        Entity.InventoryItem item = null;
        try {
            Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
            try (Connection conn = DriverManager.getConnection(Consts.CONN_STR)) {
                PreparedStatement stmt = conn.prepareStatement(Consts.SQL_FIND_BY_ID_INVENTORY);
                stmt.setInt(1, itemId);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    java.sql.Date sqlDate = rs.getDate("ExpirationDate");
                    LocalDate expirationDate = (sqlDate != null) ? sqlDate.toLocalDate() : null;

                    item = new Entity.InventoryItem(
                        rs.getInt("InventoryItemID"),
                        rs.getString("ItemName"),
                        rs.getString("Description"),
                        rs.getInt("CategoryID"),
                        rs.getInt("QuantityInStock"),
                        rs.getInt("SupplierID"),
                        expirationDate,
                        rs.getString("SerialNumber")
                    );
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return item;
    }

    public static boolean importInventoryFromXML(String absolutePath) {
        try {
            // Parse XML into InventoryItem objects
            List<InventoryItem> items = XMLImporter.loadInventoryItemsFromXML(absolutePath);
            if (items == null || items.isEmpty()) {
                return true;
            }

            // Load UCanAccess driver and open connection
            Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
            try (Connection conn = DriverManager.getConnection(Consts.CONN_STR)) {
                // For each InventoryItem, call the Add_Inventory stored procedure
                for (InventoryItem item : items) {
                    try (CallableStatement cs = conn.prepareCall(Consts.SQL_ADD_INVENTORY)) {
                        cs.setInt(1, item.getInventoryItemID());
                        cs.setString(2, item.getItemName());
                        cs.setString(3, item.getDescription());
                        cs.setInt(4, item.getCategoryID());
                        cs.setInt(5, item.getQuantityInStock());
                        cs.setInt(6, item.getSupplierID());

                        java.sql.Date sqlExpiration = null;
                        if (item.getExpirationDate() != null) {
                            sqlExpiration = java.sql.Date.valueOf(item.getExpirationDate());
                        }
                        cs.setDate(7, sqlExpiration);
                        cs.setString(8, item.getSerialNumber());

                        cs.executeUpdate();
                    } catch (Exception ignored) {
                        // Skip any row that fails without printing
                    }
                }
            }

            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    
    


}

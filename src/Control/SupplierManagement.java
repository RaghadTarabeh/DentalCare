package Control;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import Entity.Consts;

public class SupplierManagement {

	//הוספת ספק
	public static void addSupplier(int id, String name, String contactInfo) throws Exception {
      Entity.Supplier existing = getSupplierById(id);
        if (existing != null) {
            throw new Exception("Cannot add supplier: a supplier with this ID already exists.");
        }
           try {
            Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
            try (Connection conn = DriverManager.getConnection(Consts.CONN_STR)) {
                CallableStatement cs = conn.prepareCall(Consts.SQL_ADD_SUPPLIER);
                cs.setInt(1, id);
                cs.setString(2, name);
                cs.setString(3, contactInfo);
                cs.executeUpdate();
                cs.close();
            }
        } catch (SQLException sqlEx) {
            throw new Exception("Error adding supplier to database: " + sqlEx.getMessage(), sqlEx);
        } catch (ClassNotFoundException cnf) {
            throw new Exception("UCanAccess driver not found: " + cnf.getMessage(), cnf);
        }
    }


    //מחיקת ספק
    public static void removeSupplier(int supplierId) {
        try {
            Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
            try (Connection conn = DriverManager.getConnection(Consts.CONN_STR)) {
                String sql = "DELETE FROM Supplier WHERE SupplierID = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, supplierId);
                stmt.executeUpdate();
                System.out.println("Supplier removed successfully.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //עדכון ספק
    public static void updateSupplier(int supplierId, String newName, String newContactInfo) {
        try {
            Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
            try (Connection conn = DriverManager.getConnection(Consts.CONN_STR)) {
                String sql = "UPDATE Supplier SET SupplierName = ?, ContactInformation = ? WHERE SupplierID = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, newName);
                stmt.setString(2, newContactInfo);
                stmt.setInt(3, supplierId);
                stmt.executeUpdate();
                System.out.println("Supplier updated successfully.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    
    //החזרת הרשימה של כל הספקים
    public static List<Entity.Supplier> getAllSuppliers() {
        List<Entity.Supplier> suppliers = new ArrayList<>();

        try {
            Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
            try (Connection conn = DriverManager.getConnection(Consts.CONN_STR)) {
                String sql = "SELECT * FROM Supplier";
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    Entity.Supplier supplier = new Entity.Supplier(
                        rs.getInt("SupplierID"),
                        rs.getString("SupplierName"),
                        rs.getString("ContactInformation")
                    );
                    suppliers.add(supplier);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return suppliers;
    }
    
    
    // חיפוש ספק לפי ID
        public static Entity.Supplier getSupplierById(int supplierId) {
        Entity.Supplier supplier = null;

        try {
            Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
            try (Connection conn = DriverManager.getConnection(Consts.CONN_STR)) {
                String sql = "SELECT * FROM Supplier WHERE SupplierID = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, supplierId);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    supplier = new Entity.Supplier(
                        rs.getInt("SupplierID"),
                        rs.getString("SupplierName"),
                        rs.getString("ContactInformation")
                    );
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return supplier;
    }


        public static boolean importSuppliersFromXML(String absolutePath) {
            try {
                // Parse XML into Supplier objects
                List<Entity.Supplier> suppliers = XMLImporter.loadSuppliersFromXML(absolutePath);
                if (suppliers == null || suppliers.isEmpty()) {
                    return true;
                }

                // Load UCanAccess driver and open connection
                Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
                try (Connection conn = DriverManager.getConnection(Consts.CONN_STR)) {
                    // For each Supplier, call the Add_Supplier stored procedure
                    for (Entity.Supplier sup : suppliers) {
                        try (CallableStatement cs = conn.prepareCall(Consts.SQL_ADD_SUPPLIER)) {
                            cs.setInt(1, sup.getSupplierID());
                            cs.setString(2, sup.getSupplierName());
                            cs.setString(3, sup.getContactInformation());
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

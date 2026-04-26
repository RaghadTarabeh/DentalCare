package Entity;

import java.util.List;
import java.io.File;
import java.sql.*;
import java.util.ArrayList;

public class XMLData {
    private List<InventoryItem> inventoryItems;
    private List<Supplier> suppliers;

    public XMLData(List<InventoryItem> inventoryItems, List<Supplier> suppliers) {
        this.inventoryItems = inventoryItems;
        this.suppliers = suppliers;
    }

    public List<InventoryItem> getInventoryItems() {
        return inventoryItems;
    }

    public void setInventoryItems(List<InventoryItem> inventoryItems) {
        this.inventoryItems = inventoryItems;
    }

    public List<Supplier> getSuppliers() {
        return suppliers;
    }

    public void setSuppliers(List<Supplier> suppliers) {
        this.suppliers = suppliers;
    }

    public static List<Staff> getAllStaff() {
        List<Staff> staffList = new ArrayList<>();
        
        try {
            // Use relative path from project root (database is in project root)
            String dbPath = "ex1_database_2025_RT2.accdb";
            String url = "jdbc:ucanaccess://" + dbPath;
            
            // Debug information
            System.out.println("XMLData - Connecting to database...");
            System.out.println("XMLData - Database path: " + dbPath);
            
            File dbFile = new File(dbPath);
            System.out.println("XMLData - Absolute path: " + dbFile.getAbsolutePath());
            System.out.println("XMLData - File exists: " + dbFile.exists());
            
            if (!dbFile.exists()) {
                System.err.println("XMLData - Database file not found!");
                return staffList;
            }
            
            Connection conn = DriverManager.getConnection(url);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM Staff");
            
            while (rs.next()) {
                // Handle the boolean conversion for IsClinicManager
                String isClinicManagerStr = rs.getString("IsClinicManager");
                boolean isClinicManager = false;
                
                if (isClinicManagerStr != null) {
                    // Convert text to boolean
                    isClinicManager = isClinicManagerStr.equalsIgnoreCase("Yes") || 
                                    isClinicManagerStr.equalsIgnoreCase("True") || 
                                    isClinicManagerStr.equals("1");
                }
                
                Staff s = new Staff(
                    rs.getInt("StaffID"),
                    rs.getString("FirstName"),
                    rs.getString("LastName"),
                    rs.getString("PhoneNumber"),
                    rs.getString("EmailAddress"),
                    rs.getInt("RoleID"),
                    rs.getInt("SpecializationID"),
                    rs.getString("Qualification"),
                    rs.getString("ScheduleDetails"),
                    isClinicManager  // Use the converted boolean
                );
                staffList.add(s);
            }
            
            rs.close();
            stmt.close();
            conn.close();
            
            System.out.println("XMLData - Successfully loaded " + staffList.size() + " staff members");
            
        } catch (SQLException e) {
            System.err.println("XMLData - Database error: " + e.getMessage());
            e.printStackTrace();
        }
        
        return staffList;
    }
}
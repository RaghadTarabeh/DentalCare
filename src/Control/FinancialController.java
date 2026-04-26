package Control;

import Control.DatabaseConnection;
import Control.AppointmentController;
import java.sql.*;
import java.util.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class FinancialController {
    
    /**
     * Revenue data structure
     */
    public static class RevenueData {
        public String serviceName;
        public int quantity;
        public double rate;
        public double total;
        
        public RevenueData(String serviceName, int quantity, double rate, double total) {
            this.serviceName = serviceName;
            this.quantity = quantity;
            this.rate = rate;
            this.total = total;
        }
    }
    
    /**
     * Expense data structure
     */
    public static class ExpenseData {
        public String category;
        public String description;
        public double amount;
        public String date;
        
        public ExpenseData(String category, String description, double amount, String date) {
            this.category = category;
            this.description = description;
            this.amount = amount;
            this.date = date;
        }
    }
    
    /**
     * Get revenue data from appointments for specified period
     */
    public static List<RevenueData> getRevenueData(String period) {
        List<RevenueData> revenueList = new ArrayList<>();
        
        try {
            Connection conn = DatabaseConnection.getConnection();
            
            // Build date filter based on period
            String dateFilter = getDateFilter(period);
            
            // Query to get appointment data with visit reasons
            String query = "SELECT vr.ReasonName, COUNT(*) as Quantity, " +
                          "CASE " +
                          "  WHEN vr.ReasonName LIKE '%Cleaning%' THEN 350 " +
                          "  WHEN vr.ReasonName LIKE '%Filling%' OR vr.ReasonName LIKE '%Cavity%' THEN 650 " +
                          "  WHEN vr.ReasonName LIKE '%Root Canal%' THEN 1200 " +
                          "  WHEN vr.ReasonName LIKE '%Whitening%' THEN 800 " +
                          "  WHEN vr.ReasonName LIKE '%Crown%' THEN 2500 " +
                          "  WHEN vr.ReasonName LIKE '%Emergency%' THEN 450 " +
                          "  WHEN vr.ReasonName LIKE '%Checkup%' THEN 200 " +
                          "  ELSE 300 " +
                          "END as Rate " +
                          "FROM Appointment a " +
                          "INNER JOIN VisitReason vr ON a.VisitReasonID = vr.VisitReasonID " +
                          "WHERE a.AppointmentStatusID = 2 " + // Only completed appointments
                          dateFilter + 
                          "GROUP BY vr.ReasonName, " +
                          "CASE " +
                          "  WHEN vr.ReasonName LIKE '%Cleaning%' THEN 350 " +
                          "  WHEN vr.ReasonName LIKE '%Filling%' OR vr.ReasonName LIKE '%Cavity%' THEN 650 " +
                          "  WHEN vr.ReasonName LIKE '%Root Canal%' THEN 1200 " +
                          "  WHEN vr.ReasonName LIKE '%Whitening%' THEN 800 " +
                          "  WHEN vr.ReasonName LIKE '%Crown%' THEN 2500 " +
                          "  WHEN vr.ReasonName LIKE '%Emergency%' THEN 450 " +
                          "  WHEN vr.ReasonName LIKE '%Checkup%' THEN 200 " +
                          "  ELSE 300 " +
                          "END " +
                          "ORDER BY COUNT(*) DESC";
            
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                String serviceName = rs.getString("ReasonName");
                int quantity = rs.getInt("Quantity");
                double rate = rs.getDouble("Rate");
                double total = quantity * rate;
                
                revenueList.add(new RevenueData(serviceName, quantity, rate, total));
                System.out.println("Revenue: " + serviceName + " - Qty: " + quantity + " - Rate: " + rate + " - Total: " + total);
            }
            
            rs.close();
            stmt.close();
            conn.close();
            
            System.out.println("FinancialController - Loaded " + revenueList.size() + " revenue entries");
            
        } catch (SQLException e) {
            System.err.println("FinancialController - Error loading revenue data: " + e.getMessage());
            e.printStackTrace();
        }
        
        return revenueList;
    }
    
    /**
     * Get expense data from inventory and other sources
     */
    public static List<ExpenseData> getExpenseData(String period) {
        List<ExpenseData> expenseList = new ArrayList<>();
        
        try {
            Connection conn = DatabaseConnection.getConnection();
            
            // Build date filter based on period
            String dateFilter = getInventoryDateFilter(period);
            
            // Query inventory items as equipment expenses
            String inventoryQuery = "SELECT ii.ItemName, ii.QuantityInStock, " +
            	       "IIF(ii.ItemName LIKE '*Machine*' OR ii.ItemName LIKE '*Equipment*', ii.QuantityInStock * 5000, " +
            	       "IIF(ii.ItemName LIKE '*Instrument*' OR ii.ItemName LIKE '*Tool*', ii.QuantityInStock * 200, " +
            	       "IIF(ii.ItemName LIKE '*Supply*' OR ii.ItemName LIKE '*Material*', ii.QuantityInStock * 50, " +
            	       "ii.QuantityInStock * 100))) AS EstimatedCost " +
            	       "FROM [Inventory Item] ii " +
            	       "WHERE ii.QuantityInStock > 0 " +
            	       "ORDER BY EstimatedCost DESC";

            
            PreparedStatement stmt = conn.prepareStatement(inventoryQuery);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                String itemName = rs.getString("ItemName");
                double cost = rs.getDouble("EstimatedCost");
                String category = determineExpenseCategory(itemName);
                
                expenseList.add(new ExpenseData(category, itemName, cost, getCurrentDateString()));
                System.out.println("Expense: " + category + " - " + itemName + " - Cost: " + cost);
            }
            
            // Add fixed monthly expenses
            addFixedExpenses(expenseList, period);
            
            rs.close();
            stmt.close();
            conn.close();
            
            System.out.println("FinancialController - Loaded " + expenseList.size() + " expense entries");
            
        } catch (SQLException e) {
            System.err.println("FinancialController - Error loading expense data: " + e.getMessage());
            e.printStackTrace();
        }
        
        return expenseList;
    }
    
    /**
     * Add fixed monthly expenses (salaries, rent, utilities)
     */
    private static void addFixedExpenses(List<ExpenseData> expenseList, String period) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            
            // Get staff count for salary calculations
            String staffQuery = "SELECT COUNT(*) as StaffCount, RoleID FROM Staff GROUP BY RoleID";
            PreparedStatement stmt = conn.prepareStatement(staffQuery);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                int staffCount = rs.getInt("StaffCount");
                int roleID = rs.getInt("RoleID");
                
                double monthlySalary = 0;
                String roleDescription = "";
                
                switch (roleID) {
                    case 1: // Dentist
                        monthlySalary = 18000 * staffCount;
                        roleDescription = "Dentist Salaries (" + staffCount + " dentists)";
                        break;
                    case 2: // Hygienist
                        monthlySalary = 12000 * staffCount;
                        roleDescription = "Hygienist Salaries (" + staffCount + " hygienists)";
                        break;
                    case 3: // Secretary
                        monthlySalary = 10000 * staffCount;
                        roleDescription = "Secretary Salaries (" + staffCount + " secretaries)";
                        break;
                    case 4: // Clinic Manager
                        monthlySalary = 25000 * staffCount;
                        roleDescription = "Manager Salaries (" + staffCount + " managers)";
                        break;
                }
                
                if (monthlySalary > 0) {
                    expenseList.add(new ExpenseData("Staff", roleDescription, monthlySalary, getCurrentDateString()));
                }
            }
            
            // Add other fixed expenses
            expenseList.add(new ExpenseData("Rent", "Clinic Rent", 8500, getCurrentDateString()));
            expenseList.add(new ExpenseData("Utilities", "Electricity & Water", 1200, getCurrentDateString()));
            expenseList.add(new ExpenseData("Insurance", "Clinic Insurance", 1800, getCurrentDateString()));
            expenseList.add(new ExpenseData("Maintenance", "Equipment Maintenance", 800, getCurrentDateString()));
            
            rs.close();
            stmt.close();
            conn.close();
            
        } catch (SQLException e) {
            System.err.println("Error loading staff data for expenses: " + e.getMessage());
        }
    }
    
    /**
     * Determine expense category based on item name
     */
    private static String determineExpenseCategory(String itemName) {
        String lowerName = itemName.toLowerCase();
        
        if (lowerName.contains("machine") || lowerName.contains("equipment") || lowerName.contains("device")) {
            return "Equipment";
        } else if (lowerName.contains("instrument") || lowerName.contains("tool")) {
            return "Instruments";
        } else if (lowerName.contains("supply") || lowerName.contains("material") || lowerName.contains("consumable")) {
            return "Supplies";
        } else {
            return "Miscellaneous";
        }
    }
    
    /**
     * Build date filter for appointments based on period
     */
    private static String getDateFilter(String period) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate;
        
        switch (period) {
            case "This Month":
                startDate = endDate.withDayOfMonth(1);
                break;
            case "Last Month":
                startDate = endDate.minusMonths(1).withDayOfMonth(1);
                endDate = endDate.withDayOfMonth(1).minusDays(1);
                break;
            case "Last 3 Months":
                startDate = endDate.minusMonths(3);
                break;
            case "Last 6 Months":
                startDate = endDate.minusMonths(6);
                break;
            case "This Year":
                startDate = endDate.withDayOfYear(1);
                break;
            case "Last Year":
                startDate = endDate.minusYears(1).withDayOfYear(1);
                endDate = endDate.withDayOfYear(1).minusDays(1);
                break;
            default:
                startDate = endDate.minusMonths(1);
        }
        
        return "AND a.AppointmentDate >= #" + startDate + "# AND a.AppointmentDate <= #" + endDate + "# ";
    }
    
    /**
     * Build date filter for inventory (simplified - no date filtering for inventory)
     */
    private static String getInventoryDateFilter(String period) {
        // For inventory, we don't have date filtering, so return empty
        // In a real system, you'd have purchase dates or last updated dates
        return "";
    }
    
    /**
     * Get current date as formatted string
     */
    private static String getCurrentDateString() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }
}
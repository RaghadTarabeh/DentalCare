package Control;

import Entity.Patient;
import Control.DatabaseConnection;
import java.sql.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.text.NumberFormat;

public class BillingController {
    
    /**
     * Simple Invoice class for internal use
     */
    public static class InvoiceData {
        public String invoiceNumber;
        public String date;
        public String description;
        public double amount;
        public String status;
        public String dueDate;
        
        public InvoiceData(String invoiceNumber, String date, String description, 
                          double amount, String status, String dueDate) {
            this.invoiceNumber = invoiceNumber;
            this.date = date;
            this.description = description;
            this.amount = amount;
            this.status = status;
            this.dueDate = dueDate;
        }
    }
    
    /**
     * Get all invoices for a specific patient
     * @param patientID The patient ID
     * @return List of invoice data
     */
    public static List<InvoiceData> getPatientInvoices(int patientID) {
        List<InvoiceData> invoices = new ArrayList<>();
        
        try {
            Connection conn = DatabaseConnection.getConnection();
            String query = "SELECT InvoiceNumber, InvoiceDate, Description, TotalAmount, PaymentStatus, DueDate " +
                          "FROM Invoice WHERE PatientID = ? ORDER BY InvoiceDate DESC";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, patientID);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                InvoiceData invoice = new InvoiceData(
                    rs.getString("InvoiceNumber"),
                    rs.getDate("InvoiceDate").toString(),
                    rs.getString("Description"),
                    rs.getDouble("TotalAmount"),
                    rs.getString("PaymentStatus"),
                    rs.getDate("DueDate") != null ? rs.getDate("DueDate").toString() : "N/A"
                );
                invoices.add(invoice);
            }
            
            rs.close();
            stmt.close();
            conn.close();
            
            System.out.println("BillingController - Loaded " + invoices.size() + " invoices for patient " + patientID);
            
        } catch (SQLException e) {
            System.err.println("BillingController - Error loading patient invoices: " + e.getMessage());
            e.printStackTrace();
            
            // Return sample data if database fails
            return getSampleInvoices();
        }
        
        return invoices;
    }
    
    /**
     * Get sample invoice data for testing
     * @return List of sample invoices
     */
    public static List<InvoiceData> getSampleInvoices() {
        List<InvoiceData> invoices = new ArrayList<>();
        
        invoices.add(new InvoiceData("INV-2025-001", "2025-01-15", "Routine Cleaning & Checkup", 150.00, "Paid", "2025-02-15"));
        invoices.add(new InvoiceData("INV-2025-002", "2025-01-20", "Cavity Filling - Upper Molar", 320.00, "Pending", "2025-02-20"));
        invoices.add(new InvoiceData("INV-2024-045", "2024-12-10", "Orthodontic Treatment - Phase 1", 1200.00, "Paid", "2025-01-10"));
        invoices.add(new InvoiceData("INV-2024-046", "2024-12-15", "Dental X-Rays & Consultation", 85.00, "Paid", "2025-01-15"));
        invoices.add(new InvoiceData("INV-2025-003", "2025-01-05", "Emergency Treatment", 450.00, "Overdue", "2025-02-05"));
        invoices.add(new InvoiceData("INV-2024-040", "2024-11-20", "Cancelled Appointment Refund", -200.00, "Refunded", "N/A"));
        
        return invoices;
    }
    
    /**
     * Update payment status for an invoice
     * @param invoiceNumber The invoice number
     * @param newStatus The new payment status
     * @return true if update successful
     */
    public static boolean updatePaymentStatus(String invoiceNumber, String newStatus) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            String query = "UPDATE Invoice SET PaymentStatus = ?, PaymentDate = CURRENT_TIMESTAMP WHERE InvoiceNumber = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, newStatus);
            stmt.setString(2, invoiceNumber);
            
            int rowsAffected = stmt.executeUpdate();
            
            stmt.close();
            conn.close();
            
            if (rowsAffected > 0) {
                System.out.println("BillingController - Updated payment status for invoice " + invoiceNumber + " to " + newStatus);
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("BillingController - Error updating payment status: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Calculate billing summary for a patient
     * @param patientID The patient ID
     * @return Map containing billing summary
     */
    public static Map<String, Double> getBillingSummary(int patientID) {
        Map<String, Double> summary = new HashMap<>();
        summary.put("totalOwed", 0.0);
        summary.put("totalPaid", 0.0);
        summary.put("pendingRefunds", 0.0);
        
        try {
            Connection conn = DatabaseConnection.getConnection();
            
            // Get outstanding balance
            String owedQuery = "SELECT SUM(TotalAmount) FROM Invoice WHERE PatientID = ? AND PaymentStatus IN ('Pending', 'Overdue')";
            PreparedStatement owedStmt = conn.prepareStatement(owedQuery);
            owedStmt.setInt(1, patientID);
            ResultSet owedRs = owedStmt.executeQuery();
            
            if (owedRs.next()) {
                summary.put("totalOwed", owedRs.getDouble(1));
            }
            owedRs.close();
            owedStmt.close();
            
            // Get total paid
            String paidQuery = "SELECT SUM(TotalAmount) FROM Invoice WHERE PatientID = ? AND PaymentStatus = 'Paid'";
            PreparedStatement paidStmt = conn.prepareStatement(paidQuery);
            paidStmt.setInt(1, patientID);
            ResultSet paidRs = paidStmt.executeQuery();
            
            if (paidRs.next()) {
                summary.put("totalPaid", paidRs.getDouble(1));
            }
            paidRs.close();
            paidStmt.close();
            
            // Get pending refunds
            String refundQuery = "SELECT SUM(ABS(TotalAmount)) FROM Invoice WHERE PatientID = ? AND PaymentStatus = 'Refunded' AND TotalAmount < 0";
            PreparedStatement refundStmt = conn.prepareStatement(refundQuery);
            refundStmt.setInt(1, patientID);
            ResultSet refundRs = refundStmt.executeQuery();
            
            if (refundRs.next()) {
                summary.put("pendingRefunds", refundRs.getDouble(1));
            }
            refundRs.close();
            refundStmt.close();
            
            conn.close();
            
            System.out.println("BillingController - Calculated billing summary for patient " + patientID);
            
        } catch (SQLException e) {
            System.err.println("BillingController - Error calculating billing summary: " + e.getMessage());
            e.printStackTrace();
            
            // Return sample data if database fails
            summary.put("totalOwed", 770.0);
            summary.put("totalPaid", 1435.0);
            summary.put("pendingRefunds", 200.0);
        }
        
        return summary;
    }
    
    /**
     * Create a new invoice
     * @param patientID The patient ID
     * @param description Invoice description
     * @param amount Invoice amount
     * @param dueDate Due date for payment
     * @return Invoice number if successful, null if failed
     */
    public static String createInvoice(int patientID, String description, double amount, Date dueDate) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            
            // Generate invoice number
            String invoiceNumber = generateInvoiceNumber();
            
            String query = "INSERT INTO Invoice (InvoiceNumber, PatientID, InvoiceDate, Description, TotalAmount, PaymentStatus, DueDate) " +
                          "VALUES (?, ?, CURRENT_DATE, ?, ?, 'Pending', ?)";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, invoiceNumber);
            stmt.setInt(2, patientID);
            stmt.setString(3, description);
            stmt.setDouble(4, amount);
            stmt.setDate(5, dueDate);
            
            int rowsAffected = stmt.executeUpdate();
            
            stmt.close();
            conn.close();
            
            if (rowsAffected > 0) {
                System.out.println("BillingController - Created invoice " + invoiceNumber + " for patient " + patientID);
                return invoiceNumber;
            }
            
        } catch (SQLException e) {
            System.err.println("BillingController - Error creating invoice: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Generate a unique invoice number
     * @return Generated invoice number
     */
    private static String generateInvoiceNumber() {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        int year = cal.get(java.util.Calendar.YEAR);
        int month = cal.get(java.util.Calendar.MONTH) + 1;
        int day = cal.get(java.util.Calendar.DAY_OF_MONTH);
        int hour = cal.get(java.util.Calendar.HOUR_OF_DAY);
        int minute = cal.get(java.util.Calendar.MINUTE);
        
        return String.format("INV-%d-%02d%02d-%02d%02d", year, month, day, hour, minute);
    }
    
    /**
     * Process a payment for an invoice
     * @param invoiceNumber The invoice number
     * @param paymentAmount The payment amount
     * @param paymentMethod The payment method (e.g., "Credit Card", "Cash")
     * @return true if payment processed successfully
     */
    public static boolean processPayment(String invoiceNumber, double paymentAmount, String paymentMethod) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            
            // First, get the invoice details
            String getInvoiceQuery = "SELECT TotalAmount, PaymentStatus FROM Invoice WHERE InvoiceNumber = ?";
            PreparedStatement getStmt = conn.prepareStatement(getInvoiceQuery);
            getStmt.setString(1, invoiceNumber);
            ResultSet rs = getStmt.executeQuery();
            
            if (rs.next()) {
                double totalAmount = rs.getDouble("TotalAmount");
                String currentStatus = rs.getString("PaymentStatus");
                
                if ("Paid".equals(currentStatus)) {
                    rs.close();
                    getStmt.close();
                    conn.close();
                    return false; // Already paid
                }
                
                // Update payment status
                String newStatus = (paymentAmount >= totalAmount) ? "Paid" : "Partial";
                String updateQuery = "UPDATE Invoice SET PaymentStatus = ?, PaymentDate = CURRENT_TIMESTAMP, " +
                                   "PaymentMethod = ?, AmountPaid = ? WHERE InvoiceNumber = ?";
                PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
                updateStmt.setString(1, newStatus);
                updateStmt.setString(2, paymentMethod);
                updateStmt.setDouble(3, paymentAmount);
                updateStmt.setString(4, invoiceNumber);
                
                int rowsAffected = updateStmt.executeUpdate();
                
                updateStmt.close();
                rs.close();
                getStmt.close();
                conn.close();
                
                if (rowsAffected > 0) {
                    System.out.println("BillingController - Processed payment of $" + paymentAmount + 
                                     " for invoice " + invoiceNumber + " via " + paymentMethod);
                    return true;
                }
            }
            
            rs.close();
            getStmt.close();
            conn.close();
            
        } catch (SQLException e) {
            System.err.println("BillingController - Error processing payment: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Get payment history for a patient
     * @param patientID The patient ID
     * @return List of payment records
     */
    public static List<Map<String, Object>> getPaymentHistory(int patientID) {
        List<Map<String, Object>> payments = new ArrayList<>();
        
        try {
            Connection conn = DatabaseConnection.getConnection();
            String query = "SELECT InvoiceNumber, PaymentDate, AmountPaid, PaymentMethod, PaymentStatus " +
                          "FROM Invoice WHERE PatientID = ? AND PaymentStatus IN ('Paid', 'Partial', 'Refunded') " +
                          "ORDER BY PaymentDate DESC";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, patientID);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Map<String, Object> payment = new HashMap<>();
                payment.put("invoiceNumber", rs.getString("InvoiceNumber"));
                payment.put("paymentDate", rs.getTimestamp("PaymentDate"));
                payment.put("amount", rs.getDouble("AmountPaid"));
                payment.put("method", rs.getString("PaymentMethod"));
                payment.put("status", rs.getString("PaymentStatus"));
                payments.add(payment);
            }
            
            rs.close();
            stmt.close();
            conn.close();
            
            System.out.println("BillingController - Retrieved " + payments.size() + " payment records for patient " + patientID);
            
        } catch (SQLException e) {
            System.err.println("BillingController - Error retrieving payment history: " + e.getMessage());
            e.printStackTrace();
        }
        
        return payments;
    }
    
    /**
     * Check if patient has any overdue invoices
     * @param patientID The patient ID
     * @return true if patient has overdue invoices
     */
    public static boolean hasOverdueInvoices(int patientID) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            String query = "SELECT COUNT(*) FROM Invoice WHERE PatientID = ? AND PaymentStatus = 'Overdue'";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, patientID);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                boolean hasOverdue = rs.getInt(1) > 0;
                
                rs.close();
                stmt.close();
                conn.close();
                
                return hasOverdue;
            }
            
            rs.close();
            stmt.close();
            conn.close();
            
        } catch (SQLException e) {
            System.err.println("BillingController - Error checking overdue invoices: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Generate financial report for a patient
     * @param patientID The patient ID
     * @return String containing financial report
     */
    public static String generateFinancialReport(int patientID) {
        StringBuilder report = new StringBuilder();
        NumberFormat currency = NumberFormat.getCurrencyInstance();
        
        try {
            Map<String, Double> summary = getBillingSummary(patientID);
            List<InvoiceData> invoices = getPatientInvoices(patientID);
            
            report.append("FINANCIAL REPORT\n");
            report.append("================\n\n");
            report.append("Outstanding Balance: ").append(currency.format(summary.get("totalOwed"))).append("\n");
            report.append("Total Paid: ").append(currency.format(summary.get("totalPaid"))).append("\n");
            report.append("Pending Refunds: ").append(currency.format(summary.get("pendingRefunds"))).append("\n\n");
            
            report.append("INVOICE DETAILS:\n");
            report.append("================\n");
            
            for (InvoiceData invoice : invoices) {
                report.append(invoice.invoiceNumber).append(" - ")
                      .append(invoice.date).append(" - ")
                      .append(currency.format(invoice.amount)).append(" - ")
                      .append(invoice.status).append("\n");
            }
            
        } catch (Exception e) {
            report.append("Error generating financial report: ").append(e.getMessage());
        }
        
        return report.toString();
    }
}
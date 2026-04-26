package Control;

import Entity.Patient;
import Control.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PatientController {
    
    /**
     * Get patient by ID from the database
     * @param patientId The patient ID to search for
     * @return Patient object if found, null if not found
     */
    public static Patient getPatientById(int patientId) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            String query = "SELECT * FROM Patient WHERE PatientID = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, patientId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                Patient patient = new Patient(
                    rs.getInt("PatientID"),
                    rs.getString("FirstName"),
                    rs.getString("LastName"),
                    rs.getString("PhoneNumber"),
                    rs.getString("EmailAddress"),
                    rs.getInt("Age")
                );
                
                rs.close();
                stmt.close();
                conn.close();
                
                System.out.println("PatientController - Found patient: " + patient.getFullName());
                return patient;
            }
            
            rs.close();
            stmt.close();
            conn.close();
            
        } catch (SQLException e) {
            System.err.println("PatientController - Database error: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("PatientController - Patient not found with ID: " + patientId);
        return null;
    }
    
    /**
     * Get dental history for a patient (if the table exists)
     * @param patientId The patient ID
     * @return List of DentalHistoryInfo objects
     */
    public static List<DentalHistoryInfo> getPatientDentalHistory(int patientId) {
        List<DentalHistoryInfo> dentalHistoryList = new ArrayList<>();
        
        try {
            Connection conn = DatabaseConnection.getConnection();
            String query = "SELECT * FROM DentalHistory WHERE PatientID = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, patientId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                DentalHistoryInfo dentalHistory = new DentalHistoryInfo(
                    rs.getInt("DentalHistoryID"),
                    rs.getString("PastTreatments"),
                    rs.getString("XRays"),
                    rs.getInt("PatientID")
                );
                
                dentalHistoryList.add(dentalHistory);
            }
            
            rs.close();
            stmt.close();
            conn.close();
            
            System.out.println("PatientController - Found " + dentalHistoryList.size() + " dental history records for patient ID: " + patientId);
            
        } catch (SQLException e) {
            System.err.println("PatientController - Error getting dental history (table may not exist): " + e.getMessage());
        }
        
        return dentalHistoryList;
    }
    
    /**
     * Update patient information in the database
     * @param patient The patient object with updated information
     * @return true if update successful, false otherwise
     */
    public static boolean updatePatient(Patient patient) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            String query = "UPDATE Patient SET FirstName = ?, LastName = ?, PhoneNumber = ?, EmailAddress = ?, Age = ? WHERE PatientID = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            
            stmt.setString(1, patient.getFirstName());
            stmt.setString(2, patient.getLastName());
            stmt.setString(3, patient.getPhoneNumber());
            stmt.setString(4, patient.getEmailAddress());
            stmt.setInt(5, patient.getAge());
            stmt.setInt(6, patient.getPatientID());
            
            int rowsAffected = stmt.executeUpdate();
            
            stmt.close();
            conn.close();
            
            if (rowsAffected > 0) {
                System.out.println("PatientController - Successfully updated patient: " + patient.getFullName());
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("PatientController - Error updating patient: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Add a new patient to the database
     * @param patient The patient object to add
     * @return true if addition successful, false otherwise
     */
    public static boolean addPatient(Patient patient) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            String query = "INSERT INTO Patient (FirstName, LastName, PhoneNumber, EmailAddress, Age) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(query);
            
            stmt.setString(1, patient.getFirstName());
            stmt.setString(2, patient.getLastName());
            stmt.setString(3, patient.getPhoneNumber());
            stmt.setString(4, patient.getEmailAddress());
            stmt.setInt(5, patient.getAge());
            
            int rowsAffected = stmt.executeUpdate();
            
            stmt.close();
            conn.close();
            
            if (rowsAffected > 0) {
                System.out.println("PatientController - Successfully added patient: " + patient.getFullName());
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("PatientController - Error adding patient: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Check if a patient exists in the database
     * @param patientId The patient ID to check
     * @return true if patient exists, false otherwise
     */
    public static boolean patientExists(int patientId) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            String query = "SELECT COUNT(*) FROM Patient WHERE PatientID = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, patientId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                int count = rs.getInt(1);
                
                rs.close();
                stmt.close();
                conn.close();
                
                return count > 0;
            }
            
            rs.close();
            stmt.close();
            conn.close();
            
        } catch (SQLException e) {
            System.err.println("PatientController - Error checking patient existence: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    // Helper class for dental history (only if you have this table)
    public static class DentalHistoryInfo {
        private int dentalHistoryID;
        private String pastTreatments;
        private String xRays;
        private int patientID;
        
        public DentalHistoryInfo(int dentalHistoryID, String pastTreatments, String xRays, int patientID) {
            this.dentalHistoryID = dentalHistoryID;
            this.pastTreatments = pastTreatments;
            this.xRays = xRays;
            this.patientID = patientID;
        }
        
        // Getters
        public int getDentalHistoryID() { return dentalHistoryID; }
        public String getPastTreatments() { return pastTreatments; }
        public String getXRays() { return xRays; }
        public int getPatientID() { return patientID; }
        
        // Setters
        public void setPastTreatments(String pastTreatments) { this.pastTreatments = pastTreatments; }
        public void setXRays(String xRays) { this.xRays = xRays; }
    }
}
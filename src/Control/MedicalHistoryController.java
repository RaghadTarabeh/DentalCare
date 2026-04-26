package Control;

import Entity.Patient;
import Entity.MedicalHistory;
import Entity.DentalHistory;
import Control.DatabaseConnection;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class MedicalHistoryController {
    
    /**
     * Get complete medical history for a patient
     * @param patientID The patient ID
     * @return Map containing all medical history data
     */
    public static Map<String, String> getPatientMedicalHistory(int patientID) {
        Map<String, String> history = new HashMap<>();
        
        try {
            Connection conn = DatabaseConnection.getConnection();
            
            // Get medical history
            String medicalQuery = "SELECT * FROM MedicalHistory WHERE PatientID = ?";
            PreparedStatement medStmt = conn.prepareStatement(medicalQuery);
            medStmt.setInt(1, patientID);
            ResultSet medRs = medStmt.executeQuery();
            
            if (medRs.next()) {
                history.put("allergies", medRs.getString("Allergies") != null ? medRs.getString("Allergies") : "");
                history.put("preExistingConditions", medRs.getString("PreExistingCondition") != null ? medRs.getString("PreExistingCondition") : "");
                history.put("currentMedications", medRs.getString("CurrentMedications") != null ? medRs.getString("CurrentMedications") : "");
                history.put("lastUpdated", medRs.getTimestamp("LastUpdated") != null ? medRs.getTimestamp("LastUpdated").toString() : "");
            } else {
                // No medical history found, set empty values
                history.put("allergies", "");
                history.put("preExistingConditions", "");
                history.put("currentMedications", "");
                history.put("lastUpdated", "");
            }
            
            medRs.close();
            medStmt.close();
            
            // Get dental history
            String dentalQuery = "SELECT * FROM DentalHistory WHERE PatientID = ?";
            PreparedStatement denStmt = conn.prepareStatement(dentalQuery);
            denStmt.setInt(1, patientID);
            ResultSet denRs = denStmt.executeQuery();
            
            if (denRs.next()) {
                history.put("pastTreatments", denRs.getString("PastTreatments") != null ? denRs.getString("PastTreatments") : "");
                history.put("xRayHistory", denRs.getString("XRayHistory") != null ? denRs.getString("XRayHistory") : "");
                history.put("dentalNotes", denRs.getString("DentalNotes") != null ? denRs.getString("DentalNotes") : "");
            } else {
                // No dental history found, set empty values
                history.put("pastTreatments", "");
                history.put("xRayHistory", "");
                history.put("dentalNotes", "");
            }
            
            denRs.close();
            denStmt.close();
            conn.close();
            
            System.out.println("MedicalHistoryController - Loaded medical history for patient " + patientID);
            
        } catch (SQLException e) {
            System.err.println("MedicalHistoryController - Error loading medical history: " + e.getMessage());
            e.printStackTrace();
            
            // Return empty values on error
            history.put("allergies", "");
            history.put("preExistingConditions", "");
            history.put("currentMedications", "");
            history.put("pastTreatments", "");
            history.put("xRayHistory", "");
            history.put("dentalNotes", "");
            history.put("lastUpdated", "");
        }
        
        return history;
    }
    
    /**
     * Update medical history for a patient
     * @param patientID The patient ID
     * @param allergies Patient allergies
     * @param conditions Pre-existing medical conditions
     * @param medications Current medications
     * @return true if update successful
     */
    public static boolean updateMedicalHistory(int patientID, String allergies, String conditions, String medications) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            
            // Check if medical history exists
            String checkQuery = "SELECT COUNT(*) FROM MedicalHistory WHERE PatientID = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
            checkStmt.setInt(1, patientID);
            ResultSet checkRs = checkStmt.executeQuery();
            
            boolean exists = false;
            if (checkRs.next()) {
                exists = checkRs.getInt(1) > 0;
            }
            checkRs.close();
            checkStmt.close();
            
            String query;
            if (exists) {
                // Update existing record
                query = "UPDATE MedicalHistory SET Allergies = ?, PreExistingCondition = ?, CurrentMedications = ?, LastUpdated = CURRENT_TIMESTAMP WHERE PatientID = ?";
            } else {
                // Insert new record
                query = "INSERT INTO MedicalHistory (Allergies, PreExistingCondition, CurrentMedications, PatientID, LastUpdated) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)";
            }
            
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, allergies);
            stmt.setString(2, conditions);
            stmt.setString(3, medications);
            stmt.setInt(4, patientID);
            
            int rowsAffected = stmt.executeUpdate();
            
            stmt.close();
            conn.close();
            
            if (rowsAffected > 0) {
                System.out.println("MedicalHistoryController - Updated medical history for patient " + patientID);
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("MedicalHistoryController - Error updating medical history: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Update dental history for a patient
     * @param patientID The patient ID
     * @param pastTreatments Past dental treatments
     * @param xRayHistory X-ray history
     * @param dentalNotes Additional dental notes
     * @return true if update successful
     */
    public static boolean updateDentalHistory(int patientID, String pastTreatments, String xRayHistory, String dentalNotes) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            
            // Check if dental history exists
            String checkQuery = "SELECT COUNT(*) FROM DentalHistory WHERE PatientID = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
            checkStmt.setInt(1, patientID);
            ResultSet checkRs = checkStmt.executeQuery();
            
            boolean exists = false;
            if (checkRs.next()) {
                exists = checkRs.getInt(1) > 0;
            }
            checkRs.close();
            checkStmt.close();
            
            String query;
            if (exists) {
                // Update existing record
                query = "UPDATE DentalHistory SET PastTreatments = ?, XRayHistory = ?, DentalNotes = ? WHERE PatientID = ?";
            } else {
                // Insert new record
                query = "INSERT INTO DentalHistory (PastTreatments, XRayHistory, DentalNotes, PatientID) VALUES (?, ?, ?, ?)";
            }
            
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, pastTreatments);
            stmt.setString(2, xRayHistory);
            stmt.setString(3, dentalNotes);
            stmt.setInt(4, patientID);
            
            int rowsAffected = stmt.executeUpdate();
            
            stmt.close();
            conn.close();
            
            if (rowsAffected > 0) {
                System.out.println("MedicalHistoryController - Updated dental history for patient " + patientID);
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("MedicalHistoryController - Error updating dental history: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Update complete medical and dental history
     * @param patientID The patient ID
     * @param allergies Patient allergies
     * @param conditions Medical conditions
     * @param medications Current medications
     * @param pastTreatments Past dental treatments
     * @param xRayHistory X-ray history
     * @param dentalNotes Dental notes
     * @return true if all updates successful
     */
    public static boolean updateCompleteHistory(int patientID, String allergies, String conditions, 
                                              String medications, String pastTreatments, 
                                              String xRayHistory, String dentalNotes) {
        boolean medicalSuccess = updateMedicalHistory(patientID, allergies, conditions, medications);
        boolean dentalSuccess = updateDentalHistory(patientID, pastTreatments, xRayHistory, dentalNotes);
        
        return medicalSuccess && dentalSuccess;
    }
    
    /**
     * Check if patient has any known allergies
     * @param patientID The patient ID
     * @return true if patient has allergies listed
     */
    public static boolean hasAllergies(int patientID) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            String query = "SELECT Allergies FROM MedicalHistory WHERE PatientID = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, patientID);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                String allergies = rs.getString("Allergies");
                boolean hasAllergies = allergies != null && !allergies.trim().isEmpty() && 
                                     !allergies.trim().equalsIgnoreCase("none") && 
                                     !allergies.trim().equalsIgnoreCase("no known allergies");
                
                rs.close();
                stmt.close();
                conn.close();
                
                return hasAllergies;
            }
            
            rs.close();
            stmt.close();
            conn.close();
            
        } catch (SQLException e) {
            System.err.println("MedicalHistoryController - Error checking allergies: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Get patient's allergy information for alerts
     * @param patientID The patient ID
     * @return String containing allergy information
     */
    public static String getAllergyAlert(int patientID) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            String query = "SELECT Allergies FROM MedicalHistory WHERE PatientID = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, patientID);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                String allergies = rs.getString("Allergies");
                
                rs.close();
                stmt.close();
                conn.close();
                
                if (allergies != null && !allergies.trim().isEmpty() && 
                    !allergies.trim().equalsIgnoreCase("none")) {
                    return allergies;
                }
            }
            
            rs.close();
            stmt.close();
            conn.close();
            
        } catch (SQLException e) {
            System.err.println("MedicalHistoryController - Error getting allergy alert: " + e.getMessage());
            e.printStackTrace();
        }
        
        return "";
    }
    
    /**
     * Get last update timestamp for medical history
     * @param patientID The patient ID
     * @return Timestamp of last update
     */
    public static Timestamp getLastUpdateTime(int patientID) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            String query = "SELECT LastUpdated FROM MedicalHistory WHERE PatientID = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, patientID);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                Timestamp lastUpdated = rs.getTimestamp("LastUpdated");
                
                rs.close();
                stmt.close();
                conn.close();
                
                return lastUpdated;
            }
            
            rs.close();
            stmt.close();
            conn.close();
            
        } catch (SQLException e) {
            System.err.println("MedicalHistoryController - Error getting last update time: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
}
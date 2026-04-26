package Control;

import Entity.Appointment;
import Control.DatabaseConnection;
import java.sql.*;
import java.sql.Date;
import java.util.*;

public class AppointmentController {
    
    /**
     * Book a new appointment
     * @param appointment The appointment to book
     * @return true if successful, false otherwise
     */
    public static boolean bookAppointment(Appointment appointment) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            
            // First, get the next available AppointmentID
            int nextAppointmentID = getNextAppointmentID(conn);
            System.out.println("DEBUG - Using AppointmentID: " + nextAppointmentID);
            
            String query = "INSERT INTO Appointment (AppointmentID, PatientID, StaffID, AppointmentDate, AppointmentTime, VisitReasonID, AppointmentStatusID) VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(query);
            
            stmt.setInt(1, nextAppointmentID); // Set the AppointmentID
            stmt.setInt(2, appointment.getPatientID());
            stmt.setInt(3, appointment.getStaffID());
            stmt.setDate(4, appointment.getAppointmentDate());
            
            // Convert Time to String format for VARCHAR column
            String timeString = appointment.getAppointmentTime().toString();
            if (timeString.endsWith(":00")) {
                timeString = timeString.substring(0, 5); // Remove seconds (16:30:00 -> 16:30)
            }
            stmt.setString(5, timeString);
            
            stmt.setInt(6, appointment.getVisitReasonID());
            stmt.setInt(7, appointment.getAppointmentStatusID());
            
            System.out.println("DEBUG - Executing query: " + query);
            System.out.println("DEBUG - Parameters: ID=" + nextAppointmentID + ", PatientID=" + appointment.getPatientID() + 
                              ", StaffID=" + appointment.getStaffID() + ", Date=" + appointment.getAppointmentDate() + 
                              ", Time=" + timeString + ", ReasonID=" + appointment.getVisitReasonID() + 
                              ", StatusID=" + appointment.getAppointmentStatusID());
            
            int rowsAffected = stmt.executeUpdate();
            
            stmt.close();
            conn.close();
            
            if (rowsAffected > 0) {
                System.out.println("AppointmentController - Successfully booked appointment with ID: " + nextAppointmentID + " for patient ID: " + appointment.getPatientID());
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("AppointmentController - Error booking appointment: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Get the next available AppointmentID
     * @param conn Database connection
     * @return Next available ID
     */
    private static int getNextAppointmentID(Connection conn) {
        try {
            String query = "SELECT MAX(AppointmentID) FROM Appointment";
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            
            int maxId = 0;
            if (rs.next()) {
                maxId = rs.getInt(1);
            }
            
            rs.close();
            stmt.close();
            
            System.out.println("DEBUG - Max existing AppointmentID: " + maxId + ", returning: " + (maxId + 1));
            return maxId + 1; // Return next available ID
            
        } catch (SQLException e) {
            System.err.println("Error getting next AppointmentID: " + e.getMessage());
            return 1; // Default to 1 if error occurs
        }
    }
    
    /**
     * Get all appointments from the database
     * @return List of all appointments
     */
    public static List<Appointment> getAllAppointments() {
        List<Appointment> appointments = new ArrayList<>();
        
        try {
            Connection conn = DatabaseConnection.getConnection();
            String query = "SELECT * FROM Appointment ORDER BY AppointmentDate ASC, AppointmentTime ASC";
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                // Handle time as String since it's stored as VARCHAR in database
                String timeString = rs.getString("AppointmentTime");
                Time appointmentTime = null;
                
                try {
                    if (timeString != null && !timeString.trim().isEmpty()) {
                        if (timeString.length() == 5) {
                            timeString += ":00";
                        }
                        appointmentTime = Time.valueOf(timeString);
                    }
                } catch (IllegalArgumentException e) {
                    System.err.println("Invalid time format: " + timeString);
                    appointmentTime = Time.valueOf("09:00:00");
                }
                
                Appointment appointment = new Appointment(
                    rs.getInt("AppointmentID"),
                    rs.getInt("PatientID"),
                    rs.getInt("StaffID"),
                    rs.getDate("AppointmentDate"),
                    appointmentTime,
                    rs.getInt("VisitReasonID"),
                    rs.getInt("AppointmentStatusID")
                );
                appointments.add(appointment);
            }
            
            rs.close();
            stmt.close();
            conn.close();
            
            System.out.println("AppointmentController - Found " + appointments.size() + " total appointments");
            
        } catch (SQLException e) {
            System.err.println("AppointmentController - Error getting all appointments: " + e.getMessage());
            e.printStackTrace();
        }
        
        return appointments;
    }
    
    /**
     * Get patient name by ID
     * @param patientId The patient ID
     * @return Patient name as string
     */
    public static String getPatientNameById(int patientId) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            String query = "SELECT FirstName, LastName FROM Patient WHERE PatientID = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, patientId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                String name = rs.getString("FirstName") + " " + rs.getString("LastName");
                rs.close();
                stmt.close();
                conn.close();
                return name;
            }
            
            rs.close();
            stmt.close();
            conn.close();
            
        } catch (SQLException e) {
            System.err.println("AppointmentController - Error getting patient name: " + e.getMessage());
        }
        
        return "Unknown Patient";
    }
    
    /**
     * Get visit reason name by ID
     * @param visitReasonId The visit reason ID
     * @return Visit reason name
     */
    public static String getVisitReasonById(int visitReasonId) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            String query = "SELECT ReasonName FROM VisitReason WHERE VisitReasonID = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, visitReasonId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                String reasonName = rs.getString("ReasonName");
                rs.close();
                stmt.close();
                conn.close();
                return reasonName;
            }
            
            rs.close();
            stmt.close();
            conn.close();
            
        } catch (SQLException e) {
            System.err.println("AppointmentController - Error getting visit reason: " + e.getMessage());
        }
        
        return "Unknown Reason";
    }
    
    /**
     * Get status name by ID
     * @param statusId The status ID
     * @return Status name
     */
    public static String getStatusNameById(int statusId) {
        // Map status IDs to names
        switch (statusId) {
            case 1: return "Scheduled";
            case 2: return "Completed";
            case 3: return "Cancelled";
            case 4: return "War-time suspended";
            case 5: return "Urgent/Pending";
            default: return "Unknown Status";
        }
    }
    
    /**
     * Mark an appointment as completed
     * @param appointmentId The appointment ID to mark as completed
     * @return true if successful, false otherwise
     */
    public static boolean markAppointmentCompleted(int appointmentId) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            String query = "UPDATE Appointment SET AppointmentStatusID = 2 WHERE AppointmentID = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, appointmentId);
            
            int rowsAffected = stmt.executeUpdate();
            
            stmt.close();
            conn.close();
            
            if (rowsAffected > 0) {
                System.out.println("AppointmentController - Successfully marked appointment as completed: " + appointmentId);
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("AppointmentController - Error marking appointment as completed: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Get all appointments for a specific patient
     * @param patientId The patient ID
     * @return List of patient's appointments
     */
    public static List<Appointment> getPatientAppointments(int patientId) {
        List<Appointment> appointments = new ArrayList<>();
        
        try {
            Connection conn = DatabaseConnection.getConnection();
            String query = "SELECT * FROM Appointment WHERE PatientID = ? ORDER BY AppointmentDate DESC, AppointmentTime DESC";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, patientId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                // Handle time as String since it's stored as VARCHAR in database
                String timeString = rs.getString("AppointmentTime");
                Time appointmentTime = null;
                
                try {
                    if (timeString != null && !timeString.trim().isEmpty()) {
                        if (timeString.length() == 5) {
                            timeString += ":00";
                        }
                        appointmentTime = Time.valueOf(timeString);
                    }
                } catch (IllegalArgumentException e) {
                    System.err.println("Invalid time format: " + timeString);
                    appointmentTime = Time.valueOf("09:00:00");
                }
                
                Appointment appointment = new Appointment(
                    rs.getInt("AppointmentID"),
                    rs.getInt("PatientID"),
                    rs.getInt("StaffID"),
                    rs.getDate("AppointmentDate"),
                    appointmentTime,
                    rs.getInt("VisitReasonID"),
                    rs.getInt("AppointmentStatusID")
                );
                appointments.add(appointment);
            }
            
            rs.close();
            stmt.close();
            conn.close();
            
            System.out.println("AppointmentController - Found " + appointments.size() + " appointments for patient " + patientId);
            
        } catch (SQLException e) {
            System.err.println("AppointmentController - Error getting patient appointments: " + e.getMessage());
            e.printStackTrace();
        }
        
        return appointments;
    }
    
    /**
     * Get staff name by ID
     * @param staffId The staff ID
     * @return Staff name as string
     */
    public static String getStaffNameById(int staffId) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            String query = "SELECT FirstName, LastName FROM Staff WHERE StaffID = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, staffId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                String name = "Dr. " + rs.getString("FirstName") + " " + rs.getString("LastName");
                rs.close();
                stmt.close();
                conn.close();
                return name;
            }
            
            rs.close();
            stmt.close();
            conn.close();
            
        } catch (SQLException e) {
            System.err.println("AppointmentController - Error getting staff name: " + e.getMessage());
        }
        
        return "Unknown Staff";
    }
    
    /**
     * Cancel an appointment
     * @param appointmentId The appointment ID to cancel
     * @return true if successful, false otherwise
     */
    public static boolean cancelAppointment(int appointmentId) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            String query = "UPDATE Appointment SET AppointmentStatusID = 3 WHERE AppointmentID = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, appointmentId);
            
            int rowsAffected = stmt.executeUpdate();
            
            stmt.close();
            conn.close();
            
            if (rowsAffected > 0) {
                System.out.println("AppointmentController - Successfully cancelled appointment: " + appointmentId);
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("AppointmentController - Error cancelling appointment: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Reschedule an appointment
     * @param appointmentId The appointment ID to reschedule
     * @param newDate The new appointment date
     * @param newTime The new appointment time
     * @return true if successful, false otherwise
     */
    public static boolean rescheduleAppointment(int appointmentId, Date newDate, Time newTime) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            
            // Convert Time to String format for VARCHAR column
            String timeString = newTime.toString();
            if (timeString.endsWith(":00")) {
                timeString = timeString.substring(0, 5); // Remove seconds
            }
            
            String query = "UPDATE Appointment SET AppointmentDate = ?, AppointmentTime = ? WHERE AppointmentID = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setDate(1, newDate);
            stmt.setString(2, timeString);
            stmt.setInt(3, appointmentId);
            
            int rowsAffected = stmt.executeUpdate();
            
            stmt.close();
            conn.close();
            
            if (rowsAffected > 0) {
                System.out.println("AppointmentController - Successfully rescheduled appointment: " + appointmentId);
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("AppointmentController - Error rescheduling appointment: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Suspend an appointment (war-time suspended)
     * @param appointmentId The appointment ID to suspend
     * @return true if successful, false otherwise
     */
    public static boolean suspendAppointment(int appointmentId) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            String query = "UPDATE Appointment SET AppointmentStatusID = 4 WHERE AppointmentID = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, appointmentId);
            
            int rowsAffected = stmt.executeUpdate();
            
            stmt.close();
            conn.close();
            
            if (rowsAffected > 0) {
                System.out.println("AppointmentController - Successfully suspended appointment: " + appointmentId);
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("AppointmentController - Error suspending appointment: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Approve an appointment (change from pending to scheduled)
     * @param appointmentId The appointment ID to approve
     * @return true if successful, false otherwise
     */
    public static boolean approveAppointment(int appointmentId) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            String query = "UPDATE Appointment SET AppointmentStatusID = 1 WHERE AppointmentID = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, appointmentId);
            
            int rowsAffected = stmt.executeUpdate();
            
            stmt.close();
            conn.close();
            
            if (rowsAffected > 0) {
                System.out.println("AppointmentController - Successfully approved appointment: " + appointmentId);
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("AppointmentController - Error approving appointment: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
}
package Control;

import Entity.Staff;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRTableModelDataSource;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.view.JasperViewer;

import javax.swing.*;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

/**
 * TPReportLauncher – מחלקה להפעלת דוח טיפולים ב־JasperReports.
 */
public class TPReportLauncher {

    // פונקציה סטטית להפעלת דוח טיפולים לרופא שיניים לפי ה־ID
	public static void generateReport(int dentistID) {
	    InputStream stream = TPReportLauncher.class.getResourceAsStream("/Reports/TPReport.jasper");
	    if (stream == null) {
	        throw new RuntimeException("Report file not found: /Reports/TPReport.jasper");
	    }

	    try (Connection conn = DatabaseConnection.getConnection()) {
	        // ❌ טעות:
	        // Map<String, Object> params = Map.of("DentistID", dentistID);

	        // ✅ תיקון:
	        Map<String, Object> params = new HashMap<>();
	        params.put("DentistID", dentistID);

	        JasperReport report = (JasperReport) JRLoader.loadObject(stream);
	        JasperPrint print = JasperFillManager.fillReport(report, params, conn);

	        JasperViewer viewer = new JasperViewer(print, false);
	        viewer.setZoomRatio(0.75f);
	        viewer.setVisible(true);

	    } catch (Exception e) {
	        e.printStackTrace();
	        JOptionPane.showMessageDialog(
	            null,
	            "שגיאה בהרצת הדו\"ח:\n" + e.toString(),
	            "Error", JOptionPane.ERROR_MESSAGE
	        );
	    }
	}


    // מחזיר את כל הרופאים (Staff עם RoleID = 1)
    public static List<Staff> getAllDentists() {
        final String sql = """
                SELECT StaffID, FirstName, LastName,
                       PhoneNumber, EmailAddress,
                       RoleID, SpecializationID,
                       Qualification, ScheduleDetails,
                       IsClinicManager
                FROM Staff
                WHERE RoleID = 1
                ORDER BY StaffID
                """;

        List<Staff> dentists = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                dentists.add(
                    new Staff(
                        rs.getInt("StaffID"),
                        rs.getString("FirstName"),
                        rs.getString("LastName"),
                        rs.getString("PhoneNumber"),
                        rs.getString("EmailAddress"),
                        rs.getInt("RoleID"),
                        rs.getInt("SpecializationID"),
                        rs.getString("Qualification"),
                        rs.getString("ScheduleDetails"),
                        "Yes".equalsIgnoreCase(rs.getString("IsClinicManager"))
                    )
                );
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return dentists;
    }
    
    
    /**
     * Generate treatment progress report for a specific dentist using JasperReports
     * @param selectedDentist The dentist to generate the report for
     */
    public static void generateTreatmentProgressReport(Staff selectedDentist) {
        try {
            System.out.println("Generating JasperReport for: Dr. " + 
                             selectedDentist.getFirstName() + " " + selectedDentist.getLastName());
            
            // Get database connection
            Connection conn = DatabaseConnection.getConnection();
            
            // Load the compiled JasperReport file (.jasper)
            InputStream reportStream = TPReportLauncher.class.getClassLoader()
                .getResourceAsStream("Reports/TPReport.jasper");
            
            if (reportStream == null) {
                throw new RuntimeException("Report file 'TPReport.jasper' not found in resources/Reports/");
            }
            
            // Create parameters map
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("DentistID", selectedDentist.getStaffID());
            parameters.put("DentistName", "Dr. " + selectedDentist.getFirstName() + " " + selectedDentist.getLastName());
            
            // Fill the report with data
            JasperPrint jasperPrint = JasperFillManager.fillReport(reportStream, parameters, conn);
            
            // Display the report in JasperViewer
            JasperViewer viewer = new JasperViewer(jasperPrint, false);
            viewer.setTitle("Treatment Progress Report - Dr. " + selectedDentist.getFirstName() + " " + selectedDentist.getLastName());
            viewer.setVisible(true);
            
            // Close resources
            conn.close();
            reportStream.close();
            
            System.out.println("Report displayed successfully!");
            
        } catch (Exception e) {
            System.err.println("TPReportLauncher - Error generating JasperReport: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to generate treatment progress report", e);
        }
    }

    /**
     * Export report to PDF file
     */
    public static void exportReportToPDF(Staff selectedDentist, String outputPath) {
        try {
            System.out.println("Exporting report to PDF for: Dr. " + 
                             selectedDentist.getFirstName() + " " + selectedDentist.getLastName());
            
            Connection conn = DatabaseConnection.getConnection();
            
            InputStream reportStream = TPReportLauncher.class.getClassLoader()
                .getResourceAsStream("Reports/TPReport.jasper");
            
            if (reportStream == null) {
                throw new RuntimeException("Report file not found!");
            }
            
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("DentistID", selectedDentist.getStaffID());
            parameters.put("DentistName", "Dr. " + selectedDentist.getFirstName() + " " + selectedDentist.getLastName());
            
            JasperPrint jasperPrint = JasperFillManager.fillReport(reportStream, parameters, conn);
            
            // Export to PDF
            JasperExportManager.exportReportToPdfFile(jasperPrint, outputPath);
            
            conn.close();
            reportStream.close();
            
            System.out.println("Report exported to: " + outputPath);
            
        } catch (Exception e) {
            System.err.println("Error exporting report to PDF: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    
   
    
    
}

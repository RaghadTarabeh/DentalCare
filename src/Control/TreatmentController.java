package Control;

import Entity.TreatmentPlan;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TreatmentController {

    public static List<TreatmentPlan> getPatientTreatmentPlans(int patientID) {
        List<TreatmentPlan> plans = new ArrayList<>();
        String query = "SELECT * FROM TreatmentPlan WHERE PatientID = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, patientID);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                TreatmentPlan plan = new TreatmentPlan();
                plan.setTreatmentPlanID(rs.getInt("TreatmentPlanID"));
                plan.setPatientID(rs.getInt("PatientID"));

                Date start = rs.getDate("StartDate");
                Date end = rs.getDate("EstimatedCompletionDate");

                plan.setStartDate(start != null ? start : null);
                plan.setEstimatedCompletionDate(end != null ? end : null);

                plan.setStatus(rs.getString("Status"));
                plan.setTotalCost(BigDecimal.valueOf(rs.getDouble("TotalCost")));


                plans.add(plan);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return plans;
    }
}

package Entity;

public class VisitReason {
    private int visitReasonID;
    private String reasonName;

    // Constructors
    public VisitReason() {}

    public VisitReason(int visitReasonID, String reasonName) {
        this.visitReasonID = visitReasonID;
        this.reasonName = reasonName;
    }

    // Getters and Setters
    public int getVisitReasonID() {
        return visitReasonID;
    }

    public void setVisitReasonID(int visitReasonID) {
        this.visitReasonID = visitReasonID;
    }

    public String getReasonName() {
        return reasonName;
    }
    

    public void setReasonName(String reasonName) {
        this.reasonName = reasonName;
    }

    // Helper methods based on your actual data
    public boolean isCavityFilling() {
        return visitReasonID == 1 || "Cavity Filling".equalsIgnoreCase(reasonName);
    }

    public boolean isRoutineCleaning() {
        return visitReasonID == 2 || "Routine Cleaning".equalsIgnoreCase(reasonName);
    }

    // Determine if this is an urgent visit (you can customize this logic)
    public boolean isUrgent() {
        // Cavity filling is typically more urgent than routine cleaning
        return isCavityFilling() || reasonName.toLowerCase().contains("urgent") || 
               reasonName.toLowerCase().contains("emergency") || reasonName.toLowerCase().contains("pain");
    }

    // Determine if this is a routine visit
    public boolean isRoutine() {
        return isRoutineCleaning() || reasonName.toLowerCase().contains("routine") ||
               reasonName.toLowerCase().contains("cleaning") || reasonName.toLowerCase().contains("checkup");
    }

    @Override
    public String toString() {
        return reasonName + (isUrgent() ? " (URGENT)" : "");
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        VisitReason reason = (VisitReason) obj;
        return visitReasonID == reason.visitReasonID;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(visitReasonID);
    }
    
}
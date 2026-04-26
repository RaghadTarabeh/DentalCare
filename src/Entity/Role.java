package Entity;

public class Role {
    private int roleID;
    private String roleName;

    // Constructors
    public Role() {}

    public Role(int roleID, String roleName) {
        this.roleID = roleID;
        this.roleName = roleName;
    }

    // Getters and Setters
    public int getRoleID() {
        return roleID;
    }

    public void setRoleID(int roleID) {
        this.roleID = roleID;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    // Helper methods
    public boolean isDentist() {
        return roleID == 1 || "Dentist".equalsIgnoreCase(roleName);
    }

    public boolean isHygienist() {
        return roleID == 2 || "Hygienist".equalsIgnoreCase(roleName);
    }

    public boolean isSecretary() {
        return roleID == 3 || "Secretary".equalsIgnoreCase(roleName);
    }

    public boolean isClinicManager() {
        return roleID == 4 || "Clinic Manager".equalsIgnoreCase(roleName);
    }

    @Override
    public String toString() {
        return roleName;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Role role = (Role) obj;
        return roleID == role.roleID;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(roleID);
    }
}
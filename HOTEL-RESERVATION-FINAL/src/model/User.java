package model;

/**
 * A guest or admin account.
 * Line format in data/users.txt:
 * fullName|email|username|password|role|status|blockReason
 * (older 4-field lines still load: they become ACTIVE guests)
 */
public class User {
    public static final String GUEST = "GUEST";
    public static final String ADMIN = "ADMIN";

    private String fullName;
    private String email;
    private String username;
    private String password;
    private String role;
    private boolean blocked;
    private String blockReason;

    // normal sign-up: an active guest
    public User(String fullName, String email, String username, String password) {
        this(fullName, email, username, password, GUEST, false, "");
    }

    public User(String fullName, String email, String username, String password,
                String role, boolean blocked, String blockReason) {
        this.fullName = fullName;
        this.email = email;
        this.username = username;
        this.password = password;
        this.role = role;
        this.blocked = blocked;
        this.blockReason = blockReason;
    }

    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getRole() { return role; }
    public boolean isAdmin() { return ADMIN.equals(role); }
    public boolean isBlocked() { return blocked; }
    public String getBlockReason() { return blockReason; }

    public void setPassword(String password) { this.password = password; }

    public void setBlocked(boolean blocked, String reason) {
        this.blocked = blocked;
        this.blockReason = blocked ? reason : "";
    }

    public String toFileLine() {
        return String.join("|", fullName, email, username, password, role,
                blocked ? "BLOCKED" : "ACTIVE", blockReason);
    }
}

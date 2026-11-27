package service;

import model.User;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

/**
 * Reads and writes data/users.txt
 * Format per line:  fullName|email|username|password|role|status|blockReason
 * (In IntelliJ the working directory is the project root, so "data/users.txt" works.)
 */
public class FileHandler {

    public static final String USERS_FILE = "data/users.txt";

    // FILE READING: load every user from the text file
    public static ArrayList<User> loadUsers() {
        ArrayList<User> users = new ArrayList<>();
        File file = new File(USERS_FILE);

        if (!file.exists()) {
            return users; // no file yet = no users
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                String[] p = line.split("\\|", -1);
                if (p.length == 4) { // old format: plain active guest
                    users.add(new User(p[0], p[1], p[2], p[3]));
                } else if (p.length == 7) {
                    users.add(new User(p[0], p[1], p[2], p[3], p[4], "BLOCKED".equals(p[5]), p[6]));
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading users file: " + e.getMessage());
        }
        return users;
    }

    // FILE WRITING: overwrite the file with the current list
    public static boolean saveUsers(ArrayList<User> users) {
        File file = new File(USERS_FILE);
        File folder = file.getParentFile();
        if (folder != null && !folder.exists()) {
            folder.mkdirs();
        }

        try (PrintWriter pw = new PrintWriter(new FileWriter(file, StandardCharsets.UTF_8))) {
            for (User u : users) {
                pw.println(u.toFileLine());
            }
            return true;
        } catch (IOException e) {
            System.out.println("Error saving users file: " + e.getMessage());
            return false;
        }
    }

    public static boolean usernameExists(String username) {
        return findByUsername(username) != null;
    }

    public static User findByUsername(String username) {
        for (User u : loadUsers()) {
            if (u.getUsername().equalsIgnoreCase(username)) {
                return u;
            }
        }
        return null;
    }

    // Returns the User if username + password match, otherwise null
    // (the caller checks user.isBlocked())
    public static User login(String username, String password) {
        ArrayList<User> users = loadUsers();
        for (User u : users) {
            if (u.getUsername().equalsIgnoreCase(username) && u.getPassword().equals(password)) {
                return u;
            }
        }
        return null;
    }

    public static boolean addUser(User newUser) {
        ArrayList<User> users = loadUsers();
        users.add(newUser);
        return saveUsers(users);
    }

    // Returns true only if username AND email match a saved user
    public static boolean resetPassword(String username, String email, String newPassword) {
        ArrayList<User> users = loadUsers();
        for (User u : users) {
            if (u.getUsername().equalsIgnoreCase(username) && u.getEmail().equalsIgnoreCase(email)) {
                u.setPassword(newPassword);
                return saveUsers(users);
            }
        }
        return false;
    }

    // ------------------------------------------------------------ blocklist

    /** Blocks or unblocks an account (admin feature). */
    public static boolean setBlocked(String username, boolean blocked, String reason) {
        ArrayList<User> users = loadUsers();
        for (User u : users) {
            if (u.getUsername().equalsIgnoreCase(username)) {
                u.setBlocked(blocked, reason);
                return saveUsers(users);
            }
        }
        return false;
    }

    public static boolean isBlocked(String username) {
        User u = findByUsername(username);
        return u != null && u.isBlocked();
    }

    /** True if a blocked account already uses this email (stops troll re-registration). */
    public static boolean isEmailBlocked(String email) {
        for (User u : loadUsers()) {
            if (u.isBlocked() && u.getEmail().equalsIgnoreCase(email)) {
                return true;
            }
        }
        return false;
    }
}

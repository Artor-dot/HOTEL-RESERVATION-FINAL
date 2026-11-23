package service;

import model.User;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 * Reads and writes data/users.txt
 * Format per line:  fullName|email|username|password
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

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                String[] parts = line.split("\\|");
                if (parts.length == 4) {
                    users.add(new User(parts[0], parts[1], parts[2], parts[3]));
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

        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
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
        ArrayList<User> users = loadUsers();
        for (User u : users) {
            if (u.getUsername().equalsIgnoreCase(username)) {
                return true;
            }
        }
        return false;
    }

    // Returns the User if username + password match, otherwise null
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
}

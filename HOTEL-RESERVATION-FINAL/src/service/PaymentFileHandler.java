package service;

import model.Payment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;

/** Reads and writes data/payments.txt (format is documented in model/Payment.java) */
public class PaymentFileHandler {

    public static final String PAYMENTS_FILE = "data/payments.txt";

    // FILE READING
    public static ArrayList<Payment> loadAll() {
        ArrayList<Payment> list = new ArrayList<>();
        File file = new File(PAYMENTS_FILE);
        if (!file.exists()) {
            return list;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                Payment p = Payment.fromLine(line);
                if (p != null) {
                    list.add(p);
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading payments file: " + e.getMessage());
        }
        return list;
    }

    // FILE WRITING
    public static boolean saveAll(ArrayList<Payment> list) {
        File file = new File(PAYMENTS_FILE);
        File folder = file.getParentFile();
        if (folder != null && !folder.exists()) {
            folder.mkdirs();
        }
        try (PrintWriter pw = new PrintWriter(new FileWriter(file, StandardCharsets.UTF_8))) {
            for (Payment p : list) {
                pw.println(p.toFileLine());
            }
            return true;
        } catch (IOException e) {
            System.out.println("Error saving payments file: " + e.getMessage());
            return false;
        }
    }

    public static boolean add(Payment p) {
        ArrayList<Payment> list = loadAll();
        list.add(p);
        return saveAll(list);
    }

    public static ArrayList<Payment> loadForUser(String username) {
        ArrayList<Payment> mine = new ArrayList<>();
        for (Payment p : loadAll()) {
            if (p.getUsername().equalsIgnoreCase(username)) {
                mine.add(p);
            }
        }
        return mine;
    }

    public static Payment findByBooking(String bookingId) {
        for (Payment p : loadAll()) {
            if (p.getBookingId().equals(bookingId)) {
                return p;
            }
        }
        return null;
    }

    public static boolean updateStatus(String paymentId, String newStatus) {
        ArrayList<Payment> list = loadAll();
        for (Payment p : list) {
            if (p.getPaymentId().equals(paymentId)) {
                p.setStatus(newStatus);
                return saveAll(list);
            }
        }
        return false;
    }

    /** Next id like PAY-2026-0001 */
    public static String nextPaymentId() {
        int max = 0;
        for (Payment p : loadAll()) {
            String id = p.getPaymentId();
            try {
                max = Math.max(max, Integer.parseInt(id.substring(id.lastIndexOf('-') + 1)));
            } catch (NumberFormatException e) {
                // ignore ids we can't read
            }
        }
        return String.format("PAY-%d-%04d", LocalDate.now().getYear(), max + 1);
    }
}

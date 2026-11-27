package service;

import model.Payment;

import java.time.YearMonth;

/**
 * The payment "gateway" for the whole system. It checks the payment details and returns
 * a Result. It is a simulation - no real money moves - but the checks are real:
 * card numbers must pass the Luhn check, the card must not be expired, etc.
 *
 * Test cards:  4242 4242 4242 4242  -> approved
 *              4000 0000 0000 0002  -> declined by the "bank"
 */
public class PaymentService {

    public static class Result {
        public final boolean approved;
        public final String status;     // Payment.PAID or Payment.PENDING when approved
        public final String reference;  // masked, e.g. "Visa ending 4242"
        public final String message;    // what to show the user

        private Result(boolean approved, String status, String reference, String message) {
            this.approved = approved;
            this.status = status;
            this.reference = reference;
            this.message = message;
        }

        static Result ok(String status, String reference, String message) {
            return new Result(true, status, reference, message);
        }

        static Result fail(String message) {
            return new Result(false, null, null, message);
        }
    }

    /** Cash at the front desk: nothing is charged now, the guest pays on arrival. */
    public static Result payCash() {
        return Result.ok(Payment.PENDING, "Pay at front desk", "Please pay at the front desk on arrival.");
    }

    public static Result payCard(String holder, String number, String expiry, String cvv) {
        if (holder.trim().isEmpty()) {
            return Result.fail("Please enter the name on the card.");
        }
        String digits = number.replaceAll("[\\s-]", "");
        if (!digits.matches("\\d{13,19}")) {
            return Result.fail("The card number must be 13 to 19 digits.");
        }
        if (!luhnValid(digits)) {
            return Result.fail("That card number is not valid. Please check it and try again.");
        }
        if (!expiry.trim().matches("(0[1-9]|1[0-2])/\\d{2}")) {
            return Result.fail("The expiry date must look like MM/YY, e.g. 08/28.");
        }
        int month = Integer.parseInt(expiry.trim().substring(0, 2));
        int year = 2000 + Integer.parseInt(expiry.trim().substring(3, 5));
        if (YearMonth.of(year, month).isBefore(YearMonth.now())) {
            return Result.fail("This card has expired.");
        }
        if (!cvv.trim().matches("\\d{3,4}")) {
            return Result.fail("The CVV must be 3 or 4 digits.");
        }
        if (digits.endsWith("0002")) {
            return Result.fail("Your card was declined by the bank. Please use another card.");
        }
        return Result.ok(Payment.PAID, brand(digits) + " ending " + last4(digits), "Card payment approved.");
    }

    public static Result payEWallet(String provider, String mobile) {
        String number = mobile.replaceAll("[\\s-]", "");
        if (!number.matches("(09|\\+639)\\d{9}")) {
            return Result.fail("Please enter a valid mobile number, e.g. 0917 555 0194.");
        }
        return Result.ok(Payment.PAID, provider + " ending " + last4(number), provider + " payment approved.");
    }

    // ------------------------------------------------------------ helpers

    private static String last4(String s) {
        return s.substring(s.length() - 4);
    }

    private static String brand(String digits) {
        if (digits.startsWith("4")) {
            return "Visa";
        }
        if (digits.matches("5[1-5].*") || digits.startsWith("2")) {
            return "Mastercard";
        }
        if (digits.startsWith("34") || digits.startsWith("37")) {
            return "Amex";
        }
        return "Card";
    }

    /** Standard Luhn checksum used by real card numbers. */
    static boolean luhnValid(String digits) {
        int sum = 0;
        boolean doubleIt = false;
        for (int i = digits.length() - 1; i >= 0; i--) {
            int d = digits.charAt(i) - '0';
            if (doubleIt) {
                d *= 2;
                if (d > 9) {
                    d -= 9;
                }
            }
            sum += d;
            doubleIt = !doubleIt;
        }
        return sum % 10 == 0;
    }
}

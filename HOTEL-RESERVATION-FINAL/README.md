# Triple Threat — Hotel Reservation System

A Java Swing desktop app for browsing hotel rooms, booking and paying for a stay, and managing guests.
Built in IntelliJ IDEA with plain Java (no external libraries).

## Team
- ANSALI, RAZHIEL
- CABRILLES, ARTHUR JOSHUA
- COMANDAO, YHASHUA
- ENRIQUEZ, GABRIEL MARTINI
- GAMAO, JOHN SEBASTIAN

## How to run (IntelliJ IDEA)
1. **File > Open** and choose this folder (`HOTEL-RESERVATION-FINAL`).
2. If `src` is not blue, right-click `src` > **Mark Directory as > Sources Root**.
3. Set a JDK (17 or newer) under **File > Project Structure > Project SDK**.
4. Run `src/Main.java`. Keep the working directory as the project root so `data/` and `images/` are found (the default).

## Demo accounts
| Role | Username | Password |
|---|---|---|
| Admin | `admin` | `admin123` |
| Guest | `demo` | `demo123` |
| Guest (has a paid booking) | `r2r` | `tortor` |
| Guest (troll-like sample: pending Penthouse booking, 2 cancellations) | `dwadwa` | `dwadwa` |

The sample bookings and payments in `data/` are demo data. To start clean, empty `data/reservations.txt` and `data/payments.txt`.

## Guest features
- **Login / Create Account / Forgot Password** — accounts are saved in `data/users.txt`.
- **Browse Rooms** — 5 room types from `data/rooms.txt`. Pick dates and guests; each room shows **Available**, **Only N left**, or **Sold Out** for those dates.
- **Room Details + Stay Calculator** — photo, description, amenities, optional add-ons and a live price breakdown.
- **Checkout & Booking Confirmation** — choose a payment method, enter guest details, and see a live receipt.
- **My Reservations** — your bookings with a receipt panel; cancel an upcoming booking (a paid booking is refunded).
- **Payments** — your payment history: paid, due at the front desk, refunded.
- **Profile** — your account details.

## Payment system
Every booking has a payment record in `data/payments.txt`.

| Method | What the guest enters | Result |
|---|---|---|
| Cash at Front Desk | nothing | `PENDING` until the admin marks it as paid at the desk |
| Credit / Debit Card | card number, name, expiry (MM/YY), CVV | checked (Luhn, expiry, CVV), then `PAID` |
| E-Wallet (GCash / Maya) | wallet and mobile number | checked, then `PAID` |

- Payment is taken **before** the booking is saved, so a failed payment never creates a booking.
- Only a masked reference (e.g. `Visa ending 4242`) is stored. Full card numbers and CVVs are never saved.
- Cancelling a `PAID` booking marks the payment `REFUNDED`; cancelling a `PENDING` one marks it `VOID`.
- The gateway is a simulation (no real money moves). Test cards: `4242 4242 4242 4242` is approved, `4000 0000 0000 0002` is declined.

## Admin features (log in as `admin`)
- **Guests** — every guest account with search, bookings and cancellations count, and details. **Block Guest** asks for a reason and can also cancel the guest's upcoming bookings (frees the rooms, refunds payments). Blocked accounts cannot log in, cannot book, and their email cannot register a new account. **Unblock Guest** restores access.
- **Reservations** — all bookings from all guests, with search and cancel.
- **Payments** — all payments, totals (collected / pending / refunded), and **Mark as Paid** for cash received at the front desk.

## Pricing (Philippine pesos, ₱)
- Room: price per night x nights (Standard ₱2,500, Luxury Super Deluxe ₱5,500, Family Suite ₱7,500, Executive Suite ₱9,500, Penthouse ₱15,000)
- Airport Transfer: flat ₱800.00 (optional)
- Daily Room Cleaning: ₱300.00 per night (optional)
- Service Fee: ₱250.00 per night
- Room prices are in `data/rooms.txt`; the add-on and service fees are constants at the top of `src/model/BookingDraft.java`.

## Data files
Plain text (UTF-8), fields separated by `|`.

- `data/users.txt` — `fullName|email|username|password|role|status|blockReason` (role `GUEST`/`ADMIN`, status `ACTIVE`/`BLOCKED`)
- `data/rooms.txt` — `id|name|pricePerNight (in pesos)|maxGuests|units|summary|description|amenity1;amenity2;...|imagePath` (`units` = how many rooms of that type the hotel has; lines starting with `#` are ignored)
- `data/reservations.txt` — `bookingId|username|guestName|contact|roomId|roomName|checkIn|checkOut|guests|addOns|payment|total|status`
- `data/payments.txt` — `paymentId|bookingId|username|method|reference|amount|status|timestamp` (status `PAID`/`PENDING`/`REFUNDED`/`VOID`)

Room photos go in `images/` (names are set in `rooms.txt`). A missing photo shows a simple room drawing instead.

## Project structure
```
src/Main.java
src/model/    User, Room, Reservation, Payment, BookingDraft
src/service/  FileHandler (users + blocklist), RoomFileHandler, ReservationFileHandler,
              PaymentFileHandler, PaymentService, BookingService
src/ui/       MainFrame, LoginPanel, AppPanel (guest), AdminPanel (admin), BrowsePanel, DetailsPanel,
              CheckoutPanel, ReservationsPanel, PaymentsPanel, AdminGuestsPanel,
              AdminReservationsPanel, AdminPaymentsPanel, ReadOnlyTable, DateChooser, Theme
data/         users.txt, rooms.txt, reservations.txt, payments.txt
images/       room photos
```

## Notes
- A room type can only be booked while units are left for every night of the stay (checked on the catalog, the details page, and again at confirmation).
- Pop-ups (JOptionPane) are used for validation, confirmations, payment results, and admin actions.
- Passwords are stored as plain text in `users.txt`, which is fine for a class project but not for a real system.

package model;

/** One room type loaded from data/rooms.txt */
public class Room {
    private final String id;
    private final String name;
    private final double pricePerNight;
    private final int maxGuests;
    private final int units;           // how many of this room the hotel has
    private final String summary;      // short line shown on the catalog card
    private final String description;  // long text shown on the details page
    private final String[] amenities;
    private final String imagePath;    // e.g. images/standard.jpg (optional)

    public Room(String id, String name, double pricePerNight, int maxGuests, int units,
                String summary, String description, String[] amenities, String imagePath) {
        this.id = id;
        this.name = name;
        this.pricePerNight = pricePerNight;
        this.maxGuests = maxGuests;
        this.units = units;
        this.summary = summary;
        this.description = description;
        this.amenities = amenities;
        this.imagePath = imagePath;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public double getPricePerNight() { return pricePerNight; }
    public int getMaxGuests() { return maxGuests; }
    public int getUnits() { return units; }
    public String getSummary() { return summary; }
    public String getDescription() { return description; }
    public String[] getAmenities() { return amenities; }
    public String getImagePath() { return imagePath; }
}

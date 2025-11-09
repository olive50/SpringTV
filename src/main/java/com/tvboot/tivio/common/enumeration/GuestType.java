package com.tvboot.tivio.common.enumeration;

public enum GuestType {
    INDIVIDUAL("Individual", "Single guest or direct booking"),
    FAMILY("Family", "Family booking or stay with children"),
    CORPORATE("Corporate", "Guest associated with a company or business"),
    GROUP("Group", "Multiple guests booked together"),
    VIP("VIP", "Very important guest with special privileges"),
    WALK_IN("Walk-In", "Guest without prior reservation"),
    LONG_STAY("Long Stay", "Guest staying for an extended period"),
    DAY_USE("Day Use", "Guest using the room for part of the day"),
    TRAVEL_AGENT("Travel Agent", "Booking made through a travel agency"),
    OTA("Online Travel Agent", "Booking via online platform like Booking.com"),
    HOUSE_USE("House Use", "Internal or complimentary stay for staff");

    private final String displayName;
    private final String description;

    GuestType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}
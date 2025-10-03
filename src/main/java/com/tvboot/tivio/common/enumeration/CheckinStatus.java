package com.tvboot.tivio.common.enumeration;

public enum CheckinStatus {
    CHECKED_OUT,  // Guest n'est pas dans l'hôtel
    CHECKED_IN,   // Guest est actuellement dans l'hôtel
    RESERVED      // Optional: Guest a une réservation future
}
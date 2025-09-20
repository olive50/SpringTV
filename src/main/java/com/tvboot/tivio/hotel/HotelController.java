package com.tvboot.tivio.hotel;



import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/hotel")
public class HotelController {

    @GetMapping("/guest-info")
    public Map<String, Object> getGuestInfo(@RequestParam String room) {
        // 🔹 In a real app, fetch from DB instead of hardcoding
        Map<String, Object> guestConfig = new HashMap<>();
        guestConfig.put("name", "Moussa kheyar " + room);
        guestConfig.put("roomNumber", room);

        Map<String, Object> wifiConfig = new HashMap<>();
        wifiConfig.put("ssid", "Samsung_Hospitality_Guest");
        wifiConfig.put("password", "Welcome" + room); // Example: unique password per room
        wifiConfig.put("encryption", "WPA");

        Map<String, Object> response = new HashMap<>();
        response.put("guestConfig", guestConfig);
        response.put("wifiConfig", wifiConfig);

        return response;
    }
}

package com.tvboot.tivio.guest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration"
})
class GuestServiceTest {

    @Autowired
    private GuestService guestService;

    @Autowired
    private GuestRepository guestRepository;

    @Test
    void shouldCreateAndFindGuest() {
        Guest guest = Guest.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@email.com")
                .build();

        Guest saved = guestService.createGuest(guest);
        Optional<Guest> found = guestRepository.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals("John", found.get().getFirstName());
    }
}

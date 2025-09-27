package com.tvboot.tivio.guest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class GuestRepositoryTest {

    @Autowired
    private GuestRepository guestRepository;

    @Test
    void shouldSaveAndFindGuest() {
        Guest guest = Guest.builder()
                .firstName("Jane")
                .lastName("Smith")
                .email("jane.smith@email.com")
                .build();

        Guest saved = guestRepository.save(guest);
        Optional<Guest> found = guestRepository.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals("Jane", found.get().getFirstName());
    }
}

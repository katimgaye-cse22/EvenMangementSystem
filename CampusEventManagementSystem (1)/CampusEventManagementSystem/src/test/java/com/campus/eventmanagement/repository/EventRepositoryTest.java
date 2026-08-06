package com.campus.eventmanagement.repository;

import com.campus.eventmanagement.entity.Event;
import com.campus.eventmanagement.entity.User;
import com.campus.eventmanagement.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @DataJpaTest spins up an embedded H2 database (see the test-scope H2
 * dependency in pom.xml) and rolls each test back automatically - this
 * exercises the JPQL search query against a real database without needing
 * MySQL running (course concept: repository/integration testing).
 */
@DataJpaTest
class EventRepositoryTest {

    @Autowired private EventRepository eventRepository;
    @Autowired private UserRepository userRepository;

    private User admin;

    @BeforeEach
    void setUp() {
        admin = userRepository.save(User.builder()
                .fullName("Admin User").studentId("ADMIN-1").email("admin@test.com")
                .password("hashed").role(Role.ADMIN).build());

        eventRepository.save(Event.builder()
                .title("Spring Boot Workshop").description("Hands-on REST API session")
                .category("Workshop").venue("Lab 1")
                .eventDate(LocalDate.now().plusDays(5))
                .startTime(LocalTime.of(10, 0)).endTime(LocalTime.of(12, 0))
                .capacity(30).createdBy(admin).build());

        eventRepository.save(Event.builder()
                .title("Career Fair").description("Meet campus recruiters")
                .category("Career Fair").venue("Auditorium")
                .eventDate(LocalDate.now().plusDays(10))
                .startTime(LocalTime.of(9, 0)).endTime(LocalTime.of(16, 0))
                .capacity(200).createdBy(admin).build());
    }

    @Test
    void searchEvents_byKeyword_matchesTitleCaseInsensitive() {
        Page<Event> result = eventRepository.searchEvents("spring", null, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Spring Boot Workshop");
    }

    @Test
    void searchEvents_byCategory_filtersCorrectly() {
        Page<Event> result = eventRepository.searchEvents(null, "Career Fair", PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getCategory()).isEqualTo("Career Fair");
    }

    @Test
    void searchEvents_noFilters_returnsAll() {
        Page<Event> result = eventRepository.searchEvents(null, null, PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    @Test
    void findDistinctCategories_returnsUniqueSortedCategories() {
        List<String> categories = eventRepository.findDistinctCategories();

        assertThat(categories).containsExactly("Career Fair", "Workshop");
    }
}

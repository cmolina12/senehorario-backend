package com.cmolina12.senehorario_backend.service;

import com.cmolina12.senehorario_backend.domain.Meeting;
import com.cmolina12.senehorario_backend.domain.Section;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;

// ✅ CORE / CRITICAL TEST SCENARIOS

// 1. Multiple courses with one section each (no time overlaps)
//    → Expect 1 valid schedule containing all sections.

// 2. Multiple courses with multiple sections (some overlap, some valid)
//    → Expect only non-overlapping combinations to be returned.

// 3. One course with a valid section, another course with no compatible section
//    → Expect 0 valid schedules due to unavoidable conflicts.

// 4. A course with multiple sections on different days (no overlap)
//    → Expect a valid schedule per section (e.g., 2 sections = 2 schedules).

// ✅ EDGE / STRESS TEST SCENARIOS

// 5. No courses / Empty input
//    → Expect 0 schedules or an empty list.

// 6. Single course with only one section
//    → Expect 1 schedule containing that single section.

// 7. Section with multiple meetings on different days (e.g., Mon & Wed)
//    → All meetings must be considered for overlap validation.

// 8. Sections with same times but on different days (e.g., Mon 9-10 vs Tue 9-10)
//    → Should not conflict and both should be allowed in a schedule.

// 9. Duplicate NRCs or meetings (invalid data or duplicate sections)
//    → Should handle gracefully, depending on your system's behavior (optional: test for exceptions or filter logic).

// ✅ OPTIONAL / REAL-WORLD TEST SCENARIOS

// 10. Stress test with large input: 5+ courses, each with multiple sections
//     → Expect the system to handle exponential combinations correctly and efficiently.

// 11. Back-to-back meetings (end time of one equals start time of another)
//     → Should not count as overlapping (e.g., 10:00–11:00 and 11:00–12:00).

// 12. Sections with null or malformed meeting data
//     → Should raise appropriate exceptions or skip invalid entries (depending on system requirements).

class ScheduleServiceTest {

    // Helper method to create a full Section object with a single meeting.

    private Section makeFullSection(
        String nrc,
        String sectionId,
        String term,
        String ptrm,
        String campus,
        DayOfWeek day,
        String start,
        String end,
        String location,
        List<String> professors
    ) {
        Meeting meeting = new Meeting(
            day,
            LocalTime.parse(start),
            LocalTime.parse(end),
            location
        ); // Create a Meeting object with the specified day, start time, end time, and location. This method assumes there's only one meeting per section for simplicity.
        return new Section(
            nrc,
            sectionId,
            term,
            ptrm,
            campus,
            List.of(meeting),
            professors,
            5, // available seats
            30 // total seats
        ); // Create a Section object with the provided parameters, including the list of meetings and professors.
    }

    // Helper method to create a full Section object with multiple meetings.

    private Section makeFullSectionMultipleMeetings(
        String nrc,
        String sectionId,
        String term,
        String ptrm,
        String campus,
        List<Meeting> meetings, // List<Meeting> meetings, // This method allows for multiple meetings to be associated with a section, which is useful for sections that meet on different days or times.
        List<String> professors
    ) {
        return new Section(
            nrc,
            sectionId,
            term,
            ptrm,
            campus,
            meetings,
            professors,
            5, // available seats
            30 // total seats
        ); // Create a Section object with the provided parameters, including the list of meetings and professors.
    }

    @Test
    void courseWithTwoNonOverlappingSections_shouldReturnTwoSchedules() {
        Section s1 = makeFullSection(
            "11060",
            "1",
            "202519",
            "1",
            "CAMPUS PRINCIPAL",
            DayOfWeek.MONDAY,
            "09:00",
            "10:00",
            ".A_101",
            List.of("PROF. A")
        );
        Section s2 = makeFullSection(
            "11061",
            "2",
            "202519",
            "1",
            "CAMPUS PRINCIPAL",
            DayOfWeek.MONDAY,
            "10:00",
            "11:00",
            ".A_102",
            List.of("PROF. B")
        );

        List<List<Section>> candidates = List.of(List.of(s1, s2)); // Create a list of lists of Section objects, where each inner list represents the candidate sections for a certain course. In this case, we have two sections (s1 and s2) for the same course, which do not overlap in time.

        ScheduleService scheduleService = new ScheduleService(); // Create an instance of ScheduleService to use its methods for generating schedules based on course sections.

        scheduleService.verifyCorrectParameters(candidates); // Call the verifyCorrectParameters method of ScheduleService to ensure that the candidates list is valid and meets the requirements for scheduling.

        List<List<Section>> schedules = scheduleService.generateAllSchedules(
            candidates
        ); // Call the generateAllSchedules method of ScheduleService with the candidates list to get all possible schedules.

        // Assert that the size of the schedules list is 2, indicating that two non-overlapping sections were successfully scheduled. (2 because we have two sections for one course, so there are two possible schedules: one with section 1 and another with section 2)
        assert schedules.size() == 2 : "Expected 2 schedules, but got " +
        schedules.size();
        // Assert that the first schedule contains the first section and the second schedule contains the second section
        assert schedules
            .get(0)
            .contains(
                s1
            ) : "First schedule should contain section 1, but it does not.";
        assert schedules
            .get(1)
            .contains(
                s2
            ) : "Second schedule should contain section 2, but it does not.";
    }

    @Test
    void courseWithTwoOverlappingSections_shouldReturnEmptySchedule() {
        Section a1 = makeFullSection(
            "A1",
            "1",
            "202519",
            "1",
            "CAMPUS",
            DayOfWeek.MONDAY,
            "09:00",
            "10:00",
            "Edificio X Aula 1",
            List.of("PROF. A")
        );
        Section b1 = makeFullSection(
            "B1",
            "1",
            "202519",
            "1",
            "CAMPUS",
            DayOfWeek.MONDAY,
            "09:30",
            "10:30",
            "Edificio Y Aula 2",
            List.of("PROF. C")
        );

        List<List<Section>> candidates = List.of(List.of(a1), List.of(b1)); // Create a list of lists of Section objects, where each inner list represents a candidate section for a specific course for the schedule.

        ScheduleService scheduleService = new ScheduleService(); // Create an instance of ScheduleService to use its methods for generating schedules based on course sections.

        scheduleService.verifyCorrectParameters(candidates); // Call the verifyCorrectParameters method of ScheduleService to ensure that the candidates list is valid and meets the requirements for scheduling.

        List<List<Section>> schedules = scheduleService.generateAllSchedules(
            candidates
        ); // Call the generateAllSchedules method of ScheduleService with the candidates list to get all possible schedules.

        // Assert that the size of the schedules list is 0, indicating that no schedules were generated due to overlapping sections. Section a1 and Section b1 overlap in time for different courses, so no valid schedule can be created.
        assert schedules.size() == 0 : "Expected 0 schedules, but got " +
        schedules.size();
        // Assert that the schedules list is empty, confirming that no valid schedules were generated
        assert schedules.isEmpty() : "Schedules should be empty, but it is not.";
    }

    // 1. Multiple courses with one section each (no time overlaps)
    @Test
    void multipleCoursesWithOneSectionEach_shouldReturnValidSchedule() {
        Section course1 = makeFullSection(
            "10001",
            "1",
            "202519",
            "1",
            "CAMPUS A",
            DayOfWeek.MONDAY,
            "09:00",
            "10:00",
            "Room 101",
            List.of("Prof. A")
        );
        Section course2 = makeFullSection(
            "10002",
            "1",
            "202519",
            "1",
            "CAMPUS B",
            DayOfWeek.TUESDAY,
            "10:00",
            "11:00",
            "Room 102",
            List.of("Prof. B")
        );

        Section course3 = makeFullSection(
            "10003",
            "1",
            "202519",
            "1",
            "CAMPUS C",
            DayOfWeek.WEDNESDAY,
            "11:00",
            "12:00",
            "Room 103",
            List.of("Prof. C")
        );

        Section course4 = makeFullSection(
            "10004",
            "1",
            "202519",
            "1",
            "CAMPUS D",
            DayOfWeek.THURSDAY,
            "12:00",
            "13:00",
            "Room 104",
            List.of("Prof. D")
        );

        List<List<Section>> candidates = List.of(
            List.of(course1),
            List.of(course2),
            List.of(course3),
            List.of(course4)
        );

        ScheduleService scheduleService = new ScheduleService();

        scheduleService.verifyCorrectParameters(candidates); // Call the verifyCorrectParameters method of ScheduleService to ensure that the candidates list is valid and meets the requirements for scheduling.

        List<List<Section>> schedules = scheduleService.generateAllSchedules(
            candidates
        );

        assert schedules.size() == 1 : "Expected 1 valid schedule, but got " +
        schedules.size(); // One valid schedule because all courses have non-overlapping sections.
        assert schedules
            .get(0)
            .contains(course1) : "First schedule should contain course 1.";
        assert schedules
            .get(0)
            .contains(course2) : "First schedule should contain course 2.";
    }

    // 2. Multiple courses with multiple sections (some overlap, some valid)
}

package com.cmolina12.senehorario_backend.service;

import com.cmolina12.senehorario_backend.domain.Course;
import com.cmolina12.senehorario_backend.domain.Meeting;
import com.cmolina12.senehorario_backend.domain.Section;
import com.cmolina12.senehorario_backend.models.ApiCourse;
import com.cmolina12.senehorario_backend.models.Instructor;
import com.cmolina12.senehorario_backend.models.Schedule;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class CourseService {

    @Autowired
    private RestTemplate restTemplate; // RestTemplate is used to make HTTP requests to the API to fetch course data.

    @Value("${uniandes.api.base-url}")
    private String apiBaseUrl;

    /**
     * Fetches raw course sections from the API based on the provided name input.
     * This actually works the same way as if you were to use the search bar in the
     * website.
     *
     * @param nameInput the name input to filter course sections.
     * @return an array of ApiCourse objects representing the course sections.
     */

    public ApiCourse[] fetchRawSections(String nameInput) {
        String url =
            apiBaseUrl +
            "?term=&ptrm=&prefix=&attr=&nameInput=" + // The URL is constructed to include the base URL, and query
            // parameters for term, ptrm, prefix, attr, and nameInput.
            nameInput.toUpperCase(); // The URL is constructed to include the base URL, and query parameters for
        // term, ptrm, prefix, attr, and nameInput INITIALLY. The nameInput is
        // converted to uppercase to match the expected format in the API. Additional
        // query parameters can be added after the nameInput if needed.

        // The RestTemplate is used to make a GET request to the constructed URL, and
        // the response is expected to be an array of ApiCourse objects.
        return restTemplate.getForObject(url, ApiCourse[].class);
    }

    /**
     * Converts raw course sections fetched from the API into a list of Course
     * objects, each containing its sections.
     *
     * @param nameInput the name input to filter course sections.
     * @return a list of Course objects representing the courses and their sections.
     */

    public List<Course> getDomainCourses(String nameInput) {
        ApiCourse[] raw = fetchRawSections(nameInput); // Fetches raw course sections from the API based on the provided
        // name input.

        // Temporal map
        Map<String, Course> courseMap = new LinkedHashMap<>(); // Initializes a map to hold courses by their code.

        for (ApiCourse a : raw) {
            String code = a.getClazz() + a.getCourse();

            // Construct the course

            Course course = courseMap.computeIfAbsent(code, c ->
                new Course(c, a.getTitle(), Integer.parseInt(a.getCredits()))
            );

            // Fill in the sections

            List<Meeting> meetings = new ArrayList<>(); // Initializes a list to hold meetings for the course section.

            for (Schedule s : a.getSchedules()) {
                for (DayOfWeek day : parseDays(s)) {
                    LocalTime start = parseTime(s.getTime_ini());
                    LocalTime end = parseTime(s.getTime_fin());
                    String location = s.getBuilding() + " " + s.getClassroom();
                    meetings.add(new Meeting(day, start, end, location));
                }
            }

            // Professors

            List<String> profs = new ArrayList<>();

            for (Instructor ins : a.getInstructors()) {
                profs.add(reorderProfessorName(ins.getName())); // Adds the reordered professor's name
            }

            // Available Seats

            int availableSeats = Integer.parseInt(a.getSeatsavail());
            int totalSeats = Integer.parseInt(a.getMaxenrol());

            // Create a section with the course code, section number, meetings, and
            // professors.

            Section sec = new Section(
                a.getNrc(), // NRC (Número de Registro de Clase) is used as a unique identifier for the
                // section.
                a.getSection(), // Section ID, e.g., "1", "A", "B".
                a.getTerm(), // Term, e.g., "202519".
                a.getPtrm(), // PTRM (Periodo de Tiempo), e.g., "1" or "8A".
                a.getCampus(), // Campus, e.g., "CAMPUS PRINCIPAL".
                meetings, // List of Meeting objects representing the schedule for the section.
                profs, // List of professors teaching the section.
                availableSeats,
                totalSeats
            );

            // Associate the section with the course.
            course.addSection(sec); // Adds the section to the course's list of sections.
        }

        // Return the list of courses.
        return new ArrayList<>(courseMap.values());
    }

    /**
     * Parses a time string in the format "hhmm" and returns a LocalTime object.
     *
     * @param hhmm the time string in "hhmm" format.
     * @return a LocalTime object representing the parsed time.
     */

    private LocalTime parseTime(String hhmm) {
        if (hhmm == null || hhmm.length() < 4) {
            throw new IllegalArgumentException(
                "Invalid time format when trying to parse: " + hhmm
            );
        }
        int hour = Integer.parseInt(hhmm.substring(0, 2)); // Extracts the hour part from the hhmm string.
        int minute = Integer.parseInt(hhmm.substring(2, 4)); // Extracts the minute part from the hhmm string.
        return LocalTime.of(hour, minute); // Returns a LocalTime object representing the time.
    }

    /**
     * Parses a Schedule object and returns a list of DayOfWeek objects representing
     * the days of the week.
     *
     * @param s the Schedule object to parse.
     * @return a list of DayOfWeek objects representing the days of the week.
     */

    private List<DayOfWeek> parseDays(Schedule s) {
        List<DayOfWeek> days = new ArrayList<>(); // Initializes an empty list to hold the days of the week.

        if ("L".equalsIgnoreCase(s.getL())) days.add(DayOfWeek.MONDAY);
        if ("M".equalsIgnoreCase(s.getM())) days.add(DayOfWeek.TUESDAY);
        if ("I".equalsIgnoreCase(s.getI())) days.add(DayOfWeek.WEDNESDAY);
        if ("J".equalsIgnoreCase(s.getJ())) days.add(DayOfWeek.THURSDAY);
        if ("V".equalsIgnoreCase(s.getV())) days.add(DayOfWeek.FRIDAY);
        if ("S".equalsIgnoreCase(s.getS())) days.add(DayOfWeek.SATURDAY);

        return days; // Returns the list of days of the week.
    }

    /**
     * Finds sections by course code.
     *
     * @param code the course code to search for.
     * @return a list of Section objects associated with the course code.
     */

    public List<Section> findSectionsByCourseCode(String code) {
        List<Course> courses = getDomainCourses(code); // Fetches the list of courses based on the provided course code.

        if (courses.isEmpty()) {
            return new ArrayList<>(); // If no courses are found, return an empty list.
        }

        // We want to return sections from the first course found with the given code.
        // (The only one that should exist, actually)
        Course course = courses.get(0); // Gets the first course from the list.

        return course.getSections(); // Returns the list of sections associated with the course.
    }

    /**
     * Reorders the professor's name based on the number of parts in the name.
     *
     * @param name the professor's name to reorder.
     * @return the reordered name
     */

    private String reorderProfessorName(String name) {
        if (name == null || name.trim().isEmpty()) return name; // If the name is null or empty, return it as is.

        String[] parts = name.trim().split("\\s+"); // Splits the name into parts based on whitespace.
        int len = parts.length;

        if (len == 2) {
            return parts[1] + " " + parts[0]; // Original would be "LastName FirstName", we reorder it to "FirstName
            // LastName".
        } else if (len == 3) {
            return parts[2] + " " + parts[0] + " " + parts[1]; // Original would be "LastName SecondLastName FirstName",
            // we reorder it to "FirstName LastName SecondLastName".
        } else if (len == 4) {
            return parts[2] + " " + parts[3] + " " + parts[0] + " " + parts[1]; // Original would be "LastName
            // SecondLastName FirstName SecondName",
            // we reorder it to "FirstName LastName
            // SecondLastName ThirdLastName".
        } else {
            return name; // If the name has more than 4 parts, we return it as is.
        }
    }
}

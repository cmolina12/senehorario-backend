package com.cmolina12.senehorario_backend.domain;

import java.util.List;
import lombok.Getter;

public class Section {

    @Getter
    private final String nrc; // p.ej. "11060"

    @Getter
    private final String sectionId; // p.ej. "1", "A", "B"

    @Getter
    private final String term; // p.ej. "202519"

    @Getter
    private final String ptrm; // p.ej. "1" o "8A"

    @Getter
    private final String campus; // p.ej. "CAMPUS PRINCIPAL"

    @Getter
    private final List<Meeting> meetings; // horarios

    @Getter
    private final List<String> professors; // nombres de instructores

    @Getter
    private final int availableSeats; // cantidad de asientos disponibles

    @Getter
    private final int totalSeats; // cantidad total de asientos

    public Section(
        String nrc,
        String sectionId,
        String term,
        String ptrm,
        String campus,
        List<Meeting> meetings,
        List<String> professors,
        int availableSeats,
        int totalSeats
    ) {
        this.nrc = nrc;
        this.sectionId = sectionId;
        this.term = term;
        this.ptrm = ptrm;
        this.campus = campus;
        this.meetings = meetings;
        this.professors = professors;
        this.availableSeats = availableSeats;
        this.totalSeats = totalSeats;
    }
}

package com.college.attendance.controller;

import com.college.attendance.dto.Dtos.*;
import com.college.attendance.service.AttendanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/faculty")
@RequiredArgsConstructor
public class FacultyController {
    private final AttendanceService service;

    @GetMapping("/subjects")
    public List<SubjectResponse> mySubjects(Authentication auth) {
        return service.subjectsForFaculty(auth.getName());
    }

    @GetMapping("/subjects/{subjectId}/sheet")
    public List<SheetRow> sheet(@PathVariable Long subjectId,
                                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                Authentication auth) {
        return service.sheet(auth.getName(), subjectId, date);
    }

    @PostMapping("/attendance")
    public Map<String, Object> mark(@Valid @RequestBody MarkAttendanceRequest request, Authentication auth) {
        int saved = service.mark(auth.getName(), request);
        return Map.of("saved", saved);
    }
}

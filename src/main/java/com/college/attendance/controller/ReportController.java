package com.college.attendance.controller;

import com.college.attendance.dto.Dtos.ReportRow;
import com.college.attendance.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {
    private final AttendanceService service;

    @GetMapping("/subject/{subjectId}")
    public List<ReportRow> subjectReport(@PathVariable Long subjectId, Authentication auth) {
        return service.subjectReport(auth.getName(), isAdmin(auth), subjectId);
    }

    @GetMapping("/low-attendance")
    public List<ReportRow> lowAttendance(@RequestParam(defaultValue = "75") double threshold, Authentication auth) {
        return service.lowAttendance(auth.getName(), isAdmin(auth), threshold);
    }

    private boolean isAdmin(Authentication auth) {
        return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}

package com.college.attendance.controller;

import com.college.attendance.dto.Dtos.HistoryItem;
import com.college.attendance.dto.Dtos.StudentSummary;
import com.college.attendance.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
public class StudentController {
    private final AttendanceService service;

    @GetMapping("/summary")
    public StudentSummary summary(Authentication auth) {
        return service.summary(auth.getName());
    }

    @GetMapping("/history")
    public List<HistoryItem> history(@RequestParam(required = false) Long subjectId, Authentication auth) {
        return service.history(auth.getName(), subjectId);
    }
}

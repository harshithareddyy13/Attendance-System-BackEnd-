package com.college.attendance.dto;

import com.college.attendance.entity.AttendanceStatus;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.List;

/** All request/response payloads in one place. */
public final class Dtos {
    private Dtos() {}

    // ---------- auth ----------
    public record LoginRequest(@NotBlank String username, @NotBlank String password) {}
    public record LoginResponse(String token, String role, String username, String fullName) {}

    // ---------- admin: students ----------
    public record StudentRequest(
            @NotBlank(message = "Roll number is required") String rollNo,
            @NotBlank(message = "Name is required") String name,
            String email,
            @NotBlank(message = "Department is required") String department,
            @NotNull(message = "Semester is required") @Min(1) @Max(12) Integer semester,
            String password) {}
    public record StudentResponse(Long id, String rollNo, String name, String email,
                                  String department, int semester) {}

    // ---------- admin: faculty ----------
    public record FacultyRequest(
            @NotBlank(message = "Username is required") String username,
            @NotBlank(message = "Name is required") String name,
            String email,
            @NotBlank(message = "Department is required") String department,
            String password) {}
    public record FacultyResponse(Long id, String username, String name, String email, String department) {}

    // ---------- admin: subjects ----------
    public record SubjectRequest(
            @NotBlank(message = "Subject code is required") String code,
            @NotBlank(message = "Subject name is required") String name,
            @NotBlank(message = "Department is required") String department,
            @NotNull(message = "Semester is required") @Min(1) @Max(12) Integer semester,
            Long facultyId) {}
    public record SubjectResponse(Long id, String code, String name, String department, int semester,
                                  Long facultyId, String facultyName) {}

    public record Stats(long students, long faculty, long subjects, long attendanceRecords) {}

    // ---------- attendance ----------
    public record AttendanceEntry(@NotNull Long studentId, AttendanceStatus status) {}
    public record MarkAttendanceRequest(
            @NotNull Long subjectId,
            @NotNull LocalDate date,
            @NotEmpty List<AttendanceEntry> records) {}
    /** One row of the roll-call sheet. status is null if nothing was recorded yet. */
    public record SheetRow(Long studentId, String rollNo, String name, AttendanceStatus status) {}

    // ---------- reports ----------
    public record ReportRow(Long studentId, String rollNo, String name, String subjectCode,
                            long total, long present, double percentage) {}

    // ---------- student view ----------
    public record SubjectSummary(Long subjectId, String code, String name,
                                 long total, long present, double percentage) {}
    public record StudentSummary(String rollNo, String name, String department, int semester,
                                 long total, long present, double percentage,
                                 List<SubjectSummary> subjects) {}
    public record HistoryItem(LocalDate date, Long subjectId, String subjectCode, String subjectName,
                              AttendanceStatus status) {}
}

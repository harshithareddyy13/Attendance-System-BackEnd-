package com.college.attendance.controller;

import com.college.attendance.dto.Dtos.*;
import com.college.attendance.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService service;

    @GetMapping("/stats")
    public Stats stats() { return service.stats(); }

    // ---- students
    @GetMapping("/students")
    public List<StudentResponse> students() { return service.listStudents(); }

    @PostMapping("/students")
    public StudentResponse createStudent(@Valid @RequestBody StudentRequest r) { return service.createStudent(r); }

    @PutMapping("/students/{id}")
    public StudentResponse updateStudent(@PathVariable Long id, @Valid @RequestBody StudentRequest r) {
        return service.updateStudent(id, r);
    }

    @DeleteMapping("/students/{id}")
    public ResponseEntity<Void> deleteStudent(@PathVariable Long id) {
        service.deleteStudent(id);
        return ResponseEntity.noContent().build();
    }

    // ---- faculty
    @GetMapping("/faculty")
    public List<FacultyResponse> faculty() { return service.listFaculty(); }

    @PostMapping("/faculty")
    public FacultyResponse createFaculty(@Valid @RequestBody FacultyRequest r) { return service.createFaculty(r); }

    @PutMapping("/faculty/{id}")
    public FacultyResponse updateFaculty(@PathVariable Long id, @Valid @RequestBody FacultyRequest r) {
        return service.updateFaculty(id, r);
    }

    @DeleteMapping("/faculty/{id}")
    public ResponseEntity<Void> deleteFaculty(@PathVariable Long id) {
        service.deleteFaculty(id);
        return ResponseEntity.noContent().build();
    }

    // ---- subjects
    @GetMapping("/subjects")
    public List<SubjectResponse> subjects() { return service.listSubjects(); }

    @PostMapping("/subjects")
    public SubjectResponse createSubject(@Valid @RequestBody SubjectRequest r) { return service.createSubject(r); }

    @PutMapping("/subjects/{id}")
    public SubjectResponse updateSubject(@PathVariable Long id, @Valid @RequestBody SubjectRequest r) {
        return service.updateSubject(id, r);
    }

    @DeleteMapping("/subjects/{id}")
    public ResponseEntity<Void> deleteSubject(@PathVariable Long id) {
        service.deleteSubject(id);
        return ResponseEntity.noContent().build();
    }
}

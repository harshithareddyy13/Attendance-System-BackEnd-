package com.college.attendance.service;

import com.college.attendance.dto.Dtos.*;
import com.college.attendance.entity.*;
import com.college.attendance.exception.ApiException;
import com.college.attendance.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final UserRepository users;
    private final StudentRepository students;
    private final FacultyRepository faculties;
    private final SubjectRepository subjects;
    private final AttendanceRepository attendance;
    private final PasswordEncoder encoder;

    // ------------------------------------------------------------ stats
    public Stats stats() {
        return new Stats(students.count(), faculties.count(), subjects.count(), attendance.count());
    }

    // ------------------------------------------------------------ students
    public List<StudentResponse> listStudents() {
        return students.findAllByOrderByRollNo().stream().map(this::toDto).toList();
    }

    @Transactional
    public StudentResponse createStudent(StudentRequest r) {
        String roll = r.rollNo().trim();
        if (students.existsByRollNo(roll) || users.existsByUsername(roll)) {
            throw new ApiException(HttpStatus.CONFLICT, "Roll number " + roll + " already exists");
        }
        String rawPassword = isBlank(r.password()) ? roll : r.password();
        User user = User.builder().username(roll).password(encoder.encode(rawPassword))
                .fullName(r.name().trim()).role(Role.STUDENT).build();
        Student s = Student.builder().rollNo(roll).name(r.name().trim()).email(r.email())
                .department(r.department().trim()).semester(r.semester()).user(user).build();
        return toDto(students.save(s));
    }

    @Transactional
    public StudentResponse updateStudent(Long id, StudentRequest r) {
        Student s = students.findById(id).orElseThrow(() -> ApiException.notFound("Student"));
        s.setName(r.name().trim());
        s.setEmail(r.email());
        s.setDepartment(r.department().trim());
        s.setSemester(r.semester());
        s.getUser().setFullName(r.name().trim());
        if (!isBlank(r.password())) s.getUser().setPassword(encoder.encode(r.password()));
        return toDto(students.save(s));
    }

    @Transactional
    public void deleteStudent(Long id) {
        Student s = students.findById(id).orElseThrow(() -> ApiException.notFound("Student"));
        attendance.deleteByStudentId(id);
        students.delete(s); // cascades to the login account
    }

    // ------------------------------------------------------------ faculty
    public List<FacultyResponse> listFaculty() {
        return faculties.findAllByOrderByName().stream().map(this::toDto).toList();
    }

    @Transactional
    public FacultyResponse createFaculty(FacultyRequest r) {
        String username = r.username().trim();
        if (users.existsByUsername(username)) {
            throw new ApiException(HttpStatus.CONFLICT, "Username " + username + " is already taken");
        }
        if (isBlank(r.password())) throw ApiException.badRequest("Password is required for a new faculty member");
        User user = User.builder().username(username).password(encoder.encode(r.password()))
                .fullName(r.name().trim()).role(Role.FACULTY).build();
        Faculty f = Faculty.builder().name(r.name().trim()).email(r.email())
                .department(r.department().trim()).user(user).build();
        return toDto(faculties.save(f));
    }

    @Transactional
    public FacultyResponse updateFaculty(Long id, FacultyRequest r) {
        Faculty f = faculties.findById(id).orElseThrow(() -> ApiException.notFound("Faculty member"));
        f.setName(r.name().trim());
        f.setEmail(r.email());
        f.setDepartment(r.department().trim());
        f.getUser().setFullName(r.name().trim());
        if (!isBlank(r.password())) f.getUser().setPassword(encoder.encode(r.password()));
        return toDto(faculties.save(f));
    }

    @Transactional
    public void deleteFaculty(Long id) {
        Faculty f = faculties.findById(id).orElseThrow(() -> ApiException.notFound("Faculty member"));
        // subjects stay, they just become unassigned
        subjects.findByFacultyId(id).forEach(s -> s.setFaculty(null));
        faculties.delete(f);
    }

    // ------------------------------------------------------------ subjects
    public List<SubjectResponse> listSubjects() {
        return subjects.findAllByOrderByCode().stream().map(this::toDto).toList();
    }

    @Transactional
    public SubjectResponse createSubject(SubjectRequest r) {
        String code = r.code().trim().toUpperCase();
        if (subjects.existsByCode(code)) {
            throw new ApiException(HttpStatus.CONFLICT, "Subject code " + code + " already exists");
        }
        Subject s = Subject.builder().code(code).name(r.name().trim())
                .department(r.department().trim()).semester(r.semester())
                .faculty(findFaculty(r.facultyId())).build();
        return toDto(subjects.save(s));
    }

    @Transactional
    public SubjectResponse updateSubject(Long id, SubjectRequest r) {
        Subject s = subjects.findById(id).orElseThrow(() -> ApiException.notFound("Subject"));
        s.setName(r.name().trim());
        s.setDepartment(r.department().trim());
        s.setSemester(r.semester());
        s.setFaculty(findFaculty(r.facultyId()));
        return toDto(subjects.save(s));
    }

    @Transactional
    public void deleteSubject(Long id) {
        Subject s = subjects.findById(id).orElseThrow(() -> ApiException.notFound("Subject"));
        attendance.deleteBySubjectId(id);
        subjects.delete(s);
    }

    // ------------------------------------------------------------ helpers
    private Faculty findFaculty(Long id) {
        if (id == null) return null;
        return faculties.findById(id).orElseThrow(() -> ApiException.notFound("Faculty member"));
    }

    private static boolean isBlank(String s) { return s == null || s.isBlank(); }

    private StudentResponse toDto(Student s) {
        return new StudentResponse(s.getId(), s.getRollNo(), s.getName(), s.getEmail(),
                s.getDepartment(), s.getSemester());
    }

    private FacultyResponse toDto(Faculty f) {
        return new FacultyResponse(f.getId(), f.getUser().getUsername(), f.getName(), f.getEmail(),
                f.getDepartment());
    }

    private SubjectResponse toDto(Subject s) {
        Faculty f = s.getFaculty();
        return new SubjectResponse(s.getId(), s.getCode(), s.getName(), s.getDepartment(), s.getSemester(),
                f == null ? null : f.getId(), f == null ? null : f.getName());
    }
}

package com.college.attendance.service;

import com.college.attendance.dto.Dtos.*;
import com.college.attendance.entity.*;
import com.college.attendance.exception.ApiException;
import com.college.attendance.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttendanceService {
    private final StudentRepository students;
    private final FacultyRepository faculties;
    private final SubjectRepository subjects;
    private final AttendanceRepository attendance;

    // ================================================================ faculty
    public List<SubjectResponse> subjectsForFaculty(String username) {
        Faculty f = faculties.findByUserUsername(username)
                .orElseThrow(() -> ApiException.notFound("Faculty profile"));
        return subjects.findByFacultyIdOrderByCode(f.getId()).stream().map(this::toDto).toList();
    }

    /** Roll-call sheet: every student of the subject's class, with any status already saved for the date. */
    public List<SheetRow> sheet(String username, Long subjectId, LocalDate date) {
        Subject subject = ownedSubject(username, subjectId);
        Map<Long, AttendanceStatus> existing = attendance.findBySubjectIdAndDate(subjectId, date).stream()
                .collect(Collectors.toMap(a -> a.getStudent().getId(), Attendance::getStatus));
        return classOf(subject).stream()
                .map(st -> new SheetRow(st.getId(), st.getRollNo(), st.getName(), existing.get(st.getId())))
                .toList();
    }

    /** Creates or updates one record per student for the subject and date. */
    @Transactional
    public int mark(String username, MarkAttendanceRequest req) {
        Subject subject = ownedSubject(username, req.subjectId());
        if (req.date().isAfter(LocalDate.now())) {
            throw ApiException.badRequest("Attendance cannot be marked for a future date");
        }
        int saved = 0;
        for (AttendanceEntry entry : req.records()) {
            if (entry.status() == null) continue;
            Student st = students.findById(entry.studentId())
                    .orElseThrow(() -> ApiException.notFound("Student " + entry.studentId()));
            if (!inClass(st, subject)) {
                throw ApiException.badRequest(st.getRollNo() + " is not enrolled in " + subject.getCode());
            }
            Attendance a = attendance
                    .findByStudentIdAndSubjectIdAndDate(st.getId(), subject.getId(), req.date())
                    .orElseGet(() -> Attendance.builder().student(st).subject(subject).date(req.date()).build());
            a.setStatus(entry.status());
            attendance.save(a);
            saved++;
        }
        return saved;
    }

    // ================================================================ reports
    public List<ReportRow> subjectReport(String username, boolean admin, Long subjectId) {
        Subject subject = admin ? subjectOf(subjectId) : ownedSubject(username, subjectId);
        return buildReport(subject);
    }

    /** Students below the threshold across the subjects the caller can see. */
    public List<ReportRow> lowAttendance(String username, boolean admin, double threshold) {
        List<Subject> visible;
        if (admin) {
            visible = subjects.findAllByOrderByCode();
        } else {
            Faculty f = faculties.findByUserUsername(username)
                    .orElseThrow(() -> ApiException.notFound("Faculty profile"));
            visible = subjects.findByFacultyIdOrderByCode(f.getId());
        }
        return visible.stream()
                .flatMap(s -> buildReport(s).stream())
                .filter(r -> r.total() > 0 && r.percentage() < threshold)
                .sorted(Comparator.comparingDouble(ReportRow::percentage))
                .toList();
    }

    // ================================================================ student
    public StudentSummary summary(String username) {
        Student st = studentOf(username);
        Map<Long, long[]> counts = new HashMap<>(); // subjectId -> [total, present]
        for (Attendance a : attendance.findByStudentIdOrderByDateDesc(st.getId())) {
            long[] c = counts.computeIfAbsent(a.getSubject().getId(), k -> new long[2]);
            c[0]++;
            if (a.getStatus() == AttendanceStatus.PRESENT) c[1]++;
        }
        List<SubjectSummary> rows = new ArrayList<>();
        long total = 0, present = 0;
        for (Subject s : subjects.findByDepartmentIgnoreCaseAndSemesterOrderByCode(st.getDepartment(), st.getSemester())) {
            long[] c = counts.getOrDefault(s.getId(), new long[2]);
            rows.add(new SubjectSummary(s.getId(), s.getCode(), s.getName(), c[0], c[1], pct(c[1], c[0])));
            total += c[0];
            present += c[1];
        }
        return new StudentSummary(st.getRollNo(), st.getName(), st.getDepartment(), st.getSemester(),
                total, present, pct(present, total), rows);
    }

    public List<HistoryItem> history(String username, Long subjectId) {
        Student st = studentOf(username);
        return attendance.findByStudentIdOrderByDateDesc(st.getId()).stream()
                .filter(a -> subjectId == null || a.getSubject().getId().equals(subjectId))
                .map(a -> new HistoryItem(a.getDate(), a.getSubject().getId(), a.getSubject().getCode(),
                        a.getSubject().getName(), a.getStatus()))
                .toList();
    }

    // ================================================================ helpers
    private List<ReportRow> buildReport(Subject subject) {
        Map<Long, long[]> counts = new HashMap<>();
        for (Attendance a : attendance.findBySubjectId(subject.getId())) {
            long[] c = counts.computeIfAbsent(a.getStudent().getId(), k -> new long[2]);
            c[0]++;
            if (a.getStatus() == AttendanceStatus.PRESENT) c[1]++;
        }
        return classOf(subject).stream().map(st -> {
            long[] c = counts.getOrDefault(st.getId(), new long[2]);
            return new ReportRow(st.getId(), st.getRollNo(), st.getName(), subject.getCode(),
                    c[0], c[1], pct(c[1], c[0]));
        }).toList();
    }

    private List<Student> classOf(Subject s) {
        return students.findByDepartmentIgnoreCaseAndSemesterOrderByRollNo(s.getDepartment(), s.getSemester());
    }

    private boolean inClass(Student st, Subject s) {
        return st.getDepartment().equalsIgnoreCase(s.getDepartment()) && st.getSemester() == s.getSemester();
    }

    private Subject subjectOf(Long id) {
        return subjects.findById(id).orElseThrow(() -> ApiException.notFound("Subject"));
    }

    private Subject ownedSubject(String username, Long subjectId) {
        Subject s = subjectOf(subjectId);
        if (s.getFaculty() == null || !s.getFaculty().getUser().getUsername().equals(username)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "This subject is not assigned to you");
        }
        return s;
    }

    private Student studentOf(String username) {
        return students.findByUserUsername(username).orElseThrow(() -> ApiException.notFound("Student profile"));
    }

    private static double pct(long present, long total) {
        return total == 0 ? 0 : Math.round(present * 10000.0 / total) / 100.0;
    }

    private SubjectResponse toDto(Subject s) {
        Faculty f = s.getFaculty();
        return new SubjectResponse(s.getId(), s.getCode(), s.getName(), s.getDepartment(), s.getSemester(),
                f == null ? null : f.getId(), f == null ? null : f.getName());
    }
}

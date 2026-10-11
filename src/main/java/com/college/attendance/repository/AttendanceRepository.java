package com.college.attendance.repository;

import com.college.attendance.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    Optional<Attendance> findByStudentIdAndSubjectIdAndDate(Long studentId, Long subjectId, LocalDate date);
    List<Attendance> findBySubjectIdAndDate(Long subjectId, LocalDate date);
    List<Attendance> findBySubjectId(Long subjectId);
    List<Attendance> findByStudentIdOrderByDateDesc(Long studentId);
    void deleteByStudentId(Long studentId);
    void deleteBySubjectId(Long subjectId);
}

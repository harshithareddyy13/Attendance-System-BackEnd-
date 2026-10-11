package com.college.attendance.repository;

import com.college.attendance.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubjectRepository extends JpaRepository<Subject, Long> {
    boolean existsByCode(String code);
    List<Subject> findAllByOrderByCode();
    List<Subject> findByFacultyIdOrderByCode(Long facultyId);
    List<Subject> findByFacultyId(Long facultyId);
    List<Subject> findByDepartmentIgnoreCaseAndSemesterOrderByCode(String department, int semester);
}

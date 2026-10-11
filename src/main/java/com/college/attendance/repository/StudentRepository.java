package com.college.attendance.repository;

import com.college.attendance.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {
    boolean existsByRollNo(String rollNo);
    Optional<Student> findByUserUsername(String username);
    List<Student> findByDepartmentIgnoreCaseAndSemesterOrderByRollNo(String department, int semester);
    List<Student> findAllByOrderByRollNo();
}

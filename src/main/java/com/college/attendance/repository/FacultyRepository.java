package com.college.attendance.repository;

import com.college.attendance.entity.Faculty;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FacultyRepository extends JpaRepository<Faculty, Long> {
    Optional<Faculty> findByUserUsername(String username);
    List<Faculty> findAllByOrderByName();
}

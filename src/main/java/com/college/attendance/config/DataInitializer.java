package com.college.attendance.config;

import com.college.attendance.entity.*;
import com.college.attendance.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** Seeds an admin account plus a little demo data the first time the database is empty. */
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private final UserRepository users;
    private final FacultyRepository faculties;
    private final StudentRepository students;
    private final SubjectRepository subjects;
    private final PasswordEncoder encoder;

    @Override
    public void run(String... args) {
        if (users.count() > 0) return;

        users.save(User.builder().username("admin").password(encoder.encode("admin123"))
                .fullName("System Administrator").role(Role.ADMIN).build());

        Faculty meera = faculties.save(Faculty.builder().name("Dr. Meera Rao").email("meera.rao@college.edu")
                .department("CSE")
                .user(User.builder().username("faculty1").password(encoder.encode("faculty123"))
                        .fullName("Dr. Meera Rao").role(Role.FACULTY).build()).build());

        subjects.save(Subject.builder().code("CS301").name("Data Structures").department("CSE").semester(3).faculty(meera).build());
        subjects.save(Subject.builder().code("CS302").name("Database Management Systems").department("CSE").semester(3).faculty(meera).build());
        subjects.save(Subject.builder().code("CS303").name("Operating Systems").department("CSE").semester(3).faculty(meera).build());

        String[][] demo = {
                {"21CSE001", "Aarav Sharma"}, {"21CSE002", "Diya Patel"}, {"21CSE003", "Rohan Gupta"},
                {"21CSE004", "Sneha Reddy"}, {"21CSE005", "Karthik Iyer"}, {"21CSE006", "Ishita Nair"}
        };
        for (String[] d : demo) {
            students.save(Student.builder().rollNo(d[0]).name(d[1]).email(d[1].split(" ")[0].toLowerCase() + "@college.edu")
                    .department("CSE").semester(3)
                    .user(User.builder().username(d[0]).password(encoder.encode(d[0]))
                            .fullName(d[1]).role(Role.STUDENT).build()).build());
        }
    }
}

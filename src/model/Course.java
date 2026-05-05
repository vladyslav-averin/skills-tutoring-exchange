package model;

import java.util.ArrayList;
import java.util.List;
import java.io.Serializable;

public class Course implements Serializable {
    private String name;
    private String information;
    private Student tutor; // The student who provides the course
    private List<Student> enrolledStudents; // Students enrolled in the course

    public Course(String name, String information, Student tutor) {
        this.name = name;
        this.information = information;
        this.tutor = tutor;
        this.enrolledStudents = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInformation() {
        return information;
    }

    public void setInformation(String information) {
        this.information = information;
    }

    public Student getTutor() {
        return tutor;
    }

    public void setTutor(Student tutor) {
        this.tutor = tutor;
    }

    public List<Student> getEnrolledStudents() {
        return enrolledStudents;
    }

    public void addEnrolledStudent(Student student) {
        if (!this.enrolledStudents.contains(student)) {
            this.enrolledStudents.add(student);
            student.enrollInCourse(this);
        }
    }

    public void removeEnrolledStudent(Student student) {
        this.enrolledStudents.remove(student);
    }

    @Override
    public String toString() {
        return name + " (Tutor: " + tutor.getName() + ") - " + information;
    }
}

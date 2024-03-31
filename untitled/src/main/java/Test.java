import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class Test {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/cms";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "Quincychunk91!";

    public static void main(String[] args) {
        try {

            Class.forName("com.mysql.cj.jdbc.Driver");
            // Connect to the database
            Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

            // Generate reports
            generateCourseReport(connection);
            generateStudentReport(connection);
            generateLecturerReport(connection);

            // Close the connection
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void generateCourseReport(Connection connection) {
        try {
            // Retrieve data for Course Report
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT \n" +
                    "    modules.module_name AS module_name,\n" +
                    "    courses.course_programme AS programme,\n" +
                    "    COUNT(enrollments.student_id) AS num_students_enrolled,\n" +
                    "    lecturers.lecturer_name AS lecturer,\n" +
                    "    COALESCE(modules.room, 'online') AS room\n" +
                    "FROM \n" +
                    "    modules\n" +
                    "JOIN \n" +
                    "    courses ON modules.course_id = courses.course_id\n" +
                    "LEFT JOIN \n" +
                    "    enrollments ON modules.module_id = enrollments.module_id\n" +
                    "LEFT JOIN \n" +
                    "    lecturers ON modules.lecturer_id = lecturers.lecturer_id\n" +
                    "GROUP BY \n" +
                    "    modules.module_id;");

            // Generate Course Report
            // Write to txt file
            FileWriter fileWriter = new FileWriter("CourseReport.txt");
            PrintWriter printWriter = new PrintWriter(fileWriter);
            // Format and write report content
            while (resultSet.next()) {
                // Format report content
                String reportContent = resultSet.getString("module_name") + ", " + resultSet.getString("programme") + ", " +
                        resultSet.getInt("num_students_enrolled") + ", " + resultSet.getString("lecturer") + ", " +
                        resultSet.getString("room");
                // Write report content to txt file
                printWriter.println(reportContent);
            }
            printWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void generateStudentReport(Connection connection) {
        try {
            // Retrieve data for Student Report
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT \n" +
                    "    students.student_id,\n" +
                    "    students.student_name,\n" +
                    "    students.student_number,\n" +
                    "    courses.course_programme,\n" +
                    "    GROUP_CONCAT(DISTINCT modules.module_name ORDER BY modules.module_name SEPARATOR ', ') AS enrolled_modules,\n" +
                    "    GROUP_CONCAT(DISTINCT CONCAT(modules.module_name, ': ', grades.grade) ORDER BY modules.module_name SEPARATOR ', ') AS completed_modules\n" +
                    "FROM \n" +
                    "    students\n" +
                    "JOIN \n" +
                    "    enrollments ON students.student_id = enrollments.student_id\n" +
                    "JOIN \n" +
                    "    modules ON enrollments.module_id = modules.module_id\n" +
                    "JOIN \n" +
                    "    courses ON modules.course_id = courses.course_id\n" +
                    "LEFT JOIN \n" +
                    "    grades ON enrollments.enrollment_id = grades.enrollment_id\n" +
                    "GROUP BY \n" +
                    "    students.student_id,\n" +
                    "    students.student_name,\n" +
                    "    students.student_number,\n" +
                    "    courses.course_programme;\n");

            // Generate Student Report
            // Write to txt file
            FileWriter fileWriter = new FileWriter("StudentReport.txt");
            PrintWriter printWriter = new PrintWriter(fileWriter);
            // Format and write report content
            while (resultSet.next()) {
                // Format report content
                String reportContent = resultSet.getString("student_name") + ", " + resultSet.getString("student_number") + ", " +
                        resultSet.getString("course_programme") + ", " + resultSet.getString("enrolled_modules") + ", " +
                        resultSet.getString("completed_modules");
                // Write report content to txt file
                printWriter.println(reportContent);
            }
            printWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void generateLecturerReport(Connection connection) {
        try {
            // Retrieve data for Lecturer Report
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT \n" +
                    "    lecturers.lecturer_name AS lecturer_name,\n" +
                    "    lecturers.lecturer_role AS lecturer_role,\n" +
                    "    GROUP_CONCAT(DISTINCT modules.module_name ORDER BY modules.module_name SEPARATOR ', ') AS teaching_modules,\n" +
                    "    COUNT(DISTINCT enrollments.student_id) AS num_students,\n" +
                    "    lecturers.lecturer_specialization AS specialization\n" +
                    "FROM \n" +
                    "    lecturers\n" +
                    "JOIN \n" +
                    "    modules ON lecturers.lecturer_id = modules.lecturer_id\n" +
                    "JOIN \n" +
                    "    enrollments ON modules.module_id = enrollments.module_id\n" +
                    "GROUP BY \n" +
                    "    lecturers.lecturer_id;\n");

            // Generate Lecturer Report
            // Write to txt file
            FileWriter fileWriter = new FileWriter("LecturerReport.txt");
            PrintWriter printWriter = new PrintWriter(fileWriter);
            // Format and write report content
            while (resultSet.next()) {
                // Format report content
                String reportContent = resultSet.getString("lecturer_name") + ", " + resultSet.getString("lecturer_role") + ", " +
                        resultSet.getString("teaching_modules") + ", " + resultSet.getInt("num_students") + ", " +
                        resultSet.getString("specialization");
                // Write report content to txt file
                printWriter.println(reportContent);
            }
            printWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // Similar methods for generating Student Report and Lecturer Report
    // Implement generateStudentReport() and generateLecturerReport() methods
}

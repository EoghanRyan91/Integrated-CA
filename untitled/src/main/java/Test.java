import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

public class Test {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/cms";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "Quincychunk91!";

    public static void main(String[] args) {
        try {
            // Load the MySQL JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Establish connection to the database
            Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

            // Prompt the user to choose the report format
            Scanner scanner = new Scanner(System.in);
            System.out.println("Choose report format:");
            System.out.println("1. TXT");
            System.out.println("2. CSV");
            System.out.println("3. Console");
            int choice = scanner.nextInt();

            // Determine the report format based on user's choice
            ReportFormat format;
            switch (choice) {
                case 1:
                    format = ReportFormat.TXT;
                    break;
                case 2:
                    format = ReportFormat.CSV;
                    break;
                case 3:
                    format = ReportFormat.CONSOLE;
                    break;
                default:
                    System.out.println("Invalid choice, defaulting to console output.");
                    format = ReportFormat.CONSOLE;
                    break;
            }

            // Generate reports based on the chosen format
            generateCourseReport(connection, format);
            generateStudentReport(connection, format);
            generateLecturerReport(connection, format);

            // Close the database connection
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Enum to represent different report formats
    public enum ReportFormat {
        TXT, CSV, CONSOLE
    }

    // Method to generate course report
    public static void generateCourseReport(Connection connection, ReportFormat format) {
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT modules.module_name AS module_name, " +
                     "courses.course_programme AS programme, " +
                     "COUNT(enrollments.student_id) AS num_students_enrolled, " +
                     "lecturers.lecturer_name AS lecturer, " +
                     "COALESCE(modules.room, 'online') AS room " +
                     "FROM modules " +
                     "JOIN courses ON modules.course_id = courses.course_id " +
                     "LEFT JOIN enrollments ON modules.module_id = enrollments.module_id " +
                     "LEFT JOIN lecturers ON modules.lecturer_id = lecturers.lecturer_id " +
                     "GROUP BY modules.module_id")) {

            writeReport(resultSet, "CourseReport", format);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Method to generate student report
    public static void generateStudentReport(Connection connection, ReportFormat format) {
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT students.student_name, " +
                     "students.student_number, " +
                     "courses.course_programme, " +
                     "GROUP_CONCAT(DISTINCT modules.module_name ORDER BY modules.module_name SEPARATOR ', ') AS enrolled_modules, " +
                     "GROUP_CONCAT(DISTINCT CONCAT(modules.module_name, ': ', grades.grade) ORDER BY modules.module_name SEPARATOR ', ') AS completed_modules " +
                     "FROM students " +
                     "JOIN enrollments ON students.student_id = enrollments.student_id " +
                     "JOIN modules ON enrollments.module_id = modules.module_id " +
                     "JOIN courses ON modules.course_id = courses.course_id " +
                     "LEFT JOIN grades ON enrollments.enrollment_id = grades.enrollment_id " +
                     "GROUP BY students.student_id, students.student_name, students.student_number, courses.course_programme")) {

            writeReport(resultSet, "StudentReport", format);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Method to generate lecturer report
    public static void generateLecturerReport(Connection connection, ReportFormat format) {
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT lecturers.lecturer_name, " +
                     "lecturers.lecturer_role, " +
                     "GROUP_CONCAT(DISTINCT modules.module_name ORDER BY modules.module_name SEPARATOR ', ') AS teaching_modules, " +
                     "COUNT(DISTINCT enrollments.student_id) AS num_students, " +
                     "lecturers.lecturer_specialization " +
                     "FROM lecturers " +
                     "JOIN modules ON lecturers.lecturer_id = modules.lecturer_id " +
                     "JOIN enrollments ON modules.module_id = enrollments.module_id " +
                     "GROUP BY lecturers.lecturer_id")) {

            writeReport(resultSet, "LecturerReport", format);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Method to write report data based on the chosen format
    public static void writeReport(ResultSet resultSet, String fileName, ReportFormat format) throws SQLException {
        try {
            PrintWriter printWriter;
            if (format == ReportFormat.TXT) {
                printWriter = new PrintWriter(new FileWriter(fileName + ".txt"));
                writeTxtReport(resultSet, printWriter);
            } else if (format == ReportFormat.CSV) {
                printWriter = new PrintWriter(new FileWriter(fileName + ".csv"));
                writeCsvReport(resultSet, printWriter);
            } else {
                writeConsoleReport(resultSet);
            }
            System.out.println("Report has been written to " + fileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Method to write report data to a text file
    public static void writeTxtReport(ResultSet resultSet, PrintWriter printWriter) throws SQLException {
        while (resultSet.next()) {
            printWriter.println(resultSet.getString(1) + ", " +
                    resultSet.getString(2) + ", " +
                    resultSet.getString(3) + ", " +
                    resultSet.getString(4) + ", " +
                    resultSet.getString(5));
        }
        printWriter.close();
    }

    // Method to write report data to a CSV file
    public static void writeCsvReport(ResultSet resultSet, PrintWriter printWriter) throws SQLException {
        while (resultSet.next()) {
            printWriter.println(resultSet.getString(1) + "," +
                    resultSet.getString(2) + "," +
                    resultSet.getString(3) + "," +
                    resultSet.getString(4) + "," +
                    resultSet.getString(5));
        }
        printWriter.close();
    }

    // Method to write report data to the console
    public static void writeConsoleReport(ResultSet resultSet) throws SQLException {
        while (resultSet.next()) {
            System.out.println(resultSet.getString(1) + ", " +
                    resultSet.getString(2) + ", " +
                    resultSet.getString(3) + ", " +
                    resultSet.getString(4) + ", " +
                    resultSet.getString(5));
        }
    }
}

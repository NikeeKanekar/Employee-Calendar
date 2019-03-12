package main;

import org.bson.Document;
import org.json.JSONException;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static org.junit.Assert.*;

public class EmployeeControllerTest {

    EmployeeController emp = new EmployeeController();

    @Test
    public void getBalanceHours() {
        assertEquals(emp.getBalanceHours(102), 80, 0);
    }

    @Test
    public void getBalanceHoursForNewEmployee() {
        assertEquals(emp.getBalanceHours(100), 120, 0);
    }

    @Test
    public void requestTimeOffSuccessful() throws ParseException, JSONException {

        Employee employee = new Employee(101, "03-16-2019 9:00", "03-20-2019 17:00");
        assertEquals(emp.requestTimeOff(employee),  new ResponseEntity<>("Request Successful", HttpStatus.OK));

    }

    @Test
    public void requestTimeOffUnsuccessful() throws ParseException, JSONException {

        Employee employee = new Employee(103, "03-29-2019 13:00", "04-02-2019 17:00");
        assertEquals(emp.requestTimeOff(employee),  new ResponseEntity<>("Request Unsuccesful : Not enough balance", HttpStatus.NOT_FOUND));

    }

    @Test
    public void requestTimeOffAlreadyExists() throws ParseException, JSONException {

        Employee employee = new Employee(102, "03-05-2019 13:30", "03-08-2019 11:30");
        assertEquals(emp.requestTimeOff(employee),  new ResponseEntity<>("Request Unsuccessful: Time requested overlaps with existing timeoff", HttpStatus.NOT_FOUND));

    }

    @Test
    public void getTimeOffList() throws ParseException {

        SimpleDateFormat formatter = new SimpleDateFormat("MM-dd-yyyy HH:mm");
        Date sdate = formatter.parse("03-02-2019 13:30");
        Date edate = formatter.parse("03-10-2019 11:30");

        ArrayList<Document> list = new ArrayList<Document>();
        Document doc = new Document("start", sdate)
                .append("end",edate);

        list.add(doc);
        assertEquals(emp.getTimeOffList(102),list);
    }

    @Test
    public void getTotalHoursWithStartSaturdayEndWeekday() throws ParseException {

        String start ="03-02-2019 13:30";
        String end ="03-04-2019 15:30";
        assertEquals(emp.getTotalHours(start,end), 6.5, 0);
    }

    @Test
    public void getTotalHoursWithSaturdayAndTwoDays() throws ParseException {

        String start ="12-29-2018 09:00";
        String end ="01-01-2019 17:00";
        assertEquals(emp.getTotalHours(start,end), 16, 0);
    }

    @Test
    public void getTotalHoursWithStartSundayEndWeekday() throws ParseException {

        String start ="03-03-2019 13:30";
        String end ="03-04-2019 17:00";
        assertEquals(emp.getTotalHours(start,end), 8, 0);
    }

    @Test
    public void getTotalHoursWithSundayAndTwoDays() throws ParseException {

        String start ="03-03-2019 13:30";
        String end ="03-05-2019 13:30";
        assertEquals(emp.getTotalHours(start,end), 12.5, 0);
    }

    @Test
    public void getTotalHoursWithStartSaturdayEndSunday() throws ParseException {

        String start ="03-02-2019 13:30";
        String end ="03-10-2019 13:30";
        assertEquals(emp.getTotalHours(start,end), 40, 0);
    }

    @Test
    public void getTotalHoursWithStartSaturdayEndSaturday() throws ParseException {

        String start ="03-02-2019 13:30";
        String end ="03-09-2019 13:30";
        assertEquals(emp.getTotalHours(start,end), 40, 0);
    }

    @Test
    public void getTotalHoursWithStartSundayEndSunday() throws ParseException {

        String start ="03-03-2019 13:30";
        String end ="03-10-2019 13:30";
        assertEquals(emp.getTotalHours(start,end), 40, 0);
    }

    @Test
    public void getTotalHoursWithNoWeekday() throws ParseException {

        String start ="03-02-2019 13:30";
        String end ="03-03-2019 13:30";
        assertEquals(emp.getTotalHours(start,end), 0, 0);
    }

    @Test
    public void getTotalHoursWithStartFridayEndMonday() throws ParseException {

        String start ="03-01-2019 13:30";
        String end ="03-04-2019 17:00";
        assertEquals(emp.getTotalHours(start,end), 11.5, 0);
    }

    @Test
    public void getTotalHoursWithStartAndEndWeekday() throws ParseException {

        String start ="03-04-2019 13:30";
        String end ="03-08-2019 13:30";
        assertEquals(emp.getTotalHours(start,end), 32, 0);
    }
}
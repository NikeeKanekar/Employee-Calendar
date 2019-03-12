package main;

import java.util.Date;

public class Employee {

    private int employee_id;
    private String start;
    private String end;

    public Employee(int employee_id, String start_date, String end_date) {
        this.employee_id = employee_id;
        this.start = start_date;
        this.end = end_date;
    }

    public int getEmployee_id() {
        return employee_id;
    }

    public String getStart() {
        return start;
    }

    public String getEnd() {
        return end;
    }
}

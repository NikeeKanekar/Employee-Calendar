package main;

import com.mongodb.*;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.json.JSONException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;


@RestController
@RequestMapping("/timeoff")
public class EmployeeController {

    MongoClient mongoClient = new MongoClient("127.0.0.1", 27017);

    MongoDatabase database = mongoClient.getDatabase("EmployeeDB");

    MongoCollection<Document> collection = database.getCollection("EmployeeCollection");

    @RequestMapping(value="/query", method = RequestMethod.GET)
    public double getBalanceHours(@RequestParam(value="employee_id") int e_id)
    {
        BasicDBObject whereQuery = new BasicDBObject("employee_id", e_id);
        MongoCursor<Document> cursor = collection.find(whereQuery).iterator();
        double balance = 120;

        if(cursor.hasNext()) {
            Document emp = cursor.next();
            balance = (double) emp.get("balance");
        }

        return balance;
    }

    @RequestMapping(value="/request", method = RequestMethod.POST, consumes = "application/json")
    public ResponseEntity<?> requestTimeOff(@RequestBody Employee employee) throws ParseException, JSONException {

        int e_id = employee.getEmployee_id();
        String start = employee.getStart();
        String end = employee.getEnd();

        SimpleDateFormat formatter = new SimpleDateFormat("MM-dd-yyyy HH:mm");
        Date sdate = formatter.parse(start);
        Date edate = formatter.parse(end);

        double finalhours = getTotalHours(start,end);
        double balance = 120;
        boolean overlap = false;

        ArrayList<Document> timeoffList = new ArrayList<Document>();
        Document doc = new Document("start", sdate)
               .append("end",edate);
        BasicDBObject whereQuery = new BasicDBObject("employee_id", e_id);
        MongoCursor<Document> cursor = collection.find(whereQuery).iterator();

        if(cursor.hasNext()) {
            Document emp = cursor.next();
            balance = (double) emp.get("balance");
            timeoffList = (ArrayList<Document>) emp.get("timeoff");
            Date stdate = null,endate=null;
            for(Document d : timeoffList)
            {
                stdate = d.getDate("start");
                endate = d.getDate("end");

                if((stdate.before(edate) || stdate.equals(edate)) && (endate.after(sdate) || endate.equals(sdate))){
                    overlap = true;
                }
            }

            if(!overlap) {
                if ((balance - finalhours) > 0) {
                    balance = balance - finalhours;
                    timeoffList.add(doc);
                    collection.updateOne(Filters.eq("employee_id", e_id), Updates.combine(
                            Updates.set("timeoff", timeoffList),
                            Updates.set("balance", balance)
                    ));
                } else {
                    return new ResponseEntity<>("Request Unsuccesful : Not enough balance", HttpStatus.NOT_FOUND);
                }
            }
            else{
                return new ResponseEntity<>("Request Unsuccessful: Time requested overlaps with existing timeoff", HttpStatus.NOT_FOUND);
            }
        }
        else
        {
            if ((balance - finalhours) > 0) {
                balance = balance - finalhours;
                timeoffList.add(doc);
                Document newemp = new Document("employee_id", e_id)
                        .append("timeoff", timeoffList)
                        .append("balance", balance);
                collection.insertOne(newemp);

            } else {
                return new ResponseEntity<>("Request Unsuccesful : Not enough balance", HttpStatus.NOT_FOUND);
            }
        }
            return new ResponseEntity<>("Request Successful", HttpStatus.OK);

    }

    @RequestMapping(value="/list", method = RequestMethod.GET)
    public ArrayList<Document> getTimeOffList(@RequestParam(value="employee_id") int e_id){

        BasicDBObject findQuery = new BasicDBObject("employee_id", e_id);
        MongoCursor<Document> cursor = collection.find(findQuery).iterator();
        ArrayList<Document> list = new ArrayList<Document>();

        if(cursor.hasNext()) {
            Document emp = cursor.next();
            list = (ArrayList<Document>) emp.get("timeoff");
        }

        return list;
    }

    public long numberOfWeekends(Date start, Date end) throws ParseException {

        Calendar c = Calendar.getInstance();
        Calendar s = Calendar.getInstance();
        s.setTime(start);
        Calendar e = Calendar.getInstance();
        e.setTime(end);
        long days=0;

        if((s.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || s.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) &&
                (e.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || e.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)){
            days = days - 2;}
        else if((s.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || s.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) ||
                (e.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || e.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)){
            days = days - 1;}

        while(!start.equals(end))
        {
            c.setTime(start);
            if(c.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || c.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)
                days++;
            c.add(Calendar.DATE, 1);
            start = c.getTime();
        }

        if(c.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || c.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)
            days++;

        return days;
    }

    public double getHours(Date startdate, Date enddate, String start, String end) throws ParseException {

        double hours1 =0, hours2 =0;
        Calendar c = Calendar.getInstance();
        SimpleDateFormat hourFormat = new SimpleDateFormat("HH:mm");

        c.setTime(startdate);
        if(c.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY && c.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
            Date starttime = hourFormat.parse(start.substring(11));
            Date defaultStart = hourFormat.parse("17:00");
            long difference1 = defaultStart.getTime() - starttime.getTime();
            hours1 = (double) difference1 / (1000 * 60 * 60);
        }

        c.setTime(enddate);
        if(c.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY && c.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
            Date endtime = hourFormat.parse(end.substring(11));
            Date defaultEnd = hourFormat.parse("9:00");
            long difference2 = endtime.getTime() - defaultEnd.getTime();
            hours2 = (double) difference2 / (1000 * 60 * 60);
        }

        return hours1 + hours2;
    }

    public double getTotalHours(String start, String end) throws ParseException {

        SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy");
        Date startdate = dateFormat.parse(start);
        Date enddate = dateFormat.parse(end);

        long weekends = numberOfWeekends(startdate,enddate);
        long days = ((enddate.getTime() - startdate.getTime()) / (1000 * 60 * 60 * 24)) - weekends;
        double hours = getHours(startdate,enddate,start,end);

        System.out.println(weekends + "  " + hours);

        double finalhours=0;

        if(startdate.equals(enddate) || days <= 0)
            finalhours = hours;
        else
            finalhours = ((days-1)*8) + hours;

        return finalhours;
    }

}

# Employee-Calendar

Build a web application for managing the holiday calendar and time off requests for employees.

a) GET - <base-url>/timeoff/query

Given an employee id, it will tell you the balance time off hours for the employee.

Ex: http://localhost:8080/timeoff/query?employee_id=103

b) POST <base-url>/timeoff/request

Given an employee id and hours requested, this checks the remaining time off hours and returns success if there is enough balance else returns failure.

Ex: http://localhost:8080/timeoff/request

The employee id and hours requested are sent through a request body, that is an object of Employee class. The Employee class has following 3 instance properties:
i.	int employee_id
ii.	String start
iii.	String end

The two strings represent the start and end date respectively and are of the form “MM-dd-yyyy HH:mm”

Example: employee_id: 103; start: 03-01-2019 13:30; end: 03-05-2019 17:00

c) GET <base-url>/timeoff/list

Given an employee id, this list all the time-offs (both past, current and future).

Ex: http://localhost:8080/timeoff/list?employee_id=103

## Database Specifications:

For this application, I have used MongoDB, the NoSQL database.

i.	Create a database with the name EmployeeDB using the command: Use EmployeeDB
ii.	Create a collection with the name EmployeeCollection: db.createCollection(“EmployeeCollection”)

Each employee data is stored as a document consisting of following key value pairs: 

I.	id: default unique id
II.	employee_id: int
III.	timeoff: ArrayList<Document>; where Document has the key value pairs
      i.	start: Date
      ii.	end: Date
IV.	balance: int

Example: An employee record with employee_id = 101

{"_id":{"$oid":"5c86292775bd8c28f9194084"},"employee_id":101,"timeoff":[{"start":{"$date":"2019-03-16T16:00:00.000Z"},"end":{"$date":"2019-03-21T00:00:00.000Z"}}],"balance":96.0}

I have also attached the json file(EmployeeDB.json) containing the final database content after performing the Junit tests.

## Application Specifications:

i.	Each employee has an initial time off balance of 15 days, hence 120 hours.
ii.	For calculating the total requested time off hours, weekends are considered, but public holidays are not considered.
iii.	The time off hours can range between 9 AM and 5 PM. Hence, the start time can at least be 9:00 and end time can max be 17:00, since it’s a 24-hour format.
iv.	In case, a time off request overlaps with existing time off, an error message is displayed.

## Source Code Files:

1.	Application.java – This file contains the main function to run the application.
2.	EmployeeController.java – This file contains the implementation of all REST end points.
3.	Employee.java – The request body is an object of the Employee class.
4.	EmployeeControllerTest.java – This file covers the unit tests.









Shawn Au Code Challenge

This was originally a code challenge for a company application. I figure it would be pretty useful to remind myself of Spring concepts.

Run using the command
mvn spring-boot:run
or
./mvnw spring-boot:run
- might need to install mvn
- works successfully on macOS High Sierra

List of calls:
GET /add/{number}

POST /start-tracking/{number}

DELETE /stop-tracking/{number}

GET /get-moving-average/{number}

GET /get-analytics
{from:[Date], to:[Date]}

POST /reprocess-data

DELETE /clear-data

Please read RestEndpoints.java file for more information regarding each call.

Notes regarding implementation:
- If the application quits, it should be able to recover list upon startup. Due to lack of a db I am using a textfile called backup.txt. 
- NavigableMap used to return a submap if we want data within a time period.
- ArrayList used to keep track of list
- HashMap used to keep track of moving average numbers we want a quick responses for. Basically recalculate with each add. Technically could include all possible sizes but this obviously wouldn't scale - the requirements are to support many adds; not necessarily many reads
- If you have any questions feel free to contact me at shawn.hui.au@gmail.com

Example calls:

curl 'http://localhost:8080/start-tracking/1' -X POST
curl 'http://localhost:8080/start-tracking/2' -X POST
curl 'http://localhost:8080/start-tracking/3' -X POST
curl 'http://localhost:8080/start-tracking/4' -X POST
curl 'http://localhost:8080/start-tracking/5' -X POST

curl 'http://localhost:8080/stop-tracking/5' -X DELETE

curl 'http://localhost:8080/add/1' -X POST
curl 'http://localhost:8080/add/2' -X POST
curl 'http://localhost:8080/add/3' -X POST
curl 'http://localhost:8080/add/4' -X POST
curl 'http://localhost:8080/add/5' -X POST


curl 'http://localhost:8080/get-moving-average/1'
curl 'http://localhost:8080/get-moving-average/2'
curl 'http://localhost:8080/get-moving-average/3'
curl 'http://localhost:8080/get-moving-average/4'
curl 'http://localhost:8080/get-moving-average/5'

curl 'http://localhost:8080/get-analytics?from=2019-01-01-01:00&to=2019-03-01-01:00'
curl 'http://localhost:8080/reprocess-data' -X POST
curl 'http://localhost:8080/clear-data' -X DELETE

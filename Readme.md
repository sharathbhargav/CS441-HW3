# Sharath S Bhargav - HW 3 CS 441
## University of Illinois at Chicago

### Introduction
This repo contains the code for HW 3. There are 3 main components in this repo:
1) Lambda function code
2) GRPC server and client
3) REST server

### Lambda 
Navigate to project folder and run "sbt assembly". This will generate the fat jar that can be used to upload to lambda portal. Use "com.lambda1::handleRequest" as HandleRequest in lambda. Modify IAM roles as shown in video to give access to lambda function to S3 bucket.

Once the lambda function is setup, add a API Gateway trigger and get the URL for triggering lambda and update the application.conf in src/main/resources.

### GRPC

To start the GRPC server run "sbt run" and select the class GrpcServer. This will start the server on 55555 port locally. 

In application.conf change the search_string_time and search_string_interval to values that has to be searched in log files.

Run the client using "sbt run" and selecting GrpcClient. This will return a hash of all messages found or will return a no log found message.

## REST

To start the REST server run "sbt run" and select WebServer. 2 methods are enabled : GET, POST. 

GET method will take input in query parameters with "time" and "interval" as keys. The same keys can be used in POST request body.

Either curl command or Postman can act as clients for the REST server.

Example of GET request = http://localhost:8080/api/log/get?time=2021-11-03%2009:38:55.540&interval=00:00:01.800

where localhost is the location of the server and 8080 is the port sever is running on. 

Example of POST request : http://localhost:8080/api/log/post

The body of the post request will be
{
"time":"2021-11-03 08:39:57.827","interval":"00:00:00.500"
}
the content-type has to be "application/json"

## Working
The lambda function accepts requests with content-type="application/grpc+proto". The proto file "Log.proto" is used to deserialize and serialize input and output. The GrpcServer and REST servers call the API Gateway attached to the lambda function by serializing the input params into protobuf format. The response from the lambda function is also in the same format. 

The GrpcClient calls the remote function on the GRPC server to get the result which can either be a hash or a No log found message. 

The lambda function returns a 404 HTTP error code in case no log messages matching the pattern in given interval is found. This response is converted to a readable message by the GRPC server and REST server to the clients. 

Given a date time string, the lambda function searches the S3 bucket for log file named "LogFileGenerator.<yyyy-mm-dd>.log" and reads the content of that file into memory. It then searches the file using binary search to find the messages with closest time of start and end time. The start time and end time are calculated by subtracting and adding the time "interval" to "time" input. It is assumed that the start and end time do not cross the day boundary for the purpose of this assignment. 

Detailed demo of the working of code can be found in 3 videos in the playlist
https://www.youtube.com/playlist?list=PL-ytm95PAU61YPcTKuUcNbyLa8UT231vl

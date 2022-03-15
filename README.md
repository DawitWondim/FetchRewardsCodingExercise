# FetchRewardsCodingExercise Documentation

This is a backend web service that accepts HTTP requests related to transactions, spending points, and updating payers point balances. The web service was
implemented using Java Spring Boot, Spring Data JPA, and REST. The design of the backend was to have to a model layer, repository layer, and a controller. The 
database was a H2 in memory database. Initially when starting the API the database will be empty. So to test the API, you will need to pass in transactions via
a POST request to fill up the transactions. Once you do that, you will then be able to spend points and view payers balances. I have included some sample JSON
data you can use to test the API at the bottom of this document. 


MODELS: 
Transaction: This is used to store all of the transactions that would be provided by the client. It will contain payer (String), points (int), and timestamp. 
Payer: This is used to store all of the payers added to the API. They will be added whenever a transaction with their name is added. It contains the payers name
(String) and it's points balance (int).
SpendThesePoints: This model is used as the class for the amount of points the client wants to spend. It only contains 1 property and that is points(int).
PointsToSpend: This model is used to hold the data that will be returned for the Spend Points route. It will return all of the payers that spend points and the 
amount of their respective expenditures. 

REPOSITORIES:
TransactionRepository and PayerRepository are used to interact with the Transaction and Payer tables respectivelly. 

Controller:
RewardsController contains all of the endpoints for the web service. Each client response will get mapped to a specific endpoint based on the HTTP status method. The
controller handles GET MAPPING requests to view all Payer balances and Transactions in the database. Additionally, a POST MAPPING request is included to add 
transactions to the API and a PUT MAPPING request is included to update the data in the transactions and payers tables. 



Sample JSON Objects to Add Transactions
{
     "payer":"DANNON",
     "points": 50,
     "timestamp": "1995-01-01T01:00:00Z"
}
{ "payer": "UNILEVER",
  "points": 250,
  "timestamp": "1998-01-01T01:00:00Z"
}
{
    "payer": "DANNON",
    "points": 40,
    "timestamp": "2002-01-01T01:00:00Z"
}


```bash
_  _ ____ _  _ ____    _  _ ____ _  _ ____ _   _ 
|\/| |  | |  | |___ __ |\/| |  | |\ | |___  \_/  
|  | |__|  \/  |___    |  | |__| | \| |___   |   
```

-- geekylogo:)                                                 
                                                 

## Thoughts before starting

Moving money from one account to another is a task full of problems. Apart from usual clean code, immutability, and etc things to talk about, the business logic itself is full of edge cases including parallel transactions, failed transactions, and rollbacks. My main focus was to cover all edge cases simulating distributed env.    


### Frameworks
Requirement to not use `Spring` was a good one :) So i thought not using Spring but using `DropWizard` is a bit dull, hence thought lets go for something i have not used before. `Vert.x` was interesting choice for me, as it comes with scaling and distributing ability. I thought using their in-built event bus can be a good example of simulating offloading movemoney transactions to a different worker. `Vert.x` seems to be very native with non-blocking patterns, so that was helpful. However, as in every java framework to pass the gluecode period, is just a nightmare, so I did spent some time there, almost regretting going for a new framework :) But, hey, techie life... I did use `Guice` for a simple dependency injection. Blending it with `vert.x` was crazy exercise.   

### Java and things
Its pretty standard `Java 13` + `gradle`. I have not blended `lombok`, code looked fine without codgens, also I am not trying to demonstrate how many libs I can mix in. I kept everything immutable, and typesafe as much as possible, hope no refs escaped any scope:)  

### Being Functional with Java
I have used `vavr` to have a bit functional programming. I used to do scala, i know its not fashionable anymore, but you will probably see lots of scala thinking and hoping  and struggling in my code, with java restrictions:)    

### CI is Github
PRs and commits are being checked in [github](https://github.com/tigran10/move-money/actions). Very simple CI for now. More to come here when I will merge E2E. 


### Storage
I have not used any in-memory db impl, as it would only complicate things for test, and just abstracted away `Storage` with simple operations and `List` as a state. Implementation details can be argued, but most of my energy went to other part of solutions, also I thought probably for exercise sake, it is not really important to design cool in-memory storage.   

### Complexity & Locks
[AccountLocker.java](https://github.com/tigran10/move-money/blob/master/src/main/java/com/movemoney/domain/AccountLocker.java) is the magic place. As I said before, complexity of the task, was thinking about edge cases of money transfer, and solving the problem on localised example. One huge issue was concurrent transactions on the same account, with a state in `Storage`. Some kind of locking was necessary, in real life it would be some distribute place, can be even on storage level, but in my life it is [AccountLocker.java](https://github.com/tigran10/move-money/blob/master/src/main/java/com/movemoney/domain/AccountLocker.java) relying [ReentrantLock](https://docs.oracle.com/javase/7/docs/api/java/util/concurrent/locks/ReentrantLock.html). During the transaction, account will be locked, and other threads are going to wait until transaction is done. For the sake of this test, I have kept 256 locks, obviously the more accounts we have, more chances there are to share the same lock. However, worst thing that can happen (hopefully), is unnecessary waiting for different accounts. Again locks size can be configured to any number.    

### Transaction Manager & Locks
The whole computation of movemoney transfer is done inside [TransactionManager.moveMoney(...)](https://github.com/tigran10/move-money/blob/master/src/main/java/com/movemoney/service/TransactionManager.java#L38), everything before and after is obviously usual chain of calls, data transformation, validation and etc. The method orchestrates the logic with Try monads, and basically does 
* Lock accounts before going  ahead with transfer
* Tries to credit source account
* Tries to debit target account
* If any of the previous steps fails, will rollback acounts to previous state
* If credit and debit worked, saves transaction details
* Always unlock the accounts 

### TransactionWorker
On `vert.x` side, I am deploying [MoveMoneyController](https://github.com/tigran10/move-money/blob/master/src/main/java/com/movemoney/app/MoveMoneyController.java) as well as [MoveMoneyWorker](https://github.com/tigran10/move-money/blob/master/src/main/java/com/movemoney/app/MoveMoneyWorker.java). Controller will get the api call with [MoveMoneyInstruction](https://github.com/tigran10/move-money/blob/master/src/main/java/com/movemoney/app/dto/MoveMoneyInstruction.java), and will send an event on a Bus. [MoveMoneyWorker](https://github.com/tigran10/move-money/blob/master/src/main/java/com/movemoney/app/MoveMoneyWorker.java) will get the event, and will process it in a defferent thread, and almost like in a different deployable. I liked it as it simulates real life `pub/sub` and queues. So Api thread is not going to be blocked by moneytransaction handling, and will chat with [MoveMoneyWorker](https://github.com/tigran10/move-money/blob/master/src/main/java/com/movemoney/app/MoveMoneyWorker.java) via messages in a pretty async way.   

### Testing  
I have used `spock`. It was enough for now, so did not brought more toys in. Testing the concurrency and locks was done with usual executors and Futures.
For E2E, i am using own kit from vert.x, and probably will add scalatest with rest assured for e2e bdd (I have it 50% done).

### Error handling
All known and unkown erros will be bubble up to api level. Try monads will be treated respectivly, to deliver the error. Vert.x comes with some stupid limitations handling custom exceptions on the event bus. So I am not fun how it works, however, maybe i am not familiar with vert.x lifehacks, as its pretty new thing for me. 


### How build and run
Requirement to not have container was interesting. I am confused if by container you meant servers or docker containers. My assumption now is you dont want to run docker. Everyone knows docker anyway :D

Requires up to date Java 13.

To build fatjar
```bash

./gradlew clean build
```

To run fatjar on 8181 port. To change the port have a look at `ServiceBinder` class pls.
```
./gradlew runFatJar
```

### Api Endpoints and examples
Really sorry struggling with time for swagger. 


#### Create an account

```bash

% curl -H "Content-Type: application/json"  -d '{"firstName":"Theresa","ongoingBalance":{"value":12.22,"currency":"GBP","displayValue":12.22}}' localhost:8181/api/accounts --verbose

*   Trying ::1...
* TCP_NODELAY set
* Connected to localhost (::1) port 8181 (#0)
> POST /api/accounts HTTP/1.1
> Host: localhost:8181
> User-Agent: curl/7.64.1
> Accept: */*
> Content-Type: application/json
> Content-Length: 94
>
* upload completely sent off: 94 out of 94 bytes
< HTTP/1.1 201 Created
< content-length: 138
<
* Connection #0 to host localhost left intact
{"firstName":"Theresa","ongoingBalance":{"value":12.22,"currency":"GBP","displayValue":12.22},"id":"12e061d6-7527-4d05-9553-3c84d28ed72a"}* 
```

#### Find account(s)
```bash
curl localhost:8181/api/accounts/175ae967-c504-4bc3-b4d3-656ab419e4b0 --verbose
*   Trying ::1...
* TCP_NODELAY set
* Connected to localhost (::1) port 8181 (#0)
> GET /api/accounts/175ae967-c504-4bc3-b4d3-656ab419e4b0 HTTP/1.1
> Host: localhost:8181
> User-Agent: curl/7.64.1
> Accept: */*
>
< HTTP/1.1 200 OK
< content-length: 136
<
* Connection #0 to host localhost left intact
{"firstName":"Theresa","ongoingBalance":{"value":2.00,"currency":"GBP","displayValue":2.00},"id":"175ae967-c504-4bc3-b4d3-656ab419e4b0"}* 


% curl localhost:8181/api/accounts --verbose
*   Trying ::1...
* TCP_NODELAY set
* Connected to localhost (::1) port 8181 (#0)
> GET /api/accounts HTTP/1.1
> Host: localhost:8181
> User-Agent: curl/7.64.1
> Accept: */*
>
< HTTP/1.1 200 OK
< content-length: 414
<
* Connection #0 to host localhost left intact
[{"firstName":"Theresa","ongoingBalance":{"value":12.22,"currency":"GBP","displayValue":12.22},"id":"12e061d6-7527-4d05-9553-3c84d28ed72a"},{"firstName":"Theresa","ongoingBalance":{"value":2.00,"currency":"GBP","displayValue":2.00},"id":"175ae967-c504-4bc3-b4d3-656ab419e4b0"},{"firstName":"Boris","ongoingBalance":{"value":22.44,"currency":"GBP","displayValue":22.44},"id":"7d1c76e3-255b-4e55-a7c6-c278b8450484"}]* Closing connection 0
```

#### Find Transactions for account
```bash

curl localhost:8181/api/accounts/175ae967-c504-4bc3-b4d3-656ab419e4b0/transactions --verbose
*   Trying ::1...
* TCP_NODELAY set
* Connected to localhost (::1) port 8181 (#0)
> GET /api/accounts/175ae967-c504-4bc3-b4d3-656ab419e4b0/transactions HTTP/1.1
> Host: localhost:8181
> User-Agent: curl/7.64.1
> Accept: */*
>
< HTTP/1.1 200 OK
< content-length: 282
<
* Connection #0 to host localhost left intact
[{"sourceAccountId":"175ae967-c504-4bc3-b4d3-656ab419e4b0","targetAccountId":"7d1c76e3-255b-4e55-a7c6-c278b8450484","transactionDate":"2020-03-03T13:03:50.050939","amount":{"value":10.22,"currency":"GBP","displayValue":10.22},"transactionId":"02a9db4b-6ec8-4128-b05e-5758608c8f04"}]*

```

#### Finally, Move Money 

```bash

% curl -H "Content-Type: application/json"  -d '{"sourceAccountId":"175ae967-c504-4bc3-b4d3-656ab419e4b0","targetAccountId":"7d1c76e3-255b-4e55-a7c6-c278b8450484","amount":{"value":10.22,"currency":"GBP"}}' localhost:8181/api/movemoney --verbose

*   Trying ::1...
* TCP_NODELAY set
* Connected to localhost (::1) port 8181 (#0)
> POST /api/movemoney HTTP/1.1
> Host: localhost:8181
> User-Agent: curl/7.64.1
> Accept: */*
> Content-Type: application/json
> Content-Length: 157
>
* upload completely sent off: 157 out of 157 bytes
< HTTP/1.1 201 Created
< content-length: 113
<
* Connection #0 to host localhost left intact
{"status":"amount 10.22 moved from 175ae967-c504-4bc3-b4d3-656ab419e4b0 to 7d1c76e3-255b-4e55-a7c6-c278b8450484"}*
```



### Some limits and things I dont like
* My current E2E tests are not comitted yet, struggling with time to finish them off. But its going to be ready within a day or two.
* MemoryStorage can be better, its on my list to change.
* Changing server port is not done via system variables yet. So requires touching the code, sorry for that :(
* Some vert.x gluecode, I am not sure if its the best way to do it, if you guys happen to use vert.x a lot, and I have missed some cool stuff, sorry for that, feedback pls :)
* Github actions pipeline to follow.
* Warnings at the start of the application, I know about them. Apparently Vertx version that i have used do not have codex for messages traveling on a wire, even if they are jackson ready. I had to create a custom generic codec, and was a bit naughty with reflection. 

# Binance-order-book-app

#### This application is written using java Spring Boot.

##Requirements

1. Java 8 
2. Maven
2. This application uses Influx DB ( A time series database ) so make sure you have influxDB server running in your system.
   Download link is pasted here "https://portal.influxdata.com/downloads/"

#### Steps to start the application

1. Go to the cloned folder
2. cd binanceOrderBookServer
3. mvn clean install
    1. if the build failed with binance dependency, please clone "https://github.com/binance-exchange/binance-java-api.git" in your local repository and do mvn install.
4. cd target
5. java -jar orderBookApp-0.0.1-SNAPSHOT.jar

####Notes:
    Default Configurations:
        Application port: 8090
        Influx DB: 8086
    Few configurations can be altered from application.properties

#### API's 
1. **GET**: ``` http://localhost:8090/cryptoPair ```
 Get all the crypto pairs

2. **POST** ``` http://localhost:8080/marketDepth/{symbol} ```
 Get the market depth of a particular symbol

####Basic WorkFlow
    1. When application boots up, it will create a database and load all the crypto pairs to db.
    2. When a pair is selected from the ui, the current snapshot of the pair is taken using binance api
       and stored in the local cache and the db. Also, the depth event will be started for the pair
    3. UI is listening to the websocket hosted by application to get he changes from the depth events

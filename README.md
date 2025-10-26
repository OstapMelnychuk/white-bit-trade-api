# WhiteBit WebSocket Candle Builder

A Spring Boot service that connects to the WhiteBit WebSocket API to receive real-time cryptocurrency candle updates, process them, and provide structured candlestick data for downstream use.

---

## Features

- Connects to WhiteBit WebSocket (`wss://api.whitebit.com/ws`) automatically after Spring Boot context initialization.  
- Subscribes to candlestick updates for selected markets and intervals (e.g., BTC_USDT, 5-minute candles).
- Builds EMA for those candles 
- Processes incoming candle data and stores or updates the latest candles in memory.  
- Handles WebSocket errors and disconnections with automatic reconnect and exponential backoff.  
- Scheduled ping to keep the connection alive.  

---

## Requirements

- Java 21  
- Maven 3.6+  
- Docker (For DB setup. Dockerfile included in the project) 

---

## Installation

Clone the repository:

```bash
git clone https://github.com/OstapMelnychuk/white-bit-trade-api.git
```

---

## Run

To run just build the project:

```bash
mvn clean install
```

No additional configuration is needed.
In application properties all is configured to match Dockerfile db configuration. If needed they can be changed.

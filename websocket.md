```mermaid
graph TD
    A[客户端] -->|WebSocket连接| B(Netty服务器)
    B --> C[行情生成器]
    C -->|推送数据| B
    B --> D[数据库]
    D -->|历史数据| B
```
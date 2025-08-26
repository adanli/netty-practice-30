#### 一、EventLoop
1. EventLoop调度机制关键路径

| 关键方法                                                                | 具体职责    |
|---------------------------------------------------------------------|---------|
| io.netty.channel.nio.NioEventLoop.run()                             | 事件循环核心  |
| io.netty.channel.nio.NioEventLoop.processSelectedKeys()             | IO事件处理  |
| io.netty.channel.nio.NioEventLoop.processSelectedKeysOptimized()    | 优化的事件处理 |
| io.netty.channel.nio.NioEventLoop.runAllTasks()                     | 运行所有任务  |
| io.netty.util.concurrent.SingleThreadEventExecutor.execute()        | 提交任务    |
| io.netty.util.concurrent.AbstractScheduledEventExecutor.scheduled() | 定时任务调度  |

---
#### 二、ChannelPipeline
1. ChannelPipeline关键初始化路径

| 关键方法                                                    | 具体职责         |
|:--------------------------------------------------------|:-------------|
| io.netty.bootstrap.ServerBootstrap.init()               | Channel初始化   |
| io.netty.channel.DefaultChannelPipeline.<init>()        | Pipeline构建函数 |
| io.netty.channel.DefaultChannelPipeline.addLast()       | 添加处理器        |
| io.netty.channel.ChannelInitializer.initChannel()       | 初始化回调        |
| io.netty.channel.ChannelInitializer.remove()            | 初始化完成后移除自身   |
| io.netty.channel.AbstractChannelHandlerContext.<init>() | 上下文创建        |




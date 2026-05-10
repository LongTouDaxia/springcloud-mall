# 微服务架构学习项目 —— Spring Cloud Alibaba 全家桶

> 这个项目主要是为了实践微服务架构理念，用到了  
> `Spring Cloud Alibaba`、`Nacos`、`Sentinel`、`OpenFeign`、`Gateway`、`RocketMQ`、`skywalking`日志链路追踪等。  
> 这里记录一下各组件的使用方法，防止过几个月又忘了还得重新查资料。

---

## 一、微服务的模块拆分（重点）

整个项目分了好几个模块：

- **各个业务 service 模块**：比如 `user-service`、`product-service`、`order-service`。
- **gateway 模块**：统一入口，路由转发。
- **common 模块**：一开始想把所有公共东西扔进去，后来发现网关模块不能引入 `web-starter`（会和 Gateway 的 netty 冲突），也不能有数据库配置（编译测试会去找数据库连接），所以又把 common 拆成了四个：

| 模块名 | 作用 |
|--------|------|
| `common-api` | 放 FeignClient 接口，以及对应的 DTO、VO |
| `common-web` | 放 web 相关配置：全局异常、拦截器（比如透传 userId 到 UserContext） |
| `common-db` | 放数据库配置：MyBatis-Plus 分页、自动填充创建时间等 |
| `common-core` | 纯工具类：Result、异常枚举、RedisKey 枚举、JWT 工具、UserContext 线程变量 |

拆分起来确实有点绕，但清晰很多。  
如果嫌麻烦也可以少拆一点，只要能排除网关的冲突就行。学习项目嘛，无所谓了，踩坑也是经验。

数据库方面：**每个微服务一个独立的数据库**，对应的表也单独设计。数据库连接信息统一在 Nacos 配置中心里配置，本地只留一个 `application.yaml`（现在新版本不用 `bootstrap.yaml` 也能用 `spring.config.import`）。

---

## 二、Nacos —— 服务注册 + 配置中心

### 2.1 启动 Nacos

```bash
# 下载解压后，在 bin 目录下运行（单机模式）
startup.cmd -m standalone   # Windows
sh startup.sh -m standalone # Linux/Mac
2.2 服务注册配置
每个需要注册的服务，在 application.yaml 里配置：

yaml
spring:
  application:
    name: user-service
  cloud:
    nacos:
      discovery:
        server-addr: 127.0.0.1:8848
        namespace: dev   # 这里是命名空间的 ID，不是名称
      config:
        server-addr: 127.0.0.1:8848
        namespace: dev
        file-extension: yaml
        refresh-enabled: true

# 关键：从 Nacos 拉取配置文件
spring.config.import: optional:nacos:${spring.application.name}.${spring.cloud.nacos.config.file-extension}

server:
  port: 8081
  
```
namespace 用来区分环境（dev/test/prod），实际开发中很实用。

配置中心会去 Nacos 里找 user-service.yaml 这个 dataId，里面可以放数据库、Redis、MQ 等配置。

服务启动类上加上 @EnableDiscoveryClient，就能注册到 Nacos 了。

## 三、OpenFeign —— 远程调用
### 3.1 依赖
在调用方（比如 order-service）引入 spring-cloud-starter-openfeign。

### 3.2 使用步骤
在调用方的启动类 加上 @EnableFeignClients。

写一个接口，比如 ProductClientFeign：
```bash
java
@FeignClient(name = "product-service")   // name 是被调用服务在 Nacos 的服务名
public interface ProductClientFeign {

    @GetMapping("/api/product/{id}")
    ProductDTO getProductById(@PathVariable("id") Long id);
}
```
在需要调用的地方直接注入这个接口，像调用本地方法一样。

注意：@EnableFeignClients 是放在 调用方，不是被调用方。

## 四、Gateway 网关
### 4.1 统一入口

每个服务端口不一样，客户端只需要访问网关的端口（比如 8080），网关根据请求路径前缀路由到对应服务。

### 4.2 动态路由配置
我习惯把路由规则单独放在 gateway-routes.json 里，然后在 application.yaml 中加载。

示例路由（对 /api/seckill/test 做限流）：
```bash
json
{
  "id": "seckill-service-route",
  "order": 0,
  "predicates": [
    {
      "name": "Path",
      "args": { "patterns": "/api/seckill/test" }
    },
    {
      "name": "Method",
      "args": { "methods": "GET" }
    }
  ],
  "filters": [
    {
      "name": "RequestRateLimiter",
      "args": {
        "redis-rate-limiter.replenishRate": 2,
        "redis-rate-limiter.burstCapacity": 5,
        "redis-rate-limiter.requestedTokens": 1,
        "key-resolver": "#{@ipKeyResolver}"
      }
    },
    {
      "name": "StripPrefix",
      "args": { "parts": 1 }
    }
  ],
  "uri": "lb://product-service"
}
```
replenishRate=2：每秒存 2 个令牌

burstCapacity=5：最多攒 5 个令牌

requestedTokens=1：每次请求消耗 1 个令牌

key-resolver：自定义解析器（比如按 IP 或 userId 限流）

网关还会做 JWT 校验，校验通过后往请求头里塞 X-User-Id: 用户id，下游服务从请求头取出来放到 UserContext（线程变量），这样业务代码里直接用 UserContext.getUserId() 就行，不用每个接口都传参。

### 4.3 下游服务自动装配拦截器
每个服务都需要一个拦截器去解析请求头里的 X-User-Id 并塞到 UserContext。
为了不每个服务手写一遍，我把这个拦截器写在 common-web 模块里，然后用 spring.factories 或 @AutoConfiguration 自动装配，其他服务只要依赖 common-web 就自动生效。

## 五、Sentinel —— 限流 + 熔断降级
### 5.1 启动控制台
```bash
bash
java -Dserver.port=8080 -jar sentinel-dashboard-1.8.6.jar
```

### 5.2 网关限流（令牌桶）
上面那个路由示例就是在网关层对 /seckill/test 做限流。
ipKeyResolver 是自己在 GatewayConfig 里定义的 Bean：
```bash
java
@Bean
public KeyResolver ipKeyResolver() {
    return exchange -> Mono.just(exchange.getRequest().getRemoteAddress().getHostName());
}
```
你也可以改成根据 userId 限流。

### 5.3 服务接口限流
对某个服务内部的接口（比如秒杀的下单接口）做限流。
Sentinel 控制台不会保存规则，所以我从 Nacos 拉规则：
```bash
yaml
spring:
  cloud:
    sentinel:
      transport:
        dashboard: localhost:8080
        port: 8719
      eager: true
      datasource:
        flow:
          nacos:
            server-addr: ${spring.cloud.nacos.discovery.server-addr}
            dataId: sentinel-flow-rules.json
            groupId: DEFAULT_GROUP
            namespace: ${spring.cloud.nacos.discovery.namespace}
            rule-type: flow
            data-type: json
```

sentinel-flow-rules.json 内容示例：

```bash
json
[
  {
    "resource": "seckillTest",
    "grade": 1,
    "count": 10,
    "strategy": 0,
    "controlBehavior": 0,
    "clusterMode": false
  },
  {
    "resource": "seckillOrder",
    "grade": 0,
    "count": 10,
    "clusterMode": false
  }
]
```
然后在需要限流的接口上：
```bash
java
@SentinelResource(value = "seckillTest", blockHandler = "blockHandlerMethod", fallback = "fallbackMethod")
```
blockHandler：被限流后的降级方法

fallback：熔断后的降级方法

熔断规则（慢调用比例、异常比例、异常数）直接在 Sentinel 控制台配置即可，也可以同样持久化到 Nacos。

## 六、日志链路追踪 —— SkyWalking
链路追踪我用的是 SkyWalking，不用改代码，直接挂 Java Agent 就行。

### 6.1 服务端启动
下载 SkyWalking APM（9.7.0 稳定版），解压后运行 bin/startup.bat（Windows）或 ./startup.sh（Mac/Linux）。

UI 默认端口 8080，如果被 Sentinel 占用，去 webapp/webapp.yml 改成其他端口（如 8081）。

### 6.2 微服务接入 Agent
下载 SkyWalking Java Agent 包（版本与服务端一致），解压得到 skywalking-agent 文件夹。

在 IDEA 的 VM options 里为每个微服务添加：
```bash
text
-javaagent:D:\path\to\skywalking-agent\skywalking-agent.jar
-Dskywalking.agent.service_name=product-service
-Dskywalking.collector.backend_service=127.0.0.1:11800
```
启动服务，看到 SkyWalking agent begin to install transformer... 说明挂载成功。

### 6.3 日志中打印 traceId
在 pom.xml 添加依赖：
```bash
<dependency>
    <groupId>org.apache.skywalking</groupId>
    <artifactId>apm-toolkit-logback-1.x</artifactId>
    <version>9.5.0</version>
</dependency>
```
创建 src/main/resources/logback-spring.xml，内容：
```bash
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <include resource="org/springframework/boot/logging/logback/defaults.xml"/>

    <!-- 注册 SkyWalking 的 tid 转换器 -->
    <conversionRule conversionWord="tid"
                    converterClass="org.apache.skywalking.apm.toolkit.log.logback.v1.x.LogbackPatternConverter"/>

    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <!-- 注意这里改为 %tid 而不是 %X{tid} -->
            <pattern>%clr(%d{${LOG_DATEFORMAT_PATTERN:-yyyy-MM-dd HH:mm:ss.SSS}}){faint} %clr(${LOG_LEVEL_PATTERN:-%5p}) %clr(${PID:- }){magenta} %clr(---){faint} %clr([%15.15t]){faint} %clr([%tid]){magenta} %clr(%-40.40logger{39}){cyan} %clr(:){faint} %m%n${LOG_EXCEPTION_CONVERSION_WORD:-%wEx}</pattern>
        </encoder>
    </appender>

    <root level="INFO">
        <appender-ref ref="CONSOLE"/>
    </root>
</configuration>
```
调用接口后，控制台日志会显示 [tid:xxx]，且与 SkyWalking UI 中的 traceId 一致。

### 6.4 踩坑提醒
- 移除 Sleuth/Zipkin 依赖，否则会冲突报 Connection refused: localhost:9411。

- 不要用 %clr 颜色，否则在 TraceIdPatternLogbackLayout 下会报错。想加颜色可以直接用 ANSI 码（如 %red、%green）。

- 异步线程、RocketMQ 消费端一般自动传递 traceId，基本不用额外配置。



## 七、RocketMQ 事务消息
RocketMQ 支持分布式事务消息，这是它比 RabbitMQ 强的一个点。

### 7.1 发送事务消息
```bash
java
rocketMQTemplate.sendMessageInTransaction(
    "seckill-topic",   // 事务消息的主题
    MessageBuilder.withPayload(message).build(),
    message            // 这个 arg 会传递给本地事务方法
);
```
### 7.2 实现事务监听器
```bash
java
@RocketMQTransactionListener
public class TransactionListenerImpl implements RocketMQLocalTransactionListener {

    @Override
    public RocketMQLocalTransactionState executeLocalTransaction(Message msg, Object arg) {
        // 执行本地事务，比如扣减库存
        try {
            // 扣库存逻辑...
            return RocketMQLocalTransactionState.COMMIT;
        } catch (Exception e) {
            return RocketMQLocalTransactionState.ROLLBACK;
        }
    }

    @Override
    public RocketMQLocalTransactionState checkLocalTransaction(Message msg) {
        // 回查本地事务状态
        // 根据业务 key 去数据库查是否已执行成功
        return RocketMQLocalTransactionState.COMMIT; // 或 ROLLBACK
    }
}
```
流程：

- 发送半消息（消费者看不到）

- 执行本地事务

- 本地事务成功 -> COMMIT，消费者才能收到消息；本地事务失败 -> ROLLBACK，消息丢弃

如果本地事务执行异常或返回 UNKNOWN，RocketMQ 会定时回查 checkLocalTransaction，直到最终确定状态

### 7.3 消费者
```bash
java
@RocketMQMessageListener(topic = "seckill-topic", consumerGroup = "order-consumer-group")
@Component
public class SeckillOrderConsumer implements RocketMQListener<SeckillOrderMessage> {

    @Override
    public void onMessage(SeckillOrderMessage message) {
        // 处理下单逻辑
    }
}
```
消费者处理失败会重试，重试超过最大次数后需要自己补偿或进死信队列。

## 八、Git 代理（GitHub 连不上）
开代理/VPN 后，Git 需要配置代理才能连 GitHub：

bash
git config --global http.proxy http://127.0.0.1:你的代理端口
git config --global https.proxy http://127.0.0.1:你的代理端口
如果是 Clash 之类的，端口一般是 7890 或 10809。

## 九、Docker 部署
Windows 直接装 Docker Desktop，然后可以用 docker-compose.yml 一键部署后端所有服务：
```bash
yaml
version: '3'
services:
  nacos:
    image: nacos/nacos-server:latest
    ports:
      - "8848:8848"
  redis:
    image: redis:7
    ports:
      - "6379:6379"
  # ... 其他服务
```
运行：
```bash
bash
docker-compose up -d
```
如果拉镜像太慢，记得配置国内镜像加速器（阿里云或中科大）。
真正部署的时候买台云服务器，把 docker-compose.yml 和配置传上去，运行即可，有公网 IP 别人就能访问了。

# 总结
这个项目做完，基本把微服务体系里常用的东西都摸了一遍：
服务拆分、Nacos 注册+配置、Feign 调用、网关限流+鉴权、Sentinel 熔断降级、链路追踪、RocketMQ 事务消息、Docker 部署。

踩过的坑下次就知道了，记录下来省的再查资料。
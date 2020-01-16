###spring cloud脚手架

####模块分类说明：

- Eureka:服务注册中心。通过Eureka可以监控各个服务的运行状态。它具有如下几个角色：
    - Eureka server：提供服务注册于发现
    - service provider：服务提供方。将自身注册到eureka，工消费者使用
    - service consumer：服务消费方，从eureka中获取服务器提供发列表，从而消费服务
    - ![](https://www.tycoding.cn/2019/05/30/cloud/cloud-template-api/QQ20190518-195930.png)
    - 问题引入：
    
    前端APP发送一个请求，此请求需要调用某个服务的接口，比如此时有两个订单服务、一个派送服务 ( 因为要考虑并发，所以通常服务不止一个，而是一个集群 )。上图中前端APP直接请求某个具体服务的接口，如果后端服务集群非常庞大，前端就要记录很多服务的IP地址。并且对于同一服务的集群配置情况，前端APP还需要自行判断到底调用哪个服务。
    - 解决办法：
    
    显然上述的方式是不可取的。按照之前开发SSM框架前后端不分离时，通常页面直接请求的内部服务接口，从不考虑服务的具体IP地址，因为默认使用的此服务的IP地址。那么在微服务架构中也应该这样设计。所以我们加一个GateWay网关服务 ( 后面讲 )，前端APP直接请求请求网关，仅需要记录网关的IP地址即可，这样就将后端服务接口寻址调用的工作交给了服务端完成
    - ![](https://www.tycoding.cn/2019/05/30/cloud/cloud-template-api/QQ20190518-202223.png)
    - 问题引入：
      
      ​ 虽然使用了网关让前端APP直接调用网关地址，由网关负责具体的接口寻址调用，从而减轻了前端的负担。但是，同样，网关也仍然需要知道所有服务的IP地址和对应的接口，并且对于同一服务集群配置的情况仍然要考虑到底调用哪个服务才能分摊请求压力。
    - 解决办法：
      
      ​ 所以，需要一个服务，他能记录系统中所有的微服务IP地址；并且有类似负载均衡的算法，对于服务集群配置，该服务能知道调用哪些服务才能平均分配请求的压力；并且，如果哪个服务崩溃，该服务还能快速知道并不再向崩溃的服务发送请求。于是，Eureka诞生了。
      ![](https://www.tycoding.cn/2019/05/30/cloud/cloud-template-api/QQ20190518-203908.png)
    
    Eureka是服务注册中心，其他具体服务可以向这个Eureka注册中心注册自己。当前端发送请求时，首先前端APP会直接请求网关，当然网关服务也注册在Eureka注册中心上，然后网关把接收到的请求交由Eureka处理，Eureka接收到这些请求，会从自己的服务注册列表中寻找对应的服务地址，然后实现调用具体的服务。
    
    ````spring:
        application:
          # 应用名称
          name: template-eureka
      
      server:
        port: 8080
      
      eureka:
        instance:
          # Eureka注册中心HOST主机地址，可以采用：1.直接配置IP；2.配置本地域名并修改本地hosts文件
          hostname: localhost
        client:
          # 是否将自己注册到注册中心。因为项目中只有一个注册中心就是自己，所以无需再注册
          register-with-eureka: false
          # 是否从远程拉取其他注册中心，因为注册中心只有自己所以不需要。如果注册中心有多个，可以相互暴露，相互拉取
          fetch-registry: false
          service-url:
            # 该注册中心连接地址
            defaultZone: http://${eureka.instance.hostname}:${server.port}/eureka/
            
    server:
        port: 9001
      
      spring:
        application:
          name: template-admin
      
      eureka:
        client:
          service-url:
            # Eureka注册中心连接地址
            # 如果注册中心地址配置的域名，这里使用 http://域名/eureka/ 格式
            defaultZone: http://localhost:8080/eureka/
    ````
    
- Feign

    - 问题引入：
      
      ​ 在上面的介绍中，我们基本解决了前端APP调用后端服务集群的问题。但又必须考虑一个问题，分布式微服务项目，即各个服务相互独立，但是各个服务又存在相互调用的关系。如何解决服务于服务之间的通信呢？
    - 解决办法：
      
      ​ 在微服务架构中，服务于服务的通讯都是基于Http Restful的。SpringCloud有两种调用方式：
      
      使用 Ribbon + RestTemplate。Ribbon是一个负载均衡客户端，可以很好的控制Http和Tcp的一些行为。而RestTemplate是Spring本身提供的用于远程调用Rest接口的HTTP客户端。
      使用 Feign。Feign是一个声明式的HTTP客户端，仅需要一个@FeignClient注解就能实现远程调用。Feign默认集成了Ribbon，并和Eureka结合，默认实现了负载均衡的效果。
      
      ````
      
      ````
- Hystrix

    - 问题引入：
      
      ​ 在集群项目中，如果突然某个服务因为访问压力过大崩溃了，Eureka再调用该服务就会调用失败，并且如果该服务向下还存在与其他服务的关联，那么就会造成其他服务也不可用，从而使错误传递下去，这也就是雪崩效应。如何解决呢？
    - 解决办法：
      
      ​ Hystrix的出现就是解决这一现象。Hystrix熔断器就像家用电闸中的保险丝，如果整个电路中某处发生了漏电、或者用电过高保险丝就会直接熔断，直接停电阻止事态恶化。如果集群中某个服务不可用、或者响应时间过长，Hystrix会直接阻断Eureka再调用此服务，从而避免了系统中所有服务都不可用。
      
      ```
      feign:
        hystrix:
          # 开启Feign的Hystrix熔断器支持
          enabled: true
      ```
- Hystrix-Dashboard

    - 问题引入：
      
      ​ 上面介绍了使用Hystrix实现熔断服务，但作为开发者，我们并不能确定服务什么时候被熔断。
    - 解决办法：
      
      ​ 提供了hystrix-dashboard工具实现实时监控Hystrix熔断器的状态。
- Zuul

    Zuul路由网关
    ![](https://www.tycoding.cn/2019/05/30/cloud/cloud-template-api/Lusifer201805292246011.png)
    
    如上是一个基本的微服务架构图，先抛开左侧的配置服务，如果前端APP请求后台接口，显然前端无法记录后端那么多服务的API地址，按照SSM框架的开发思路，前端APP仅需要记录一个IP地址即可，所有的请求都应该是请求这个IP中的某个接口。
    
    那么Zuul路由网关的作用就是如此，他实现将客户端按照一定约束的不同请求转发到对应的服务，这样就实现客户端仅记录一个IP地址就能实现请求不同服务的接口。
    
    ```
    server:
      port: 9003
    
    spring:
      application:
        name: template-zuul
    
    eureka:
      client:
        service-url:
          # Eureka注册中心连接地址
          # 如果注册中心地址配置的域名，这里使用 http://域名/eureka/ 格式
          defaultZone: http://localhost:8080/eureka/
    
    zuul:
      routes:
        # 路由名称，随意
        template-admin:
          # 路由地址
          path: /api/admin/**
          # 该路由地址对应的服务名称
          serviceId: template-admin
        template-auth:
          path: /api/auth/**
          serviceId: template-auth
  ```
    
- Config

    如果系统服务模块非常多时，每次修改服务配置都要修改服务src/main/resources下的application.yml可能会很麻烦，spring-cloud-config就解决了这个问题，Config Server端实现将配置文件内容以接口的形式暴露，Client端通过该接口得到配置文件内容，并以此初始化自己的应用。
    
    ```
    server:
      port: 8888
    
    spring:
      application:
        name: template-config
    
      # 获取本地配置文件，本身支持：本地储存、git远程、SVN
      profiles:
        active: native
    
      cloud:
        config:
          server:
            # 获取本地配置文件的位置
            native:
              search-locations: classpath:config/
    
    eureka:
      client:
        service-url:
          # Eureka注册中心连接地址
          # 如果注册中心地址配置的域名，这里使用 http://域名/eureka/ 格式
          defaultZone: http://localhost:8080/eureka/
  ```
    
- ZipKin

    **ZipKin** 服务链路追踪。可以追踪系统中服务间的依赖调用关系，查看调用的详细数据，收集服务的详细数据。提供 
    
    **Zipkin UI** 可以轻松的在Web端收集和分析数据。
    
    ```
    spring:
      application:
        name: template-zipkin
    
      # 解决Zipkin启动The bean 'characterEncodingFilter', defined in
      main:
        allow-bean-definition-overriding: true
    
    server:
      port: 9411
    
    eureka:
      client:
        serviceUrl:
          defaultZone: http://localhost:8080/eureka/
    
    management:
      metrics:
        web:
          # 解决Zipkin报错IllegalArgumentException: Prometheus requires that all meters
          server:
            auto-time-requests: false
    ```
    
- Spring-Boot-Admin

   Spring Boot Admin 实现堆各个微服务的健康状态、会话数量、并发数、服务资源、延迟等信息的收集，是一套强大的监控管理系统
   ```yml
  server:
    port: 9004
  
  spring:
    application:
      name: template-boot-admin
    zipkin:
      base-url: http://localhost:9411
  
  eureka:
    client:
      service-url:
        # Eureka注册中心连接地址
        # 如果注册中心地址配置的域名，这里使用 http://域名/eureka/ 格式
        defaultZone: http://localhost:8080/eureka/
  
  management:
    endpoint:
      health:
        show-details: always
    endpoints:
      web:
        exposure:
          include: health,info
  ```

[参考](https://www.tycoding.cn/2019/05/30/cloud/cloud-template-api/)
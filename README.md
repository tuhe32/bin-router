# 工程简介

基于Spring的适配器路由，将原先的Controller层封装，直接在Service层暴露路由，兼容原有Controller写法。

- 直接在Service注册路由信息
- 自定义的接口返回格式封装
- 自定义异常处理
- 可生成routes全局路由表，intellij安装play2routes插件后，可以直接跳转到路由映射接口
- 可根据接口注释的@tag标签生成API文档，可导出到Apifox等接口文档管理软件中，方便mock和前端联调

# 使用说明

- 引用：Maven

  ```xml
  <dependency>
    <groupId>io.github.tuhe32</groupId>
    <artifactId>adapter-api-spring-boot-starter</artifactId>
    <version>1.0.5</version>
  </dependency>
  ```

- 基本使用：

  1、@GetApiMapping，@PostApiMapping等注解，注册相应路由（支持正则路由）

  2、在Service类上使用@ApiMapping("/xxx")可作为接口前缀

  2、@apiNote，@param，@return会自动整合到API接口文档中

  ```java
  /**
   * @apiNote 用户信息
   */
  @Service
  public class UserServiceImpl {
  
      /**
       * @apiNote 获取用户资料
       * @param userId 用户标识
       * @return 用户信息
       */
      @GetApiMapping(value = "/getUser")
      public UserInfo getUser(Long userId) {
          if (userId == 1) {
              throw new PlatformException("参数 'userId'不能为空");
          }
          return mock();
      }
  
      public UserInfo mock() {
          UserInfo info = new UserInfo();
          info.setName("小明");
          info.setSex("男");
          info.setUserId(111L);
          info.setIdcard("430527198108145443");
          return info;
      }
  
      /**
       * @apiNote 用户登录
       * @param userId 用户标识
       * @param password 用户密码
       * @return 用户信息
       */
      @GetApiMapping(value = "/login/{userId}/{password}")
      public UserInfo login(Long userId, String password)  {
          System.out.println("userId: "+userId);
          System.out.println("password: "+password);
          return mock();
      }
  
  }
  ```

- 生成routes全局路由表

  浏览器请求 http://127.0.0.1:[端口号]/doc/router，如http://127.0.0.1:8080/doc/router。

  访问后resources目录下会生成routes路由表文件
  <img width="1117" alt="image" src="https://user-images.githubusercontent.com/18496264/203773150-2b09e004-6924-4b09-b576-cdc5b7e987e9.png">

- 生成API文档

  浏览器请求 http://127.0.0.1:[端口号]/doc/api-json/[项目名] ，

  如http://127.0.0.1:8080/doc/api-json/shopAdmin。

  访问后resources目录下会生成基于OpenApi3.0格式的openapi.json文件，可导入到Apifox中实现接口的Mock以及前后端对接

- 自定义接口返回格式：1.实现ResponseProcessor接口，2.用@Component注解标记。

  ```java
  @Component
  public class ResponseTest implements ResponseProcessor {
  
      @Override
      public Object buildSuccess(Object t) {
          return Result.ok(t);
      }
  
      @Override
      public Object buildFailure(String errCode, String errMessage) {
          return Result.fail(Integer.parseInt(errCode), errMessage);
      }
  }
  ```

- 自定义异常处理：

  1.实现GlobalExceptionResolver接口

  2.用@Component注解标记，

  3.@ExceptionHandler(PlatformException.class)标记自定义异常处理方法

  4.@ResponseStatus(HttpStatus.OK)标记返回的HttpStatus

  ```java
  @Component
  public class GlobalExceptionHandler implements GlobalExceptionResolver {
      private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
  
      /**
       * 处理内部警告异常
       */
      @ExceptionHandler(PlatformException.class)
      @ResponseStatus(HttpStatus.OK)
      public Result handlePlatformException(PlatformException e) {
          logger.error("系统警告：{}",e.getMessage());
          if(e.getCode() == null) return Result.fail(e.getMessage());
          return Result.fail(e.getCode(),e.getMessage());
      }
  
  }
  ```


# 注意事项

- 支持Java Bean Validation，使用方式如下，【方法参数上只支持@Valid，不支持@Validated】

  ```java
  // 若没有接口，直接在实现类增加校验注解
  // 若类上有@Override，必须在父类或接口上写校验注解
  // 即使只有@Valid，类上也要用@Validated注解标记
  @Validated
  public interface UserService {
  
      UserInfo getUser(@NotNull @Min(1) Long userId);
  
      void saveUser(@NotNull @Valid UserInfo userInfo);
  
      UserInfo login(@NotNull @Positive Long userId, @NotNull String password);
  }
  ```

  ```java
  // 在参数实体增加检验注解
  public class UserInfo {
  
     @NotNull
     private String name;
  
     @NotNull
     @Positive
     private Long userId;
  
     private String sex;
  
     private String idcard;
  }
  ```

- 如果配置文件中设置了context-path，会使所有请求路径前强行增加/api前缀，需要增加设置

  ```yaml
  server:
    servlet:
      context-path: /api
     
  # 需要设置以下，不然会默认/api
  # 接口前缀设置
  adapter-api:
    apiPrefix:
    docUrlPrefix: /doc
  ```

# 延伸阅读

正则路由匹配问题参考：[fast-route](https://www.scienjus.com/fastroute-spring/ )

根据自带的tag生成API文档使用：[smart-doc](https://smart-doc-group.github.io/#/zh-cn/start/quickstart )

接口参数解析以及封装参考：[jfinal](https://jfinal.com/doc/3-3 )

重写springMvc的RequestMappingInfoHandlerMapping

项目整体思路参考one-api

默认返回结果封装成Response，使用了cola-component-dto工具包

默认异常处理，使用了cola-component-exception工具包

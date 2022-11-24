# 工程简介

基于Spring的适配器路由，将原先的Controller层封装，直接在Service层暴露路由，但是兼容原有Controller写法。

- 直接在Service中注册路由信息
- 可自定义的接口返回格式封装
- 监听自定义异常处理
- 可生成routes项目全局路由表，intellij安装play2routes插件后，可以直接跳转到路由映射接口
- 可根据接口注释的@tag标签生成API文档，可导出到Apifox等接口文档管理软件中，方便mock和前端联调

# 使用说明

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

  3.@ExceptionHandler(PlatformException.class)标记自定义的异常

  4.@ResponseStatus(HttpStatus.OK)标记返回的httpStatus

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

  

# 延伸阅读

正则路由匹配问题参考的

[fast-route]: https://www.scienjus.com/fastroute-spring/

根据自带的tag生成API文档使用了

[smart-doc]: https://smart-doc-group.github.io/#/zh-cn/start/quickstart

接口参数解析以及封装参考了

[jfinal]: https://jfinal.com/doc/3-3

接口使用重写springMvc的RequestMappingInfoHandlerMapping，没有直接使用dispathServlet

项目整体思路参考one-api

默认返回结果封装成Response，使用了cola-component-dto工具包

默认异常处理，使用了cola-component-exception工具包

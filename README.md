# 工程简介

基于Spring的适配器路由，将原先的Controller层封装，直接在Service层暴露路由，但是兼容原有Controller写法

# 延伸阅读

正则路由匹配问题参考的

[fast-route]: https://www.scienjus.com/fastroute-spring/

根据自带的tag生成API文档使用了

[smart-doc]: https://smart-doc-group.github.io/#/zh-cn/start/quickstart

接口参数解析以及封装参考了

[jfinal]: https://jfinal.com/doc/3-3

接口使用重写springMvc的RequestMappingInfoHandlerMapping，没有直接使用dispathServlet

项目整体思路参考one-api

结果封装成Response使用了cola-component-dto工具包

统一异常处理，使用了cola-component-exception工具包

package com.yiyitech.mf.bootstrap;

import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceAutoConfigure;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(exclude = {DruidDataSourceAutoConfigure.class})
@ComponentScan(basePackages = {"com.yiyitech.**.controller", "com.yiyitech.**.service", "com.yiyitech.**.job", "com.yiyitech.**.util", "com.yiyitech.**.filter", "com.yiyitech.**.config", "com.yiyitech.support.**"})
@MapperScan(basePackages = "com.yiyitech.**.mapper")
//@Import(value = {ThirdPartySmsClient.class})
public class AdminApplication {

   /* @Autowired
    private TkMassMsgTask tkMassMsgTask;*/

    public static void main(String[] args) {
        SpringApplication.run(AdminApplication.class, args);
    }

 /*   @PostConstruct
    public void test(){
        tkMassMsgTask.run("");
    }*/
}

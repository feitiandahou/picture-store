package org.example.picturestorebackend;

import org.apache.shardingsphere.spring.boot.ShardingSphereAutoConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(exclude = {ShardingSphereAutoConfiguration.class})
@EnableAspectJAutoProxy
@EnableAsync
@MapperScan("org.example.picturestorebackend.mapper")
public class PictureStoreBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(PictureStoreBackendApplication.class, args);
    }

}

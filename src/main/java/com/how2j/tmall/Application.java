package com.how2j.tmall;
import com.how2j.tmall.util.PortUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
//这个注解用于启动缓存
@EnableCaching
//为es和jpa分别指定不同的包名（Elasticsearch查询使用）
@EnableElasticsearchRepositories(basePackages = "com.how2j.tmall.es")
@EnableJpaRepositories(basePackages = {"com.how2j.tmall.dao", "com.how2j.tmall.pojo"})
public class Application {
    //调用main方法的时候，静态代码块会执行，从而检测是否能启动springboot
    static {
        //检查端口6379是否启动，6379 就是 Redis 服务器使用的端口。如果未启动，那么就会退出 springboot。
        PortUtil.checkPort(6379,"Redis 服务端",true);
        PortUtil.checkPort(9300,"ElasticSearch 服务端",true);
        PortUtil.checkPort(5601,"Kibana 工具", true);
    }
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}

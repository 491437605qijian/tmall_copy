package com.how2j.tmall.es;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import com.how2j.tmall.pojo.Product;

//ProductESDAO作用：用于链接 ElasticSearch 的DAO
//需要和 jpa 的dao ，放在不同的包下，因为 jpa 的dao 做了 链接 redis 的，如果放在同一个包下，会彼此影响，出现启动异常
public interface ProductESDAO extends ElasticsearchRepository<Product,Integer>{

}

package com.my.dao;

import com.my.model.Product;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ArticleRepository extends ElasticsearchRepository<Product,Long> {
}

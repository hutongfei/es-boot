package com.my.controller;

import com.my.dao.ArticleRepository;
import com.my.model.Product;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.RandomUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.MoreLikeThisQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

@RequestMapping("es")
@RestController
@Api(tags = "es operate module")
public class EsController {

//    @Autowired
//    private ArticleRepository articleRepository;

    @Autowired
    private ElasticsearchRestTemplate template;

//    @ApiOperation(value = "insert product data")
//    @GetMapping("save")
//    public Product saveEs(@RequestParam String title,
//                          @RequestParam String categoryId,
//                          @RequestParam String description,
//                          @RequestParam String imgUrl,
//                          @RequestParam BigDecimal price,
//                          @RequestParam Integer sales,
//                          @RequestParam String details
//    ) {
//        Product product = new Product();
//        product.setId(RandomUtils.nextLong(1, 1000000000));
//        product.setCode(RandomUtils.nextLong(100, 1000000000) + "");
//        product.setTitle(title);
//        product.setCategoryId(categoryId);
//        product.setDescription(description);
//        product.setImgUrl(imgUrl);
//        product.setState(0);
//        product.setPrice(price);
//        product.setSales(sales);
//        product.setCreateTime(new Date().getTime());
//        product.setDetails(details);
//        Product save = articleRepository.save(product);
//        return save;
//    }
//
//
//    @GetMapping("get/{id}")
//    public Optional<Product> get(@PathVariable Long id) {
//        return articleRepository.findById(id);
//    }
//
//
//    @GetMapping("list")
//    public List<Product> list() {
//        Iterable<Product> all = articleRepository.findAll();
//        Iterator<Product> iterator = all.iterator();
//        List<Product> list = new ArrayList<>();
//        while (iterator.hasNext()) {
//            list.add(iterator.next());
//        }
//        return list;
//    }

//
//    @GetMapping("update")
//    public Product update(@RequestParam(required = true) Integer id,
//                          @RequestParam(required = false) String title,
//                          @RequestParam(required = false) String author,
//                          @RequestParam(required = false) String content) {
//        Optional<Product> result = articleRepository.findById(id);
//        Product product = result.get();
//        if (product == null) throw new RuntimeException("data is not exist !");
//
//        product.setId(id);
//        product.setTitle(StringUtils.isNoneBlank(title) ? title : product.getTitle());
//        product.setContent(StringUtils.isNoneBlank(content) ? content : product.getContent());
//        product.setAuthor(StringUtils.isNoneBlank(author) ? author : product.getAuthor());
//        articleRepository.deleteById(id);
//        articleRepository.save(product);
//        return product;
//    }
//    @ApiOperation(value = " get goods data")
//    @GetMapping("pageList1")
//    @Deprecated
//    public Page<Product> pageList1(
//            @RequestParam(required = false) String title,
//            @RequestParam(required = false) String author,
//            @RequestParam(required = false) String code,
//            @RequestParam(required = false) String content,
//            @RequestParam(required = false) String categoryId,
//            @RequestParam(required = false) String description,
//            @RequestParam(required = false) String imgUrl,
//            @RequestParam(required = false) BigDecimal price,
//            @RequestParam(required = false) Integer state,
//            @RequestParam(required = false) String details,
//            @RequestParam(required = false, defaultValue = "0") Integer start,
//            @RequestParam(required = false, defaultValue = "10") Integer num,
//            @RequestParam(required = false, defaultValue = "1") Integer sortType //1 createTimne desc 2 sales desc 3 price desc
//    ) {
//        Product condition = new Product();
//        condition.setCode(code);
//        condition.setTitle(title);
//        condition.setCategoryId(categoryId);
//        condition.setDescription(description);
//        condition.setImgUrl(imgUrl);
//        condition.setState(state);
//        condition.setPrice(price);
//
//        Sort sort = Sort.by(Sort.Direction.DESC, sortType == 1 ? "createTime" : sortType == 2 ? "sales" : sortType == 3 ? "price" : "createTime");
//        PageRequest pageRequest = PageRequest.of(start, num, sort);
//        Page<Product> list = articleRepository.searchSimilar(condition, new String[]{"id", "code", "title", "price", "createTime", "description"}, pageRequest);
//        return list;
//    }


    @ApiOperation(value = " get goods data")
    @GetMapping("pageList")
    public SearchHits<Product> pageList(
            @RequestParam(required = false, defaultValue = "电脑") String title,
            @RequestParam(required = false, defaultValue = "0") Integer start,
            @RequestParam(required = false, defaultValue = "10") Integer num,
            @RequestParam(required = false, defaultValue = "1") Integer sortType, //1 createTimne desc 2 sales desc 3 price desc
            @RequestParam(required = false, defaultValue = "") BigDecimal minPrice,
            @RequestParam(required = false, defaultValue = "") BigDecimal maxPrice
    ) {
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();
        nativeSearchQueryBuilder.withPageable(PageRequest.of(start, num));
        nativeSearchQueryBuilder.withQuery(QueryBuilders.matchQuery("title", title));

        // 范围查找
        if (minPrice != null && maxPrice != null) {
            nativeSearchQueryBuilder.withQuery(QueryBuilders.rangeQuery("price").from(minPrice).to(maxPrice).includeUpper(false).includeLower(false));
        } else if (minPrice != null) {
            nativeSearchQueryBuilder.withQuery(QueryBuilders.rangeQuery("price").gt(minPrice));
        } else if (maxPrice != null) {
            nativeSearchQueryBuilder.withQuery(QueryBuilders.rangeQuery("price").lte(maxPrice));
        }

        nativeSearchQueryBuilder.withQuery(QueryBuilders.queryStringQuery("0").field("state"));


        nativeSearchQueryBuilder.withFields("id", "title", "code", "createTime", "imgUrl", "description", "details", "state", "price", "sales", "categoryId");
        SortBuilder sortBuilder = sortType == 2 ? SortBuilders.fieldSort("sales").order(SortOrder.DESC) :
                sortType == 3 ? SortBuilders.fieldSort("price").order(SortOrder.DESC) :
                        SortBuilders.fieldSort("createTime").order(SortOrder.DESC);
        nativeSearchQueryBuilder.withSort(sortBuilder);
        NativeSearchQuery searchQuery = nativeSearchQueryBuilder.build();

        SearchHits<Product> search = template.search(searchQuery, Product.class);
        return search;
    }


}

package com.how2j.tmall.comparator;

import java.util.Comparator;

import com.how2j.tmall.pojo.Product;

//分类产品排序（综合 = 销量 x 评价）
public class ProductAllComparator implements Comparator<Product>{

    //desc排序，把p2放前面
    @Override
    public int compare(Product p1, Product p2) {
        return p2.getReviewCount()*p2.getSaleCount()-p1.getReviewCount()*p1.getSaleCount();
    }

}

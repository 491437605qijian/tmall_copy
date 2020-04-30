package com.how2j.tmall.comparator;

import java.util.Comparator;

import com.how2j.tmall.pojo.Product;

//分类产品排序（价格）
public class ProductPriceComparator implements Comparator<Product> {

    //asc排序，把p1放前面
    @Override
    public int compare(Product p1, Product p2) {
        return (int) (p1.getPromotePrice()-p2.getPromotePrice());
    }

}

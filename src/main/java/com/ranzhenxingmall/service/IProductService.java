package com.ranzhenxingmall.service;

import com.ranzhenxingmall.common.ServerResponse;
import com.ranzhenxingmall.pojo.Product;

public interface IProductService {
    ServerResponse saveOrUpdateProduct(Product product);

    ServerResponse setSaleStatus(Integer productId, Integer status);

    ServerResponse manageProductDetail(Integer productId);

    ServerResponse getProductList(int pageNum, int pageSize);

    ServerResponse searchProduct(String productName, Integer productId, Integer pageNum, Integer pageSize);
}

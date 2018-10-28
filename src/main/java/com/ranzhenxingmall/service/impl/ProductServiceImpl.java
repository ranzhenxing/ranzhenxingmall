package com.ranzhenxingmall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.ranzhenxingmall.common.ResponseCode;
import com.ranzhenxingmall.common.ServerResponse;
import com.ranzhenxingmall.dao.CategoryMapper;
import com.ranzhenxingmall.dao.ProductMapper;
import com.ranzhenxingmall.pojo.Category;
import com.ranzhenxingmall.pojo.Product;
import com.ranzhenxingmall.service.IProductService;
import com.ranzhenxingmall.util.DateTimeUtil;
import com.ranzhenxingmall.util.PropertiesUtil;
import com.ranzhenxingmall.vo.ProductDetailVo;
import com.ranzhenxingmall.vo.ProductListVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service("iProductService")
public class ProductServiceImpl implements IProductService {
    @Autowired
    ProductMapper productMapper;
    @Autowired
    CategoryMapper categoryMapper;

    @Override
    public ServerResponse saveOrUpdateProduct(Product product) {
        if (product == null) {
            return ServerResponse.createByErrorMessage("新增或者更新产品参数不正确");
        }
        if (StringUtils.isNotBlank(product.getSubImages())) {
            String[] subImages = product.getSubImages().split(",");
            if (subImages.length > 0) {
                product.setMainImage(subImages[0]);
            }
        }

        //新增
        if (product.getId() == null) {
            int insertResult = productMapper.insert(product);
            if (insertResult > 0) {
                return ServerResponse.createBySuccessMessage("新增产品成功");
            }
            return ServerResponse.createByErrorMessage("新增产品失败");
        }

        //修改
        Product selectProduct = productMapper.selectByPrimaryKey(product.getId());
        if (selectProduct == null) {
            return ServerResponse.createByErrorMessage("更新产品不存在");
        }
        int updateResult = productMapper.updateByPrimaryKey(product);
        if (updateResult > 0) {
            return ServerResponse.createBySuccessMessage("更新产品成功");
        }
        return ServerResponse.createByErrorMessage("更新产品失败");
    }

    @Override
    public ServerResponse setSaleStatus(Integer productId, Integer status) {
        if (productId == null || status == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Product selectProduct = productMapper.selectByPrimaryKey(productId);
        if (selectProduct == null) {
            return ServerResponse.createByErrorMessage("更新产品不存在");
        }
        Product product = new Product();
        product.setId(productId);
        product.setStatus(status);
        int updateResult = productMapper.updateByPrimaryKeySelective(product);
        if (updateResult > 0) {
            return ServerResponse.createBySuccessMessage("修改销售状态成功");
        }
        return ServerResponse.createByErrorMessage("修改销售状态失败");
    }

    @Override
    public ServerResponse manageProductDetail(Integer productId) {
        if (productId == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }

        Product selectProduct = productMapper.selectByPrimaryKey(productId);
        if (selectProduct == null) {
            return ServerResponse.createByErrorMessage("产品已下架或者删除");
        }
        ProductDetailVo productDetailVo = transferToProductVo(selectProduct);

        return ServerResponse.createBySuccess(productDetailVo);
    }

    @Override
    public ServerResponse getProductList(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Product> productList = productMapper.selectList();
        List<ProductListVo> listVos = Lists.newArrayList();
        for (Product product : productList) {
            listVos.add(transferToProductListVo(product));
        }
        PageInfo pageInfo = new PageInfo(productList);
        pageInfo.setList(listVos);
        return ServerResponse.createBySuccess(pageInfo);
    }

    @Override
    public ServerResponse searchProduct(String productName, Integer productId, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        if (StringUtils.isNotBlank(productName)) {
            productName= "%" + productName + "%";
        }
        List<Product> productList = productMapper.selectByNameAndProductId(productName,productId);
        List<ProductListVo> listVos = Lists.newArrayList();
        for (Product product : productList) {
            listVos.add(transferToProductListVo(product));
        }
        PageInfo pageInfo = new PageInfo(productList);
        pageInfo.setList(listVos);
        return ServerResponse.createBySuccess(pageInfo);
    }

    private ProductListVo transferToProductListVo(Product product) {
        ProductListVo productListVo = new ProductListVo();
        productListVo.setId(product.getId());
        productListVo.setName(product.getName());
        productListVo.setCategoryId(product.getCategoryId());
        productListVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix", "http://img.ranzhenxingmall.com/"));
        productListVo.setMainImage(product.getMainImage());
        productListVo.setPrice(product.getPrice());
        productListVo.setSubtitle(product.getSubtitle());
        productListVo.setStatus(product.getStatus());
        return productListVo;
    }

    private ProductDetailVo transferToProductVo(Product product) {
        ProductDetailVo productDetailVo = new ProductDetailVo();
        productDetailVo.setId(product.getId());
        productDetailVo.setSubtitle(product.getSubtitle());
        productDetailVo.setPrice(product.getPrice());
        productDetailVo.setMainImage(product.getMainImage());
        productDetailVo.setSubImages(product.getSubImages());
        Integer categoryId = product.getCategoryId();
        productDetailVo.setCategoryId(categoryId);
        productDetailVo.setDetail(product.getDetail());
        productDetailVo.setName(product.getName());
        productDetailVo.setStatus(product.getStatus());
        productDetailVo.setStock(product.getStock());
        productDetailVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix", "http://img.ranzhenxingmall.com/"));
        Category category = categoryMapper.selectByPrimaryKey(categoryId);
        if (category == null) {
            productDetailVo.setParentCategoryId(0);
        } else {
            productDetailVo.setParentCategoryId(category.getParentId());
        }
        productDetailVo.setCreateTime(DateTimeUtil.dateToStr(product.getCreateTime()));
        productDetailVo.setUpdateTime(DateTimeUtil.dateToStr(product.getUpdateTime()));
        return productDetailVo;
    }
}

package com.ranzhenxingmall.service.impl;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.ranzhenxingmall.common.Const;
import com.ranzhenxingmall.common.ResponseCode;
import com.ranzhenxingmall.common.ServerResponse;
import com.ranzhenxingmall.dao.CartMapper;
import com.ranzhenxingmall.dao.ProductMapper;
import com.ranzhenxingmall.pojo.Cart;
import com.ranzhenxingmall.pojo.Product;
import com.ranzhenxingmall.service.ICartService;
import com.ranzhenxingmall.util.BigDecimalUtil;
import com.ranzhenxingmall.util.PropertiesUtil;
import com.ranzhenxingmall.vo.CartProductVo;
import com.ranzhenxingmall.vo.CartVo;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service("iCartService")
public class CartServiceImpl implements ICartService {

    @Autowired
    private CartMapper cartMapper;
    @Autowired
    private ProductMapper productMapper;

    @Override
    public ServerResponse<CartVo> add(Integer userId, Integer productId, Integer count) {
            if(productId==null||count==null){
                return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
            }
        Cart cart = cartMapper.selectCartByUserIdProductId(userId, productId);
            if(cart==null){
                //产品目前不在购物车中有记录，需要新增产品记录
                Cart cartItem=new Cart();
                cartItem.setChecked(Const.Cart.CHECKED);
                cartItem.setProductId(productId);
                cartItem.setQuantity(count);
                cartItem.setUserId(userId);
                cartMapper.insert(cartItem);
            }else {
                cart.setQuantity(cart.getQuantity()+count);
                cartMapper.updateByPrimaryKeySelective(cart);
            }

        return this.list(userId);
    }

    @Override
    public ServerResponse<CartVo> update(Integer userId, Integer productId, Integer count) {
        if(productId==null||count==null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Cart cart = cartMapper.selectCartByUserIdProductId(userId,productId);
        if(cart != null){
            cart.setQuantity(count);
        }
        cartMapper.updateByPrimaryKey(cart);
        return this.list(userId);
    }

    @Override
    public ServerResponse<CartVo> deleteProduct(Integer userId, String productIds) {
        List<String> productList = Splitter.on(",").splitToList(productIds);
        if(CollectionUtils.isEmpty(productList)){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        cartMapper.deleteByUserIdProductIds(userId,productList);
        return this.list(userId);
    }

    @Override
    public ServerResponse<CartVo> list(Integer userId) {
        CartVo cartVo = this.getCartVoLimit(userId);
        return ServerResponse.createBySuccess(cartVo);
    }

    private CartVo getCartVoLimit(Integer userId) {
        CartVo cartVo=new CartVo();
        List<Cart> cartList = cartMapper.selectCartByUserId(userId);
        List<CartProductVo> cartVoList= Lists.newArrayList();

        BigDecimal cartTotalPrice=new BigDecimal("0");
        if(CollectionUtils.isNotEmpty(cartList)){
            CartProductVo cartProductVoTemp;
            for (Cart cart : cartList) {
                cartProductVoTemp=new CartProductVo();
                cartProductVoTemp.setId(cart.getId());
                cartProductVoTemp.setUserId(cart.getUserId());
                cartProductVoTemp.setProductId(cart.getProductId());

                Product product = productMapper.selectByPrimaryKey(cart.getProductId());
                if(product!=null){
                    cartProductVoTemp.setProductMainImage(product.getMainImage());
                    cartProductVoTemp.setProductName(product.getName());
                    cartProductVoTemp.setProductSubtitle(product.getSubtitle());
                    cartProductVoTemp.setProductStatus(product.getStatus());
                    cartProductVoTemp.setProductPrice(product.getPrice());
                    cartProductVoTemp.setProductStock(product.getStock());
                    int buyLimitCount=0;
                    if(product.getStock()>cart.getQuantity()){
                        //库存充足
                        buyLimitCount=cart.getQuantity();
                        cartProductVoTemp.setLimitQuantity(Const.Cart.LIMIT_NUM_SUCCESS);
                    }else {
                        buyLimitCount=product.getStock();
                        cartProductVoTemp.setLimitQuantity(Const.Cart.LIMIT_NUM_FAIL);
                        //购物车中更新有效库存
                        Cart cartForQuantity=new Cart();
                        cartForQuantity.setId(cart.getId());
                        cartForQuantity.setQuantity(buyLimitCount);
                        cartMapper.updateByPrimaryKeySelective(cartForQuantity);
                    }

                    cartProductVoTemp.setQuantity(buyLimitCount);
                    //计算总价
                    cartProductVoTemp.setProductTotalPrice(BigDecimalUtil.mul(product.getPrice().doubleValue(),buyLimitCount));
                    cartProductVoTemp.setProductChecked(cart.getChecked());
                }

                if(cart.getChecked() == Const.Cart.CHECKED){
                    //如果已经勾选,增加到整个的购物车总价中
                    cartTotalPrice = BigDecimalUtil.add(cartTotalPrice.doubleValue(),cartProductVoTemp.getProductTotalPrice().doubleValue());
                }
                cartVoList.add(cartProductVoTemp);
            }

        }
        cartVo.setCartTotalPrice(cartTotalPrice);
        cartVo.setAllChecked(this.getAllCheckedStatus(userId));
        cartVo.setCartProductVoList(cartVoList);
        cartVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));
        return cartVo;
    }

    @Override
    public ServerResponse<CartVo> selectOrUnSelect(Integer userId, Integer productId, Integer checked) {
        cartMapper.checkedOrUncheckedProduct(userId,productId,checked);
        return this.list(userId);
    }

    @Override
    public ServerResponse<Integer> getCartProductCount(Integer userId) {
        if(userId == null){
            return ServerResponse.createBySuccess(0);
        }
        return ServerResponse.createBySuccess(cartMapper.selectCartProductCount(userId));
    }

    private boolean getAllCheckedStatus(Integer userId){
        if(userId == null){
            return false;
        }
        return cartMapper.selectCartProductCheckedStatusByUserId(userId) == 0;

    }
}

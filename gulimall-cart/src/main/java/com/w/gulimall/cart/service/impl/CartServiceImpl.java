package com.w.gulimall.cart.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.w.common.constant.CartConstant;
import com.w.common.utils.R;
import com.w.gulimall.cart.feign.ProductFeignService;
import com.w.gulimall.cart.interceptor.CartInterceptor;
import com.w.gulimall.cart.service.CartService;
import com.w.gulimall.cart.to.UserInfoTo;
import com.w.gulimall.cart.vo.CartItemVo;
import com.w.gulimall.cart.vo.CartVo;
import com.w.gulimall.cart.vo.SkuInfoVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * @author xin
 * @date 2023-02-10-13:29
 */
@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ProductFeignService productFeignService;

    @Autowired
    private ThreadPoolExecutor executor;

    @Override
    public CartItemVo addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        String res = (String) cartOps.get(skuId.toString());
        if (StringUtils.isEmpty(res)) {
            CartItemVo cartItemVo = new CartItemVo();
            CompletableFuture<Void> getSkuInfoTask = CompletableFuture.runAsync(() -> {
                R skuInfo = productFeignService.getInfo(skuId);
                SkuInfoVo data = skuInfo.getData("skuInfo", new TypeReference<SkuInfoVo>() {
                });

                cartItemVo.setCheck(true);
                cartItemVo.setCount(num);
                cartItemVo.setImage(data.getSkuDefaultImg());
                cartItemVo.setPrice(data.getPrice());
                cartItemVo.setSkuId(skuId);
                cartItemVo.setTitle(data.getSkuTitle());

            }, executor);

            CompletableFuture<Void> voidCompletableFuture = CompletableFuture.runAsync(() -> {
                List<String> stringList = productFeignService.getSkuSaleAttrValues(skuId);
                cartItemVo.setSkuAttrValues(stringList);
            }, executor);

            CompletableFuture.allOf(getSkuInfoTask, voidCompletableFuture).get();

            String s = JSON.toJSONString(cartItemVo);
            cartOps.put(skuId.toString(), s);
            return cartItemVo;
        }
        else {
//            购物车由此商品，修改数量
            CartItemVo cartItemVo = JSON.parseObject(res, CartItemVo.class);
            cartItemVo.setCount(cartItemVo.getCount()+1);
            String string = JSON.toJSONString(cartItemVo);
            cartOps.put(skuId.toString(),string);
            return cartItemVo;
        }

    }

    @Override
    public CartItemVo getCartItem(Long skuId) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();

        String str = (String) cartOps.get(skuId.toString());
        CartItemVo cartItemVo = JSON.parseObject(str, CartItemVo.class);
        return cartItemVo;

    }

    @Override
    public CartVo getCart() throws ExecutionException, InterruptedException {
        CartVo cartVo = new CartVo();
        UserInfoTo userInfoTo = CartInterceptor.toThreadLocal.get();
        if (userInfoTo.getUserId()!=null){
            //1.登录
            String cartKey = CartConstant.CART_PREFIX + userInfoTo.getUserId();
            List<CartItemVo> tempCartItems = getCartItems(CartConstant.CART_PREFIX + userInfoTo.getUserKey());
            if (tempCartItems!=null){
//                合并临时购物车
                for (CartItemVo cartItem : tempCartItems) {
                    addToCart(cartItem.getSkuId(),cartItem.getCount());
                }
                //清楚临时购物车
                clearCart(CartConstant.CART_PREFIX + userInfoTo.getUserKey());
            }
            List<CartItemVo> cartItems = getCartItems(cartKey);
            cartVo.setItems(cartItems);

        }else {
            //2.未登录
            String cartKey = CartConstant.CART_PREFIX + userInfoTo.getUserKey();
            List<CartItemVo> cartItems = getCartItems(cartKey);
            cartVo.setItems(cartItems);
        }

        return cartVo;
    }

    @Override
    public void clearCart(String cartKey) {
        redisTemplate.delete(cartKey);
    }

    @Override
    public void checkItem(Long skuId, Integer check) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        CartItemVo item = getCartItem(skuId);

        item.setCheck(check==1?true:false);

        String str = JSON.toJSONString(item);

        cartOps.put(skuId,str);
    }

    @Override
    public void countItem(Long skuId, Integer num) {
        CartItemVo item = getCartItem(skuId);
        item.setCount(num);

        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        cartOps.put(skuId.toString(),JSON.toJSONString(item));

    }

    @Override
    public void deleteItem(Long skuId) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        cartOps.delete(skuId.toString());
    }

    @Override
    public List<CartItemVo> getCurrentUserCartItems() {
        UserInfoTo userInfoTo = CartInterceptor.toThreadLocal.get();
        if (userInfoTo==null){
            return null;
        }else {
            String s = CartConstant.CART_PREFIX + userInfoTo.getUserId();
//            String s = CartConstant.CART_PREFIX + "1";
            List<CartItemVo> cartItems = getCartItems(s);
            List<CartItemVo> collect = cartItems.stream()
                    .filter(item -> item.getCheck())
                    .map(item -> {
                        BigDecimal price = productFeignService.getPrice(item.getSkuId());
//                        更新价格为最新
                        item.setPrice(price);
                        return item;
                    }).collect(Collectors.toList());
            return collect;
        }
    }

    private BoundHashOperations<String, Object, Object> getCartOps() {
        UserInfoTo userInfoTo = CartInterceptor.toThreadLocal.get();

        String cartKey = "";

        if (userInfoTo.getUserId() != null) {
            cartKey = CartConstant.CART_PREFIX + userInfoTo.getUserId();
        } else {
            cartKey = CartConstant.CART_PREFIX + userInfoTo.getUserKey();
        }

        BoundHashOperations<String, Object, Object> operations = redisTemplate.boundHashOps(cartKey);
        return operations;
    }

    private List<CartItemVo> getCartItems(String cartKey){
        BoundHashOperations<String, Object, Object> ops = redisTemplate.boundHashOps(cartKey);
        List<Object> values = ops.values();
        if (values!=null&&values.size()>0){
            List<CartItemVo> collect = values.stream().map((value) -> {
                String str = (String) value;
                CartItemVo cartItemVo = JSON.parseObject(str, CartItemVo.class);
                return cartItemVo;
            }).collect(Collectors.toList());
            return collect;
        }
        return null;
    }


}

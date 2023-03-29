package com.w.gulimall.cart.service;

import com.w.gulimall.cart.vo.CartItemVo;
import com.w.gulimall.cart.vo.CartVo;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * @author xin
 * @date 2023-02-10-13:29
 */
public interface CartService {
    CartItemVo addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException;


    CartItemVo getCartItem(Long skuId);

    /**
     * 获取整个购物车
     * @return
     */
    CartVo getCart() throws ExecutionException, InterruptedException;

    /**
     * 清空购物车
     * @param cartKey
     */
    void clearCart(String cartKey);

    /**
     * 勾选购物项
     * @param skuId
     * @param check
     */
    void checkItem(Long skuId, Integer check);

    /**
     * 修改数量
     * @param skuId
     * @param num
     */
    void countItem(Long skuId, Integer num);

    /**
     * 删除
     * @param skuId
     */
    void deleteItem(Long skuId);

    List<CartItemVo> getCurrentUserCartItems();
}

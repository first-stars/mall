package com.w.gulimall.cart.controller;

import com.w.gulimall.cart.interceptor.CartInterceptor;
import com.w.gulimall.cart.service.CartService;
import com.w.gulimall.cart.to.UserInfoTo;
import com.w.gulimall.cart.vo.CartItemVo;
import com.w.gulimall.cart.vo.CartVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * @author xin
 * @date 2023-02-10-15:58
 */
@Controller
public class CartController {

    @Autowired
    private CartService cartService;


    @GetMapping("/currentUserCartItems")
    @ResponseBody
    public List<CartItemVo> getCurrentUserCartItems(){
        List<CartItemVo> currentUserCartItems = cartService.getCurrentUserCartItems();
        return currentUserCartItems;
//        return null;
    }

    @GetMapping("/deleteItem")
    public String deleteItem(@RequestParam("skuId") Long skuId){
        cartService.deleteItem(skuId);
        return "redirect:http://cart.gulimall.com/cart.html";
    }

    @GetMapping("/countItem")
    public String countItem(@RequestParam("skuId") Long skuId ,
                            @RequestParam("num") Integer num){
        cartService.countItem(skuId,num);
        return "redirect:http://cart.gulimall.com/cart.html";
    }

    @GetMapping("/checkItem")
    public String checkItem(@RequestParam("skuId") Long skuId ,
                            @RequestParam("check") Integer check){
        cartService.checkItem(skuId,check);
        return "redirect:http://cart.gulimall.com/cart.html";
    }

    @GetMapping("/cart.html")
    public String cartListPage(Model model) throws ExecutionException, InterruptedException {
//        快速得到用户信息，id user_key
//        UserInfoTo userInfoTo = CartInterceptor.toThreadLocal.get();
//        System.out.println(userInfoTo);
        CartVo cartVo = cartService.getCart();
        model.addAttribute("cart",cartVo);
        return "cartList";
    }

    /**
     * 添加商品
     * @param skuId
     * @param num
     * @param
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @GetMapping("/addToCart")
    public String addToCart(@RequestParam("skuId") Long skuId, @RequestParam("num") Integer num, RedirectAttributes ra) throws ExecutionException, InterruptedException {
        cartService.addToCart(skuId,num);
        ra.addAttribute("skuId",skuId);
        return "redirect:http://cart.gulimall.com/addToCartSuccess.html";

    }

    @GetMapping("/addToCartSuccess.html")
    public String addToCartSuccessPage(@RequestParam("skuId") Long skuId,Model model){
        CartItemVo item = cartService.getCartItem(skuId);
        model.addAttribute("item",item);
        return "success";
    }
}

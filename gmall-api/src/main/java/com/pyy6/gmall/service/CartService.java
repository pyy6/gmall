package com.pyy6.gmall.service;

import com.pyy6.gmall.bean.CartInfo;

import java.util.List;

public interface CartService {
    CartInfo ifCartExistSku(CartInfo cartInfo);

    void updateCart(CartInfo cartInfoDB);

    void saveCart(CartInfo cartInfo);

    void syncCache(String userId);

    List<CartInfo> getCartCache(String userId);

    void updateCartChecked(CartInfo cartInfo);

    void combineCart(List<CartInfo> cartInfos, String id);

    List<CartInfo> getCartCacheByChecked(String userId);

    void deleteCartById(List<CartInfo> cartCacheByChecked);
}

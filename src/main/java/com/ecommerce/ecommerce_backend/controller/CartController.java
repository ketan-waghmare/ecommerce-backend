package com.ecommerce.ecommerce_backend.controller;

import com.ecommerce.ecommerce_backend.entity.Cart;
import com.ecommerce.ecommerce_backend.security.UserPrincipal;
import com.ecommerce.ecommerce_backend.services.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {


    @Autowired
    private CartService cartService;

//    @PostMapping("/create")
//    public Cart createCart() {
//        return cartService.createCart();
//    }

    @PostMapping("/add")
    public Cart addToCart(
            @RequestParam String cartId,
            @RequestParam Long productId,
            @RequestParam int quantity
    ) {
        return cartService.addToCart(cartId,productId,quantity);
    }

    @GetMapping
    public Cart getCart(@RequestParam(required = false) String cartId,@AuthenticationPrincipal UserPrincipal user) {
            System.out.println("USer Details ==>" + user);

            if (user != null) {
                return cartService.getCartByUserId(user.getUserId());
            }

            if(cartId == null) {
                throw new RuntimeException("CartId required for guest user");
            }

            return cartService.getCart(cartId);
    }

    // DELETE a single cart item
    @DeleteMapping("/remove/{cartItemId}")
    public ResponseEntity<String> removeCartItem(@PathVariable Long cartItemId) {

        System.out.println("DELETE HIT for cartItemId = " + cartItemId);

        cartService.removeCartItem(cartItemId);
        return ResponseEntity.ok("Item removed from cart");

    }

    @PutMapping("/update/{cartItemId}")
    public ResponseEntity<Cart> updateQuantity(
            @PathVariable Long cartItemId,
            @RequestParam int quantity
    ) {
        Cart cart = cartService.updateQuantity(cartItemId, quantity);
        return ResponseEntity.ok(cart);
    }

    //merge cart
    @PostMapping("/merge/{guestCartId}")
    public  ResponseEntity<Cart> mergeCart(
            @PathVariable String guestCartId,
            Authentication authentication
            ) {

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        Long userId = principal.getUserId();
        Cart mergeCart = cartService.mergeCart(guestCartId, userId);

        return  ResponseEntity.ok(mergeCart);
    }
}

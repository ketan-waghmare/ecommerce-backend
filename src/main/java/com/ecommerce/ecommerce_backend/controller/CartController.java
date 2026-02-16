package com.ecommerce.ecommerce_backend.controller;

import com.ecommerce.ecommerce_backend.entity.Cart;
import com.ecommerce.ecommerce_backend.entity.User;
import com.ecommerce.ecommerce_backend.repository.UserRepository;
import com.ecommerce.ecommerce_backend.security.UserPrincipal;
import com.ecommerce.ecommerce_backend.services.CartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/cart")
public class CartController {


    @Autowired
    private CartService cartService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/add")
    public ResponseEntity<Cart> addToCart(
            @RequestParam(required = false) String cartId,
            @RequestParam Long productId,
            @RequestParam(defaultValue = "1") int quantity,
            Authentication authentication
    ) {

        User user = getUserFromAuthentication(authentication);

        log.info("Add to cart request - ProductId: {}, Quantity: {}, CartId: {}, Authenticated: {}",
                productId, quantity, cartId, authentication != null);

        Cart cart = cartService.addToCart(cartId,productId,quantity,user);
        return ResponseEntity.ok(cart);
    }

    private User getUserFromAuthentication(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return null;
        }

        Object principal = authentication.getPrincipal();

        // If it's already a User entity, return it
        if (principal instanceof User) {
            return (User) principal;
        }

        // If it's a UserPrincipal, get the user from database
        if (principal instanceof UserPrincipal) {
            UserPrincipal userPrincipal = (UserPrincipal) principal;
            Long userId = userPrincipal.getUserId();
            return userRepository.findById(userId).orElse(null);
        }

        return null;
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

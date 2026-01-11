package com.ecommerce.ecommerce_backend.services;

import com.ecommerce.ecommerce_backend.entity.Cart;
import com.ecommerce.ecommerce_backend.entity.CartItem;
import com.ecommerce.ecommerce_backend.entity.Product;
import com.ecommerce.ecommerce_backend.entity.User;
import com.ecommerce.ecommerce_backend.repository.CartItemRepository;
import com.ecommerce.ecommerce_backend.repository.CartRepository;
import com.ecommerce.ecommerce_backend.repository.ProductRepository;
import com.ecommerce.ecommerce_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

//    public Cart createCart() {
//        Cart cart = new Cart();
//        cart.setCartId(UUID.randomUUID().toString());
//        return cartRepository.save(cart);
//    }

    public Cart addToCart(String cartId, Long productId, int quantity) {

        Cart cart = cartRepository.findByCartId(cartId)
                .orElseGet(() -> {
                   Cart c = new Cart();
                   c.setCartId(cartId);
                   c.setTotalAmount(0.0);
                   return cartRepository.save(c);
                });

        Product product =productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        CartItem item = cartItemRepository
                .findByCartAndProduct(cart,product)
                .orElseGet(() -> {
                   CartItem ci = new CartItem();
                   ci.setCart(cart);
                   ci.setProduct(product);
                   ci.setQuantity(0);
                   ci.setPrice(product.getPrice());
                   return ci;
                });

        item.setQuantity(item.getQuantity() + quantity);
        cartItemRepository.save(item);

        double total = cart.getItems().stream()
                .mapToDouble(i -> i.getPrice() * i.getQuantity())
                .sum();

        cart.setTotalAmount(total);

        return cartRepository.save(cart);
    }

    public Cart getCart(String cartId) {
        return cartRepository.findByCartId(cartId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));
    }

    public Cart getCartByUserId(Long userId) {
        return cartRepository.findByUserId(userId).orElseThrow(() -> new RuntimeException("Cart not found using userId"));
    }

    @Transactional
    public Cart removeCartItem(Long cartItemId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("CartItem not found"));

        Cart cart = cartItem.getCart(); // store before delete

        cartItemRepository.delete(cartItem);

        return cart;
    }

    @Transactional
    public Cart updateQuantity(Long cartItemId, int quantity) {

        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));


        if (quantity < 1) {
            cartItemRepository.delete(item);
        } else {
            item.setQuantity(quantity);
            cartItemRepository.save(item);
        }

        return item.getCart();
    }


    //merge cart
    @Transactional
    public Cart mergeCart(String guestCartId, Long userId) {

        Cart guestCart = cartRepository.findByCartId(guestCartId)
                .orElseThrow(() -> new RuntimeException("Guest cart not found"));

        User user = userRepository.findById(userId).orElseThrow(() ->  new RuntimeException("User not found"));

//        Cart userCart = createNewUserCart(user);

        Cart userCart = cartRepository.findByUserId(userId)
                .orElseGet(() -> createNewUserCart(user));

        for (CartItem guestItem : guestCart.getItems()) {

            Optional<CartItem> existingItem = userCart.getItems().stream()
                    .filter(i -> i.getProduct().getId()
                            .equals(guestItem.getProduct().getId()))
                    .findFirst();

            if (existingItem.isPresent()) {
                existingItem.get().setQuantity(
                        existingItem.get().getQuantity() + guestItem.getQuantity()
                );
            } else {
                CartItem newItem = new CartItem();
                newItem.setProduct(guestItem.getProduct());
                newItem.setQuantity(guestItem.getQuantity());
                newItem.setPrice(guestItem.getPrice());
                newItem.setCart(userCart);

                userCart.getItems().add(newItem);
            }
        }

        cartRepository.save(userCart);
        cartRepository.delete(guestCart);

        return userCart;
    }

    private Cart createNewUserCart(User user) {
        Cart cart = new Cart();
        cart.setUser(user);
        return cartRepository.save(cart);
    }



}

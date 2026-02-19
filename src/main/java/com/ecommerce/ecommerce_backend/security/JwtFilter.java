package com.ecommerce.ecommerce_backend.security;

import com.ecommerce.ecommerce_backend.services.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.java.Log;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        System.out.println(
                "AUTH BEFORE: " + SecurityContextHolder.getContext().getAuthentication()
        );

        System.out.println(">>>> JWT Filter HIT : " +request.getRequestURI());
        System.out.println("AUTH HEADER ==> " + request.getHeader("Authorization"));


        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            String email = jwtUtil.extractEmail(token);
            Long userId = jwtUtil.extractUserId(token);

            System.out.println(">>>> JWT Filter Email  : ==>" +email);
            System.out.println(">>>> JWT Filter userId  : ==>" +userId);

            if(email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                UserPrincipal principal = new UserPrincipal(userId,email,null);

                System.out.println(">>>> JWT Filter inside email if condition : ==>" +email);

//                UserDetails userDetails = userDetailsService.loadUserByUsername(email);

//                System.out.println(">>>> JWT Filter UserDetails userDetails = userDetailsService.loadUserByUsername(email) : ==>" +userDetails.toString());

                if (jwtUtil.validateToken(token)) {

                    System.out.println(">>>> JWT Filter jwtUtil.validateToken(token) : ==>" +jwtUtil.validateToken(token));

                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            principal,
                            null,
                            principal.getAuthorities()
                    );

                    authentication.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }


        }


//        if (authHeader != null) {
//            System.out.println("AUTH HEADER: " + authHeader);
//        }
//
//        UserDetails userDetails = userDetailsService.loadUserByUsername()
//
//        UsernamePasswordAuthenticationToken auth =
//                new UsernamePasswordAuthenticationToken(
//                        userDetails,
//                        null,
//                        userDetails.getAuthorities()
//                );
//
//        SecurityContextHolder.getContext().setAuthentication(auth);

        filterChain.doFilter(request, response);
    }
}

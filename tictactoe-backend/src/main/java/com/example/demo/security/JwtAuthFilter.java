package com.example.demo.security;

import java.io.IOException;
import java.util.Collections;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        // 1. Lấy header Authorization
        String authHeader = request.getHeader("Authorization");
        String token = null;
        String username = null;

        // 2. Kiểm tra xem Header có hợp lệ không
        // LOGIC MỚI: Nếu KHÔNG có token, ta không báo lỗi ngay, mà cho đi tiếp
        // (chain.doFilter)
        // Lý do: Nếu đây là trang /login hay /register (được permitAll bên
        // SecurityConfig), nó sẽ chạy OK.
        // Nếu đây là trang cần bảo mật, Spring Security sẽ chặn sau đó.
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        // 3. Nếu có token, bắt đầu xử lý
        token = authHeader.substring(7);

        // Kiểm tra tính hợp lệ của token
        // Lưu ý: Nếu token sai, lúc này ta mới có thể chặn hoặc bỏ qua.
        // Ở đây tôi chọn cách an toàn: Nếu token lỗi, cứ cho qua nhưng không set
        // Authentication -> Spring sẽ chặn sau.
        if (jwtService.isTokenValid(token)) {
            username = jwtService.extractUsername(token);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        new User(username, "", Collections.emptyList()), null, Collections.emptyList());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Xác thực thành công, lưu vào SecurityContext
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        // 4. Cho request đi tiếp đến Controller
        chain.doFilter(request, response);
    }
}
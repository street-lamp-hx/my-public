package com.yiyitech.mf.filter;

import com.yiyitech.mf.model.dobj.LoginUser;
import com.yiyitech.mf.util.TokenUtil;
import io.jsonwebtoken.Claims;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author hx
 * @version 1.0.0
 * @ClassName JwtAuthenticationFilter.java
 * @Description
 * @createTime 2025年06月08日 16:16:00
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (token != null && TokenUtil.validateToken(token)) {
                Claims claims = TokenUtil.parseToken(token);
                String phone = claims.getSubject();
                String id = claims.get("id", String.class);
                String userName = claims.get("userName", String.class);
                String parentId = claims.get("parentId", String.class);
                List<String> roles = claims.get("roles", List.class);
                List<String> perms = claims.get("permissions", List.class);

                List<GrantedAuthority> authorities = new ArrayList<>();
                if (roles != null) {
                    for (String role : roles) {
                        authorities.add(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));
                    }
                }
                if (perms != null) {
                    for (String perm : perms) {
                        authorities.add(new SimpleGrantedAuthority(perm));
                    }
                }
                LoginUser loginUser = new LoginUser(id, userName, parentId, phone, roles, perms);
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(loginUser, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }
        filterChain.doFilter(request, response);
    }
}

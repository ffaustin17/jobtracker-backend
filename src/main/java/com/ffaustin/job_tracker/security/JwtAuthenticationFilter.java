package com.ffaustin.job_tracker.security;

import com.ffaustin.job_tracker.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    
    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService){
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    /**
     * This method is called for every incoming HTTP request. It
     * extracts the JWT from the Authorization header,
     * validates the token,
     * loads the associated user and sets the authentication in the SecurityContext.
     * @param request the incoming HTTP request
     * @param response the outgoing HTTP request
     * @param filterChain the filter chain to continue processing the request
     * @throws ServletException if an error occurs while processing the servlet
     * @throws IOException if an I/O error occurs while handling the request
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException
    {
        final String authHeader = request.getHeader("Authorization");

        // Skip filtering if no Bearer token is found
        if(authHeader == null || !authHeader.startsWith("Bearer")){
            logger.debug("No JWT found in Authorization header");
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        String userEmail = jwtUtil.extractEmail(token);

        // Authenticate only if the user is not already authenticated
        if(userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null){
            UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

            // Validated the token and set the authentication in the context
            if(jwtUtil.isTokenValid(token)){
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails,
                        null,
                        userDetails.getAuthorities());

                SecurityContextHolder.getContext().setAuthentication(authToken);
                logger.info("JWT authenticated user: {}", userEmail);
            }
            else{
                logger.warn("Invalid JWT token for user: {}", userEmail);
            }
        }

        // continue with the next filter in the chain
        filterChain.doFilter(request, response);
    }
}

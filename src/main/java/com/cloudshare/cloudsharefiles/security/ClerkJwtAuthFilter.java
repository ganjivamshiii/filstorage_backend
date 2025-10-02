package com.cloudshare.cloudsharefiles.security;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import java.security.PublicKey;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Base64;
import java.util.Collection;
import java.io.IOException;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Value;

@Component
@RequiredArgsConstructor
public class ClerkJwtAuthFilter extends OncePerRequestFilter{
    
    @Value("${clerk.issuer}")
    private String clerkIssuer;

    private final ClerkJwksProvider jwksProvider;
  

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Allow webhooks to bypass authentication
        if(request.getRequestURI().contains("/webhooks")||(request.getRequestURI().contains("/public"))){
            filterChain.doFilter(request,response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        System.out.println("[DEBUG] Authorization header: " + authHeader);

        if(authHeader == null || !authHeader.startsWith("Bearer ")){
            System.out.println("[DEBUG] Authorization header missing or invalid");
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Authorization Header is not valid");
            return;
        }

        try {
            String token = authHeader.substring(7);
            System.out.println("[DEBUG] JWT token received: " + token);

            // Split JWT into 3 parts
            String[] chunks = token.split("\\.");
            if(chunks.length < 3){
                System.out.println("[DEBUG] JWT does not have 3 parts");
                response.sendError(HttpServletResponse.SC_FORBIDDEN,"Invalid JWT token");
                return;
            }

            // Decode header and check kid
            String headerJson = new String(Base64.getUrlDecoder().decode(chunks[0]));
            ObjectMapper mapper = new ObjectMapper();
            JsonNode headerNode = mapper.readTree(headerJson);
            
            // Log the entire header for debugging
            System.out.println("[DEBUG] JWT header content: " + headerJson);

            // Safe check for 'kid' field
            JsonNode kidNode = headerNode.get("kid");
            if(kidNode == null || kidNode.isNull()) {
                System.out.println("[DEBUG] Token header is missing 'kid' field. Available fields: " + headerNode.fieldNames());
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Token header is missing 'kid' field");
                return;
            }

            String kid = kidNode.asText();
            if(kid == null || kid.trim().isEmpty()) {
                System.out.println("[DEBUG] 'kid' field is empty or null");
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "'kid' field is empty");
                return;
            }

            System.out.println("[DEBUG] Using kid: " + kid);
            PublicKey publicKey = jwksProvider.getPublicKey(kid);

            if(publicKey == null) {
                System.out.println("[DEBUG] No public key found for kid: " + kid);
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Unable to find public key for token");
                return;
            }

            // Parse claims using public key
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(publicKey)
                    .setAllowedClockSkewSeconds(6000)
                    .requireIssuer(clerkIssuer)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            System.out.println("[DEBUG] JWT claims: subject=" + claims.getSubject() +
                    ", issuer=" + claims.getIssuer() +
                    ", exp=" + claims.getExpiration());

            // Validate essential claims
            if(claims.getSubject() == null || claims.getSubject().trim().isEmpty()) {
                System.out.println("[DEBUG] JWT subject is missing or empty");
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid token: missing subject");
                return;
            }

            // Set Spring Authentication
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(
                            claims.getSubject(),
                            null,
                            Collections.singleton(new SimpleGrantedAuthority("ROLE_ADMIN"))
                    );
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);

            System.out.println("[DEBUG] Authentication set for user: " + claims.getSubject());

            // Proceed with the filter chain
            filterChain.doFilter(request,response);

        } catch(io.jsonwebtoken.ExpiredJwtException e) {
            System.out.println("[DEBUG] JWT token expired: " + e.getMessage());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token expired");
        } catch(io.jsonwebtoken.MalformedJwtException e) {
            System.out.println("[DEBUG] JWT token malformed: " + e.getMessage());
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Malformed JWT token");
        } catch(io.jsonwebtoken.security.SignatureException e) {
            System.out.println("[DEBUG] JWT signature validation failed: " + e.getMessage());
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid JWT signature");
        } catch(Exception e){
            System.out.println("[DEBUG] JWT validation failed: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            e.printStackTrace(); // Add stack trace for better debugging
            response.sendError(HttpServletResponse.SC_FORBIDDEN,"Invalid JWT token");
        }
    }
   
}
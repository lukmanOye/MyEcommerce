package com.example.ecommerce.config;

import com.example.ecommerce.model.User;
import com.example.ecommerce.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {
    private static final Logger logger = LoggerFactory.getLogger(OAuth2SuccessHandler.class);

    private final ObjectMapper objectMapper;
    private final UserService userService;

    @Lazy
    @Autowired
    public OAuth2SuccessHandler(ObjectMapper objectMapper, UserService userService) {
        this.objectMapper = objectMapper;
        this.userService = userService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String provider = ((OAuth2AuthenticationToken) authentication).getAuthorizedClientRegistrationId();
        Map<String, Object> attributes = oAuth2User.getAttributes();

        logger.info("Processing OAuth2 success for provider: {}", provider);
        logger.debug("OAuth2 attributes: {}", attributes);

        String email = extractEmail(attributes, provider);
        String name = extractName(attributes, provider);

        if (email == null || email.isBlank()) {
            if ("github".equals(provider)) {
                Object id = attributes.get("id");
                email = id != null ? "gh_" + id + "@github.com" : null;
                logger.debug("Generated fallback email for GitHub: {}", email);
            }
            if (email == null) {
                logger.error("Email not found for provider: {}", provider);
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.getWriter().write("{\"error\":\"Email not found in OAuth2 attributes\"}");
                return;
            }
        }

        try {
            User user = userService.createOrUpdateOAuthUser(email, name);
            logger.info("User saved to database: email={}, name={}, provider={}", email, name, provider);

            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("status", "success");
            responseBody.put("user", Map.of(
                    "id", user.getUserId(),
                    "email", user.getEmail(),
                    "name", user.getName() != null ? user.getName() : provider + " User",
                    "roles", user.getRoles().stream().map(role -> Map.of("id", role.getRoleId(), "name", role.getName())).toList()
            ));

            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setStatus(HttpServletResponse.SC_OK);
            objectMapper.writeValue(response.getWriter(), responseBody);
        } catch (Exception e) {
            logger.error("Failed to save user for provider {}: {}", provider, e.getMessage(), e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("{\"error\":\"Failed to process OAuth2 user: " + e.getMessage() + "\"}");
        }
    }

    private String extractEmail(Map<String, Object> attributes, String provider) {
        switch (provider) {
            case "google":
                String email = (String) attributes.get("email");
                logger.debug("Extracted email for Google: {}", email);
                return email;
            case "github":
                email = (String) attributes.get("email");
                if (email == null) {
                    Object id = attributes.get("id");
                    email = id != null ? "gh_" + id + "@github.com" : null;
                    logger.debug("Generated fallback email for GitHub: {}", email);
                }
                return email;
            default:
                return null;
        }
    }

    private String extractName(Map<String, Object> attributes, String provider) {
        switch (provider) {
            case "google":
                String name = (String) attributes.get("name");
                logger.debug("Extracted name for Google: {}", name);
                return name;
            case "github":
                name = (String) attributes.get("login");
                logger.debug("Extracted name for GitHub: {}", name);
                return name;
            default:
                return provider + " User";
        }
    }
}
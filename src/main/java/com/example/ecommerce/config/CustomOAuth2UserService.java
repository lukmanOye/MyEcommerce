package com.example.ecommerce.config;

import com.example.ecommerce.model.User;
import com.example.ecommerce.model.UserPrincipal;
import com.example.ecommerce.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private static final Logger logger = LoggerFactory.getLogger(CustomOAuth2UserService.class);
    private final UserService userService;

    @Lazy
    @Autowired
    public CustomOAuth2UserService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        try {
            OAuth2User oauth2User = super.loadUser(userRequest);
            String provider = userRequest.getClientRegistration().getRegistrationId();
            Map<String, Object> attributes = oauth2User.getAttributes();

            logger.debug("Processing OAuth2 user from provider: {}", provider);
            logger.trace("OAuth2 attributes: {}", attributes);

            String email = extractEmail(attributes, provider);
            String name = extractName(attributes, provider);

            validateAttributes(email, name, provider, attributes);

            User user = userService.createOrUpdateOAuthUser(email, name);
            logger.info("Successfully processed OAuth user - Email: {}, Name: {}, Provider: {}", email, name, provider);

            return new UserPrincipal(user, attributes);
        } catch (Exception ex) {
            logger.error("OAuth2 user processing failed for provider {}: {}", userRequest.getClientRegistration().getRegistrationId(), ex.getMessage(), ex);
            throw new OAuth2AuthenticationException("Failed to process OAuth2 user: " + ex.getMessage());
        }
    }

    private void validateAttributes(String email, String name, String provider, Map<String, Object> attributes) {
        if (email == null || email.isBlank()) {
            logger.warn("Email not found in OAuth2 attributes from {}. Generating fallback email.", provider);
            if ("github".equals(provider)) {
                Object id = attributes.get("id");
                if (id != null) {
                    email = "gh_" + id + "@github.com";
                } else {
                    throw new IllegalStateException("Email and ID not found in GitHub attributes");
                }
            } else if ("google".equals(provider)) {
                Object sub = attributes.get("sub"); // Google unique ID
                if (sub != null) {
                    email = "google_" + sub + "@no-email.com";
                } else {
                    throw new IllegalStateException("Email and sub not found in Google attributes");
                }
            } else {
                throw new IllegalStateException("Email not found in OAuth2 attributes from " + provider);
            }
        }
        if (name == null || name.isBlank()) {
            logger.warn("Name not found in OAuth2 attributes from {}. Using default name.", provider);
            name = provider + " User";
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
                throw new IllegalArgumentException("Unsupported provider: " + provider);
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
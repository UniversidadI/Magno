package com.unibague.magno.infrastructure.configuration.security.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Profile;

import java.io.IOException;

/**
 * Custom handler for successful OAuth2 authentication.
 * Sets the Google ID token in an HTTP-only cookie for subsequent API requests
 * and redirects the user to the frontend application.
 */
@Component
@RequiredArgsConstructor
@Profile("!test")
public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final OAuth2AuthorizedClientService authorizedClientService;

    @Value("${app.frontend.success-redirect}")
    private String successRedirectUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OidcUser oidcUser = (OidcUser) authentication.getPrincipal();

        String googleIdToken = oidcUser.getIdToken().getTokenValue();

        ResponseCookie cookie = ResponseCookie.from("MAGNO_SESSION", googleIdToken)
                .httpOnly(true)
                .secure(request.isSecure())
                .path("/")
                .maxAge(4 * 60 * 60)
                .sameSite("Lax")
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
        response.sendRedirect(successRedirectUrl.strip());
    }

}


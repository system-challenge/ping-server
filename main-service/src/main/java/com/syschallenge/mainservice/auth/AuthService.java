/*
 * Copyright by the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.syschallenge.mainservice.auth;

import com.syschallenge.mainservice.auth.response.GoogleAuthResponse;
import com.syschallenge.mainservice.oauth.GoogleOAuthV4Service;
import com.syschallenge.mainservice.oauth.model.GoogleOAuthIdTokenUser;
import com.syschallenge.mainservice.property.GoogleOAuthProperty;
import com.syschallenge.mainservice.security.UserDetails;
import com.syschallenge.mainservice.security.jwt.JwtUtil;
import com.syschallenge.mainservice.user.UserLinkedSocialRepository;
import com.syschallenge.mainservice.user.UserRepository;
import com.syschallenge.mainservice.user.model.User;
import com.syschallenge.mainservice.user.model.UserLinkedSocial;
import com.syschallenge.mainservice.user.model.UserLinkedSocialType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

/**
 * Service for handling authentication-related operations
 *
 * @author panic08
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtUtil jwtUtil;
    private final GoogleOAuthV4Service googleOAuthService;
    private final GoogleOAuthProperty googleOAuthProperty;
    private final UserRepository userRepository;
    private final UserLinkedSocialRepository userLinkedSocialRepository;


    /**
     * Authenticates a user through Google OAuth using an authorization code
     *
     * @param code authorization code provided by Google OAuth
     * @return response containing the Google authentication response with the JWT token
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public GoogleAuthResponse authByGoogle(String code) {
        GoogleOAuthIdTokenUser googleOAuthIdTokenUser =
                googleOAuthService.extractGoogleOAuthIdTokenUser(googleOAuthProperty.getClientId(),
                        googleOAuthProperty.getClientSecret(), googleOAuthProperty.getRedirectUri(),
                        code, googleOAuthProperty.getGrantType());

        if (userLinkedSocialRepository.existsByVerification(googleOAuthIdTokenUser.userId())) {
            User currentUser = userRepository.findById(
                    userLinkedSocialRepository.findUserIdByVerification(googleOAuthIdTokenUser.userId())
            );

            String jwtToken = jwtUtil.generateToken(new UserDetails(currentUser.getId()));

            return new GoogleAuthResponse(jwtToken);
        } else {
            User newUser = userRepository.save(
                    User.builder()
                            .email(googleOAuthIdTokenUser.email())
                            .registeredAt(LocalDateTime.now())
                            .build()
            );

            CompletableFuture.runAsync(() -> {
                userLinkedSocialRepository.save(
                        UserLinkedSocial.builder()
                                .userId(newUser.getId())
                                .type(UserLinkedSocialType.GOOGLE)
                                .verification(googleOAuthIdTokenUser.userId())
                                .build()
                );
            });

            String jwtToken = jwtUtil.generateToken(new UserDetails(newUser.getId()));

            return new GoogleAuthResponse(jwtToken);
        }
    }
}

/*
 * Copyright by the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.syschallenge.mainservice.security.jwt;

import com.syschallenge.mainservice.security.UserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * @author panic08
 * @since 1.0.0
 */
class JwtUtilTest {
    @InjectMocks
    private JwtUtil jwtUtil;

    @Mock
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(userDetails.getUsername()).thenReturn("testUUID");

        ReflectionTestUtils.setField(jwtUtil, "SECRET", "Z3dlZ2VHR0dHRVdnd2Vhc2Rhc2RzYWRhc2Rhc2RzYWQ=");
    }

    @Test
    void testGenerateToken() {
        String token = jwtUtil.generateToken(userDetails);
        assertNotNull(token);
        assertEquals("testUUID", jwtUtil.extractIdFromToken(token));
    }

    @Test
    void testExtractExpiration() {
        String token = jwtUtil.generateToken(userDetails);
        Date expiration = jwtUtil.extractExpiration(token);
        assertNotNull(expiration);
        assertTrue(expiration.after(new Date()));
    }

    @Test
    void testIsTokenValid() {
        String token = jwtUtil.generateToken(userDetails);
        assertTrue(jwtUtil.isTokenValid(token));
    }

    @Test
    void testIsTokenExpired() {
        String token = jwtUtil.generateToken(userDetails);
        assertFalse(jwtUtil.isTokenExpired(token));
    }

    @Test
    void testExtractIdFromToken() {
        String token = jwtUtil.generateToken(userDetails);
        assertEquals("testUUID", jwtUtil.extractIdFromToken(token));
    }
}
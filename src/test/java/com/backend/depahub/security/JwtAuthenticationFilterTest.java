package com.backend.depahub.security;

import com.backend.depahub.services.JwtService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private FilterChain filterChain;

    private JwtAuthenticationFilter filter;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthenticationFilter(jwtService);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void continuaSinAutenticarSiNoHayHeaderAuthorization() throws Exception {
        filter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(jwtService, never()).extraerEmail(any());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void continuaSinAutenticarSiElHeaderNoEsBearer() throws Exception {
        request.addHeader("Authorization", "Basic credenciales");

        filter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(jwtService, never()).extraerEmail(any());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void autenticaConElEmailDelTokenValido() throws Exception {
        request.addHeader("Authorization", "Bearer token.valido");
        when(jwtService.extraerEmail("token.valido")).thenReturn("admin@depahub.test");

        filter.doFilter(request, response, filterChain);

        assertEquals("admin@depahub.test", SecurityContextHolder.getContext().getAuthentication().getName());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void noPisaUnaAutenticacionYaExistente() throws Exception {
        UsernamePasswordAuthenticationToken existente =
                new UsernamePasswordAuthenticationToken("ya@depahub.test", null, List.of());
        SecurityContextHolder.getContext().setAuthentication(existente);
        request.addHeader("Authorization", "Bearer token.valido");
        when(jwtService.extraerEmail("token.valido")).thenReturn("nuevo@depahub.test");

        filter.doFilter(request, response, filterChain);

        assertEquals("ya@depahub.test", SecurityContextHolder.getContext().getAuthentication().getName());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void responde401SiElTokenEsInvalido() throws Exception {
        request.addHeader("Authorization", "Bearer token.invalido");
        when(jwtService.extraerEmail("token.invalido")).thenThrow(new JwtException("token invalido"));

        filter.doFilter(request, response, filterChain);

        assertEquals(401, response.getStatus());
        assertEquals("application/json", response.getContentType());
        assertEquals("{\"message\":\"Token inválido o expirado\"}", response.getContentAsString());
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void responde401SiElTokenProvocaUnArgumentoInvalido() throws Exception {
        request.addHeader("Authorization", "Bearer ");
        when(jwtService.extraerEmail("")).thenThrow(new IllegalArgumentException("token vacio"));

        filter.doFilter(request, response, filterChain);

        assertEquals(401, response.getStatus());
        assertEquals("{\"message\":\"Token inválido o expirado\"}", response.getContentAsString());
        verify(filterChain, never()).doFilter(request, response);
    }
}

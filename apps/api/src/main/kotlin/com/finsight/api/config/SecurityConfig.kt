package com.finsight.api.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.cors.CorsConfigurationSource


@Configuration
class SecurityConfig {
    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .cors { }              // 아래 CORS bean 사용
            .csrf { it.disable() } // SPA/REST면 disable가 편함(쿠키 기반이면 별도 처리)
            .authorizeHttpRequests {
                it.requestMatchers("/",
                    "/login",
                    "/error",
                    "/favicon.ico",
                    "/actuator/health").permitAll()
                it.requestMatchers(
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/static/**"
                ).permitAll()
                it.requestMatchers(HttpMethod.GET, "/public/**").permitAll()
                // 인증이 필요한 페이지
                it.requestMatchers(
                    "/dashboard",
                    "/test.html",
                    "/agent-test.html"
                ).authenticated()
                it.requestMatchers("/api/**").authenticated()
                it.anyRequest().permitAll()
            }
            .oauth2Login { oauth2 ->
                oauth2
                    .loginPage("/login")
                    .successHandler(oauth2SuccessHandler())
                    .failureUrl("/login?error=true")
            }
            .logout { logout ->
                logout
                    .logoutSuccessUrl("/")
                    .deleteCookies("JSESSIONID")
                    .invalidateHttpSession(true)
            }

        // (옵션) 리소스 서버 JWT 검증을 켜려면:
        // http.oauth2ResourceServer { it.jwt(Customizer.withDefaults()) }

        return http.build()
    }

    @Bean
    fun oauth2SuccessHandler(): AuthenticationSuccessHandler {
        return AuthenticationSuccessHandler { request, response, authentication ->
            // 로그인 성공 시 대시보드로 리다이렉션
            response.sendRedirect("/dashboard")
        }
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val config = CorsConfiguration().apply {
            allowedOrigins = listOf("http://localhost:3000", "http://localhost:5173")
            allowedMethods = listOf("GET","POST","PUT","PATCH","DELETE","OPTIONS")
            allowedHeaders = listOf("*")
            allowCredentials = true
        }
        return UrlBasedCorsConfigurationSource().also {
            it.registerCorsConfiguration("/**", config)
        }
    }
}
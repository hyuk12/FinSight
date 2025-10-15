package com.finsight.api.config

import org.springframework.web.servlet.config.annotation.ViewControllerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

class WebMvcConfig: WebMvcConfigurer {
    override fun addViewControllers(registry: ViewControllerRegistry) {
        // /login 경로를 login.html로 매핑
        registry.addViewController("/login").setViewName("forward:/login.html")

        // /dashboard 경로를 dashboard.html로 매핑
        registry.addViewController("/dashboard").setViewName("forward:/dashboard.html")
    }
}
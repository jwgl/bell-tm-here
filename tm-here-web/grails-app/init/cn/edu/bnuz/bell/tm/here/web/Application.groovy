package cn.edu.bnuz.bell.tm.here.web

import cn.edu.bnuz.bell.config.ExternalConfigLoader
import cn.edu.bnuz.bell.menu.module.EnableModuleMenu
import cn.edu.bnuz.bell.report.EnableReportClient
import grails.boot.GrailsApp
import grails.boot.config.GrailsAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.cloud.netflix.eureka.EnableEurekaClient
import org.springframework.context.EnvironmentAware
import org.springframework.core.env.Environment
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer

@SpringBootApplication
@EnableResourceServer
@EnableEurekaClient
@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableModuleMenu
@EnableReportClient
class Application extends GrailsAutoConfiguration implements EnvironmentAware {
    static void main(String[] args) {
        GrailsApp.run(Application, args)
    }

    @Override
    void setEnvironment(Environment environment) {
        ExternalConfigLoader.load(environment)
    }
}
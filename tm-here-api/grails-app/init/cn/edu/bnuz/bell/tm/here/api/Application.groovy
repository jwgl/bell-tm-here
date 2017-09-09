package cn.edu.bnuz.bell.tm.here.api

import cn.edu.bnuz.bell.config.ExternalConfigLoader
import cn.edu.bnuz.bell.here.FreeListenSettings
import grails.boot.GrailsApp
import grails.boot.config.GrailsAutoConfiguration
import grails.converters.JSON
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
class Application extends GrailsAutoConfiguration implements EnvironmentAware {
    static void main(String[] args) {
        GrailsApp.run(Application, args)
    }

    @Override
    void setEnvironment(Environment environment) {
        ExternalConfigLoader.load(environment)
    }

    @Override
    Closure doWithSpring() {
        { ->
            JSON.registerObjectMarshaller(FreeListenSettings) { FreeListenSettings it ->
                [
                        term          : it.term.id,
                        applyStartDate: it.applyStartDate,
                        applyEndDate  : it.applyEndDate,
                        checkStartDate: it.checkStartDate,
                        checkEndDate  : it.checkEndDate,
                        today         : it.today,
                ]
            }
        }
    }
}
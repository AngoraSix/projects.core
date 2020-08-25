package com.angorasix.projects.core

import com.angorasix.projects.core.application.ProjectService
import com.angorasix.projects.core.presentation.handler.ProjectHandler
import com.angorasix.projects.core.presentation.router.ProjectRouter
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.support.GenericApplicationContext
import org.springframework.context.support.beans

val beans = beans {
    bean<ProjectService>()
    bean<ProjectHandler>()
    bean {
        ProjectRouter(ref()).projectRouterFunction()
    }
}

class BeansInitializer : ApplicationContextInitializer<GenericApplicationContext> {
    override fun initialize(context: GenericApplicationContext) = beans.initialize(context)

}

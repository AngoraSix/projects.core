package com.angorasix.projects.core

import com.angorasix.projects.core.application.ProjectService
import com.angorasix.projects.core.infrastructure.persistence.converter.ZonedDateTimeConvertersUtils
import com.angorasix.projects.core.presentation.handler.ProjectHandler
import com.angorasix.projects.core.presentation.router.ProjectRouter
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.support.GenericApplicationContext
import org.springframework.context.support.beans
import org.springframework.data.mongodb.core.convert.MongoCustomConversions

val beans = beans {
    bean<MongoCustomConversions> {
        MongoCustomConversions(
            listOf(
                ref<ZonedDateTimeConvertersUtils.ZonedDateTimeReaderConverter>(),
                ref<ZonedDateTimeConvertersUtils.ZonedDateTimeWritingConverter>(),
            ),
        )
    }
    bean<ProjectService>()
    bean<ProjectHandler>()
    bean {
        ProjectRouter(ref(), ref()).projectRouterFunction()
    }
}

class BeansInitializer : ApplicationContextInitializer<GenericApplicationContext> {
    override fun initialize(context: GenericApplicationContext) = beans.initialize(context)
}

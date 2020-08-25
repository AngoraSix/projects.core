package com.angorasix.projects.core.integration


/**
 *
 *
 * @author rozagerardo
 */

//val beans = beans {
    /*   bean<ProjectService>()
       bean<ProjectHandler>()
       bean {
           ProjectRouter(ref()).projectRouterFunction()
       }*/
//    bean<IMongoImportConfig> {
//        val jsonFile = env.getProperty("integration.mongodb.importJsonFile")
//        MongoImportConfigBuilder().db("projects.core").collection("projects").version(Version.Main.DEVELOPMENT).net(Net(
//                getHost().hostAddress,
//                Network.getFreeServerPort(getHost()),
//                Network.localhostIsIPv6())).jsonArray(true).upsert(true).dropCollection(true).importFile(
//                ClassPathResource(jsonFile!!).path).build()
//    }
//    bean<MongoImportExecutable> {
//        MongoImportStarter.getDefaultInstance().prepare(ref<IMongoImportConfig>());
//    }
//}
//
//class IntegrationBeansInitializer : ApplicationContextInitializer<GenericApplicationContext> {
//    override fun initialize(context: GenericApplicationContext) = beans.initialize(context)
//
//}


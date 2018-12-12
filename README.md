# spring-boot-with-spring-batch-admin

[![Build Status](https://travis-ci.org/damienbeaufils/spring-boot-with-spring-batch-admin.svg?branch=master)](https://travis-ci.org/damienbeaufils/spring-boot-with-spring-batch-admin)

An example project that shows how to run Spring Batch Admin within a Spring Boot application. Because it could be a bit tricky.

Run the application and go to http://localhost:8080. You should see the Spring Batch Admin manager and be able to run a sample job that converts csv to xml.

## Brief explanation of the tricks

* Do not use `@EnableBatchProcessing` because there will be a conflict with the beans that are already declared in Spring Batch Admin XML webapp config
  * see [BatchAdminConfiguration.java](src/main/java/com/example/demo/admin/BatchAdminConfiguration.java)
* Use `@EnableWebMvc` to import Spring MVC configuration which is needed at least for Spring Batch Admin View Resolver
  * see [BatchAdminConfiguration.java](src/main/java/com/example/demo/admin/BatchAdminConfiguration.java)
* Create an override of Spring Batch Admin `webapp-config.xml` that does not import `data-source-context.xml` in order to create and manage datasource using Spring Boot default behavior
  * see [batch-admin-webapp-config-override.xml](src/main/resources/batch-admin-webapp-config-override.xml)
* Because we do not use `@EnableBatchProcessing`, we have to manually declare beans for `JobBuilderFactory` and `StepBuilderFactory`
  * see [BatchAdminConfiguration.java](src/main/java/com/example/demo/admin/BatchAdminConfiguration.java)
* Load Spring Batch Admin `servlet-config.xml` using a `ServletRegistrationBean` 
  * see [BatchAdminConfiguration.java](src/main/java/com/example/demo/admin/BatchAdminConfiguration.java)
* Exclude Spring Boot `FreeMarkerAutoConfiguration` to avoid conflict with `FreeMarkerConfig` bean declared in Spring Batch Admin
  * see [SpringBootWithSpringBatchAdminApplication.java](src/main/java/com/example/demo/SpringBootWithSpringBatchAdminApplication.java)
* In order to load in Spring Batch Admin the jobs declared with Java `@Bean` annotation, add an XML context configuration file in `META-INF/spring/batch/jobs` that uses annotations and scans package
  * see [detect-jobs-declared-with-bean-annotations.xml](src/main/resources/META-INF/spring/batch/jobs/detect-jobs-declared-with-bean-annotations.xml)
* Disable batch jobs auto-start with `spring.batch.job.enabled` configuration property
  * see [application.yml](src/main/resources/application.yml)

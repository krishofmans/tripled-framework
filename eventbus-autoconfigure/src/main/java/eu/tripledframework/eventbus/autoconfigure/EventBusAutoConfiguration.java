package eu.tripledframework.eventbus.autoconfigure;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import eu.tripledframework.eventbus.domain.EventBusInterceptor;
import eu.tripledframework.eventbus.domain.asynchronous.AsynchronousEventBus;
import eu.tripledframework.eventbus.domain.interceptor.LoggingEventBusInterceptor;
import eu.tripledframework.eventbus.domain.interceptor.ValidatingEventBusInterceptor;
import eu.tripledframework.eventbus.domain.synchronous.SynchronousEventBus;

@Configuration
@EnableConfigurationProperties(EventBusProperties.class)
public class EventBusAutoConfiguration {

  @Bean
  @ConditionalOnProperty(value = "eu.tripledframework.eventbus.mode", havingValue = "sync")
  public SynchronousEventBus synchronousEventBus() {
    List<EventBusInterceptor> interceptors =
        Arrays.asList(new LoggingEventBusInterceptor(),
            new ValidatingEventBusInterceptor(localValidatorFactoryBean().getValidator()));

    return new SynchronousEventBus(interceptors);
  }

  @Bean
  @ConditionalOnProperty(value = "eu.tripledframework.eventbus.mode", matchIfMissing = true, havingValue = "async")
  public AsynchronousEventBus asynchronousEventBus() {
    List<EventBusInterceptor> interceptors =
        Arrays.asList(new LoggingEventBusInterceptor(),
            new ValidatingEventBusInterceptor(localValidatorFactoryBean().getValidator()));

    return new AsynchronousEventBus(interceptors, taskExecutor());
  }

  @Bean
  @ConditionalOnProperty(value = "eu.tripledframework.eventbus.mode", matchIfMissing = true, havingValue = "async")
  public Executor taskExecutor() {
    ThreadPoolTaskExecutor executorService = new ThreadPoolTaskExecutor();
    executorService.setCorePoolSize(5);
    executorService.setMaxPoolSize(10);

    executorService.afterPropertiesSet();
    return executorService;
  }

  @Bean
  @ConditionalOnMissingBean(LocalValidatorFactoryBean.class)
  public LocalValidatorFactoryBean localValidatorFactoryBean() {
    return new LocalValidatorFactoryBean();
  }


}

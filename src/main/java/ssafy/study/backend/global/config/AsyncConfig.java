package ssafy.study.backend.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class AsyncConfig implements WebMvcConfigurer {

	@Override
	public void configureAsyncSupport(AsyncSupportConfigurer configurer) {

		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

		// 프리티어 t3.micro 인스턴스 기준으로 설정
		executor.setCorePoolSize(2);
		executor.setMaxPoolSize(8);
		executor.setQueueCapacity(500);
		executor.setAllowCoreThreadTimeOut(true);
		executor.setThreadNamePrefix("sse-exec-");

		executor.initialize();

		configurer.setTaskExecutor(executor);
	}
}

package com.jspark.rabbitMQClient;

import com.jspark.rabbitMQClient.javaFX.StageReadyEvent;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

public class RabbitMqToolApplication extends Application {

	private ConfigurableApplicationContext applicationContext;

	@Override
	public void init() {
		// Spring Boot 컨텍스트 초기화
		applicationContext = new SpringApplicationBuilder(SpringBootMain.class).run();
	}

	@Override
	public void start(Stage stage) {
		// Spring 컨텍스트에서 FXML 로더와 UI 컴포넌트를 가져와 화면을 띄움
		applicationContext.publishEvent(new StageReadyEvent(stage));
	}

	@Override
	public void stop() {
		// 애플리케이션 종료 시 Spring 컨텍스트 종료
		applicationContext.close();
		Platform.exit();
	}
}

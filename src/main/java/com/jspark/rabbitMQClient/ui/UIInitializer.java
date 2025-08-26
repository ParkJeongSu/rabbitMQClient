package com.jspark.rabbitMQClient.ui;

import com.jspark.rabbitMQClient.javaFX.StageReadyEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class UIInitializer implements ApplicationListener<StageReadyEvent> {

    @Value("classpath:/MainView.fxml")
    private Resource fxml;

    private final ApplicationContext applicationContext;

    public UIInitializer(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void onApplicationEvent(StageReadyEvent event) {
        try {
            Stage stage = event.getStage();
            FXMLLoader fxmlLoader = new FXMLLoader(fxml.getURL());
            // Spring 컨텍스트가 컨트롤러를 관리하도록 설정
            fxmlLoader.setControllerFactory(applicationContext::getBean);
            Parent root = fxmlLoader.load();
            Scene scene = new Scene(root, 600, 400);
            stage.setScene(scene);
            stage.setTitle("RabbitMQ Tool");
            stage.show();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
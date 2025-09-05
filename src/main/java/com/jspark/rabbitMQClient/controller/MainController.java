package com.jspark.rabbitMQClient.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MainController {

    @FXML private TextField exchangeField;
    @FXML private TextField routingKeyField;
    @FXML private TextArea messageToSendArea;
    @FXML private TextArea receivedMessagesArea;

    private final RabbitTemplate rabbitTemplate;

    @FXML
    private void handleSendAction() {
        String exchange = exchangeField.getText();
        String routingKey = routingKeyField.getText();
        String message = messageToSendArea.getText();
        rabbitTemplate.convertAndSend(exchange, routingKey, message);
        messageToSendArea.clear();
    }

}
package com.jspark.rabbitMQClient.controller;

import com.jspark.rabbitMQClient.service.RabbitMQService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MainController {

    @FXML private TextField exchangeField;
    @FXML private TextField routingKeyField;
    @FXML private TextArea messageToSendArea;
    @FXML private TextArea receivedMessagesArea;

    private final RabbitMQService rabbitMQService;

    @FXML
    private void handleSendAction() {
        String exchange = exchangeField.getText();
        String routingKey = routingKeyField.getText();
        String message = messageToSendArea.getText();
        rabbitMQService.sendMessage(exchange, routingKey, message);
        messageToSendArea.clear();
    }

}
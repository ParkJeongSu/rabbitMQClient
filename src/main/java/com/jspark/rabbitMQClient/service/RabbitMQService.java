package com.jspark.rabbitMQClient.service;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RabbitMQService {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void sendMessage(String exchange, String routingKey, String message) {
        rabbitTemplate.convertAndSend(exchange, routingKey, message);
    }

    public void sendMessage( String message) {
        rabbitTemplate.convertAndSend(message);
    }
    public void sendMessage( String routingkey,String message) {
        rabbitTemplate.convertAndSend(routingkey,message);
    }
}
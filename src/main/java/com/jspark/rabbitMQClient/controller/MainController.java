package com.jspark.rabbitMQClient.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Optional;
import java.util.stream.Stream;

import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;

import jakarta.annotation.PostConstruct;

@Lazy
@Component
@RequiredArgsConstructor
public class MainController {

    @FXML private TextField exchangeField;
    @FXML private TextField routingKeyField;
    @FXML private TextArea messageToSendArea;
    @FXML private TextArea receivedMessagesArea;
    @FXML private ListView<String> jsonFilesListView;

    private final RabbitTemplate rabbitTemplate;

    @FXML
    private void handleSendAction() {
        String exchange = exchangeField.getText();
        String routingKey = routingKeyField.getText();
        String message = messageToSendArea.getText();
        // 새로운 스레드에서 메시지 전송 및 응답 수신을 처리
        new Thread(() -> {
            try {
                // convertAndSendAndReceive를 사용하여 응답을 기다립니다.
                // 10초 타임아웃은 RabbitConfig에 이미 설정되어 있습니다.
                Object result = rabbitTemplate.convertSendAndReceive(exchange, routingKey, message);

                // UI 업데이트는 JavaFX Application Thread에서 실행
                Platform.runLater(() -> {
                    if (result != null) {
                        receivedMessagesArea.appendText("응답 수신: " + result.toString() + "\n");
                    } else {
                        // 타임아웃 발생 시
                        receivedMessagesArea.appendText("응답 타임아웃 (10초)\n");
                    }
                });

            } catch (AmqpException e) {
                // 메시지 전송 실패 시 에러 처리
                Platform.runLater(() -> {
                    Alert alert = new Alert(AlertType.ERROR);
                    alert.setTitle("RabbitMQ 오류");
                    alert.setHeaderText("메시지 전송 실패");
                    alert.setContentText("메시지를 보내는 도중 오류가 발생했습니다: " + e.getMessage());
                    alert.showAndWait();
                });
            }
        }).start();
    }

    @PostConstruct
    public void initialize() {
        System.out.println("Initializing MainController and loading JSON files...");
        loadJsonFiles();
    }

    // JAR 파일이 실행되는 경로를 기준으로 json 폴더의 경로를 가져오는 메서드
    private Path getJsonFolderPath() {
        return Paths.get(System.getProperty("user.dir")).resolve("json");
    }
    /*
    private Path getJsonFolderPath() {
        try {
            String classPath = MainController.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
            if (classPath.endsWith(".jar")) {
                return Path.of(classPath).getParent().resolve("json");
            } else {
                return Path.of(System.getProperty("user.dir")).resolve("json");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    */
    private void loadJsonFiles() {

        if(jsonFilesListView == null)
        {
            return;
        }
        jsonFilesListView.getItems().clear();
        Path jsonFolderPath = getJsonFolderPath();
        if (jsonFolderPath == null || !Files.exists(jsonFolderPath) || !Files.isDirectory(jsonFolderPath)) {
            System.err.println("JSON 폴더를 찾을 수 없습니다: " + jsonFolderPath);
            return;
        }

        try (Stream<Path> files = Files.list(jsonFolderPath)) {
            files.filter(Files::isRegularFile)
                    .filter(p -> p.toString().toLowerCase().endsWith(".json"))
                    .map(p -> p.getFileName().toString())
                    .forEach(fileName -> jsonFilesListView.getItems().add(fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleJsonFileSelection(MouseEvent event) {
        if (event.getClickCount() == 1) {
            String selectedFile = jsonFilesListView.getSelectionModel().getSelectedItem();
            if (selectedFile != null) {
                try {
                    Path filePath = getJsonFolderPath().resolve(selectedFile);
                    String jsonContent = Files.readString(filePath);
                    messageToSendArea.setText(jsonContent);
                } catch (IOException e) {
                    e.printStackTrace();
                    messageToSendArea.setText("파일을 읽어오는 데 실패했습니다: " + selectedFile);
                }
            }
        }
    }

    @FXML
    private void handleAddAction() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("새 파일 추가");
        dialog.setHeaderText("새로운 JSON 파일 이름을 입력하세요.");
        dialog.setContentText("파일 이름:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(fileName -> {
            if (!fileName.toLowerCase().endsWith(".json")) {
                fileName += ".json";
            }
            try {
                Path newFilePath = getJsonFolderPath().resolve(fileName);
                if (Files.exists(newFilePath)) {
                    Alert alert = new Alert(AlertType.ERROR);
                    alert.setTitle("오류");
                    alert.setHeaderText("파일 생성 실패");
                    alert.setContentText("동일한 이름의 파일이 이미 존재합니다.");
                    alert.showAndWait();
                } else {
                    Files.createFile(newFilePath);
                    loadJsonFiles(); // 목록 갱신
                    Alert alert = new Alert(AlertType.INFORMATION);
                    alert.setTitle("성공");
                    alert.setHeaderText(null);
                    alert.setContentText(fileName + " 파일이 성공적으로 생성되었습니다.");
                    alert.showAndWait();
                }
            } catch (IOException e) {
                e.printStackTrace();
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("오류");
                alert.setHeaderText("파일 생성 실패");
                alert.setContentText("파일을 생성하는 도중 오류가 발생했습니다.");
                alert.showAndWait();
            }
        });
    }

    @FXML
    private void handleModifyAction() {
        String selectedFile = jsonFilesListView.getSelectionModel().getSelectedItem();
        if (selectedFile == null) {
            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle("경고");
            alert.setHeaderText(null);
            alert.setContentText("수정할 파일을 선택해 주세요.");
            alert.showAndWait();
            return;
        }

        try {
            Path filePath = getJsonFolderPath().resolve(selectedFile);
            String newContent = messageToSendArea.getText();
            // 기존 내용을 덮어쓰기
            Files.writeString(filePath, newContent, StandardOpenOption.TRUNCATE_EXISTING);
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("성공");
            alert.setHeaderText(null);
            alert.setContentText(selectedFile + " 파일의 내용이 성공적으로 수정되었습니다.");
            alert.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("오류");
            alert.setHeaderText("파일 수정 실패");
            alert.setContentText("파일을 수정하는 도중 오류가 발생했습니다.");
            alert.showAndWait();
        }
    }

    @FXML
    private void handleDeleteAction() {
        String selectedFile = jsonFilesListView.getSelectionModel().getSelectedItem();
        if (selectedFile == null) {
            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle("경고");
            alert.setHeaderText(null);
            alert.setContentText("삭제할 파일을 선택해 주세요.");
            alert.showAndWait();
            return;
        }

        Alert confirm = new Alert(AlertType.CONFIRMATION);
        confirm.setTitle("파일 삭제");
        confirm.setHeaderText(selectedFile + " 파일을 삭제하시겠습니까?");
        confirm.setContentText("이 작업은 되돌릴 수 없습니다.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                Path filePath = getJsonFolderPath().resolve(selectedFile);
                Files.delete(filePath);
                loadJsonFiles(); // 목록 갱신
                messageToSendArea.clear(); // 메시지 입력창 초기화
                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setTitle("성공");
                alert.setHeaderText(null);
                alert.setContentText(selectedFile + " 파일이 성공적으로 삭제되었습니다.");
                alert.showAndWait();
            } catch (IOException e) {
                e.printStackTrace();
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("오류");
                alert.setHeaderText("파일 삭제 실패");
                alert.setContentText("파일을 삭제하는 도중 오류가 발생했습니다.");
                alert.showAndWait();
            }
        }
    }
}
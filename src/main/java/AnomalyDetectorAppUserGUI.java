import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Paths;

public class AnomalyDetectorAppUserGUI extends Application {

    private TextField testFilePathField;
    private File selectedTestFile;
    private TextArea resultTextArea;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Anomaly Detector User");

        Label selectTestFileLabel = new Label("Файл для проверки:");
        selectTestFileLabel.setStyle("-fx-font-size: 14;");
        testFilePathField = new TextField();
        testFilePathField.setPromptText("Выберите файл");
        testFilePathField.setStyle("-fx-font-size: 12;");
        testFilePathField.setEditable(false);
        testFilePathField.setPrefWidth(300);

        Button selectTestFileButton = new Button("Обзор");
        selectTestFileButton.setStyle("-fx-font-size: 14;");
        selectTestFileButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            selectedTestFile = fileChooser.showOpenDialog(primaryStage);
            if (selectedTestFile != null) {
                testFilePathField.setText(selectedTestFile.getPath());
            }
        });

        Button detectButton = new Button("Проверить на аномалии");
        detectButton.setStyle("-fx-font-size: 14;");
        detectButton.setOnAction(e -> {
            if (selectedTestFile != null) {
                try {
                    String fileContent = new String(Files.readAllBytes(Paths.get(selectedTestFile.getPath())));
                    String result = sendDetectionRequest(fileContent);
                    resultTextArea.setText(result);
                } catch (IOException | InterruptedException ex) {
                    resultTextArea.setText("Ошибка при отправке файла: " + ex.getMessage());
                }
            } else {
                resultTextArea.setText("Пожалуйста, выберите файл.");
            }
        });

        resultTextArea = new TextArea();
        resultTextArea.setEditable(false);
        resultTextArea.setStyle("-fx-font-size: 14;");
        resultTextArea.setPromptText("Результаты проверки будут отображены здесь");

        Button exitButton = new Button("Выход");
        exitButton.setStyle("-fx-font-size: 14;");
        exitButton.setOnAction(e -> primaryStage.close());

        HBox exitButtonContainer = new HBox(exitButton);
        exitButtonContainer.setAlignment(Pos.BOTTOM_RIGHT);
        exitButtonContainer.setPadding(new Insets(10));

        VBox mainContent = new VBox(10);
        mainContent.setPadding(new Insets(20));
        mainContent.getChildren().addAll(
                selectTestFileLabel, testFilePathField, selectTestFileButton,
                new Label(""),
                detectButton,
                new Label(""),
                resultTextArea);

        BorderPane layout = new BorderPane();
        layout.setCenter(mainContent);
        layout.setBottom(exitButtonContainer);

        Scene scene = new Scene(layout, 600, 400);
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    private String sendDetectionRequest(String data) throws IOException, InterruptedException {
        String serverUrl = "http://localhost:8080/anomalies/detect"; // URL вашего сервера
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl))
                .POST(HttpRequest.BodyPublishers.ofString(data))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }
}

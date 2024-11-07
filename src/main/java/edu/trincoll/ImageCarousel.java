package edu.trincoll;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ImageCarousel extends Application {
    private final VBox root = new VBox(10); // Use VBox to stack controls vertically
    private final StackPane imageContainer = new StackPane();
    private List<Image> images;
    private final ImageView imageView = new ImageView();
    private Iterator<Image> imageIterator;
    private Stage stage;
    private static final double DEFAULT_WIDTH = 800;
    private static final double DEFAULT_HEIGHT = 600;
    private static final double PADDING = 20;
    private final Map<Image, String> imageFilenames = new HashMap<>();
    private Button toggleFullScreenButton;
    private boolean wasMaximized = false;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        stage.setTitle("Image Carousel");

        images = loadImages();
        System.out.println("Successfully loaded " + images.size() + " images");

        if (images.isEmpty()) {
            System.err.println("No valid images found in resources directory!");
            return;
        }

        setupImageView();
        setupControls();
        setupRoot();

        Scene scene = new Scene(root, DEFAULT_WIDTH, DEFAULT_HEIGHT);
        setupKeyboardHandlers(scene);

        stage.setScene(scene);
        stage.show();

        startImageRotation();
    }

    private void setupControls() {
        toggleFullScreenButton = new Button("Toggle Full Screen (F11)");
        toggleFullScreenButton.setOnAction(e -> toggleFullScreen());
        // Center the button
        toggleFullScreenButton.setMaxWidth(Double.MAX_VALUE);
    }

    private void setupRoot() {
        root.setPadding(new Insets(PADDING));
        imageContainer.getChildren().add(imageView);
        root.getChildren().addAll(toggleFullScreenButton, imageContainer);

        // Make the image container fill available space
        VBox.setVgrow(imageContainer, javafx.scene.layout.Priority.ALWAYS);
    }

    private void setupKeyboardHandlers(Scene scene) {
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.F11) {
                toggleFullScreen();
            } else if (e.getCode() == KeyCode.ESCAPE && stage.isFullScreen()) {
                toggleFullScreen(); // Use the same toggle method for consistency
            }
        });

        // Add a full screen property listener to handle external full screen changes
        stage.fullScreenProperty().addListener((obs, wasFullScreen, isFullScreen) -> {
            if (!isFullScreen) {
                javafx.application.Platform.runLater(() -> {
                    if (wasMaximized) {
                        stage.setMaximized(true);
                    } else {
                        // Reset to default size first
                        stage.setWidth(DEFAULT_WIDTH);
                        stage.setHeight(DEFAULT_HEIGHT);
                        // Then adjust to current image
                        adjustCurrentImageSize();
                    }
                });
            }
        });
    }

    private void toggleFullScreen() {
        if (!stage.isFullScreen()) {
            // Store current window state before going full screen
            wasMaximized = stage.isMaximized();
            stage.setFullScreen(true);
        } else {
            stage.setFullScreen(false);
            // Add a small delay to let the stage exit full-screen before resizing
            javafx.application.Platform.runLater(() -> {
                if (wasMaximized) {
                    stage.setMaximized(true);
                } else {
                    // Reset to default size first to ensure clean resize
                    stage.setWidth(DEFAULT_WIDTH);
                    stage.setHeight(DEFAULT_HEIGHT);
                    // Then adjust to current image
                    adjustCurrentImageSize();
                }
            });
        }
        adjustCurrentImageSize();
    }

    private void setupImageView() {
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(DEFAULT_WIDTH - PADDING * 2);
        imageView.setFitHeight(DEFAULT_HEIGHT - PADDING * 2);

        // Make the ImageView respond to size changes of its container
        imageView.fitWidthProperty().bind(imageContainer.widthProperty());
        imageView.fitHeightProperty().bind(imageContainer.heightProperty());
    }

    private void startImageRotation() {
        if (images.isEmpty()) return;

        imageIterator = images.iterator();
        if (imageIterator.hasNext()) {
            changeImage();
        }

        Duration duration = Duration.seconds(3);
        KeyFrame keyFrame = new KeyFrame(duration, e -> changeImage());
        Timeline timeline = new Timeline(keyFrame);
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private List<Image> loadImages() {
        Path resourcesPath = Paths.get("src/main/resources").toAbsolutePath();

        try (Stream<Path> paths = Files.walk(resourcesPath)) {
            return paths
                    .filter(Files::isRegularFile)
                    .filter(p -> {
                        String name = p.getFileName().toString().toLowerCase();
                        boolean isSupported = name.endsWith(".jpg") ||
                                              name.endsWith(".jpeg") ||
                                              name.endsWith(".png") ||
                                              name.endsWith(".gif");
                        if (!isSupported) {
                            System.out.println("Skipping unsupported file type: " + name);
                        }
                        return isSupported;
                    })
                    .map(this::loadImage)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

        } catch (IOException e) {
            System.err.println("Error scanning resources directory: " + e.getMessage());
            return List.of();
        }
    }

    private Image loadImage(Path path) {
        try {
            // Try loading synchronously first to validate
            try (var input = Files.newInputStream(path)) {
                Image img = new Image(input);
                if (!img.isError() && img.getWidth() > 0) {
                    // If successful, load with background loading enabled
                    String fileUri = path.toUri().toString();
                    Image finalImg = new Image(fileUri, true);
                    imageFilenames.put(finalImg, path.getFileName().toString());
                    return finalImg;
                }
                System.err.println("Initial load failed for " + path.getFileName() +
                                   " (width=" + img.getWidth() + ")");
            }
        } catch (Exception e) {
            System.err.println("Error loading " + path.getFileName() + ": " + e.getMessage());
        }
        return null;
    }

    private void changeImage() {
        if (!imageIterator.hasNext()) {
            imageIterator = images.iterator();
        }

        if (imageIterator.hasNext()) {
            Image newImage = imageIterator.next();
            if (newImage != null && !newImage.isError()) {
                imageView.setImage(newImage);
                System.out.println("Displaying: " + imageFilenames.get(newImage));
                adjustCurrentImageSize();
            }
        }
    }

    private void adjustCurrentImageSize() {
        Image currentImage = imageView.getImage();
        if (currentImage == null || currentImage.isError()) return;

        double imgWidth = currentImage.getWidth();
        double imgHeight = currentImage.getHeight();
        javafx.geometry.Rectangle2D screenBounds = javafx.stage.Screen.getPrimary().getVisualBounds();

        if (stage.isFullScreen()) {
            // In full screen, use the screen dimensions
            imageContainer.setPrefSize(screenBounds.getWidth(), screenBounds.getHeight());
            toggleFullScreenButton.setVisible(false);
        } else {
            // Reset container size
            imageContainer.setPrefSize(-1, -1);
            toggleFullScreenButton.setVisible(true);

            // Calculate maximum allowed dimensions (90% of screen)
            double maxWidth = screenBounds.getWidth() * 0.9;
            double maxHeight = screenBounds.getHeight() * 0.9;

            // Calculate scale to fit within screen bounds
            double scale = Math.min(
                    maxWidth / imgWidth,
                    maxHeight / imgHeight
            );

            // Apply scaling
            double finalWidth = scale < 1 ? imgWidth * scale : imgWidth;
            double finalHeight = scale < 1 ? imgHeight * scale : imgHeight;

            // Account for padding and button
            Insets padding = root.getPadding();
            double buttonHeight = toggleFullScreenButton.getHeight();
            double paddingWidth = padding.getLeft() + padding.getRight();
            double paddingHeight = padding.getTop() + padding.getBottom() + buttonHeight + 10;

            // Set stage size with some minimal bounds
            stage.setWidth(Math.max(DEFAULT_WIDTH, finalWidth + paddingWidth));
            stage.setHeight(Math.max(DEFAULT_HEIGHT, finalHeight + paddingHeight));

            // Center on screen
            stage.centerOnScreen();
        }
    }
}
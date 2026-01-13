package com.college.fx.components;

import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.concurrent.atomic.AtomicReference;

public class ImageCropperDialog {

    private final Image sourceImage;
    private Image croppedImage;
    private final Stage stage;

    private static final double VIEWPORT_SIZE = 400;
    private static final double CROP_SIZE = 250;

    public ImageCropperDialog(Image image, Stage owner) {
        this.sourceImage = image;
        this.stage = new Stage();
        this.stage.initOwner(owner);
        this.stage.initModality(Modality.WINDOW_MODAL);
        this.stage.setTitle("Edit Profile Picture");
        this.stage.setResizable(false);

        createView();
    }

    private void createView() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #0f172a;");

        // --- Header ---
        Label titleLabel = new Label("Align & Resize");
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold; -fx-padding: 15;");
        BorderPane.setAlignment(titleLabel, Pos.CENTER);
        root.setTop(titleLabel);

        // --- Image Area ---
        ImageView imageView = new ImageView(sourceImage);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);

        // Initial fit - make it cover the viewport comfortably
        double scale = Math.max(VIEWPORT_SIZE / sourceImage.getWidth(), VIEWPORT_SIZE / sourceImage.getHeight());
        imageView.setFitWidth(sourceImage.getWidth() * scale);
        imageView.setFitHeight(sourceImage.getHeight() * scale);

        // Container for the image (allows panning)
        StackPane imageLayer = new StackPane(imageView);
        imageLayer.setMinSize(VIEWPORT_SIZE, VIEWPORT_SIZE);
        imageLayer.setMaxSize(VIEWPORT_SIZE, VIEWPORT_SIZE);
        imageLayer.setAlignment(Pos.CENTER);

        // Clip to viewport to hide overflow
        Rectangle clip = new Rectangle(VIEWPORT_SIZE, VIEWPORT_SIZE);
        imageLayer.setClip(clip);

        // --- Drag to Pan Logic ---
        final AtomicReference<Point2D> dragAnchor = new AtomicReference<>();

        imageView.setOnMousePressed(e -> dragAnchor.set(new Point2D(e.getSceneX(), e.getSceneY())));
        imageView.setOnMouseDragged(e -> {
            if (dragAnchor.get() != null) {
                double deltaX = e.getSceneX() - dragAnchor.get().getX();
                double deltaY = e.getSceneY() - dragAnchor.get().getY();

                imageView.setTranslateX(imageView.getTranslateX() + deltaX);
                imageView.setTranslateY(imageView.getTranslateY() + deltaY);

                dragAnchor.set(new Point2D(e.getSceneX(), e.getSceneY()));
            }
        });
        imageView.setOnMouseReleased(e -> dragAnchor.set(null));

        // --- Mask Overlay (Dark with Circular Hole) ---
        Rectangle darkOverlay = new Rectangle(VIEWPORT_SIZE, VIEWPORT_SIZE);
        darkOverlay.setFill(Color.rgb(0, 0, 0, 0.7)); // Semi-transparent black

        Circle hole = new Circle(VIEWPORT_SIZE / 2, VIEWPORT_SIZE / 2, CROP_SIZE / 2);
        Shape mask = Shape.subtract(darkOverlay, hole);
        mask.setFill(Color.rgb(15, 23, 42, 0.7)); // Dark blue-ish tint matching theme
        mask.setMouseTransparent(true); // Allow clicks to pass through to image

        // Visual guide border for the circle
        Circle guideRing = new Circle(VIEWPORT_SIZE / 2, VIEWPORT_SIZE / 2, CROP_SIZE / 2);
        guideRing.setFill(Color.TRANSPARENT);
        guideRing.setStroke(Color.WHITE);
        guideRing.setStrokeWidth(2);
        guideRing.setMouseTransparent(true);

        StackPane viewportStack = new StackPane(imageLayer, mask, guideRing);
        viewportStack.setMaxSize(VIEWPORT_SIZE, VIEWPORT_SIZE);
        viewportStack.setStyle("-fx-border-color: #334155; -fx-border-width: 1px;");

        root.setCenter(viewportStack);

        // --- Controls ---
        Slider zoomSlider = new Slider(0.5, 3.0, 1.0);
        zoomSlider.setPrefWidth(200);

        // Link slider to scale
        zoomSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            double zoom = newVal.doubleValue();
            imageView.setScaleX(zoom);
            imageView.setScaleY(zoom);
        });

        // Scroll to zoom on image
        imageLayer.addEventFilter(ScrollEvent.SCROLL, e -> {
            double delta = e.getDeltaY();
            double currentZoom = zoomSlider.getValue();
            double newZoom = currentZoom + (delta > 0 ? 0.1 : -0.1);
            zoomSlider.setValue(Math.max(0.5, Math.min(3.0, newZoom)));
            e.consume();
        });

        Label zoomLabel = new Label("Zoom");
        zoomLabel.setStyle("-fx-text-fill: #94a3b8;");

        Button saveBtn = new Button("Save");
        saveBtn.setStyle(
                "-fx-background-color: #2563eb; -fx-text-fill: white; -fx-padding: 8 20; -fx-background-radius: 6; -fx-cursor: hand; -fx-font-weight: bold;");
        saveBtn.setOnAction(e -> {
            performCrop(imageLayer, imageView, zoomSlider.getValue());
            stage.close();
        });

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #cbd5e1; -fx-cursor: hand;");
        cancelBtn.setOnAction(e -> stage.close());

        HBox buttonBox = new HBox(10, cancelBtn, saveBtn);
        buttonBox.setAlignment(Pos.CENTER);

        VBox controls = new VBox(15,
                new HBox(10, zoomLabel, zoomSlider),
                buttonBox);
        controls.setAlignment(Pos.CENTER);
        controls.setPadding(new javafx.geometry.Insets(20));
        controls.setStyle(
                "-fx-background-color: #1e293b; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 10, 0, 0, -5);");

        root.setBottom(controls);

        Scene scene = new Scene(root, 450, 550);
        stage.setScene(scene);
    }

    private void performCrop(StackPane imageLayer, ImageView imageView, double zoom) {
        // To crop exactly what is visible in the circle, we can take a snapshot of the
        // imageLayer
        // but masked to the central crop region.

        // 1. Calculate the bounds of the crop area relative to the stackpane
        double cropX = (VIEWPORT_SIZE - CROP_SIZE) / 2;
        double cropY = (VIEWPORT_SIZE - CROP_SIZE) / 2;

        // 2. Snapshot parameters
        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);
        params.setViewport(new Rectangle2D(cropX, cropY, CROP_SIZE, CROP_SIZE));

        // 3. Take snapshot of the ImageLayer (which contains the transformed ImageView)
        // We act on imageLayer because it holds the translation/pan state,
        // while imageView holds the scale/zoom state.
        this.croppedImage = imageLayer.snapshot(params, null);
    }

    public Image showAndWait() {
        stage.showAndWait();
        return croppedImage;
    }
}

// Artwork3DViewer.java
package controller;

import javafx.animation.Animation;
import javafx.animation.RotateTransition;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SubScene;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Shape3D;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

public class Artwork3DViewer extends BorderPane {

    private final Group root3D = new Group();
    private final SubScene subScene;
    private final PerspectiveCamera camera;
    private final Rotate rotateX = new Rotate(0, Rotate.X_AXIS);
    private final Rotate rotateY = new Rotate(0, Rotate.Y_AXIS);
    private final Rotate rotateZ = new Rotate(0, Rotate.Z_AXIS);

    private double anchorX, anchorY;
    private double anchorAngleX = 0;
    private double anchorAngleY = 0;
    private double anchorAngleZ = 0;

    private Shape3D artwork;
    private RotateTransition rotateTransition;
    private boolean autoRotating = false;

    // Lighting control
    private final Group lightGroup = new Group();

    public Artwork3DViewer() {
        // Create 3D scene
        camera = new PerspectiveCamera(true);
        camera.setFarClip(10000.0);
        camera.setNearClip(0.1);
        camera.setTranslateZ(-1000);

        subScene = new SubScene(root3D, 800, 600, true, javafx.scene.SceneAntialiasing.BALANCED);
        subScene.setFill(Color.LIGHTGRAY);
        subScene.setCamera(camera);

        root3D.getChildren().add(lightGroup);

        // Setup UI
        setCenter(subScene);
        setupControls();

        // Handle mouse events for manual rotation
        setupMouseControl();
    }

    private void setupControls() {
        Button resetBtn = new Button("Reset View");
        resetBtn.setOnAction(e -> resetView());

        Button autoRotateBtn = new Button("Auto Rotate");
        autoRotateBtn.setOnAction(e -> toggleAutoRotate());

        // Zoom slider
        Slider zoomSlider = new Slider(500, 2000, 1000);
        zoomSlider.setShowTickLabels(true);
        zoomSlider.setShowTickMarks(true);
        zoomSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            camera.setTranslateZ(-newVal.doubleValue());
        });

        HBox controls = new HBox(10, resetBtn, autoRotateBtn, zoomSlider);
        controls.setStyle("-fx-padding: 10px; -fx-background-color: #f0f0f0;");

        setBottom(controls);
    }

    private void setupMouseControl() {
        subScene.setOnMousePressed(event -> {
            anchorX = event.getSceneX();
            anchorY = event.getSceneY();
            anchorAngleX = rotateX.getAngle();
            anchorAngleY = rotateY.getAngle();
        });

        subScene.setOnMouseDragged(event -> {
            // Stop auto-rotation if user starts dragging
            if (autoRotating) {
                rotateTransition.pause();
            }

            rotateX.setAngle(anchorAngleX - (anchorY - event.getSceneY()));
            rotateY.setAngle(anchorAngleY + anchorX - event.getSceneX());
        });

        subScene.setOnMouseReleased(event -> {
            // Resume auto-rotation if it was active
            if (autoRotating) {
                rotateTransition.play();
            }
        });

        // Mouse wheel for zoom
        subScene.setOnScroll(event -> {
            double zoomFactor = 1.05;
            double deltaY = event.getDeltaY();

            if (deltaY < 0) {
                zoomFactor = 0.95;
            }

            double newZ = camera.getTranslateZ() * zoomFactor;
            // Limit zoom range
            if (newZ > -2000 && newZ < -100) {
                camera.setTranslateZ(newZ);
            }
        });
    }

    public void displayArtwork(String type, double width, double height, double depth, String imagePath) {
        // Clear previous artwork
        if (artwork != null) {
            root3D.getChildren().remove(artwork);
        }

        // Get dimensions from the "dimensions" attribute
        String dimensionsStr = "30x45x30"; // Default if no dimensions available
        try {
            // Parse from your dimensions string format if available
            // Example: "30x45x30" -> width=30, height=45, depth=30
            String[] dims = dimensionsStr.split("x");
            if (dims.length >= 3) {
                width = Double.parseDouble(dims[0]);
                height = Double.parseDouble(dims[1]);
                depth = Double.parseDouble(dims[2]);
            }
        } catch (Exception e) {
            System.err.println("Error parsing dimensions: " + e.getMessage());
        }

        // Create texture material from image
        PhongMaterial material = new PhongMaterial();
        try {
            Image image = new Image(imagePath);
            material.setDiffuseMap(image);
            material.setSpecularColor(Color.WHITE);
            material.setSpecularPower(32);
        } catch (Exception e) {
            System.err.println("Error loading image: " + e.getMessage());
            material.setDiffuseColor(Color.CORAL);
        }

        // Create 3D shape based on type
        switch (type.toLowerCase()) {
            case "vase":
                artwork = createVase(width, height, depth, material);
                break;
            case "assiette décorative":
                artwork = createPlate(width, height, depth, material);
                break;
            case "sculpture":
                artwork = createSculpture(width, height, depth, material);
                break;
            case "poterie":
                artwork = createPottery(width, height, depth, material);
                break;
            case "bibelot":
                artwork = createFigurine(width, height, depth, material);
                break;
            default:
                // Default to a box if type is unknown
                artwork = createDefaultShape(width, height, depth, material);
        }

        // Apply transforms for rotation
        artwork.getTransforms().addAll(rotateX, rotateY, rotateZ);

        // Add to scene
        root3D.getChildren().add(artwork);

        // Reset view
        resetView();
    }

    private Shape3D createVase(double width, double height, double depth, PhongMaterial material) {
        // Create a cylinder for vase
        Cylinder vase = new Cylinder(width/2, height);
        vase.setMaterial(material);
        return vase;
    }

    private Shape3D createPlate(double width, double height, double depth, PhongMaterial material) {
        // Create a flattened cylinder for plate
        Cylinder plate = new Cylinder(width/2, height/10);
        plate.setMaterial(material);
        return plate;
    }

    private Shape3D createSculpture(double width, double height, double depth, PhongMaterial material) {
        // For sculpture, let's use a sphere as example (could be more complex)
        Sphere sculpture = new Sphere(Math.min(width, height)/2);
        sculpture.setMaterial(material);
        return sculpture;
    }

    private Shape3D createPottery(double width, double height, double depth, PhongMaterial material) {
        // For pottery, cylinder with different dimensions
        Cylinder pottery = new Cylinder(width/2, height);
        pottery.setMaterial(material);
        return pottery;
    }

    private Shape3D createFigurine(double width, double height, double depth, PhongMaterial material) {
        // For bibelot/figurine, a scaled box
        Box figurine = new Box(width, height, depth);
        figurine.setMaterial(material);
        return figurine;
    }

    private Shape3D createDefaultShape(double width, double height, double depth, PhongMaterial material) {
        // Default shape is a box
        Box box = new Box(width, height, depth);
        box.setMaterial(material);
        return box;
    }

    private void resetView() {
        // Reset rotation angles
        rotateX.setAngle(0);
        rotateY.setAngle(0);
        rotateZ.setAngle(0);

        // Reset camera position
        camera.setTranslateZ(-1000);

        // Stop auto-rotation if active
        if (rotateTransition != null) {
            rotateTransition.stop();
        }
        autoRotating = false;
    }

    private void toggleAutoRotate() {
        if (autoRotating) {
            rotateTransition.stop();
            autoRotating = false;
        } else {
            if (rotateTransition == null) {
                rotateTransition = new RotateTransition(Duration.seconds(10), artwork);
                rotateTransition.setAxis(Rotate.Y_AXIS);
                rotateTransition.setByAngle(360);
                rotateTransition.setCycleCount(Animation.INDEFINITE);
                rotateTransition.setAutoReverse(false);
            }
            rotateTransition.play();
            autoRotating = true;
        }
    }
}
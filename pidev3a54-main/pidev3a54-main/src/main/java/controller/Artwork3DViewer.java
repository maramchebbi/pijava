package controller;

import javafx.scene.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.scene.transform.*;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.image.Image;
import javafx.geometry.Insets;

public class Artwork3DViewer extends BorderPane {
    private PerspectiveCamera camera = new PerspectiveCamera(true);
    private Group root3D = new Group();
    private SubScene subScene;
    private double anchorX, anchorY;
    private double anchorAngleX = 0;
    private double anchorAngleY = 0;
    private Rotate rotateX = new Rotate(0, Rotate.X_AXIS);
    private Rotate rotateY = new Rotate(0, Rotate.Y_AXIS);

    public Artwork3DViewer() {
        initialize3DScene(); // on initialise ici
        setupCamera();
        setupLighting();
        addRotationControls();
        addUIComponents();
    }

    private void initialize3DScene() {
        subScene = new SubScene(root3D, 800, 600, true, SceneAntialiasing.BALANCED);
        subScene.setFill(Color.LIGHTGRAY);
        this.setCenter(subScene);
    }

    private void setupCamera() {
        camera.setNearClip(0.1);
        camera.setFarClip(10000.0);
        camera.setTranslateZ(-1000);
        subScene.setCamera(camera);
    }

    private void setupLighting() {
        PointLight pointLight = new PointLight(Color.WHITE);
        pointLight.setTranslateZ(-800);

        AmbientLight ambientLight = new AmbientLight(Color.rgb(80, 80, 80));

        root3D.getChildren().addAll(pointLight, ambientLight);
        root3D.getTransforms().addAll(rotateX, rotateY);
    }

    private void addRotationControls() {
        subScene.setOnMousePressed(event -> {
            anchorX = event.getSceneX();
            anchorY = event.getSceneY();
            anchorAngleX = rotateX.getAngle();
            anchorAngleY = rotateY.getAngle();
        });

        subScene.setOnMouseDragged(event -> {
            rotateX.setAngle(anchorAngleX - (anchorY - event.getSceneY()));
            rotateY.setAngle(anchorAngleY + anchorX - event.getSceneX());
        });

        subScene.setOnScroll(event -> {
            double delta = event.getDeltaY();
            camera.setTranslateZ(camera.getTranslateZ() + delta);
        });
    }

    private void addUIComponents() {
        Slider zoomSlider = new Slider(-2000, -500, -1000);
        zoomSlider.valueProperty().addListener((obs, oldVal, newVal) ->
                camera.setTranslateZ(newVal.doubleValue())
        );

        Button resetView = new Button("Reset View");
        resetView.setOnAction(e -> resetView());

        HBox controls = new HBox(10, new Label("Zoom:"), zoomSlider, resetView);
        controls.setPadding(new Insets(10));
        this.setBottom(controls);
    }

    public void displayArtwork(String type, double width, double height, double depth, String texturePath) {
        root3D.getChildren().removeIf(node -> node instanceof Shape3D);

        Shape3D artwork = createArtworkModel(type, width, height, depth);
        applyMaterial(artwork, texturePath);
        root3D.getChildren().add(artwork);
    }

    private Shape3D createArtworkModel(String type, double width, double height, double depth) {
        switch (type.toLowerCase()) {
            case "box":
                return new Box(width, height, depth);
            case "sphere":
                return new Sphere(width / 2); // radius
            case "cylinder":
                return new Cylinder(width / 2, height); // radius, height
            default:
                System.err.println("Unknown artwork type: " + type);
                return new Box(width, height, depth); // default
        }
    }

    private void applyMaterial(Shape3D shape, String texturePath) {
        PhongMaterial material = new PhongMaterial();
        material.setDiffuseColor(Color.BEIGE);

        if (texturePath != null && !texturePath.isEmpty()) {
            try {
                material.setDiffuseMap(new Image(getClass().getResourceAsStream(texturePath)));
            } catch (Exception e) {
                System.err.println("Could not load texture: " + e.getMessage());
            }
        }

        shape.setMaterial(material);
    }

    private void resetView() {
        rotateX.setAngle(0);
        rotateY.setAngle(0);
        camera.setTranslateZ(-1000);
    }
}
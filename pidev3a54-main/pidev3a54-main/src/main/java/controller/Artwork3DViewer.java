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
import java.io.File;

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
        initialize3DScene();
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
        pointLight.setTranslateY(-300); // Meilleur éclairage

        AmbientLight ambientLight = new AmbientLight(Color.rgb(100, 100, 100));

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

    public void displayArtwork(String type, double width, double height, double depth, String imagePath) {
        root3D.getChildren().removeIf(node -> node instanceof Shape3D);

        Shape3D artwork = createArtworkModel(type, width, height, depth);
        applyTexture(artwork, imagePath);
        root3D.getChildren().add(artwork);
    }

    private Shape3D createArtworkModel(String type, double width, double height, double depth) {
        switch (type.toLowerCase()) {
            case "vase":
                Cylinder vase = new Cylinder(width/2, height);
                vase.setTranslateY(-height/2); // Centrer verticalement
                return vase;
            case "bowl":
                Sphere bowl = new Sphere(width/2);
                bowl.setScaleY(0.5); // Écraser pour forme de bol
                bowl.setTranslateY(-height/2);
                return bowl;
            case "plate":
                Cylinder plate = new Cylinder(width/2, depth);
                plate.setTranslateY(-depth/2);
                return plate;
            default:
                Box box = new Box(width, height, depth);
                box.setTranslateY(-height/2);
                return box;
        }
    }

    private void applyTexture(Shape3D shape, String imagePath) {
        PhongMaterial material = new PhongMaterial();
        material.setSpecularColor(Color.WHITE);
        material.setSpecularPower(64);

        try {
            // Solution pour les chemins absolus et relatifs
            Image texture;
            if (new File(imagePath).exists()) {
                texture = new Image("file:" + imagePath);
            } else {
                texture = new Image(getClass().getResourceAsStream(imagePath));
            }

            material.setDiffuseMap(texture);
            System.out.println("Texture loaded successfully from: " + imagePath);
        } catch (Exception e) {
            System.err.println("Error loading texture: " + e.getMessage());
            material.setDiffuseColor(Color.TAN); // Couleur de fallback
        }

        shape.setMaterial(material);
    }

    private void resetView() {
        rotateX.setAngle(0);
        rotateY.setAngle(0);
        camera.setTranslateZ(-1000);
    }
}
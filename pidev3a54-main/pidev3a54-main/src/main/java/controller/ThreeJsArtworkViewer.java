// ThreeJsArtworkViewer.java
package controller;

import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import java.io.File;

public class ThreeJsArtworkViewer extends BorderPane {

    private final WebView webView;
    private final WebEngine webEngine;

    public ThreeJsArtworkViewer() {
        webView = new WebView();
        webEngine = webView.getEngine();
        setCenter(webView);
    }

    public void displayArtwork(String type, String imagePath, String dimensions) {
        // Créer un HTML temporaire avec Three.js et l'image
        String htmlContent = generateThreeJsHtml(type, imagePath, dimensions);
        webEngine.loadContent(htmlContent);
    }

    private String generateThreeJsHtml(String type, String imagePath, String dimensions) {
        // Extraire les dimensions
        double width = 200;
        double height = 300;
        double depth = 200;

        try {
            String[] dims = dimensions.split("x");
            if (dims.length >= 2) {
                width = Double.parseDouble(dims[0]);
                height = Double.parseDouble(dims[1]);
                depth = dims.length > 2 ? Double.parseDouble(dims[2]) : width;
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du parsing des dimensions: " + e.getMessage());
        }

        // Convertir le chemin d'image en URI utilisable par le navigateur
        String imageUri = new File(imagePath).toURI().toString();

        // Générer le HTML avec Three.js
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <meta charset='utf-8'>\n" +
                "    <title>Three.js Artwork Viewer</title>\n" +
                "    <style>\n" +
                "        body { margin: 0; overflow: hidden; }\n" +
                "        canvas { display: block; }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <script src='https://cdnjs.cloudflare.com/ajax/libs/three.js/r128/three.min.js'></script>\n" +
                "    <script>\n" +
                "        // Scene setup\n" +
                "        const scene = new THREE.Scene();\n" +
                "        scene.background = new THREE.Color(0xf0f0f0);\n" +
                "        \n" +
                "        const camera = new THREE.PerspectiveCamera(75, window.innerWidth / window.innerHeight, 0.1, 1000);\n" +
                "        camera.position.z = " + (Math.max(width, Math.max(height, depth)) * 2) + ";\n" +
                "        \n" +
                "        const renderer = new THREE.WebGLRenderer({ antialias: true });\n" +
                "        renderer.setSize(window.innerWidth, window.innerHeight);\n" +
                "        document.body.appendChild(renderer.domElement);\n" +
                "        \n" +
                "        // Lighting\n" +
                "        const ambientLight = new THREE.AmbientLight(0xffffff, 0.5);\n" +
                "        scene.add(ambientLight);\n" +
                "        \n" +
                "        const directionalLight = new THREE.DirectionalLight(0xffffff, 0.8);\n" +
                "        directionalLight.position.set(1, 1, 1);\n" +
                "        scene.add(directionalLight);\n" +
                "        \n" +
                "        // Load texture\n" +
                "        const textureLoader = new THREE.TextureLoader();\n" +
                "        const texture = textureLoader.load('" + imageUri + "');\n" +
                "        \n" +
                "        // Material\n" +
                "        const material = new THREE.MeshPhongMaterial({ \n" +
                "            map: texture,\n" +
                "            shininess: 50\n" +
                "        });\n" +
                "        \n" +
                "        // Create appropriate geometry based on type\n" +
                "        let geometry;\n" +
                "        let mesh;\n" +
                "        \n" +
                "        switch('" + type.toLowerCase() + "') {\n" +
                "            case 'vase':\n" +
                "                geometry = new THREE.CylinderGeometry(" + width/2 + ", " + width/2 + ", " + height + ", 32);\n" +
                "                break;\n" +
                "            case 'assiette décorative':\n" +
                "                geometry = new THREE.CylinderGeometry(" + width/2 + ", " + width/2 + ", " + height/10 + ", 32);\n" +
                "                break;\n" +
                "            case 'sculpture':\n" +
                "                geometry = new THREE.SphereGeometry(" + Math.min(width, height)/2 + ", 32, 32);\n" +
                "                break;\n" +
                "            case 'poterie':\n" +
                "                geometry = new THREE.CylinderGeometry(" + width/2 + ", " + width/3 + ", " + height + ", 32);\n" +
                "                break;\n" +
                "            default:\n" +
                "                geometry = new THREE.BoxGeometry(" + width + ", " + height + ", " + depth + ");\n" +
                "        }\n" +
                "        \n" +
                "        mesh = new THREE.Mesh(geometry, material);\n" +
                "        scene.add(mesh);\n" +
                "        \n" +
                "        // Add orbit controls\n" +
                "        let rotationSpeed = 0.005;\n" +
                "        let isDragging = false;\n" +
                "        let previousMousePosition = { x: 0, y: 0 };\n" +
                "        \n" +
                "        document.addEventListener('mousedown', function(e) {\n" +
                "            isDragging = true;\n" +
                "            previousMousePosition = { x: e.clientX, y: e.clientY };\n" +
                "        });\n" +
                "        \n" +
                "        document.addEventListener('mousemove', function(e) {\n" +
                "            if (isDragging) {\n" +
                "                const deltaMove = {\n" +
                "                    x: e.clientX - previousMousePosition.x,\n" +
                "                    y: e.clientY - previousMousePosition.y\n" +
                "                };\n" +
                "                \n" +
                "                mesh.rotation.y += deltaMove.x * 0.01;\n" +
                "                mesh.rotation.x += deltaMove.y * 0.01;\n" +
                "                \n" +
                "                previousMousePosition = { x: e.clientX, y: e.clientY };\n" +
                "            }\n" +
                "        });\n" +
                "        \n" +
                "        document.addEventListener('mouseup', function() {\n" +
                "            isDragging = false;\n" +
                "        });\n" +
                "        \n" +
                "        // Handle window resize\n" +
                "        window.addEventListener('resize', function() {\n" +
                "            camera.aspect = window.innerWidth / window.innerHeight;\n" +
                "            camera.updateProjectionMatrix();\n" +
                "            renderer.setSize(window.innerWidth, window.innerHeight);\n" +
                "        });\n" +
                "        \n" +
                "        // Mouse wheel zoom\n" +
                "        document.addEventListener('wheel', function(e) {\n" +
                "            const zoomSpeed = 0.1;\n" +
                "            if (e.deltaY < 0) {\n" +
                "                camera.position.z -= zoomSpeed * 10;\n" +
                "            } else {\n" +
                "                camera.position.z += zoomSpeed * 10;\n" +
                "            }\n" +
                "            // Keep zoom within reasonable limits\n" +
                "            camera.position.z = Math.max(5, Math.min(camera.position.z, 100));\n" +
                "        });\n" +
                "        \n" +
                "        // Animation loop\n" +
                "        function animate() {\n" +
                "            requestAnimationFrame(animate);\n" +
                "            \n" +
                "            // Auto-rotate if not being dragged\n" +
                "            if (!isDragging) {\n" +
                "                mesh.rotation.y += rotationSpeed;\n" +
                "            }\n" +
                "            \n" +
                "            renderer.render(scene, camera);\n" +
                "        }\n" +
                "        \n" +
                "        animate();\n" +
                "    </script>\n" +
                "</body>\n" +
                "</html>";
    }
}
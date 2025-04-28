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

    public void displayArtwork(String type, double width, double height, double depth, String imagePath) {
        // Create temporary HTML with Three.js and the image
        String htmlContent = generateThreeJsHtml(type, width, height, depth, imagePath);
        webEngine.loadContent(htmlContent);
    }

    private String generateThreeJsHtml(String type, double width, double height, double depth, String imagePath) {
        // Normalize dimensions for better display
        double maxDimension = Math.max(Math.max(width, height), depth);
        double normalizedWidth = width / maxDimension;
        double normalizedHeight = height / maxDimension;
        double normalizedDepth = depth / maxDimension;

        // Scale factor to make object a reasonable size in the scene
        double scaleFactor = 5.0;
        double displayWidth = normalizedWidth * scaleFactor;
        double displayHeight = normalizedHeight * scaleFactor;
        double displayDepth = normalizedDepth * scaleFactor;

        // Calculate camera distance based on dimensions
        double cameraDistance = maxDimension * 1.5;

        // Generate the HTML with Three.js
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <meta charset='utf-8'>\n" +
                "    <title>Three.js Artwork Viewer</title>\n" +
                "    <style>\n" +
                "        body { margin: 0; overflow: hidden; background-color: #f5f5f5; }\n" +
                "        canvas { display: block; }\n" +
                "        .info-panel {\n" +
                "            position: absolute;\n" +
                "            bottom: 10px;\n" +
                "            left: 10px;\n" +
                "            background-color: rgba(255, 255, 255, 0.7);\n" +
                "            padding: 10px;\n" +
                "            border-radius: 5px;\n" +
                "            font-family: Arial, sans-serif;\n" +
                "        }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class='info-panel'>\n" +
                "        <strong>Controls:</strong> Drag to rotate | Scroll to zoom | Double-click to toggle auto-rotation\n" +
                "    </div>\n" +
                "    <script src='https://cdnjs.cloudflare.com/ajax/libs/three.js/r128/three.min.js'></script>\n" +
                "    <script src='https://cdnjs.cloudflare.com/ajax/libs/three.js/r128/examples/js/controls/OrbitControls.js'></script>\n" +
                "    <script>\n" +
                "        // Scene setup\n" +
                "        const scene = new THREE.Scene();\n" +
                "        scene.background = new THREE.Color(0xf5f5f5);\n" +
                "        \n" +
                "        // Add a subtle environment\n" +
                "        const gridHelper = new THREE.GridHelper(20, 20, 0xc9c9c9, 0xd4d4d4);\n" +
                "        gridHelper.position.y = -" + displayHeight/2 + " - 0.01;\n" +
                "        scene.add(gridHelper);\n" +
                "        \n" +
                "        // Camera setup\n" +
                "        const camera = new THREE.PerspectiveCamera(45, window.innerWidth / window.innerHeight, 0.1, 1000);\n" +
                "        camera.position.set(" + displayWidth + ", " + displayHeight/2 + ", " + cameraDistance + ");\n" +
                "        \n" +
                "        const renderer = new THREE.WebGLRenderer({ antialias: true });\n" +
                "        renderer.setSize(window.innerWidth, window.innerHeight);\n" +
                "        renderer.shadowMap.enabled = true;\n" +
                "        renderer.shadowMap.type = THREE.PCFSoftShadowMap;\n" +
                "        document.body.appendChild(renderer.domElement);\n" +
                "        \n" +
                "        // Lighting\n" +
                "        const ambientLight = new THREE.AmbientLight(0xffffff, 0.5);\n" +
                "        scene.add(ambientLight);\n" +
                "        \n" +
                "        const keyLight = new THREE.DirectionalLight(0xffffff, 0.8);\n" +
                "        keyLight.position.set(1, 1, 2);\n" +
                "        keyLight.castShadow = true;\n" +
                "        keyLight.shadow.camera.near = 0.1;\n" +
                "        keyLight.shadow.camera.far = 100;\n" +
                "        keyLight.shadow.camera.left = -20;\n" +
                "        keyLight.shadow.camera.right = 20;\n" +
                "        keyLight.shadow.camera.top = 20;\n" +
                "        keyLight.shadow.camera.bottom = -20;\n" +
                "        keyLight.shadow.mapSize.width = 2048;\n" +
                "        keyLight.shadow.mapSize.height = 2048;\n" +
                "        scene.add(keyLight);\n" +
                "        \n" +
                "        const fillLight = new THREE.DirectionalLight(0xffffff, 0.4);\n" +
                "        fillLight.position.set(-1, 0.5, -1);\n" +
                "        scene.add(fillLight);\n" +
                "        \n" +
                "        const rimLight = new THREE.DirectionalLight(0xffffff, 0.3);\n" +
                "        rimLight.position.set(0, -1, 0);\n" +
                "        scene.add(rimLight);\n" +
                "        \n" +
                "        // Load texture\n" +
                "        const textureLoader = new THREE.TextureLoader();\n" +
                "        const texture = textureLoader.load('" + imagePath + "');\n" +
                "        texture.anisotropy = renderer.capabilities.getMaxAnisotropy();\n" +
                "        \n" +
                "        // Create plane for shadow\n" +
                "        const shadowPlane = new THREE.Mesh(\n" +
                "            new THREE.PlaneGeometry(40, 40),\n" +
                "            new THREE.MeshStandardMaterial({ color: 0xf5f5f5, roughness: 0.8, metalness: 0.1 })\n" +
                "        );\n" +
                "        shadowPlane.rotation.x = -Math.PI / 2;\n" +
                "        shadowPlane.position.y = -" + displayHeight/2 + ";\n" +
                "        shadowPlane.receiveShadow = true;\n" +
                "        scene.add(shadowPlane);\n" +
                "        \n" +
                "        // Materials\n" +
                "        const standardMaterial = new THREE.MeshStandardMaterial({\n" +
                "            map: texture,\n" +
                "            roughness: 0.5,\n" +
                "            metalness: 0.1,\n" +
                "        });\n" +
                "        \n" +
                "        // Create appropriate geometry based on type\n" +
                "        let geometry;\n" +
                "        let mesh;\n" +
                "        const lowerCaseType = '" + type.toLowerCase() + "';\n" +
                "        \n" +
                "        if (lowerCaseType.includes('vase')) {\n" +
                "            // Create a more vase-like shape using lathe geometry\n" +
                "            const points = [];\n" +
                "            const segments = 20;\n" +
                "            const baseRadius = " + displayWidth/2 + ";\n" +
                "            const neckRadius = baseRadius * 0.7;\n" +
                "            const lipRadius = baseRadius * 0.9;\n" +
                "            const height = " + displayHeight + ";\n" +
                "            \n" +
                "            // Base points\n" +
                "            points.push(new THREE.Vector2(0, -height/2));\n" +
                "            points.push(new THREE.Vector2(baseRadius * 0.2, -height/2));\n" +
                "            points.push(new THREE.Vector2(baseRadius, -height/2 + height*0.1));\n" +
                "            points.push(new THREE.Vector2(baseRadius, -height/2 + height*0.2));\n" +
                "            \n" +
                "            // Middle section\n" +
                "            points.push(new THREE.Vector2(baseRadius * 0.8, 0));\n" +
                "            \n" +
                "            // Neck\n" +
                "            points.push(new THREE.Vector2(neckRadius, height/2 - height*0.2));\n" +
                "            points.push(new THREE.Vector2(neckRadius, height/2 - height*0.05));\n" +
                "            \n" +
                "            // Lip\n" +
                "            points.push(new THREE.Vector2(lipRadius, height/2));\n" +
                "            points.push(new THREE.Vector2(0, height/2));\n" +
                "            \n" +
                "            geometry = new THREE.LatheGeometry(points, 32);\n" +
                "            \n" +
                "            // For proper texture mapping on lathe\n" +
                "            standardMaterial.map.wrapS = THREE.RepeatWrapping;\n" +
                "            standardMaterial.map.repeat.set(4, 1);\n" +
                "        }\n" +
                "        else if (lowerCaseType.includes('assiette') || lowerCaseType.includes('plat')) {\n" +
                "            // Create a plate shape\n" +
                "            const plateRadius = " + displayWidth/2 + ";\n" +
                "            const depth = " + displayHeight/10 + ";\n" +
                "            geometry = new THREE.CylinderGeometry(plateRadius, plateRadius, depth, 32);\n" +
                "            \n" +
                "            // Custom UVs for better texture mapping on a plate\n" +
                "            standardMaterial.map.wrapS = THREE.RepeatWrapping;\n" +
                "            standardMaterial.map.wrapT = THREE.RepeatWrapping;\n" +
                "            standardMaterial.map.repeat.set(1, 1);\n" +
                "        }\n" +
                "        else if (lowerCaseType.includes('sculpture')) {\n" +
                "            // More complex shape for sculptures\n" +
                "            geometry = new THREE.SphereGeometry(" + Math.min(displayWidth, displayHeight)/2 + ", 32, 32);\n" +
                "            \n" +
                "            // Distort the sphere to make it more sculpture-like\n" +
                "            const positions = geometry.attributes.position;\n" +
                "            const vector = new THREE.Vector3();\n" +
                "            \n" +
                "            for (let i = 0; i < positions.count; i++) {\n" +
                "                vector.fromBufferAttribute(positions, i);\n" +
                "                const distortion = Math.sin(vector.y * 4) * 0.1;\n" +
                "                vector.x *= (1 + distortion);\n" +
                "                vector.z *= (1 + distortion);\n" +
                "                positions.setXYZ(i, vector.x, vector.y, vector.z);\n" +
                "            }\n" +
                "            \n" +
                "            geometry.computeVertexNormals();\n" +
                "        }\n" +
                "        else if (lowerCaseType.includes('poterie')) {\n" +
                "            // Create a pottery-like shape\n" +
                "            geometry = new THREE.CylinderGeometry(" + displayWidth/2 + ", " + displayWidth/3 + ", " + displayHeight + ", 32);\n" +
                "            standardMaterial.map.wrapS = THREE.RepeatWrapping;\n" +
                "            standardMaterial.map.repeat.set(2, 1);\n" +
                "        }\n" +
                "        else {\n" +
                "            // Default to box geometry for paintings or other objects\n" +
                "            geometry = new THREE.BoxGeometry(" + displayWidth + ", " + displayHeight + ", " + displayDepth + ");\n" +
                "        }\n" +
                "        \n" +
                "        mesh = new THREE.Mesh(geometry, standardMaterial);\n" +
                "        mesh.castShadow = true;\n" +
                "        mesh.receiveShadow = true;\n" +
                "        scene.add(mesh);\n" +
                "        \n" +
                "        // Add orbit controls\n" +
                "        const controls = new THREE.OrbitControls(camera, renderer.domElement);\n" +
                "        controls.enableDamping = true;\n" +
                "        controls.dampingFactor = 0.05;\n" +
                "        controls.minDistance = 3;\n" +
                "        controls.maxDistance = 20;\n" +
                "        controls.target.set(0, 0, 0);\n" +
                "        controls.update();\n" +
                "        \n" +
                "        // Auto-rotation\n" +
                "        let autoRotate = true;\n" +
                "        const rotationSpeed = 0.003;\n" +
                "        \n" +
                "        document.addEventListener('dblclick', function() {\n" +
                "            autoRotate = !autoRotate;\n" +
                "        });\n" +
                "        \n" +
                "        // Handle window resize\n" +
                "        window.addEventListener('resize', function() {\n" +
                "            camera.aspect = window.innerWidth / window.innerHeight;\n" +
                "            camera.updateProjectionMatrix();\n" +
                "            renderer.setSize(window.innerWidth, window.innerHeight);\n" +
                "        });\n" +
                "        \n" +
                "        // Animation loop\n" +
                "        function animate() {\n" +
                "            requestAnimationFrame(animate);\n" +
                "            \n" +
                "            // Apply auto-rotation if enabled\n" +
                "            if (autoRotate && !controls.enabled) {\n" +
                "                mesh.rotation.y += rotationSpeed;\n" +
                "            }\n" +
                "            \n" +
                "            controls.update();\n" +
                "            renderer.render(scene, camera);\n" +
                "        }\n" +
                "        \n" +
                "        // Start the animation loop\n" +
                "        animate();\n" +
                "    </script>\n" +
                "</body>\n" +
                "</html>";
    }
}
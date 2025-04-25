# cartoonize.py
import sys
import cv2
import numpy as np
import tensorflow as tf
from PIL import Image
import cv2
print(cv2.__version__)

from huggingface_hub import snapshot_download

def resize_crop(image):
    h, w, _ = image.shape
    if min(h, w) > 720:
        if h > w:
            h = int(720 * h / w)
            w = 720
        else:
            w = int(720 * w / h)
            h = 720
    image = cv2.resize(image, (w, h), interpolation=cv2.INTER_AREA)
    h, w = (h // 8) * 8, (w // 8) * 8
    return image[:h, :w, :]

def preprocess_image(image):
    image = resize_crop(image)
    image = image.astype(np.float32) / 127.5 - 1
    image = np.expand_dims(image, axis=0)
    return tf.constant(image)

def cartoonize(image_path):
    model_path = snapshot_download("sayakpaul/whitebox-cartoonizer", repo_type="model")
    loaded_model = tf.saved_model.load(model_path)
    concrete_func = loaded_model.signatures["serving_default"]

    image = cv2.imread(image_path)
    image = cv2.cvtColor(image, cv2.COLOR_BGR2RGB)
    input_tensor = preprocess_image(image)

    result = concrete_func(input_tensor)["final_output:0"]

    output = (result[0].numpy() + 1.0) * 127.5
    output = np.clip(output, 0, 255).astype(np.uint8)
    output = cv2.cvtColor(output, cv2.COLOR_RGB2BGR)
    cv2.imwrite("result.png", output)

if __name__ == "__main__":
    image_path = sys.argv[1]  # Récupère le chemin de l’image envoyé depuis Java
    cartoonize(image_path)

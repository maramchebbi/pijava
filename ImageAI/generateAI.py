from diffusers import StableDiffusionPipeline
import torch

pipe = StableDiffusionPipeline.from_pretrained(
    "CompVis/stable-diffusion-v1-4",
    torch_dtype=torch.float16,
    revision="fp16",
    use_auth_token="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"  # Mets ici ton token Hugging Face
).to("cuda")

prompt = "a fantasy landscape with mountains and rivers"
image = pipe(prompt).images[0]
image.save("image.png")

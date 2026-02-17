import os
from PIL import Image

SOURCE_IMAGE = "picture/major.jpg"
OUTPUT_DIR = "google_play"

def generate_assets():
    if not os.path.exists(SOURCE_IMAGE):
        print(f"Error: {SOURCE_IMAGE} not found.")
        return

    if not os.path.exists(OUTPUT_DIR):
        os.makedirs(OUTPUT_DIR)

    try:
        img = Image.open(SOURCE_IMAGE)
        print(f"Source image loaded: {SOURCE_IMAGE} ({img.size})")

        # 1. Generate Application Icon (512x512)
        icon_512 = img.resize((512, 512), Image.Resampling.LANCZOS)
        icon_path = os.path.join(OUTPUT_DIR, "play_store_icon_512.png")
        icon_512.save(icon_path, "PNG")
        print(f"Generated Application Icon: {icon_path}")

        # 2. Generate Feature Graphic (1024x500)
        # Crop center 1024x500 from 1024x1024
        width, height = img.size
        target_w, target_h = 1024, 500
        
        # Calculate crop coordinates
        left = (width - target_w) / 2
        top = (height - target_h) / 2
        right = (width + target_w) / 2
        bottom = (height + target_h) / 2
        
        feature_graphic = img.crop((left, top, right, bottom))
        # Ensure it's exactly 1024x500 if the source was slightly different
        if feature_graphic.size != (1024, 500):
             feature_graphic = feature_graphic.resize((1024, 500), Image.Resampling.LANCZOS)
             
        feature_path = os.path.join(OUTPUT_DIR, "feature_graphic_1024x500.png")
        feature_graphic.save(feature_path, "PNG")
        print(f"Generated Feature Graphic: {feature_path}")

        print("\nAll Play Store assets generated successfully in 'google_play' directory.")

    except Exception as e:
        print(f"Error generating assets: {e}")

if __name__ == "__main__":
    generate_assets()

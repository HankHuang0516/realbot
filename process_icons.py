import os
from PIL import Image

SOURCE_ICON = "picture/major.jpg"
RES_DIR = "app/src/main/res"

DENSITIES = {
    "mipmap-mdpi": 48,
    "mipmap-hdpi": 72,
    "mipmap-xhdpi": 96,
    "mipmap-xxhdpi": 144,
    "mipmap-xxxhdpi": 192
}

def generate_icons():
    source_path = SOURCE_ICON
    if not os.path.exists(source_path):
        print(f"Error: {source_path} not found.")
        # Try finding any png
        files = [f for f in os.listdir('.') if f.endswith('.png') and 'pixel' in f]
        if files:
            print(f"Using {files[0]} instead.")
            source_path = files[0]
        else:
            return

    try:
        img = Image.open(source_path)
        # Convert to RGBA
        img = img.convert("RGBA")

        for folder, size in DENSITIES.items():
            path = os.path.join(RES_DIR, folder)
            if not os.path.exists(path):
                os.makedirs(path)
            
            # Resize
            resized = img.resize((size, size), Image.Resampling.LANCZOS) # High-quality resampling for photos
            
            # Save ic_launcher.png
            resized.save(os.path.join(path, "ic_launcher.png"))
            
            # Save ic_launcher_round.png (Same for now, or crop circle if needed)
            # Simple circular crop simulation or just save same
            resized.save(os.path.join(path, "ic_launcher_round.png"))
            
            print(f"Generated {folder}: {size}x{size}")

        print("Done generating icons.")

    except Exception as e:
        print(f"Error processing icons: {e}")

if __name__ == "__main__":
    generate_icons()

import os
from PIL import Image, ImageOps, ImageDraw

# Configuration
SOURCE_IMAGE_PATH = r'c:\Hank\Other\project\realbot\picture\c4e11566-1469-40c2-afe1-2740437d596b.png'
RES_PATH = r'c:\Hank\Other\project\realbot\app\src\main\res'

# Densities and sizes
DENSITIES = {
    'mipmap-mdpi': (48, 48),
    'mipmap-hdpi': (72, 72),
    'mipmap-xhdpi': (96, 96),
    'mipmap-xxhdpi': (144, 144),
    'mipmap-xxxhdpi': (192, 192),
}

def create_icons():
    if not os.path.exists(SOURCE_IMAGE_PATH):
        print(f"Error: Source image not found at {SOURCE_IMAGE_PATH}")
        return

    try:
        # Open source image
        img = Image.open(SOURCE_IMAGE_PATH).convert("RGBA")
        print(f"Loaded source image: {SOURCE_IMAGE_PATH}")

        for folder, size in DENSITIES.items():
            folder_path = os.path.join(RES_PATH, folder)
            os.makedirs(folder_path, exist_ok=True)
            
            # Create square icon (ic_launcher.png) - Standard
            # Resize with LANCZOS for high quality
            square_icon = img.resize(size, Image.Resampling.LANCZOS)
            square_out = os.path.join(folder_path, 'ic_launcher.png')
            square_icon.save(square_out, 'PNG')
            print(f"Generated {square_out}")

            # Create round icon (ic_launcher_round.png) - Standard
            mask = Image.new('L', size, 0)
            draw = ImageDraw.Draw(mask)
            draw.ellipse((0, 0) + size, fill=255)
            
            round_icon = square_icon.copy()
            round_icon.putalpha(mask)
            
            round_out = os.path.join(folder_path, 'ic_launcher_round.png')
            round_icon.save(round_out, 'PNG')
            print(f"Generated {round_out}")

        # Adaptive Icons (v26+)
        # We use the source image as the background to verify full bleed.
        # Foreground will be transparent.
        ADAPTIVE_DENSITIES = {
            'mipmap-mdpi': (108, 108),
            'mipmap-hdpi': (162, 162),
            'mipmap-xhdpi': (216, 216),
            'mipmap-xxhdpi': (324, 324),
            'mipmap-xxxhdpi': (432, 432),
        }

        for folder, size in ADAPTIVE_DENSITIES.items():
            folder_path = os.path.join(RES_PATH, folder)
            os.makedirs(folder_path, exist_ok=True)

            # Background: 
            # The safe zone is a circle of diameter 66dp within 108dp.
            # Scale factor = 66 / 108 = 0.611
            # To be safe, let's scale the image to ~72% of the full size
            # and center it on a white background.
            scale_factor = 0.72
            target_size = (int(size[0] * scale_factor), int(size[1] * scale_factor))
            
            # Create white background
            bg_icon = Image.new("RGBA", size, (255, 255, 255, 255))
            
            # Resize source image
            resized_img = img.resize(target_size, Image.Resampling.LANCZOS)
            
            # Paste centered
            offset = ((size[0] - target_size[0]) // 2, (size[1] - target_size[1]) // 2)
            bg_icon.paste(resized_img, offset, mask=resized_img)
            
            bg_out = os.path.join(folder_path, 'ic_launcher_background.png')
            bg_icon.save(bg_out, 'PNG')
            print(f"Generated {bg_out}")

            # Foreground: Transparent
            fg_icon = Image.new("RGBA", size, (0, 0, 0, 0))
            fg_out = os.path.join(folder_path, 'ic_launcher_foreground.png')
            fg_icon.save(fg_out, 'PNG')
            print(f"Generated {fg_out}")

        # Create mipmap-anydpi-v26 XMLs
        anydpi_path = os.path.join(RES_PATH, 'mipmap-anydpi-v26')
        os.makedirs(anydpi_path, exist_ok=True)

        xml_content = """<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@mipmap/ic_launcher_background" />
    <foreground android:drawable="@mipmap/ic_launcher_foreground" />
</adaptive-icon>"""

        with open(os.path.join(anydpi_path, 'ic_launcher.xml'), 'w', encoding='utf-8') as f:
            f.write(xml_content)
        print(f"Generated {os.path.join(anydpi_path, 'ic_launcher.xml')}")

        with open(os.path.join(anydpi_path, 'ic_launcher_round.xml'), 'w', encoding='utf-8') as f:
            f.write(xml_content)
        print(f"Generated {os.path.join(anydpi_path, 'ic_launcher_round.xml')}")

        print("Icon update complete!")

    except Exception as e:
        print(f"An error occurred: {e}")

if __name__ == "__main__":
    create_icons()

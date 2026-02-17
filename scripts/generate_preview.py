from PIL import Image, ImageDraw, ImageOps
import os

RES_PATH = r'c:\Hank\Other\project\realbot\app\src\main\res\mipmap-xxxhdpi'
OUTPUT_PATH = r'c:\Hank\Other\project\realbot\icon_preview.png'

def create_preview():
    try:
        # Load background (which now has the logo + white padding)
        bg_path = os.path.join(RES_PATH, 'ic_launcher_background.png')
        if not os.path.exists(bg_path):
            print(f"Error: {bg_path} not found.")
            return

        bg_img = Image.open(bg_path).convert("RGBA")
        size = bg_img.size
        
        # Create circular mask (simulating Android adaptive icon mask)
        # Note: In real Android, the mask is about 72dp diameter within 108dp canvas,
        # but here we just want to show the circular cutout.
        # Let's use a mask that covers the "safe zone" (66/108 ratio)
        mask_size = (int(size[0] * (66/108)), int(size[1] * (66/108)))
        mask_size = (size[0], size[1]) # Simplify: use full circle for preview since we put whitespace
        
        mask = Image.new('L', size, 0)
        draw = ImageDraw.Draw(mask)
        # Draw circle on the whole image to show the cut
        # The adaptive icon is 108dp, the safe zone is circle of 66dp in center.
        # The system mask applies to the safe zone? No, the mask applies to the viewport (72dp).
        # But we already scaled our content.
        # Let's just draw a circle in the center.
        
        # Simulating the viewport: ~66% of the full 108dp image is visible in the circle
        viewport_diameter = int(size[0] * (72/108)) 
        offset = (size[0] - viewport_diameter) // 2
        
        viewport_mask = Image.new('L', size, 0)
        draw_vp = ImageDraw.Draw(viewport_mask)
        draw_vp.ellipse((offset, offset, offset + viewport_diameter, offset + viewport_diameter), fill=255)
        
        # Apply mask
        preview_img = bg_img.copy()
        preview_img.putalpha(viewport_mask)
        
        # Crop to viewport
        preview_img = preview_img.crop((offset, offset, offset + viewport_diameter, offset + viewport_diameter))

        # Create a "phone screen" background
        screen_bg = Image.new("RGB", (500, 800), (50, 50, 150)) # Blue-ish background
        draw_screen = ImageDraw.Draw(screen_bg)
        
        # Paste icon in center
        icon_pos = ((500 - preview_img.width) // 2, (800 - preview_img.height) // 2)
        screen_bg.paste(preview_img, icon_pos, preview_img)
        
        # Add text
        # (Optional, skip font loading complexity)
        
        screen_bg.save(OUTPUT_PATH)
        print(f"Preview saved to {OUTPUT_PATH}")

    except Exception as e:
        print(f"Error: {e}")

if __name__ == "__main__":
    create_preview()

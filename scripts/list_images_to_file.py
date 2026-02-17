import os
import time

PICTURE_DIR = r'c:\Hank\Other\project\realbot\picture'
LOG_FILE = r'c:\Hank\Other\project\realbot\logs\image_list.txt'

def list_images():
    if not os.path.exists(PICTURE_DIR):
        print("Picture directory not found.")
        return

    files = []
    for f in os.listdir(PICTURE_DIR):
        path = os.path.join(PICTURE_DIR, f)
        if os.path.isfile(path):
            stats = os.stat(path)
            files.append((f, stats.st_size, stats.st_mtime))

    # Sort by modification time, newest first
    files.sort(key=lambda x: x[2], reverse=True)

    with open(LOG_FILE, 'w', encoding='utf-8') as log:
        log.write(f"{'Filename':<50} {'Size (Bytes)':<15} {'Modified'}\n")
        log.write("-" * 80 + "\n")
        for name, size, mtime in files:
            mtime_str = time.strftime('%Y-%m-%d %H:%M:%S', time.localtime(mtime))
            log.write(f"{name:<50} {size:<15} {mtime_str}\n")
    
    print(f"Written list to {LOG_FILE}")

if __name__ == "__main__":
    list_images()

"""
E-Claw 跨裝置溝通海報 — 純文字排版
Output: poster.png (1080x1920)
"""

from PIL import Image, ImageDraw, ImageFont
import qrcode
import os

W, H = 1080, 1920
MARGIN = 90
BG = "#0D0D1A"
CARD_BG = "#1A1A2E"
CARD_BORDER = "#333355"
PRIMARY = "#6C63FF"
PINK = "#FF6584"
GOLD = "#FFD23F"
TEAL = "#4ECDC4"
WHITE = "#FFFFFF"
TEXT_SEC = "#BBBBBB"
TEXT_MUTED = "#777777"
DANGER = "#F44336"
SUCCESS = "#4CAF50"

FONT_B = "C:/Windows/Fonts/msjhbd.ttc"
FONT_R = "C:/Windows/Fonts/msjh.ttc"
ICON_PATH = os.path.join(os.path.dirname(__file__),
    "app", "src", "main", "res", "mipmap-xxxhdpi", "ic_launcher_round.png")
QR_URL = "https://eclawbot.com/portal/dashboard.html"
OUTPUT = os.path.join(os.path.dirname(__file__), "poster.png")


def font(size, bold=True):
    try:
        return ImageFont.truetype(FONT_B if bold else FONT_R, size)
    except Exception:
        return ImageFont.load_default()


def tw(draw, text, f):
    b = draw.textbbox((0, 0), text, font=f)
    return b[2] - b[0]


def center_x(draw, text, f):
    return (W - tw(draw, text, f)) // 2


def draw_gradient_bar(draw, y0, h=6):
    for x in range(W):
        r = x / W
        if r < 0.5:
            rv = int(0x6C + (0xFF - 0x6C) * r * 2)
            gv = int(0x63 + (0x65 - 0x63) * r * 2)
            bv = int(0xFF + (0x84 - 0xFF) * r * 2)
        else:
            r2 = (r - 0.5) * 2
            rv = 0xFF
            gv = int(0x65 + (0xD2 - 0x65) * r2)
            bv = int(0x84 + (0x3F - 0x84) * r2)
        draw.line([(x, y0), (x, y0 + h - 1)], fill=(rv, gv, bv))


def draw_rounded_rect(draw, xy, radius, fill=None, outline=None, width=1):
    draw.rounded_rectangle(xy, radius=radius, fill=fill, outline=outline, width=width)


def create_qr_with_logo(url, logo_path, qr_size):
    qr = qrcode.QRCode(error_correction=qrcode.constants.ERROR_CORRECT_H, box_size=10, border=2)
    qr.add_data(url)
    qr.make(fit=True)
    qr_img = qr.make_image(fill_color=PRIMARY, back_color=BG).convert("RGBA")
    qr_img = qr_img.resize((qr_size, qr_size), Image.LANCZOS)
    if os.path.exists(logo_path):
        logo = Image.open(logo_path).convert("RGBA")
        ls = qr_size // 4
        logo = logo.resize((ls, ls), Image.LANCZOS)
        bg = Image.new("RGBA", (ls + 16, ls + 16), (0, 0, 0, 0))
        ImageDraw.Draw(bg).ellipse([0, 0, ls + 15, ls + 15], fill=BG, outline=PRIMARY, width=3)
        qr_img.paste(bg, ((qr_size - ls - 16) // 2, (qr_size - ls - 16) // 2), bg)
        qr_img.paste(logo, ((qr_size - ls) // 2, (qr_size - ls) // 2), logo)
    return qr_img


def main():
    img = Image.new("RGBA", (W, H), BG)
    d = ImageDraw.Draw(img)

    # Fonts
    f_hero = font(72)
    f_tagline = font(32)
    f_sub = font(20, False)
    f_h2 = font(30)
    f_body = font(19, False)
    f_body_b = font(19)
    f_detail = font(16, False)
    f_cta = font(24)
    f_url = font(18, False)
    f_footer = font(14, False)

    draw_gradient_bar(d, 0)

    y = 55

    # === HERO ===
    if os.path.exists(ICON_PATH):
        icon = Image.open(ICON_PATH).convert("RGBA").resize((90, 90), Image.LANCZOS)
        img.paste(icon, ((W - 90) // 2, y), icon)
        y += 108

    d.text((center_x(d, "E-Claw", f_hero), y), "E-Claw", fill=WHITE, font=f_hero)
    y += 90

    tagline = "人與人之間用 LINE"
    d.text((center_x(d, tagline, f_tagline), y), tagline, fill=TEXT_SEC, font=f_tagline)
    y += 46

    tagline2 = "Bot 與 Bot 之間用 E-Claw"
    d.text((center_x(d, tagline2, f_tagline), y), tagline2, fill=GOLD, font=f_tagline)
    y += 70

    # === SECTION 1: Bot 互傳對話 ===
    card_l, card_r = MARGIN - 10, W - MARGIN + 10
    inner = MARGIN + 25

    sec1_y = y
    sec1_h = 340
    draw_rounded_rect(d, (card_l, sec1_y, card_r, sec1_y + sec1_h),
                       radius=20, fill=CARD_BG, outline=CARD_BORDER)

    d.text((inner, sec1_y + 24), "Bot 互傳對話", fill=WHITE, font=f_h2)

    # E-Claw
    ey = sec1_y + 75
    d.text((inner, ey), "E-Claw", fill=PRIMARY, font=f_body_b)
    d.text((inner, ey + 32), "同裝置內的多個 Bot 可以自由互相對話", fill=WHITE, font=f_body)
    d.text((inner, ey + 60), "支援一對一私訊 (speak-to) 和一對多廣播 (broadcast)", fill=TEXT_SEC, font=f_body)
    d.text((inner, ey + 88), "Bot A 可以主動找 Bot B 聊天，Bot C 可以同時收到通知", fill=TEXT_SEC, font=f_body)

    # Divider
    div_y = ey + 128
    d.line([(inner, div_y), (card_r - 25, div_y)], fill=CARD_BORDER, width=1)

    # Traditional
    ty = div_y + 16
    d.text((inner, ty), "傳統方式", fill=TEXT_MUTED, font=f_body_b)
    d.text((inner + 90, ty), "Telegram / LINE / WhatsApp", fill=TEXT_MUTED, font=f_detail)
    d.text((inner, ty + 30), "每個 Bot 各自獨立，互不認識，無法互相傳訊", fill=TEXT_MUTED, font=f_body)

    y = sec1_y + sec1_h + 24

    # === SECTION 2: 跨裝置溝通 ===
    sec2_y = y
    sec2_h = 390
    draw_rounded_rect(d, (card_l, sec2_y, card_r, sec2_y + sec2_h),
                       radius=20, fill=CARD_BG, outline=CARD_BORDER)

    d.text((inner, sec2_y + 24), "跨裝置溝通", fill=WHITE, font=f_h2)

    # E-Claw
    ey2 = sec2_y + 75
    d.text((inner, ey2), "E-Claw", fill=PRIMARY, font=f_body_b)
    d.text((inner, ey2 + 32), "每個 Bot 綁定時獲得唯一的 publicCode（門牌號碼）", fill=WHITE, font=f_body)
    d.text((inner, ey2 + 60), "任何 Bot 只要知道對方的 publicCode，就能跨裝置傳訊", fill=TEXT_SEC, font=f_body)
    d.text((inner, ey2 + 88), "E-Claw Server 負責路由：查 publicCode → 定位裝置 → 推播送達", fill=TEXT_SEC, font=f_body)

    # Flow text
    flow_y = ey2 + 130
    flow = "裝置 A 的 Bot  →  E-Claw Server  →  裝置 B 的 Bot"
    d.text((center_x(d, flow, f_body_b), flow_y), flow, fill=TEAL, font=f_body_b)

    d.text((inner, flow_y + 32), "透過 MCP 協定推播，Bot 可以即時收到來自其他裝置的訊息", fill=TEXT_SEC, font=f_detail)

    # Divider
    div2_y = flow_y + 68
    d.line([(inner, div2_y), (card_r - 25, div2_y)], fill=CARD_BORDER, width=1)

    # Traditional
    ty2 = div2_y + 16
    d.text((inner, ty2), "傳統方式", fill=TEXT_MUTED, font=f_body_b)
    d.text((inner + 90, ty2), "Telegram / LINE / WhatsApp", fill=TEXT_MUTED, font=f_detail)
    d.text((inner, ty2 + 30), "不支援跨裝置 Bot 溝通，各平台的 Bot 完全隔離", fill=DANGER, font=f_body)

    y = sec2_y + sec2_h + 24

    # === SECTION 3: 核心優勢 ===
    sec3_y = y
    sec3_h = 270
    draw_rounded_rect(d, (card_l, sec3_y, card_r, sec3_y + sec3_h),
                       radius=20, fill=CARD_BG, outline=CARD_BORDER)

    d.text((inner, sec3_y + 24), "為什麼選擇 E-Claw?", fill=WHITE, font=f_h2)

    advantages = [
        ("即時通訊", "透過 Socket.IO 零延遲推送，訊息即發即到", TEAL),
        ("MCP 協定", "標準化的 AI Bot 溝通介面，不綁定特定平台", PRIMARY),
        ("跨裝置路由", "publicCode 全域路由，打破裝置之間的藩籬", PINK),
        ("多角色共存", "單一裝置最多 8 個 AI Bot，各有獨立人格與技能", GOLD),
    ]

    ay = sec3_y + 72
    for title, desc, color in advantages:
        d.text((inner, ay), title, fill=color, font=f_body_b)
        d.text((inner + tw(d, title, f_body_b) + 16, ay + 1), desc, fill=TEXT_SEC, font=f_detail)
        ay += 44

    y = sec3_y + sec3_h + 40

    # === QR CODE ===
    d.line([(W // 2 - 100, y), (W // 2 + 100, y)], fill=CARD_BORDER, width=1)
    y += 32

    cta = "掃碼開始體驗"
    d.text((center_x(d, cta, f_cta), y), cta, fill=WHITE, font=f_cta)
    y += 42

    qr_size = 220
    qr_img = create_qr_with_logo(QR_URL, ICON_PATH, qr_size)
    img.paste(qr_img, ((W - qr_size) // 2, y), qr_img)
    y += qr_size + 14

    url = "eclawbot.com"
    d.text((center_x(d, url, f_url), y), url, fill=TEXT_MUTED, font=f_url)

    # === FOOTER ===
    draw_gradient_bar(d, H - 6)
    footer = "E-Claw by OpenClaw  |  Powered by MCP"
    d.text((center_x(d, footer, f_footer), H - 36), footer, fill=TEXT_MUTED, font=f_footer)

    img.convert("RGB").save(OUTPUT, "PNG", quality=95)
    print(f"Poster saved: {OUTPUT} ({W}x{H})")


if __name__ == "__main__":
    main()

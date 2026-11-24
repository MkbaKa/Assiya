"""Compose README artwork from the project's logo and in-game screenshots.

Run from any directory. Requires Pillow and a CJK font; set README_FONT_DIR
when the Microsoft YaHei fonts are installed outside C:/Windows/Fonts.
"""
from pathlib import Path
import os

from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parents[3]
OUT = Path(__file__).resolve().parent
FONTS = Path(os.environ.get('README_FONT_DIR', 'C:/Windows/Fonts'))


def font(size, bold=False):
    return ImageFont.truetype(str(FONTS / ('msyhbd.ttc' if bold else 'msyh.ttc')), size)


def rounded_image(image, radius):
    result = image.convert('RGBA')
    mask = Image.new('L', result.size)
    ImageDraw.Draw(mask).rounded_rectangle((0, 0, result.width, result.height), radius, fill=255)
    result.putalpha(mask)
    return result


def canvas(size, dark):
    # A quiet blue/lilac field follows the existing Assiya logo palette.
    top, bottom = ((17, 24, 43), (31, 29, 53)) if dark else ((240, 247, 255), (243, 238, 252))
    result = Image.new('RGB', size)
    draw = ImageDraw.Draw(result)
    for y in range(size[1]):
        t = y / (size[1] - 1)
        color = tuple(round(a + (b - a) * t) for a, b in zip(top, bottom))
        draw.line((0, y, size[0], y), fill=color)
    return result.convert('RGBA')


def save(image, name):
    image.save(OUT / f'{name}.webp', quality=94, method=6)


def hero(dark):
    image = canvas((1200, 590), dark)
    draw = ImageDraw.Draw(image)
    fg = '#F2F5FF' if dark else '#20263C'
    muted = '#AEBBD3' if dark else '#5A6681'
    accent = '#8BDFF2' if dark else '#276A87'
    logo = Image.open(ROOT / '.github/assets/logo.png').convert('RGBA')
    # Remove only unused margins; keep the existing project identity intact.
    logo = logo.crop((30, 130, 1510, 860))
    logo.thumbnail((535, 280), Image.Resampling.LANCZOS)
    image.alpha_composite(logo, (44, 48))
    draw.text((65, 330), '让装备上的属性', font=font(43, True), fill=fg)
    draw.text((65, 390), '在游戏里生效', font=font(43, True), fill=fg)
    draw.text((68, 482), '配置驱动属性的格式与行为', font=font(22), fill=muted)
    draw.text((68, 528), 'PAPER  /  LORE + NBT  /  JAVASCRIPT + GROOVY', font=font(13), fill=accent)
    draw.rounded_rectangle((624, 84, 1142, 493), radius=22,
                           fill='#151C2F' if dark else '#FFFFFF')
    draw.text((657, 116), '游戏内属性详情', font=font(24, True), fill=fg)
    shot = Image.open(ROOT / 'assiya-docs/img/getting-start/values.png')
    shot = shot.resize((468, 256), Image.Resampling.NEAREST)
    image.alpha_composite(rounded_image(shot, 8), (649, 170))
    draw.text((657, 450), '/assiya status <玩家名>', font=font(19), fill=muted)
    save(image, 'hero-dark' if dark else 'hero-light')


def reading(dark):
    image = canvas((1200, 490), dark)
    draw = ImageDraw.Draw(image)
    fg = '#F2F5FF' if dark else '#20263C'
    muted = '#AEBBD3' if dark else '#5A6681'
    draw.text((66, 50), '物品上的文字', font=font(22, True), fill=fg)
    draw.text((660, 50), '游戏里的结果', font=font(22, True), fill=fg)
    # The left specimen uses the unmodified default Lore example.
    draw.rounded_rectangle((64, 108, 526, 414), radius=16, fill='#1C1727')
    draw.text((98, 141), '村里最好的剑', font=font(29, True), fill='#FFFFFF')
    draw.line((98, 199, 492, 199), fill='#4D3B63', width=2)
    draw.text((98, 240), '攻击力: 100', font=font(34), fill='#FF7B7B')
    draw.text((98, 326), 'minecraft:diamond_sword', font=font(20), fill='#89818F')
    draw.line((565, 255, 612, 255), fill=muted, width=3)
    draw.line((600, 245, 612, 255, 600, 265), fill=muted, width=3)
    shot = Image.open(OUT / 'equipment-source.png')
    shot = shot.resize((470, round(shot.height * 470 / shot.width)), Image.Resampling.NEAREST)
    image.alpha_composite(rounded_image(shot, 8), (658, 163))
    draw.text((660, 340), '查看属性，再悬停查看数值。', font=font(22), fill=muted)
    draw.text((66, 445), '默认 Lore 格式示例', font=font(18), fill=muted)
    save(image, 'reading-dark' if dark else 'reading-light')


if __name__ == '__main__':
    for is_dark in (False, True):
        hero(is_dark)
        reading(is_dark)

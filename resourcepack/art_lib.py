"""16x16 픽셀아트 헬퍼 (PIL)."""
from PIL import Image
import random

def new(): return Image.new("RGBA", (16, 16), (0, 0, 0, 0))
def hexc(h, a=255):
    h = h.lstrip('#'); return (int(h[0:2],16), int(h[2:4],16), int(h[4:6],16), a)
def shade(c, f):
    return (max(0,min(255,int(c[0]*f))), max(0,min(255,int(c[1]*f))), max(0,min(255,int(c[2]*f))), c[3])
OUT = hexc('2b2118')

def px(im, x, y, c):
    if 0 <= x < 16 and 0 <= y < 16: im.putpixel((x, y), c)

def rect(im, x0, y0, x1, y1, c):
    for y in range(y0, y1+1):
        for x in range(x0, x1+1): px(im, x, y, c)

def ellipse(im, cx, cy, rx, ry, c, shaded=True):
    for y in range(16):
        for x in range(16):
            dx, dy = (x+0.5-cx)/rx, (y+0.5-cy)/ry
            if dx*dx+dy*dy <= 1:
                f = 1.0
                if shaded:
                    f = 1.18 if (dx+dy) < -0.7 else (0.8 if (dx+dy) > 0.7 else 1.0)
                px(im, x, y, shade(c, f))

def outline(im, col=OUT):
    """불투명 픽셀 주변에 외곽선"""
    src = im.copy()
    for y in range(16):
        for x in range(16):
            if src.getpixel((x, y))[3] == 0:
                for dx, dy in ((1,0),(-1,0),(0,1),(0,-1)):
                    nx, ny = x+dx, y+dy
                    if 0 <= nx < 16 and 0 <= ny < 16 and src.getpixel((nx, ny))[3] > 0:
                        im.putpixel((x, y), col); break
    return im

def line(im, x0, y0, x1, y1, c):
    n = max(abs(x1-x0), abs(y1-y0), 1)
    for i in range(n+1):
        px(im, round(x0+(x1-x0)*i/n), round(y0+(y1-y0)*i/n), c)

def speckle(im, c, seed, n=6, mask_color=None):
    r = random.Random(seed); cnt = 0; tries = 0
    while cnt < n and tries < 200:
        tries += 1
        x, y = r.randrange(16), r.randrange(16)
        if im.getpixel((x, y))[3] > 0 and im.getpixel((x, y)) != OUT:
            px(im, x, y, c); cnt += 1

LEAF = hexc('4caf50'); LEAF_D = hexc('2e7d32')

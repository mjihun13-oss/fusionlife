from art_lib import *

ART = {}
def art(name):
    def d(f): ART[name] = f; return f
    return d

CROP_COL = {'tomato':'e53935','corn':'fdd835','rice':'e6c35c','lettuce':'7cb342','pepper':'c62828',
            'onion':'a1527a','garlic':'f1ece0','strawberry':'e91e4f','cabbage':'9ccc65','grape':'7b1fa2'}

@art('tomato')
def _():
    im = new(); ellipse(im, 8, 9.5, 5.5, 4.8, hexc('e53935'))
    for x, y in ((5,5),(7,4),(9,4),(11,5),(8,5)): px(im, x, y, LEAF)
    px(im, 8, 3, LEAF_D); px(im, 8, 4, LEAF_D); px(im, 6, 8, hexc('ff8a80')); px(im, 6, 9, hexc('ff8a80'))
    return outline(im)

@art('corn')
def _():
    im = new(); ellipse(im, 8, 8, 3.2, 6.5, hexc('fdd835'))
    for y in range(3, 14, 2):
        for x in (6, 8, 10): px(im, x, y, hexc('f9a825'))
    for y in range(4, 14, 2):
        for x in (7, 9): px(im, x, y, hexc('fff176'))
    for y in range(7, 15):
        px(im, 4 + (y-7)//3, y, LEAF); px(im, 11 - (y-7)//3, y, LEAF_D)
    return outline(im)

@art('rice')
def _():
    im = new()
    for i, x0 in enumerate((4, 8, 12)):
        line(im, x0, 15, x0 + (i-1)*1, 5, hexc('9e9d24'))
        for k in range(4):
            y = 3 + k*2
            px(im, x0 + (i-1) - 1, y, hexc('f4d56b')); px(im, x0 + (i-1) + 1, y+1, hexc('e6c35c'))
        px(im, x0 + (i-1), 2, hexc('f4d56b'))
    return outline(im)

@art('lettuce')
def _():
    im = new(); ellipse(im, 8, 9, 6, 5.5, hexc('7cb342'))
    ellipse(im, 8, 8, 4, 4, hexc('9ccc65'))
    ellipse(im, 8, 8, 2, 2, hexc('c5e1a5'))
    for x, y in ((4,6),(11,6),(3,10),(12,10)): px(im, x, y, hexc('558b2f'))
    return outline(im)

@art('pepper')
def _():
    im = new()
    for i in range(10):
        x, y = 4 + i, 13 - i
        px(im, x, y, hexc('c62828')); px(im, x+1, y, hexc('e53935')); px(im, x, y-1, hexc('e53935')); px(im, x+1, y+1, hexc('8e0000'))
    px(im, 12, 3, LEAF_D); px(im, 13, 2, LEAF_D); px(im, 12, 2, LEAF); px(im, 11, 3, LEAF)
    px(im, 6, 11, hexc('ff8a80'))
    return outline(im)

@art('onion')
def _():
    im = new(); ellipse(im, 8, 10, 5, 4.5, hexc('b05f8e'))
    for x in (6, 8, 10): line(im, x, 7, x, 14, hexc('8e3f6f'))
    px(im, 8, 5, hexc('d8c27a')); px(im, 8, 4, hexc('d8c27a')); px(im, 7, 5, hexc('b9a45a'))
    px(im, 7, 15, hexc('e0e0e0')); px(im, 8, 15, hexc('e0e0e0')); px(im, 9, 15, hexc('e0e0e0'))
    return outline(im)

@art('garlic')
def _():
    im = new(); ellipse(im, 8, 10, 5, 4.5, hexc('f5f0e6'))
    for x in (6, 8, 10): line(im, x, 7, x-0 if x==8 else x+(1 if x>8 else -1), 14, hexc('cfc6b3'))
    px(im, 8, 5, hexc('f5f0e6')); px(im, 8, 4, hexc('d8cfba')); px(im, 8, 3, hexc('d8cfba')); px(im, 7, 5, hexc('f5f0e6')); px(im, 9, 5, hexc('f5f0e6'))
    px(im, 8, 15, hexc('a1887f'))
    return outline(im)

@art('strawberry')
def _():
    im = new()
    for y in range(4, 14):
        w = int(5 - (y-4)*0.42)
        for x in range(8-w, 8+w+1): px(im, x, y, hexc('e91e4f'))
    for x, y in ((6,6),(9,7),(7,9),(10,9),(8,11),(6,11)): px(im, x, y, hexc('ffe082'))
    px(im, 5, 5, hexc('ff7a99')); px(im, 6, 5, hexc('ff7a99'))
    for x in (5, 6, 7, 8, 9, 10, 11): px(im, x, 3, LEAF)
    px(im, 8, 2, LEAF_D); px(im, 8, 1, LEAF_D)
    return outline(im)

@art('cabbage')
def _():
    im = new(); ellipse(im, 8, 8.5, 6.2, 6, hexc('9ccc65'))
    ellipse(im, 8, 8.5, 4, 4, hexc('c5e1a5'))
    line(im, 8, 3, 8, 14, hexc('7cb342')); line(im, 4, 6, 8, 9, hexc('7cb342')); line(im, 12, 6, 8, 9, hexc('7cb342'))
    return outline(im)

@art('grape')
def _():
    im = new()
    pts = [(5,6),(8,6),(11,6),(6,9),(9,9),(7,12),(10,12),(8,14)]
    pts = [(5,7),(8,7),(11,7),(6.5,10),(9.5,10),(8,13)]
    for (x, y) in pts: ellipse(im, x, y, 2.2, 2.2, hexc('7b1fa2'))
    for (x, y) in pts: px(im, int(x)-1, int(y)-1, hexc('ce93d8'))
    px(im, 8, 3, hexc('6d4c41')); px(im, 8, 4, hexc('6d4c41')); px(im, 9, 3, LEAF); px(im, 10, 3, LEAF); px(im, 10, 2, LEAF_D)
    return outline(im)

# ---- 씨앗 봉투 ----
def seed_packet(accent):
    im = new(); rect(im, 4, 5, 11, 14, hexc('d7b98a')); rect(im, 4, 5, 11, 6, hexc('b89463'))
    for x in range(4, 12, 2): px(im, x, 5, hexc('8d6e3f'))
    rect(im, 5, 8, 10, 12, hexc('f3e6c8'))
    ellipse(im, 7.5, 10, 2, 2, hexc(accent), shaded=False)
    px(im, 12, 13, hexc(accent)); px(im, 13, 14, hexc(accent)); px(im, 3, 14, shade(hexc(accent), 0.8))
    return outline(im)
for cid, col in CROP_COL.items():
    ART['seed_' + cid] = (lambda c=col: seed_packet(c))

# ---- 가공 재료 ----
def sack(col, tie):
    im = new(); ellipse(im, 8, 10, 5.2, 4.8, hexc(col)); rect(im, 6, 4, 10, 6, hexc(col))
    rect(im, 6, 6, 10, 6, hexc(tie)); px(im, 5, 3, hexc(tie)); px(im, 11, 3, hexc(tie))
    px(im, 6, 9, shade(hexc(col), 1.15)); return outline(im)
ART['flour'] = lambda: sack('f5f2ea', 'a1887f')
ART['corn_flour'] = lambda: sack('f6d963', 'a1887f')

@art('rice_grain')
def _():
    im = new()
    for (x, y) in ((6,11),(8,11),(10,11),(7,9),(9,9),(8,7),(5,13),(7,13),(9,13),(11,13)):
        px(im, x, y, hexc('fbfbf3')); px(im, x+1, y, hexc('e6e3d0'))
    return outline(im)

@art('oil')
def _():
    im = new(); rect(im, 6, 5, 9, 13, hexc('e0a800')); rect(im, 7, 2, 8, 4, hexc('cfd8dc')); rect(im, 6, 4, 9, 4, hexc('90a4ae'))
    rect(im, 7, 6, 7, 11, hexc('ffe082')); px(im, 9, 12, hexc('a67c00')); return outline(im)

@art('noodle')
def _():
    im = new()
    for k in range(4):
        y = 4 + k*3
        for x in range(3, 13): px(im, x, y + (1 if (x//2) % 2 else 0), hexc('f2d58c') if k % 2 == 0 else hexc('e6c26b'))
    return outline(im)

@art('seaweed')
def _():
    im = new()
    for i, x0 in enumerate((5, 8, 11)):
        for y in range(2, 15): px(im, x0 + (1 if (y//3 + i) % 2 else 0), y, hexc('2e7d32') if i != 1 else hexc('43a047'))
    return outline(im)

@art('salt')
def _():
    im = new(); ellipse(im, 8, 11, 5.5, 3.5, hexc('f4f6f8'))
    for (x, y) in ((6,6),(8,5),(10,7),(7,8),(9,9)): rect(im, x, y, x+1, y+1, hexc('ffffff')); px(im, x, y+1, hexc('b0bec5'))
    return outline(im)

@art('shell')
def _():
    im = new(); ellipse(im, 8, 8.5, 6, 5.5, hexc('f8bbd0'))
    for x in range(3, 14, 2): line(im, 8, 13, x, 4, hexc('f06292'))
    rect(im, 6, 13, 10, 14, hexc('d28aa5')); return outline(im)

@art('pearl')
def _():
    im = new(); ellipse(im, 8, 8, 4.5, 4.5, hexc('f5f5ff')); px(im, 6, 6, hexc('ffffff')); px(im, 7, 6, hexc('ffffff'))
    px(im, 10, 10, hexc('c5cae9')); px(im, 9, 11, hexc('c5cae9')); return outline(im)

@art('bait')
def _():
    im = new()
    for (x, y) in ((4,6),(5,7),(6,8),(7,9),(8,10),(9,11),(10,12)): px(im, x, y, hexc('e57373')); px(im, x, y+1, hexc('b85c5c'))
    for (x, y) in ((7,5),(8,6),(9,7),(10,8),(11,9)): px(im, x, y, hexc('ef9a9a')); px(im, x, y+1, hexc('c77070'))
    return outline(im)

from art_lib import *
from art_crops import ART

# id: (body, belly, accent, kind, pattern, rarity)
FISH = {
 'carp': ('c98a3d','f0d7a0','a8652a','std','scales',1), 'crucian': ('9a8f6a','d8cfa8','6e6648','std','belly',1),
 'anchovy': ('6f8fa8','dfe9f0','3f5c73','small','lateral',1), 'mackerel': ('3f7fa6','e3eef2','1f3f56','long','stripes',1),
 'sardine': ('8aa7b8','eef3f6','4c6a7c','small','lateral',1), 'loach': ('a38b5a','d9c79a','6b5a35','long','spots',1),
 'catfish': ('5a5a5e','b0b0b4','2f2f33','long','whisker',1), 'pollock': ('b09a74','efe3c8','7d6b48','std','lateral',1),
 'herring': ('7fa3b5','f2f6f8','45687a','std','belly',1), 'tilapia': ('9bb07f','e5ecd4','60744a','std','stripes',1),
 'mullet': ('93a3ad','e8edf0','5d6d77','std','lateral',1), 'bluegill': ('4f8f6f','f0a550','2a5a44','tall','spots',1),
 'sweetfish': ('7fb089','f4f0cf','e8c640','std','spots',2), 'trout': ('7e9a63','f3e6c2','d9534f','std','spots',2),
 'snapper': ('e0605a','f9d3cf','b23b36','std','spots',2), 'flounder': ('b59a6e','efe3c8','7a633a','flat','spots',2),
 'eel': ('3c4a3a','d8d8a8','1f281e','eel','belly',2), 'clownfish': ('f57c00','fff3e0','ffffff','small','stripes',2),
 'squid_fish': ('e9d8e0','ffffff','c58aa8','squid','spots',2), 'snakehead': ('4d5b3a','c9c9a0','2a3320','long','stripes',2),
 'salmon': ('e88a78','ffd0c4','b9503f','big','belly',3), 'seabass': ('7f8f9c','dfe6eb','4b5964','big','lateral',3),
 'blowfish': ('d8c27a','fff4cc','8a7440','round','spots',3), 'yellowtail': ('4f86a8','f3efc7','e8c640','big','lateral',3),
 'angelfish': ('f2e8b0','ffffff','3b3b3b','tall','stripes',3), 'mandarin': ('a8b05a','f0f0c0','5c6a2a','std','spots',3),
 'tuna': ('2f5ea8','d6e3f2','1a3466','big','lateral',4), 'sturgeon': ('6b6f75','d0d0d4','3c3f44','sturgeon','scutes',4),
 'anglerfish': ('5d4a6b','a89ab5','ffee58','round','lure',4), 'moonfish': ('cfd8f5','ffffff','8c9eff','tall','glow',4),
 'icefish': ('bfe8f5','ffffff','7cc7e0','std','glow',4),
 'golden_carp': ('ffc107','fff3b0','e65100','big','scales',5), 'coelacanth': ('3d4f7a','8fa3c9','1b2745','big','scales',5),
 'dragonfish': ('c62828','ffcdd2','ffd54f','dragon','fins',5), 'rainbow_trout': ('7cb342','ffffff','e040fb','big','rainbow',5),
 'kraken_baby': ('6a1b9a','d1a3e8','ffeb3b','squid','spots',5), 'swamp_king': ('3b5d32','a9c58b','1a2d17','big','whisker',5),
}

def mk(spec):
    body, belly, acc, kind, pat, rar = [hexc(x) if i < 3 else x for i, x in enumerate(spec)]
    im = new()
    cx, cy, rx, ry = {'std':(7,8,5.4,3.4),'big':(7.2,8,6.2,4.2),'long':(7.5,8,6.8,2.4),'small':(7.5,8,4.6,2.6),
                      'flat':(7.5,8,6,4.4),'round':(7.5,8,5,4.6),'tall':(7.5,8,4.2,5.2),'eel':(8,8,7,2),'sturgeon':(7.5,8,7,2.8),
                      'dragon':(7.2,8,6.2,3.4),'squid':(8,6,3.5,4)}[kind]
    if kind == 'squid':
        ellipse(im, 8, 6, 3.6, 4.4, body)
        for x in (5, 7, 9, 11): line(im, x, 9, x + (1 if x < 8 else -1) * 0, 14, shade(body, 0.85)); px(im, x, 14, belly)
        px(im, 7, 5, hexc('111111')); px(im, 9, 5, hexc('111111'))
        for (x, y) in ((6,3),(9,3),(8,7)): px(im, x, y, acc)
        return finish(im, rar)
    if kind == 'eel':
        for x in range(1, 15):
            y = 8 + round(1.6 * __import__('math').sin(x / 2.2))
            px(im, x, y, body); px(im, x, y + 1, body); px(im, x, y + 2, belly if x % 3 else shade(belly, .85)); px(im, x, y - 1, shade(body, .8))
        px(im, 2, 8 + round(1.6*__import__('math').sin(2/2.2)), hexc('ffffff'))
        return finish(im, rar)
    ellipse(im, cx, cy, rx, ry, body)
    # 배
    for y in range(int(cy + ry*0.35), 16):
        for x in range(16):
            if im.getpixel((x, y))[3] > 0 and im.getpixel((x, y)) != OUT: px(im, x, y, belly)
    # 꼬리
    tx = int(cx + rx) - 1
    for i in range(4):
        h = 1 + i
        for yy in range(int(cy) - h, int(cy) + h + 1): px(im, tx + i, yy, acc if kind != 'dragon' else hexc('ffd54f'))
    # 등지느러미
    if kind in ('tall', 'dragon', 'std', 'big', 'round'):
        for i in range(4): px(im, int(cx) - 1 + i, int(cy - ry) - 0 - (1 if i in (1, 2) else 0), acc)
    if kind == 'tall':
        for i in range(4): px(im, int(cx) + i // 2, int(cy + ry) + 0, acc)
    if kind == 'dragon':
        for k in range(3): px(im, 3 + k * 3, int(cy - ry) - 1, hexc('ffd54f')); px(im, 3, 6, hexc('ffd54f'))
        px(im, 1, 8, hexc('ffd54f')); px(im, 0, 9, hexc('ffd54f'))
    if kind == 'round':
        for (x, y) in ((3,4),(6,3),(9,3),(12,4),(3,12),(6,13),(9,13),(12,12)): px(im, x, y, shade(body, .7))
    # 무늬
    if pat == 'stripes':
        for x in range(int(cx - rx) + 3, int(cx + rx) - 1, 3):
            for y in range(16):
                if im.getpixel((x, y))[3] > 0 and im.getpixel((x, y)) != OUT: px(im, x, y, acc)
    elif pat == 'spots':
        speckle(im, acc, hash(spec[0]) & 0xffff, 8)
    elif pat == 'lateral':
        for x in range(int(cx - rx) + 2, int(cx + rx) - 1): px(im, x, int(cy), acc)
    elif pat == 'scales':
        for y in range(int(cy - ry) + 2, int(cy + ry), 2):
            for x in range(int(cx - rx) + 3 + (y % 4 == 0), int(cx + rx) - 1, 3):
                if im.getpixel((x, y))[3] > 0 and im.getpixel((x, y)) != OUT: px(im, x, y, shade(body, .8))
    elif pat == 'whisker':
        px(im, int(cx - rx) - 1, int(cy) + 1, OUT); px(im, int(cx - rx) - 1, int(cy) + 2, OUT); px(im, int(cx - rx), int(cy) + 3, OUT)
    elif pat == 'scutes':
        for x in range(int(cx - rx) + 3, int(cx + rx) - 1, 2): px(im, x, int(cy - ry) + 1, acc); px(im, x, int(cy), acc)
    elif pat == 'glow':
        speckle(im, hexc('ffffff'), 7, 6); speckle(im, acc, 9, 6)
    elif pat == 'lure':
        px(im, 4, 2, acc); px(im, 3, 1, acc); px(im, 4, 1, hexc('ffffff')); line(im, 5, 4, 4, 2, OUT)
        for x in range(3, 13, 2): px(im, x, int(cy) + 3, hexc('ffffff'))
    elif pat == 'rainbow':
        cols = ['e53935', 'fdd835', '43a047', '1e88e5', '8e24aa']
        for i, x in enumerate(range(int(cx - rx) + 2, int(cx + rx) - 1)):
            px(im, x, int(cy), hexc(cols[i % 5]))
    # 눈
    ex, ey = int(cx - rx * 0.55), int(cy - ry * 0.25)
    if kind == 'flat': ey = int(cy - ry * 0.5)
    px(im, ex, ey, hexc('ffffff')); px(im, ex, ey + (0 if kind == 'flat' else 0), hexc('111111')) if kind != 'anglerfish' else None
    return finish(im, rar)

def finish(im, rar):
    outline(im)
    if rar >= 4:
        for (x, y) in ((1, 2), (14, 3), (2, 13), (13, 13)): 
            if im.getpixel((x, y))[3] == 0: px(im, x, y, hexc('fff59d'))
    if rar >= 5:
        for (x, y) in ((8, 0), (0, 8), (15, 8), (8, 15)):
            if im.getpixel((x, y))[3] == 0: px(im, x, y, hexc('ffd54f'))
    return im

for fid, spec in FISH.items(): ART['fish_' + fid] = (lambda s=spec: mk(s))

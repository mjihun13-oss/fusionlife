from art_lib import *
from art_fish import ART

BOWL = hexc('eceff1'); BOWL_D = hexc('b0bec5')
def bowl(fill, tops=(), steam=True, fill2=None):
    im = new()
    for y in range(9, 14):
        w = 6 - max(0, y - 11)
        for x in range(8 - w, 8 + w): px(im, x, y, BOWL if y < 13 else BOWL_D)
    rect(im, 5, 14, 10, 14, BOWL_D)
    ellipse(im, 8, 9, 6, 2.2, fill, shaded=False)
    if fill2: speckle(im, hexc(fill2) if isinstance(fill2, str) else fill2, hash(str(fill)) & 0xff, 6)
    for (x, y, c) in tops: px(im, x, y, hexc(c))
    if steam:
        for (x, y) in ((6, 5), (7, 4), (9, 5), (10, 3)): px(im, x, y, (255, 255, 255, 150))
    return outline(im)

def plate(items):
    im = new(); ellipse(im, 8, 10, 7, 4, hexc('eceff1')); ellipse(im, 8, 10, 5, 2.6, hexc('cfd8dc'), shaded=False)
    items(im); return outline(im)

def bottle(liquid, cork='8d6e63', label=None, cup=False):
    im = new()
    if not cup:
        rect(im, 6, 5, 9, 14, hexc('cfe8ef')); rect(im, 7, 2, 8, 4, hexc('cfe8ef')); rect(im, 7, 1, 8, 1, hexc(cork))
        rect(im, 7, 7, 8, 13, hexc(liquid)); rect(im, 6, 9, 9, 13, hexc(liquid))
        px(im, 6, 6, (255, 255, 255, 200))
        if label: rect(im, 6, 10, 9, 11, hexc(label))
    else:
        for y in range(4, 14):
            w = 4 if y > 10 else 5
            for x in range(8 - w, 8 + w): px(im, x, y, hexc('d9f0f7') if x in (8 - w, 8 + w - 1) else hexc(liquid))
        line(im, 11, 2, 9, 7, hexc('ef5350')); px(im, 11, 1, hexc('ef5350'))
    return outline(im)

ART['cooked_rice'] = lambda: bowl(hexc('fbfbf3'), [(5,9,'e6e3d0'),(8,8,'e6e3d0'),(10,9,'ffffff')])
ART['salad'] = lambda: bowl(hexc('7cb342'), [(5,9,'e53935'),(9,8,'e53935'),(7,8,'f5f0e6'),(11,9,'c5e1a5')], steam=False)
ART['tomato_soup'] = lambda: bowl(hexc('d84315'), [(6,9,'ffab91'),(9,8,'7cb342')])
ART['corn_soup'] = lambda: bowl(hexc('fbc02d'), [(6,9,'fff59d'),(10,9,'f9a825')])
ART['spicy_stew'] = lambda: bowl(hexc('c62828'), [(5,9,'ffcc80'),(8,8,'ffffff'),(10,9,'7cb342')])
ART['kimchi_stew'] = lambda: bowl(hexc('bf360c'), [(5,9,'ffcc80'),(7,8,'f48fb1'),(10,9,'ef9a9a')])
ART['ramen'] = lambda: bowl(hexc('f2d58c'), [(5,9,'ffffff'),(6,8,'ffd54f'),(9,9,'7cb342'),(11,8,'e57373')])
ART['fried_rice'] = lambda: bowl(hexc('f4d56b'), [(5,9,'e57373'),(8,8,'fff59d'),(10,9,'7cb342'),(7,9,'ffffff')], steam=False)
ART['kimchi'] = lambda: bowl(hexc('e53935'), [(5,9,'ffffff'),(8,8,'c5e1a5'),(10,9,'ffffff'),(7,9,'fff59d')], steam=False)

def _tomato_egg(im):
    ellipse(im, 8, 10, 4.5, 2.4, hexc('ffd54f'), shaded=False)
    for (x, y) in ((5,10),(7,9),(9,10),(10,9),(8,11)): rect(im, x, y, x+1, y, hexc('e53935'))
ART['tomato_egg'] = lambda: plate(_tomato_egg)

def _sashimi(im):
    for i in range(4): rect(im, 4 + i*2, 8 + (i % 2), 5 + i*2, 11 - (i % 2) + 0, hexc('ff8a80')); px(im, 4 + i*2, 9, hexc('ffd0c4'))
    px(im, 11, 12, hexc('7cb342')); px(im, 12, 12, hexc('7cb342'))
ART['sashimi'] = lambda: plate(_sashimi)

def _grilled(im):
    ellipse(im, 8, 10, 5, 2.2, hexc('c98a3d'))
    for x in (6, 8, 10): line(im, x, 8, x + 1, 12, hexc('6d4c41'))
    px(im, 4, 9, hexc('111111'))
    px(im, 12, 10, hexc('ffeb3b')); px(im, 12, 11, hexc('ffeb3b'))
ART['grilled_fish'] = lambda: plate(_grilled)

def _bulgogi(im):
    ellipse(im, 8, 10, 5, 2.4, hexc('6d3b1f'))
    for (x, y) in ((6,9),(9,9),(8,11),(11,10)): rect(im, x, y, x+1, y, hexc('a0522d'))
    for (x, y) in ((5,10),(10,11)): px(im, x, y, hexc('aed581'))
ART['bulgogi'] = lambda: plate(_bulgogi)

def _sushi(im):
    for x0 in (4, 9):
        rect(im, x0, 9, x0 + 3, 11, hexc('fbfbf3'))
        rect(im, x0 - 1, 8, x0 + 4, 9, hexc('ff8a65')); px(im, x0, 8, hexc('ffd0c4'))
ART['sushi'] = lambda: plate(_sushi)

def _popcorn(im):
    pass

def popcorn():
    im = new()
    for y in range(7, 15):
        for x in range(4, 12):
            px(im, x, y, hexc('e53935') if (x // 2) % 2 == 0 else hexc('fafafa'))
    rect(im, 3, 6, 12, 6, hexc('ffffff'))
    for (x, y) in ((4,3),(6,2),(8,3),(10,2),(5,5),(7,4),(9,5),(11,4)):
        rect(im, x, y, x + 1, y + 1, hexc('fff8e1')); px(im, x, y, hexc('ffecb3'))
    px(im, 7, 3, hexc('fdd835'))
    return outline(im)
ART['popcorn'] = popcorn

def loaf(slice_=False, garlic=False):
    im = new(); ellipse(im, 8, 9, 6.5, 4.2, hexc('c98a3d')); ellipse(im, 8, 8, 5, 2.6, hexc('e0a85a'))
    for x in (5, 8, 11): line(im, x, 6, x + 1, 8, hexc('a96d2c'))
    if garlic:
        for (x, y) in ((6,7),(9,8),(11,7)): px(im, x, y, hexc('fff59d')); 
        for (x, y) in ((7,8),(10,7)): px(im, x, y, hexc('7cb342'))
    return outline(im)
ART['bread_loaf'] = lambda: loaf()
ART['garlic_bread'] = lambda: loaf(garlic=True)

def pizza():
    im = new(); ellipse(im, 8, 8, 7, 7, hexc('e0a85a'), shaded=False); ellipse(im, 8, 8, 5.6, 5.6, hexc('ffca28'), shaded=False)
    for (x, y) in ((6,5),(10,6),(5,9),(9,10),(8,7),(11,9)): ellipse(im, x, y, 1.4, 1.4, hexc('c62828'), shaded=False)
    for (x, y) in ((8,5),(6,8),(10,8),(7,11)): px(im, x, y, hexc('558b2f'))
    for a in ((8, 8, 8, 1), (8, 8, 14, 11), (8, 8, 2, 11)): line(im, *a, hexc('c68a2c'))
    return outline(im)
ART['pizza'] = pizza

def cake():
    im = new(); rect(im, 3, 9, 12, 13, hexc('fff3e0')); rect(im, 3, 11, 12, 11, hexc('ffcc80')); rect(im, 3, 13, 12, 13, hexc('d7a86e'))
    rect(im, 3, 7, 12, 8, hexc('ff5c8a'))
    for x in (4, 7, 10): px(im, x, 9, hexc('ff5c8a')); px(im, x, 10, hexc('ff5c8a'))
    for x in (4, 8, 11): ellipse(im, x, 5.5, 1.7, 1.9, hexc('e91e4f')); px(im, x, 3, LEAF)
    return outline(im)
ART['strawberry_cake'] = cake

ART['tomato_juice'] = lambda: bottle('e53935', label='fff3e0')
ART['grape_juice'] = lambda: bottle('7b1fa2', label='fff3e0')
ART['strawberry_smoothie'] = lambda: bottle('ff6f91', cup=True)
ART['wine'] = lambda: bottle('8e1b3a', cork='6d4c41', label='f5e6c8')
ART['makgeolli'] = lambda: bottle('f4f0e0', cork='90a4ae', label='5d8a4a')

# ---- 도구 / 기계 / 책 ----
def tool_hoe():
    im = new(); line(im, 3, 14, 11, 6, hexc('8d6e3f')); line(im, 4, 14, 12, 6, hexc('6d4c1f'))
    rect(im, 9, 2, 13, 3, hexc('ffd54f')); rect(im, 12, 2, 13, 6, hexc('ffd54f')); px(im, 10, 2, hexc('fff59d')); px(im, 13, 3, hexc('c79a00'))
    px(im, 11, 5, hexc('4dd0e1')); px(im, 12, 5, hexc('00acc1'))
    return outline(im)
ART['hoe_farmer'] = tool_hoe

def fertilizer():
    im = new(); rect(im, 4, 5, 11, 14, hexc('8d6e3f')); rect(im, 4, 5, 11, 6, hexc('6d4c1f'))
    rect(im, 5, 8, 10, 12, hexc('c5e1a5')); line(im, 8, 12, 8, 9, hexc('2e7d32')); px(im, 7, 9, hexc('43a047')); px(im, 9, 10, hexc('43a047'))
    px(im, 12, 3, hexc('ffee58')); px(im, 3, 4, hexc('ffee58')); px(im, 13, 6, hexc('ffee58'))
    return outline(im)
ART['fertilizer'] = fertilizer

def rod(tier):
    cols = {1:('8bc34a','aed581'), 2:('b0bec5','eceff1'), 3:('ffca28','fff59d'), 4:('4dd0e1','b2ebf2'), 5:('e040fb','f3b6ff')}[tier]
    im = new()
    for i in range(12): px(im, 2 + i, 13 - i, hexc(cols[0])); px(im, 3 + i, 13 - i, shade(hexc(cols[0]), .75))
    px(im, 2, 13, hexc('5d4037')); px(im, 3, 13, hexc('5d4037'))
    for i in range(5): px(im, 14, 2 + i, hexc('eeeeee'))
    px(im, 14, 7, hexc('e53935')); px(im, 13, 8, hexc('e53935'))
    px(im, 7, 9, hexc(cols[1])); px(im, 9, 7, hexc(cols[1]))
    if tier >= 5:
        for (x, y) in ((1, 10), (6, 14), (12, 12), (5, 5)): px(im, x, y, hexc('ffd54f'))
    return outline(im)
for t, n in enumerate(('bamboo', 'iron', 'gold', 'diamond', 'legend'), 1): ART['rod_' + n] = (lambda t=t: rod(t))

def book(cover, mark):
    im = new(); rect(im, 3, 2, 12, 14, hexc(cover)); rect(im, 3, 2, 4, 14, shade(hexc(cover), .7)); rect(im, 11, 3, 12, 13, hexc('f5ecd0'))
    mark(im); return outline(im)
def _cb(im): ellipse(im, 8, 8, 2.5, 2.5, hexc('eceff1'), shaded=False); line(im, 6, 11, 10, 11, hexc('eceff1')); px(im, 8, 5, hexc('ff8a65'))
def _dx(im):
    ellipse(im, 8, 8, 2.8, 1.6, hexc('4dd0e1'), shaded=False); px(im, 10, 8, hexc('1a237e')); px(im, 11, 8, hexc('4dd0e1'))
    px(im, 6, 11, hexc('ffd54f')); px(im, 9, 4, hexc('ffd54f'))
ART['cookbook'] = lambda: book('c0392b', _cb)
ART['dex'] = lambda: book('6a1b9a', _dx)

def machine(body, top, detail):
    im = new(); rect(im, 2, 7, 13, 14, hexc(body)); rect(im, 2, 5, 13, 6, hexc(top)); rect(im, 2, 14, 13, 14, shade(hexc(body), .7))
    detail(im); return outline(im)
def _board(im):
    rect(im, 3, 9, 12, 12, hexc('d7b98a')); line(im, 4, 10, 10, 10, hexc('b89463')); line(im, 3, 3, 8, 8, hexc('cfd8dc')); rect(im, 2, 2, 3, 3, hexc('6d4c1f'))
def _pan(im):
    ellipse(im, 7, 9, 6, 3, hexc('424242')); ellipse(im, 7, 9, 4.6, 2, hexc('616161'), shaded=False); rect(im, 12, 8, 15, 9, hexc('8d6e3f'))
def _pot(im):
    rect(im, 3, 6, 12, 13, hexc('78909c')); rect(im, 2, 5, 13, 6, hexc('b0bec5')); rect(im, 3, 3, 12, 4, hexc('90a4ae')); px(im, 7, 2, hexc('cfd8dc')); px(im, 8, 2, hexc('cfd8dc')); rect(im, 5, 8, 10, 8, hexc('546e7a'))
def _oven(im):
    rect(im, 4, 8, 11, 12, hexc('263238')); rect(im, 5, 9, 10, 11, hexc('ff8f00')); px(im, 6, 10, hexc('ffd54f')); px(im, 8, 10, hexc('ffd54f')); px(im, 4, 6, hexc('e53935')); px(im, 7, 6, hexc('e53935'))
def _mixer(im):
    rect(im, 5, 2, 10, 8, hexc('b3e5fc')); rect(im, 6, 4, 9, 8, hexc('ef5350')); px(im, 7, 3, hexc('eceff1')); rect(im, 4, 9, 11, 10, hexc('546e7a')); px(im, 8, 12, hexc('e53935'))
def _mill(im):
    ellipse(im, 8, 8, 5, 5, hexc('9e9e9e')); ellipse(im, 8, 8, 3, 3, hexc('757575'), shaded=False); px(im, 8, 8, hexc('424242')); line(im, 8, 8, 13, 3, hexc('8d6e3f'))
def _press(im):
    rect(im, 6, 2, 9, 7, hexc('90a4ae')); rect(im, 3, 8, 12, 9, hexc('546e7a')); rect(im, 4, 11, 11, 13, hexc('8d6e3f')); px(im, 7, 12, hexc('e53935')); px(im, 9, 12, hexc('7b1fa2'))
def _ferm(im):
    rect(im, 3, 5, 12, 14, hexc('8d6e3f')); 
    for y in (7, 11): rect(im, 3, y, 12, y, hexc('546e7a'))
    rect(im, 5, 3, 10, 4, hexc('6d4c1f')); px(im, 12, 10, hexc('ffd54f')); px(im, 12, 9, hexc('ffd54f'))
def _spr(im):
    pass
ART['m_cutting_board'] = lambda: machine('8d6e3f', 'a1887f', _board)
ART['m_fry_pan'] = lambda: machine('5d4037', '795548', _pan)
ART['m_pot'] = lambda: machine('455a64', '607d8b', _pot)
ART['m_oven'] = lambda: machine('b0bec5', 'eceff1', _oven)
ART['m_mixer'] = lambda: machine('78909c', '90a4ae', _mixer)
ART['m_mill'] = lambda: machine('bdbdbd', 'e0e0e0', _mill)
ART['m_press'] = lambda: machine('6d4c41', '8d6e63', _press)
ART['m_ferment'] = lambda: machine('6d4c41', '8d6e63', _ferm)

def sprinkler():
    im = new(); rect(im, 7, 8, 8, 14, hexc('b0bec5')); rect(im, 5, 14, 10, 14, hexc('78909c')); rect(im, 5, 6, 10, 7, hexc('cd7f32')); rect(im, 6, 4, 9, 5, hexc('e5a35c'))
    for (x, y) in ((2,4),(3,2),(12,2),(13,4),(1,7),(14,7),(8,1)): px(im, x, y, hexc('4fc3f7'))
    for (x, y) in ((3,5),(12,5),(2,8),(13,8)): px(im, x, y, hexc('81d4fa'))
    return outline(im)
ART['m_sprinkler'] = sprinkler

def shop():
    im = new(); rect(im, 3, 7, 12, 14, hexc('8d6e3f')); rect(im, 2, 5, 13, 6, hexc('a1887f'))
    rect(im, 2, 2, 13, 4, hexc('43a047'))
    for x in range(2, 14, 4): rect(im, x, 2, x + 1, 4, hexc('f5f5f5'))
    ellipse(im, 8, 10.5, 2.4, 2.4, hexc('4dd0e1')); px(im, 7, 9, hexc('e0f7fa'))
    return outline(im)
ART['m_seedshop'] = shop

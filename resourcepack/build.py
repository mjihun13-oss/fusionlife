import sys, os, re, json, zipfile, hashlib, shutil
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from art_food import ART
ROOT = os.path.dirname(os.path.abspath(__file__))
SRC = os.path.join(ROOT, '..', 'src', 'main', 'java', 'kr', 'fusion')
OUT = os.path.join(ROOT, 'FusionLife-Pack')

# --- 플러그인 소스에서 커스텀 아이템 id 수집 (텍스처 누락 방지) ---
ids = set()
txt = {n: open(os.path.join(SRC, n), encoding='utf-8').read() for n in os.listdir(SRC) if n.endswith('.java')}
for m in re.finditer(r'\b(?:food|ing)\("([a-z_]+)"', txt['Cooking.java']): ids.add(m.group(1))
for m in re.finditer(r'^\s+f\("([a-z_]+)"', txt['Fishing.java'], re.M): ids.add('fish_' + m.group(1))
for m in re.finditer(r'Items\.reg\("([a-z_]+)"', txt['Fishing.java']): ids.add(m.group(1))
for m in re.finditer(r'\brod\("([a-z_]+)"', txt['Fishing.java']): ids.add(m.group(1))
for m in re.finditer(r'\bcrop\("([a-z]+)"', txt['Farming.java']): ids.update([m.group(1), 'seed_' + m.group(1)])
for m in re.finditer(r'Items\.reg\("([a-z_]+)"', txt['Farming.java']): ids.add(m.group(1))
for m in re.finditer(r'\bm\("([a-z_]+)"', txt['Machines.java']): ids.add('m_' + m.group(1))
for m in re.finditer(r'Items\.reg\("([a-z_]+)"', txt['FusionLife.java']): ids.add(m.group(1))
ids.discard('') 
# 함수 정의 줄(def crop/ing/food 등)에서 잡힌 변수 이름 제거
ids -= {'seed_', 'fish_', 'm_'}
missing = sorted(i for i in ids if i not in ART)
extra = sorted(i for i in ART if i not in ids)
if missing: print('텍스처 없음:', missing); sys.exit(1)
if extra: print('플러그인에 없는 텍스처(무시):', extra)

shutil.rmtree(OUT, ignore_errors=True)
for d in ('assets/fusion/items', 'assets/fusion/models/item', 'assets/fusion/textures/item'):
    os.makedirs(os.path.join(OUT, d))
for i in sorted(ids):
    ART[i]().save(os.path.join(OUT, 'assets/fusion/textures/item', i + '.png'))
    json.dump({"parent": "minecraft:item/generated", "textures": {"layer0": "fusion:item/" + i}},
              open(os.path.join(OUT, 'assets/fusion/models/item', i + '.json'), 'w'))
    json.dump({"model": {"type": "minecraft:model", "model": "fusion:item/" + i}},
              open(os.path.join(OUT, 'assets/fusion/items', i + '.json'), 'w'))
json.dump({"pack": {"pack_format": 46, "supported_formats": {"min_inclusive": 46, "max_inclusive": 99},
                    "description": "§6FusionLife §7농사·낚시·요리 리소스팩"}},
          open(os.path.join(OUT, 'pack.mcmeta'), 'w', encoding='utf-8'), ensure_ascii=False, indent=2)
from PIL import Image
Image.open(os.path.join(OUT, 'assets/fusion/textures/item/tomato.png')).resize((128, 128), Image.NEAREST).save(os.path.join(OUT, 'pack.png'))

zp = os.path.join(ROOT, 'FusionLife-Pack.zip')
with zipfile.ZipFile(zp, 'w', zipfile.ZIP_DEFLATED) as z:
    for dp, _, fs in os.walk(OUT):
        for f in sorted(fs):
            full = os.path.join(dp, f)
            z.write(full, os.path.relpath(full, OUT).replace(os.sep, '/'))
sha = hashlib.sha1(open(zp, 'rb').read()).hexdigest()
print('아이템', len(ids), '종 / zip', os.path.getsize(zp), 'bytes / sha1', sha)
open(os.path.join(ROOT, 'FusionLife-Pack.sha1'), 'w').write(sha + '\n')

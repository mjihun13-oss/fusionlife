# FusionLife (농사 + 낚시 + 요리 + 산업)
명령어 없음 · 설정 없음 · jar를 plugins에 넣으면 오버월드에서 바로 작동. Paper 1.21.x / Java 21.

## 빌드
- GitHub에 올리면 Actions가 자동 빌드 → Artifacts의 FusionLife.jar 다운로드
- 또는 `mvn package` → target/FusionLife.jar

## 플레이
- 첫 접속 시 요리책 지급(우클릭: 전체 레시피). 작업대 레시피는 자동 해금.
- 풀 베기 4% → 야생 씨앗 → 경작지에 우클릭 심기 (작물은 바닐라 밀/비트/당근/감자 모양)
- 비료(우클릭), 스프링클러(반경4 자동 성장), 농부의 괭이(3x3 수확+재심기)
- 낚싯대 5단계, 물고기 36종(바이옴·밤·비·깊이·등급), 오프핸드 미끼
- 조리기구 8종 + 스프링클러 + 출하상자(닫으면 에메랄드/금조각 정산)
- 숙련도(농사/낚시/요리) 자동 상승

## 추가 (v1.1)
- 도감(우클릭): 물고기 36 / 작물 10 / 요리 · 가공품 — 최대 크기·횟수 기록. 첫 접속 시 지급, 작업대에서도 제작 가능
- 씨앗 상점: 기계(상점 블록, 작업대 제작)를 설치 후 우클릭 → 다이아몬드로 씨앗 구매. 가격은 config.yml
- 출하상자(자동 정산) 제거 — 거래는 플레이어끼리

## 리소스팩 (resourcepack/)
- `python3 resourcepack/build.py` → `FusionLife-Pack.zip` + sha1 생성 (플러그인 소스에서 아이템 id를 읽어 텍스처 누락 검사)
- 1.21.4+ 의 item_model 방식. 아이템 110종 전용 아이콘. (설치형 기계는 손에 든 아이콘만 바뀌고 블록은 바닐라 모양)
- 적용: zip을 직접 접근 가능한 URL에 올린 뒤 plugins/FusionLife/config.yml 의 resource-pack.url / sha1 입력 (또는 server.properties 의 resource-pack)
- 팩이 없는 클라이언트에서는 아이템이 깨져 보이므로 required: true 권장

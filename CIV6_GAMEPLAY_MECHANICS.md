# Civilizations VI - Tổng hợp chi tiết cơ chế chơi

> Tài liệu tổng hợp từ nhiều nguồn trên internet (PCGamesN, Eurogamer, Game Rant, CivFanatics, Fandom/Civilization Wiki, wikiHow, KeenGamer, TheGamer, Prima Games, Wikipedia...), nhằm cung cấp bức tranh toàn diện về mọi hệ thống của Civ 6 từ khởi đầu đến cuối game. Bao gồm cả base game và 2 bản mở rộng **Rise and Fall (R&F)** và **Gathering Storm (GS)**.

---

## MỤC LỤC

1. [Tổng quan game](#1-tổng-quan-game)
2. [Thiết lập trận đấu](#2-thiết-lập-trận-đấu)
3. [Bản đồ & địa hình](#3-bản-đồ--địa-hình)
4. [Nguồn tài nguyên (Yields)](#4-nguồn-tài-nguyên-yields)
5. [Housing & Amenities](#5-housing--amenities)
6. [Thành phố & Quận (Districts)](#6-thành-phố--quận-districts)
7. [Adjacency Bonus (Bonus kề cận)](#7-adjacency-bonus-bonus-kề-cận)
8. [Công nghệ, Civic & Policy Cards](#8-công-nghệ-civic--policy-cards)
9. [Chính phủ (Government)](#9-chính-phủ-government)
10. [Đơn vị & Vòng lượt](#10-đơn-vị--vòng-lượt)
11. [Thám hiểm & Barbarian](#11-thám-hiểm--barbarian)
12. [Thương mại & Trade Routes](#12-thương-mại--trade-routes)
13. [Tôn giáo & Đức tin](#13-tôn-giáo--đức-tin)
14. [Great People (Nhân tài)](#14-great-people-nhân-tài)
15. [Thành bang (City-States) & Envoys](#15-thành-bang-city-states--envoys)
16. [Ngoại giao & Quan hệ quốc tế](#16-ngoại-giao--quan-hệ-quốc-tế)
17. [Chiến tranh, Casus Belli & Grievances](#17-chiến-tranh-casus-belli--grievances)
18. [Chiến tranh mệt mỏi (War Weariness)](#18-chiến-tranh-mệt-mỏi-war-weariness)
19. [Rise & Fall: Loyalty, Governors, Golden/Dark Ages](#19-rise--fall-loyalty-governors-goldendark-ages)
20. [Gathering Storm: Khí hậu, Thảm họa, Năng lượng](#20-gathering-storm-khí-hậu-thảm-họa-năng-lượng)
21. [World Congress & Diplomatic Victory](#21-world-congress--diplomatic-victory)
22. [Gián điệp (Spies)](#22-gián-điệp-spies)
23. [6 Điều kiện chiến thắng](#23-6-điều-kiện-chiến-thắng)
24. [Chiến lược theo giai đoạn game](#24-chiến-lược-theo-giai-đoạn-game)
25. [Nguồn tham khảo](#25-nguồn-tham-khảo)

---

## 1. Tổng quan game

- **Civ 6** là game **4X chiến thuật theo lượt** (eXplore, eXpand, eXploit, eXterminate) trên bản đồ **lục giác (hex)**.
- **One Unit Per Tile (UPT):** mỗi ô chỉ chứa 1 đơn vị quân sự. Điều này buộc phải tổ chức đội hình, phối hợp nhiều loại binh chủng thay vì xếp chồng.
- Mỗi **lượt** bao gồm: di chuyển/ra lệnh đơn vị, ra lệnh sản xuất, nghiên cứu công nghệ/civic, xử lý ngoại giao, rồi kết thúc lượt.
- Mục tiêu cuối cùng: đạt **1 trong 6 điều kiện chiến thắng** (Domination, Science, Culture, Religion, Diplomacy-GS, Score) trước khi AI hoặc người chơi khác đạt được.
- Game có **tutorial** trong game dạy các thao tác cơ bản; các nguồn bài viết khuyên người mới nên chơi thử 1-2 ván ở độ khó thấp (Warlord/Prince) với cố vấn bật để làm quen.

---

## 2. Thiết lập trận đấu

Các lựa chọn chính trong màn tạo game:

| Thiết lập | Ý nghĩa |
|---|---|
| **Ruleset** | Base game / Rise and Fall / Gathering Storm (chọn bộ luật áp dụng) |
| **Độ khó** | Từ Settler (dễ) đến Deity (khó). **Prince** = cân bằng (không bonus cho ai). AI được bonus tài nguyên/nghiên cứu ở độ khó > Prince. Người mới nên chơi **Warlord/Prince**. |
| **Tốc độ game** | Online (nhanh, ~½ Standard), Standard, Epic, Marathon (dài, ~2× Standard). Ảnh hưởng thời gian nghiên cứu, sản xuất, số điểm Era cần thiết. |
| **Bản đồ** | Kích thước: Duel (2 người) → Huge (12 người). Loại bản đồ: Continents, Pangaea, Archipelago, Terra... quyết định thế trận. |
| **Cường độ thảm họa** (GS) | Tần suất/mức độ thiên tai. Mặc định khuyên để mức 2. |
| **Advanced Setup** | Tùy chỉnh chi tiết: civ tham gia, điều kiện thắng, tài nguyên, barbarian, thành bang, v.v. |

**Cách đặt thành phố đầu tiên:** Vị trí Settler khởi đầu thường đã tối ưu. Nên gần nguồn nước ngọt (sông/hồ), có đất bằng + đồi (để xây mỏ lấy Production), không lãng phí tài nguyên. Khu đất không có nước ngọt sẽ cần **Aqueduct** sau này.

---

## 3. Bản đồ & địa hình

- **Terrain cơ bản:** Đồng bằng (Plains), Đồng cỏ (Grassland), Đồi (Hills), Sa mạc (Desert), Tundra, Tuyết (Snow), Băng (Ice).
- **Chồng địa hình (Terrain features):** Rừng (Woods), Rừng mưa (Rainforest), Đầm lầy (Marsh), Oasis, lũ (Floodplains).
- **Nước:** Sông (River) cho bonus lớn (thương mại, công trình thủy lợi), Hồ, Đại dương, Bờ biển.
- **Natural Wonders:** Các kỳ quan thiên nhiên (Crater Lake, Mount Everest, Uluru, Great Barrier Reef...) cho yield đặc biệt và Era Score khi khám phá.
- **Độ hấp dẫn (Appeal):** từ Disgusting → Breathtaking; ảnh hưởng đến Neighborhood (Housing), Resort, National Park (du lịch - Culture Victory).
- **Tài nguyên trên ô:**
  - **Bonus resources:** Gia súc, Lúa mì, Cá... (tăng yield, cải tạo bằng Farmer).
  - **Luxury resources:** Dyes, Incense, Pearls, Tobacco, Silk... (cung cấp Amenities).
  - **Strategic resources:** Sắt, Ngựa, Dầu, Than, Uranium... (cần cho đơn vị quân sự & năng lượng).
  - **Lưu ý:** xây district lên ô có tài nguyên sẽ **mất tài nguyên đó** — cần lên kế hoạch trước.

---

## 4. Nguồn tài nguyên (Yields)

Sáu yield chính của một ô/dân/kinh tế:

| Yield | Vai trò |
|---|---|
| **Food (Lương thực)** | Tăng trưởng dân số. Dư thừa Food tích lũy đến mốc để tăng dân. |
| **Production (Sản xuất)** | Xây district, công trình, kỳ quan, đơn vị, dự án. Nguồn chính từ Mỏ, Quarry, rừng, đồi, xưởng. |
| **Gold (Vàng)** | Mua đơn vị/công trình/kỳ quan bằng vàng; mua tài nguyên; chi phí duy trì quân. |
| **Science (Khoa học)** | Nghiên cứu công nghệ (cây Technology). |
| **Culture (Văn hóa)** | Nghiên cứu Civic; mở rộng biên giới thành phố (mỗi Culture tích lũy làm thành phố chiếm thêm ô đất). |
| **Faith (Đức tin)** | Có Pantheon, mua đơn vị tôn giáo, Great People; bản chất đòn bẩy cho tôn giáo/du lịch cuối game. |

Ngoài ra có **Amenities** (đo độ hạnh phúc) và **Housing** (khả năng chứa dân) — hai "yield ngầm" cực quan trọng.

**Cách tăng yield:** dân cư làm việc trên các ô (citizen working tile), xây district/công trình, cải tạo ô bằng Builders, trade routes, chính sách, tôn giáo.

---

## 5. Housing & Amenities

### 5.1 Housing
- Là **giới hạn cứng** cho dân số mỗi thành phố, mô phỏng nơi ở & vệ sinh.
- Dân số **không thể vượt quá** Housing hiện có; càng gần trần, Food càng kém hiệu quả.
- Nguồn tăng Housing:
  - **Nước ngọt** khi định cư (sông/hồ) cho Housing cao hơn hẳn.
  - Công trình: Granary, Sewer, Neighborhood (quan trọng nhất cuối game, bonus theo Appeal từ +1 đến +3), Aqueduct (+6 Housing).
  - Cải tạo: Farm, Pasture, Plantation, Camp, Fishing Boats (cộng dồn dần).
  - Preserve (New Frontier Pass) cho Housing theo độ hấp dẫn.
- **Nguy hiểm khi thiếu Housing:** tăng trưởng ngừng, sản xuất giảm, dân không vui → ảnh hưởng Loyalty, có thể dẫn đến nổi loạn.

### 5.2 Amenities
- Là "Happiness" phiên bản địa phương — mỗi thành phố tự quản lý riêng (thay cho happiness toàn đế chế ở Civ 5).
- **Công thức nhu cầu:** `Amenities cần = ceil(Population/2) - 1` (theo một số nguồn), dân từ ~3 trở lên mới cần amenity đầu tiên. Tỷ lệ phổ biến: 1 Amenity cho mỗi 2 dân (bắt đầu từ dân 3).
- **Ảnh hưởng theo mức thiếu/thừa:**
  - Thừa (Happy/Ecstatic): +5-10% các nguồn không-phải-Food.
  - Đủ (Content): không bonus.
  - Thiếu nhẹ (Unhappy): -10% tăng trưởng dân, -5% yield (mức độ cụ thể thay đổi theo bản vá).
  - Thiếu nhiều (Unrest): ngừng tăng trưởng, -30% yield, có khả năng sinh ra **rebellion** (quân barbarian xuất hiện cạnh thành).
  - Thiếu trầm trọng (Revolt): -50% yield, xác suất nổi loạn cao.
- **Cách tăng Amenities:**
  - **Luxury Resources:** mỗi loại luxury đang cải tạo/sở hữu cấp **1 Amenity cho 4 thành phố** đang thiếu nhất. Bản sao thừa **không tạo thêm amenity** → nên đem trao đổi.
  - **Entertainment Complex** và **Water Park** (district) với các công trình vùng (regional buildings) phủ nhiều thành phố.
  - Một số chính sách (Retainers, New Deal, Propaganda), tôn giáo (River Goddess, Zen Meditation), Great Merchant (tạo luxury mới), Great Engineer.
  - Quan hệ ngoại giao (trao đổi luxury với AI).
- **War Weariness** làm giảm Amenities (xem mục 18).

---

## 6. Thành phố & Quận (Districts)

### 6.1 Khái niệm
- Thay đổi lớn nhất so với Civ 5: **công trình không còn xếp chồng trong 1 ô** nữa. Thành phố giờ là một mạng lưới các **district** chiếm nhiều ô riêng biệt trên bản đồ.
- **City Center** (trung tâm thành) được tính là district đầu tiên (chứa Palace, Monument...).
- Mỗi thành phố có **giới hạn số district** theo dân số: bắt đầu xây được 1 district khi **dân 1**, và thêm 1 slot district mỗi khi dân tăng lên mốc 4, 7, 10, 13, 16... (công thức: dân ≥ 1, 4, 7, 10, 13...).
- Mỗi district chứa tối đa 3 công trình, chỉ xây được khi thành phố đạt dân số tương ứng.

### 6.2 Danh sách District

| District | Nguồn chính | Công trình tiêu biểu | Ghi chú |
|---|---|---|---|
| **Campus** | Science | Library → University → Research Lab | Bonus cạnh núi/rừng mưa |
| **Theater Square** | Culture | Amphitheater → Museum → Broadcast Center | Chứa Great Works; bonus cạnh kỳ quan |
| **Holy Site** | Faith | Shrine → Temple → Worship building | Nơi sinh Great Prophet; bonus cạnh núi/natural wonder |
| **Commercial Hub** | Gold | Market → Bank → Stock Exchange | Bonus cạnh sông/cảng |
| **Harbor** | Gold + Food + Housing | Lighthouse → Shipyard → Seaport | Bonus cạnh tài nguyên ven biển; phải ở ô nước |
| **Industrial Zone** | Production | Workshop → Factory → Power Plant | Bonus cạnh mỏ/quarry; công trình phủ vùng |
| **Encampment** | Quân sự | Barracks/Stable → Armory → Military Academy | Tăng phòng thủ, lưu đơn vị |
| **Entertainment Complex** | Amenities | Arena → Zoo → Stadium | Phủ vùng amenities |
| **Water Park** (GS) | Amenities (nước) | Ferris Wheel → Aquarium → Aquatics Center | Bản thủy của Entertainment Complex |
| **Aerodrome** (GS) | Không quân | Hangar → Airport | Chứa máy bay |
| **Spaceport** | Khoa học (thắng) | Không có công trình | Chỉ để chạy dự án không gian |
| **Aqueduct** | Housing | - | Cầu nối nước, bonus cạnh núi |
| **Neighborhood** | Housing | - | Bonus theo Appeal; mở muộn |
| **Canal** (GS) | Vận chuyển | - | Nối biển qua đất liền |
| **Dam** (GS) | Chống lũ | - | Trên ô lũ (floodplain) |
| **Preserve** (NFP) | Culture + Housing | - | Tăng yield địa hình theo Appeal |

### 6.3 Luật đặt District
- Phải đặt trong phạm vi 3 ô từ City Center (đất thuộc lãnh thổ thành phố).
- **Một district/loại trên 1 ô duy nhất** cho mỗi thành phố; toàn đế chế mỗi loại chỉ 1 ô.
- Không thể đặt lên tài nguyên đã cải tạo (sẽ mất tài nguyên), lên kỳ quan, lên ô có đơn vị khác.
- **Không thể tháo dỡ district** sau khi xây → lên kế hoạch trước là cực kỳ quan trọng (dùng Map Tacks để đánh dấu vị trí dự kiến).
- Chi phí sản xuất district tăng theo số district đã xây toàn đế chế (scaling cost).

### 6.4 District lựa chọn theo chiến thuật
- **Science Victory:** Campus sớm và mạnh.
- **Culture Victory:** Theater Square, đặc biệt là Great Works + Tourism.
- **Religious Victory:** Holy Site sớm để giành Great Prophet.
- **Domination:** Encampment, Industrial Zone.
- **Kinh tế chung:** Commercial Hub + Harbor song song để có nhiều trade routes & gold.

---

## 7. Adjacency Bonus (Bonus kề cận)

Đây là cơ chế cốt lõi của district: **vị trí đặt district quyết định lượng yield nó tạo ra.** Bonus dựa trên các yếu tố nằm kề bên:

| District | Yếu tố cho bonus |
|---|---|
| **Campus** | +1 Science/ô Núi; +½/ô Rừng mưa; +½/district khác |
| **Holy Site** | +2 Faith/ô Natural Wonder; +1 Faith/ô Núi; +½/ô rừng; +½/district |
| **Commercial Hub** | +2 Gold/ô sông; +2 Gold cạnh Harbor; +½/district |
| **Harbor** | +1 Gold/ô tài nguyên biển; +½/district |
| **Industrial Zone** | +1 Production/ô Mỏ (Mine) hoặc Quarry; +½/district |
| **Theater Square** | +1 Culture/ô Kỳ quan; +½/district |
| **Encampment** | Không bonus kề cận (đặt vị trí chiến lược) |
| **Entertainment Complex** | Không bonus kề cận (chỉ cần đất bằng) |

**Nguyên tắc chiến lược:**
- Đặt district **cạnh nhau và cạnh City Center** (City Center được tính như district) để cộng hưởng bonus +½.
- **District chaining:** chuỗi nhiều district cùng vùng đất cạnh nhau giúp tăng đáng kể tổng bonus.
- Trước khi xây, dùng **lens "City Details"** để xem trước yield của từng ô — quyết định vị trí tối ưu.
- Giữ ít nhất 2 ô trống cạnh City Center cho district & kỳ quan cần kề trung tâm.

---

## 8. Công nghệ, Civic & Policy Cards

### 8.1 Hai cây nghiên cứu song song
- **Technology (cây công nghệ):** mở đơn vị, công trình, district, cải tạo đất. Tốc độ theo Science mỗi lượt.
- **Civics (cây chính trị-xã hội):** mở chính phủ, Policy Cards, một số district/công trình. Tốc độ theo Culture.
- Điểm cộng hưởng: hoàn thành điều kiện "boost" (ví dụ: gặp Natural Wonder để boost Astrology) giúp nghiên cứu nhanh hơn — luôn kiểm tra các boost.

### 8.2 Policy Cards
- **Civic** mở ra các **Policy Card** (thẻ chính sách) — bạn lắp vào các **slot chính sách** của chính phủ hiện tại.
- Có 4 loại slot: **Economic, Military, Diplomatic, Wildcard** (slot tự do).
- Ví dụ chính sách quan trọng:
  - `Caravansaries` (+2 Gold mọi trade route) → thay bằng `Triangular Trade` (+4 Gold, +1 Faith) → `E-Commerce` (quốc tế +5 Production, +10 Gold).
  - `Revelation` (+2 Great Prophet points/turn) cho religion.
  - `Scripture` (+100% Holy Site adjacency).
  - `Propaganda` / `Martial Law` (giảm 25% war weariness).
  - `Collectivization` (+4 Food từ domestic trade).
  - `New Deal` (+4 Housing, +2 Amenities, -8 Gold cho thành phố có ≥3 district).
- **Đổi chính sách miễn phí** mỗi khi đổi chính phủ; còn lại đổi mất vàng (tăng theo số lần đổi).

---

## 9. Chính phủ (Government)

- Chính phủ tiến hóa theo thời đại khi nghiên cứu Civic:
  - **Cổ đại:** Chiefdom (thẻ đầu tiên, ít slot).
  - **Cổ điển:** Autocracy (quân sự), Oligarchy (quân sự), Classical Republic (văn hóa).
  - **Trung cổ/Phục hưng:** Monarchy, Theocracy (tôn giáo), Merchant Republic (kinh tế, +2 trade route).
  - **Hiện đại trở đi:** Democracy, Communism, Fascism... với nhiều slot & bonus chuyên sâu.
- **Thẻ được đổi miễn phí khi đổi chính phủ** — một mốc quan trọng để tái cấu trúc chiến lược.
- Mỗi chính phủ có **tính năng riêng** và số slot khác nhau (vd Democracy mạnh về economic + culture, Merchant Republic mạnh về gold/trade).

---

## 10. Đơn vị & Vòng lượt

### 10.1 Loại đơn vị
- **Civilian units:** Settler (lập thành), Builder (cải tạo đất, có số lượt dùng giới hạn), Trader, Scout, Great People, Missionary/Apostle/Inquisitor, Rock Band (du lịch), Archaeologist, Naturalist.
- **Military units:** Melee, Ranged, Cavalry, Naval, Naval Raider, Support (Battering Ram, Siege Tower, Artillery), Anti-Cavalry, Air, Nuclear.
- **Corps & Army (GS):** gộp 2-3 đơn vị cùng loại thành Corps (2) / Army (3) để tăng sức mạnh, tiết kiệm slot UPT. Nghiên cứu tech để mở (Mercenaries/Nationalism...).

### 10.2 Cơ chế chiến đấu
- Mỗi đơn vị có **Combat Strength** (tấn công) và **Defense** (phòng thủ); so sánh trực tiếp quyết định kết quả (hệ thống % thắng dựa trên chênh lệch sức mạnh).
- **Terrain modifier:** phòng thủ trên đồi, sau sông, trong rừng/rừng mưa được cộng.
- **Promotion (thăng cấp):** đơn vị tích lũy XP qua chiến đấu, lên cấp chọn 1 trong vài nhánh promotion (cây thăng cấp theo loại binh).
- **Healing:** đơn vị hồi máu khi không di chuyển/tấn công; trong lãnh thổ mình hồi nhanh hơn.
- **Zone of Control (ZoC):** đơn vị địch cạnh bên cản di chuyển.
- **Hỗ trợ:** Battering Ram/Bombard tăng hiệu quả phá tường thành; máy bay cần Aerodrome/Hangar.
- **Nước:** vận chuyển quân qua biển cần Embark (đơn vị bộ có thể xuống nước sau tech, nhưng không thể tấn công từ nước trừ tàu chiến).
- **Thành phố:** tấn công thành cần phá Vỏ thành (City Defense), lực lượng đồn trú; chiếm thành khi HP về 0 (chọn giữ/tán thiêu - Keep/Raze, hoặc trả về - Return).

---

## 11. Thám hiểm & Barbarian

- **Scout** khám phá bản đồ: phát hiện **Natural Wonders** (Era Score + boost Astrology), **Tribal Villages** (phần thưởng ngẫu nhiên: gold, đơn vị, relic, đức tin, cải tạo...).
- **Barbarian:** quân cướp xuất hiện từ **Barbarian Camps** gần tầm nhìn dân; mở rộng thành Scout → ra quân tấn công.
- Dọn camp sớm quan trọng vì barbarian có thể:
  - Cướp đơn vị civilian (Settler/Builder).
  - Hành quân vào lãnh thổ gây thiệt hại.
- Một số quốc gia/thành bang có bonus khi diệt barbarian (vd Georgia tăng Faith từ kill barbs).

---

## 12. Thương mại & Trade Routes

### 12.1 Cơ bản
- **Trader Unit** tạo mỗi trade route giữa 2 thành phố. Số route tối đa tăng theo district (Commercial Hub, Harbor), chính phủ (Merchant Republic +2), chính sách, thành bang (Carthage...).
- **Yields thay đổi theo đích đến:**
  - **Domestic (nội địa):** chủ yếu **Food + Production** theo các district ở thành đích (City Center +1 Food +1 Production; Campus +1 Food; Harbor/Industrial +1 Production; Theater/Holy Site +1 Food...).
  - **International (quốc tế):** chủ yếu **Gold**, cộng thêm Science/Culture/Faith theo tài nguyên và district của thành đích.
- **Đặc điểm:** mỗi trader **tự xây đường (road)** dọc lộ trình — đường giúp đơn vị di chuyển nhanh, rất quan trọng đầu game.
- **Trading Posts:** khi hoàn tất route, có trading post ở hai đầu; **mở rộng tầm với** (route có thể "nối tiếp" qua các post) và **+1 Gold mỗi post đi qua**.
- **Khoảng cách:** trader di chuyển 1 ô/lượt, tầm 15 ô đất / 30 ô biển (có thể mở rộng bằng trading posts).

### 12.2 Chiến lược trade
- **Đầu game:** ưu tiên **domestic routes** — tăng trưởng thành mới nhanh (~2×), dựng đường nối, an toàn khỏi barbarian. Đặc biệt gửi route Food/Production cho thành mới.
- **Giữa/cuối game:** **international routes** thắng thế — nhiều vàng (có thể 40+ gold/route với chính sách + Golden Age dedication `Reform the Coinage`), phục vụ culture victory (tourism), yêu cầu của thành bang.
- **Lưu ý:** tuyên chiến làm hủy route quốc tế; trader bị cướp (plunder) mất hàng.

---

## 13. Tôn giáo & Đức tin

### 13.1 Trình tự
1. **Pantheon:** khi tích đủ **25 Faith** (Standard), chọn 1 Pantheon belief (từ ~22 lựa chọn) — **vĩnh viễn** áp dụng toàn đế chế. Nên chọn loại cho bonus lâu dài (vd `Divine Spark` +1 GPP từ Holy Site/Campus/Theater, hoặc terrain-based).
2. **Great Prophet:** tích **Great Prophet Points** từ Holy Site (1/turn), Shrine (+1), Temple, Stonehenge, chính sách `Revelation`, dự án Holy Site Prayers. Cần 60 điểm (Standard). **Số GP giới hạn** (~nửa số người chơi, làm tròn lên) → cuộc đua.
3. **Found Religion:** GP đứng trên Holy Site hoặc Stonehenge → chọn tên tôn giáo + **Follower belief + 1 belief khác** (trong 4 loại). Các belief đã chọn **không ai được chọn lại**.
4. **Evangelize:** dùng **Apostle** (có ≥3 charge) để thêm belief — tối đa 4 loại + Pantheon. Apostle cũng dùng để **Launch Inquisition** (mở mua Inquisitor).
5. **Lan truyền:** **Religious pressure** (áp lực tự lan từ thành phố cùng tôn giáo); Missionary rẻ nhưng không tấn công được; Apostle mạnh, có promotion ngẫu nhiên; Inquisitor chuyên **diệt tôn giáo khác** trong lãnh thổ mình.
6. **Theological Combat:** đơn vị tôn giáo giao chiến với nhau (không phải chiến đấu quân sự thường), thua thì "chết"; thắng giúp lan truyền tôn giáo.

### 13.2 Các loại Belief
- **Follower:** bonus nội tại cho thành theo tôn giáo (vd +Food, +Production, +Faith từ công trình).
- **Founder:** bonus dựa trên số tín đồ tôn giáo của bạn trên toàn cầu (vd +Gold/Vàng theo followers).
- **Enhancer:** tăng hiệu quả lan truyền (áp lực, phạm vi, tốc độ thăng cấp tín đồ).
- **Worship:** mở công trình thờ cúng đặc biệt (Cathedral, Mosque, Synagogue, Dar-e Mehr, Wat...) cho bonus Faith + slot Great Work.

### 13.3 Vai trò ngoài Religious Victory
- Faith mạnh dùng để **mua Great People**, **mua Rock Bands** (culture), mua đơn vị quân (vd nhánh Theocracy / tướng).
- Religious Victory: tôn giáo của bạn phải trở thành **tôn giáo đa số của mọi nền văn minh khác** (mỗi thành phố có tôn giáo chủ đạo, chuyển qua Apostle/Missionary). Một số nguồn yêu cầu giữ được nền tôn giáo đó trong N lượt.
- **Số tôn giáo tối đa theo bản đồ:** Duel=2, Tiny=3, Small=4, Standard=5, Large=6, Huge=7.
- Lưu ý: **Kongo (Mvemba a Nzinga)** không thể có Great Prophet; **Arabia** tự nhận GP cuối cùng nếu chưa có.

---

## 14. Great People (Nhân tài)

- Các loại chính: **Great Scientist** (bonus khoa học), **Great Engineer** (thúc sản xuất/kỳ quan), **Great Merchant** (vàng + tài nguyên), **Great General / Great Admiral** (hỗ trợ quân sự), **Great Prophet** (sáng lập tôn giáo), **Great Writer / Artist / Musician** (tạo **Great Works** phục vụ Culture), **Great Merchant** (luxury mới).
- **Cách tích điểm:** mỗi district loại tương ứng + công trình + một số kỳ quan + chính sách (vd `Divine Spark`) sinh GPP mỗi lượt.
- **Kích hoạt (Activate):** đứng tại ô điều kiện (vd Great General đứng cạnh quân → +Combat Strength cho binh trong phạm vi; Great Artist phải đứng tại Theater Square có slot trống để tạo Great Work).
- **Patronage:** dùng Gold hoặc Faith để **mua thẳng** một GP sắp được sinh — tốn nhưng quyết đoán khi đua.
- Great Works (Tác phẩm) gồm: **Written, Art (landscape/portrait), Music, Relics, Artifacts (khảo cổ)** — nguồn Tourism chính cho Culture Victory. Slot trống để trưng bày (Theater Square buildings, một số kỳ quan).
- **Archaeologist:** đào tạo tại Museum có slot để tạo Artifacts từ địa điểm khảo cổ trên bản đồ.
- **Rock Band** (cuối game): đơn vị biểu diễn tạo Tourism tại thành phố nước khác; có thể bị cấm biểu diễn.

---

## 15. Thành bang (City-States) & Envoys

- **Thành bang (CS)** là các quốc gia nhỏ độc lập, không thể thắng game, nhưng cung cấp bonus cho người chơi có **ảnh hưởng**.
- **Envoy (Sứ thần):** nguồn từ Civic (chính sách, kỳ quan), quest của CS, khám phá, chính phủ, yêu cầu... Mỗi CS, **ai gửi nhiều Envoy nhất là Suzerain**.
- **Bậc ảnh hưởng:** 1 Envoy → bonus tài nguyên cho thành thủ đô; 3 Envoy → tăng; 6 → tăng nữa (theo từng CS cụ thể).
- **Suzerain bonus:** quyền sử dụng **đặc quyền riêng của CS** (vd Carthage +1 trade route/Encampment; Zanzibar cấp Cinnamon & Cloves +6 Amenities; La Venta...). Suzerain còn có thể **gọi quân của CS**, giao thương, và nhận Era/Diplomatic bonus.
- **Mất Suzeraincy** nếu CS bị ai khác vượt số Envoy → theo dõi thường xuyên.
- **Conquest:** ai đó tấn công CS → **bị Grievance/warmonger penalty** với toàn thế giới (Liberation War là Casus Belli "sạch").

---

## 16. Ngoại giao & Quan hệ quốc tế

- **Quan hệ (Relations)** được đánh giá qua "hidden agenda" và "public agenda" của từng leader:
  - **Hidden Agenda:** ẩn, mỗi leader có riêng (thích xây kỳ quan, ghét người tuyên chiến...).
  - **Public Agenda:** công khai, ảnh hưởng bởi hành động của bạn (nhiều quân thì bị coi hiếu chiến...).
- **Giao dịch (Deals):** vàng, tài nguyên (luxury/strategic), công nghệ, đất (bán ô), **Diplomatic Favor** (GS) — có thể mua bán thẳng trên bàn đàm phán.
- **Promises:** hứa hẹn với AI (vd không định cư gần họ, không lan tôn giáo). Thực hiện đúng → thêm favor/quan hệ; phá lời → Grievance.
- **Denounce:** phản đối công khai — bước đệm để tuyên chiến Formal War mà không bị penalty quá nặng.
- **Alliances (liên minh):** qua Civic, có nhiều cấp (Research, Military, Economic, Cultural...), cung cấp bonus chung + **1 Diplomatic Favor/turn** mỗi liên minh.
- **Gifting / Bảo trợ:** tặng quà cải thiện quan hệ.
- **AI trading:** AI hay đòi tài nguyên; dùng luxury thừa để mua vàng/công nghệ.

---

## 17. Chiến tranh, Casus Belli & Grievances

### 17.1 Tuyên chiến
- Phải **tuyên chiến trước** khi tấn công (không thể tấn công đơn vị lãnh thổ nước khác khi chưa tuyên chiến).
- Các kiểu tuyên chiến:
  - **Surprise War:** tấn công bất ngờ → penalty nặng nhất.
  - **Formal War:** sau khi Denounce → penalty vừa.
  - **Casus Belli** (các dạng chính đáng): Liberation War (giải phóng), Holy War (tôn giáo), Reconquest, Protectorate (bảo vệ CS), Colonial, War of Territorial Expansion, Golden Age War (R&F), Ideological War... → **giảm mạnh penalty**.
- **Warmonger penalty (bản cũ) / Grievances (GS):**
  - Mỗi hành động chiến tranh (tuyên chiến, chiếm thành, tàn phá) gây **Grievance** với từng nước biết đến bạn — đối tác gây phẫn nộ khi nước đó bị hại.
  - Grievance cao → quan hệ xấu, AI có thể **khởi xướng Emergency/World Congress chống bạn**.
  - **Casus Belli giảm Grievance đáng kể** (vd Golden Age War chỉ 25-50% penalty; Liberation War không penalty cho tuyên chiến).
  - Grievance **giảm dần theo lượt** (trừ một số hiệu ứng ngăn giảm).
  - Cổ đại gần như không penalty (BWS thấp); penalty tăng theo thời đại đến Công nghiệp (BWS 24) rồi giữ nguyên.

### 17.2 Tiến trình chiếm thành
1. Phá **Vỏ thành** (City Ranged Defense) bằng pháo binh/naval/không quân.
2. Tấn công bằng melee/siege để hạ HP thành về 0.
3. Chọn: **Keep** (giữ thành — bonus yield, nhưng ảnh hưởng Loyalty), **Raze** (tàn phá — gây Grievance rất nặng), **Return** (trả về — nhận thiện chí + Diplomatic Favor).

### 17.3 Mẹo chiến tranh
- Đừng tấn công civilian đơn vị (Settler/Builder) nếu không muốn penalty.
- Tập trung chiếm **từng thành một** để giảm penalty tích lũy.
- Giữ lực lượng bọc lót, hỗ trợ pháo binh để giảm thương vong → giảm war weariness.

---

## 18. Chiến tranh mệt mỏi (War Weariness)

- Cơ chế phản ánh tâm lý dân chúng khi chiến tranh kéo dài: giảm sản xuất, giảm Loyalty, tăng nổi loạn.
- **Các yếu tố tăng war weariness:**
  - Thời gian chiến tranh dài.
  - Thương vong (đặc biệt đơn vị bị giết, đặc biệt là khi bị nước ngoài tấn công).
  - Chiến ở **lãnh thổ địch** > trung lập > lãnh thổ mình.
  - Dùng **vũ khí hạt nhân**.
  - Tuyên chiến **Surprise War** (không Casus Belli).
  - Bị **gián điệp địch** kích động bất ổn.
  - **Gandhi** (Ấn Độ): kẻ xâm lược nhận gấp đôi war weariness.
- **Ảnh hưởng:** mỗi **400 điểm weariness** làm mất **1 Amenity** (phân bổ vào các thành phố gần mặt trận, thành phố bị chiếm nhận gấp 4×; dân bản địa không nhận quá mức cần thiết).
- **Cách giảm:**
  - **Ký hòa bình** (giảm nhanh, ~200/turn khi hòa bình; dừng tích lũy).
  - Không dùng WMD.
  - Tránh chiến gần thành mình.
  - Policy: `Propaganda`, `Martial Law` (giảm 25%), `Defense of the Motherland` (không weariness trong lãnh thổ, cần Communism).
  - Casus Belli đúng đắn.
  - Tăng Amenities để bù trừ.

---

## 19. Rise & Fall: Loyalty, Governors, Golden/Dark Ages

### 19.1 Era Score & Great Ages
- Khi bước sang thời đại mới, mỗi nước rơi vào một trong: **Dark Age, Normal Age, Golden Age, Heroic Age**.
- Quyết định bởi **Era Score** = tổng **Era Points** thu được từ **Historic Moments** trong thời đại trước:
  - Khám phá Natural Wonders, châu lục mới.
  - Xây district/công trình/kỳ quan đầu tiên.
  - Nghiên cứu công nghệ đầu tiên của thời đại (thưởng thêm nếu là người đầu tiên).
  - Diệt đơn vị barbarian, chiếm thành...
- **Golden Age:** bonus mạnh — chọn **Dedication** (chuyên hóa mục tiêu: thám hiểm, quân sự, khoa học, tôn giáo, du lịch...), cộng Loyalty áp lực lớn lên thành phố cạnh biên giới, giảm chi phí duy trì quân, yield tăng.
- **Dark Age:** penalty (yield giảm, Loyalty áp lực giảm, quân duy trì đắt) nhưng mở khóa **Dark Age Policies** cực mạnh (vd `Isolationism` +khoa học trong nước...).
- **Heroic Age:** từ Dark Age → đạt ngưỡng Golden Age vượt trội ở thời đại kế → chọn **3 Dedications**. Đây là cách "bù nước rút" phổ biến (Dark Age rồi Heroic).
- **Dedication (Normal Age):** chỉ cho Era Points khi làm hành động phù hợp (động lực leo lên Golden Age).

### 19.2 Loyalty (Trung thành)
- Mỗi thành phố có **Loyalty** (0-100). Thấp → mất kiểm soát, thành **tự do (Free City)**, có thể bị nước khác chiêu mộ/chiếm.
- **Các yếu tố ảnh hưởng:**
  - Khoảng cách đến thủ đô (xa → áp lực âm).
  - **Áp lực Loyalty từ các thành phố lân cận của nước khác** (thành gần biên giới địch → dễ rớt).
  - Amenities thiếu, dân không vui.
  - Dân số thành mình (đông dân → ổn định hơn).
  - Governor tại chỗ (+8 Loyalty ổn định).
  - Golden Age: áp lực tỏa ra mạnh (giúp flip thành địch).
- **Tăng Loyalty:** đặt Governor, đồn quân, tăng Amenities, xây Thành phố gần thủ đô, kỳ quan (Huang He...), tránh mở rộng quá nhanh ở vùng địch.
- **Chiến thuật:** dùng Golden Age + Governor để **flip** thành phố biên giới đối phương (đổi phe không cần chiến tranh).

### 19.3 Governors (Thống đốc)
- 7 governor (từ Civic), mỗi người 1 **cây thăng cấp (3 nhánh)**:
  - **Magnus:** hậu cần (giảm chi phí settler trong thành, tăng production...).
  - **Pingala:** văn hóa/khoa học thành phố (tăng science/culture theo dân).
  - **Victor:** quân sự/phòng thủ (tăng combat, Loyalty).
  - **Liang:** cải tạo/đất (bảo vệ cải tạo khỏi pillage).
  - **Reyna:** kinh tế (tăng gold, giảm chi phí district).
  - **Moksha:** tôn giáo.
  - **Amani:** ngoại giao (tăng Envoy ảnh hưởng, giúp giành Suzerain).
- Governor có thể **chuyển qua lại giữa các thành phố** nhưng cần vài lượt để "ổn định" (establish).
- Được tăng thêm governor theo mốc Civic và các cấp thăng tiến riêng.

---

## 20. Gathering Storm: Khí hậu, Thảm họa, Năng lượng

### 20.1 Thảm họa tự nhiên
- **Loại:** Núi lửa phun (volcano), Lũ lụt (floods), Bão (hurricane/blizzard), Hạn hán (drought - nông nghiệp), Sóng nhiệt.
- Thảm họa **phá hủy cải tạo/district**, giết đơn vị, nhưng **làm đất phì nhiêu** (đất sau lũ tốt hơn — +yield) — một số trường hợp có lợi về lâu dài.
- Cường độ thiên tai đặt ở màn setup.
- Có thể giảm thiểu: **Dam** chống lũ, Flood Barrier (chống dâng biển), dự án cứu trợ.

### 20.2 Năng lượng & Tài nguyên chiến lược
- Từ Công nghiệp trở đi, thành phố cần **Power (điện)** để các công trình muộn hoạt động hết công suất (Research Lab, Broadcast Center...).
- Nguồn điện: **Than (coal), Dầu (oil), Uranium** (nhiều CO2) hoặc **Năng lượng tái tạo** (thủy điện, gió, địa nhiệt, mặt trời — ít/không CO2).
- Năng lượng tái tạo là **tài nguyên sạch**, nhưng một số loại cần địa hình (kênh núi cho thủy điện...).

### 20.3 Biến đổi khí hậu (Climate Change)
- Đốt nhiên liệu hóa thạch sinh **CO2** (than 3.28 carbon/đơn vị, dầu 1.96, uranium 0.768...).
- CO2 tích lũy thúc đẩy **7 giai đoạn khí hậu**: tăng xác suất thiên tai, **băng tan → mực nước biển dâng** (2 giai đoạn: ngập nông rồi ngập sâu; thành ven biển có thể bị mất ô đất).
- **Giảm nhẹ:** chuyển năng lượng sạch, **Carbon Recapture project** (loại 50.000 tấn CO2), Global Climate Accord (World Congress - áp hạn mức phát thải).
- **Chiến thuật "xấu":** nếu đối thủ nhiều thành ven biển, để biển dâng có thể có lợi cho bạn (không đúng/sai đường lối — tùy chiến thuật).

### 20.4 Cơ chế khác của GS
- **Kênh đào (Canal), Dam** — công trình đất.
- **Aerodrome, nút không quân.**
- **Future Era** mới (Robot khổng lồ Giant Death Robot, công nghệ tương lai).
- **Diplomatic Favor & World Congress** (mục 21).

---

## 21. World Congress & Diplomatic Victory

- **World Congress (Đại hội thế giới)** xuất hiện từ đầu **Trung Cổ**, họp **mỗi ~30 lượt**.
- **Diplomatic Favor** — loại tiền tệ ngoại giao:
  - Kiếm: +1/turn cho mỗi thành bang bạn là Suzerain, +1/turn theo cấp chính phủ, +1/turn mỗi liên minh, thực hiện lời hứa, giải phóng thành phố (+100), thắng Scored Competitions...
  - Tiêu: **mua phiếu bầu** trong World Congress, gọi Special Session, **trích lời hứa (promise)** với AI, hoặc **trao đổi thương mại** (mua bán được).
- **Resolutions:** các đề xuất ảnh hưởng toàn cầu — cấm/cho phép tài nguyên, tăng/giảm chi phí đơn vị, cấm tôn giáo... mỗi đề xuất có 2 lựa chọn đối lập (tích cực/tiêu cực).
- **Discussions / Scored Competitions:** cuộc thi có điểm (vd World Games) hoặc **Emergencies** (các nước hợp tác chống lại một nước vi phạm).
- **Special Sessions:** họp khẩn khi có Emergency hoặc khi một nước bị thiên tai nặng (xin viện trợ).
- **Diplomatic Victory:**
  - Từ **Modern Era**, đề xuất **"World Leader"** xuất hiện — bỏ Diplomatic Favor (phiếu) để đổi **Diplomatic Victory Points**.
  - **Cần đạt 10 điểm** (các phiếu đúng của Resolution cũng cho điểm, thắng Scored Competition cho điểm...).
  - Về bản chất: kiểm soát Favor, mua phiếu khôn ngoan, tham gia World Congress đều đặn.
- **AI lưu ý:** AI hay "vote bậy", nên không phải lúc nào chiến lược ngoại giao cũng trực quan; cần tích Favor dư để mua quyết định.

---

## 22. Gián điệp (Spies)

- Mở khóa khi nghiên cứu tech (Defensive Tactics...), nhận thêm Spy từ Civic/tech/Governor.
- **Các hoạt động (Operations):**
  - **Truy cập công nghệ** (đánh cắp khoa học).
  - **Đánh cắp tác phẩm/Gold**.
  - **Recruit Partisans** (kích động nổi loạn), **Foment Unrest** (giảm Loyalty), **Neutralize Governor** (bắt cóc thống đốc - tạm ngưng hiệu lực).
  - **Bảo vệ lãnh thổ:** đặt Spy trong thành để **chống gián điệp** (Counterspy) và phát hiện/xử lý spy địch.
- **Tỷ lệ thành công** phụ thuộc cấp độ Spy (Rookie → Special Agent), phạm vi nhiệm vụ, khu vực (Council), Governor (Amani? thực ra mỗi spy có cây thăng tiến), và **Diplomatic Visibility** (tầm nhìn ngoại giao — càng cao càng dễ).
- **Diplomatic Visibility:** tăng từ trade routes, trading posts, chính sách, alliance... Nó tăng **combat strength** và hiệu quả gián điệp.
- Golden Age dedication `Bodyguard of Lies` giúp Spy làm việc nhanh/ngay.

---

## 23. 6 Điều kiện chiến thắng

### 23.1 Domination (Quân sự)
- Chiếm giữ **mọi thủ đô của mọi nền văn minh còn lại** và giữ được.
- Chiến lược: quân mạnh, hậu cần (traders/roads), siege; cần xử lý Loyalty ở thành chiếm được.

### 23.2 Science (Khoa học)
- Xây **Spaceport**, chạy chuỗi **dự án không gian** (Space Race): phóng vệ tinh → hạ cánh Mặt Trăng → **thuộc địa Sao Hỏa** (Mars Hydro/Colony...) → chiến thắng khi hoàn thành dự án cuối.
- Chiến lược: Campus mạnh, Spaceport ở thành có production cao, chính sách hỗ trợ (Research Grants, Off-world Mission...).

### 23.3 Culture (Văn hóa - Du lịch)
- Vượt **Foreign Tourism** của bạn qua **Domestic Tourism** của mọi nước khác.
- Tạo **Tourism** từ: Great Works, Wonders, Rock Bands, Resort (National Park), trade routes quốc tế (Online Communities), quảng bá...
- **Culture của đối thủ** là "hàng rào" bạn phải vượt — nước có văn hóa cao khó thắng bằng tourism hơn.
- Điều kiện thường tính bằng số **Foreign Tourists > số Domestic Tourists của mỗi nước khác**.

### 23.4 Religious (Tôn giáo)
- Tôn giáo của bạn trở thành **tôn giáo đa số của mọi nền văn minh khác**.
- Chỉ có 1 tôn giáo chiến thắng (nếu 2 nước cùng theo điều kiện, ai "thống trị" hơn).
- Chiến lược: giành GP sớm, lan truyền mạnh (Apostle, Mt. St Michel Martyr, thánh tích), Inquisitor bảo vệ.

### 23.5 Diplomacy (Ngoại giao - GS)
- Tích đủ **10 Diplomatic Victory Points** qua World Congress (mục 21).
- Nguồn điểm: phiếu thắng Resolution, thắng Scored Competitions, World Leader votes (mua bằng Favor).

### 23.6 Score (Điểm - khi hết thời gian)
- Nếu không ai thắng trước, hết **mốc điểm số** (Score Victory, thường là Future Era) → tính tổng điểm (dân số, kỳ quan, thành phố, tech/civic, Great People...).

---

## 24. Chiến lược theo giai đoạn game

### 24.1 Early game (Cổ đại → Cổ điển)
- **Focus:** khám phá (Scout), phòng thủ (1-2 Warriors/Slinger), tăng trưởng, giành Pantheon.
- **Build order gợi ý:** Scout → Slinger/Warrior → Monument → Settler → (Campus hoặc Holy Site tùy chiến thuật) → Builder.
- Đặt thành phố 2-3 gần nước ngọt, đủ Food + Production.
- Nếu chơi tôn giáo: xây Holy Site sớm, đua Great Prophet. Nếu không: Campus sớm.
- **Đừng mở rộng quá nhanh nếu chưa đủ quân/hậu cần** — barbarian và Loyalty (nếu có R&F) sẽ phạt.

### 24.2 Mid game (Trung cổ → Công nghiệp)
- Mở rộng lên **8-12 thành phố**, liên kết bằng trade routes.
- Đa dạng district: Commercial Hub + Harbor để nhiều route & gold.
- Bắt đầu **chuẩn bị victory path**: Science → xây Campus mạnh; Culture → Theater Square + Great Works; Domination → Encampment + Industrial.
- Quản lý **Amenities & Housing** (Neighborhoods, Entertainment Complex, luxury trade).
- Nếu có R&F: đua **Golden Age** qua Era Score; cẩn thận Loyalty ở biên giới.

### 24.3 Late game (Hiện đại → Tương lai)
- **Tập trung 1 victory duy nhất.** Mọi quyết định (công nghệ, civic, chính sách, dự án) phục vụ mục tiêu đó.
- Science: chạy Space Race ngay khi có Spaceport; dùng chính sách Off-world, dự án Research Grant.
- Culture: Rock Bands, National Parks, tourism mở rộng.
- Domination: quân hiện đại (tank, máy bay, nuke), chiến tranh chớp nhoáng.
- Đối phó: theo dõi World Congress (Diplomatic victory của AI), chống gián điệp, chống war weariness khi chiến.
- **Đừng quên Power (điện)** cho công trình cuối nếu chơi GS.

---

## 25. Nguồn tham khảo

- PCGamesN — *Civ 6 strategy guide: beginner tips and tutorials*
- Eurogamer — *Civ 6 guide hub* (Era Score, Religion & Faith, Amenities, Districts, Science/Culture/Domination Victory)
- Game Rant — *Civilization 6: Complete Guide* (how-to, victory types, game modes)
- wikiHow — *How to Play Civilization VI*
- Civilization Wiki / Fandom — các trang: District, Religion, Housing, Amenities, War Weariness, Warmongering, Trade Route, World Congress, Climate, Great Prophet, Great People
- Civ6.fandom.com (Civilization VI Wiki) — Amenities, Religion, Trade Routes, The World Congress, How To Play
- CivFanatics — *Rise and Fall features*, *Trade Routes Guide*, *Housing Guide*
- KeenGamer — *Housing and Amenity Guide*
- TheGamer — *Amenities explained*, *Golden Ages*, *Religious Victory*
- Screen Rant — *Amenity Guide*
- eXputer — *War Weariness*, *Warmonger Penalty*
- Prima Games — *Gathering Storm review*, *How Districts Work*
- Wikipedia — *Civilization VI: Gathering Storm*
- Well of Souls — *Civ 6 Gathering Storm analyst*
- ExpertBeacon — *Domestic vs international trade routes*

---

*Tài liệu mang tính tham khảo tổng hợp; một số con số cụ thể có thể thay đổi theo phiên bản game (bản vá, DLC). Nên kiểm tra Civilopedia trong game cho số liệu chính xác tại bản bạn đang chơi.*

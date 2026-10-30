# A teljes, valós 106 gyakorlat + 13 beépített program — pontos átadás

**Forrásfájl**: [`data/reference/native-catalog-main-7473.json`](../data/reference/native-catalog-main-7473.json).

Ez **adat és négynyelvű tartalom**, nem régi alkalmazáskód. Az AI Studio feladata, hogy létrehozza a saját, natív V2 adatmodellt és a beépített katalógus indításkori seedelését. Ne találjon ki új neveket, ID-ket, edzésprogramokat a megadottak helyett. A `raw` objektumok a történeti extra mezőket megőrzik; `names`, `notes`, `muscles`, `measurementType`, `demo` mindenütt olvasandó. A YouTube keresési találatot ne kezelje ellenőrzött bemutatóvideóként.

**Mért adatok az eredeti katalógus vizsgálatából:**
- Gyakorlatok: **106**, egyedi stabil ID: **106**.
- Kész programok: **13**.
- Név/leírás: mind a 106 elemhez HU/EN/DE/RO, teljes négynyelvű tartalom.
- A 13 program összes gyakorlathivatkozása feloldható a 106 gyakorlatból.
- A sablonprogramok másolhatók/személyre szabhatók, de a gyári etalont nem írhatja felül felhasználói szerkesztés.

## A 13 program teljes listája

1. **Otthoni A/B – Alap** (`home-basic`) — 2 nap: A (7 gyakorlat); B (7 gyakorlat)
2. **Otthoni A/B – 2. szint** (`home-level2`) — 2 nap: A (7 gyakorlat); B (7 gyakorlat)
3. **Otthoni változatos** (`home-varied`) — 2 nap: Teljes test 1 (6 gyakorlat); Teljes test 2 (6 gyakorlat)
4. **Konditermi Full Body** (`gym-fullbody`) — 1 nap: Full Body (6 gyakorlat)
5. **Konditermi A/B** (`gym-ab`) — 2 nap: A (6 gyakorlat); B (6 gyakorlat)
6. **Otthoni felsőtest A/B** (`home-upper-ab`) — 2 nap: Mell / hát / váll (6 gyakorlat); Hát / mell / kar (6 gyakorlat)
7. **Otthoni gyors 30 perc** (`home-quick-30`) — 2 nap: Gyors A (4 gyakorlat); Gyors B (4 gyakorlat)
8. **Calisthenics A/B** (`calisthenics-ab`) — 2 nap: Toló / húzó (6 gyakorlat); Technika / törzs (6 gyakorlat)
9. **Gumiszalagos A/B** (`bands-ab`) — 2 nap: Felsőtest A (6 gyakorlat); Felsőtest B (6 gyakorlat)
10. **Kettlebell Full Body A/B** (`kettlebell-ab`) — 2 nap: Full Body A (6 gyakorlat); Full Body B (6 gyakorlat)
11. **Konditermi felső / alsó** (`gym-upper-lower`) — 4 nap: Felsőtest A (6 gyakorlat); Alsótest A (5 gyakorlat); Felsőtest B (6 gyakorlat); Alsótest B (5 gyakorlat)
12. **Konditermi Push / Pull / Legs** (`gym-ppl`) — 3 nap: Push (6 gyakorlat); Pull (6 gyakorlat); Legs (6 gyakorlat)
13. **Konditermi erő A/B/C** (`gym-strength-abc`) — 3 nap: Guggolás / nyomás (4 gyakorlat); Felhúzás / váll (4 gyakorlat); Vegyes (6 gyakorlat)

## Első kötelező AI Studio unit teszt
1. JSON parse és source/meta egyezés a forrásfájlban.
2. exercise count=106; unique ID count=106; minden nyelven nem üres név és leírás.
3. program count=13; mindegyik nap megnyitható és minden hivatkozás érvényes.
4. `running` és `hiking` mérés-típusa `distance`; a táv/idő egységeket nem lehet ismétlésként értelmezni.
5. Ismételt seed után saját gyakorlat, saját program és saját átírás sértetlen; napló nem változik.
6. Programok valódi UI-kártyából aktiválhatók, nap/gyakorlat megnyitható, edzés indítható.
7. Profilból származtatott `home-basic` / `home-level2` személyes előnézet csak jóváhagyás után állít elő tervet.

A kijelzőn a **106 / 13** számot csak akkor lehet késznek mutatni, ha az összes gyakorlati út és visszaolvasás is működik.

# TrainPilot Native V2 – Rendszerterv és Moduláris Megvalósítási Terv

Átfogó mérnöki és vizuális megvalósítási terv a **TrainPilot Native V2** Android (Phone + Wear OS) alkalmazáshoz. A terv integrálja a felhasználó által jóváhagyott 3 prioritást:
1. **Health Connect és érzékelő szinkronmotor** a valós biometrikus adatokhoz és terhelésbecsléshez (`D04.01`–`D04.26`),
2. **A 4 feltöltött natív mockup lap (24 képernyő) pixel-pontos vizuális és komponens-szintű illesztése**,
3. **Moduláris, lépésről lépésre haladó fejlesztési és tesztelési struktúra**.

---

## Felhasználói döntések és jóváhagyott prioritások

> [!IMPORTANT]
> A Phase 1 interaktív pontosítás során megerősített döntések:
> - **Kiemelt natív motor**: Health Connect és érzékelő szinkronmotor (`androidx.health.connect.client`), amely a telefon és az óra valós biometrikus adatait (pulzus, HRV/rMSSD, alvás, lépések, aktív és nyugalmi energia, regenerációs készenlét) biztosítja a Room adatbázisban és a Coach nézeten.
> - **Mockup illesztés**: Mind a 4 beküldött lap (Kezdőlap & Tervezés, Edzés & Mérés, Napló & Térkép & Egészség, Beállítások & Szerkesztés) vizuális elemei (sötét tónus, borostyánsárga `0xFFFFB800` / `0xFFE5A000` kiemelések, lekerekített kártyák, státuszjelzők, gombhierarchia) mintaszerűen tükröződnek a Jetpack Compose felületen.
> - **Végrehajtási folyamat**: Részletes architekturális terv jóváhagyása után moduláris kódfrissítés, folyamatos `compile_applet` és Robolectric tesztellenőrzés mellett.

---

## 1. Áttekintés és Fő Koncepció

### 1.1 Cél és funkció
A TrainPilot Native V2 egy kompromisszummentes, prémium natív fitnesz és edzésnaplózó ökoszisztéma modern Jetpack Compose és Wear OS alapokon:
- **Nulla függőség felhős adatbázisoktól alapesetben**: A felhasználó minden adata lokálisan és privát módon tárolódik titkosított/védett Room adatbázisban.
- **5+3 Navigációs modell (`docs/01-NAVIGACIO-5-PLUS-3.md`)**:
  - **Felső 5 főoldal**: `Kezdőlap` (Home), `Edzés` (Training), `Napló` (Journal), `Egészség` (Health), `Programok` (Programs).
  - **Felső 3 közvetlen cél**: `Beállítások` (Settings), `Naptár` (Calendar), `Coach` (AI/Algoritmikus edző).
  - Tilos az alsó dock és a régi 2×4 rács elrendezés!
- **Valós natív motorok**:
  - **Health Connect szinkronmotor**: Napi adatok olvasása/írása (alvás, nyugalmi pulzus, HRV, lépésszám, energia).
  - **Edzés- és Pihenőidőzítő motor**: Egyoldali (`TIMED_UNILATERAL`) és kétoldali (`TIMED_BILATERAL`) stopperórák, automatikus léptetés, rezgés- és hangjelzés.
  - **Előre kitöltő motor (`B01`)**: Az előző edzés azonos gyakorlatának utolsó érvényes sorozataiból súly/ismétlés pre-fill.
  - **Epley 1RM és PR detektálás (`C07`/`C08`)**: Minden teljesített sorozat után egyéni csúcs és volumen analízis.
  - **GPS útvonalkövető és privát térképmotor**: Offline és privát útvonalpont-mentés, pontos távolság- és tempó-számítás.
  - **Wear OS Single-Session Timer Protocol**: Kétirányú állapotátadás és képernyőzár (3 másodperces feloldás).

---

## 2. Felhasználói Élmény és Vizuális Rendszer (Mockup Illesztés)

A 4 beérkezett natív mockup lap összesen 24 képernyőjének formai, tipográfiai és kontraszt-szabályai:

### 2.1 Színpaletta és Vizuális Tónus
- **Háttér (`Background`)**: Prémium mélysötét (`0xFF0D1117` / `0xFF0A0E14`), amely OLED kijelzőkön akkumulátorkímélő és kiemeli az aktív elemeket.
- **Kártya és Felület (`SurfaceContainer`)**: Sötét pala-antracit (`0xFF161B22` és `0xFF1F242C`) finom `1.dp` szegéllyel (`0xFF30363D`).
- **Elsődleges Kiemelés (`Primary Amber`)**: Életteli edzés-sárga/arany (`0xFFFFB800`), aktív állapotban (`0xFFE5A000`).
- **Funkcionális Színek**:
  - Befejezett/Sikeres: Smaragdzöld (`0xFF2EA043` / `0xFF3FB950`).
  - Figyelmeztetés/Szünet: Narancssárga (`0xFFD29922`).
  - Hiba/Megszakítás/Törlés: Rubin-piros (`0xFFF85149`).
  - Egészség/Pulzus/HRV: Cyan-kék (`0xFF388BFD`) és Türkiz (`0xFF2DD4BF`).

### 2.2 Képernyő-összevetés a 4 lap alapján
```
┌────────────────────────────────────────────────────────────────────────┐
│ LAP 1: KEZDŐLAP ÉS TERVEZÉS (01-main-pages-and-planning)              │
├──────────────────┬──────────────────┬──────────────────┬───────────────┤
│ 1. Kezdőlap      │ Folytatható edzés kártya, Óra kapcsolat, Napi biometria   │
│ 2. Edzés         │ Gyors edzés, Gyakorlat választás, A/B terv indítás        │
│ 3. Programok     │ Aktuális program, Programlista, Új program gomb           │
│ 4. Naptár        │ Havi nézet pontokkal, Napi edzéstervek és státuszok       │
│ 5. Gyakorlatok   │ Izomcsoport szűrők (Láb, Mell, Hát, Váll, Kar), Kereső   │
│ 6. Gyak. részlet │ Végrehajtás lépései, Izomcsoport anatómia, Hozzáadás gomb │
└──────────────────┴──────────────────┴──────────────────┴───────────────┘

┌────────────────────────────────────────────────────────────────────────┐
│ LAP 2: EDZÉS ÉS MÉRÉS (02-workouts-and-measurement)                   │
├──────────────────┬──────────────────┬──────────────────┬───────────────┤
│ 1. Súlyzós edzés │ Sorozattáblázat (#, Kg, Ism, RIR, Pipa), Pihenő körgyűrű   │
│ 2. Időalapú edzés│ Nagy stopper, Leállítás/Nullázás, Sorozat-léptetés        │
│ 3. Kétoldali mérés│ Bal/Jobb párhuzamos stopper, Külön indítás/szünet         │
│ 4. Kültéri mérés │ GPS útvonal kapcsoló, Élő tempó/távolság, Térkép kártya   │
│ 5. Intervallumok │ Munkaidő, Pihenőidő, Ismétlések, Hang/Rezgés kapcsolók   │
│ 6. Befejezés     │ Időtartam, Volumen, Hangulat ikonok, Megjegyzés, Mentés  │
└──────────────────┴──────────────────┴──────────────────┴───────────────┘

┌────────────────────────────────────────────────────────────────────────┐
│ LAP 3: NAPLÓ, TÉRKÉP ÉS EGÉSZSÉG (03-journal-maps-and-health)         │
├──────────────────┬──────────────────┬──────────────────┬───────────────┤
│ 1. Napló         │ Havi edzéskártyák, PR jelvény, Mini GPS-térkép bélyegkép │
│ 2. Edzés részlet │ Munka/Pihenőidő felbontás, Sorozatok lenyitható listája │
│ 3. Edzésútvonal  │ Teljes képernyős útvonalrajz, 48dp Piros X bezáró gomb    │
│ 4. Fejlődés      │ 1RM becslés görbe, Volumen oszlopdiagram, Személyes rekord│
│ 5. Egészség      │ Források (Samsung Health/Health Connect), 26 biometria     │
│ 6. Coach         │ Készenléti mérőműszer (54/100), Javasolt edzés és terhelés│
└──────────────────┴──────────────────┴──────────────────┴───────────────┘

┌────────────────────────────────────────────────────────────────────────┐
│ LAP 4: BEÁLLÍTÁSOK ÉS SZERKESZTÉS (04-editing-and-settings)           │
├──────────────────┬──────────────────┬──────────────────┬───────────────┤
│ 1. Beállítások   │ 32 témapaletta, 4 nyelv, Időzítő, Kapcsolt eszközök       │
│ 2. Óra & szinkron│ Handoff gomb, Átadási állapot, Óra akku és státusz        │
│ 3. Mentés/Import │ JSON/ZIP export-import, Ellenőrző előnézet, Duplikáció-védelem │
│ 4. Engedélyek    │ GPS, Értesítések, Health Connect, Samsung Health státuszok│
│ 5. Privát fotók  │ Zero-permission Photo Picker, Privát tároló, Előtte/Utána │
│ 6. Program szerk.│ Napok és gyakorlatok átrendezése, Sorozat/Ismétlés/Pihenő │
└──────────────────┴──────────────────┴──────────────────┴───────────────┘
```

---

## 3. Rendszerarchitektúra és Natív Motorok

### 3.1 Architekturális Áttekintés

```
┌────────────────────────────────────────────────────────────────────────────────────────┐
│                                JETPACK COMPOSE UI LAYER                                │
│  ┌──────────────────────────────────────────────────────────────────────────────────┐  │
│  │ TopFivePlusThreeHeader (Kezdőlap | Edzés | Napló | Egészség | Programok + 3 Cél) │  │
│  └──────────────────────────────────────────────────────────────────────────────────┘  │
│          ▲                              ▲                               ▲              │
│          │                              │                               │              │
│  ┌───────────────┐              ┌───────────────┐               ┌───────────────┐      │
│  │ Home/Training │              │ Active Workout│               │ Health & Coach│      │
│  │ Screens V01-06│              │ Screens V07-12│               │ Screens V17-18│      │
│  └───────┬───────┘              └───────┬───────┘               └───────┬───────┘      │
│          │                              │                               │              │
├──────────┴──────────────────────────────┴───────────────────────────────┴──────────────┤
│                             TRAINPILOT VIEWMODEL & REPOSITORY                          │
│  - Reactive StateFlow (UI State, Navigation Backstack, Active Workout Session)         │
│  - Multi-language localizer (HU, EN, DE, RO)                                           │
│  - 32 Native Theme Palettes (`LocalTrainPilotTokens`)                                  │
├────────────────────────────────────────────────────────────────────────────────────────┤
│                                  NATIVE ENGINES LAYER                                  │
│  ┌───────────────────────┐ ┌───────────────────────┐ ┌──────────────────────────────┐  │
│  │ Health Connect Engine │ │ Workout & Stopwatches │ │ Epley 1RM & PR Badging       │  │
│  │ (26 metrics, D04.01)  │ │ (Unilateral/Bilateral)│ │ (Volume & Strength Analysis) │  │
│  └───────────────────────┘ └───────────────────────┘ └──────────────────────────────┘  │
│  ┌───────────────────────┐ ┌───────────────────────┐ ┌──────────────────────────────┐  │
│  │ GPS Route & Map Engine│ │ Wear Protocol Bridge  │ │ Backup & Migration Engine    │  │
│  │ (Polyline & Distance) │ │ (Single Timer Owner)  │ │ (JSON inspection & dedup)    │  │
│  └───────────────────────┘ └───────────────────────┘ └──────────────────────────────┘  │
├────────────────────────────────────────────────────────────────────────────────────────┤
│                                LOCAL DATA PERSISTENCE                                  │
│  ┌──────────────────────────────────────────────────────────────────────────────────┐  │
│  │ Room Database: Exercises, Sessions, Sets, HealthMetrics, Programs, Schedules     │  │
│  └──────────────────────────────────────────────────────────────────────────────────┘  │
└────────────────────────────────────────────────────────────────────────────────────────┘
```

### 3.2 Kiemelt: Health Connect és Érzékelő Szinkronmotor
A motor biztosítja az Egészség (`V17`) és Coach (`V18`) képernyők biometrikus adatfolyamát:
1. **Health Connect kliens ellenőrzése**:
   - `HealthConnectClient.getSdkStatus(context)` vizsgálata (telepítve / frissítés szükséges / nem támogatott).
2. **Engedélykészlet**:
   - Pulzus (`HeartRateRecord`), Lépésszám (`StepsRecord`), Alvás (`SleepSessionRecord`), Aktív kalória (`ActiveCaloriesBurnedRecord`), Nyugalmi pulzus (`RestingHeartRateRecord`).
3. **Szinkronizáció és gyorsítótárazás**:
   - A beolvasott adatok automatikusan a `HealthMetricEntry` Room táblába kerülnek a `D04.01`–`D04.26` kulcsok alá.
   - Ha a Health Connect nem elérhető vagy nincs engedélyezve, a motor elegánsan helyi rögzítést kínál kézi bevitellel vagy a legutolsó érvényes méréssel, megelőzve az összeomlást.
4. **Coach készenléti számítás (`calculateCoachReadiness`)**:
   - 3 különálló mutató aggregálása:
     - `deviceReadiness`: Hardveres vagy Health Connect alvás + HRV arány (0–100),
     - `trainPilotReadiness`: Edzésterhelési volumen és izomcsoport-pihenőidő arány (0–100),
     - `overallScore`: Súlyozott átlag magyarázó szöveggel ("A legutóbbi terhelés korlátozza a javaslatot").

---

## 4. Moduláris Megvalósítási Terv és Fázisok

### 1. Fázis: Vizuális és Komponens Finomhangolás a Mockupok Alapján
- **Színek és stílustokok**: A `TrainPilotTheme.kt` és komponensek ellenőrzése a 4 feltöltött kép színvilágához igazítva (`#FFB800` sárga gombok, lekerekített kártyák, chip és pill jelvények).
- **Fejléc és 5+3 elrendezés**: A `TopFivePlusThreeHeader` vizuális megjelenésének finomítása, hogy a fülek aktív állapota és a 3 felső ikon pontosan megfeleljen a mockupoknak.
- **24 képernyő részletei**:
  - `V01` Kezdőlap: Alvás, Pulzus, Lépések, Samsung energia widgetek és folytatható edzés kártya.
  - `V07`–`V10` Edzésképernyők: Stopper, pihenő körgyűrű, egyoldali/kétoldali oszlopos időzítők.
  - `V13`–`V16` Napló és Fejlődés: Havi szűrő, PR matricák, térkép bélyegképek, 1RM és volumen grafikon.
  - `V17`–`V18` Egészség és Coach: 26 biometrikus mérőszám kártyái és a körcikk-diagramos készenléti mutató.

### 2. Fázis: Health Connect és Érzékelő Szinkronmotor Építése
- `HealthConnectEngine.kt` modul létrehozása a domain/data rétegben.
- Engedélykérési szerződés (`PermissionController.createRequestPermissionResultContract()`) bekötése a `V22` Engedélyek képernyőhöz.
- Periodikus vagy felhasználó által kezdeményezett ("Frissítés" gomb) szinkronizáció beépítése, amely frissíti a 26 kulcsot a Room táblában.
- Samsung Health és Health Connect státuszok elkülönített megjelenítése a `V17` képernyőn a mockup szerint.

### 3. Fázis: Tesztelés és Ellenőrzés
- Új Robolectric tesztek írása a Health Connect motorhoz és az adatszinkronizációhoz (`TrainPilotHealthConnectTest.kt`).
- Teljes projekt fordítási ellenőrzése a `compile_applet` eszközzel.
- Egységtesztek futtatása a telefonos (`:app`) és órás (`:wear`) modulokon (`gradle :app:testDebugUnitTest :wear:testDebugUnitTest`).

# TrainPilot Native V2 — AI Studio indítóutasítás

**Feladat:** teljesen új, használható, natív Android + Wear OS TrainPilot alkalmazást készíts Kotlin/Jetpack Compose alapon. Ez a repó egy **specifikációs indulócsomag**; kezdetben nincs benne alkalmazáskód. **A teljes alkalmazáskódot, teszteket, Gradle-t, erőforrásokat, workflow-kat és APK-kat te hozod létre**, fázisonként.

## Források fontossági sorrendje
1. `AGENTS.md` és ez a dokumentum; friss felhasználói döntés a **felső 5+3 navigációról** a `docs/01-NAVIGACIO-5-PLUS-3.md` szerint.
2. **A négy eredeti natív mockup**: `design/mockups/REFERENCES.md`, összesen 24 tervezett nézet. A képek grafikai referenciák, NEM kész UI-képernyőfotók.
3. `design/specifications/01–05`: vizuális rendszer, a 24 nézet működési szerződése, történeti adatbekötés, elfogadási feltételek, 81 megőrzési követelmény.
4. `docs/02-MEGLEVO-FUNKCIOK-1.3.71.md` és `docs/03-RESZLETES-FUNKCIOLELTAR.md`: régi TrainPilot képességei, tételes paritásleltár.
5. Olvasási/viselkedési referencia: [eredeti TrainPilot](https://github.com/fuloplevente1998/TrainPilot) és [korábbi AI Studio próbálkozás](https://github.com/fuloplevente1998/TrainPilot-AI-Studio). Ezek **nem szerkesztendők**.

## Végleges navigáció — semmi más
**Öt felső főoldal:** Kezdőlap / Edzés / Napló / Egészség / Programok.
**Három kisebb felső cél:** Beállítások / Naptár / Coach.
A Fejlődés a Napló/Coach felől érhető el. Nincs alsó dock, régi 2×4 menü, WebView-főképernyő, negyedik kis gomb. A mockupokat megjelenésben kövesd, a régi alsó navigációs ábrákat **ne** másold.

## Architektúra és nem alkuképes működés
- Natív Kotlin, Compose, Navigation, ViewModel/StateFlow, Room, DataStore, WorkManager, Health Connect és csak indokolt integrációk. Telefon + külön natív Wear OS app, shared üzleti modell/protokoll.
- Valódi felhasználói adatok, működő gombok, mentés, validáció, hibakezelés, offline működés. Nincs statikus demó, beégetett egészség- vagy GPS-mérés, kamu Coach, inaktív díszgomb.
- Edzéstervek/napok, gyors edzés, sorozatok, RIR, súly/ismétlés/idő/táv, automatikus előtöltés, PR és napló, képek, naptár, Coach/progresszió; Health Connect/Samsung Health, GPS, Wear OS, Drive/Calendar integráció. Tételes kötelező lista: `docs/02-` és `docs/03-`.
- A telefon és óra között egy edzés- és időzítő-tulajdonos legyen, offline eseménynaplóval és nyugtázott átadással. Nem lehet kettős rögzítés, elveszett session, hamis siker.
- Teljes HU/EN/DE/RO lokalizáció, választható témák (32 történeti paletta) és kontraszt, 320dp/2x betűméret, TalkBack, >=48dp érintési felületek.
- Google, Health és Samsung hitelesítési/partnerengedélyek hiányában valós állapotot és üzenetet jeleníts meg, ne hamisíts integrációs sikert.
- A napló és személyes adatok egyszeri áthozatalát lásd `docs/04-ADATATVITEL.md`; **nincs szükség folyamatos main ↔ V2 szinkronra**.
- Ne másold egy az egyben a régi Android/ vagy WebView/ alkalmazást ebbe a repóba: új, karbantartható appot készíts, a régi repo olvasási teszt-orákulum. Hasznosított algoritmus esetén paritást és teszteket igazolj.

## Végrehajtás és kapuk
1. **Fázis 0:** `docs/05-FEJLESZTESI-ES-TESZTTERV.md` szerinti teljes követelmény/architektúra-vázlat, modul- és route-leltár. A 24 nézet és 81 követelmény kapjon felelős adatforrást és elfogadást.
2. **Fázis 1:** valóban indítható natív Android projekt, teljes 5+3 navigáció és adaptív, mockup-alapú shell. Nincs nem működő funkció kikiáltva késznek.
3. **Fázis 2:** valódi lokális edzésmotor, tervek, aktív edzés, napló és adatbázis; perzisztencia és teszt.
4. **Fázis 3:** program/Coach/progresszió, GPS/időmérés, Health, Wear és engedélyek valódi működéssel.
5. **Fázis 4:** import/backup, Drive/Calendar, többnyelvűség, teljes funkció-paritás, készülékes regresszió.
6. **Minden fázis végén:** új commit a V2 repóban, Android + szükség esetén Wear Gradle-build GitHub Actionsban, egység/Compose/UI tesztek, letölthető **külön telepíthető** debug APK, bizonyítható teszteredmények. Ne állítsd késznek a funkciót pusztán azért, mert fordul.

## Forráskezelés
Csak ezt a repót módosíthatod: `fuloplevente1998/TrainPilot-Native-V2`.
Az eredeti `TrainPilot` és a `TrainPilot-AI-Studio` csak olvasandó. Kiadási aláírást, meglévő telepítést és éles adatot nem érinthetsz automatikusan. Új tesztcsomag használjon elkülönített `applicationId`-t.

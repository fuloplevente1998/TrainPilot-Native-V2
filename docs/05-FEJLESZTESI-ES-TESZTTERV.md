# V2 — mérhető megvalósítási és tesztkapuk

Ez nem arra utasít, hogy csak tervet készíts: **valós alkalmazást kell létrehoznod** a V2 repóban, de kis, ellenőrizhető egységekben.

| Fázis | Kész eredmény | Kötelező bizonyíték |
|---|---|---|
| 0. Követelmény | Tételes összerendelés: 24 mintanézet, 81 UI/megőrzési pont, teljes funkcióleltár, modulok és adatmodellek | verziózott ellenőrzőlista; minden eltérés nyitottként jelölt |
| 1. Natív shell | Android Compose projekt; felső 5+3 navigáció; adaptív oldalak, themes, négy nyelv | sikeres `assembleDebug`; képernyőteszt 320–393dp és font2; letölthető APK |
| 2. Alap edzésút | Programok, gyakorlatkönyvtár, gyors/tervezett edzés, set editor, napló, PR, tartós helyi adat | reális end-to-end: új terv→edzés→lezárás→napló→újraindítás→megmaradt adatok |
| 3. Idő/GPS/Coach | timer/interval, kétoldali mozgás, GPS, valós route és állapot, Coach/Progresszió | engedély/hiány/stop/resume/hiba tesztek; nem kitalált számok |
| 4. Health/Wear | Health Connect/Samsung, Wear OS, offline ack tulajdonosprotokoll | telefon–óra tesztek, duplamérés elleni forgatókönyv, real source-provenance |
| 5. Backup/felhő | JSON/ZIP roundtrip és személyes import; Drive/Calendar | roundtrip/deduplikáció/megszakítás/konfliktus teszt |
| 6. Release gate | 24 nézet és teljes leltár, accessibility, minden nyelv és paletta, migráció | valós eszközön tesztelt működés; telefon+Wear artifact, nincs P0/P1 nyitott hiba |

Minden fázisban: `TODO/NOT_IMPLEMENTED` látható és őszinte; nem lehet done egy letakaratlan demo. Feature-paritás per sor: Missing / Implemented / Unit tested / Integration tested / Device verified; a legfelső állapot nem következik automatikusan az előzőből. Fordítható APK ≠ funkcionális készség.

A GitHub Actions workflow-okat az AI Studio készítse el a kóddal együtt. Az első körben külön telepíthető `com.repforge.app.nativev2.debug`-szerű debug azonosító használható (név csak javaslat); ellenőrizni kell az alkalmazásazonosítót, provider authority-kat, deeplinkeket és Wear/Health jogosultságokat. Az eredeti release-t soha ne írja felül automatikusan.

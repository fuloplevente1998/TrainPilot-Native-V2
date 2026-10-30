# Változásjegyzék (CHANGELOG) — TrainPilot Native V2

## [2.0.0-debug] — 2026-10-30

### Fázis 0: Követelmény- és Architektúra-leltár
- Rögzítve a 24 natív nézet (`V01`–`V24`) és a 107 követelmény (`A01`–`E20` + `D04.01`–`D04.26`) adatforrás- és elfogadási leképezése (`docs/07-V2-ARCHITEKTURA-ES-PARITASMATRIX.md`).
- Elkülönített debug csomagazonosító (`com.repforge.app.nativev2.debug`) konfigurálva a meglévő `com.repforge.app` telepítések védelmére.

### Fázis 1: Natív Android + Wear OS Shell, Felső 5+3 Navigáció, 32 Téma és 4 Nyelv
- Megvalósítva a kötelező felső **5+3 navigáció** (`Kezdőlap`, `Edzés`, `Napló`, `Egészség`, `Programok` + `Beállítások`, `Naptár`, `Coach`), alsó navigáció és 2×4 menü nélkül, 320dp keskeny kijelző és 2.0x betűméret támogatással.
- Megvalósítva a 32 történeti színpaletta (16 Basic + 16 Vivid) kontrasztvédett (`onAccent`, `accentText`, `selectedBorder`) tokenekkel.
- Teljes HU / EN / DE / RO futásidejű lokalizáció.

### Fázis 2: Lokális Edzésmotor, Room Adatbázis, Előtöltés, Aktív Edzés és Napló
- Room perzisztencia (`TrainPilotDatabase`) programokhoz, gyakorlatkatalógushoz, aktív edzés drafthoz, edzésnaplóhoz, PR-ekhez, naptárhoz, egészségmetrikákhoz, testsúlynaplóhoz, privát GPS-útvonalakhoz és edzésfotókhoz.
- Automatikus súly/ismétlés előtöltés a Napló legutóbbi teljesített megfelelő gyakorlatából (`B01`), teljesítettség és RIR öröklése nélkül.
- Súlyzós (`V07`), egyoldali időalapú (`V08`), kétoldali időalapú (`V09`) és kültéri/távolság (`V10`) aktív edzésmódok, sorozat-visszavonással (`Undo`), pihenőidőzítő szüneteltetéssel (`G04`), valamint explicit lezárási űrlappal (`V12`, `G07`).
- Edzésnapló kereséssel, PR-részletekkel (`Új PR` korábbi → új rekord), és utólagos 6-fokozatú effort/fájdalom/megjegyzés szerkesztéssel (`C03`).

### Fázis 3: Coach/Progresszió, Naptár Ciklus-tervező, GPS Útvonaltérkép, Egészség (26 kulcs) és Wear OS
- `NativeCoachReadiness` motor: szétválasztott mai Samsung Energy Score, TrainPilot saját regenerációs becslés és terhelés/fájdalom által korlátozott Coach pontszám (`V18`), gyakorlatonkénti progressziós javaslatokkal.
- Havi 7 oszlopos naptárrács és többhetes ismétlődő tervező (`E02`) napi, váltott napos és hétköznapi ütemezéssel, előnézettel.
- Privát GPS-útvonal rögzítés és `MercatorRouteCanvas` teljes képernyős útvonaltérkép piros X bezárással (`V15`).
- 26 egészségmetrika (`D04.01`–`D04.26`), forrásmegjelöléssel és nyers rekordböngészővel, hamisított szenzoradatok nélkül (`V17`).
- Telefon↔Wear OS edzés- és időzítő-tulajdonos protokoll (`ownerEpoch`, nyugtázott átadás, offline eseménynapló) és külön `:wear` modul korona/lünetta és 3 mp-es érintésvédelem-feloldással.

### Fázis 4–5: Egyszeri Személyes Adatátvitel, JSON/ZIP Mentés és Automatizált Tesztek
- Egyszeri, jóváhagyott JSON/ZIP backup import előnézettel, entitásszámlálóval, determinisztikus deduplikációval, megszakított művelet helyreállításával és visszaolvasási ellenőrzéssel (`docs/04-ADATATVITEL.md`, `V21`).
- Robolectric és egységtesztek a navigációra, adatbázisra, előtöltésre, kétoldali időmérésre, Coach-ra, naptárra és backup deduplikációra, valamint GitHub Actions CI workflow (`.github/workflows/android-v2-ci.yml`).

### Fázis 6: Health Connect és Érzékelő Szinkronmotor, 4 Natív Mockup Pontos Illesztése
- Létrehozva a `HealthConnectAndSensorEngine` natív modul az Android Framework beépített HealthConnectManager (API 34+) és a hardveres szenzorok (SensorManager lépésszámláló és pulzusmérő) kezelésére.
- Bekötve a valós idejű `syncHealthData()` művelet, amely a 26 biometrikus mérőszámot (`D04.01`–`D04.26`) lokálisan frissíti a Room táblában és újraszámítja a Coach felkészültségi pontszámot.
- Mockup 1 (`01-main-pages-and-planning`): Kezdőlapon beépítve a Galaxy Watch7 kapcsolati kártya és a 2x2 biometriai rács (Alvás, Nyugalmi pulzus, Lépések, Samsung energia).
- Mockup 3 (`03-journal-maps-and-health`): Egészség képernyőn elkülönített Adatforrások kártya (Samsung Health & Health Connect) Frissítés és Engedélyek gombokkal, 6 elemes Napi adatok csemperács. Coach képernyőn Kulcsjelek szekció és Javasolt edzés kártya nagy sárga `▶ Edzés indítása` gombbal.
- Bővítve a Robolectric tesztkészlet (`healthConnectAndSensorEngine_syncsMetricsAndUpdatesCoachReadiness`).

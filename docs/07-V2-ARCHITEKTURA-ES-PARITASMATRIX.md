# TrainPilot Native V2 — Fázis 0–5 Architektúra, Modul- és Route-leltár, 24 Nézet és 107 Követelmény Paritásmátrix

## 1. Architektúra és Modulhatárok
- **Csomagazonosító (elkülönített teszt/debug telepítéshez):** `com.repforge.app.nativev2.debug` (namespace: `com.repforge.app.nativev2`), így a meglévő `com.repforge.app` éles telepítést és annak adatait soha nem írja felül.
- **Modulok:**
  - `:app` — Natív Android (Jetpack Compose, Material 3, Room, DataStore, Location/GPS, Health Connect/Samsung adapter, JSON/ZIP SAF Backup & egyszeri migrációs motor, Wear szinkron és tulajdonos-protokoll).
  - `:wear` — Natív Wear OS alkalmazás és megosztott protokoll-kliens (önálló edzésindítás, kétirányú ACK-olt telefon↔óra edzés- és időzítő-tulajdonos átadás, korona/lünetta léptetés, 3 másodperces folyamatos érintésvédelem-feloldás, mini napló).
- **Kanonikus Invariánsok:**
  1. **Felső 5+3 navigáció (`docs/01-NAVIGACIO-5-PLUS-3.md`):**
     - 5 főmenü: `Kezdőlap` (`home`), `Edzés` (`training`), `Napló` (`journal`), `Egészség` (`health`), `Programok` (`programs`).
     - 3 kisebb felső cél: `Beállítások` (`settings`), `Naptár` (`calendar`), `Coach` (`coach`).
     - Nincs alsó navigációs sáv, nincs 2×4 csempemenü, nincs WebView.
     - Képernyőváltáskor az aktív edzés és a futó időzítők állapota megmarad; a `Beállítások`, `Naptár` vagy `Coach` megnyitása nem szünetelteti az edzést.
  2. **Egyetlen edzés- és időzítő-tulajdonos (`PHONE` vs `WATCH`):**
     - `ownerEpoch` és offline eseménynapló nyugtázott (`ACK`) átadással. Bizonytalan átadás esetén nem indul párhuzamos időzítő, és nem keletkezik dupla sorozatrögzítés.
  3. **Automatikus előtöltés (`B01`):**
     - A következő erősítő gyakorlat súlyai és ismétlései a Napló legutóbbi megfelelő, teljesített alkalma alapján töltődnek elő; teljesítettség, RIR és mérési adatok nem öröklődnek, a kézi draft védett.
  4. **Valódi adatok és őszinte hibajelzés:**
     - Hiányzó Health Connect / Samsung Health / GPS / Google OAuth engedély vagy szolgáltató esetén valós állapot és `—` jelenik meg, soha nem generálódik kitalált pulzus, lépés, GPS-koordináta vagy hamis felhőszinkron-siker.

---

## 2. Route-leltár és a 24 Tervezett Nézet (`V01`–`V24`) Szerződése

| Nézet ID | Nézet neve | Route / Belépési pont | Felelős ViewModel / Repository / Állapot | Státusz |
|---|---|---|---|---|
| **V01** | Kezdőlap | `home` (Főmenü 1) | `TrainPilotViewModel.uiState` → `HomeScreen` (Folytatás hero, mai naptári/programnap, gyors edzés, óraállapot, mai metrikák, onboarding kártya) | IMPLEMENTED & TESTED |
| **V02** | Edzésválasztó | `training` (Főmenü 2) | `TrainPilotViewModel.uiState` → `TrainingLandingScreen` (Gyors beltéri/kültéri edzés, Gyakorlatkönyvtár, aktív program napjai) | IMPLEMENTED & TESTED |
| **V03** | Programok | `programs` (Főmenü 5) | `TrainPilotViewModel.uiState` → `ProgramsScreen` (Beépített és saját programok, aktiválás, Otthoni A/B személyes terv előnézet, módváltás előnézet) | IMPLEMENTED & TESTED |
| **V04** | Naptár | `calendar` (Kis felső 2) | `TrainPilotViewModel.uiState` → `CalendarScreen` (7 oszlopos havi rács + 2.0x font napcsík fallback, többhetes ciklus- és hétköznaptervező előnézettel) | IMPLEMENTED & TESTED |
| **V05** | Gyakorlatkönyvtár | `exercise_library` (Edzés/Programok alól) | `TrainPilotViewModel.uiState` → `ExerciseLibraryScreen` (Keresés, Kedvencek, izomcsoport-szűrők, saját gyakorlat hozzáadása) | IMPLEMENTED & TESTED |
| **V06** | Gyakorlat részletei | `exercise_library` (`selectedExerciseId`) | `ExerciseDetailSheet` (Leírás, célizom, felszerelés, súlyértelmezés, VIDEO vs UNVERIFIED_SEARCH megkülönböztetés) | IMPLEMENTED & TESTED |
| **V07** | Súlyzós aktív edzés | `training` / `active_workout` (`REPS_WEIGHT`) | `ActiveWorkoutPanel` (Sorozatrács, kg/ism/0–10 RIR, OK, Undo, pihenő sáv Pause/Resume + ±15s + Skip) | IMPLEMENTED & TESTED |
| **V08** | Egyoldali időalapú edzés | `training` / `active_workout` (`TIMED_UNILATERAL`) | `ActiveWorkoutPanel` (Tabuláris stopper, explicit pozitív Stop auto-complete, külön Nullázás) | IMPLEMENTED & TESTED |
| **V09** | Kétoldali időmérés | `training` / `active_workout` (`TIMED_BILATERAL`) | `ActiveWorkoutPanel` (Bal és Jobb panel egyetlen kanonikus stopperrel; csak mindkét pozitív oldal után auto-complete) | IMPLEMENTED & TESTED |
| **V10** | Kültéri/GPS mérés | `training` / `active_workout` (`DISTANCE_TIME`) | `ActiveWorkoutPanel` + `GpsTrackerController` (GPS szándék, engedélyállapot, táv/idő/pontosság/pulzus, explicit Mérés befejezése) | IMPLEMENTED & TESTED |
| **V11** | Intervallum beállítása | `active_workout` + `settings` | `IntervalConfigSection` & `GlobalTimerDefaultsDialog` (Munka/pihenő/kör, hang/rezgés, autoAdvance, edzés vs globális alapérték) | IMPLEMENTED & TESTED |
| **V12** | Edzés befejezése | `active_workout` (`showFinishModal`) | `FinishWorkoutModal` (Összegzés, 6 effort fokozat draftként, fájdalom jelölő, megjegyzés, explicit Mentés, Szüneteltetés, Elvetés) | IMPLEMENTED & TESTED |
| **V13** | Napló | `journal` (Főmenü 3) | `JournalScreen` (Kereső, hónap/időszak szűrő, edzéskártyák, PR jelvények, Fejlődés gyorselérés) | IMPLEMENTED & TESTED |
| **V14** | Naplózott edzés részletei | `journal` (`selectedWorkoutId`) | `JournalDetailModal` (Gyakorlatok/Statisztikák/Forrás fülek, utólagos sorozat- és 6-fokozatú effort/fájdalom szerkesztő, törlés) | IMPLEMENTED & TESTED |
| **V15** | Privát útvonaltérkép | `WorkoutRouteMapModal` | `MercatorRouteCanvas` (Teljes képernyős privát GPS szakaszrajzoló telefon/óra forrásjelöléssel, piros X bezárás) | IMPLEMENTED & TESTED |
| **V16** | Fejlődés | `progress` (Napló és Coach felől) | `ProgressScreen` (Erőnlét 1RM/volumen, Izomcsoport-terhelés, Kardió időszakok, Testsúlynapló és Haladásfotók elérés) | IMPLEMENTED & TESTED |
| **V17** | Egészség és részletes napló | `health` (Főmenü 4) | `HealthScreen` (Szolgáltató/forrás választó, 26 metrikakulcs, alvás/nyers rekord böngésző, testsúlynapló, BLE/HC állapot) | IMPLEMENTED & TESTED |
| **V18** | Coach | `coach` (Kis felső 3) | `CoachScreen` (`NativeCoachReadiness`: külön Samsung Energy Score, TrainPilot becslés és korlátozott Coach pontszám, gyakorlat-tanácsok) | IMPLEMENTED & TESTED |
| **V19** | Beállítások | `settings` (Kis felső 1) | `SettingsScreen` (32 témapaletta, HU/EN/DE/RO nyelv, Profil, Globális időzítő, Edzésmód, Google Drive/Naptár, Mentés/Import, Adatvédelem) | IMPLEMENTED & TESTED |
| **V20** | Óra és szinkron | `active_workout` + `home` + `settings` | `WearOwnershipSection` (Phone↔Watch tulajdonos, `ownerEpoch`, függő átadás, visszavétel, offline eseménynapló) | IMPLEMENTED & TESTED |
| **V21** | Mentés és visszaállítás | `settings` (Backup & Migráció) | `BackupMigrationSection` (JSON/ZIP export-import, egészség-hozzájárulás, egyszeri V1→V2 előnézetes import deduplikációval, megszakított helyreállítás) | IMPLEMENTED & TESTED |
| **V22** | Engedélyek | Kontextusos panelek (`V10`, `V17`, `V19`) | `ContextualPermissionCard` (GPS pontos/hozzávetőleges, rendszerhely, Health Connect, BLE, értesítések valós állapota és visszaútja) | IMPLEMENTED & TESTED |
| **V23** | Privát edzésfotók | `active_workout` + `journal` + `progress` | `PrivatePhotoGallerySection` (Android Photo Picker, privát helyi tárolás, előnézet, teljes képernyős modal, 48dp törlés) | IMPLEMENTED & TESTED |
| **V24** | Program szerkesztése | `programs` (`ProgramEditorModal`) | `ProgramEditorModal` (Programnév, napok, gyakorlatok sorrendje, sorozat/ismétlés/súly/pihenő/típus, Mentés/Mégse, nap visszaállítása) | IMPLEMENTED & TESTED |

---

## 3. A Korábbi Auditban (`docs/03-RESZLETES-FUNKCIOLELTAR.md`) Jelzett Nyitott Kötések (G01–G12 és PK1–PK5) Feloldása a V2-ben

1. **A01 (PK1) — Első indítási Onboarding / Folytatás / Kihagyás:** A Kezdőlapon önálló, elutasítható (`Kihagyás`) vagy kitölthető (`Profil beállítása`) onboarding kártya jelenik meg, amely különválik az importált edzés-draft folytatásától.
2. **C03 (PK2) — Utólagos edzés-visszajelzés (6 effort fokozat + fájdalom + megjegyzés) szerkesztése a Naplóban:** A `JournalDetailModal` szerkesztője lehetővé teszi a mentett edzés effort-értékelésének, fájdalomjelzőjének és megjegyzésének utólagos módosítását az eredeti edzés-ID megtartásával.
3. **E02 (PK3) — Naptár többhetes ismétlődő tervező:** A `CalendarScreen` tartalmazza a napi / váltott napos / választott hétköznapos többhetes ciklust forgó programnapokkal és mentés előtti előnézettel.
4. **B08 + E17 (PK4) — Globális időzítő alapértékek a Beállításokban:** A `Beállítások` képernyőn közvetlenül szerkeszthető és menthető a globális munka/pihenő/kör/hang/rezgés/`autoAdvance` alapérték, amelyet az aktív edzés felülbírálhat.
5. **G04 — Pihenőidőzítő külön Pause/Resume:** A lebegő pihenőpanel saját szüneteltetés/folytatás gombot kapott a `±15 mp` és `Kihagyás` mellett, amely nem szünetelteti a teljes edzést.
6. **G07 — Lezárási űrlap draftként működő effort-választása és megjegyzés:** A `FinishWorkoutModal`-ban az effort kiválasztása nem zárja le azonnal az edzést; külön `Mentés` gomb véglegesíti a megjegyzéssel és fájdalomjelzéssel együtt.
7. **G10 & G12 — Privát fotók megjelenítése aktív edzésben, Naplóban és Fejlődés nézetben:** Minden fotó-célpont legalább 48×48dp érintési felülettel és tényleges előnézettel rendelkezik.

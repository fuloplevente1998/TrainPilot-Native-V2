# Történeti források, olvasási referenciák

A V2 repóban **szándékosan nincs átmásolva alkalmazásforrás**. Az AI Studio az eredeti forrásokból funkciókat és teszt-orákulumot olvas, de az új V2 programot teljes egészében maga építi fel.

- [Eredeti TrainPilot](https://github.com/fuloplevente1998/TrainPilot): meglévő működő app, semmilyen V2 commit nem kerülhet oda.
- [Korábbi TrainPilot-AI-Studio](https://github.com/fuloplevente1998/TrainPilot-AI-Studio): régi natív implementáció és hiba-/teszttapasztalat, szintén csak olvasásra.
- [Natív mockupok, 4 képtábla](https://github.com/fuloplevente1998/TrainPilot-AI-Studio/tree/main/docs/reference/native/design/mockups).
- [Korábbi natív UI szerződés](https://github.com/fuloplevente1998/TrainPilot-AI-Studio/tree/main/docs/reference/native/design/final-ui-ux).
- [Funkcióleltár forrása](https://github.com/fuloplevente1998/TrainPilot-AI-Studio/blob/main/docs/reference/audits/NATIVE_FINAL_FEATURE_INVENTORY.md).
- [WebView 1.3.71 referenciabuild](https://github.com/fuloplevente1998/TrainPilot-AI-Studio/tree/main/reference/stable-main).

**Kritikus verziókonfliktus:** a történeti `01-VISUAL_SYSTEM`, `02-SCREEN_CONTRACTS` és más dokumentumok eredetileg 5 alsó fület, a későbbi AI Studio pedig 5 felső + 3 kis felső elemet ismert. A V2-ben csak az új felső **5+3** navigáció érvényes.

Mockup lehet grafikai kiindulás, de minden képen megjelenő adat/action csak valódi V2 implementáció után nevezhető késznek.

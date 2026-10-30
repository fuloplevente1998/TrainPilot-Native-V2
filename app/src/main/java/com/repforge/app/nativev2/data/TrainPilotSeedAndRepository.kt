package com.repforge.app.nativev2.data

import com.repforge.app.nativev2.data.db.CalendarScheduleEntity
import com.repforge.app.nativev2.data.db.ExerciseCatalogEntity
import com.repforge.app.nativev2.data.db.ProgramDayEntity
import com.repforge.app.nativev2.data.db.ProgramEntity
import com.repforge.app.nativev2.data.db.ProgramExerciseEntity
import com.repforge.app.nativev2.data.db.TrainPilotDao
import com.repforge.app.nativev2.domain.DemoSourceType
import com.repforge.app.nativev2.domain.ExerciseLoadType
import com.repforge.app.nativev2.domain.TrainingProfile
import com.repforge.app.nativev2.domain.WeightSemantics
import java.time.LocalDate

object TrainPilotSeedCatalog {
    fun builtInExercises(): List<ExerciseCatalogEntity> = listOf(
        ExerciseCatalogEntity(
            id = "ex_bench_press",
            name = "Fekvenyomás rúddal",
            muscleGroup = "Mell",
            equipment = "Rúd + fekpad",
            loadType = ExerciseLoadType.REPS_WEIGHT.name,
            weightSemantics = WeightSemantics.TOTAL_BARBELL_OR_MACHINE.name,
            description = "Lapockák összezárva a padon, kontrollált leengedés a mellkas alsó részéhez, majd erőteljes kinyomás.",
            demoSourceType = DemoSourceType.VERIFIED_VIDEO.name,
            demoUrl = "https://www.youtube.com/results?search_query=barbell+bench+press+form",
            isCustom = false,
            isFavorite = true
        ),
        ExerciseCatalogEntity(
            id = "ex_incline_db_press",
            name = "Ferde pados nyomás kézisúlyzóval",
            muscleGroup = "Mell",
            equipment = "Kézisúlyzó + állítható pad",
            loadType = ExerciseLoadType.REPS_WEIGHT.name,
            weightSemantics = WeightSemantics.PER_HAND_DUMBBELL.name,
            description = "30 fokos padon kézisúlyzós nyomás; a megadott súly kézenként értendő.",
            demoSourceType = DemoSourceType.VERIFIED_VIDEO.name,
            demoUrl = "https://www.youtube.com/results?search_query=incline+dumbbell+press",
            isCustom = false,
            isFavorite = true
        ),
        ExerciseCatalogEntity(
            id = "ex_barbell_row",
            name = "Döntött törzsű evezés rúddal",
            muscleGroup = "Hát",
            equipment = "Rúd",
            loadType = ExerciseLoadType.REPS_WEIGHT.name,
            weightSemantics = WeightSemantics.TOTAL_BARBELL_OR_MACHINE.name,
            description = "Semleges gerinccel, csípőből előredőlve húzd a rudat az alhas irányába.",
            demoSourceType = DemoSourceType.VERIFIED_VIDEO.name,
            demoUrl = "https://www.youtube.com/results?search_query=barbell+row+technique",
            isCustom = false,
            isFavorite = false
        ),
        ExerciseCatalogEntity(
            id = "ex_lat_pulldown",
            name = "Lehúzás mellhez csigán",
            muscleGroup = "Hát",
            equipment = "Csigás torony",
            loadType = ExerciseLoadType.REPS_WEIGHT.name,
            weightSemantics = WeightSemantics.TOTAL_BARBELL_OR_MACHINE.name,
            description = "Vállszélesnél kissé szélesebb fogással húzd a rudat a felső mellkashoz.",
            demoSourceType = DemoSourceType.UNVERIFIED_SEARCH.name,
            demoUrl = "https://www.youtube.com/results?search_query=lat+pulldown",
            isCustom = false,
            isFavorite = false
        ),
        ExerciseCatalogEntity(
            id = "ex_squat",
            name = "Hátsó guggolás rúddal",
            muscleGroup = "Láb",
            equipment = "Rúd + erőkeret",
            loadType = ExerciseLoadType.REPS_WEIGHT.name,
            weightSemantics = WeightSemantics.TOTAL_BARBELL_OR_MACHINE.name,
            description = "Feszes törzzsel ereszkedj legalább vízszintes combhelyzetig, majd nyomd fel a súlyt.",
            demoSourceType = DemoSourceType.VERIFIED_VIDEO.name,
            demoUrl = "https://www.youtube.com/results?search_query=barbell+back+squat",
            isCustom = false,
            isFavorite = true
        ),
        ExerciseCatalogEntity(
            id = "ex_rdl",
            name = "Román felhúzás (RDL)",
            muscleGroup = "Láb",
            equipment = "Rúd vagy kézisúlyzó",
            loadType = ExerciseLoadType.REPS_WEIGHT.name,
            weightSemantics = WeightSemantics.TOTAL_BARBELL_OR_MACHINE.name,
            description = "Csípőhátratolás enyhén hajlított térddel, a combhajlító megnyúlásáig.",
            demoSourceType = DemoSourceType.VERIFIED_VIDEO.name,
            demoUrl = "https://www.youtube.com/results?search_query=romanian+deadlift",
            isCustom = false,
            isFavorite = false
        ),
        ExerciseCatalogEntity(
            id = "ex_ohp",
            name = "Vállból nyomás állva (OHP)",
            muscleGroup = "Váll",
            equipment = "Rúd vagy kézisúlyzó",
            loadType = ExerciseLoadType.REPS_WEIGHT.name,
            weightSemantics = WeightSemantics.TOTAL_BARBELL_OR_MACHINE.name,
            description = "Feszes farizommal és hasfallal nyomd a súlyt a fej fölé.",
            demoSourceType = DemoSourceType.VERIFIED_VIDEO.name,
            demoUrl = "https://www.youtube.com/results?search_query=overhead+press",
            isCustom = false,
            isFavorite = false
        ),
        ExerciseCatalogEntity(
            id = "ex_plank_unilateral",
            name = "Elülső Plank tartás (időalapú)",
            muscleGroup = "Törzs",
            equipment = "Saját testsúly",
            loadType = ExerciseLoadType.TIMED_UNILATERAL.name,
            weightSemantics = WeightSemantics.BODYWEIGHT_ONLY.name,
            description = "Egyoldali/közös időmérés: explicit pozitív Stop automatikusan teljesítettnek jelöli a sorozatot.",
            demoSourceType = DemoSourceType.VERIFIED_VIDEO.name,
            demoUrl = "https://www.youtube.com/results?search_query=plank+hold",
            isCustom = false,
            isFavorite = true
        ),
        ExerciseCatalogEntity(
            id = "ex_side_plank_bilateral",
            name = "Oldalsó Plank (Bal + Jobb kétoldali időmérés)",
            muscleGroup = "Törzs",
            equipment = "Saját testsúly",
            loadType = ExerciseLoadType.TIMED_BILATERAL.name,
            weightSemantics = WeightSemantics.BODYWEIGHT_ONLY.name,
            description = "Kétoldali időmérés: a sorozat csak akkor lesz automatikusan kész, ha a Bal ÉS a Jobb oldal is pozitív idővel áll meg.",
            demoSourceType = DemoSourceType.VERIFIED_VIDEO.name,
            demoUrl = "https://www.youtube.com/results?search_query=side+plank+left+right",
            isCustom = false,
            isFavorite = true
        ),
        ExerciseCatalogEntity(
            id = "ex_outdoor_run",
            name = "Kültéri futás / gyaloglás (GPS + idő/táv)",
            muscleGroup = "Kardió / Kültéri",
            equipment = "Kültéri / GPS",
            loadType = ExerciseLoadType.DISTANCE_TIME.name,
            weightSemantics = WeightSemantics.BODYWEIGHT_ONLY.name,
            description = "Mért idő és távolság opcionális privát GPS-útvonallal. Explicit Mérés befejezése rögzíti a sorozatot.",
            demoSourceType = DemoSourceType.UNVERIFIED_SEARCH.name,
            demoUrl = "https://www.youtube.com/results?search_query=running+pacing",
            isCustom = false,
            isFavorite = true
        ),
        ExerciseCatalogEntity(
            id = "ex_goblet_squat",
            name = "Goblet guggolás",
            muscleGroup = "Láb",
            equipment = "Kézisúlyzó",
            loadType = ExerciseLoadType.REPS_WEIGHT.name,
            weightSemantics = WeightSemantics.PER_HAND_DUMBBELL.name,
            description = "Mellkas előtt tartott kézisúlyzóval végzett mély guggolás egyenes törzzsel.",
            demoSourceType = DemoSourceType.VERIFIED_VIDEO.name,
            demoUrl = "https://www.youtube.com/results?search_query=goblet+squat+form",
            isCustom = false,
            isFavorite = true
        ),
        ExerciseCatalogEntity(
            id = "ex_floor_press",
            name = "Földön fekve nyomás",
            muscleGroup = "Mell",
            equipment = "Kézisúlyzó",
            loadType = ExerciseLoadType.REPS_WEIGHT.name,
            weightSemantics = WeightSemantics.PER_HAND_DUMBBELL.name,
            description = "Talajon hanyatt fekve végzett kézisúlyzós mellnyomás vállkímélő mozgástartományban.",
            demoSourceType = DemoSourceType.VERIFIED_VIDEO.name,
            demoUrl = "https://www.youtube.com/results?search_query=dumbbell+floor+press",
            isCustom = false,
            isFavorite = true
        ),
        ExerciseCatalogEntity(
            id = "ex_one_arm_row",
            name = "Egykezes evezés",
            muscleGroup = "Hát",
            equipment = "Kézisúlyzó",
            loadType = ExerciseLoadType.REPS_WEIGHT.name,
            weightSemantics = WeightSemantics.PER_HAND_DUMBBELL.name,
            description = "Támaszkodva végzett egykezes hátizom-evezés a csípő irányába.",
            demoSourceType = DemoSourceType.VERIFIED_VIDEO.name,
            demoUrl = "https://www.youtube.com/results?search_query=one+arm+dumbbell+row",
            isCustom = false,
            isFavorite = true
        ),
        ExerciseCatalogEntity(
            id = "ex_bulgarian_split_squat",
            name = "Bolgár guggolás",
            muscleGroup = "Láb",
            equipment = "Kézisúlyzó",
            loadType = ExerciseLoadType.REPS_WEIGHT.name,
            weightSemantics = WeightSemantics.PER_HAND_DUMBBELL.name,
            description = "Hátsó láb emelvényen, kontrollált egyoldali süllyedés kézisúlyzóval.",
            demoSourceType = DemoSourceType.VERIFIED_VIDEO.name,
            demoUrl = "https://www.youtube.com/results?search_query=bulgarian+split+squat",
            isCustom = false,
            isFavorite = true
        ),
        ExerciseCatalogEntity(
            id = "ex_pushup",
            name = "Fekvőtámasz",
            muscleGroup = "Mell",
            equipment = "Saját testsúly",
            loadType = ExerciseLoadType.BODYWEIGHT.name,
            weightSemantics = WeightSemantics.BODYWEIGHT_ONLY.name,
            description = "Klasszikus fekvőtámasz feszes törzzsel.",
            demoSourceType = DemoSourceType.VERIFIED_VIDEO.name,
            demoUrl = "https://www.youtube.com/results?search_query=push+up+form",
            isCustom = false,
            isFavorite = false
        ),
        ExerciseCatalogEntity(
            id = "ex_bicep_curl",
            name = "Bicepszezés",
            muscleGroup = "Kar",
            equipment = "Kézisúlyzó",
            loadType = ExerciseLoadType.REPS_WEIGHT.name,
            weightSemantics = WeightSemantics.PER_HAND_DUMBBELL.name,
            description = "Állva végzett karhajlítás kézisúlyzóval, könyök a törzs mellett rögzítve.",
            demoSourceType = DemoSourceType.VERIFIED_VIDEO.name,
            demoUrl = "https://www.youtube.com/results?search_query=dumbbell+bicep+curl",
            isCustom = false,
            isFavorite = false
        ),
        ExerciseCatalogEntity(
            id = "ex_reverse_lunge",
            name = "Hátralépős kitörés",
            muscleGroup = "Láb",
            equipment = "Kézisúlyzó",
            loadType = ExerciseLoadType.REPS_WEIGHT.name,
            weightSemantics = WeightSemantics.PER_HAND_DUMBBELL.name,
            description = "Térdkímélő hátralépős kitörés kézisúlyzóval.",
            demoSourceType = DemoSourceType.VERIFIED_VIDEO.name,
            demoUrl = "https://www.youtube.com/results?search_query=dumbbell+reverse+lunge",
            isCustom = false,
            isFavorite = false
        )
    )

    fun builtInPrograms(): Triple<List<ProgramEntity>, List<ProgramDayEntity>, List<ProgramExerciseEntity>> {
        val programs = listOf(
            ProgramEntity(
                id = "prog_upper_lower",
                name = "Otthoni A/B – Alap (személyre szabott)",
                description = "Választott felszerelés • Kezdő • 2 napos ciklus",
                isBuiltIn = true,
                isActive = true,
                trainingStyle = "NORMAL",
                progressionType = "RIR_COACH",
                loadIncrementKg = 2.5
            ),
            ProgramEntity(
                id = "prog_home_ab_lvl2",
                name = "Otthoni A/B – 2. szint",
                description = "Otthon • Középhaladó • 2 napos ciklus • 7/7 gyakorlat",
                isBuiltIn = true,
                isActive = false,
                trainingStyle = "NORMAL",
                progressionType = "RIR_COACH",
                loadIncrementKg = 2.5
            ),
            ProgramEntity(
                id = "prog_home_varied",
                name = "Otthoni változatos",
                description = "Otthon • Középhaladó • 2 napos ciklus • 6/6 gyakorlat",
                isBuiltIn = true,
                isActive = false,
                trainingStyle = "NORMAL",
                progressionType = "DOUBLE",
                loadIncrementKg = 2.0
            ),
            ProgramEntity(
                id = "prog_fullbody_hit",
                name = "Konditermi Full Body (HIT)",
                description = "Konditerem • Haladó • Rövid, magas intenzitású teljes testes program",
                isBuiltIn = true,
                isActive = false,
                trainingStyle = "HIT",
                progressionType = "DOUBLE",
                loadIncrementKg = 2.5
            )
        )

        val days = listOf(
            ProgramDayEntity(
                id = "day_ul_upper_a",
                programId = "prog_upper_lower",
                dayOrder = 1,
                title = "Alap A",
                focusAreas = "Láb / far • Mell • Hát • Váll • Kar • Törzs"
            ),
            ProgramDayEntity(
                id = "day_ul_lower_b",
                programId = "prog_upper_lower",
                dayOrder = 2,
                title = "Alap B",
                focusAreas = "Láb • Hát • Mell • Oldalsó törzs"
            ),
            ProgramDayEntity(
                id = "day_ul_outdoor_c",
                programId = "prog_upper_lower",
                dayOrder = 3,
                title = "C nap — Kardió / Kültéri állóképesség",
                focusAreas = "Kardió / Kültéri, Törzs"
            ),
            ProgramDayEntity(
                id = "day_lvl2_a",
                programId = "prog_home_ab_lvl2",
                dayOrder = 1,
                title = "A nap",
                focusAreas = "Mell, Hát, Láb, Váll"
            ),
            ProgramDayEntity(
                id = "day_lvl2_b",
                programId = "prog_home_ab_lvl2",
                dayOrder = 2,
                title = "B nap",
                focusAreas = "Láb, Hát, Kar, Törzs"
            ),
            ProgramDayEntity(
                id = "day_varied_a",
                programId = "prog_home_varied",
                dayOrder = 1,
                title = "A nap",
                focusAreas = "Teljes test A"
            ),
            ProgramDayEntity(
                id = "day_hit_a",
                programId = "prog_fullbody_hit",
                dayOrder = 1,
                title = "HIT Teljes Test A",
                focusAreas = "Mell, Hát, Láb"
            )
        )

        val exercises = listOf(
            ProgramExerciseEntity("pe_1", "prog_upper_lower", "day_ul_upper_a", "ex_goblet_squat", 1, 3, 8, 20, 5.5, 90),
            ProgramExerciseEntity("pe_2", "prog_upper_lower", "day_ul_upper_a", "ex_floor_press", 2, 2, 8, 15, 5.5, 90),
            ProgramExerciseEntity("pe_3", "prog_upper_lower", "day_ul_upper_a", "ex_one_arm_row", 3, 2, 8, 15, 5.5, 90),
            ProgramExerciseEntity("pe_4", "prog_upper_lower", "day_ul_upper_a", "ex_rdl", 4, 2, 8, 15, 5.5, 90),
            ProgramExerciseEntity("pe_4b", "prog_upper_lower", "day_ul_upper_a", "ex_ohp", 5, 2, 8, 15, 5.5, 90),
            ProgramExerciseEntity("pe_4c", "prog_upper_lower", "day_ul_upper_a", "ex_bicep_curl", 6, 2, 8, 15, 5.5, 60),
            ProgramExerciseEntity("pe_4d", "prog_upper_lower", "day_ul_upper_a", "ex_plank_unilateral", 7, 2, 1, 1, 0.0, 60),

            ProgramExerciseEntity("pe_5", "prog_upper_lower", "day_ul_lower_b", "ex_bulgarian_split_squat", 1, 3, 8, 12, 5.5, 90),
            ProgramExerciseEntity("pe_6", "prog_upper_lower", "day_ul_lower_b", "ex_barbell_row", 2, 2, 8, 15, 11.0, 90),
            ProgramExerciseEntity("pe_6b", "prog_upper_lower", "day_ul_lower_b", "ex_pushup", 3, 2, 8, 15, 0.0, 90),
            ProgramExerciseEntity("pe_7", "prog_upper_lower", "day_ul_lower_b", "ex_side_plank_bilateral", 4, 2, 1, 1, 0.0, 60),

            ProgramExerciseEntity("pe_8", "prog_upper_lower", "day_ul_outdoor_c", "ex_outdoor_run", 1, 1, 1, 1, 0.0, 60),
            ProgramExerciseEntity("pe_9", "prog_upper_lower", "day_ul_outdoor_c", "ex_plank_unilateral", 2, 2, 1, 1, 0.0, 60),

            ProgramExerciseEntity("pe_lvl2_1", "prog_home_ab_lvl2", "day_lvl2_a", "ex_incline_db_press", 1, 3, 8, 12, 10.0, 90),
            ProgramExerciseEntity("pe_lvl2_2", "prog_home_ab_lvl2", "day_lvl2_a", "ex_one_arm_row", 2, 3, 8, 12, 10.0, 90),
            ProgramExerciseEntity("pe_lvl2_3", "prog_home_ab_lvl2", "day_lvl2_b", "ex_bulgarian_split_squat", 1, 3, 8, 12, 8.0, 90),

            ProgramExerciseEntity("pe_var_1", "prog_home_varied", "day_varied_a", "ex_goblet_squat", 1, 3, 10, 15, 8.0, 90),
            ProgramExerciseEntity("pe_var_2", "prog_home_varied", "day_varied_a", "ex_floor_press", 2, 3, 10, 15, 8.0, 90),

            ProgramExerciseEntity("pe_10", "prog_fullbody_hit", "day_hit_a", "ex_bench_press", 1, 2, 6, 8, 60.0, 150),
            ProgramExerciseEntity("pe_11", "prog_fullbody_hit", "day_hit_a", "ex_lat_pulldown", 2, 2, 8, 10, 65.0, 120),
            ProgramExerciseEntity("pe_12", "prog_fullbody_hit", "day_hit_a", "ex_squat", 3, 2, 8, 10, 85.0, 180)
        )

        return Triple(programs, days, exercises)
    }

    /**
     * Generates a previewable Personal Home A/B program from the user's TrainingProfile (A09).
     * Respects avoidedAreas and excludedExerciseIds from the profile!
     */
    fun generatePersonalHomeAbPreview(profile: TrainingProfile): Triple<ProgramEntity, List<ProgramDayEntity>, List<ProgramExerciseEntity>> {
        val progId = "prog_personal_home_ab"
        val avoidedSummary = if (profile.avoidedAreas.isNotEmpty()) {
            " • Kerülve: ${profile.avoidedAreas.joinToString(", ")}"
        } else ""
        val prog = ProgramEntity(
            id = progId,
            name = "Otthoni A/B – Alap (személyre szabott)",
            description = "${profile.equipment} • ${profile.experience} • 2 napos ciklus • ${profile.sessionMinutes} perc$avoidedSummary",
            isBuiltIn = false,
            isActive = true,
            trainingStyle = "NORMAL",
            progressionType = "RIR_COACH",
            loadIncrementKg = 2.0
        )
        val dayA = ProgramDayEntity(
            id = "day_home_a",
            programId = progId,
            dayOrder = 1,
            title = "Alap A",
            focusAreas = "Láb / far • Mell • Hát • Váll • Kar"
        )
        val dayB = ProgramDayEntity(
            id = "day_home_b",
            programId = progId,
            dayOrder = 2,
            title = "Alap B",
            focusAreas = "Láb • Hát • Mell • Törzs"
        )

        val candidateDayA = listOf(
            Triple("ex_goblet_squat", "Láb", 5.5),
            Triple("ex_floor_press", "Mell", 5.5),
            Triple("ex_one_arm_row", "Hát", 5.5),
            Triple("ex_rdl", "Láb", 5.5),
            Triple("ex_ohp", "Váll", 5.5),
            Triple("ex_bicep_curl", "Kar", 5.5),
            Triple("ex_plank_unilateral", "Törzs", 0.0)
        ).filter { (exId, area, _) ->
            exId !in profile.excludedExerciseIds && area !in profile.avoidedAreas
        }.ifEmpty {
            listOf(Triple("ex_plank_unilateral", "Törzs", 0.0))
        }

        val candidateDayB = listOf(
            Triple("ex_bulgarian_split_squat", "Láb", 5.5),
            Triple("ex_barbell_row", "Hát", 11.0),
            Triple("ex_pushup", "Mell", 0.0),
            Triple("ex_reverse_lunge", "Láb", 5.5),
            Triple("ex_incline_db_press", "Mell", 8.0),
            Triple("ex_side_plank_bilateral", "Törzs", 0.0)
        ).filter { (exId, area, _) ->
            exId !in profile.excludedExerciseIds && area !in profile.avoidedAreas
        }.ifEmpty {
            listOf(Triple("ex_side_plank_bilateral", "Törzs", 0.0))
        }

        val exsA = candidateDayA.mapIndexed { idx, (exId, _, kg) ->
            ProgramExerciseEntity(
                id = "pe_home_a_${idx + 1}",
                programId = progId,
                programDayId = dayA.id,
                exerciseId = exId,
                sortOrder = idx + 1,
                targetSets = if (idx == 0) 3 else 2,
                targetRepsMin = 8,
                targetRepsMax = 15,
                defaultWeightKg = kg,
                restSeconds = 90
            )
        }
        val exsB = candidateDayB.mapIndexed { idx, (exId, _, kg) ->
            ProgramExerciseEntity(
                id = "pe_home_b_${idx + 1}",
                programId = progId,
                programDayId = dayB.id,
                exerciseId = exId,
                sortOrder = idx + 1,
                targetSets = if (idx == 0) 3 else 2,
                targetRepsMin = 8,
                targetRepsMax = 12,
                defaultWeightKg = kg,
                restSeconds = 90
            )
        }

        return Triple(prog, listOf(dayA, dayB), exsA + exsB)
    }

    suspend fun ensureSeeded(dao: TrainPilotDao) {
        if (dao.getExerciseCatalogNow().isEmpty()) {
            dao.upsertExercises(builtInExercises())
        }
        if (dao.getProgramsNow().isEmpty()) {
            val (progs, days, exs) = builtInPrograms()
            progs.forEach { dao.upsertProgram(it) }
            dao.upsertProgramDays(days)
            dao.upsertProgramExercises(exs)
        }
        if (dao.getCalendarSchedulesNow().isEmpty()) {
            val today = LocalDate.now().toString()
            val month = today.substring(0, 7)
            dao.upsertCalendarSchedule(
                CalendarScheduleEntity(
                    id = "sched_seed_today",
                    dateIso = today,
                    monthIso = month,
                    timeHm = "17:30",
                    programId = "prog_upper_lower",
                    programDayId = "day_ul_upper_a",
                    title = "Felső / Alsó Erő & Hypertrophia (A/B) — A nap — Felsőtest Erő + Törzs",
                    status = "PLANNED"
                )
            )
        }
    }
}

package com.repforge.app.nativev2.data.db

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "programs")
data class ProgramEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val isBuiltIn: Boolean,
    val isActive: Boolean,
    val trainingStyle: String = "NORMAL", // NORMAL, HIT
    val progressionType: String = "RIR_COACH", // DOUBLE, LINEAR, RIR_COACH
    val loadIncrementKg: Double = 2.5
)

@Entity(tableName = "program_days")
data class ProgramDayEntity(
    @PrimaryKey val id: String,
    val programId: String,
    val dayOrder: Int,
    val title: String,
    val focusAreas: String
)

@Entity(tableName = "program_exercises")
data class ProgramExerciseEntity(
    @PrimaryKey val id: String,
    val programId: String,
    val programDayId: String,
    val exerciseId: String,
    val sortOrder: Int,
    val targetSets: Int,
    val targetRepsMin: Int,
    val targetRepsMax: Int,
    val defaultWeightKg: Double,
    val restSeconds: Int
)

@Entity(tableName = "exercise_catalog")
data class ExerciseCatalogEntity(
    @PrimaryKey val id: String,
    val name: String,
    val muscleGroup: String,
    val equipment: String,
    val loadType: String, // REPS_WEIGHT, TIMED_UNILATERAL, TIMED_BILATERAL, DISTANCE_TIME, BODYWEIGHT
    val weightSemantics: String, // TOTAL_BARBELL_OR_MACHINE, PER_HAND_DUMBBELL, BODYWEIGHT_ONLY
    val description: String,
    val demoSourceType: String, // VERIFIED_VIDEO, UNVERIFIED_SEARCH
    val demoUrl: String,
    val isCustom: Boolean = false,
    val isFavorite: Boolean = false
)

@Entity(tableName = "workout_history")
data class WorkoutHistoryEntity(
    @PrimaryKey val id: String,
    val title: String,
    val programId: String?,
    val programDayId: String?,
    val scheduleId: String?,
    val startedAtEpochMs: Long,
    val finishedAtEpochMs: Long,
    val durationSeconds: Int,
    val dateIso: String, // YYYY-MM-DD
    val monthIso: String, // YYYY-MM
    val effortCode: Int, // 1..6
    val hadPain: Boolean,
    val notes: String,
    val totalVolumeKg: Double,
    val prSummaryJson: String, // List<PrBadgeRecord> serialized
    val sourceOwner: String, // PHONE or WATCH
    val hasPrivateRoute: Boolean = false
)

@Entity(tableName = "workout_history_sets")
data class WorkoutHistorySetEntity(
    @PrimaryKey val id: String,
    val workoutId: String,
    val exerciseId: String,
    val exerciseName: String,
    val muscleGroup: String,
    val loadType: String,
    val setNumber: Int,
    val weightKg: Double,
    val reps: Int,
    val rir: Int?, // nullable 0..10
    val leftSeconds: Int,
    val rightSeconds: Int,
    val distanceMeters: Double,
    val durationSeconds: Int,
    val completedAtEpochMs: Long
)

@Entity(tableName = "calendar_schedules")
data class CalendarScheduleEntity(
    @PrimaryKey val id: String,
    val dateIso: String, // YYYY-MM-DD
    val monthIso: String, // YYYY-MM
    val timeHm: String, // HH:mm
    val programId: String?,
    val programDayId: String?,
    val title: String,
    val targetDistanceMeters: Double? = null,
    val status: String = "PLANNED", // PLANNED, COMPLETED, CANCELLED
    val linkedWorkoutId: String? = null
)

@Entity(tableName = "health_daily_metrics", primaryKeys = ["dateIso", "metricKey", "source"])
data class HealthDailyMetricEntity(
    val dateIso: String,
    val metricKey: String, // D04.01 .. D04.26 keys
    val valueNumber: Double,
    val valueText: String,
    val unit: String,
    val source: String, // Health Connect, Samsung Health, TrainPilot Wear, Kézi napló
    val recordedAtHm: String
)

@Entity(tableName = "weight_entries")
data class WeightEntryEntity(
    @PrimaryKey val id: String,
    val dateIso: String,
    val weightKg: Double,
    val bodyFatPercent: Double?,
    val note: String,
    val source: String = "Kézi napló",
    val timestampMs: Long
)

@Entity(tableName = "workout_routes")
data class WorkoutRouteEntity(
    @PrimaryKey val workoutId: String,
    val routePointsJson: String, // List<RoutePointModel>
    val totalDistanceMeters: Double,
    val pointCount: Int,
    val hasWatchSegments: Boolean,
    val hasPhoneSegments: Boolean
)

@Entity(tableName = "workout_photos")
data class WorkoutPhotoEntity(
    @PrimaryKey val id: String,
    val workoutId: String?,
    val dateIso: String,
    val caption: String,
    val localUriOrTag: String,
    val createdAtMs: Long
)

@Entity(tableName = "kv_preferences")
data class KeyValuePrefEntity(
    @PrimaryKey val prefKey: String,
    val prefValue: String
)

@Dao
interface TrainPilotDao {
    @Query("SELECT * FROM programs ORDER BY isActive DESC, isBuiltIn DESC, name ASC")
    fun observePrograms(): Flow<List<ProgramEntity>>

    @Query("SELECT * FROM programs ORDER BY isActive DESC, isBuiltIn DESC, name ASC")
    suspend fun getProgramsNow(): List<ProgramEntity>

    @Query("SELECT * FROM program_days ORDER BY programId, dayOrder ASC")
    fun observeProgramDays(): Flow<List<ProgramDayEntity>>

    @Query("SELECT * FROM program_days WHERE programId = :programId ORDER BY dayOrder ASC")
    suspend fun getProgramDaysForProgram(programId: String): List<ProgramDayEntity>

    @Query("SELECT * FROM program_exercises ORDER BY programDayId, sortOrder ASC")
    fun observeProgramExercises(): Flow<List<ProgramExerciseEntity>>

    @Query("SELECT * FROM program_exercises WHERE programDayId = :dayId ORDER BY sortOrder ASC")
    suspend fun getProgramExercisesForDay(dayId: String): List<ProgramExerciseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProgram(program: ProgramEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProgramDays(days: List<ProgramDayEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProgramExercises(exercises: List<ProgramExerciseEntity>)

    @Query("DELETE FROM programs WHERE id = :programId AND isBuiltIn = 0")
    suspend fun deleteCustomProgram(programId: String)

    @Query("DELETE FROM program_days WHERE programId = :programId")
    suspend fun deleteProgramDays(programId: String)

    @Query("DELETE FROM program_exercises WHERE programId = :programId")
    suspend fun deleteProgramExercises(programId: String)

    @Transaction
    suspend fun activateProgramExclusive(programId: String) {
        val all = getProgramsNow()
        all.forEach { p ->
            upsertProgram(p.copy(isActive = (p.id == programId)))
        }
    }

    @Query("SELECT * FROM exercise_catalog ORDER BY isFavorite DESC, name ASC")
    fun observeExerciseCatalog(): Flow<List<ExerciseCatalogEntity>>

    @Query("SELECT * FROM exercise_catalog ORDER BY isFavorite DESC, name ASC")
    suspend fun getExerciseCatalogNow(): List<ExerciseCatalogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertExercises(items: List<ExerciseCatalogEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertExercise(item: ExerciseCatalogEntity)

    @Query("DELETE FROM exercise_catalog WHERE id = :id AND isCustom = 1")
    suspend fun deleteCustomExercise(id: String)

    @Query("SELECT * FROM workout_history ORDER BY finishedAtEpochMs DESC")
    fun observeWorkoutHistory(): Flow<List<WorkoutHistoryEntity>>

    @Query("SELECT * FROM workout_history ORDER BY finishedAtEpochMs DESC")
    suspend fun getWorkoutHistoryNow(): List<WorkoutHistoryEntity>

    @Query("SELECT * FROM workout_history_sets ORDER BY completedAtEpochMs DESC, setNumber ASC")
    fun observeWorkoutHistorySets(): Flow<List<WorkoutHistorySetEntity>>

    @Query("SELECT * FROM workout_history_sets ORDER BY completedAtEpochMs DESC, setNumber ASC")
    suspend fun getAllHistorySetsNow(): List<WorkoutHistorySetEntity>

    @Query("SELECT * FROM workout_history_sets WHERE workoutId = :workoutId ORDER BY setNumber ASC")
    suspend fun getSetsForWorkoutNow(workoutId: String): List<WorkoutHistorySetEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertWorkoutHistory(workout: WorkoutHistoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertWorkoutHistorySets(sets: List<WorkoutHistorySetEntity>)

    @Query("DELETE FROM workout_history WHERE id = :workoutId")
    suspend fun deleteWorkoutHistory(workoutId: String)

    @Query("DELETE FROM workout_history_sets WHERE workoutId = :workoutId")
    suspend fun deleteWorkoutHistorySets(workoutId: String)

    @Query("SELECT * FROM calendar_schedules ORDER BY dateIso ASC, timeHm ASC")
    fun observeCalendarSchedules(): Flow<List<CalendarScheduleEntity>>

    @Query("SELECT * FROM calendar_schedules ORDER BY dateIso ASC, timeHm ASC")
    suspend fun getCalendarSchedulesNow(): List<CalendarScheduleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCalendarSchedules(items: List<CalendarScheduleEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCalendarSchedule(item: CalendarScheduleEntity)

    @Query("SELECT * FROM health_daily_metrics ORDER BY dateIso DESC, metricKey ASC")
    fun observeHealthMetrics(): Flow<List<HealthDailyMetricEntity>>

    @Query("SELECT * FROM health_daily_metrics ORDER BY dateIso DESC, metricKey ASC")
    suspend fun getHealthMetricsNow(): List<HealthDailyMetricEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertHealthMetrics(items: List<HealthDailyMetricEntity>)

    @Query("SELECT * FROM weight_entries ORDER BY dateIso DESC, timestampMs DESC")
    fun observeWeightEntries(): Flow<List<WeightEntryEntity>>

    @Query("SELECT * FROM weight_entries ORDER BY dateIso DESC, timestampMs DESC")
    suspend fun getWeightEntriesNow(): List<WeightEntryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertWeightEntry(entry: WeightEntryEntity)

    @Query("DELETE FROM weight_entries WHERE id = :id")
    suspend fun deleteWeightEntry(id: String)

    @Query("SELECT * FROM workout_routes")
    fun observeWorkoutRoutes(): Flow<List<WorkoutRouteEntity>>

    @Query("SELECT * FROM workout_routes WHERE workoutId = :workoutId LIMIT 1")
    suspend fun getWorkoutRouteNow(workoutId: String): WorkoutRouteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertWorkoutRoute(route: WorkoutRouteEntity)

    @Query("SELECT * FROM workout_photos ORDER BY createdAtMs DESC")
    fun observeWorkoutPhotos(): Flow<List<WorkoutPhotoEntity>>

    @Query("SELECT * FROM workout_photos ORDER BY createdAtMs DESC")
    suspend fun getWorkoutPhotosNow(): List<WorkoutPhotoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertWorkoutPhoto(photo: WorkoutPhotoEntity)

    @Query("DELETE FROM workout_photos WHERE id = :photoId")
    suspend fun deleteWorkoutPhoto(photoId: String)

    @Query("SELECT * FROM kv_preferences")
    fun observePreferences(): Flow<List<KeyValuePrefEntity>>

    @Query("SELECT prefValue FROM kv_preferences WHERE prefKey = :key LIMIT 1")
    suspend fun getPrefNow(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setPref(entity: KeyValuePrefEntity)

    @Query("DELETE FROM kv_preferences WHERE prefKey = :key")
    suspend fun deletePref(key: String)

    @Transaction
    suspend fun eraseAllUserDataAndReseed() {
        val customs = getProgramsNow().filter { !it.isBuiltIn }
        customs.forEach {
            deleteCustomProgram(it.id)
            deleteProgramDays(it.id)
            deleteProgramExercises(it.id)
        }
        getWorkoutHistoryNow().forEach {
            deleteWorkoutHistorySets(it.id)
            deleteWorkoutHistory(it.id)
        }
    }
}

@Database(
    entities = [
        ProgramEntity::class,
        ProgramDayEntity::class,
        ProgramExerciseEntity::class,
        ExerciseCatalogEntity::class,
        WorkoutHistoryEntity::class,
        WorkoutHistorySetEntity::class,
        CalendarScheduleEntity::class,
        HealthDailyMetricEntity::class,
        WeightEntryEntity::class,
        WorkoutRouteEntity::class,
        WorkoutPhotoEntity::class,
        KeyValuePrefEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class TrainPilotDatabase : RoomDatabase() {
    abstract fun dao(): TrainPilotDao

    companion object {
        @Volatile
        private var INSTANCE: TrainPilotDatabase? = null

        fun getInstance(context: Context): TrainPilotDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    TrainPilotDatabase::class.java,
                    "trainpilot_native_v2.db"
                ).fallbackToDestructiveMigration().build().also { INSTANCE = it }
            }
        }
    }
}

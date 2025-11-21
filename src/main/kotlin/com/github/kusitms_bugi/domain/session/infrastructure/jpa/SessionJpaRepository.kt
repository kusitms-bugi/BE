package com.github.kusitms_bugi.domain.session.infrastructure.jpa

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime
import java.util.*

interface SessionJpaRepository : JpaRepository<Session, UUID> {

    @Query("SELECT DISTINCT s FROM Session s LEFT JOIN FETCH s.statusHistory LEFT JOIN FETCH s.metrics WHERE s.id = :id")
    override fun findById(@Param("id") id: UUID): Optional<Session>

    @Query(
        value = """
            SELECT
                TO_CHAR(s.created_at, 'YYYY-MM-DD') as date,
                COALESCE(ROUND(AVG(s.score)), 0) as avg_score
            FROM session s
            WHERE s.user_id = :userId
              AND s.created_at BETWEEN :startDate AND :endDate
              AND s.deleted_at IS NULL
            GROUP BY TO_CHAR(s.created_at, 'YYYY-MM-DD')
            ORDER BY date
        """,
        nativeQuery = true
    )
    fun getAverageScoresByDate(
        @Param("userId") userId: UUID,
        @Param("startDate") startDate: LocalDateTime,
        @Param("endDate") endDate: LocalDateTime
    ): List<Array<Any>>

    @Query(
        value = """
            WITH active_ranges AS (
                SELECT
                    s.id as session_id,
                    ss1.timestamp as start_time,
                    ss2.timestamp as end_time
                FROM session s
                CROSS JOIN LATERAL (
                    SELECT timestamp, status,
                           ROW_NUMBER() OVER (PARTITION BY session_id ORDER BY timestamp) as rn
                    FROM session_status
                    WHERE session_id = s.id
                      AND status IN ('STARTED', 'RESUMED')
                      AND deleted_at IS NULL
                ) ss1
                CROSS JOIN LATERAL (
                    SELECT timestamp, status,
                           ROW_NUMBER() OVER (PARTITION BY session_id ORDER BY timestamp) as rn
                    FROM session_status
                    WHERE session_id = s.id
                      AND status IN ('PAUSED', 'STOPPED')
                      AND deleted_at IS NULL
                      AND timestamp > ss1.timestamp
                    ORDER BY timestamp
                    LIMIT 1
                ) ss2
                WHERE s.user_id = :userId
                  AND s.deleted_at IS NULL
                  AND ss2.rn >= ss1.rn
            ),
            active_metrics AS (
                SELECT
                    sm.session_id,
                    sm.score,
                    sm.timestamp,
                    LEAD(sm.timestamp) OVER (PARTITION BY sm.session_id ORDER BY sm.timestamp) as next_timestamp
                FROM session_metric sm
                WHERE sm.session_id IN (SELECT session_id FROM active_ranges)
                  AND sm.deleted_at IS NULL
                  AND EXISTS (
                      SELECT 1 FROM active_ranges ar
                      WHERE ar.session_id = sm.session_id
                        AND sm.timestamp >= ar.start_time
                        AND sm.timestamp <= ar.end_time
                  )
            )
            SELECT
                LEAST(GREATEST(score, 1), 6) as level,
                COALESCE(SUM(
                    CASE
                        WHEN next_timestamp IS NOT NULL THEN
                            EXTRACT(EPOCH FROM (next_timestamp - timestamp)) * 1000
                        ELSE 0
                    END
                ), 0) as duration_millis
            FROM active_metrics
            WHERE next_timestamp IS NOT NULL
            GROUP BY LEAST(GREATEST(score, 1), 6)
            ORDER BY level
        """,
        nativeQuery = true
    )
    fun calculateLevelDurationsForUser(@Param("userId") userId: UUID): List<Array<Any>>

    @Query(
        value = """
            WITH active_ranges AS (
                SELECT
                    s.id as session_id,
                    ss1.timestamp as start_time,
                    ss2.timestamp as end_time
                FROM session s
                CROSS JOIN LATERAL (
                    SELECT timestamp,
                           ROW_NUMBER() OVER (PARTITION BY session_id ORDER BY timestamp) as rn
                    FROM session_status
                    WHERE session_id = s.id
                      AND status IN ('STARTED', 'RESUMED')
                      AND deleted_at IS NULL
                ) ss1
                CROSS JOIN LATERAL (
                    SELECT timestamp,
                           ROW_NUMBER() OVER (PARTITION BY session_id ORDER BY timestamp) as rn
                    FROM session_status
                    WHERE session_id = s.id
                      AND status IN ('PAUSED', 'STOPPED')
                      AND deleted_at IS NULL
                      AND timestamp > ss1.timestamp
                    ORDER BY timestamp
                    LIMIT 1
                ) ss2
                WHERE s.user_id = :userId
                  AND s.created_at BETWEEN :startDate AND :endDate
                  AND s.deleted_at IS NULL
                  AND ss2.rn >= ss1.rn
            ),
            session_bad_durations AS (
                SELECT
                    ar.session_id,
                    SUM(
                        CASE
                            WHEN sm.score >= 4 AND next_sm.timestamp IS NOT NULL THEN
                                EXTRACT(EPOCH FROM (next_sm.timestamp - sm.timestamp)) * 1000
                            ELSE 0
                        END
                    ) as bad_duration_millis
                FROM active_ranges ar
                JOIN session_metric sm ON sm.session_id = ar.session_id
                LEFT JOIN LATERAL (
                    SELECT timestamp
                    FROM session_metric
                    WHERE session_id = ar.session_id
                      AND timestamp > sm.timestamp
                      AND deleted_at IS NULL
                    ORDER BY timestamp
                    LIMIT 1
                ) next_sm ON true
                WHERE sm.deleted_at IS NULL
                  AND sm.timestamp >= ar.start_time
                  AND sm.timestamp <= ar.end_time
                GROUP BY ar.session_id
            )
            SELECT
                COALESCE(ROUND(AVG(bad_duration_millis / 1000.0)), 0) as avg_bad_seconds_per_session
            FROM session_bad_durations
            WHERE bad_duration_millis > 0
        """,
        nativeQuery = true
    )
    fun getHighlightStats(
        @Param("userId") userId: UUID,
        @Param("startDate") startDate: LocalDateTime,
        @Param("endDate") endDate: LocalDateTime
    ): Int?

    @Query(
        value = """
            WITH active_ranges AS (
                SELECT
                    s.id as session_id,
                    s.score as session_score,
                    ss1.timestamp as start_time,
                    ss2.timestamp as end_time
                FROM session s
                CROSS JOIN LATERAL (
                    SELECT timestamp,
                           ROW_NUMBER() OVER (PARTITION BY session_id ORDER BY timestamp) as rn
                    FROM session_status
                    WHERE session_id = s.id
                      AND status IN ('STARTED', 'RESUMED')
                      AND deleted_at IS NULL
                ) ss1
                CROSS JOIN LATERAL (
                    SELECT timestamp,
                           ROW_NUMBER() OVER (PARTITION BY session_id ORDER BY timestamp) as rn
                    FROM session_status
                    WHERE session_id = s.id
                      AND status IN ('PAUSED', 'STOPPED')
                      AND deleted_at IS NULL
                      AND timestamp > ss1.timestamp
                    ORDER BY timestamp
                    LIMIT 1
                ) ss2
                WHERE s.user_id = :userId
                  AND s.created_at > :createdAt
                  AND s.score IS NOT NULL
                  AND s.deleted_at IS NULL
                  AND ss2.rn >= ss1.rn
            ),
            session_level_durations AS (
                SELECT
                    ar.session_id,
                    ar.session_score,
                    SUM(
                        CASE
                            WHEN LEAST(GREATEST(sm.score, 1), 6) IN (1, 2) AND next_sm.timestamp IS NOT NULL THEN
                                EXTRACT(EPOCH FROM (next_sm.timestamp - sm.timestamp)) * 1000
                            ELSE 0
                        END
                    ) as good_duration_millis,
                    SUM(
                        CASE
                            WHEN next_sm.timestamp IS NOT NULL THEN
                                EXTRACT(EPOCH FROM (next_sm.timestamp - sm.timestamp)) * 1000
                            ELSE 0
                        END
                    ) as total_duration_millis
                FROM active_ranges ar
                JOIN session_metric sm ON sm.session_id = ar.session_id
                LEFT JOIN LATERAL (
                    SELECT timestamp
                    FROM session_metric
                    WHERE session_id = ar.session_id
                      AND timestamp > sm.timestamp
                      AND deleted_at IS NULL
                    ORDER BY timestamp
                    LIMIT 1
                ) next_sm ON true
                WHERE sm.deleted_at IS NULL
                  AND sm.timestamp >= ar.start_time
                  AND sm.timestamp <= ar.end_time
                GROUP BY ar.session_id, ar.session_score
            ),
            composite_scores AS (
                SELECT
                    CASE
                        WHEN total_duration_millis > 0 THEN
                            (session_score * 0.65) + ((good_duration_millis / total_duration_millis) * 100.0 * 0.35)
                        ELSE
                            session_score * 0.65 + 50.0 * 0.35
                    END as composite_score
                FROM session_level_durations
            )
            SELECT COALESCE(ROUND(AVG(composite_score)), 50) as avg_composite_score
            FROM composite_scores
        """,
        nativeQuery = true
    )
    fun getAverageCompositeScore(
        @Param("userId") userId: UUID,
        @Param("createdAt") createdAt: LocalDateTime
    ): Int?

    @Query(
        value = """
            WITH active_ranges AS (
                SELECT
                    s.id as session_id,
                    s.created_at,
                    ss1.timestamp as start_time,
                    ss2.timestamp as end_time
                FROM session s
                CROSS JOIN LATERAL (
                    SELECT timestamp,
                           ROW_NUMBER() OVER (PARTITION BY session_id ORDER BY timestamp) as rn
                    FROM session_status
                    WHERE session_id = s.id
                      AND status IN ('STARTED', 'RESUMED')
                      AND deleted_at IS NULL
                ) ss1
                CROSS JOIN LATERAL (
                    SELECT timestamp,
                           ROW_NUMBER() OVER (PARTITION BY session_id ORDER BY timestamp) as rn
                    FROM session_status
                    WHERE session_id = s.id
                      AND status IN ('PAUSED', 'STOPPED')
                      AND deleted_at IS NULL
                      AND timestamp > ss1.timestamp
                    ORDER BY timestamp
                    LIMIT 1
                ) ss2
                WHERE s.user_id = :userId
                  AND s.created_at BETWEEN :startDate AND :endDate
                  AND s.deleted_at IS NULL
                  AND ss2.rn >= ss1.rn
            ),
            active_metrics_with_recovery AS (
                SELECT
                    ar.session_id,
                    ar.created_at,
                    sm.score,
                    sm.timestamp,
                    EXTRACT(HOUR FROM sm.timestamp) as hour_of_day,
                    EXTRACT(DOW FROM ar.created_at) as day_of_week,
                    LEAD(sm.timestamp) OVER (PARTITION BY ar.session_id ORDER BY sm.timestamp) as next_timestamp,
                    LEAD(sm.score) OVER (PARTITION BY ar.session_id ORDER BY sm.timestamp) as next_score,
                    CASE
                        WHEN sm.score >= 4 AND
                             LAG(sm.score) OVER (PARTITION BY ar.session_id ORDER BY sm.timestamp) < 4
                        THEN sm.timestamp
                        ELSE NULL
                    END as bad_posture_start
                FROM active_ranges ar
                JOIN session_metric sm ON sm.session_id = ar.session_id
                WHERE sm.deleted_at IS NULL
                  AND sm.timestamp >= ar.start_time
                  AND sm.timestamp <= ar.end_time
            )
            SELECT
                CAST(hour_of_day AS INTEGER) as hour_of_day,
                CAST(day_of_week AS INTEGER) as day_of_week,
                COALESCE(SUM(
                    CASE
                        WHEN score >= 4 AND next_timestamp IS NOT NULL THEN
                            EXTRACT(EPOCH FROM (next_timestamp - timestamp)) * 1000
                        ELSE 0
                    END
                ), 0) as bad_posture_duration_millis,
                COALESCE(SUM(
                    CASE
                        WHEN score <= 3 AND next_timestamp IS NOT NULL THEN
                            EXTRACT(EPOCH FROM (next_timestamp - timestamp)) * 1000
                        ELSE 0
                    END
                ), 0) as good_posture_duration_millis,
                COUNT(CASE WHEN bad_posture_start IS NOT NULL AND next_score < 4 THEN 1 END) as recovery_count,
                COALESCE(AVG(
                    CASE
                        WHEN bad_posture_start IS NOT NULL AND next_score < 4 THEN
                            EXTRACT(EPOCH FROM (next_timestamp - bad_posture_start)) * 1000
                        ELSE NULL
                    END
                ), 0) as avg_recovery_time_millis
            FROM active_metrics_with_recovery
            GROUP BY hour_of_day, day_of_week
            ORDER BY hour_of_day, day_of_week
        """,
        nativeQuery = true
    )
    fun getPosturePatternStats(
        @Param("userId") userId: UUID,
        @Param("startDate") startDate: LocalDateTime,
        @Param("endDate") endDate: LocalDateTime
    ): List<Array<Any>>

    @Query(
        value = """
            WITH active_ranges AS (
                SELECT
                    s.id as session_id,
                    s.created_at,
                    ss1.timestamp as start_time,
                    ss2.timestamp as end_time
                FROM session s
                CROSS JOIN LATERAL (
                    SELECT timestamp,
                           ROW_NUMBER() OVER (PARTITION BY session_id ORDER BY timestamp) as rn
                    FROM session_status
                    WHERE session_id = s.id
                      AND status IN ('STARTED', 'RESUMED')
                      AND deleted_at IS NULL
                ) ss1
                CROSS JOIN LATERAL (
                    SELECT timestamp,
                           ROW_NUMBER() OVER (PARTITION BY session_id ORDER BY timestamp) as rn
                    FROM session_status
                    WHERE session_id = s.id
                      AND status IN ('PAUSED', 'STOPPED')
                      AND deleted_at IS NULL
                      AND timestamp > ss1.timestamp
                    ORDER BY timestamp
                    LIMIT 1
                ) ss2
                WHERE s.user_id = :userId
                  AND s.created_at BETWEEN :startDate AND :endDate
                  AND s.deleted_at IS NULL
                  AND ss2.rn >= ss1.rn
            ),
            session_metrics AS (
                SELECT
                    ar.session_id,
                    ar.created_at,
                    LEAST(GREATEST(sm.score, 1), 6) as level,
                    EXTRACT(EPOCH FROM (
                        LEAD(sm.timestamp) OVER (PARTITION BY ar.session_id ORDER BY sm.timestamp) - sm.timestamp
                    )) * 1000 as duration_millis
                FROM active_ranges ar
                JOIN session_metric sm ON sm.session_id = ar.session_id
                WHERE sm.deleted_at IS NULL
                  AND sm.timestamp >= ar.start_time
                  AND sm.timestamp <= ar.end_time
            ),
            daily_stats AS (
                SELECT
                    DATE(created_at) as date,
                    SUM(CASE WHEN duration_millis IS NOT NULL THEN duration_millis / 1000.0 ELSE 0 END) / 60.0 as active_minutes,
                    SUM(CASE WHEN level >= 4 AND duration_millis IS NOT NULL THEN duration_millis ELSE 0 END) as bad_duration_millis,
                    SUM(CASE WHEN level <= 3 AND duration_millis IS NOT NULL THEN duration_millis ELSE 0 END) as good_duration_millis,
                    SUM(CASE WHEN duration_millis IS NOT NULL THEN duration_millis ELSE 0 END) as total_duration_millis,
                    AVG(CASE WHEN duration_millis IS NOT NULL THEN level ELSE NULL END) as avg_level
                FROM session_metrics
                GROUP BY DATE(created_at)
            )
            SELECT
                TO_CHAR(date, 'YYYY-MM-DD') as date,
                COALESCE(ROUND(active_minutes), 0) as active_minutes,
                COALESCE(bad_duration_millis, 0) as bad_duration_millis,
                COALESCE(good_duration_millis, 0) as good_duration_millis,
                COALESCE(total_duration_millis, 0) as total_duration_millis,
                COALESCE(avg_level, 0) as avg_level
            FROM daily_stats
            ORDER BY date
        """,
        nativeQuery = true
    )
    fun getAttendanceStats(
        @Param("userId") userId: UUID,
        @Param("startDate") startDate: LocalDateTime,
        @Param("endDate") endDate: LocalDateTime
    ): List<Array<Any>>
}

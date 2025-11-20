package com.github.kusitms_bugi.domain.dashboard.application

import com.github.kusitms_bugi.domain.dashboard.presentation.dto.response.PostureGraphResponse
import com.github.kusitms_bugi.domain.session.infrastructure.jpa.SessionJpaRepository
import com.github.kusitms_bugi.domain.user.infrastructure.jpa.User
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import kotlin.math.round

@Service
@Transactional(readOnly = true)
class PostureGraphService(
    private val sessionJpaRepository: SessionJpaRepository
) {

    fun getPostureGraph(user: User): PostureGraphResponse {
        val today = LocalDate.now()
        val startDate = today.minusDays(30)

        val days = (0..30).map { offset ->
            today.minusDays(offset.toLong())
        }.reversed()

        val sessions = sessionJpaRepository.findByUserAndCreatedAtBetween(
            user,
            startDate.atStartOfDay(),
            today.atTime(23, 59, 59)
        )

        val sessionsByDay = sessions.groupBy { session ->
            session.createdAt.toLocalDate()
        }

        val points = days.associateWith { day ->
            sessionsByDay[day]
                ?.mapNotNull { it.score }
                ?.takeIf { it.isNotEmpty() }
                ?.average()
                ?.let { round(it).toInt() }
                ?: 0
        }

        return PostureGraphResponse(points)
    }
}

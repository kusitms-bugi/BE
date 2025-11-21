package com.github.kusitms_bugi.domain.dashboard.application

import com.github.kusitms_bugi.domain.dashboard.presentation.dto.response.PostureGraphResponse
import com.github.kusitms_bugi.domain.session.infrastructure.jpa.SessionJpaRepository
import com.github.kusitms_bugi.domain.user.infrastructure.jpa.User
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

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

        val scoresByDate = sessionJpaRepository.getAverageScoresByDate(
            user.id,
            startDate.atStartOfDay(),
            today.atTime(23, 59, 59)
        ).associate { result ->
            LocalDate.parse(result[0] as String) to (result[1] as Number).toInt()
        }

        val points = days.associateWith { day ->
            scoresByDate[day] ?: 0
        }

        return PostureGraphResponse(points)
    }
}

package com.github.kusitms_bugi.domain.session.presentation.dto.response

import java.util.*

data class GetSessionResponse(
    val sessionId: UUID,
    val totalActiveTimeSeconds: Long
)

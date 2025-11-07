package com.github.kusitms_bugi.domain.session.presentation.dto.response

data class GetSessionReportResponse(
    val totalSeconds: Long,
    val goodSeconds: Long,
    val score: Int
)

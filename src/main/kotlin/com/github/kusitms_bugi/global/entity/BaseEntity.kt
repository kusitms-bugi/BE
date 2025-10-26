package com.github.kusitms_bugi.global.entity

import jakarta.persistence.Column
import jakarta.persistence.EntityListeners
import jakarta.persistence.Id
import jakarta.persistence.MappedSuperclass
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime
import java.util.*

@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
abstract class BaseEntity {
    @Id
    @Column(columnDefinition = "UUID")
    val id: UUID = UUID.randomUUID()

    @CreatedDate
    @Column(nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()

    @LastModifiedDate
    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()

    @Column
    var deletedAt: LocalDateTime? = null

    fun delete() {
        this.deletedAt = LocalDateTime.now()
    }

    fun isDeleted(): Boolean = deletedAt != null
}

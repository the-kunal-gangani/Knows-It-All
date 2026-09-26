package com.example.know_it_all.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "time_capsule_needs",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["uid"],
            childColumns = ["posterId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["posterId"]),
        Index(value = ["status"])
    ]
)
data class TimeCapsuleNeed(
    @PrimaryKey
    val needId: String = UUID.randomUUID().toString(),
    val posterId: String,
    val posterRole: UserRole,
    val title: String,
    val needDescription: String,
    val matchedTags: List<String> = emptyList(),
    val offeredInReturn: String = "",
    val urgency: NeedUrgency = NeedUrgency.NORMAL,
    val status: NeedStatus = NeedStatus.OPEN,
    val createdAt: Long = System.currentTimeMillis()
)

enum class NeedUrgency {
    LOW,
    NORMAL,
    HIGH
}

enum class NeedStatus {
    OPEN,
    MATCHED,
    FULFILLED,
    CLOSED
}
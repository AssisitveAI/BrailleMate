package kr.ac.kaist.aailab.braillemate.android.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val regulationId: Int,
    val createdAt: Long = System.currentTimeMillis()
)

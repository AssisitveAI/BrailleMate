package kr.ac.kaist.aailab.braillemate.android.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "regulations")
data class RegulationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val section: String,          // "한글 점자", "수학 점자", 등
    val chapter: String,          // "제1장 자모"
    val subSection: String? = null, // "제1절 첫소리로 쓰인 자음자"
    val articleNumber: String,    // "제1항"
    val content: String,          // 조항 본문
    val examples: String? = null, // JSON: [{"text":"너비","braille":"cs~o"}]
    val notes: String? = null,    // [다만], [붙임] 내용
    val orderIndex: Int = 0       // 정렬 순서
)

package kr.ac.kaist.aailab.braillemate.android.data.local.entity

import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.FtsOptions

@Fts4(contentEntity = RegulationEntity::class, tokenizer = FtsOptions.TOKENIZER_UNICODE61)
@Entity(tableName = "regulations_fts")
data class RegulationFts(
    val section: String,
    val chapter: String,
    val subSection: String?,
    val articleNumber: String,
    val content: String,
    val examples: String?,
    val notes: String?
)

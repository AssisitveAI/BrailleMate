package kr.ac.kaist.aailab.braillemate.android.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kr.ac.kaist.aailab.braillemate.android.data.local.dao.BookmarkDao
import kr.ac.kaist.aailab.braillemate.android.data.local.dao.ChatHistoryDao
import kr.ac.kaist.aailab.braillemate.android.data.local.dao.RegulationDao
import kr.ac.kaist.aailab.braillemate.android.data.local.entity.BookmarkEntity
import kr.ac.kaist.aailab.braillemate.android.data.local.entity.ChatMessageEntity
import kr.ac.kaist.aailab.braillemate.android.data.local.entity.RegulationEntity
import kr.ac.kaist.aailab.braillemate.android.data.local.entity.RegulationFts

@Database(
    entities = [
        RegulationEntity::class,
        RegulationFts::class,
        BookmarkEntity::class,
        ChatMessageEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class BrailleDatabase : RoomDatabase() {
    abstract fun regulationDao(): RegulationDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun chatHistoryDao(): ChatHistoryDao

    companion object {
        const val DATABASE_NAME = "braillemate.db"
    }
}

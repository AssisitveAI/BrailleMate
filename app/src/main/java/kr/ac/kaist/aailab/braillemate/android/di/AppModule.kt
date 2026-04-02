package kr.ac.kaist.aailab.braillemate.android.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kr.ac.kaist.aailab.braillemate.android.data.local.BrailleDatabase
import kr.ac.kaist.aailab.braillemate.android.data.local.dao.BookmarkDao
import kr.ac.kaist.aailab.braillemate.android.data.local.dao.ChatHistoryDao
import kr.ac.kaist.aailab.braillemate.android.data.local.dao.RegulationDao
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): BrailleDatabase {
        return Room.databaseBuilder(
            context,
            BrailleDatabase::class.java,
            BrailleDatabase.DATABASE_NAME
        ).build()
    }

    @Provides
    fun provideRegulationDao(db: BrailleDatabase): RegulationDao = db.regulationDao()

    @Provides
    fun provideBookmarkDao(db: BrailleDatabase): BookmarkDao = db.bookmarkDao()

    @Provides
    fun provideChatHistoryDao(db: BrailleDatabase): ChatHistoryDao = db.chatHistoryDao()

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.dataStore
    }
}

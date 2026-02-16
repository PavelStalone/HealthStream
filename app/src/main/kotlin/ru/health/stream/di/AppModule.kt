package ru.health.stream.di

import androidx.navigation3.runtime.NavKey
import com.arttttt.nav3router.Router
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    fun provideNavigationRouter(): Router<NavKey> = Router()
}

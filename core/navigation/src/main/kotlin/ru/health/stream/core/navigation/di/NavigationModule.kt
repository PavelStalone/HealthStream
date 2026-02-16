package ru.health.stream.core.navigation.di

import androidx.navigation3.runtime.NavKey
import com.arttttt.nav3router.Router
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal object NavigationModule {

    @Provides
    fun provideNavigationRouter(): Router<NavKey> = Router()
}

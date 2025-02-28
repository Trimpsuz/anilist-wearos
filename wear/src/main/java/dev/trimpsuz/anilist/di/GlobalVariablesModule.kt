package dev.trimpsuz.anilist.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.trimpsuz.anilist.utils.GlobalVariables
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object GlobalVariablesModule {

    @Singleton
    @Provides
    fun provideGlobalVariables(): GlobalVariables {
        return GlobalVariables()
    }

}
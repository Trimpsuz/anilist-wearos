package dev.trimpsuz.anilist.di

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.network.okHttpClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.trimpsuz.anilist.utils.ANILIST_GRAPHQL_URL
import dev.trimpsuz.anilist.utils.GlobalVariables
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Singleton
    @Provides
    fun provideApolloClient(
        authorizationInterceptor: AuthorizationInterceptor
    ): ApolloClient {
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(authorizationInterceptor)
            .build()

        return ApolloClient.Builder()
            .serverUrl(ANILIST_GRAPHQL_URL)
            .okHttpClient(okHttpClient)
            .httpExposeErrorBody(true)
            .build()
    }

    class AuthorizationInterceptor(
        private val globalVariables: GlobalVariables,
    ) : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request().newBuilder()
                .apply {
                    globalVariables.accessToken?.let {
                        addHeader("Authorization", "Bearer $it")
                    }
                }
                .build()
            return chain.proceed(request)
        }
    }

    @Singleton
    @Provides
    fun provideAuthorizationInterceptor(
        globalVariables: GlobalVariables
    ): AuthorizationInterceptor {
        return AuthorizationInterceptor(globalVariables)
    }
}

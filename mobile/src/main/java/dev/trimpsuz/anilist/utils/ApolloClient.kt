package dev.trimpsuz.anilist.utils

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.http.HttpRequest
import com.apollographql.apollo.api.http.HttpResponse
import com.apollographql.apollo.network.http.HttpInterceptor
import com.apollographql.apollo.network.http.HttpInterceptorChain
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first

class AuthInterceptor(private val tokenFlow: StateFlow<String>) : HttpInterceptor {
    override suspend fun intercept(request: HttpRequest, chain: HttpInterceptorChain): HttpResponse {
        val token = tokenFlow.first()
        val modifiedRequest = request.newBuilder()
            .addHeader("Authorization", "Bearer $token")
            .build()
        return chain.proceed(modifiedRequest)
    }
}

object ApolloInstance {
    fun createApolloClient(tokenFlow: StateFlow<String>): ApolloClient {
        return ApolloClient.Builder()
            .serverUrl(ANILIST_GRAPHQL_URL)
            .addHttpInterceptor(AuthInterceptor(tokenFlow))
            .build()
    }
}

/*object ApolloInstance {
    @Volatile
    private var apolloClient: ApolloClient? = null

    fun getApolloClient(token: String): ApolloClient {
        return apolloClient ?: synchronized(this) {
            apolloClient ?: createApolloClient(token).also { apolloClient = it }
        }
    }

    private fun createApolloClient(token: String): ApolloClient {
        return ApolloClient.Builder()
            .serverUrl(ANILIST_GRAPHQL_URL)
            .addHttpHeader("Authorization", "Bearer $token")
            .build()
    }
}*/
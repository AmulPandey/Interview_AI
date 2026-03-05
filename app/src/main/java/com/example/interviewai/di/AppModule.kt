package com.example.interviewai.di

import android.content.Context
import com.example.interviewai.data.local.TokenDataStore
import com.example.interviewai.data.remote.ApiService
import com.example.interviewai.data.repository.AuthRepository
import com.example.interviewai.data.repository.AuthRepositoryImpl
import com.example.interviewai.data.repository.InterviewRepository
import com.example.interviewai.data.repository.InterviewRepositoryImpl
import com.example.interviewai.data.repository.ResumeRepository
import com.example.interviewai.data.repository.ResumeRepositoryImpl
import com.example.interviewai.ui.theme.ThemePreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import com.example.interviewai.BuildConfig

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    private const val BASE_URL = BuildConfig.BASE_URL

    @Provides
    @Singleton
    fun provideOkHttpClient(tokenDataStore: TokenDataStore): OkHttpClient {

        val authInterceptor = Interceptor { chain ->
            val accessToken = runBlocking { tokenDataStore.accessToken.firstOrNull() }

            val originalRequest = chain.request()
            val authenticatedRequest = originalRequest.newBuilder().apply {
                if (!accessToken.isNullOrBlank()) header("Authorization", "Bearer $accessToken")
            }.build()

            val response = chain.proceed(authenticatedRequest)

            if (response.code == 401) {
                response.close()
                val newAccessToken = runBlocking {
                    try {
                        val refreshToken = tokenDataStore.refreshToken.firstOrNull()
                            ?: return@runBlocking null
                        val refreshRetrofit = Retrofit.Builder()
                            .baseUrl(BASE_URL)
                            .addConverterFactory(GsonConverterFactory.create())
                            .client(OkHttpClient.Builder().build())
                            .build()
                        val refreshApi   = refreshRetrofit.create(ApiService::class.java)
                        val refreshResp  = refreshApi.refreshToken(refreshToken)
                        if (refreshResp.isSuccessful) {
                            val body = refreshResp.body()!!
                            tokenDataStore.saveTokens(body.accessToken, body.refreshToken)
                            body.accessToken
                        } else {
                            tokenDataStore.clear()
                            null
                        }
                    } catch (e: Exception) {
                        tokenDataStore.clear()
                        null
                    }
                }
                if (newAccessToken != null) {
                    return@Interceptor chain.proceed(
                        originalRequest.newBuilder()
                            .header("Authorization", "Bearer $newAccessToken")
                            .build()
                    )
                }
            }
            response
        }

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService =
        retrofit.create(ApiService::class.java)

    @Provides @Singleton
    fun provideAuthRepository(impl: AuthRepositoryImpl): AuthRepository = impl

    @Provides @Singleton
    fun provideInterviewRepository(impl: InterviewRepositoryImpl): InterviewRepository = impl

    @Provides @Singleton
    fun provideResumeRepository(impl: ResumeRepositoryImpl): ResumeRepository = impl

    @Provides
    @Singleton
    fun provideThemePreferences(
        @ApplicationContext context: Context
    ): ThemePreferences = ThemePreferences(context)
}

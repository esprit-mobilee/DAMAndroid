    package com.example.esprit.di

    import com.example.esprit.network.ApiService
    import com.example.esprit.network.AiService
    import com.example.esprit.network.AuthInterceptor
    import com.example.esprit.util.Constants
    import com.example.esprit.util.DataStoreManager
    import dagger.Module
    import dagger.Provides
    import dagger.hilt.InstallIn
    import dagger.hilt.components.SingletonComponent
    import okhttp3.Interceptor
    import okhttp3.OkHttpClient
    import okhttp3.logging.HttpLoggingInterceptor
    import retrofit2.Retrofit
    import retrofit2.converter.gson.GsonConverterFactory
    import javax.inject.Singleton

    /**
     * NetworkModule
     * Provides all networking dependencies:
     *  - OkHttpClient with logging + AuthInterceptor
     *  - Retrofit instance
     *  - ApiService implementation
     */
    @Module
    @InstallIn(SingletonComponent::class)
    object NetworkModule {

        // 🧾 Logs every request & response (useful during development)
        @Provides
        @Singleton
        fun provideLoggingInterceptor(): HttpLoggingInterceptor =
            HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

        // 🔐 AuthInterceptor adds JWT automatically from DataStoreManager
        @Provides
        @Singleton
        fun provideAuthInterceptor(
            dataStoreManager: DataStoreManager
        ): AuthInterceptor = AuthInterceptor(dataStoreManager)

        // Provide as Interceptor for OkHttpClient
        @Provides
        @Singleton
        fun provideAuthInterceptorAsInterceptor(
            authInterceptor: AuthInterceptor
        ): Interceptor = authInterceptor

        // ⚙️ OkHttp client with both interceptors
        @Provides
        @Singleton
        fun provideOkHttpClient(
            logging: HttpLoggingInterceptor,
            authInterceptorAsInterceptor: Interceptor
        ): OkHttpClient =
            OkHttpClient.Builder()
                .addInterceptor(logging)
                .addInterceptor(authInterceptorAsInterceptor)
                .build()

        // 🔧 Gson with custom deserializers
        @Provides
        @Singleton
        fun provideGson(): com.google.gson.Gson =
            com.google.gson.GsonBuilder()
                .registerTypeAdapter(
                    com.example.esprit.model.Application::class.java,
                    com.example.esprit.model.ApplicationDeserializer()
                )
                .create()

        // 🌐 Retrofit instance
        @Provides
        @Singleton
        fun provideRetrofit(client: OkHttpClient, gson: com.google.gson.Gson): Retrofit =
            Retrofit.Builder()
                .baseUrl(Constants.BASE_URL) // e.g. "http://10.0.2.2:3000/api/"
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()

        // 🚀 ApiService interface implementation
        @Provides
        @Singleton
        fun provideApiService(retrofit: Retrofit): ApiService =
            retrofit.create(ApiService::class.java)


        // 🤖 AiService (optional endpoints)


        @Provides
        @Singleton
        fun provideAiService(retrofit: Retrofit): AiService =
            retrofit.create(AiService::class.java)
    }

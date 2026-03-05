package com.example.interviewai.util


import android.content.Context
import android.content.res.Configuration
import java.util.Locale

object LocaleHelper {
    fun applyLocale(context: Context, languageCode: String): Context {
        if (languageCode == "en") {
            // Reset to default for English
            val locale = Locale.ENGLISH
            Locale.setDefault(locale)
            val config = Configuration(context.resources.configuration)
            config.setLocale(locale)
            return context.createConfigurationContext(config)
        }

        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }
}
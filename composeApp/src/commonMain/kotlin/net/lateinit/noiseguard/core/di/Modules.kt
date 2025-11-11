package net.lateinit.noiseguard.core.di

import net.lateinit.noiseguard.domain.usecase.ClassifyNoiseTypeUseCase
import net.lateinit.noiseguard.presentation.viewmodel.HomeViewModel
import net.lateinit.noiseguard.notification.LiveUpdateController
import org.koin.dsl.module
import net.lateinit.noiseguard.domain.label.LabelLocalizer
import net.lateinit.noiseguard.domain.label.PassthroughLabelLocalizer

val dataModule = module {
    // DataSource
    // singleOf(::AudioDataSource)
    // single { createDatabase(get()) }

    // Repository
    // singleOf(::AudioRepositoryImpl) bind AudioRepository::class
    // singleOf(::NoiseRepositoryImpl) bind NoiseRepository::class
}

val domainModule = module {
    // UseCases
    single { ClassifyNoiseTypeUseCase() }
    // Default localizer (overridden on Android/iOS)
    single<LabelLocalizer> { PassthroughLabelLocalizer() }
}

val presentationModule = module {
    // ViewModels
    factory { HomeViewModel(get(), get(), get(), get(), get<LiveUpdateController>()) }
    // factoryOf(::RecordingViewModel)
    // factoryOf(::HistoryViewModel)
    // factoryOf(::AnalysisViewModel)
    // factoryOf(::SettingsViewModel)
}

fun appModules() = listOf(
    dataModule,
    domainModule,
    presentationModule
)

package net.lateinit.noiseguard.core.di

import net.lateinit.noiseguard.presentation.viewmodel.HomeViewModel
import org.koin.dsl.module

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
    // factoryOf(::StartRecordingUseCase)
    // factoryOf(::StopRecordingUseCase)
    // factoryOf(::GetNoiseLevelsUseCase)
    // factoryOf(::GetRecordsUseCase)
    // factoryOf(::SaveRecordUseCase)
    // factoryOf(::DeleteRecordUseCase)
    // factoryOf(::AnalyzeNoiseUseCase)
}

val presentationModule = module {
    // ViewModels
    factory { HomeViewModel(get()) }
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
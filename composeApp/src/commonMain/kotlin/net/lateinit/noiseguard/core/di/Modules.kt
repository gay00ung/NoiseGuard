//package net.lateinit.noiseguard.core.di
//
//import net.lateinit.noiseguard.domain.repository.AudioRepository
//import net.lateinit.noiseguard.domain.repository.NoiseRepository
//import net.lateinit.noiseguard.domain.usecase.GetNoiseLevelsUseCase
//import net.lateinit.noiseguard.domain.usecase.StartRecordingUseCase
//import net.lateinit.noiseguard.domain.usecase.StopRecordingUseCase
//import net.lateinit.noiseguard.presentation.viewmodel.HomeViewModel
//import net.lateinit.noiseguard.presentation.viewmodel.RecordingViewModel
//import org.koin.core.module.dsl.factoryOf
//import org.koin.core.module.dsl.singleOf
//import org.koin.dsl.bind
//import org.koin.dsl.module
//
//val dataModule = module {
//    // DataSource
////    singleOf(::AudioDataSource)
////    single { createDatabase(get()) }
//
//    // Repository
////    singleOf(::AudioRepositoryImpl) bind AudioRepository::class
////    singleOf(::NoiseRepositoryImpl) bind NoiseRepository::class
//}
//
//val domainModule = module {
//    // UseCases
//    factoryOf(::StartRecordingUseCase)
//    factoryOf(::StopRecordingUseCase)
//    factoryOf(::GetNoiseLevelsUseCase)
////    factoryOf(::GetRecordsUseCase)
////    factoryOf(::SaveRecordUseCase)
////    factoryOf(::DeleteRecordUseCase)
////    factoryOf(::AnalyzeNoiseUseCase)
//}
//
//val presentationModule = module {
//    // ViewModels
//    factoryOf(::HomeViewModel)
//    factoryOf(::RecordingViewModel)
////    factoryOf(::HistoryViewModel)
////    factoryOf(::AnalysisViewModel)
////    factoryOf(::SettingsViewModel)
//}
//
//fun appModules() = listOf(
//    dataModule,
//    domainModule,
//    presentationModule
//)
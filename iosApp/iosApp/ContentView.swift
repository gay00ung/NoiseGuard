import UIKit
import SwiftUI
import ComposeApp
import TensorFlowLite
import AVFoundation

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    private let runner = IOSAudioClassifierRunner()
    var body: some View {
        ComposeView()
            .ignoresSafeArea()
            .onAppear {
                // Kotlin에서 enabled 플래그를 바꾸면 러너 시작/중지
                Timer.scheduledTimer(withTimeInterval: 0.5, repeats: true) { _ in
                    let enabled = IOSClassificationBridgeKt.isIOSClassificationEnabled()
                    if enabled && !runner.isRunningNow() {
                        runner.start()
                    } else if !enabled && runner.isRunningNow() {
                        runner.stop()
                    }
                }
            }
    }
}

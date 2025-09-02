import Foundation
import AVFoundation
import ComposeApp
import TensorFlowLiteTaskAudio
import Darwin

/// iOS 오디오 입력 → TaskAudio AudioRecord 사용해 분류 → Kotlin으로 전달
final class IOSAudioClassifierRunner {
    private var timer: Timer?
    private var isRunning = false
    private var classifier: AudioClassifier?
    private var inputTensor: AudioTensor?
    private var audioRecord: AudioRecord?
    private var labelMap: [Int: String] = [:]
    private let scoreThreshold: Float = 0.3

    func start() {
        guard !isRunning else { return }
        isRunning = true

        let session = AVAudioSession.sharedInstance()
        try? session.setCategory(.playAndRecord, mode: .measurement, options: [.allowBluetooth, .defaultToSpeaker])
        try? session.setActive(true)

        if classifier == nil {
            do {
                let modelPath = ModelFileProviderBlockingKt.ensureModelFileBlocking(fileName: "yamnet.tflite")
                let exists = FileManager.default.fileExists(atPath: modelPath)
                let size = (try? FileManager.default.attributesOfItem(atPath: modelPath)[.size] as? NSNumber)?.intValue ?? -1
                print("[IOSAudioClassifierRunner] Using model path: \(modelPath), exists=\(exists), size=\(size)")

                // 일부 기기에서 delegate 관련 크래시 회피
                setenv("TF_LITE_DISABLE_XNNPACK", "1", 1)
                setenv("TFLITE_ENABLE_XNNPACK", "0", 1)

                let options = AudioClassifierOptions(modelPath: modelPath)
                let clf = try AudioClassifier.classifier(options: options)
                let tensor = clf.createInputAudioTensor()
                let arec = try clf.createAudioRecord()
                try arec.startRecording()
                self.classifier = clf
                self.inputTensor = tensor
                self.audioRecord = arec
                print("[IOSAudioClassifierRunner] Classifier ready. bufferSize=\(tensor.bufferSize) sr=\(tensor.audioFormat.sampleRate) ch=\(tensor.audioFormat.channelCount)")
                self.loadLabelMap()
            } catch {
                print("[IOSAudioClassifierRunner] Classifier init error: \(error)")
            }
        } else if let arec = self.audioRecord {
            try? arec.startRecording()
        }

        // 주기적으로 레코더에서 읽어와 분류
        timer = Timer.scheduledTimer(withTimeInterval: 0.1, repeats: true) { _ in
            if !IOSClassificationBridgeKt.isIOSClassificationEnabled() {
                self.stop()
                return
            }
            guard let clf = self.classifier, let tensor = self.inputTensor, let arec = self.audioRecord else { return }
            do {
                try tensor.load(audioRecord: arec)
                let result = try clf.classify(audioTensor: tensor)
                if let first = result.classifications.first {
                    var labels: [String] = []
                    for item in first.categories {
                        guard let cat = item as? ClassificationCategory else { continue }
                        if cat.score < self.scoreThreshold { continue }
                        let name = (cat.label ?? cat.displayName) ?? self.labelMap[Int(cat.index)]
                        if let n = name, !n.isEmpty { labels.append(n) }
                    }
                    if !labels.isEmpty { IOSClassificationBridgeKt.publishIOSLabels(labels: labels) }
                }
            } catch {
                print("[IOSAudioClassifierRunner] classify error: \(error)")
            }
        }
    }

    func stop() {
        guard isRunning else { return }
        isRunning = false
        timer?.invalidate(); timer = nil
        if let arec = audioRecord { try? arec.stop() }
        try? AVAudioSession.sharedInstance().setActive(false)
    }

    func isRunningNow() -> Bool { isRunning }

    private func loadLabelMap() {
        // CSV: index,mid,display_name
        let csv = ModelFileProviderBlockingKt.readTextResourceBlocking(fileName: "yamnet_class_map.csv")
        var map: [Int: String] = [:]
        for rawLine in csv.split(whereSeparator: { $0 == "\n" || $0 == "\r\n" }) {
            let line = String(rawLine)
            let t = line.trimmingCharacters(in: .whitespacesAndNewlines)
            if t.isEmpty { continue }
            if t.lowercased().contains("display_name") && t.lowercased().contains("index") { continue }
            let parts = t.split(separator: ",").map { String($0) }
            if parts.count >= 2, let idx = Int(parts[0].trimmingCharacters(in: .whitespaces)) {
                let name = parts.last!.trimmingCharacters(in: .whitespacesAndNewlines)
                if !name.isEmpty { map[idx] = name }
            }
        }
        self.labelMap = map
        print("[IOSAudioClassifierRunner] Loaded label map entries: \(map.count)")
    }
}


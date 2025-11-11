import Foundation
import TensorFlowLite
import ComposeApp

final class TFLiteBridge {
    private var interpreter: Interpreter?

    /// Copies the model from KMP resources to iOS cache and initializes Interpreter.
    func loadModel(name: String = "yamnet.tflite", threads: Int = 2) throws {
        let modelPath = ModelFileProviderBlockingKt.ensureModelFileBlocking(fileName: name)
        var options = Interpreter.Options()
        options.threadCount = threads
        let interp = try Interpreter(modelPath: modelPath, options: options)
        try interp.allocateTensors()
        self.interpreter = interp
        print("[TFLiteBridge] Model loaded at: \(modelPath)")
    }

    /// Loads label CSV text from KMP resources.
    func loadLabelsCSV(name: String = "yamnet_class_map.csv") -> [String] {
        let text = ModelFileProviderBlockingKt.readTextResourceBlocking(fileName: name)
        return text.split(whereSeparator: { $0 == "\n" || $0 == "\r\n" }).map(String.init)
    }

    /// Returns basic tensor info for quick sanity check.
    func tensorInfo() throws -> (inputCount: Int, outputCount: Int) {
        guard let interp = interpreter else { throw NSError(domain: "TFLiteBridge", code: -1, userInfo: [NSLocalizedDescriptionKey: "Interpreter not initialized"]) }
        return (interp.inputTensorCount, interp.outputTensorCount)
    }
}


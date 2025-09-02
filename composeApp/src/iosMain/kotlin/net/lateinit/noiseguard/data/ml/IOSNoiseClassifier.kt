package net.lateinit.noiseguard.data.ml

class IOSNoiseClassifier : NoiseClassifierApi {
    override fun initialize() {
        // Swift 측 러너가 첫 호출 시 모델 로드.
    }

    override fun startRecordingAndClassifying(onResult: (List<String>) -> Unit) {
        IOSClassificationBridge.setListener(onResult)
        IOSClassificationBridge.setEnabled(true)
    }

    override fun stopRecording() {
        IOSClassificationBridge.setEnabled(false)
        IOSClassificationBridge.clearListener()
    }
}

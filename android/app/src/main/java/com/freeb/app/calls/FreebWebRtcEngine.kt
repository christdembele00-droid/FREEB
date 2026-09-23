package com.freeb.app.calls

import android.content.Context
import org.webrtc.Camera2Enumerator
import org.webrtc.CameraVideoCapturer
import org.webrtc.DefaultVideoDecoderFactory
import org.webrtc.DefaultVideoEncoderFactory
import org.webrtc.EglBase
import org.webrtc.IceCandidate
import org.webrtc.MediaConstraints
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription
import org.webrtc.SurfaceTextureHelper
import org.webrtc.VideoCapturer
import org.webrtc.VideoTrack

class FreebWebRtcEngine(
    private val context: Context,
    private val eglBase: EglBase
) {
    companion object {
        @Volatile private var initialized = false

        private fun initializeWebRtc(context: Context) {
            if (initialized) return
            synchronized(this) {
                if (!initialized) {
                    PeerConnectionFactory.initialize(
                        PeerConnectionFactory.InitializationOptions
                            .builder(context.applicationContext)
                            .setEnableInternalTracer(false)
                            .createInitializationOptions()
                    )
                    initialized = true
                }
            }
        }
    }

    private val factory: PeerConnectionFactory
    private val videoCapturer: VideoCapturer
    private val surfaceTextureHelper: SurfaceTextureHelper
    private val videoSource: org.webrtc.VideoSource
    private val audioSource: org.webrtc.AudioSource
    val localVideoTrack: VideoTrack
    val localAudioTrack: org.webrtc.AudioTrack

    private var peerConnection: PeerConnection? = null

    init {
        initializeWebRtc(context)

        factory = PeerConnectionFactory.builder()
            .setVideoEncoderFactory(
                DefaultVideoEncoderFactory(eglBase.eglBaseContext, true, true)
            )
            .setVideoDecoderFactory(
                DefaultVideoDecoderFactory(eglBase.eglBaseContext)
            )
            .createPeerConnectionFactory()

        videoCapturer = createFrontCameraCapturer(context)
        videoSource = factory.createVideoSource(false)
        surfaceTextureHelper = SurfaceTextureHelper.create(
            "FREEB-Camera",
            eglBase.eglBaseContext
        )
        videoCapturer.initialize(
            surfaceTextureHelper,
            context.applicationContext,
            videoSource.capturerObserver
        )
        videoCapturer.startCapture(1280, 720, 30)

        localVideoTrack = factory.createVideoTrack("FREEB_VIDEO", videoSource)
        audioSource = factory.createAudioSource(MediaConstraints())
        localAudioTrack = factory.createAudioTrack("FREEB_AUDIO", audioSource)
    }

    fun createPeerConnection(onRemoteTrack: (org.webrtc.VideoTrack) -> Unit, onIce: (IceCandidate) -> Unit) {
        peerConnection?.close()

        val iceServers = listOf(
            PeerConnection.IceServer.builder("stun:stun.l.google.com:19302")
                .createIceServer()
        )

        val observer = object : PeerConnection.Observer {
            override fun onIceCandidate(candidate: IceCandidate) {
                onIce(candidate)
            }

            override fun onTrack(transceiver: org.webrtc.RtpTransceiver?) {
                val track = transceiver?.receiver?.track()
                if (track is org.webrtc.VideoTrack) onRemoteTrack(track)
            }

            override fun onSignalingChange(state: PeerConnection.SignalingState?) = Unit
            override fun onIceConnectionChange(state: PeerConnection.IceConnectionState?) = Unit
            override fun onIceConnectionReceivingChange(receiving: Boolean) = Unit
            override fun onIceGatheringChange(state: PeerConnection.IceGatheringState?) = Unit
            override fun onIceCandidatesRemoved(candidates: Array<out IceCandidate>?) = Unit
            override fun onAddStream(stream: org.webrtc.MediaStream?) = Unit
            override fun onRemoveStream(stream: org.webrtc.MediaStream?) = Unit
            override fun onDataChannel(dataChannel: org.webrtc.DataChannel?) = Unit
            override fun onRenegotiationNeeded() = Unit
            override fun onAddTrack(receiver: org.webrtc.RtpReceiver?, mediaStreams: Array<out org.webrtc.MediaStream>?) = Unit
            override fun onConnectionChange(newState: PeerConnection.PeerConnectionState?) = Unit
            override fun onStandardizedIceConnectionChange(newState: PeerConnection.IceConnectionState?) = Unit
        }

        peerConnection = factory.createPeerConnection(iceServers, observer)
        val pc = peerConnection ?: error("PeerConnection unavailable")
        pc.addTrack(localVideoTrack)
        pc.addTrack(localAudioTrack)
    }

    fun createOffer(onLocalDescription: (SessionDescription) -> Unit) {
        createSdp(true, onLocalDescription)
    }

    fun createAnswer(onLocalDescription: (SessionDescription) -> Unit) {
        createSdp(false, onLocalDescription)
    }

    private fun createSdp(offer: Boolean, onLocalDescription: (SessionDescription) -> Unit) {
        val pc = peerConnection ?: error("PeerConnection unavailable")
        val constraints = MediaConstraints().apply {
            mandatory.add(
                MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true")
            )
            mandatory.add(
                MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true")
            )
        }
        val observer = object : SdpObserver {
            override fun onCreateSuccess(description: SessionDescription?) {
                if (description == null) return
                pc.setLocalDescription(object : SdpObserver {
                    override fun onSetSuccess() = onLocalDescription(description)
                    override fun onSetFailure(error: String?) = Unit
                    override fun onCreateSuccess(description: SessionDescription?) = Unit
                    override fun onCreateFailure(error: String?) = Unit
                }, description)
            }
            override fun onSetSuccess() = Unit
            override fun onCreateFailure(error: String?) = Unit
            override fun onSetFailure(error: String?) = Unit
        }
        if (offer) pc.createOffer(observer, constraints) else pc.createAnswer(observer, constraints)
    }

    fun setRemoteDescription(type: String, sdp: String) {
        val pc = peerConnection ?: return
        pc.setRemoteDescription(
            object : SdpObserver {
                override fun onSetSuccess() = Unit
                override fun onSetFailure(error: String?) = Unit
                override fun onCreateSuccess(description: SessionDescription?) = Unit
                override fun onCreateFailure(error: String?) = Unit
            },
            SessionDescription(
                if (type.equals("offer", true)) SessionDescription.Type.OFFER
                else SessionDescription.Type.ANSWER,
                sdp
            )
        )
    }

    fun addIceCandidate(candidate: IceCandidate) {
        peerConnection?.addIceCandidate(candidate)
    }

    fun close() {
        runCatching { videoCapturer.stopCapture() }
        videoCapturer.dispose()
        surfaceTextureHelper.dispose()
        videoSource.dispose()
        audioSource.dispose()
        peerConnection?.close()
        peerConnection = null
        factory.dispose()
    }

    private fun createFrontCameraCapturer(context: Context): CameraVideoCapturer {
        val enumerator = Camera2Enumerator(context)
        enumerator.deviceNames.firstOrNull(enumerator::isFrontFacing)?.let { name ->
            return enumerator.createCapturer(name, null)
                ?: error("Unable to create front camera capturer")
        }
        return enumerator.deviceNames.firstNotNullOfOrNull { name ->
            enumerator.createCapturer(name, null)
        } ?: error("No camera available")
    }
}

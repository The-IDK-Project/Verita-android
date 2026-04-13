package io.theidkteam.verita.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import org.matrix.android.sdk.api.session.Session
import org.matrix.android.sdk.api.session.room.Room
import org.matrix.android.sdk.api.session.room.timeline.TimelineEvent
import org.matrix.android.sdk.api.session.room.timeline.TimelineSettings
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val session: Session
) : ViewModel() {

    private var room: Room? = null

    fun initRoom(roomId: String) {
        room = session.getRoom(roomId)
    }

    val timelineEvents: Flow<List<TimelineEvent>>? by lazy {
        room?.timelineService()?.createTimeline(null, TimelineSettings(30))?.apply {
            start()
        }?.getTimelineEventsLive()?.asFlow()
    }

    fun sendMessage(text: String) {
        room?.sendService()?.sendTextMessage(text)
    }

    fun sendVoiceMessage(file: java.io.File, duration: Long) {
        room?.sendService()?.sendAudio(
            file = file,
            duration = duration,
            mimeType = "audio/ogg",
            fileName = "Voice Message.ogg",
            waveForm = null // Optional: waveform data
        )
    }

    fun sendCircleVideo(file: java.io.File, duration: Long, width: Int, height: Int) {
        // "Circles" are usually square videos with a specific flag or just treated as video
        room?.sendService()?.sendVideo(
            file = file,
            duration = duration,
            width = width,
            height = height,
            mimeType = "video/mp4",
            fileName = "Circle Video.mp4",
            thumbnailFile = null
        )
    }
}

package io.theidkteam.verita.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.matrix.android.sdk.api.session.Session
import org.matrix.android.sdk.api.session.room.Room
import org.matrix.android.sdk.api.session.room.timeline.Timeline
import org.matrix.android.sdk.api.session.room.timeline.TimelineEvent
import org.matrix.android.sdk.api.session.room.timeline.TimelineSettings
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    @org.jetbrains.annotations.Nullable private val session: Session?
) : ViewModel() {

    private var room: Room? = null
    private var timeline: Timeline? = null

    private val _timelineEvents = MutableStateFlow<List<TimelineEvent>>(emptyList())
    val timelineEvents: StateFlow<List<TimelineEvent>> = _timelineEvents

    fun initRoom(roomId: String) {
        room = session?.roomService()?.getRoom(roomId)
        timeline = room?.timelineService()?.createTimeline(null, TimelineSettings(30))
        timeline?.addListener(object : Timeline.Listener {
            override fun onTimelineUpdated(snapshot: List<TimelineEvent>) {
                _timelineEvents.value = snapshot
            }
            override fun onTimelineFailure(throwable: Throwable) {}
            override fun onNewTimelineEvents(eventIds: List<String>) {}
            override fun onStateUpdated(direction: Timeline.Direction, state: Timeline.PaginationState) {}
        })
        timeline?.start()
    }

    override fun onCleared() {
        timeline?.dispose()
        super.onCleared()
    }

    fun sendMessage(text: String) {
        room?.sendService()?.sendTextMessage(text)
    }
}

package io.theidkteam.verita.ui.chat

import android.util.Log
import androidx.lifecycle.ViewModel
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
        if (room?.roomId == roomId) return
        
        Log.d("ChatViewModel", "Initializing room: $roomId")
        timeline?.dispose()
        room = session?.roomService()?.getRoom(roomId)
        
        // Create timeline starting from the end of the room
        timeline = room?.timelineService()?.createTimeline(null, TimelineSettings(30))
        timeline?.addListener(object : Timeline.Listener {
            override fun onTimelineUpdated(snapshot: List<TimelineEvent>) {
                Log.d("ChatViewModel", "Timeline updated: ${snapshot.size} events")
                // Reverse the list so newest messages are at index 0 for reverseLayout
                _timelineEvents.value = snapshot.reversed()
            }
            override fun onTimelineFailure(throwable: Throwable) {
                Log.e("ChatViewModel", "Timeline failed", throwable)
            }
            override fun onNewTimelineEvents(eventIds: List<String>) {
                Log.d("ChatViewModel", "New events received: ${eventIds.size}")
            }
            override fun onStateUpdated(direction: Timeline.Direction, state: Timeline.PaginationState) {
                Log.d("ChatViewModel", "Timeline state updated: $state in direction $direction")
            }
        })
        timeline?.start()
        
        // Initial pagination to fill the screen with messages
        timeline?.paginate(Timeline.Direction.BACKWARDS, 30)
    }

    override fun onCleared() {
        timeline?.dispose()
        super.onCleared()
    }

    fun sendMessage(text: String) {
        room?.sendService()?.sendTextMessage(text)
    }

    fun getContentUrlResolver() = session?.contentUrlResolver()
}

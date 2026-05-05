package io.theidkteam.verita.ui.chat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.matrix.android.sdk.api.session.Session
import org.matrix.android.sdk.api.session.room.Room
import org.matrix.android.sdk.api.session.room.timeline.Timeline
import org.matrix.android.sdk.api.session.room.timeline.TimelineEvent
import org.matrix.android.sdk.api.session.room.timeline.TimelineSettings
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val session: Session?
) : ViewModel() {

    private var room: Room? = null
    private var timeline: Timeline? = null

    private val _timelineEvents = MutableStateFlow<List<TimelineEvent>>(emptyList())
    val timelineEvents: StateFlow<List<TimelineEvent>> = _timelineEvents

    data class TestMessage(val id: String, val sender: String, val body: String)

    private val _testMessagesMap = mutableMapOf<String, List<TestMessage>>()
    private val _testMessages = MutableStateFlow<List<TestMessage>>(emptyList())
    val testMessages: StateFlow<List<TestMessage>> = _testMessages

    private var currentRoomId: String? = null

    val myUserId: String? = session?.myUserId

    fun initRoom(roomId: String) {
        if (room?.roomId == roomId || currentRoomId == roomId) return
        
        // Save current test messages before switching
        currentRoomId?.let { oldId ->
            _testMessagesMap[oldId] = _testMessages.value
        }
        
        currentRoomId = roomId
        Log.d("ChatViewModel", "Initializing room: $roomId")
        timeline?.dispose()
        room = session?.roomService()?.getRoom(roomId)
        
        // Load or initialize test messages for this room
        if (room == null) {
            val roomName = if (roomId == "1") "Android Devs" else "Verita Support"
            _testMessages.value = _testMessagesMap[roomId] ?: listOf(
                TestMessage(System.currentTimeMillis().toString(), "Bot", "Welcome to the $roomName chat! This is a separate test branch.")
            )
        } else {
            _testMessages.value = emptyList()
        }
        
        timeline = room?.timelineService()?.createTimeline(null, TimelineSettings(30))
        timeline?.addListener(object : Timeline.Listener {
            override fun onTimelineUpdated(snapshot: List<TimelineEvent>) {
                _timelineEvents.value = snapshot.reversed()
            }
            override fun onTimelineFailure(throwable: Throwable) {
                Log.e("ChatViewModel", "Timeline failed", throwable)
            }
            override fun onNewTimelineEvents(eventIds: List<String>) {}
            override fun onStateUpdated(direction: Timeline.Direction, state: Timeline.PaginationState) {}
        })
        timeline?.start()
        timeline?.paginate(Timeline.Direction.BACKWARDS, 30)
    }

    override fun onCleared() {
        timeline?.dispose()
        super.onCleared()
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return
        
        // Capitalize sentences/first letter as requested by user.
        // Assuming capitalization of sentences/first letter since that's standard for chat.
        val capitalizedText = text.trim().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        
        val currentRoom = room
        if (currentRoom != null) {
            currentRoom.sendService().sendTextMessage(capitalizedText)
        } else {
            // Mock sending for demo purposes
            val myMsg = TestMessage(System.currentTimeMillis().toString(), "Me", capitalizedText)
            _testMessages.value = _testMessages.value + myMsg
            
            // Simulating a reply after 1 second
            viewModelScope.launch {
                delay(1000)
                val replyBody = when {
                    capitalizedText.contains("hello", ignoreCase = true) || capitalizedText.contains("hi", ignoreCase = true) -> 
                        "Hello! I'm Verita test bot. Your login is currently in demo mode."
                    capitalizedText.contains("how are you", ignoreCase = true) || capitalizedText.contains("hows it going", ignoreCase = true) ->
                        "I'm doing great, I'm just a set of code lines, but I work!"
                    else -> "Message received: \"$capitalizedText\". To send real messages, log in to a Matrix account."
                }
                val botMsg = TestMessage((System.currentTimeMillis() + 1).toString(), "Bot", replyBody)
                _testMessages.value = _testMessages.value + botMsg
            }
        }
    }

    fun getContentUrlResolver() = session?.contentUrlResolver()
    
    fun isRoomEncrypted(): Boolean {
        return room?.roomSummary()?.isEncrypted ?: false
    }
}

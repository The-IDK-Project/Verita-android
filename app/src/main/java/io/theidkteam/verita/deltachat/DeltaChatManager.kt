package io.theidkteam.verita.deltachat

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

data class DeltaChatRoom(
    val id: String,
    val name: String,
    val lastMessage: String,
    val time: String,
    val isBeta: Boolean = true
)

@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class DeltaChatManager @Inject constructor() {
    
    private val _isConfigured = MutableStateFlow(false)
    val isConfigured = _isConfigured.asStateFlow()

    private val roomsFlow = _isConfigured.flatMapLatest { configured ->
        if (!configured) {
            flowOf(emptyList())
        } else {
            // Mock data for beta
            flowOf(
                listOf(
                    DeltaChatRoom("dc_1", "Delta Chat User", "Hello from Delta Chat! 📧", "14:20"),
                    DeltaChatRoom("dc_2", "Email Group", "Discussion about privacy", "Yesterday")
                )
            )
        }
    }

    fun setConfigured(configured: Boolean) {
        _isConfigured.value = configured
    }
    
    fun getDeltaRooms(): Flow<List<DeltaChatRoom>> = roomsFlow
}

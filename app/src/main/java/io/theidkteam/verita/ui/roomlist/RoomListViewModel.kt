package io.theidkteam.verita.ui.roomlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.matrix.android.sdk.api.session.Session
import org.matrix.android.sdk.api.session.room.model.Membership
import org.matrix.android.sdk.api.session.room.RoomSummaryQueryParams
import org.matrix.android.sdk.api.session.room.model.RoomSummary
import org.matrix.android.sdk.api.session.sync.SyncState
import io.theidkteam.verita.deltachat.DeltaChatManager
import io.theidkteam.verita.deltachat.DeltaChatRoom
import javax.inject.Inject

@HiltViewModel
class RoomListViewModel @Inject constructor(
    @org.jetbrains.annotations.Nullable private val session: Session?,
    private val deltaChatManager: DeltaChatManager
) : ViewModel() {

    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState

    init {
        session?.let { s ->
            if (s.isOpenable) {
                try {
                    s.open()
                    
                    // Safely collect sync state using Flow
                    s.syncService().getSyncStateLive().asFlow()
                        .onEach { _syncState.value = it }
                        .launchIn(viewModelScope)
                    
                    if (s.syncService().getSyncState() == SyncState.Idle) {
                        s.syncService().startSync(true)
                    }
                } catch (t: Throwable) {
                    // Session might already be open or initialization failed
                }
            }
        }
    }

    val isLogged: Boolean = session != null

    val rooms: Flow<List<RoomSummary>> = session?.let { s ->
        val queryParams = RoomSummaryQueryParams.Builder().apply {
            memberships = Membership.all()
        }.build()
        s.roomService().getRoomSummariesLive(queryParams).asFlow()
    } ?: flowOf(emptyList())

    val deltaRooms: Flow<List<DeltaChatRoom>> = deltaChatManager.getDeltaRooms()

    fun enableDeltaChat() {
        deltaChatManager.setConfigured(true)
    }

    fun logout() {
        viewModelScope.launch {
            session?.syncService()?.stopSync()
            session?.signOutService()?.signOut(true, false)
        }
    }
}

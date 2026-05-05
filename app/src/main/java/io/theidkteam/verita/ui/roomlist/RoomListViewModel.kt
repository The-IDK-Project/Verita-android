package io.theidkteam.verita.ui.roomlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.matrix.android.sdk.api.session.Session
import org.matrix.android.sdk.api.session.room.RoomSummaryQueryParams
import org.matrix.android.sdk.api.session.room.model.RoomSummary
import org.matrix.android.sdk.api.session.sync.SyncState
import javax.inject.Inject

@HiltViewModel
class RoomListViewModel @Inject constructor(
    @org.jetbrains.annotations.Nullable private val session: Session?
) : ViewModel() {

    init {
        session?.let { s ->
            if (s.isOpenable) {
                try {
                    s.open()
                } catch (t: Throwable) {
                    // Catch potential AssertionError if already open or other initialization errors
                }
                
                try {
                    // Only start sync if it's currently Idle to avoid AssertionError
                    if (s.syncService().getSyncState() is SyncState.Idle) {
                        s.syncService().startSync(true)
                    }
                } catch (t: Throwable) {
                    // Catch potential AssertionError if session is still not open
                }
            }
        }
    }

    val rooms: Flow<List<RoomSummary>>? = session?.let {
        val queryParams = RoomSummaryQueryParams.Builder().build()
        it.roomService().getRoomSummariesLive(queryParams).asFlow()
    }

    override fun onCleared() {
        super.onCleared()
    }

    fun logout() {
        viewModelScope.launch {
            session?.syncService()?.stopSync()
            session?.signOutService()?.signOut(true, false)
        }
    }
}

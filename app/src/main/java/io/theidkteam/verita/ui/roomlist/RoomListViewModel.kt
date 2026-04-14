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
import javax.inject.Inject

@HiltViewModel
class RoomListViewModel @Inject constructor(
    @org.jetbrains.annotations.Nullable private val session: Session?
) : ViewModel() {

    val rooms: Flow<List<RoomSummary>>? = session?.let {
        val queryParams = RoomSummaryQueryParams.Builder().build()
        it.roomService().getRoomSummariesLive(queryParams).asFlow()
    }

    fun logout() {
        viewModelScope.launch {
            session?.signOutService()?.signOut(true, false)
        }
    }
}

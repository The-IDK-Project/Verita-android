package io.theidkteam.verita.ui.roomlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import org.matrix.android.sdk.api.session.Session
import org.matrix.android.sdk.api.session.room.RoomSummaryQueryParams
import org.matrix.android.sdk.api.session.room.model.RoomSummary
import javax.inject.Inject

@HiltViewModel
class RoomListViewModel @Inject constructor(
    private val session: Session?
) : ViewModel() {

    val rooms: Flow<List<RoomSummary>>? = session?.let {
        val queryParams = RoomSummaryQueryParams.Builder().build()
        it.roomService().getRoomSummariesLive(queryParams).asFlow()
    }

    fun logout() {
        session?.authenticationService()?.logout(true)
    }
}

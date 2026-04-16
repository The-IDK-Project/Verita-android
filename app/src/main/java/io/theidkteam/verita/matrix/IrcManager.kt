package io.theidkteam.verita.matrix

import org.matrix.android.sdk.api.session.Session
import org.matrix.android.sdk.api.session.room.Room
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IrcManager @Inject constructor() {
    // Bridge IRC logic would go here
    // Usually via appservice-irc or specific IRC bridges in Matrix
    
    fun isIrcRoom(room: Room): Boolean {
        return room.roomSummary()?.displayName?.contains("irc", ignoreCase = true) == true
    }
}

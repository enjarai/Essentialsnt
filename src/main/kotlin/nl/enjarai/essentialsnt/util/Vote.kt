package nl.enjarai.essentialsnt.util

import java.util.*

class Vote(val successCallback: () -> Unit, val failureCallback: () -> Unit) {
    private val votes: MutableMap<UUID, Boolean> = mutableMapOf()

    fun vote(uuid: UUID, vote: Boolean) {
        votes[uuid] = vote
    }

    fun tryVote(uuid: UUID, vote: Boolean): Boolean {
        if (votes.containsKey(uuid)) {
            return false
        }
        votes[uuid] = vote
        return true
    }

    fun success(): Boolean {
        return votes.values.count { it } > votes.values.count { !it }
    }

    fun end() {
        if (success()) {
            successCallback()
        } else {
            failureCallback()
        }
    }
}
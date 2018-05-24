package matterlink.api

import com.google.gson.GsonBuilder

/**
 * Created by nikky on 07/05/18.
 *
 * @author Nikky
 * @version 1.0
 */
data class ApiMessage (
        var username: String = "",
        var text: String = "",
        var gateway: String = "",
        var channel: String = "",
        var userid: String = "",
        var avatar: String = "",
        var account: String = "",
        var protocol: String = "",
        var event: String = "",
        var id: String? = ""
) {
    fun encode(): String {
        return gson.toJson(this)
    }

    companion object {
        val USER_ACTION = "user_action"
        val JOIN_LEAVE = "join_leave"

        private val gson = GsonBuilder()
                .create()

        fun decode(json: String): ApiMessage {
            return gson.fromJson(json, ApiMessage::class.java)
        }
    }
}

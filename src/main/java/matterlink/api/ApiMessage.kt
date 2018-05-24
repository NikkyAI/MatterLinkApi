package matterlink.api

import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName

/**
 * Created by nikky on 07/05/18.
 *
 * @author Nikky
 * @version 1.0
 */
data class ApiMessage (
        @SerializedName("username") private var _username: String? = null,
        @SerializedName("text")     private var _text: String? = null,
        @SerializedName("gateway")  private var _gateway: String? = null,
        @SerializedName("channel")  private var _channel: String? = null,
        @SerializedName("userid")   private var _userid: String? = null,
        @SerializedName("avatar")   private var _avatar: String? = null,
        @SerializedName("account")  private var _account: String? = null,
        @SerializedName("protocol") private var _protocol: String? = null,
        @SerializedName("event")    private var _event: String? = null,
        @SerializedName("id")       private var _id: String? = null
) {
    var username: String
        get() = _username ?: ""
        set(username) { this._username = username }

    var text: String
        get() = _text ?: ""
        set(text) { this._text = text }
    
    var gateway: String
        get() = _gateway ?: ""
        set(gateway) { this._gateway = gateway }
    
    var channel: String
        get() = _channel ?: ""
        set(channel) { this._channel = channel }
    
    var userid: String
        get() = _userid ?: ""
        set(userid) { this._userid = userid }
    
    var avatar: String
        get() = _avatar ?: ""
        set(avatar) { this._avatar = avatar }
    
    var account: String
        get() = _account ?: ""
        set(account) { this._account = account }

    var protocol: String
        get() = _protocol ?: ""
        set(protocol) { this._protocol = protocol }

    var event: String
        get() = _event ?: ""
        set(event) { this._event = event }

    var id: String
        get() = _id ?: ""
        set(id) { this._id = id }
    
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

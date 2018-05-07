package moe.nikky.matterlink.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Created by nikky on 07/05/18.
 *
 * @author Nikky
 * @version 1.0
 */
public class ApiMessage {
    private String username;
    private String text;
    private String gateway;
    private String channel;
    private String userid;
    private String avatar;
    private String account;
    private String event;
    private String id;

    public ApiMessage() {
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("ApiMessage{");
        sb.append("username='").append(username).append('\'');
        sb.append(", text='").append(text).append('\'');
        sb.append(", gateway='").append(gateway).append('\'');
        sb.append(", channel='").append(channel).append('\'');
        sb.append(", userid='").append(userid).append('\'');
        sb.append(", avatar='").append(avatar).append('\'');
        sb.append(", account='").append(account).append('\'');
        sb.append(", event='").append(event).append('\'');
        sb.append(", id='").append(id).append('\'');
        sb.append('}');
        return sb.toString();
    }

    private static Gson gson = new GsonBuilder()
            .create();

    public static ApiMessage decode(String json) {
        return gson.fromJson(json, ApiMessage.class);
    }

    public String encode() {
        return gson.toJson(this);
    }

    public String getUsername() {
        return username != null ? username : "";
    }

    public ApiMessage setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getText() {
        return text != null ? text : "";
    }

    public ApiMessage setText(String text) {
        this.text = text;
        return this;
    }

    public String getGateway() {
        return gateway != null ? gateway : "";
    }

    public ApiMessage setGateway(String gateway) {
        this.gateway = gateway;
        return this;
    }

    public String getChannel() {
        return channel != null ? channel : "";
    }

    public ApiMessage setChannel(String channel) {
        this.channel = channel;
        return this;
    }

    public String getUserid() {
        return userid != null ? userid : "";
    }

    public ApiMessage setUserid(String userid) {
        this.userid = userid;
        return this;
    }

    public String getAvatar() {
        return avatar != null ? avatar : "";
    }

    public ApiMessage setAvatar(String avatar) {
        this.avatar = avatar;
        return this;
    }

    public String getAccount() {
        return account != null ? account : "";
    }

    public ApiMessage setAccount(String account) {
        this.account = account;
        return this;
    }

    public String getEvent() {
        return event != null ? event : "";
    }

    public ApiMessage setEvent(String event) {
        this.event = event;
        return this;
    }

    public String getId() {
        return id != null ? id : "";
    }

    public ApiMessage setId(String id) {
        this.id = id;
        return this;
    }
}

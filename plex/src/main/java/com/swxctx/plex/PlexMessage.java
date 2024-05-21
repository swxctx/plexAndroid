package com.swxctx.plex;

import com.google.gson.annotations.SerializedName;

/**
 * @Author swxctx
 * @Date 2024-05-20
 * @Describe:
 */
public class PlexMessage {
    @SerializedName("seq")
    private long seq;
    @SerializedName("uri")
    private String uri;
    @SerializedName("body")
    private String body;

    public PlexMessage(String uri, String body) {
        this.uri = uri;
        this.body = body;
    }

    public PlexMessage(String uri) {
        this.uri = uri;
    }

    public long getSeq() {
        return seq;
    }

    public void setSeq(long seq) {
        this.seq = seq;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return "Message{" +
                "seq=" + seq +
                ", uri='" + uri + '\'' +
                ", body='" + body + '\'' +
                '}';
    }
}

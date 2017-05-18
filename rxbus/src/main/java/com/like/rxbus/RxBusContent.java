package com.like.rxbus;

/**
 * 发送内容的包装，避免content为null时，不能发送数据。
 *
 * @param <T>
 */
public class RxBusContent<T> {
    private ContentType contentType;
    private T content;

    public enum ContentType {
        /**
         * 没有数据
         */
        NO_DATA,
        /**
         * 有数据
         */
        HAS_DATA
    }

    public RxBusContent() {
        contentType = ContentType.NO_DATA;
        this.content = null;
    }

    public ContentType getContentType() {
        return contentType;
    }

    public void setContentType(ContentType contentType) {
        this.contentType = contentType;
    }

    public RxBusContent(T content) {
        contentType = ContentType.HAS_DATA;
        this.content = content;
    }

    public T getContent() {
        return content;
    }

    public void setContent(T content) {
        this.content = content;
    }
}

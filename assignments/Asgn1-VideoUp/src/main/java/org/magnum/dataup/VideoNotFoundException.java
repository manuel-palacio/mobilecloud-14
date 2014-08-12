package org.magnum.dataup;


public class VideoNotFoundException extends RuntimeException {

    private final Long id;

    public VideoNotFoundException(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
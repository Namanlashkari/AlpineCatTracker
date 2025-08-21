package com.alpinecattracker;

import java.util.Objects;

/**
 * Represents a cat with an identifier, a display name and a photo URL.
 */
public class Cat {
    private final String id;
    private final String name;
    private final String photoUrl;

    public Cat(String id, String name, String photoUrl) {
        this.id = Objects.requireNonNull(id, "id");
        this.name = Objects.requireNonNull(name, "name");
        this.photoUrl = Objects.requireNonNull(photoUrl, "photoUrl");
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }
}

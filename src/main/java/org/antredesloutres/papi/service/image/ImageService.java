package org.antredesloutres.papi.service.image;

import org.antredesloutres.papi.exception.EntityNotFoundException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class ImageService {

    private final Path root = Paths.get("generated-images");

    public Resource loadImage(String filename) {
        try {
            Path file = root.resolve(filename).normalize().toAbsolutePath();
            if (!file.startsWith(root.toAbsolutePath())) {
                throw new EntityNotFoundException("Image", filename);
            }
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new EntityNotFoundException("Image", filename);
            }
        } catch (MalformedURLException e) {
            throw new EntityNotFoundException("Image", filename);
        }
    }
}

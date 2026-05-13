package org.antredesloutres.papi.service.image;

import org.antredesloutres.papi.exception.EntityNotFoundException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class ImageService {

    private final Path root = Paths.get("generated-images");

    public void saveImage(String filename, BufferedImage image) throws IOException {
        if (!Files.exists(root)) {
            Files.createDirectories(root);
        }
        Path file = root.resolve(filename);
        String format = filename.substring(filename.lastIndexOf('.') + 1);
        ImageIO.write(image, format, file.toFile());
    }

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

package org.antredesloutres.papi.controller;

import lombok.RequiredArgsConstructor;
import org.antredesloutres.papi.service.DatapackService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/datapack")
@RequiredArgsConstructor
public class DatapackController {

    private final DatapackService datapackService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public void uploadDatapack(
            @RequestParam("file") MultipartFile file,
            @RequestParam("tag") String tag) {
        datapackService.importDatapack(file, tag);
    }
}

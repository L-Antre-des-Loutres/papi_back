package org.antredesloutres.papi.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.antredesloutres.papi.dto.domain.TypeMatchupDto;
import org.antredesloutres.papi.dto.response.TypeMatchupResponse;
import org.antredesloutres.papi.mapper.TypeMatchupMapper;
import org.antredesloutres.papi.service.domain.TypeMatchupService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/types/matchups")
@RequiredArgsConstructor
public class TypeMatchupController {

    private final TypeMatchupService matchupService;
    private final TypeMatchupMapper matchupMapper;

    @GetMapping
    public List<TypeMatchupResponse> getAll() {
        return matchupMapper.toResponseList(matchupService.findAll());
    }

    @PutMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void upsert(@Valid @RequestBody TypeMatchupDto dto) {
        matchupService.upsert(dto);
    }

    @DeleteMapping("/{attackingId}/{defendingId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reset(@PathVariable Integer attackingId, @PathVariable Integer defendingId) {
        matchupService.reset(attackingId, defendingId);
    }
}

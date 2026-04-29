package org.antredesloutres.papi.controller;

import lombok.RequiredArgsConstructor;
import org.antredesloutres.papi.dto.response.MovesetResponse;
import org.antredesloutres.papi.mapper.MovesetMapper;
import org.antredesloutres.papi.service.domain.MovesetService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/movesets")
@RequiredArgsConstructor
public class MovesetController {

    private final MovesetService movesetService;
    private final MovesetMapper movesetMapper;

    @GetMapping
    public Page<MovesetResponse> getAll(@PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        return movesetService.getAllMovesets(pageable).map(movesetMapper::toResponse);
    }

    @GetMapping("/{id}")
    public MovesetResponse getById(@PathVariable Integer id) {
        return movesetMapper.toResponse(movesetService.getMovesetById(id));
    }

    @GetMapping("/pkmn/{pkmnId}")
    public List<MovesetResponse> getByPkmnId(@PathVariable Integer pkmnId) {
        return movesetMapper.toResponseList(movesetService.getPkmnMovesetByPkmnId(pkmnId));
    }
}

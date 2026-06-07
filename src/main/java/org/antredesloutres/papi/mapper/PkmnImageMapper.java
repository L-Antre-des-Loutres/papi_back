package org.antredesloutres.papi.mapper;

import org.antredesloutres.papi.dto.response.PkmnImageResponse;
import org.antredesloutres.papi.model.domain.PkmnImage;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PkmnImageMapper {

    PkmnImageResponse toResponse(PkmnImage image);

    List<PkmnImageResponse> toResponseList(List<PkmnImage> images);
}

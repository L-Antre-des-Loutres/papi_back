package org.antredesloutres.papi.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.antredesloutres.papi.dto.auth.LoginRequest;
import org.antredesloutres.papi.dto.domain.MovesetRequest;
import org.antredesloutres.papi.dto.domain.PkmnTranslationRequest;
import org.antredesloutres.papi.dto.domain.PkmnUpdateDto;
import org.antredesloutres.papi.dto.domain.TypeCreateRequest;
import org.antredesloutres.papi.dto.domain.TypeMatchupDto;
import org.antredesloutres.papi.dto.domain.UserCreateRequest;
import org.antredesloutres.papi.dto.domain.UserUpdateRequest;
import org.antredesloutres.papi.dto.request.ColorRequest;
import org.antredesloutres.papi.dto.request.DescribedTranslationRequest;
import org.antredesloutres.papi.dto.request.IntValueRequest;
import org.antredesloutres.papi.dto.request.NameTranslationRequest;
import org.antredesloutres.papi.dto.request.SymbolRequest;
import org.antredesloutres.papi.dto.request.TagRequest;
import org.antredesloutres.papi.dto.request.TagsRequest;
import org.antredesloutres.papi.model.enumerated.Effectiveness;
import org.antredesloutres.papi.model.enumerated.MoveLearnMethod;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class DtoValidationTest {

    private static final Validator VALIDATOR =
            Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void loginRequest_acceptsValid() {
        // arrange
        LoginRequest req = new LoginRequest("alice", "pwd");

        // act
        Set<ConstraintViolation<LoginRequest>> violations = VALIDATOR.validate(req);

        // assert
        assertThat(violations).isEmpty();
    }

    @Test
    void loginRequest_rejectsBlankUsername() {
        // arrange
        LoginRequest req = new LoginRequest("", "pwd");

        // act
        Set<ConstraintViolation<LoginRequest>> violations = VALIDATOR.validate(req);

        // assert
        assertThat(violations).extracting(v -> v.getPropertyPath().toString()).contains("username");
    }

    @Test
    void loginRequest_rejectsNullPassword() {
        // arrange
        LoginRequest req = new LoginRequest("alice", null);

        // act
        Set<ConstraintViolation<LoginRequest>> violations = VALIDATOR.validate(req);

        // assert
        assertThat(violations).extracting(v -> v.getPropertyPath().toString()).contains("password");
    }

    @Test
    void movesetRequest_acceptsValid() {
        // arrange
        MovesetRequest req = new MovesetRequest(1, MoveLearnMethod.LEVEL_UP, 5);

        // act
        Set<ConstraintViolation<MovesetRequest>> violations = VALIDATOR.validate(req);

        // assert
        assertThat(violations).isEmpty();
    }

    @Test
    void movesetRequest_rejectsNullMoveId() {
        // arrange
        MovesetRequest req = new MovesetRequest(null, MoveLearnMethod.LEVEL_UP, 5);

        // act
        Set<ConstraintViolation<MovesetRequest>> violations = VALIDATOR.validate(req);

        // assert
        assertThat(violations).extracting(v -> v.getPropertyPath().toString()).contains("moveId");
    }

    @Test
    void movesetRequest_rejectsLearnLevelLessThanOne() {
        // arrange
        MovesetRequest req = new MovesetRequest(1, MoveLearnMethod.LEVEL_UP, 0);

        // act
        Set<ConstraintViolation<MovesetRequest>> violations = VALIDATOR.validate(req);

        // assert
        assertThat(violations).extracting(v -> v.getPropertyPath().toString()).contains("learnLevel");
    }

    @Test
    void movesetRequest_acceptsNullLearnLevel() {
        // arrange
        MovesetRequest req = new MovesetRequest(1, MoveLearnMethod.MACHINE, null);

        // act
        Set<ConstraintViolation<MovesetRequest>> violations = VALIDATOR.validate(req);

        // assert
        assertThat(violations).isEmpty();
    }

    @Test
    void typeMatchupDto_rejectsNullFields() {
        // arrange
        TypeMatchupDto dto = new TypeMatchupDto(null, null, null);

        // act
        Set<ConstraintViolation<TypeMatchupDto>> violations = VALIDATOR.validate(dto);

        // assert
        assertThat(violations).extracting(v -> v.getPropertyPath().toString())
                .containsExactlyInAnyOrder("attackingTypeId", "defendingTypeId", "effectiveness");
    }

    @Test
    void typeMatchupDto_acceptsValid() {
        // arrange
        TypeMatchupDto dto = new TypeMatchupDto(1, 2, Effectiveness.SUPER_EFFECTIVE);

        // act
        Set<ConstraintViolation<TypeMatchupDto>> violations = VALIDATOR.validate(dto);

        // assert
        assertThat(violations).isEmpty();
    }

    @Test
    void userCreateRequest_rejectsShortUsername() {
        // arrange
        UserCreateRequest req = new UserCreateRequest("ab", "strongpass", null);

        // act
        Set<ConstraintViolation<UserCreateRequest>> violations = VALIDATOR.validate(req);

        // assert
        assertThat(violations).extracting(v -> v.getPropertyPath().toString()).contains("username");
    }

    @Test
    void userCreateRequest_rejectsShortPassword() {
        // arrange
        UserCreateRequest req = new UserCreateRequest("alice", "short", null);

        // act
        Set<ConstraintViolation<UserCreateRequest>> violations = VALIDATOR.validate(req);

        // assert
        assertThat(violations).extracting(v -> v.getPropertyPath().toString()).contains("password");
    }

    @Test
    void userCreateRequest_acceptsValid() {
        // arrange
        UserCreateRequest req = new UserCreateRequest("alice", "strongpass", "ROLE_USER");

        // act
        Set<ConstraintViolation<UserCreateRequest>> violations = VALIDATOR.validate(req);

        // assert
        assertThat(violations).isEmpty();
    }

    @Test
    void userUpdateRequest_acceptsAllNull() {
        // arrange
        UserUpdateRequest req = new UserUpdateRequest(null, null, null);

        // act
        Set<ConstraintViolation<UserUpdateRequest>> violations = VALIDATOR.validate(req);

        // assert
        assertThat(violations).isEmpty();
    }

    @Test
    void userUpdateRequest_rejectsTooLongUsername() {
        // arrange
        UserUpdateRequest req = new UserUpdateRequest("a".repeat(51), null, null);

        // act
        Set<ConstraintViolation<UserUpdateRequest>> violations = VALIDATOR.validate(req);

        // assert
        assertThat(violations).extracting(v -> v.getPropertyPath().toString()).contains("username");
    }

    @Test
    void typeCreateRequest_acceptsValidColorWithoutHash() {
        // arrange
        TypeCreateRequest req = new TypeCreateRequest("fairy", "ee99ac", "Fairy");

        // act
        Set<ConstraintViolation<TypeCreateRequest>> violations = VALIDATOR.validate(req);

        // assert
        assertThat(violations).isEmpty();
    }

    @Test
    void typeCreateRequest_rejectsMalformedColor() {
        // arrange
        TypeCreateRequest req = new TypeCreateRequest("fairy", "not-a-color", "Fairy");

        // act
        Set<ConstraintViolation<TypeCreateRequest>> violations = VALIDATOR.validate(req);

        // assert
        assertThat(violations).extracting(v -> v.getPropertyPath().toString()).contains("color");
    }

    @Test
    void typeCreateRequest_rejectsBlankSymbol() {
        // arrange
        TypeCreateRequest req = new TypeCreateRequest("", "#abcdef", "X");

        // act
        Set<ConstraintViolation<TypeCreateRequest>> violations = VALIDATOR.validate(req);

        // assert
        assertThat(violations).extracting(v -> v.getPropertyPath().toString()).contains("symbol");
    }

    @Test
    void colorRequest_rejectsBlank() {
        // arrange
        ColorRequest req = new ColorRequest("");

        // act
        Set<ConstraintViolation<ColorRequest>> violations = VALIDATOR.validate(req);

        // assert
        assertThat(violations).isNotEmpty();
    }

    @Test
    void symbolRequest_rejectsBlank() {
        // arrange
        SymbolRequest req = new SymbolRequest("");

        // act
        Set<ConstraintViolation<SymbolRequest>> violations = VALIDATOR.validate(req);

        // assert
        assertThat(violations).isNotEmpty();
    }

    @Test
    void tagRequest_rejectsBlank() {
        // arrange
        TagRequest req = new TagRequest(" ");

        // act
        Set<ConstraintViolation<TagRequest>> violations = VALIDATOR.validate(req);

        // assert
        assertThat(violations).isNotEmpty();
    }

    @Test
    void tagsRequest_rejectsNullTags() {
        // arrange
        TagsRequest req = new TagsRequest(null);

        // act
        Set<ConstraintViolation<TagsRequest>> violations = VALIDATOR.validate(req);

        // assert
        assertThat(violations).extracting(v -> v.getPropertyPath().toString()).contains("tags");
    }

    @Test
    void intValueRequest_rejectsNullValue() {
        // arrange
        IntValueRequest req = new IntValueRequest(null);

        // act
        Set<ConstraintViolation<IntValueRequest>> violations = VALIDATOR.validate(req);

        // assert
        assertThat(violations).extracting(v -> v.getPropertyPath().toString()).contains("value");
    }

    @Test
    void nameTranslationRequest_rejectsBlank() {
        // arrange
        NameTranslationRequest req = new NameTranslationRequest("");

        // act
        Set<ConstraintViolation<NameTranslationRequest>> violations = VALIDATOR.validate(req);

        // assert
        assertThat(violations).isNotEmpty();
    }

    @Test
    void describedTranslationRequest_rejectsBlankName() {
        // arrange
        DescribedTranslationRequest req = new DescribedTranslationRequest("", "desc");

        // act
        Set<ConstraintViolation<DescribedTranslationRequest>> violations = VALIDATOR.validate(req);

        // assert
        assertThat(violations).extracting(v -> v.getPropertyPath().toString()).contains("name");
    }

    @Test
    void describedTranslationRequest_acceptsNullDescription() {
        // arrange
        DescribedTranslationRequest req = new DescribedTranslationRequest("Tackle", null);

        // act
        Set<ConstraintViolation<DescribedTranslationRequest>> violations = VALIDATOR.validate(req);

        // assert
        assertThat(violations).isEmpty();
    }

    @Test
    void pkmnTranslationRequest_rejectsBlankPkmnName() {
        // arrange
        PkmnTranslationRequest req = new PkmnTranslationRequest("", "Normal", "desc");

        // act
        Set<ConstraintViolation<PkmnTranslationRequest>> violations = VALIDATOR.validate(req);

        // assert
        assertThat(violations).extracting(v -> v.getPropertyPath().toString()).contains("pkmnName");
    }

    @Test
    void pkmnUpdateDto_rejectsStatOver255() {
        // arrange
        PkmnUpdateDto dto = new PkmnUpdateDto();
        dto.setBaseHp(256);

        // act
        Set<ConstraintViolation<PkmnUpdateDto>> violations = VALIDATOR.validate(dto);

        // assert
        assertThat(violations).extracting(v -> v.getPropertyPath().toString()).contains("baseHp");
    }

    @Test
    void pkmnUpdateDto_rejectsEvOver3() {
        // arrange
        PkmnUpdateDto dto = new PkmnUpdateDto();
        dto.setEvHp(5);

        // act
        Set<ConstraintViolation<PkmnUpdateDto>> violations = VALIDATOR.validate(dto);

        // assert
        assertThat(violations).extracting(v -> v.getPropertyPath().toString()).contains("evHp");
    }

    @Test
    void pkmnUpdateDto_rejectsMaleRatioOutOfRange() {
        // arrange
        PkmnUpdateDto dto = new PkmnUpdateDto();
        dto.setMaleRatio(150);

        // act
        Set<ConstraintViolation<PkmnUpdateDto>> violations = VALIDATOR.validate(dto);

        // assert
        assertThat(violations).extracting(v -> v.getPropertyPath().toString()).contains("maleRatio");
    }

    @Test
    void pkmnUpdateDto_acceptsMaleRatioMinusOneForGenderless() {
        // arrange
        PkmnUpdateDto dto = new PkmnUpdateDto();
        dto.setMaleRatio(-1);

        // act
        Set<ConstraintViolation<PkmnUpdateDto>> violations = VALIDATOR.validate(dto);

        // assert
        assertThat(violations).isEmpty();
    }

    @Test
    void pkmnUpdateDto_acceptsAllNullFields() {
        // arrange
        PkmnUpdateDto dto = new PkmnUpdateDto();

        // act
        Set<ConstraintViolation<PkmnUpdateDto>> violations = VALIDATOR.validate(dto);

        // assert
        assertThat(violations).isEmpty();
    }
}

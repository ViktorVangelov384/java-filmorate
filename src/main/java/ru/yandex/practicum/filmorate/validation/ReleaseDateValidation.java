package ru.yandex.practicum.filmorate.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import ru.yandex.practicum.filmorate.controller.FilmController;

import java.time.LocalDate;

public class ReleaseDateValidation implements ConstraintValidator<ReleaseValidDate, LocalDate> {
    private static final LocalDate MIN_DATE = FilmController.MIN_RELEASE_DATE;

    @Override
    public boolean isValid(LocalDate value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        return !value.isBefore(MIN_DATE);
    }
}

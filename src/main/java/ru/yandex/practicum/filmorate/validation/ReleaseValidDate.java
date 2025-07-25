package ru.yandex.practicum.filmorate.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ReleaseDateValidation.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ReleaseValidDate {
    String message() default "Дата релиза не может быть раньше 1895-12-28";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}


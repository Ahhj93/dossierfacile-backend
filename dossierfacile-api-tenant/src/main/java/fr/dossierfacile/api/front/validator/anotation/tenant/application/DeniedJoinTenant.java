package fr.dossierfacile.api.front.validator.anotation.tenant.application;

import fr.dossierfacile.api.front.validator.tenant.application.DeniedJoinTenantValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Deprecated
@Target({ElementType.TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(
        validatedBy = {DeniedJoinTenantValidator.class}
)
public @interface DeniedJoinTenant {
    String message() default "join tenant cannot access to this step";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

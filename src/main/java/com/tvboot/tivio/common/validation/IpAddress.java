package com.tvboot.tivio.common.validation;
//https://www.geeksforgeeks.org/how-to-validate-an-ip-address-using-regular-expressions-in-java/

//https://elyor.hashnode.dev/custom-validator-with-spring-boot

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = IpAddressValidator.class)
@Documented
public @interface IpAddress {
        String message() default "Not valid IP address";
        Class<?>[] groups() default {};
        Class<? extends Payload>[] payload() default {};
}

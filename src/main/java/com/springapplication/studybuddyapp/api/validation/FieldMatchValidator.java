package com.springapplication.studybuddyapp.api.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Objects;

public class FieldMatchValidator implements ConstraintValidator<FieldMatch, Object> {
    private String first;
    private String second;

    @Override
    public void initialize(FieldMatch annotation) {
        this.first = annotation.first();
        this.second = annotation.second();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        try {
            PropertyDescriptor[] pds = Introspector.getBeanInfo(value.getClass()).getPropertyDescriptors();
            Object firstVal = null;
            Object secondVal = null;
            for (PropertyDescriptor pd : pds) {
                if (pd.getName().equals(first)) {
                    Method getter = pd.getReadMethod();
                    firstVal = getter != null ? getter.invoke(value) : null;
                }
                if (pd.getName().equals(second)) {
                    Method getter = pd.getReadMethod();
                    secondVal = getter != null ? getter.invoke(value) : null;
                }
            }
            boolean matches = Objects.equals(firstVal, secondVal);
            if (!matches) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                        .addPropertyNode(second)
                        .addConstraintViolation();
            }
            return matches;
        } catch (Exception ex) {
            return false;
        }
    }
}


package fr.esgi.fx.kanban.servlet;

import java.lang.reflect.Field;

final class ServletTestUtils {

    private ServletTestUtils() {
    }

    static void setField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Impossible d'injecter le champ de test: " + fieldName, e);
        }
    }
}


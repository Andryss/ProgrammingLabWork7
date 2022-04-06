package MovieObjects;

import java.io.Serializable;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>Special annotation for reflecting Movie class in runtime</p>
 * <p>With that you can easier add new fields in Movie objects</p>
 * <p>It's funny</p>
 * @see java.lang.reflect
 * @see Movie
 * @see Commands.ElementCommand
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FieldSetter {
    String fieldName();
    String example();
    int index();
}

package general.commands;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Special annotation, which label all commands (to create the command you need to "annotate" constructor
 * with only one <code>java.lang.String</code> argument and implement the <code>Command</code> interface)
 */
@Target(ElementType.CONSTRUCTOR)
@Retention(RetentionPolicy.RUNTIME)
public @interface ParseCommand {
    String name();
    String example();
}

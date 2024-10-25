package ace.model.decorator;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface IFieldInfo
{
    String fieldName();

    Class<?> fieldType();
}


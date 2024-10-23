package ace.model.decorator;

import ace.model.interfaces.IDbItem;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class FieldInfo
{
    public String FieldName;
    public Class<?> FieldType;

    public FieldInfo(String fieldName, Class<?> fieldType)
    {
        FieldName = fieldName;
        FieldType = fieldType;
    }

    public static <T extends IDbItem> List<FieldInfo> getFieldInformationFromClass(Class<T> tClass){
        Field[] fields = tClass.getDeclaredFields();
        List<FieldInfo> annotatedFields = new ArrayList<>();
        for (Field field : fields)
        {
            if (field.isAnnotationPresent(IFieldInfo.class))
            {
                IFieldInfo fieldInfo = field.getAnnotation(IFieldInfo.class);

                String fieldName = fieldInfo.fieldName();
                Class<?> fieldType = fieldInfo.fieldType();
                annotatedFields.add(new FieldInfo(fieldName, fieldType));
            }
        }

        return annotatedFields;
    }
}

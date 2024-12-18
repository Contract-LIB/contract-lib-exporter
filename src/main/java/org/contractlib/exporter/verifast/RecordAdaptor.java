package org.contractlib.exporter.verifast;

import org.stringtemplate.v4.Interpreter;
import org.stringtemplate.v4.ModelAdaptor;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.misc.STNoSuchPropertyException;

import java.lang.reflect.Method;

public class RecordAdaptor implements ModelAdaptor<Record> {
    @Override
    public Object getProperty(Interpreter interp, ST self, Record model, Object property, String propertyName) throws STNoSuchPropertyException {

        if (model == null) {
            throw new NullPointerException("o");
        }

        Class<?> c = model.getClass();

        if (property==null) {
            return throwNoSuchProperty(c, propertyName, null);
        }

        try {
            Method method = c.getMethod(propertyName);
            return method.invoke(model);
        } catch (Exception e) {
            throwNoSuchProperty(c, propertyName, e);
        }
        // unreachable ...
        return null;
    }

    protected Object throwNoSuchProperty(Class<?> clazz, String propertyName, Exception cause) {
        throw new STNoSuchPropertyException(cause, null, clazz.getName() + "." + propertyName);
    }
}

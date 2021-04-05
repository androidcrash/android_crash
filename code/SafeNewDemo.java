package com.company;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class SafeNewDemo {

    public SafeNewDemo() throws InvocationTargetException, NoSuchMethodException, InstantiationException, InterruptedException, IllegalAccessException {
        String str = (String) safeNew(String.class, "Safe New Demo");
        System.out.println(str);
    }

    public Object safeNew(Class<?> classType, Object... args) throws NoSuchMethodException, IllegalAccessException, InstantiationException, InvocationTargetException, InterruptedException {
        Class<?>[] argTypes = new Class<?>[args.length];
        for (int i = 0; i < args.length; i++) {
            argTypes[i] = args[i].getClass();
        }
        return safeNewInternal(0, classType, argTypes, args);
    }

    private Object safeNewInternal(int times, Class<?> classType, Class<?>[] argTypes, Object... args) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, InterruptedException {
        if (times < 5) {
            try {
                Constructor<?> constructor = classType.getConstructor(argTypes);
                return constructor.newInstance(args);
            } catch (OutOfMemoryError e) {
                System.gc();
                Thread.sleep(1000);
                times++;
                return safeNewInternal(times, classType, argTypes, args);
            }
        } else {
            Constructor<?> constructor = classType.getConstructor(argTypes);
            return constructor.newInstance(args);
        }
    }
}

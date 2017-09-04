package com.demonwav.clash;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import sun.misc.Unsafe;

public class Clash {

    public static <T> T init(final Class<T> clazz, final String[] args) {
        final T t;
        try {
            //noinspection unchecked
            t = (T) getUnsafe().allocateInstance(clazz);
        } catch (InstantiationException e) {
            throw new ClashException(e);
        }

        final Field[] fields = getRelevantFields(clazz);
        verifyArgNames(fields);
        final Map<String, Field> argMap = mapArgumentsToFields(fields);

        for (int i = 0; i < args.length; i++) {
            if (!args[i].startsWith("-")) {
                continue;
            }

            final String argument;
            final String value;

            if (args[i].startsWith("--")) {
                final int index = args[i].indexOf('=');
                if (index == -1) {
                    throw new ClashException("long-form arguments must specify value with '='!");
                }

                argument = args[i].substring(2, index);

                if (index == args[i].length()) {
                    value = "";
                } else {
                    value = args[i].substring(index + 1);
                }
            } else {
                argument = args[i].substring(1);
                value = args[++i];
            }

            final Field field = argMap.get(argument);
            if (field == null) {
                throw new ClashException("Unknown argument: " + argument);
            }

            argMap.entrySet().removeIf(entry -> entry.getValue() == field);

            try {
                initializeValue(field, t, value);
            } catch (IllegalAccessException | NoSuchFieldException | InstantiationException e) {
                throw new ClashException(e);
            }
        }

        for (Field field : argMap.values()) {
            final Argument argument = field.getAnnotation(Argument.class);
            assert argument != null;

            try {
                if (!argument.defaultCreator().isInterface()) {
                    setField(field, t, init(argument.defaultCreator()).createDefault(field.getName()));
                } else if (!argument.defaultValue().equals("")) {
                    initializeValue(field, t, argument.defaultValue());
                } else if (argument.required()) {
                    throw new ClashException("Required argument not provided: " + argument.shortNames()[0]);
                }
            } catch (IllegalAccessException | InstantiationException | NoSuchFieldException e) {
                throw new ClashException(e);
            }
        }

        final Method[] methods = getRelevantMethods(clazz);
        for (Method method : methods) {
            try {
                method.setAccessible(true);
                method.invoke(t);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new ClashException(e);
            }
        }

        return t;
    }

    private static Field[] getRelevantFields(final Class<?> clazz) {
        return getAll(Field.class, clazz, Class::getDeclaredFields, f ->
            f.getAnnotation(Argument.class) != null &&
            !Modifier.isTransient(f.getModifiers()) &&
            !Modifier.isStatic(f.getModifiers())
        );
    }

    private static Method[] getRelevantMethods(final Class<?> clazz) {
        return getAll(Method.class, clazz, Class::getDeclaredMethods, m ->
            m.getAnnotation(Init.class) != null &&
            !Modifier.isStatic(m.getModifiers())
        );
    }

    private static <T> T[] getAll(final Class<T> arrayClazz, final Class<?> beanClazz, final Function<Class<?>, T[]> func, final Predicate<T> tester) {
        final T[] stuff = func.apply(beanClazz);

        // Filter out invalid things
        //noinspection unchecked
        T[] stuff2 = (T[]) Array.newInstance(arrayClazz, stuff.length);
        int counter = 0;
        for (T t : stuff) {
            if (tester.test(t)) {
                stuff2[counter++] = t;
            }
        }
        stuff2 = Arrays.copyOf(stuff2, counter);

        final Class<?> superClazz = beanClazz.getSuperclass();

        if (superClazz == null) {
            return stuff2;
        }

        final T[] moreStuff = getAll(arrayClazz, superClazz, func, tester);
        if (moreStuff.length == 0) {
            return stuff2;
        }

        //noinspection unchecked
        final T[] newStuff = (T[]) Array.newInstance(arrayClazz, stuff2.length + moreStuff.length);
        System.arraycopy(stuff2, 0, newStuff, 0, stuff2.length);
        System.arraycopy(moreStuff, 0, newStuff, stuff2.length, moreStuff.length);

        return newStuff;
    }

    private static void verifyArgNames(final Field[] fields) {
        final String dashMessage = "Argument name must not begin with a dash (-)! : ";
        final String whitespaceMessage = "Argument name must not contain whitespace! : ";
        final Pattern pattern = Pattern.compile("\\s");

        for (Field field : fields) {
            final Argument annotation = field.getAnnotation(Argument.class);
            assert annotation != null;

            for (String name : annotation.shortNames()) {
                if (name.startsWith("-")) {
                    throw new ClashException.Dashes(dashMessage + name);
                }
                if (pattern.matcher(name).find()) {
                    throw new ClashException.Whitespace(whitespaceMessage + name);
                }
            }

            for (String name : annotation.longNames()) {
                if (name.startsWith("-")) {
                    throw new ClashException.Dashes(dashMessage + name);
                }

                if (pattern.matcher(name).find()) {
                    throw new ClashException.Whitespace(whitespaceMessage + name);
                }
            }
        }
    }

    private static Map<String, Field> mapArgumentsToFields(final Field[] fields) {
        final Map<String, Field> map = new HashMap<>(fields.length);

        for (Field field : fields) {
            final Argument annotation = field.getAnnotation(Argument.class);
            assert annotation != null;

            for (String name : annotation.shortNames()) {
                map.put(name, field);
            }
            for (String name : annotation.longNames()) {
                map.put(name, field);
            }
        }

        return map;
    }

    private static void initializeValue(final Field field, final Object object, final String value)
            throws IllegalAccessException, NoSuchFieldException, InstantiationException {

        final Class<?> type = field.getType();
        field.setAccessible(true);

        final Argument annotation = field.getAnnotation(Argument.class);
        assert annotation != null;

        if (!annotation.initializer().isInterface()) {
            final Initializer initializer = init(annotation.initializer());
            final Object objectValue = initializer.initialize(field.getName(), value);
            setField(field, object, objectValue);
        } else {
            final Object objectValue = getFieldValue(type, field, value);
            setField(field, object, objectValue);
        }
    }

    @SuppressWarnings("unchecked") private static Object getFieldValue(final Class<?> type, final Field field, final String value) {
        // primitives aren't covered by their boxed classes in these checks
        // String first because it's probably very common
        if (type.isAssignableFrom(String.class)) {
            return value;
        // Primitives and Boxed types
        } else if (type == byte.class || type == Byte.class) {
            return Byte.valueOf(value);
        } else if (type == short.class || type == Short.class) {
            return Short.valueOf(value);
        } else if (type == int.class || type == Integer.class) {
            return Integer.valueOf(value);
        } else if (type == long.class || type == Long.class) {
            return Long.valueOf(value);
        } else if (type == float.class || type == Float.class) {
            return Float.valueOf(value);
        } else if (type == double.class || type == Double.class) {
            return Double.valueOf(value);
        } else if (type == boolean.class || type == Boolean.class) {
            return Boolean.valueOf(value);
        } else if (type == char.class || type == Character.class) {
            return value.charAt(0);
        // Other Number children
        } else if (type == BigInteger.class) {
            return new BigInteger(value);
        } else if (type == BigDecimal.class) {
            return new BigDecimal(value);
        } else if (type == AtomicInteger.class) {
            return new AtomicInteger(Integer.valueOf(value));
        } else if (type == AtomicLong.class) {
            return new AtomicLong(Long.valueOf(value));
        // Number
        } else if (type == Number.class) {
            // Default to BigDecimal since it's most likely to be able to hold whatever input is given
            return new BigDecimal(value);
        // Enum
        } else if (type.isEnum()) {
            return getEnum(value, type);
        // Primitive arrays
        } else if (type == byte[].class) {
            return handleArray(byte.class, value, Byte::valueOf);
        } else if (type == short[].class) {
            return handleArray(short.class, value, Short::valueOf);
        } else if (type == int[].class) {
            return handleArray(int.class, value, Integer::valueOf);
        } else if (type == long[].class) {
            return handleArray(long.class, value, Long::valueOf);
        } else if (type == float[].class) {
            return handleArray(float.class, value, Float::valueOf);
        } else if (type == double[].class) {
            return handleArray(double.class, value, Double::valueOf);
        } else if (type == boolean[].class) {
            return handleArray(boolean.class, value, Boolean::valueOf);
        } else if (type == char[].class) {
            final String trimmed = value.trim();
            final char s = trimmed.charAt(0);
            final char e = trimmed.charAt(trimmed.length() - 1);
            final boolean isArray = (s == '[' || s == '(' || s == '{') && (e == ']' || e == ')' || e == '}');
            if (isArray) {
                return handleArray(char.class, value, string -> string.charAt(0));
            } else {
                return value.toCharArray();
            }
        // Boxed arrays
        } else if (type == Byte[].class) {
            return handleArray(Byte.class, value, Byte::valueOf);
        } else if (type == Short[].class) {
            return handleArray(Short.class, value, Short::valueOf);
        } else if (type == Integer[].class) {
            return handleArray(Integer.class, value, Integer::valueOf);
        } else if (type == Long[].class) {
            return handleArray(Long.class, value, Long::valueOf);
        } else if (type == Float[].class) {
            return handleArray(Float.class, value, Float::valueOf);
        } else if (type == Double[].class) {
            return handleArray(Double.class, value, Double::valueOf);
        } else if (type == Boolean[].class) {
            return handleArray(Boolean.class, value, Double::valueOf);
        } else if (type == Character[].class) {
            final String trimmed = value.trim();
            final char s = trimmed.charAt(0);
            final char e = trimmed.charAt(trimmed.length() - 1);
            final boolean isArray = (s == '[' || s == '(' || s == '{') && (e == ']' || e == ')' || e == '}');
            if (isArray) {
                return handleArray(Character.class, value, string -> string.charAt(0));
            } else {
                return value.toCharArray();
            }
        // Other Number arrays
        } else if (type == BigInteger[].class) {
            return handleArray(BigInteger.class, value, BigInteger::new);
        } else if (type == BigDecimal[].class) {
            return  handleArray(BigDecimal.class, value, BigDecimal::new);
        } else if (type == AtomicInteger[].class) {
            return handleArray(AtomicInteger.class, value, s -> new AtomicInteger(Integer.valueOf(s)));
        } else if (type == AtomicLong[].class) {
            return handleArray(AtomicLong.class, value, s -> new AtomicLong(Long.valueOf(s)));
        } else if (type == Number[].class) {
            // Default to array of BigDecimal's, since it's most likely to be able to hold whatever input is given
            return handleArray(Number.class, value, BigDecimal::new);
        // Other arrays
        } else if (type == String[].class) {
            return handleArray(String.class, value, Function.identity());
        // Array of enums
        } else if (type.isArray() && type.getComponentType().isEnum()) {
            return handleArray(type.getComponentType(), value, s -> getEnum(s, type.getComponentType()));
        // Other cases
        } else if (List.class.isAssignableFrom(type)) {
            final Type genericType = field.getGenericType();
            if (genericType instanceof ParameterizedType) {
                final ParameterizedType paramType = (ParameterizedType) genericType;
                final Class<?> listType = (Class<?>) paramType.getActualTypeArguments()[0];
                final Object array = handleArray(listType, value, string -> getFieldValue(listType, field, string));
                final ArrayList list = new ArrayList();
                final int length = Array.getLength(array);
                for (int i = 0; i < length; i++) {
                    list.add(Array.get(array, i));
                }
                return list;
            } else {
                final Object array = handleArray(Object.class, value, Function.identity());
                final ArrayList list = new ArrayList();
                final int length = Array.getLength(array);
                for (int i = 0; i < length; i++) {
                    list.add(Array.get(array, i));
                }
                return list;
            }
        } else if (Set.class.isAssignableFrom(type)) {
            final Type genericType = field.getGenericType();
            if (genericType instanceof ParameterizedType) {
                final ParameterizedType paramType = (ParameterizedType) genericType;
                final Class<?> listType = (Class<?>) paramType.getActualTypeArguments()[0];
                final Object array = handleArray(listType, value, string -> getFieldValue(listType, field, string));
                final HashSet set = new HashSet();
                final int length = Array.getLength(array);
                for (int i = 0; i < length; i++) {
                    set.add(Array.get(array, i));
                }
                return set;
            } else {
                final Object array = handleArray(Object.class, value, Function.identity());
                final HashSet set = new HashSet();
                final int length = Array.getLength(array);
                for (int i = 0; i < length; i++) {
                    set.add(Array.get(array, i));
                }
                return set;
            }
        } else {
            throw new IllegalStateException("Cannot map field of type '" + type + "'");
        }
    }

    private static Object handleArray(final Class<?> arrayClass, final String arg, final Function<String, ?> func) {
        final String trimmed = arg.trim();
        final String noBrackets = trimmed.replaceAll("(^[\\[({]\\s*|\\s*[])}]$)", "");
        final String[] items;
        if (noBrackets.contains(",")) {
            items = noBrackets.split(",");
        } else {
            items = noBrackets.split("\\s+");
        }

        final Object array = Array.newInstance(arrayClass, items.length);

        for (int i = 0; i < items.length; i++) {
            Array.set(array, i, func.apply(items[i]));
        }

        return array;
    }

    private static void setField(final Field field, final Object object, final Object value) throws IllegalAccessException, NoSuchFieldException {
        field.setAccessible(true);

        if (Modifier.isFinal(field.getModifiers())) {
            // We can set final fields, so why not
            final Field modifiers = field.getClass().getDeclaredField("modifiers");
            modifiers.setAccessible(true);
            modifiers.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        }

        field.set(object, value);
    }

    private static Enum<?> getEnum(final String value, final Class<?> type) {
        final String cleanedValue = value.trim().replaceAll("\\s+", "_");
        final Enum<?>[] constants = (Enum<?>[]) type.getEnumConstants();
        for (Enum<?> constant : constants) {
            if (constant.name().equalsIgnoreCase(cleanedValue)) {
                return constant;
            }
        }
        throw new ClashException("Could not match '" + value + "' with the a value for " + type.getName());
    }

    private static Unsafe getUnsafe() {
        try {
            final Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            return (Unsafe) theUnsafe.get(null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new ClashException(e);
        }
    }

    private static <T> T init(Class<T> clazz) {
        try {
            final Constructor<T> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (NoSuchMethodException | InstantiationException | InvocationTargetException | IllegalAccessException e) {
            throw new ClashException(e);
        }
    }
}

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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import sun.misc.Unsafe;

public final class Clash {

    /*
     * Impl Note: Auto-(un)boxing is intentionally not used in this class as I feel it's best kept explicit as we're
     * dealing with a lot of reflection / unsafe. IntelliJ can be configured to mark Auto-(un)boxing as an error, though
     * unfortunately we can't enable javac to do that, so this is up to the developer to maintain.
     */

    private static final Unsafe unsafe = getUnsafe();

    private static final Pattern arrayPattern = Pattern.compile("(^[\\[({]\\s*|\\s*[])}]$)");

    private static final Pattern whitespacePattern = Pattern.compile("\\s+");

    static final String ARGUMENT_DEFAULT_VALUE = "__CLASH_ARGUMENT_DEF_VAL";

    private Clash() {}

    public static <T> T init(final Class<T> clazz, final String[] args) {
        final T t;
        try {
            //noinspection unchecked
            t = (T) unsafe.allocateInstance(clazz);
        } catch (final InstantiationException e) {
            throw new ClashException(e);
        }

        final Field[] fields = getRelevantFields(clazz);
        verifyArgNames(fields);
        final Map<String, Field> argMap = mapArgumentsToFields(fields);

        initializeGivenArguments(args, argMap, t);
        initializeDefaultValues(argMap, t);

        runInitMethods(clazz, t);

        return t;
    }

    private static Field[] getRelevantFields(final Class<?> clazz) {
        return getAll(null, 0, Field[]::new, clazz, Class::getDeclaredFields, f ->
            f.getAnnotation(Argument.class) != null &&
            !Modifier.isTransient(f.getModifiers()) &&
            !Modifier.isStatic(f.getModifiers())
        );
    }

    private static Method[] getRelevantMethods(final Class<?> clazz) {
        return getAll(null, 0, Method[]::new, clazz, Class::getDeclaredMethods, m ->
            m.getAnnotation(Init.class) != null &&
            !Modifier.isStatic(m.getModifiers())
        );
    }

    private static <T> T[] getAll(
        final T[] resultArray,
        final int index,
        final IntFunction<T[]> arrayConst,
        final Class<?> beanClazz,
        final Function<Class<?>, T[]> func,
        final Predicate<T> tester
    ) {
        if (index < 0) {
            throw new IllegalArgumentException("index must not be negative");
        }

        final T[] stuff = func.apply(beanClazz);

        T[] output;
        if (resultArray == null) {
            output = arrayConst.apply(stuff.length);
        } else {
            output = resultArray;
        }

        // Filter out invalid things
        int counter = 0;
        for (final T t : stuff) {
            if (tester.test(t)) {
                if (index + counter == output.length) {
                    output = expandArray(arrayConst, output);
                }
                output[index + counter++] = t;
            }
        }

        final Class<?> superClazz = beanClazz.getSuperclass();

        if (superClazz == null) {
            final T[] result;
            if (counter + index == output.length) {
                result = output;
            } else {
                result = arrayConst.apply(counter + index);
                System.arraycopy(output, 0, result, 0, result.length);
            }
            return result;
        }

        return getAll(output, index + counter, arrayConst, superClazz, func, tester);
    }

    private static <T> T[] expandArray(final IntFunction<T[]> arrayConst, final T[] array) {
        final T[] result = arrayConst.apply(array.length * 2);
        System.arraycopy(array, 0, result, 0, array.length);
        return result;
    }

    private static void verifyArgNames(final Field[] fields) {
        final String dashMessage = "Argument name must not begin with a dash (-)! : ";
        final String whitespaceMessage = "Argument name must not contain whitespace! : ";
        final Pattern pattern = Pattern.compile("\\s");

        for (final Field field : fields) {
            final Argument annotation = field.getAnnotation(Argument.class);
            Objects.requireNonNull(annotation, "field must contain Argument annotation by this point");

            for (final String name : annotation.shortNames()) {
                if (name.startsWith("-")) {
                    throw new ClashException.Dashes(dashMessage + name);
                }
                if (pattern.matcher(name).find()) {
                    throw new ClashException.Whitespace(whitespaceMessage + name);
                }
            }

            for (final String name : annotation.longNames()) {
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

        for (final Field field : fields) {
            final Argument annotation = field.getAnnotation(Argument.class);
            Objects.requireNonNull(annotation, "field must contain Argument annotation by this point");

            for (final String name : annotation.shortNames()) {
                map.put(name, field);
            }
            for (final String name : annotation.longNames()) {
                map.put(name, field);
            }
        }

        return map;
    }

    private static void initializeGivenArguments(final String[] args, final Map<String, Field> argMap, final Object o) {
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

            initializeValue(field, o, value);
        }
    }

    private static void initializeDefaultValues(final Map<String, Field> argMap, final Object o) {
        for (final Field field : argMap.values()) {
            final Argument argument = field.getAnnotation(Argument.class);
            if (argument == null) {
                throw new IllegalStateException("Received field must be an argument");
            }

            if (!argument.defaultCreator().isInterface()) {
                setField(field, o, init(argument.defaultCreator()).createDefault(field.getName()));
            } else if (!argument.defaultValue().equals(ARGUMENT_DEFAULT_VALUE)) {
                initializeValue(field, o, argument.defaultValue());
            } else if (argument.required()) {
                throw new ClashException("Required argument not provided: " + argument.shortNames()[0]);
            }
        }
    }

    private static void runInitMethods(final Class<?> clazz, final Object o) {
        final Method[] methods = getRelevantMethods(clazz);
        for (final Method method : methods) {
            try {
                method.setAccessible(true);
                method.invoke(o);
            } catch (final IllegalAccessException | InvocationTargetException e) {
                throw new ClashException(e);
            }
        }
    }

    private static void initializeValue(final Field field, final Object object, final String value) {
        final Class<?> type = field.getType();
        field.setAccessible(true);

        final Argument annotation = field.getAnnotation(Argument.class);
        Objects.requireNonNull(annotation, "field must contain Argument annotation by this point");

        if (!annotation.initializer().isInterface()) {
            final Initializer<?> initializer = init(annotation.initializer());
            final Object objectValue = initializer.initialize(field.getName(), value);
            setField(field, object, objectValue);
        } else {
            final Object objectValue = getFieldValue(type, field, value);
            setField(field, object, objectValue);
        }
    }

    private static Object getFieldValue(final Class<?> type, final Field field, final String value) {
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
            return Character.valueOf(value.charAt(0));
        // Other Number children
        } else if (type == BigInteger.class) {
            return new BigInteger(value);
        } else if (type == BigDecimal.class) {
            return new BigDecimal(value);
        } else if (type == AtomicInteger.class) {
            return new AtomicInteger(Integer.parseInt(value));
        } else if (type == AtomicLong.class) {
            return new AtomicLong(Long.parseLong(value));
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
            return handleCharArray(value, false);
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
            return handleCharArray(value, true);
        // Other Number arrays
        } else if (type == BigInteger[].class) {
            return handleArray(BigInteger.class, value, BigInteger::new);
        } else if (type == BigDecimal[].class) {
            return  handleArray(BigDecimal.class, value, BigDecimal::new);
        } else if (type == AtomicInteger[].class) {
            return handleArray(AtomicInteger.class, value, s -> new AtomicInteger(Integer.parseInt(s)));
        } else if (type == AtomicLong[].class) {
            return handleArray(AtomicLong.class, value, s -> new AtomicLong(Long.parseLong(s)));
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
        } else if (type.isAssignableFrom(List.class)) {
            return handleGenericCollection(field, value, new ArrayList<>());
        } else if (type.isAssignableFrom(Set.class)) {
            return handleGenericCollection(field, value, new HashSet<>());
        } else {
            throw new IllegalStateException("Cannot map field of type '" + type + "'");
        }
    }

    private static Object handleArray(final Class<?> arrayClass, final String arg, final Function<String, ?> func) {
        final String trimmed = arg.trim();
        final String noBrackets = arrayPattern.matcher(trimmed).replaceAll("");
        final String[] items;
        if (noBrackets.contains(",")) {
            items = noBrackets.split(",");
        } else {
            items = whitespacePattern.split(noBrackets);
        }

        final Object array = Array.newInstance(arrayClass, items.length);

        for (int i = 0; i < items.length; i++) {
            Array.set(array, i, func.apply(items[i]));
        }

        return array;
    }

    private static void setField(final Field field, final Object object, final Object value) {
        if (value == null) {
            return; // there is nothing to set
        }

        final long offset = unsafe.objectFieldOffset(field);
        final Class<?> type = field.getType();

        if (type == byte.class) {
            if (!(value instanceof Byte)) {
                throw new IllegalStateException("Target field is a byte, but type of value does not match");
            }
            unsafe.putByte(object, offset, ((Byte) value).byteValue());
        } else if (type == short.class) {
            if (!(value instanceof Short)) {
                throw new IllegalStateException("Target field is a short, but type of value does not match");
            }
            unsafe.putShort(object, offset, ((Short) value).shortValue());
        } else if (type == int.class) {
            if (!(value instanceof Integer)) {
                throw new IllegalStateException("Target field is an int, but type of value does not match");
            }
            unsafe.putInt(object, offset, ((Integer) value).intValue());
        } else if (type == long.class) {
            if (!(value instanceof Long)) {
                throw new IllegalStateException("Target field is a long, but type of value does not match");
            }
            unsafe.putLong(object, offset, ((Long) value).longValue());
        } else if (type == float.class) {
            if (!(value instanceof Float)) {
                throw new IllegalStateException("Target field is a float, but type of value does not match");
            }
            unsafe.putFloat(object, offset, ((Float) value).floatValue());
        } else if (type == double.class) {
            if (!(value instanceof Double)) {
                throw new IllegalStateException("Target field is a double, but type of value does not match");
            }
            unsafe.putDouble(object, offset, ((Double) value).doubleValue());
        } else if (type == boolean.class) {
            if (!(value instanceof Boolean)) {
                throw new IllegalStateException("Target field is a boolean, but type of value does not match");
            }
            unsafe.putBoolean(object, offset, ((Boolean) value).booleanValue());
        } else if (type == char.class) {
            if (!(value instanceof Character)) {
                throw new IllegalStateException("Target field is a char, but type of value does not match");
            }
            unsafe.putChar(object, offset, ((Character) value).charValue());
        } else {
            unsafe.putObject(object, offset, value);
        }
    }

    private static Enum<?> getEnum(final String value, final Class<?> type) {
        final String cleanedValue = whitespacePattern.matcher(value.trim()).replaceAll("_");
        final Enum<?>[] constants = (Enum<?>[]) type.getEnumConstants();
        for (final Enum<?> constant : constants) {
            if (constant.name().equalsIgnoreCase(cleanedValue)) {
                return constant;
            }
        }
        throw new ClashException("Could not match '" + value + "' with the a value for " + type.getName());
    }

    private static Object handleCharArray(final String value, final boolean boxed) {
        final String trimmed = value.trim();
        final char s = trimmed.charAt(0);
        final char e = trimmed.charAt(trimmed.length() - 1);
        final boolean isArray = (s == '[' || s == '(' || s == '{') && (e == ']' || e == ')' || e == '}');
        if (isArray) {
            final Class<?> clazz = boxed ? Character.class : char.class;
            return handleArray(clazz, value, string -> Character.valueOf(string.charAt(0)));
        } else {
            final char[] array = value.toCharArray();
            if (boxed) {
                final Character[] res = new Character[array.length];
                // Can't use System.arraycopy with primitive -> boxed
                for (int i = 0; i < array.length; i++) {
                    res[i] = Character.valueOf(array[i]);
                }
                return res;
            } else {
                return value.toCharArray();
            }
        }
    }

    private static Object handleGenericCollection(
        final Field field,
        final String value,
        final Collection<?> collection
    ) {
        final Type genericType = field.getGenericType();
        if (genericType instanceof ParameterizedType) {
            final ParameterizedType paramType = (ParameterizedType) genericType;
            final Class<?> listType = (Class<?>) paramType.getActualTypeArguments()[0];
            final Object array = handleArray(listType, value, string -> getFieldValue(listType, field, string));
            final int length = Array.getLength(array);
            for (int i = 0; i < length; i++) {
                collection.add(cast(Array.get(array, i)));
            }
        } else {
            final Object array = handleArray(Object.class, value, Function.identity());
            final int length = Array.getLength(array);
            for (int i = 0; i < length; i++) {
                collection.add(cast(Array.get(array, i)));
            }
        }
        return collection;
    }

    private static Unsafe getUnsafe() {
        try {
            final Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            return (Unsafe) theUnsafe.get(null);
        } catch (final NoSuchFieldException | IllegalAccessException e) {
            throw new ClashException(e);
        }
    }

    private static <T> T init(Class<T> clazz) {
        try {
            final Constructor<T> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (final Exception e) {
            throw new ClashException(e);
        }
    }

    private static <T> T cast(final Object o) {
        @SuppressWarnings("unchecked")
        final T t = (T) o;
        return t;
    }
}

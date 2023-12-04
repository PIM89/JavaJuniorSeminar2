

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TestProcessor {
    public static void runTest(Class<?> testClass) {
        final Constructor<?> declaredConstructor;
        try {
            declaredConstructor = testClass.getDeclaredConstructor();
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Для класса \"" + testClass.getName() + "\" не найден конструктор без аргументов");
        }

        final Object testObj;
        try {
            testObj = declaredConstructor.newInstance();
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("Не удалось создать объект класса \"" + testClass.getName() + "\"");
        }


        List<Method> methodsTestAnnotation = new ArrayList<>();
        List<Method> methodsBeforeEachAnnotation = new ArrayList<>();
        List<Method> methodsAfterEachAnnotation = new ArrayList<>();

        for (Method method : testClass.getMethods()) {
            if (!method.isAnnotationPresent(Skip.class)) {
                if (method.isAnnotationPresent(Test.class)) {
                    checkTestMethod(method);
                    methodsTestAnnotation.add(method);
                }

                if (method.isAnnotationPresent(BeforeEach.class)) {
                    checkTestMethod(method);
                    methodsBeforeEachAnnotation.add(method);
                }

                if (method.isAnnotationPresent(AfterEach.class)) {
                    checkTestMethod(method);
                    methodsAfterEachAnnotation.add(method);
                }
            }
        }
        methodsBeforeEachAnnotation.forEach(it -> runTest(it, testObj));

        methodsTestAnnotation.stream()
                .sorted(new MethodsComparatorAnnotationByOrder())
                .forEach(it -> runTest(it, testObj));

        methodsAfterEachAnnotation.stream()
                .forEach(it -> runTest(it, testObj));
    }

    private static void checkTestMethod(Method method) {
        if (!method.getReturnType().isAssignableFrom(void.class) || method.getParameterCount() != 0) {
            throw new IllegalArgumentException("Метод \"" + method.getName() + "\" должен быть void и не иметь аргументов");
        }
    }

    private static void runTest(Method testMethod, Object testObj) {
        try {
            testMethod.invoke(testObj);
        } catch (InvocationTargetException | IllegalAccessException | AssertionError e) {
            throw new RuntimeException("Не удалось запустить тестовый метод \"" + testMethod.getName() + "\"");
        }
    }
}

class MethodsComparatorAnnotationByOrder implements Comparator<Method> {
    @Override
    public int compare(Method o1, Method o2) {
        Test annotation1 = o1.getAnnotation(Test.class);
        Test annotation2 = o2.getAnnotation(Test.class);
        return annotation1.order() - annotation2.order();
    }
}

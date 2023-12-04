public class Homework {
    public static void main(String[] args) {
        TestProcessor.runTest(MyTest.class);
    }

    static class MyTest {
        @Test(order = -2)
        @AfterEach
        public void firstTest() {
            System.out.println("firstTest запущен");
        }

        @Test()
        @BeforeEach
        public void secondTest() {
            System.out.println("secondTest запущен");
        }

        @Test(order = 5)
        @Skip
        public void thirdTest() {
            System.out.println("thirdTest запущен");
        }
    }
}

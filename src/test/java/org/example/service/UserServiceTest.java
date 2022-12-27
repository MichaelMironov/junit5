package org.example.service;

import org.example.dao.UserDao;
import org.example.extensions.ConditionalExtension;
import org.example.extensions.injector.Injector;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test for simple UserService.
 */
@Tag("fast")
//@Injector
@ExtendWith({ConditionalExtension.class, MockitoExtension.class})
class UserServiceTest {
    private static final User IVAN = User.of(1, "Ivan", "1234");
    private static final User PETR = User.of(2, "Petr", "1111");

    @Captor
    private ArgumentCaptor<Integer> argumentCaptor;
    private @Mock(lenient = true) UserDao userDao; //Не бросать эксепшен, если стаб не используется в классах
    private @InjectMocks UserService users;
    @Test
    void userEmpty() {
        List<User> usersAll = users.getAll();
        assertTrue(usersAll.isEmpty());
    }

    @BeforeEach
    void before(){
        //TODO added mockito extensions
        Mockito.doReturn(true).when(userDao).delete(IVAN.getId()); // Mock
//        this.userDao = Mockito.mock(UserDao.class);
//        this.userDao = Mockito.spy(new UserDao());
//        this.users = new UserService(userDao);
    }

    @Test
    void throwExcIfDatabaseNotAvailable(){
        Mockito.doThrow(RuntimeException.class).when(userDao).delete(IVAN.getId());
    }

    @Test
    void shouldDeleteExistUser(){
//        Mockito.doReturn(true).when(userDao).delete(IVAN.getId()); // Mock
//        Mockito.doReturn(true).when(userDao).delete(Mockito.any());
        users.add(IVAN);
        boolean deleteResult = users.delete(IVAN.getId());
//        ArgumentCaptor<Integer> argumentCaptor = ArgumentCaptor.forClass(Integer.class); //TODO added mockito ext:capture
        Mockito.verify(userDao, Mockito.times(1)).delete(argumentCaptor.capture());
        Mockito.verify(userDao,Mockito.times(1)).delete(IVAN.getId());
        Mockito.verify(userDao,Mockito.atLeast(1)).delete(IVAN.getId());

        assertThat(deleteResult).isTrue();
    }

    @Test
    void usersSize() {
        System.out.println(users.hashCode());
        users.add(IVAN, PETR);
        List<User> usersAll = users.getAll();
        assertThat(usersAll).hasSize(2);
    }

    @Test
    void usersToMap() {
        System.out.println(users.hashCode());

        users.add(IVAN, PETR);
        Map<Integer, User> usersMap = users.getAllConvertedById();
        assertAll(
                () -> assertThat(usersMap).containsKeys(IVAN.getId(), PETR.getId()),
                () -> assertThat(usersMap).containsValues(IVAN, PETR));
    }

    @Test
    void throwExceptionIfLogNull() {
        System.out.println(users.hashCode());

        try {
            users.login(null, IVAN.getPassword());
            fail("Login can't be null");
        } catch (IllegalArgumentException ex) {
            assertTrue(true);
        }
    }

    @Test
    void throwExceptionIfPassNull() {
        System.out.println(users.hashCode());

        Throwable exception = assertThrows(IllegalArgumentException.class, () -> users.login(PETR.getUsername(), null));
        assertThat(exception.getMessage()).isEqualTo("username or password is null");
    }

    @Test
    void test() {
        User user = User.of(IVAN.getId(), IVAN.getUsername(), IVAN.getPassword());
        ServiceSteps.method(user);
    }


    /*
     * Nested test's class with tests for single functionality
     *
     * */
    @Nested
    class LoginTests {
        @RepeatedTest(value = 5, name = RepeatedTest.LONG_DISPLAY_NAME)
        @Tag("login")
        void successLogin(RepetitionInfo repetitionInfo) {
            System.out.println(repetitionInfo);
            users.add(IVAN);
            Optional<User> expectedUser = users.login(IVAN.getUsername(), IVAN.getPassword());
            expectedUser.ifPresent(user -> assertThat(user).isEqualTo(IVAN));
        }

        @Test
        @Tag("login")
        void wrongPassword() {
            users.add(IVAN);
            Optional<User> expectedUser = users.login("d", "wrong");
            assertThat(expectedUser).isNotPresent();
        }


        @Timeout(value = 200, unit = TimeUnit.MILLISECONDS)
        @MethodSource("org.example.service.UserServiceTest#loginParameters")
        @ParameterizedTest(name = "{index}: {arguments} ")
        void loginParameterizedTest(String username, String password, Optional<User> user) {
            users.add(IVAN, PETR);
            Optional<User> expectedUser = users.login(username, password);
            assertThat(expectedUser).isEqualTo(user);
        }

        @Test
        void checkLoginPerformance(){
            System.out.println(Thread.currentThread().getName());
            Optional<User> user = assertTimeoutPreemptively(Duration.ofMillis(50L), () -> {
                System.out.println(Thread.currentThread().getName());
                return users.login(IVAN.getUsername(), IVAN.getPassword());
            });

        }
    }
    static Stream<Arguments> loginParameters() {
        return Stream.of(
                Arguments.of("Ivan", "1234", Optional.of(IVAN)),
                Arguments.of("Petr", "1111", Optional.of(PETR)),
                Arguments.of("Petr", "wrong", Optional.empty()),
                Arguments.of("wrong", "1234", Optional.empty())
        );
    }
}

package hello.jdbc.exception.basic;


import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import java.sql.SQLException;

@Slf4j
public class UncheckedAppTest {

    @Test
    void unchecked() {
        Controller controller = new Controller();
        Assertions.assertThatThrownBy(controller::request)
                .isInstanceOf(RuntimeConnectException.class);
    }

    static class Controller {
        Service service = new Service();

        // 계속 연쇄적으로 throws가 증가함
        public void request() {
            service.logic();
        }
    }

    static class Service {
        Repository repository = new Repository();
        NetworkClient networkClient = new NetworkClient();

        public void logic() {
            networkClient.call();
            repository.call();
        }
    }

    static class NetworkClient {
        public void call() {
            // 런타임 예외로 변경
            throw new RuntimeConnectException("연결 실패");
        }
    }

    static class Repository {
        public void call() {
            try {
                runSQL();
            } catch (SQLException e) {
                // runSQL의 Checked 예외 발생시 잡는다.
                // 이후 발생한 Checked예외를 Runtime예외로 변환해서 던진다.
                throw new RuntimeSQLException(e);
            }
        }

        public void runSQL() throws SQLException {
            throw new SQLException("ex");
        }
    }

    static class RuntimeConnectException extends RuntimeException {
        public RuntimeConnectException(String message) {
            super(message);
        }
    }

    static class RuntimeSQLException extends RuntimeException {
        // 이 예외가 왜 터졌는지 이전 예외를 매개변수로 넣을 수 있다.
        public RuntimeSQLException(Throwable cause) {
            super(cause);
        }
    }
}

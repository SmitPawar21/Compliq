import java.lang.reflect.Method;
import org.springframework.ai.document.Document;

public class TestDoc {
    public static void main(String[] args) {
        for (Method m : Document.class.getMethods()) {
            System.out.println(m.getName());
        }
    }
}

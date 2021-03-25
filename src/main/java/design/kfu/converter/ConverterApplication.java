package design.kfu.converter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ConverterApplication {

    public static String filePath = "test.csv";

    public static void main(String[] args) {
        if (args.length > 0) {
            filePath = args[0];
        }
        SpringApplication.run(ConverterApplication.class, args);
    }
}

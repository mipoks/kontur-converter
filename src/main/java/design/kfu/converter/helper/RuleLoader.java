package design.kfu.converter.helper;


import design.kfu.converter.ConverterApplication;
import design.kfu.converter.reader.FileRuleReader;
import design.kfu.converter.reader.RuleCreater;
import org.springframework.stereotype.Component;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;

@Component
public class RuleLoader {

    private HashMap<String, ArrayList<Pair<String, BigDecimal>>> rules;
    private boolean loaded;

    private String file = ConverterApplication.filePath;

    public RuleLoader() {
        this.rules = new HashMap<>();
    }

    public void loadConvertingRules() throws IOException {
        FileRuleReader fileReader = new FileRuleReader();
        RuleCreater ruleCreater = new RuleCreater(fileReader.read(new File(file)));
        rules = ruleCreater.create();
        loaded = true;
    }

    public HashMap<String, ArrayList<Pair<String, BigDecimal>>> getRules() {
        return rules;
    }

    public boolean isLoaded() {
        return loaded;
    }
}

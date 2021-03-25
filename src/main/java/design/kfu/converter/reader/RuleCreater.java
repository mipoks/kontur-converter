package design.kfu.converter.reader;

import au.com.bytecode.opencsv.CSVWriter;
import design.kfu.converter.helper.Pair;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class RuleCreater {

    private String ruleFile = "bridges.csv";
    private HashMap<String, ArrayList<Pair<String, BigDecimal>>> treePrototype;

    public RuleCreater(HashMap<String, ArrayList<Pair<String, BigDecimal>>> treePrototype) {
        this.treePrototype = treePrototype;
    }

    private void dfs(String v, HashMap<String, Boolean> usedMap, HashMap<String, ArrayList<Pair<String, BigDecimal>>> ourTree) {
        usedMap.put(v, true);

        ArrayList<Pair<String, BigDecimal> > arrayList = treePrototype.get(v);
        for (Pair<String, BigDecimal> pair : arrayList)
        {
            String nextV = pair.getKey();
            if (!usedMap.containsKey(nextV)) {
                if (ourTree.containsKey(v))
                    ourTree.get(v).add(new Pair<>(nextV, pair.getValue()));
                else {
                    ArrayList<Pair<String, BigDecimal> > pairs = new ArrayList<>();
                    pairs.add(new Pair<>(nextV, pair.getValue()));
                    ourTree.put(v, pairs);
                }
                dfs(nextV, usedMap, ourTree);
            }
        }
    }

    //Make tree from rules: delete excess edges
    public HashMap<String, ArrayList<Pair<String, BigDecimal>>> create() {
        Set<String> mySet = treePrototype.keySet();
        HashMap<String, Boolean> usedMap = new HashMap<>(mySet.size());

        HashMap<String, ArrayList<Pair<String, BigDecimal>>> ourTree = new HashMap<>();
        for (String ver : mySet) {
            if (!usedMap.containsKey(ver))
                dfs(ver, usedMap, ourTree);
        }

        //Write prepared rules to file
        try(CSVWriter csvWriter = new CSVWriter(new FileWriter(new File(ruleFile), Charset.forName("UTF-8")))) {
            Set<String> myNewSet = ourTree.keySet();
            for (String str : myNewSet) {
                ArrayList<Pair<String, BigDecimal>> arrayList = ourTree.get(str);
                for (Pair<String, BigDecimal> pair : arrayList) {
                    csvWriter.writeNext(new String[]{str, pair.getKey(), String.valueOf(pair.getValue())});
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        FileRuleReader fileReader = new FileRuleReader();
        return fileReader.read(new File(ruleFile));
    }

}

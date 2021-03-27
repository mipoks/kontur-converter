package design.kfu.converter.helper;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class Converter {

    private HashMap<String, ArrayList<Pair<String, BigDecimal>>> info;

    public Converter(HashMap<String, ArrayList<Pair<String, BigDecimal>>> info) {
        this.info = info;
    }

    @Autowired
    public Converter(RuleLoader ruleLoader) throws IOException {
        if (!ruleLoader.isLoaded())
            ruleLoader.loadConvertingRules();
        info = ruleLoader.getRules();
    }

    private Pair<BigDecimal, String> ansFound = null;

    //Dog-nail for void DFS
    private Pair<BigDecimal, String> getPopAnsFound() {
        Pair<BigDecimal, String> temp = ansFound;
        ansFound = null;
        return temp;
    }

    //DFS
    private void findInRuleDFS(String from, List<String> variants, HashMap<String, Boolean> usedFroms, BigDecimal ans) {
        log.info("Searching " + from + " from: " + variants);
        usedFroms.put(from, true);
        //If answer is a neighbour
        Iterator<String> it = variants.listIterator();
        while (it.hasNext()) {
            String temp = it.next();
            if (temp.equals(from)) {
                ansFound = new Pair<>(ans, from);
                return;
            }
        }
        //Else call next depth level
        if (!info.containsKey(from)) {
            return;
        }
        ArrayList<Pair<String, BigDecimal>> pairs = info.get(from);
        log.info("Count of ways " + pairs.size());
        for (Pair<String, BigDecimal> pair : pairs) {
            log.info("There is way from: " + from + " to " + pair.getKey());
            if (!usedFroms.containsKey(pair.getKey()) && ansFound == null) {
                log.info("Jump from: " + from + " to: " + pair.getKey());
                findInRuleDFS(pair.getKey(), variants, usedFroms, ans.multiply(pair.getValue()));
            }
        }
    }

    //Reduce fraction: try to match and delete numerator with denumerator
    //Work with numbers (coefficients)
    private Pair<BigDecimal, Pair<List<String>, List<String>>> reduceFraction(List<String> numDenum) {
        if (numDenum.size() == 0)
            throw new IllegalStateException("Can't convert");

        BigDecimal coef = new BigDecimal(1);
        List<String> numerators = argsOfExpression(numDenum.get(0));

        if (numDenum.size() == 2) {
            List<String> denumerators = argsOfExpression(numDenum.get(1));
            Iterator<String> it = denumerators.listIterator();
            while (it.hasNext()) {
                String now = it.next();
                try {
                    coef = coef.multiply(BigDecimal.valueOf(1 / Double.parseDouble(now)));
                    it.remove();
                } catch (NumberFormatException e) {
                }
            }

            it = numerators.listIterator();
            while (it.hasNext()) {
                String now = it.next();
                try {
                    coef = coef.multiply(BigDecimal.valueOf(Double.parseDouble(now)));
                    it.remove();
                } catch (NumberFormatException e) {
                    findInRuleDFS(now, denumerators, new HashMap<>(), new BigDecimal(1));
                    if (ansFound != null) {
                        Pair<BigDecimal, String> temp = getPopAnsFound();
                        denumerators.remove(temp.getValue());
                        coef = coef.multiply(temp.getKey());
                        it.remove();
                    }
                }
            }
            return new Pair<>(coef, new Pair<>(numerators, denumerators));
        }
        if (numDenum.size() == 1) {
            Iterator<String> it = numerators.listIterator();
            while (it.hasNext()) {
                String now = it.next();
                try {
                    coef = coef.multiply(BigDecimal.valueOf(Double.parseDouble(now)));
                    it.remove();
                } catch (NumberFormatException e) {
                }
            }
            return new Pair<>(coef, new Pair<>(numerators, new ArrayList<>()));
        }
        return new Pair<>(coef, new Pair<>(new ArrayList<>(), new ArrayList<>()));
    }

    public BigDecimal convert(String from, String to) throws IllegalStateException{
        List<String> fromNumDenum = splitDevide(from);
        List<String> toNumDenum = splitDevide(to);

        Pair<BigDecimal, Pair<List<String>, List<String>>> reducedFractionFrom = reduceFraction(fromNumDenum);
        Pair<BigDecimal, Pair<List<String>, List<String>>> reducedFractionTo = reduceFraction(toNumDenum);


        List<String> fromNumerators = reducedFractionFrom.getValue().getKey();
        List<String> toNumerators = reducedFractionTo.getValue().getKey();
        if (fromNumerators.size() != toNumerators.size())
            throw new IllegalStateException("Can't convert");

        ArrayList<BigDecimal> numeratorsCoef = new ArrayList<>();
        ArrayList<BigDecimal> denumeratorsCoef = new ArrayList<>();

        for (String numerator : fromNumerators) {
            log.info("Search: " + numerator + " From: " + toNumerators);
            findInRuleDFS(numerator, toNumerators, new HashMap<>(), new BigDecimal(1));
            if (ansFound == null)
                throw new IllegalStateException("Can't convert");

            Pair<BigDecimal, String> temp = getPopAnsFound();
            toNumerators.remove(temp.getValue());
            log.info("To numerator added: " + temp);
            numeratorsCoef.add(temp.getKey());
        }

        fromNumerators = reducedFractionFrom.getValue().getValue();
        toNumerators = reducedFractionTo.getValue().getValue();

        if (fromNumerators.size() != toNumerators.size())
            throw new IllegalStateException("Can't convert");

        for (String denumerator : fromNumerators) {
            findInRuleDFS(denumerator, toNumerators, new HashMap<>(), new BigDecimal(1));
            if (ansFound == null)
                throw new IllegalStateException("Can't convert");

            Pair<BigDecimal, String> temp = getPopAnsFound();
            toNumerators.remove(temp.getValue());
            log.info("To denumerator added: " + temp);
            denumeratorsCoef.add(temp.getKey());
        }

        BigDecimal[] onlyNumerators = toOnlyNumerators(numeratorsCoef, denumeratorsCoef);
        return getAnswer(onlyNumerators)
                .multiply(reducedFractionFrom.getKey())
                .divide(reducedFractionTo.getKey(), 25, RoundingMode.UP);
    }


    private List<String> argsOfExpression(String exp) {
        return new LinkedList<>(Arrays.asList(exp.split("\\*")));
    }

    private List<String> splitDevide(String exp) {
        exp = exp.replace(" ", "");
        return new LinkedList<>(Arrays.asList(exp.split("/")));
    }

    //Multiply sorted coefficients
    private BigDecimal getAnswer(BigDecimal[] numeratorsArray) {
        BigDecimal answer = new BigDecimal(1);

        for (int i = 0; i < numeratorsArray.length / 2; i++) {
            answer = answer.multiply(numeratorsArray[i]).multiply(numeratorsArray[numeratorsArray.length - i - 1]);
        }
        if (numeratorsArray.length % 2 == 1) {
            answer = answer.multiply(numeratorsArray[numeratorsArray.length / 2]);
        }
        return answer;
    }

    //Move denumerator to numerators: denumerator = 1 / numerator
    public BigDecimal[] toOnlyNumerators(List<BigDecimal> numerators, List<BigDecimal> denumerators) {
        BigDecimal[] onlyNumerators = new BigDecimal[numerators.size() + denumerators.size()];
        int i = 0;
        for (BigDecimal decimal : denumerators) {
            onlyNumerators[i] = new BigDecimal(1).divide(decimal, 25, RoundingMode.UP);
            i++;
        }
        for (BigDecimal decimal : numerators) {
            onlyNumerators[i] = decimal;
            i++;
        }
        Arrays.sort(onlyNumerators);
        return onlyNumerators;
    }

    public String format15num(BigDecimal longNum) {
        DecimalFormat decimalFormat = new DecimalFormat("#.###############");
        return decimalFormat.format(longNum).replace(",", ".");
    }

    public boolean validateFromAndTo(String toValidate) {
        toValidate = toValidate.replace(" ", "");
        Matcher m = Pattern.compile("[\\*|\\/]{1,1}").matcher(toValidate);
        ArrayList<String> elements = new ArrayList<>();
        int pos;
        for (pos = 0; m.find(); pos = m.end()) {
            elements.add(toValidate.substring(pos, m.start()));
        }
        elements.add(toValidate.substring(pos));

        for (String str : elements) {
            try {
                Double.parseDouble(str);
            } catch (NumberFormatException e) {
                if (!info.containsKey(str)) {
                    log.info(str + " no rule for this argument");
                    return false;
                }
            }
        }
        return true;
    }

}
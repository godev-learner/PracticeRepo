
import java.util.*;

public class Wordle {
    public static void main(String[] args){
        runUnitTests(getUnitTests());
    }
    public static String normalizeString(String str, String name) {
        if (!isAlpha(str)) {
          System.out.printf("%s word %s can only contain letters\n", name, str);
          return null;
        }
        if (str.length() != 5) {
          System.out.printf("%s word %s must be exactly 5 letters\n", name, str);
          return null;
        }
        return str.toLowerCase();
      }

    public static String checkWordle(String target, String guessWord) {//tease, peace
        String normalizedTarget = normalizeString(target, "Target");
        String normalizedGuessWord = normalizeString(guessWord, "GuessWord");
        if (normalizedTarget == null || normalizedGuessWord == null) {
          return null;
        }
        char[] result = new char[guessWord.length()];
        Arrays.fill(result, '_');//[_,_,_,_,_]
        
        Map<Character, List<Integer>> charMap = new HashMap<>();
    
        for(int j =0; j<target.length(); j++){
          char c = target.charAt(j);
          if (charMap.containsKey(c)) {
            // Retrieve the list associated with the character and add the index to it
            charMap.get(c).add(j);
        } else {
            // Create a new list, add the index to it, and put it in the map
            List<Integer> list = new ArrayList<>();
            list.add(j);
            charMap.put(c, list);
        }
        }
        // t -> {0} , e -> {1,4} ,a -> {2}, s -> {3}
        //result[B,_,_,_,_]
    
        for(int i = 0;i< guessWord.length(); i++){ //1
          if(result[i] != '_'){//
            continue;
          }
          char c = guessWord.charAt(i); //e
          if(charMap.containsKey(c)){//
            List<Integer> indx = charMap.get(c);//{1,4}
            if(!indx.isEmpty()){
              Iterator<Integer> iterator = indx.iterator();
              while (iterator.hasNext()) {
                int k = iterator.next();
                if (k < guessWord.length() && k < target.length() && guessWord.charAt(k) == target.charAt(k)) {
                  result[k] = 'G';
                  iterator.remove(); // Safely remove the element using the iterator
                }
              }
            }
            if(result[i] == '_' && indx.size()>0) {
              result[i] = 'Y';
              indx.remove(indx.size()-1);
            }else if(result[i] == '_'){
              result[i] = 'B';
            }
          }else{
            result[i] = 'B';
          }
        }
    
        return String.valueOf(result);
      }

    public static void runUnitTests(UnitTest[] unitTests) {
        int totalPassed = 0;
    
        for (int i =0; i < unitTests.length; i++) {
          UnitTest unitTest = unitTests[i];
          String result = checkWordle(unitTest.target, unitTest.input);
          String status = "Fail";
          if ((result == null && result == unitTest.expectedResult)
              || result.equals(unitTest.expectedResult) ) {
            totalPassed++;
            status = "Pass";
          }
          System.out.printf("(%s) Target: %s, Input: %s, Result: %s, Expected result: %s\n", status, unitTest.target, unitTest.input, result, unitTest.expectedResult);
        }
    
        if (unitTests.length > 0) {
          System.out.printf("\nPass rate: %d / %d\n", totalPassed, unitTests.length);
        } else {
          System.out.println("\nNo unit tests provided");
        }
      }
    
      public static UnitTest[] getUnitTests() {
        return new UnitTest[]{
          new UnitTest("serai", "prune", "BYBBY"),
          new UnitTest("serai", "raise", "YYYYY"),
          new UnitTest("tease", "wreak", "BBYYB"),
          new UnitTest("tease", "fleet", "BBYYY"),
          new UnitTest("tease", "peace", "BGGBG"),
          new UnitTest("tease", "green", "BBYYB"),
          new UnitTest("tease", "eagle", "YYBBG"),
          new UnitTest("tease", "geese", "BGBGG"),
          new UnitTest("tease", "easel", "YYYYB"),
          new UnitTest("tease", "eerie", "BGBBG"),
          new UnitTest("class", "sassy", "YYBGB"),  
        };
      }
    
      public static boolean isAlpha(String str) {
        char[] chars = str.toCharArray();
        boolean result = true;
        for(int i = 0; i < chars.length; i++) {
          boolean isAlphaChar = Character.isLetter(chars[i]);
          if (!isAlphaChar) {
            result = false;
          }
        }
        return result;
      }
}
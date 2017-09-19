import java.io.*;
import java.lang.Thread;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Variable {

  public String name;
  private String type;
  private String address;
  private String pointsTo;
  public String value;
  private ArrayList<Variable> elements;


  // Returns new index of where the variable concludes
  public int parseVariable(String[] lines, int linesIndex, String nameIn) {

    try {

      String line = lines[linesIndex];

      if (line.trim().isEmpty() || line.split(":")[0].trim().isEmpty()) return linesIndex+1;

      int index = 0;

      // Get address
      address = "";
      while (true) {
        char c = line.charAt(index);
        if (c == ':') break;
        address += c;
        ++index;
      }

      index = nextNonSpaceCharacter(line, index+1);
      boolean hasType = (line.charAt(index) == '(');

      // Get type
      if (hasType) {
        type = "";
        ++index;
        while (line.charAt(index) != ')') {
          type += line.charAt(index);
          ++index;
        }
        index = nextNonSpaceCharacter(line, index+1);
      }

      // Get name
      name = "";
      while (line.charAt(index) != ' ') {
        name += line.charAt(index);
        ++index;
      }
      if (!nameIn.isEmpty()) name = nameIn + ":" + name;

      // Skip =
      ++index;
      assert (line.charAt(index) == '=') :
                   "Line [" + line + "] index " + index + " not =";
      index = nextNonSpaceCharacter(line, index+1);

      // If it's a multielement variable
      if (line.charAt(index) == '{') {
        elements = new ArrayList<Variable>();
        ++linesIndex;
        assert (linesIndex < lines.length) : "linesIndex overran lines.length";
        while (!lines[linesIndex].endsWith("}")) {
          Variable element = new Variable();
          linesIndex = element.parseVariable(lines, linesIndex, name);
          elements.add(element);
        }
        return linesIndex+1;
      }

      // Address pointed to (if there is one) and value
      boolean haveTwoChunks = !(isLastTextChunk(line, index));
      boolean isPointer = (index < line.length() -1 &&
                           line.charAt(index) == '0' &&
                           line.charAt(index+1) == 'x');

      String chunk = "";
      int terminator = haveTwoChunks ? nextSpaceCharacter(line, index) : line.length();

      while (index < terminator) {
        chunk += line.charAt(index);
        ++index;
      }

      if (isPointer) pointsTo = chunk;
      else value = chunk;

      if (haveTwoChunks) {
        value = "";
        index = nextNonSpaceCharacter(line, index+1);
        while (index < line.length()) {
          value += line.charAt(index);
          ++index;
        }
      }

      return linesIndex + 1;

    } catch (Exception ex) {
      ex.printStackTrace();
      System.exit(1);
    }

    return -1; // unreachable code

  } // parseVariable()



  private int nextNonSpaceCharacter(String line, int index) {
    try {

      while (index < line.length()) {
        if (line.charAt(index) != ' ') return index;
        ++index;
      }

    } catch (Exception ex) {
      ex.printStackTrace();
      System.exit(1);
    }

    assert false : "Expected non-space character but none was found in [" + line + "].";
    return 4+8+15+16+23+42; // Unreachable code
  }


  private int nextSpaceCharacter(String line, int index) {
    try {

      while (index < line.length()) {
        if (line.charAt(index) == ' ') return index;
        ++index;
      }

    } catch (Exception ex) {
      ex.printStackTrace();
      System.exit(1);
    }

    assert false : "Expected space character but none was found in [" + line + "].";
    return 4+8+15+16+23+42; // Unreachable code
  }


  private boolean isLastTextChunk(String line, int index) {

    try {

      while (index < line.length()) {
        if (line.charAt(index) == ' ') return false;
        ++index;
      }

    } catch (Exception ex) {
      ex.printStackTrace();
      System.exit(1);
    }

    return true;
  }


  public String print(int eventNumber, int lineNumber, String scope) {
  
    if (type == null && address == null) return "";

    String output = "";

    try {

      output += "variable_access~!~";
      output += (eventNumber + "|");
      output += (lineNumber + "|");
      output += (scope + "|");
      output += (address + "|");
      output += (type + "|");
      output += (name + "|");

      if (elements != null) {
        output += "<multielement>" + System.lineSeparator();
        for (Variable v : elements) {
          output += v.print(eventNumber, lineNumber, scope);
        }
      }
      else {
        if (pointsTo != null) {
          if (value == null) output += (pointsTo + System.lineSeparator());
          else output += (pointsTo + "|");
        }
        if (value != null) output += (value + System.lineSeparator());
      }

    } catch (Exception ex) {
      ex.printStackTrace();
      System.exit(1);
    }
    
    

    return output;

  } // print()


  public ArrayList<Variable> changedValues(Variable compare) {

    ArrayList<Variable> changedList = new ArrayList<Variable>();

    try {

      String error = "\n" + print(-1, -1, "<N/A>") + "\n" +
                     compare.print(-1, -1, "<N/A>") + "\n";

      if (compare.elements != null) {
        assert (elements != null) : "Variable element nullity A:" + error;
        assert (elements.size() == compare.elements.size()) :
                     "Variable element size:" + error;
      }
      if (compare.elements == null)
        assert elements == null : "Variable element nullity B:" + error;
      if (compare.pointsTo != null)
        assert pointsTo != null : "Variable pointsTo nullity A:" + error;
      if (compare.pointsTo == null)
        assert pointsTo == null : "Variable pointsTo nullity B:" + error;

      if (elements == null) {
        if (!(value == null && compare.value == null) &&
            ((value == null && compare.value != null) ||
            (value != null && compare.value == null) ||
            !compare.value.equals(value)))
        {
          changedList.add(this);
        } else if (pointsTo != null) {
          if (!compare.pointsTo.equals(pointsTo)) changedList.add(this);
        }
      }
      else {
        for (int n = 0; n < elements.size(); ++n) {
          changedList.addAll(elements.get(n).changedValues(compare.elements.get(n)));
        }
      }

    } catch (Exception ex) {
      ex.printStackTrace();
      System.exit(1);
    }

    return changedList;

  } // changedValues()


} // Variable class




public class Analyzer {

  // TODO: external functions

  static Hashtable<String, Variable> variables = new Hashtable<>(); // scope,name -> value
  static Hashtable<String, String> registers = new Hashtable<>(); // name -> value
  static ArrayList<String> backtrace = new ArrayList<>();
  static ArrayList<String> externalFunctions = new ArrayList<>();
  static ArrayList<String> sensitiveData = new ArrayList<>();
  static ArrayList<String> coreSizeZeroStructs = new ArrayList<>();
//  static ArrayList<Pair<String, Integer>> pendingFunctions
//    = new ArrayList<Pair<String, Integer>>(); // for prioritizing call order

  static int eventNumber = 0;
  static int lineNumber = 0;
  static String function = "<unknown>";

  static int speedMultiplier = 7;

  static FileWriter fw;
  static BufferedWriter bw;

  static final String GLOBAL = "*G*";

  public static void main(String[] args) {

    try {

      if (args.length < 1) {
        System.err.println("Usage: Analyzer binary [sm:speed_multiplier] [arch:architecture]" +
                           " [sd:func,variable_to_track]");
        System.exit(1);
      }

      String binary = args[0];
      String architecture = "x86_64"; // default

      for (int n = 1; n < args.length; ++n) {
        String[] arg = args[n].split(":");
        if (arg[0].equals("sm")) speedMultiplier = Integer.parseInt(arg[1]);
        else if (arg[0].equals("arch")) architecture = arg[1];
        else if (arg[0].equals("sd")) sensitiveData.add(arg[1]);
      }

      // Read list of externally defined functions
/*      String inputFilename = "./simple-functions.txt";
      BufferedReader br = new BufferedReader(new FileReader(inputFilename));

      String readLine = "";

      while ((readLine = br.readLine()) != null) externalFunctions.add(readLine);
      br.close(); /* */

      // Interact with LLDB through stdin/stdout
      String outputFilename = binary + "-output.vaccs";

      fw = new FileWriter(outputFilename, true);
      bw = new BufferedWriter(fw);

      InputStreamReader isr = new InputStreamReader(System.in);
      BufferedReader input = new BufferedReader(isr);

      String line = "";

      System.out.println("target create --no-dependents --arch " + architecture + " " + binary);
      Thread.sleep(1000);
      consumeInput(input);
      
      System.out.println("image dump sections " + binary);
      Thread.sleep(1000);
      parseSections(input);

      System.out.println("breakpoint set --name main");
      Thread.sleep(100);
      consumeInput(input);

      System.out.println("run");
      Thread.sleep(1000);

      while (true) {
        analyzeLine(input);
        ++eventNumber;
        System.out.println("step");
        Thread.sleep(10);
      }

    } catch (Exception ex) {

      ex.printStackTrace();

    } finally {

      try {
        bw.close();
        fw.close();
      } catch (Exception ex) {
        System.err.println(ex);
      }

    }

  } // main


  private static void consumeInput(BufferedReader input)
              throws IOException, InterruptedException
  {
    String line = "";
    while (input.ready()) {
      line = input.readLine();
      System.err.println("Consume Received: '" + line + "'");
      Thread.sleep(10);
    }
  }
  
  
  private static void parseSections(BufferedReader input)
              throws IOException, InterruptedException
  {
    String[] inputLines = getLldbInput(input);

    for (int n = 0; n < inputLines.length; ++n) {
      String line = inputLines[n].trim();
      if (line.startsWith("0x")) {
        String[] splitLine = line.split("\\s+");
        // ID, type, file address, file offset, file size, flags, section name
        if (splitLine.length < 7) continue;
        bw.write("section~!~" +
                 splitLine[0]    + "|" + // ID
                 splitLine[1]    + "|" + // type
                 splitLine[2]    + "|" + // file address
                 splitLine[3]    + "|" + // file offset
                 splitLine[4]    + "|" + // file size
                 splitLine[5]    + "|" + // flags
                 splitLine[6]          + // section name
                 System.lineSeparator());
      }
    }
  }


  private static void analyzeLine(BufferedReader input)
              throws IOException, InterruptedException
  {
    // Get current line number, which function we're in,
    // and compile list of external function calls
    String resourceZeroed = parseSourceLine(input);

    int sleepTime = speedMultiplier * 10;

    // Get backtrace
    System.out.println("bt");
    Thread.sleep(sleepTime);
    parseBacktrace(input);

    // Get local vars + args
    System.out.println("frame variable -L");
    Thread.sleep(sleepTime);
    parseVariables(input, false);

    // Get global vars
    System.out.println("target variable -L");
    Thread.sleep(sleepTime);
    parseVariables(input, true);

    // Get registers
    System.out.println("register read");
    Thread.sleep(sleepTime);
    parseRegisters(input);

    // Get assembly
    System.out.println("disassemble --line");
    Thread.sleep(sleepTime);
    parseAssembly(input);
    
    // If zeroed resource is core limit, print event
    if (resourceZeroed != null && coreSizeZeroStructs.contains(resourceZeroed)) {
      bw.write("sd_corezero~!~" +
               eventNumber      + "|" +
               lineNumber       +
               System.lineSeparator());
    }

  } // analyzeLine()


  // Returns resource that has been zeroed out (if any)
  private static String parseSourceLine(BufferedReader input)
              throws IOException, InterruptedException
  {
//    pendingFunctions.clear();
    String[] inputLines = getLldbInput(input);
    int index = 0;

    // Function
    for (int n = index; n < inputLines.length; ++n) {
      index = n;
      String line = inputLines[n];
      if (line.contains("frame")) {
        function = line.split("`")[1].split("\\s+")[0].split("\\(")[0];
        break;
      }
    }

    String sourceLine = "";

    // Line number
    for (int n = index; n < inputLines.length; ++n) {
      String line = inputLines[n];
      String[] splitLine = line.split("\\s+");
      if (splitLine[0].equals("->")) {
        sourceLine = line.substring(2);
        lineNumber = Integer.parseInt(splitLine[1]);
        break;
      }
    }
    
    if (!sensitiveData.isEmpty()) {
      // Look for mlock/munlock calls
      //String regex = "(\\{|\\}|;|\\s+)mlock\\s*\\(";
      String regex = "(\\bmlock\\b\\s*\\()|(\\bmunlock\\b\\s*\\()";
      Pattern pattern = Pattern.compile(regex);
      Matcher matcher = pattern.matcher(sourceLine);
      if (matcher.find()) {
        // Get the variable being memory-locked
        String variable = "";
        int idx = matcher.end();
        char c = sourceLine.charAt(idx);
        while (c != ' ' && c != '\n' && c != ',' && c != '\t') {
          variable += c;
          ++idx;
          c = sourceLine.charAt(idx);
        }
        
        // Check if it's a tracked variable
        if (sensitiveData.contains(function + "," + variable) ||
            sensitiveData.contains(GLOBAL + "," + variable))
        {
          String eventType = "sd_lock~!~";
          if (sourceLine.contains("munlock")) eventType = "sd_unlock~!~";
          bw.write(eventType     +
                   eventNumber   + "|" +
                   lineNumber    + "|" +
                   function      + "|" +
                   variable      +
                   System.lineSeparator());
        }
      }
      
      // Look for setrlimit calls for zeroing the core size
      regex = "\\bsetrlimit\\b\\s*\\(";
      pattern = Pattern.compile(regex);
      matcher = pattern.matcher(sourceLine);
      if (matcher.find()) {
        // Get the limit being set
        String limit = "";
        int idx = matcher.end();
        char c = sourceLine.charAt(idx);
        while (c != ' ' && c != '\n' && c != ',' && c != '\t') {
          limit += c;
          ++idx;
          c = sourceLine.charAt(idx);
        }
        if (limit.equals("RLIMIT_CORE")) {
          // See if the struct has had its limit set to 0
          ++idx;
          String variable = "";
          c = sourceLine.charAt(idx);
          while (c != ')' && c != '\n') {
            if (c != ' ' && c != '&' && c != '\t') variable += c;
            ++idx;
            c = sourceLine.charAt(idx);
          }
          return variable;
        }
      }
    }
    
    return null;
    
    // Compile list of external function calls
    // We aren't doing anything with this right now, but we might in the future.
/*    for (String function : externalFunctions) {
      String regex = "\\b" + function + "\\b"; // whole word only
      Pattern pattern = Pattern.compile(regex);
      Matcher matcher = pattern.matcher(sourceLine);
      if (matcher.find()) pendingFunctions.add(
        Pair.createPair(function, matcher.start()));
    }

    // Sort by column in the line (back to front isn't guaranteed to give the
    // correct call order but it should be a good guess in many cases)
    Collections.sort(pendingFunctions, new Comparator<Pair>() {
      @Override
      public int compare(Pair lhs, Pair rhs) {
        // -1: less than, 1: greater than, 0: equal, all inverted for descending
        return (Integer)(lhs.getSecond()) > (Integer)(rhs.getSecond()) ? -1 :
               ((Integer)(lhs.getSecond()) < (Integer)(rhs.getSecond())) ? 1 :
               0;
      }
    }); /* */

  } // parseSourceLine()


  private static void parseBacktrace(BufferedReader input)
              throws IOException, InterruptedException
  {
    String[] inputLines = getLldbInput(input);

    ArrayList<String> currentBacktrace = new ArrayList<String>();

    for (int n = inputLines.length-1; n >= 0; --n) {
      String line = inputLines[n];
      if (line.startsWith("(lldb)")) continue;
      if (!line.contains("frame #")) continue;
      String[] halves = line.split("`");
      if (halves.length < 2) continue; // If it won't even tell us what the function is called, forget it.
      String[] front = halves[0].split("\\s+");
      String[] back = halves[1].split("\\s+");
      String frameFile = front[front.length-1];
      String frameFunction = back[0];
      currentBacktrace.add(frameFile + "`" + frameFunction.split("\\(")[0]);
    }

    // Check for termination -- obsoleted
/*    if (currentBacktrace.size() == 1 && currentBacktrace.get(0).endsWith("`_start")) {
      System.out.println("continue");
      Thread.sleep(1000);
      System.out.println("quit");
      Thread.sleep(10);
      bw.close();
      fw.close();
      System.exit(0);
    } /* */

    // If current and old backtrace don't match, output returns or invocations
    if (backtrace.size() != currentBacktrace.size()) {
      int sizeDifference = backtrace.size() - currentBacktrace.size();
      if (sizeDifference > 0) {
        for (int n = 0; n < sizeDifference; ++n) {
          backtrace.remove(backtrace.size()-1);
          bw.write("return~!~" + ++eventNumber + System.lineSeparator());
        }
      }
      else {
        for (int n = currentBacktrace.size() + sizeDifference;
             n < currentBacktrace.size(); ++n)
        {
          backtrace.add(currentBacktrace.get(n));
          bw.write("function_invocation~!~"                 +
                   eventNumber                              + "|" +
                   lineNumber                               + "|" +
                   currentBacktrace.get(n)                  +
                   System.lineSeparator());
        }
      }
    }
    else {
      boolean stacksEqual = true;
      for (int n = 0; n < backtrace.size(); ++n) {
        if (!backtrace.get(n).equals(currentBacktrace.get(n))) {
          stacksEqual = false;
          break;
        }
      }
      if (!stacksEqual) { // Stacks are different without having changed size
        for (int n = 0; n < backtrace.size(); ++n) bw.write("return~!~" + eventNumber + System.lineSeparator());
        backtrace.clear();
        for (String s : currentBacktrace) {
          backtrace.add(s);
          bw.write("function_invocation~!~" +
                   eventNumber              + "|" +
                   lineNumber               + "|" +
                   s                        +
                   System.lineSeparator());
        }
      }
    }

  } // parseBacktrace()


  private static void parseVariables(BufferedReader input, boolean global)
              throws IOException, InterruptedException
  {
    try {

      String[] inputLines = getLldbInput(input);

      // Check for termination
      if (inputLines.length >= 2 && inputLines[1].contains("main = ")) {
        System.out.println("continue");
        Thread.sleep(1000);
        System.out.println("quit");
        Thread.sleep(10);
        bw.close();
        fw.close();
        System.exit(1);
      }

      int n = 0;
      while (n < inputLines.length) {
        String line = inputLines[n];
        if (line.startsWith("(lldb)"))     { ++n; continue; }
        if (line.startsWith("Global var")) { ++n; continue; }
        Variable variable = new Variable();
        n = variable.parseVariable(inputLines, n, "");

        String scope = "";
        scope = global ? GLOBAL : function;
        String key = scope + "," + variable.name;

        if (variables.keySet().contains(key)) {
          ArrayList<Variable> changedList = variable.changedValues(variables.get(key));
          for (Variable v : changedList) {
            bw.write(v.print(eventNumber, lineNumber, scope)); // output variable data
            
            // Sensitive data outputs
            if (sensitiveData.contains(scope + "," + v.name) ||
                sensitiveData.contains(GLOBAL + "," + v.name)) 
            {
              if (v.value.equals("0") || v.value.equals("\"\""))
                bw.write("sd_clear~!~" +
                         eventNumber   + "|" +
                         lineNumber    + "|" +
                         scope         + "|" +
                         v.name        +
                         System.lineSeparator());
              else
                bw.write("sd_set~!~"   +
                         eventNumber   + "|" +
                         lineNumber    + "|" +
                         scope         + "|" +
                         v.name        +
                         System.lineSeparator());
            }
            
            // Check for structs that zero out core size
            if (v.name.endsWith("rlim_max")) {
              if (v.value.equals("0")) coreSizeZeroStructs.add(v.name.split(":")[0]);
              else coreSizeZeroStructs.remove(v.name.split(":")[0]);
            }
          }
          if (!changedList.isEmpty()) variables.put(key, variable);
        }
        else {
          variables.put(key, variable);
          bw.write(variable.print(eventNumber, lineNumber, scope));
        }

      }

    } catch (Exception ex) {
      bw.flush();
      ex.printStackTrace();
      System.exit(1);
    }
  } // parseVariable()


  private static void parseRegisters(BufferedReader input)
              throws IOException, InterruptedException
  {
    String[] inputLines = getLldbInput(input);
    for (int n = 0; n < inputLines.length; ++n) {
      String line = inputLines[n];
      if (line.startsWith("(lldb)")) continue;
      if (line.startsWith("General")) continue;
      String[] splitLine = line.split("\\s+");

      String name = splitLine[1];
      String value = splitLine[3];

      if (!registers.keySet().contains(name) || !registers.get(name).equals(value)) {
        registers.put(name, value);
        bw.write("register~!~" +
                 eventNumber   + "|" +
                 lineNumber    + "|" +
                 name          + "|" +  // register name (splitLine[1])
                 value         +        // value (splitLine[3])
                 System.lineSeparator());
      }
    }
  } // parseRegisters()


  private static void parseAssembly(BufferedReader input)
              throws IOException, InterruptedException
  {
    String[] inputLines = getLldbInput(input);
    if (inputLines.length < 7) {
      System.err.println("ERROR: Assembly output in unexpected format");
      bw.flush();
      System.exit(1);
    }
    for (int n = 6; n < inputLines.length; ++n) {
      String line = inputLines[n].replaceAll("->", "").trim();
      if (line.contains("=") && line.split("=").length == 2 && line.split("=")[1].trim().startsWith("0x")) {
        bw.write("assembly~!~" +
                 lineNumber    + "|" +
                 line          +
                 System.lineSeparator());
      }
    }
  } // parseAssembly()


  private static String[] getLldbInput(BufferedReader input)
                  throws IOException, InterruptedException
  {
    String lldbInput = "";
    while (input.ready()) {
      lldbInput += input.readLine() + System.lineSeparator();
      Thread.sleep(10);
    }
    System.err.println("Analyze Received: '" + lldbInput + "'");
    return lldbInput.split(System.lineSeparator());
  } // getLldbInput()


} // Analyzer class



// Thanks to hevy of stackoverflow for this:
/*
class Pair<First, Second> {
    private First first;
    private Second second;

    public Pair(First first, Second second) {
        this.first = first;
        this.second = second;
    }

    public void setFirst(First first) {
        this.first = first;
    }

    public void setSecond(Second second) {
        this.second = second;
    }

    public First getFirst() {
        return first;
    }

    public Second getSecond() {
        return second;
    }

    public void set(First first, Second second) {
        setFirst(first);
        setSecond(second);
    }

    public static <First, Second> Pair<First, Second> createPair(
        First element0, Second element1)
    {
        return new Pair<First, Second>(element0, element1);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Pair pair = (Pair) o;

        if (first != null ? !first.equals(pair.first) : pair.first != null) return false;
        if (second != null ? !second.equals(pair.second) : pair.second != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = first != null ? first.hashCode() : 0;
        result = 31 * result + (second != null ? second.hashCode() : 0);
        return result;
    }
} // Pair class
/* */

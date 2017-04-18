package prophecy.common;

// By Stefan Reich with some stuff stolen from the other guy ^^
// License: Public domain. Simple as that.

import junit.framework.TestCase;

import static prophecy.common.JSONMinify.minify;

public class JSONMinifyTest extends TestCase {
  public void testMinify() {
    System.out.println("Running test1...");
    String test1 = "// this is a JSON file with comments\n" +
      "{\n" +
      "\"foo\": \"bar\", // this is cool\n" +
      "\"bar\": [\n" +
      "\"baz\", \"bum\", \"zam\"\n" +
      "],\n" +
      "/* the rest of this document is just fluff\n" +
      "in case you are interested. */\n" +
      "\"something\": 10,\n" +
      "\"else\": 20\n" +
      "}\n" +
      "/* NOTE: You can easily strip the whitespace and comments\n" +
      " from such a file with a good minifier such as JSONMinify :) \n"+
      "*/";

    String test1_res = "{\"foo\":\"bar\",\"bar\":[\"baz\",\"bum\",\"zam\"],\"something\":10,\"else\":20}";
    assertEquals(test1_res, minify(test1));

    System.out.println("Running test2...");
    String test2 = "{\"/*\":\"*/\",\"//\":\"\",/*\"//\"*/\"/*/\"://\n" +
      "\"//\"}" +
      "";

    String test2_res = "{\"/*\":\"*/\",\"//\":\"\",\"/*/\":\"//\"}";
    assertTrue(minify(test2).equals(test2_res));

    System.out.println("Running test3...");
    String test3 = "/*\n" +
      "this is a\n" +
      "multi line comment */{\n" +
      "\n" +
      "\"foo\"\n" +
      ":" +
      " \"bar/*\"// something\n" +
      " , \"b\\\"az\":/*\n" +
      "something else */\"blah\"\n" +
      "\n" +
      "}";

    String test3_res = "{\"foo\":\"bar/*\",\"b\\\"az\":\"blah\"}";
    assertTrue(minify(test3).equals(test3_res));

    System.out.println("Running test4...");
    String test4 = "{\"foo\": \"ba\\\"r//\", \"bar\\\\\": \"b\\\\\\\"a/*z\", \n" +
      "\"baz\\\\\\\\\": /* yay */ \"fo\\\\\\\\\\\"*/o\"\n" +
      "}";
    String test4_res = "{\"foo\":\"ba\\\"r//\",\"bar\\\\\":\"b\\\\\\\"a/*z\",\"baz\\\\\\\\\":\"fo\\\\\\\\\\\"*/o\"}";
    assertTrue(minify(test4).equals(test4_res));

    System.out.println("Running test5...");
    String test5 = "// this is a comment //\n" +
      "{ // another comment\n" +
      " true, \"foo\", // 3rd comment\n" +
      " \"http://www.ariba.com\" // comment after URL\n" +
      " \n" +
      "}";
    String test5_res = "{true,\"foo\",\"http://www.ariba.com\"}";
    assertTrue(minify(test5).equals(test5_res));
    System.out.println(test5);
    System.out.println(minify(test5));

    String test6 = "['spaces in string']";
    assertEquals(test6, minify(test6));

    String test7 = "[['spaces in string']]";
    assertEquals(test7, minify(test7));

    // OK, this finally catches the bug...
    String test8 = "[\n" +
      "  ['words', 'test sample extension', 'I\\'m installed!'],\n" +
      "]";
    String minified8 = "[['words','test sample extension','I\\'m installed!'],]";
    assertEquals(minified8, minify(test8));
  }

  public void testUnicodeLiterals() {
    assertEquals("\"\\u12AB\"", minify("\"\\u12AB\""));
  }
}

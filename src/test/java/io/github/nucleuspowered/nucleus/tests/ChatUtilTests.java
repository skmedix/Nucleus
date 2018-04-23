/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.tests;

import com.google.inject.Guice;
import io.github.nucleuspowered.nucleus.NucleusPlugin;
import io.github.nucleuspowered.nucleus.internal.text.TextParsingUtils;
import io.github.nucleuspowered.nucleus.tests.util.TestModule;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Tests elements of the {@link TextParsingUtils}
 */
public class ChatUtilTests extends TestBase {

    private static Pattern patternToTest;

    @BeforeClass
    public static void setup() throws Exception {
        TestBase.testSetup();

        NucleusPlugin plugin = Guice.createInjector(new TestModule()).getInstance(NucleusPlugin.class);

        Field parserToTest = TextParsingUtils.class.getDeclaredField("urlParser");
        parserToTest.setAccessible(true);
        patternToTest = (Pattern)parserToTest.get(new TextParsingUtils(plugin));
    }

    /**
     * Tests that the specified permissions are in the permission list.
     */
    @SuppressWarnings("CanBeFinal")
    @RunWith(Parameterized.class)
    public static class URLtests {
        @BeforeClass
        public static void setup() throws Exception {
            ChatUtilTests.setup();
        }

        @Parameterized.Parameters(name = "{index}: Message {0}, expecting {1}")
        public static Iterable<Object[]> data() {
            return Arrays.asList(new Object[][] {
                    {"http://nucleuspowered.org", true, "http://nucleuspowered.org", null},
                    {"&chttp://nucleuspowered.org", true, "http://nucleuspowered.org", "&c"},
                    {"blag", false, null, null},
                    {"hello, please visit http://nucleuspowered.org", true, "http://nucleuspowered.org", null},
                    {"hello, please visit http://nucleuspowered.org/docs today", true, "http://nucleuspowered.org/docs", null},
                    {"hello, please visit &khttp://nucleuspowered.org/docs &otoday", true, "http://nucleuspowered.org/docs", "&k"},
                    {"hello, please visit &k&chttp://nucleuspowered.org/docs &otoday", true, "http://nucleuspowered.org/docs", "&k&c"},
                    {"blag &cblag", false, null, null},
                    {"hello, please visit &k&cnucleuspowered.org/docs &otoday", true, "nucleuspowered.org/docs", "&k&c"},
                    {"hello, please visit https://google.com", true, "https://google.com", null},
                    {"test:nucleuspowered.org", false, null, null}
            });
        }

        @Parameterized.Parameter()
        public String message;

        @Parameterized.Parameter(1)
        public boolean result;

        @Parameterized.Parameter(2)
        public String url;

        @Parameterized.Parameter(3)
        public String codes;

        @Test
        public void testRegexIsValid() throws IllegalAccessException, InstantiationException {
            Matcher m = patternToTest.matcher(this.message);

            // Do we match?
            boolean actual = m.find();
            String r = "";
            if (actual) {
                r = m.group();
            }

            Assert.assertEquals(r, this.result, actual);

            // More tests for those that do match
            if (this.result) {
                Assert.assertEquals(this.url, m.group("url"));
                Assert.assertEquals(this.codes, m.group("colour"));
            }
        }
    }
}

package org.exigencecorp.gen;

import java.io.File;
import java.io.FileReader;
import java.io.StringWriter;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.exigencecorp.util.Join;

public class GDirectoryTest extends TestCase {

    private File foo = new File("./bin/org/exigencecorp/Foo.java");

    public void setUp() throws Exception {
        super.setUp();
        if (this.foo.exists()) {
            this.foo.delete();
        }
    }

    public void testOutput() throws Exception {
        Assert.assertFalse(this.foo.exists());

        GDirectory bin = new GDirectory("./bin");
        GClass gc = bin.getClass("org.exigencecorp.Foo");
        gc.getMethod("foo").setBody("int i = 0;");
        bin.output();

        Assert.assertEquals(Join.lines(
            "package org.exigencecorp;",
            "",
            "public class Foo {",
            "",
            "    public void foo() {",
            "        int i = 0;",
            "    }",
            "",
            "}",
            ""), this.read(this.foo));
    }

    private String read(File file) throws Exception {
        StringWriter writer = new StringWriter();
        FileReader reader = new FileReader(file);
        char[] buffer = new char[1024 * 4];
        int n = 0;
        while (-1 != (n = reader.read(buffer))) {
            writer.write(buffer, 0, n);
        }
        return writer.toString();
    }

}

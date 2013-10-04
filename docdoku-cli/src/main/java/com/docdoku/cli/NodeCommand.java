package com.docdoku.cli;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class NodeCommand {
    /*
   * Called from node-webkit client, return json string
   * */
    public static void main(String[] args) {
        PrintStream originOut=System.out;

        System.setOut(new PrintStream(new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                // ignore ...
            }
        }));


        Object result = MainCommandImpl.main(args);

        // restore output stream
        System.setOut(originOut);
        System.out.println(result);
    }
}

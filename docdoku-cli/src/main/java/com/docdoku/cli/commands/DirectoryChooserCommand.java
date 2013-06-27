package com.docdoku.cli.commands;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineParser;

import javax.swing.*;

public class DirectoryChooserCommand implements CommandLine {

    public void exec() {
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(new java.io.File("."));
        chooser.setDialogTitle("Please choose your working directory");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            System.out.println(chooser.getSelectedFile());
        }
        else {
            System.err.println("empty");
        }
    }

    @Override
    public String getDescription() {
        return "Open a directory chooser.";
    }
}

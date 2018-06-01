package com.codlex.distributed.systems.homework1.starter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.common.collect.ImmutableList;

public final class JavaProcess {

    private JavaProcess() {}

    public static Process exec(Class klass, String... args) throws IOException,
                                               InterruptedException {
        String javaHome = System.getProperty("java.home");
        String javaBin = javaHome +
                File.separator + "bin" +
                File.separator + "java";
        String classpath = System.getProperty("java.class.path");
        String className = klass.getCanonicalName();

        List<String> argsAll = new ArrayList<>();
        argsAll.addAll(ImmutableList.of(javaBin, "-cp", classpath, className));
        argsAll.addAll(Arrays.asList(args));

        ProcessBuilder builder = new ProcessBuilder(argsAll);

        Process process = builder.start();

        return process;
    }

}

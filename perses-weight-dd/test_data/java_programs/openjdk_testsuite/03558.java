

import java.util.List;
import java.util.ArrayList;
import java.io.File;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;



public class WsCompileExample {
    File destDir;
    File srcDir;
    protected boolean compilerDebug = false;
    protected boolean compilerOptimize = false;
    protected String userClasspath = null;

    public static void main(String[] args) {
        new WsCompileExample().do_main(args);
    }

    public void do_main(String[] args) {
        if(!args[0].equals("-s")) {
            throw new RuntimeException("specify -s for src");
        }

        
        srcDir =  new File(args[1]);
        if(!args[2].equals("-d")) {
            throw new RuntimeException("specify -d for dest");
        }
        destDir =  new File(args[3]);
        if(!destDir.exists())
            destDir.mkdirs();
        System.out.println("----test compile 1-----");
        compileGeneratedClasses();

        
         srcDir = new File(args[1]+"1");
         destDir =  new File(args[3]+"1");
        if(!destDir.exists())
            destDir.mkdirs();
        System.out.println("----test compile 2-----");
        compileGeneratedClasses();

    }
    protected void compileGeneratedClasses() {
        List sourceFiles = new ArrayList();

        for (File f: srcDir.listFiles()) {
            if (f.getName().endsWith(".java")) {
                sourceFiles.add(f.getAbsolutePath());
            }
        }

        if (sourceFiles.size() > 0) {
                                String classDir = destDir.getAbsolutePath();
            String classpathString = createClasspathString();
            System.out.println("classpathString: " + classpathString);

                                String[] args = new String[4 + (compilerDebug == true ? 1 : 0) +
                (compilerOptimize == true ? 1 : 0) +
                sourceFiles.size()];
            args[0] = "-d";
            args[1] = classDir;
            args[2] = "-classpath";
            args[3] = classpathString;

            int baseIndex = 4;
            if (compilerDebug) {
                args[baseIndex++] = "-g";
            }
            if (compilerOptimize) {
                args[baseIndex++] = "-O";
            }
            for (int i = 0; i < sourceFiles.size(); ++i) {
                args[baseIndex + i] = (String)sourceFiles.get(i);
            }

            
            JavaCompilerHelper compilerHelper = new JavaCompilerHelper(System.out);
            boolean result = compilerHelper.compile(args);
            if (!result) {
                System.out.println("wscompile.compilation Failed");
            }
        }
    }

    protected String createClasspathString() {
        if (userClasspath == null) {
            userClasspath = "";
        }
                          String jcp = userClasspath + File.pathSeparator + System.getProperty("java.class.path");
                  return jcp;
    }
}

class JavaCompilerHelper {
    public JavaCompilerHelper(OutputStream out) {
                this.out = out;
        }

        public boolean compile(String[] args) {
                return internalCompile(args);
        }

        protected boolean internalCompile(String[] args) {

                System.out.println("Args: ");
                for(String arg : args){
                        System.out.print(arg+" ");
                }
        System.out.println();
                ClassLoader cl = Thread.currentThread().getContextClassLoader();
                Class comSunToolsJavacMainClass = null;
                try {
                        
                        comSunToolsJavacMainClass =
                                cl.loadClass("com.sun.tools.javac.Main");
                        try {
                                Method compileMethod =
                                        comSunToolsJavacMainClass.getMethod(
                                                "compile",
                                                compile141MethodSignature);
                                try {
                                        Object result =
                                                compileMethod.invoke(
                                                        null,
                                                        new Object[] { args, new PrintWriter(out)});
                                        if (!(result instanceof Integer)) {
                                                return false;
                                        }
                                        return ((Integer) result).intValue() == 0;
                                } catch (IllegalAccessException e3) {
                                        return false;
                                } catch (IllegalArgumentException e3) {
                                        return false;
                                } catch (InvocationTargetException e3) {
                                        return false;
                                }
                        } catch (NoSuchMethodException e2) {
              System.out.println("ERROR: Compile failed with error:" + e2.toString() );
                        }
                } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                        return false;
                } catch (SecurityException e) {
                        return false;
                }
                return true;
        }

        protected String getGenericErrorMessage() {return "javacompiler.error"; }
        protected void run() {  }
        protected boolean parseArguments(String[] args) {return false;}
        protected OutputStream out;

        protected static final Class[] compile141MethodSignature;
        static
        {
                compile141MethodSignature = new Class[2];
                compile141MethodSignature[0] = (new String[0]).getClass();
                compile141MethodSignature[1] = PrintWriter.class;
        }
}

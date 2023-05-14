package net.yury;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class ClassUtils {

    /**
     * 取得某个包下实现了某个接口所有类
     * @param packageName
     * @param c
     * @param <T>
     * @return
     */
    public static <T> List<Class<? extends T>> getAllClassByInterface(String packageName, Class<T> c) {
        if (!c.isInterface()) {
            throw new RuntimeException("clazz c is not a interface");
        }
        List<Class<? extends T>> res = new ArrayList<>();
        // 获取当前包下以及子包下所以的类
        getClasses(packageName, ((pkgName, classSimpleName, clazz) -> {
            if (c.isAssignableFrom(clazz) && (!c.equals(clazz))) {
                @SuppressWarnings("unchecked")
                Class<T> var1 = (Class<T>) clazz;
                res.add(var1);
            }
        }));
        return res;
    }

    public static interface ClassVisitor {
        /**
         * @param packageName 包名
         * @param classSimpleName 类名
         * @param clazz 类
         */
        public void visit(String packageName, String classSimpleName, Class<?> clazz);
    }

    public static void getClasses(String packageName, ClassVisitor visitor) {
        // 获取包的名字 并进行替换
        String packageDirName = packageName.replace('.', '/');
        // 定义一个枚举的集合 并进行循环来处理这个目录下的things
        Enumeration<URL> dirs = null;
        try {
            dirs = Thread.currentThread().getContextClassLoader().getResources(packageDirName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // 循环迭代下去
        while (dirs.hasMoreElements()) {
            // 获取下一个元素
            URL url = dirs.nextElement();
            // 得到协议的名称
            String protocol = url.getProtocol();
            // 如果是以文件的形式保存在服务器上
            if ("file".equals(protocol)) {
                // 获取包的物理路径
                String filePath = null;
                try {
                    filePath = URLDecoder.decode(url.getFile(), "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
                // 以文件的方式扫描整个包下的文件 并添加到集合中
                findAndAddClassesInPackageByFile(packageName, filePath, true, visitor);
            }
        }
    }

    public static void findAndAddClassesInPackageByFile(String packageName, String packagePath, final boolean recursive, ClassVisitor visitor){
        // 获取此包的目录 建立一个File
        File dir = new File(packagePath);
        // 如果不存在或者 也不是目录就直接返回
        if (!dir.exists() || !dir.isDirectory()) {
            return;
        }
        // 如果存在 就获取包下的所有文件 包括目录
        File[] dirFiles = dir.listFiles(new FileFilter() {
            // 自定义过滤规则 如果可以循环(包含子目录) 或则是以.class结尾的文件(编译好的java类文件)
            public boolean accept(File file) {
                return (recursive && file.isDirectory()) || (file.getName().endsWith(".class"));
            }
        });
        // 循环所有文件
        for (File file : dirFiles) {
            // 如果是目录 则继续扫描
            if (file.isDirectory()) {
                findAndAddClassesInPackageByFile(packageName + "." + file.getName(),
                        file.getAbsolutePath(),
                        recursive,
                        visitor);
            } else {
                // 如果是java类文件 去掉后面的.class 只留下类名
                String className = file.getName().substring(0, file.getName().length() - 6);
                try {
                    //添加到集合中去
                    visitor.visit(packageName, className, Class.forName(packageName + '.' + className));
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
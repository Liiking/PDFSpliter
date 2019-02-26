package com.qwy.tool;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * Created by qianweiying on 2018/7/9.
 * 定义一些公共方法
 */
public class BaseClazz {

    /**
     * 打印日志信息
     *
     * @param msg       要打印的日志信息
     */
    protected void print(String msg) {
        System.out.println(msg);
    }

    /**
     * 捕获控制台输出到GUI界面上
     *
     * @param textArea       展示信息的控件
     */
    protected void outputUI(final JTextArea textArea){
        OutputStream textAreaStream = new OutputStream() {
            public void write(int b) throws IOException {
                textArea.append(String.valueOf((char)b));
            }

            public void write(byte b[]) throws IOException {
                textArea.append(new String(b));
            }

            public void write(byte b[], int off, int len) throws IOException {
                textArea.append(new String(b, off, len));
            }
        };
        PrintStream myOut = new PrintStream(textAreaStream);
        System.setOut(myOut);
        System.setErr(myOut);
    }

    /**
     * 显示提示信息
     *
     * @param message       要展示的提示信息
     */
    protected void showMessage(String message) {
        JOptionPane.showMessageDialog(null, message, "提示", JOptionPane.PLAIN_MESSAGE);
    }

    /**
     * 打开文件夹选择器
     */
    protected String fileChooser(JTextField tf) {
        return fileChooser("", tf, "", null);
    }

    protected String fileChooser(String type, JTextField tf) {
        return fileChooser(type, tf, "", null);
    }

    protected String fileChooser(String type, JTextField tf, OnFileSelectListener listener) {
        return fileChooser(type, tf, "", listener);
    }

    /**
     * 打开文件选择器
     *
     * @param type 要选取的文件类型
     * @param tf 要填入的textfield
     * @param origin 初始目录
     */
    protected String fileChooser(String type, JTextField tf, String origin, OnFileSelectListener listener) {
        if (isEmpty(origin)) {
            origin = "./";
        }
        JFileChooser chooser = new JFileChooser(origin);
        if (!isEmpty(type)) {
            FileNameExtensionFilter filter = new FileNameExtensionFilter(
                    "." + type, type);
            // 设置文件类型
            chooser.setFileFilter(filter);
        } else {
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        }
        // 打开选择器面板
        int returnVal = chooser.showOpenDialog(new JPanel());
        // 保存文件从这里入手，输出的是文件名
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            String path = chooser.getSelectedFile().getAbsolutePath();
            tf.setText(path);
            if (listener != null) {
                listener.onFileSelect(path);
            }
            return path;
        }
        return "";
    }

    /**
     * 判断给定路径文件是否是pdf
     *
     * @param s     要判断的字符串
     */
    protected boolean isPdf(String s) {
        return !isEmpty(s) && (s.endsWith(".pdf") || s.endsWith(".PDF"));
    }

    /**
     * 判断给定字符串是否为空
     *
     * @param s     要判断的字符串
     */
    protected boolean isEmpty(String s) {
        return s == null || "".equals(s);
    }

    /**
     * 判断给定字符串是否为纯数字
     *
     * @param s     要判断的字符串
     */
    protected boolean isDigit(String s) {
        return !isEmpty(s) && s.matches("[0-9]+");
    }

    /**
     * 交换两个数
     *
     * @param a     待交换的数
     * @param b     待交换的数
     */
    protected void swap(int a, int b) {
        a = a ^ b ^ (b = a);
    }

    /**
     * 文件选择监听
     */
    public interface OnFileSelectListener {
        void onFileSelect(String path);
    }

}

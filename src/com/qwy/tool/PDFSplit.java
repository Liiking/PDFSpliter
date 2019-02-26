package com.qwy.tool;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfReader;
import org.pdfbox.pdmodel.PDDocument;
import org.pdfbox.util.PDFTextStripper;

import javax.swing.*;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;

/**
 * Created by qianweiying on 2018/7/9.
 * PDF截取小工具
 */
public class PDFSplit extends BaseClazz {

    private JTextField pdfPath;
    private JButton btnSelect;
    private JButton btnSplit;
    private JLabel labelPdf;
    private JPanel panelPage;
    private JPanel panelPath;
    private JPanel rootPanel;
    private JTextArea taLog;
    private JButton btnClear;
    private JSpinner spinner1;
    private JSpinner spinner2;
    private JLabel labelFrom;
    private JLabel labelTo;
    private JButton btnTransToWord;

    private String message = "正在执行，请稍后";
    private boolean isExec = false;
    private int totalPage = 0;// 总页数

    public PDFSplit() {
        initSpinner(spinner1);
        initSpinner(spinner2);
        taLog.setLineWrap(true);// 激活自动换行功能
        taLog.setWrapStyleWord(true);// 激活断行不断字功能
        outputUI(taLog);
        btnClear.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isExec) {
                    showMessage(message);
                    return ;
                }
                taLog.setText("");
            }
        });

        btnSelect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (isExec) {
                    showMessage(message);
                    return ;
                }
                fileChooser("pdf", pdfPath, new OnFileSelectListener() {
                    @Override
                    public void onFileSelect(String path) {
                        if (isPdf(path)) {
                            selectPDF(path);
                        }
                    }
                });
            }
        });

        btnSplit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                String pdf = pdfPath.getText();
                if (isEmpty(pdf)) {
                    showMessage("请先选取PDF文件！");
                    return ;
                }
                if (totalPage < 1) {
                    showMessage("文件读取失败，请重新选取！");
                    return ;
                }
                String start = spinner1.getValue().toString();// tfStart.getText();
                String end = spinner2.getValue().toString();// tfEnd.getText();
                int from = 0, to = 0;
                if (isDigit(start)) {
                    from = Integer.parseInt(start);
                } else {
                    print("输入的起始页数无效，将从第1页开始截取");
                }
                if (isDigit(end)) {
                    to = Integer.parseInt(end);
                } else {
                    print("输入的终止页数无效，将截取到最后一页");
                }
                if (from < 1) {
                    from = 1;
                }
                if (to < 1) {
                    to = totalPage;
                }
                if (isPdf(pdf)) {
                    splitPDFFile(pdf, from, to);
                } else {
                    showMessage("请先选取有效的PDF文件！");
                }
            }
        });

        // 拖拽填充路径，判断是PDF文件
        pdfPath.setTransferHandler(new TransferHandler() {
            private static final long serialVersionUID = 1L;
            @Override
            public boolean importData(JComponent comp, Transferable t) {
                try {
                    Object o = t.getTransferData(DataFlavor.javaFileListFlavor);
                    String filepath = o.toString();
                    if (filepath.startsWith("[")) {
                        filepath = filepath.substring(1);
                    }
                    if (filepath.endsWith("]")) {
                        filepath = filepath.substring(0, filepath.length() - 1);
                    }
                    if (isPdf(filepath)) {
                        pdfPath.setText(filepath);
                        selectPDF(filepath);
                    }
                    return true;
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            }
            @Override
            public boolean canImport(JComponent comp, DataFlavor[] flavors) {
                for (int i = 0; i < flavors.length; i++) {
                    if (DataFlavor.javaFileListFlavor.equals(flavors[i])) {
                        return true;
                    }
                }
                return false;
            }
        });

        btnTransToWord.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                // 将选中PDF转成word
                pdf2Word();
            }
        });
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("PDF截取小工具");
        JPanel rootPane = new PDFSplit().rootPanel;
        frame.setContentPane(rootPane);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setSize(550, 550);
        frame.setResizable(false);
        frame.setLocationRelativeTo(rootPane);// 居中
        frame.setVisible(true);
    }

    /**
     * 将选中的PDF转成word
     */
    private void pdf2Word() {
        String pdf = pdfPath.getText();
        if (isEmpty(pdf)) {
            showMessage("请先选取PDF文件！");
            return ;
        }
        if (totalPage < 1) {
            showMessage("文件读取失败，请重新选取！");
            return ;
        }
        if (isExec) {
            showMessage(message);
            return ;
        }
        isExec = true;
        print("--------------- 开始转换 ---------------");
        String start = spinner1.getValue().toString();// tfStart.getText();
        String end = spinner2.getValue().toString();// tfEnd.getText();
        int from = 0, to = 0;
        if (isDigit(start)) {
            from = Integer.parseInt(start);
        }
        if (isDigit(end)) {
            to = Integer.parseInt(end);
        }
        if (from < 1) {
            from = 1;
        }
        if (to < 1) {
            to = totalPage;
        }
        try {
            String savepath = pdf.substring(0, pdf.lastIndexOf(File.separator)) + "/target_" + from + "-" + end + "_" + System.currentTimeMillis() + ".doc";
            PDDocument doc = PDDocument.load(new File(pdf));
            File docF = new File(savepath);
            if (!docF.exists()) {
                docF.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(savepath);
            Writer writer = new OutputStreamWriter(fos, "UTF-8");
            PDFTextStripper stripper = new PDFTextStripper();
//      doc.addSignature(arg0, arg1, arg2);
            stripper.setSortByPosition(true);// 排序
            //stripper.setWordSeparator("");// pdfbox对中文默认是用空格分隔每一个字，通过这个语句消除空格（视频是这么说的）
            stripper.setStartPage(from);// 设置转换的开始页
            stripper.setEndPage(to);// 设置转换的结束页
            stripper.writeText(doc, writer);
            writer.close();
            doc.close();
            print("pdf转换word成功！保存路径：" + savepath);
            print("--------------- 转换结束 ---------------");
        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
        } catch (UnsupportedEncodingException uee) {
            uee.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            isExec = false;
        }
    }

    /**
     * 选取了PDF文件 打印日志
     *
     * @param path      文件路径
     */
    private void selectPDF(String path) {
        print("选取PDF文件：" + path);
        loadPDFFile(path);
    }

    /**
     * 加载PDF文件，获取文件总页数
     *
     * @param pdf       要加载的PDF文件路径
     */
    public void loadPDFFile(String pdf) {
        try {
            PdfReader reader = new PdfReader(pdf);
            totalPage = reader.getNumberOfPages();
            print("===文件读取成功，共" + totalPage + "页===");
            spinner1.setModel(new SpinnerNumberModel(1, 1, totalPage, 1));
            spinner2.setModel(new SpinnerNumberModel(totalPage, 1, totalPage, 1));
            initSpinner(spinner1);
            initSpinner(spinner2);
        } catch (IOException e) {
            e.printStackTrace();
            totalPage = -1;
        }
    }

    /**
     * 初始化spinner，设置只能输入数字
     *
     * @param spinner           要初始化的spinner
     */
    private void initSpinner(JSpinner spinner) {
        JSpinner.NumberEditor editor = new JSpinner.NumberEditor(spinner, "0");
        spinner.setEditor(editor);
        JFormattedTextField textField = ((JSpinner.NumberEditor) spinner.getEditor())
                .getTextField();
        textField.setEditable(false);
//        DecimalFormat format = ((JSpinner.NumberEditor) spinner.getEditor()).getFormat();
//        format.setMaximumFractionDigits(max);
//        format.setMinimumFractionDigits(1);
        DefaultFormatterFactory factory = (DefaultFormatterFactory) textField
                .getFormatterFactory();
        NumberFormatter formatter = (NumberFormatter) factory.getDefaultFormatter();
        formatter.setAllowsInvalid(false);
    }

    /**
     * 截取pdfFile的第from页至第end页，组成一个新的文件名
     *
     * @param respdfFile  需要分割的PDF
     * @param from  起始页
     * @param end  结束页
     */
    public void splitPDFFile(String respdfFile, int from, int end) {
        if (isExec) {
            showMessage(message);
            return ;
        }
        isExec = true;
        Document document = null;
        PdfCopy copy = null;
        String savepath;
        try {
            print("----开始截取文件----");
            PdfReader reader = new PdfReader(respdfFile);
            int n = reader.getNumberOfPages();
            if (end == 0) {
                end = n;
            }
            if (end < 1) {
                return ;
            }
            if (from < 1) {
                from = 1;
            }
            if (from > end) {
                swap(from, end);
            }
            savepath = respdfFile.substring(0, respdfFile.lastIndexOf(File.separator)) + "/target_" + from + "-" + end + "_" + System.currentTimeMillis() + ".pdf";
            ArrayList<String> savepaths = new ArrayList<String>();
            String staticpath = respdfFile.substring(0, respdfFile.lastIndexOf("\\") + 1);
            //String savepath = staticpath+ newFile;
            savepaths.add(savepath);
            document = new Document(reader.getPageSize(1));
            copy = new PdfCopy(document, new FileOutputStream(savepaths.get(0)));
            document.open();
            for (int j = from;j <= end; j++) {
                print("正在截取第" + j + "页...");
                document.newPage();
                PdfImportedPage page = copy.getImportedPage(reader, j);
                copy.addPage(page);
            }
            document.close();
            print("---截取成功！文件路径：" + savepath);
        } catch (IOException e) {
            e.printStackTrace();
            isExec = false;
        } catch (DocumentException e) {
            e.printStackTrace();
            isExec = false;
        } finally {
            isExec = false;
        }
    }

}

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Autumn on 2018/4/13.
 */
public class PdfWaterMark {
    private JButton btnSelect;
    private JList list1;
    private JList list2;
    private JPanel mainPnl;
    private JPanel btnPnl;
    private JButton btnOK;

    static PdfWaterMark pdfWaterMark = null;
    static DefaultListModel dlmCommon = new DefaultListModel();
    static DefaultListModel dlmError = new DefaultListModel();
    static File root = null;

    public static void main(String[] args) {
        JFrame frame = new JFrame("水印添加工具");
        frame.setIconImage(new ImageIcon("src/main/resources/pdf.png").getImage());    //icon
        frame.setSize(400,300);    //窗口大小
        frame.setLocation(200,200);    //窗口位置

        pdfWaterMark = new PdfWaterMark();
        JPanel panel = pdfWaterMark.mainPnl;
        JPanel bpnl = pdfWaterMark.btnPnl;
        bpnl.setBounds(20,20,100,150);
        pdfWaterMark.list1.setModel(dlmCommon);
        pdfWaterMark.list2.setModel(dlmError);
        pdfWaterMark.btnSelect.setSize(50,30);

        frame.setContentPane(panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    public PdfWaterMark() {
        btnSelect.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = new JFileChooser();    //文件选择器
                chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);   //可选择文件和文件夹
                chooser.setDialogTitle("选择根文件夹");     //对话框title
                chooser.showDialog(new Label(), "选择");   //确定按钮text
                root = chooser.getSelectedFile();   //获取选择文件or文件夹
                if(root!=null){
                    pdfWaterMark.btnSelect.setText(root.getAbsolutePath());
                }else {
                    pdfWaterMark.btnSelect.setText("选择根文件夹");
                }

            }
        });


        btnOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(root == null){
                    JOptionPane.showMessageDialog(null, "请选择跟目录", "提示", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                //获取配置文件路径
                String iniPath = getIni(root);   //获取根文件夹中的ini配置文件绝对路径,返回zero或者more时为异常
                if (iniPath.equals("more")){
                    JOptionPane.showMessageDialog(null, "请确保只有一个ini配置文件!", "错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }else if(iniPath.equals("zero")){
                    JOptionPane.showMessageDialog(null, "无ini配置文件!", "错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (root.isDirectory()) {   //判断选择的文件是否为文件夹
                    File[] pdfDirs = root.listFiles();   //获取根文件夹下的所有文件pdfDirs(文件夹和.ini)
                    for (File pdfDir : pdfDirs) {    //遍历pdfDirs
                        if (pdfDir.isDirectory()){    //当时文件夹时
                            String dirName = pdfDir.getName();   //pdfDir文件夹名
                            String detailInfo = null;   //水印详细信息
                            if((detailInfo=isContaint(dirName,iniPath)) != null){   //配置文件中是否有文件名,如果有,则返回所有信息
                                File[] pdfs = pdfDir.listFiles( new FilenameFilter() {    //获得pdfDir文件夹下所有的pdf文件
                                    public boolean accept(File dir, String name) {
                                        if (name.endsWith(".pdf")){    //只过滤.ini文件结尾的文件
                                            return true;
                                        }
                                        return false;
                                    }
                                });    //将pdfDir所有的文件列出来
                                if(pdfs == null || pdfs.length ==0){
                                    dlmError.addElement(pdfDir.getAbsoluteFile()+"文件夹下无pdf文件");
                                }
                                for (File pdf:pdfs){   //遍历pdf文件
                                    String inPath = pdf.getAbsolutePath();    //pdf绝对路径
                                    String outPath = pdf.getParent()+"\\"+System.currentTimeMillis()+pdf.getName(); //水印pdf绝对路径
                                    try {
                                        new PdfUtil().addWaterMark(inPath,outPath,detailInfo,20,10);   //加水印
                                        new PdfUtil().delFile(inPath);   //删除源文件
                                        new PdfUtil().reNameFile(outPath,inPath);   //将水印文件名重命名为源文件名
                                        dlmCommon.addElement(inPath+"水印添加成功");
                                    } catch (Exception e1) {
                                        e1.printStackTrace();
                                    }
                                }
                            }else{
                                dlmError.addElement("ini配置文件中无 "+dirName+" 信息");
                            }
                        }
                    }
                } else {   //选择的不是文件夹
                    JOptionPane.showMessageDialog(null, "请选择根文件夹", "警告", JOptionPane.WARNING_MESSAGE);
                }
                //执行完毕后将错误日志打印到errorLog.txt
                try {
                    File errParent = new File(new File("").getCanonicalPath());   //获取项目路径
                    File errLog = new File(errParent.getParent()+"/errLog.txt");  //项目父级
                    if(!errLog.exists()){     //无文件则创建
                        errLog.createNewFile();
                    }
                    FileOutputStream fw = new FileOutputStream(errLog,true);
                    OutputStreamWriter ost = new OutputStreamWriter(fw);
                    BufferedWriter bw = new BufferedWriter(ost);

                    Object[] arr = dlmError.toArray();
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-hh:mm:ss ");
                    String curtime = simpleDateFormat.format(new Date(System.currentTimeMillis())).toString();
                    bw.write(curtime+"\r\n");
                    for (Object obj : arr){
                        bw.write(new String((obj.toString()+"\r\n").getBytes(),"utf-8"));
                    }
                    bw.write("\r\n");
                    bw.close();
                    ost.close();
                    fw.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }

            }
        });
    }

    /**
     * 根据根目录文件夹获取ini配置文件路径
     * @param rootDir  根目录文件夹
     * @return more 多个配置文件
     *          zero 无配置文件
     *          返回ini绝对路径
     */
    public String getIni(File rootDir){

        FilenameFilter filenameFilter = new FilenameFilter() {    //文件名过滤器
            public boolean accept(File dir, String name) {
                if (name.endsWith(".ini")){    //只过滤.ini文件结尾的文件
                    return true;
                }
                return false;
            }
        };

        File[] files = rootDir.listFiles(filenameFilter);    //获取文件夹中的ini配置文件
        if (files.length > 1){
            return "more";
        }else if(files.length == 0){
            return "zero";
        }else{
            for (File file:files){
                System.out.println("找到配置文件:"+file.getAbsolutePath());
                return file.getAbsolutePath();
            }
        }
        return null;
    }

    /**
     * 判断dirName是否存在iniPath配置文件中
     * @param dirName  文件名
     * @param iniPath  配置文件绝对路径
     * @return
     */
    public String isContaint(String dirName,String iniPath){
        try {
            FileReader fileReader = new FileReader(new File(iniPath));   //FileReader封装File
            BufferedReader bufferedReader = new BufferedReader(fileReader);  //BufferedReader封装FileReader
            String line = null;
            //逐行读取配置文件
            while ((line=bufferedReader.readLine())!=null){
                if (new String(line.replace(" ","").getBytes(),"utf-8").contains(dirName)){  //配置文件中按空格分开信息，将所有信息串为一个字符串
                    System.out.println(dirName+"找到"+line);
                    return line;   //返回文件名在配置文件中的所有信息
                }
            }
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return null;
    }

}

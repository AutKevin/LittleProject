import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Element;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;

import java.io.*;

/**
 * PDF工具类
 */
public class PdfUtil {
    public static void main(String[] args) throws Exception {
        String inPath = "F://PDF//test3.pdf";
        String outPath = "F://PDF//test6.pdf";
        new PdfUtil().addWaterMark(inPath,outPath,"这是一个水印",20,10);    //添加水印
        new PdfUtil().delFile(inPath);     //删除源文件
        new PdfUtil().reNameFile(outPath,inPath);   //将水印图片重命名为源文件名
    }

    /**
     *
     * 【功能描述：添加图片和文字水印】 【功能详细描述：功能详细描述】
     * @param srcFile 待加水印文件
     * @param destFile 加水印后存放地址
     * @param text 加水印的文本内容
     * @param textWidth 文字横坐标,起点为左下角
     * @param textHeight 文字纵坐标,起点为左下角s
     * @throws Exception
     */
    public void addWaterMark(String srcFile, String destFile, String text,
                             int textWidth, int textHeight) throws Exception
    {
        // 待加水印的文件
        PdfReader reader = new PdfReader(srcFile);
        // 加完水印的文件
        PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(
                destFile));
        int total = reader.getNumberOfPages();   //总页数
        PdfContentByte content;
        // 设置字体
        BaseFont font = BaseFont.createFont("C:/Windows/Fonts/SIMYOU.TTF",BaseFont.IDENTITY_H,BaseFont.NOT_EMBEDDED);
        // 循环对每页插入水印
        for (int i = 1; i <= total; i++)
        {
            // 水印的起始
            content = stamper.getUnderContent(i);
            // 开始
            content.beginText();
            // 设置颜色 默认为蓝色
            content.setColorFill(BaseColor.BLACK);
            // 设置字体及字号
            content.setFontAndSize(font, 8);
            // 设置起始位置
            content.setTextMatrix(textWidth, textHeight);
            // 开始写入水印
            content.showTextAligned(Element.ALIGN_LEFT, text, textWidth,textHeight, 0);
            content.endText();
        }
        stamper.close();
        reader.close();
    }

    /**
     * 复制文件
     * @param srcFile
     * @param destFile
     * @throws IOException
     */
    public void copyFile(String srcFile, String destFile) throws IOException {
        File file1 = new File(srcFile);
        File file2 = new File(destFile);
        InputStream in = null;
        OutputStream out = null;
        if (file1 != null && file1.isFile()) {
            in = new FileInputStream(file1);
        }else{
            System.out.println("复制源文件失败!");
            throw new FileNotFoundException(srcFile);
        }
        if (file2 != null) {
            out = new FileOutputStream(file2);
        }

        byte[] bytes = new byte[1024*4];
        int len;
        while ((len =in.read(bytes))>-1){
            out.write(bytes,0,len);
        }
        out.close();
        in.close();
    }

    /**
     * 重命名
     * @param srcFile
     * @param destFile
     * @return
     * @throws IOException
     */
    public boolean reNameFile(String srcFile, String destFile) throws IOException {
        File file1 = new File(srcFile);
        File file2 = new File(destFile);
        System.out.println(srcFile+" 重命名为 "+destFile+" 成功");
        return file1.renameTo(file2);
    }

    /**
     * 删除
     * @param srcFile
     * @return
     * @throws IOException
     */
    public boolean delFile(String srcFile) throws IOException {
        File file1 = new File(srcFile);
        System.out.println("删除 "+srcFile+" 成功");
        return file1.delete();
    }

}

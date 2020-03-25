package nbkj.cht.bletest;

public class MyFileItem {
    private String fileName;
    private String fileSize;


    public MyFileItem(String fileName, String fileSize) {
        super();
        this.fileName = fileName;
        this.fileSize = fileSize;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFileSize() {
        return fileSize;
    }

}

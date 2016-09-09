import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.MultipartPostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.params.HttpClientParams;

import javax.swing.*;
import javax.swing.Timer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

/**
 * Created by Admin on 2016/9/6 0006.
 */
public class SelectApkDialog extends JFrame {
    private JPanel contentPanel;
    private JTextField txtApiKey;
    private JTextField txtUKey;
    private JLabel apiKey;
    private JLabel lblLog;
    private JLabel uerKey;
    private JComboBox combProject;
    private JTextArea txtLog;
    private JLabel lblProject;
    private JLabel lblTips;
    private JButton btnPath;
    private JLabel lblPath;
    private JButton btnNext;
    private JButton btnCancle;
    private JLabel lblApkPath;
    private JProgressBar pbar;
    private Project mProject;
    Module[] modules;
    private ProgressMonitor progressDialog;
    private SimThread thread;
    private Timer timer;

    public SelectApkDialog(Project project) {
        this.mProject = project;
        modules = ModuleManager.getInstance(project).getModules();
//        Messages.showMessageDialog(project.getProjectFilePath(), "", Messages.getErrorIcon());
        setContentPane(contentPanel);
        setTitle("MeTools");
        this.setAlwaysOnTop(true);
        for (Module m : modules
                ) {
            combProject.addItem(m.getName());
        }
        initListener();
    }

    private void initListener() {
        btnPath.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                browseForFile();
                //FileChooser f=new FileChooser();
//                //初始化文件选择框
//                JFileChooser fDialog = new JFileChooser();
//                //设置文件选择框的标题
//                fDialog.setDialogTitle("选择apk文件上传到MeTool");
//                //弹出选择框
//                int returnVal = fDialog.showOpenDialog(btnPath);
//                // 如果是选择了文件
//                if (JFileChooser.APPROVE_OPTION == returnVal) {
//                    //打印出文件的路径，你可以修改位 把路径值 写到 textField 中
//                    System.out.println(fDialog.getSelectedFile());
//                }
            }
        });
        btnCancle.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        btnNext.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //sendUrlData("https://www.pgyer.com/apiv1/app/upload ", lblApkPath.getText(), txtLog.getText());
                thread = new SimThread(1000);
                thread.start();
                progressDialog = new ProgressMonitor(SelectApkDialog.this, "Waiting...", null, 0, thread.getTarget());
                timer.start();
                btnNext.setEnabled(false);
            }
        });
        timer = new Timer(500, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int cur = thread.getCurrent();
                //textArea.append(cur + "\n");
                progressDialog.setProgress(cur);
                if (cur == thread.getTarget() || progressDialog.isCanceled()) {
                    timer.stop();
                    progressDialog.close();
                    thread.interrupt();
                    btnNext.setEnabled(true);
                }
            }
        }
        );
    }

    private void browseForFile() {
        FileChooserDescriptor descriptor = FileChooserDescriptorFactory.createSingleFileDescriptor();
        descriptor.setTitle("APK文件");
        descriptor.setDescription("选择apk文件上传到MeTool");
        descriptor.setHideIgnored(false);
        // 10.5 does not have #chooseFile
        VirtualFile[] virtualFiles = FileChooser.chooseFiles(descriptor, btnPath, mProject, null);
        if (virtualFiles != null && virtualFiles.length > 0) {
            lblApkPath.setText(virtualFiles[0].getPath());
        }
    }

    public int sendUrlData(String url, String path, String info) {
        File targetFile = new File(path);
        MultipartPostMethod filePost = new MultipartPostMethod(url);
        HttpClient client = null;
        long startdate = new Date().getTime();
        System.out.println("开始时间：" + startdate);
        int status = 0;
        try {
            client = new HttpClient();
            FilePart part = new FilePart("file", targetFile);
            part.setCharSet("utf-8 ");
            part.setContentType("application/octet-stream ");
            filePost.addPart(part);
            filePost.addParameter("uKey", "bb131d3f9344ba45a48a6ddbd8131ea2");
            filePost.addParameter("_api_key", "0708676afb2dde5d4e815f883644481");
            filePost.addParameter("updateDescription", info);//更新说明
            HttpClientParams httparams = new HttpClientParams();
            httparams.setSoTimeout(60000);
            client.setParams(httparams);

            status = client.executeMethod(filePost);
            filePost.releaseConnection();
        } catch (FileNotFoundException e) {
            status = 0;
        } catch (HttpException e) {
            status = 0;
        } catch (IOException e) {
            status = 0;
        }
        long endDate = new Date().getTime();
        System.out.println("时间差：" + (endDate - startdate) / 1000 + "=========" + status);
        return status;
    }

    class SimThread extends Thread {
        private int current;
        private int target;

        public SimThread(int t) {
            current = 0;
            target = t;
        }

        public int getTarget() {
            return this.target;
        }

        public int getCurrent() {
            return this.current;
        }

        @SuppressWarnings("static-access")
        public void run() {
            try {
                while (current < target) {
                    this.sleep(100);
                    current++;
                }
            } catch (InterruptedException e) {
            }
        }
    }
}

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.charset.*;
import java.util.*;
import javax.swing.*;


/**
 * Java GUI program to set static IP parameters in /etc/dhcpcd.conf
 *
 * @author Knute Johnson
 * @version 0.71beta
 */
public class IPConfig extends JPanel {
    /** Program version */
    public static final String VERSION = "0.71beta";

    /** Color raspberry */
    public static final Color RASPBERRY = new Color(0,0,0);

    /** dhcpcd.conf file */
    private final File CONF_FILE = new File("/etc/dhcpcd.conf");

    /** Temporary file */
    private final File TEMP_FILE = new File(
     System.getProperty("user.home"),"temp"); 

    /** Script file name */
    private final String SCRIPT_NAME =
     System.getProperty("user.home") + "/ipconfig-script";

    /** Script file */
    private final File SCRIPT_FILE = new File(SCRIPT_NAME);
    
    /** List to hold lines from dhcpcd.conf file */
    private final java.util.List<String> LINES = new ArrayList<>();

    /** JMenuBar reference */
    private final JMenuBar menuBar;

    /** eth0 address field */
    private final JTextField eth0AddressField;

    /** eth0 bits field */
    private final JTextField eth0BitsField;

    /** eth0 routers field */
    private final JTextField eth0RoutersField;

    /** eth0 domain name servers field */
    private final JTextField eth0NameServersField;

    /** wlan0 address field */
    private final JTextField wlan0AddressField;

    /** wlan0 bits field */
    private final JTextField wlan0BitsField;

    /** wlan0 routers field */
    private final JTextField wlan0RoutersField;

    /** wlan0 domain name servers field */
    private final JTextField wlan0NameServersField;

    /** index of interface eth0 line */
    private int eth0;

    /** index of interface wlan0 line */
    private int wlan0;

    /**
     * Creates a new IPConfig GUI
     *
     * @param   frame a reference to the containing JFrame
     *
     * @throws  IOException if an I/O error occurs reading the dhcpcd.conf file
     */
    public IPConfig(JFrame frame) throws IOException {
        super(new GridBagLayout());
        
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5,15,5,15);
        c.gridx = c.gridy = 0;
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridwidth = 2;  c.anchor = GridBagConstraints.CENTER;
        JLabel l = new RaspberryJLabel("Ethernet IP (eth0)",JLabel.LEFT);
		l.setBorder(BorderFactory.createEmptyBorder(15,0,0,0));
        add(l,c);

        ++c.gridy; c.gridwidth = 1;  c.anchor = GridBagConstraints.WEST;
        l = new RaspberryJLabel("IPv4 Address:",JLabel.RIGHT);
        add(l,c);

        ++c.gridx;
        JPanel eth0Panel = new JPanel(new FlowLayout(FlowLayout.LEFT,0,0));
        eth0AddressField = new RaspberryJTextField(12);
        eth0Panel.add(eth0AddressField);

        l = new RaspberryJLabel(" / ");
        eth0Panel.add(l);

        eth0BitsField = new RaspberryJTextField(2);
        eth0Panel.add(eth0BitsField);
        add(eth0Panel,c);

        c.gridx = 0;  ++c.gridy;
        l = new RaspberryJLabel("Router:",JLabel.RIGHT);
        add(l,c);

        ++c.gridx;
        eth0RoutersField = new RaspberryJTextField(12);
        add(eth0RoutersField,c);

        c.gridx = 0;  ++c.gridy;
        l = new RaspberryJLabel("DNS Servers:",JLabel.RIGHT);
        add(l,c);

        ++c.gridx;
        eth0NameServersField = new RaspberryJTextField(12);
        add(eth0NameServersField,c);

        c.gridx = 0;  ++c.gridy;
        c.gridwidth = 2;  c.anchor = GridBagConstraints.CENTER;
        l = new RaspberryJLabel("Wi-Fi IP (wlan0)",JLabel.LEFT);        
        l.setBorder(BorderFactory.createEmptyBorder(25, 0, 0, 0));        
        add(l,c);

        ++c.gridy; c.gridwidth = 1;  c.anchor = GridBagConstraints.WEST;
        l = new RaspberryJLabel("IPv4 Address:",JLabel.RIGHT);
        add(l,c);

        ++c.gridx;
        JPanel wlan0Panel = new JPanel(new FlowLayout(FlowLayout.LEFT,0,0));
        wlan0AddressField = new RaspberryJTextField(12);
        wlan0Panel.add(wlan0AddressField);

        l = new RaspberryJLabel(" / ");
        wlan0Panel.add(l);

        wlan0BitsField = new RaspberryJTextField(2);
        wlan0Panel.add(wlan0BitsField);
        add(wlan0Panel,c);

        c.gridx = 0;  ++c.gridy;
        l = new RaspberryJLabel("Router:",JLabel.RIGHT);
        add(l,c);

        ++c.gridx;
        wlan0RoutersField = new RaspberryJTextField(12);
        add(wlan0RoutersField,c);

        c.gridx = 0;  ++c.gridy;
        l = new RaspberryJLabel("DNS Servers:",JLabel.RIGHT);
        add(l,c);

        ++c.gridx;
        wlan0NameServersField = new RaspberryJTextField(12);
        add(wlan0NameServersField,c);

        c.gridx = 0;  ++c.gridy;  c.gridwidth = 2;
        c.anchor = GridBagConstraints.EAST;
        c.fill = GridBagConstraints.NONE;
        c.insets = new Insets(5,15,10,15);
        JButton b = new JButton("Save");
        b.setForeground(RASPBERRY);
        b.addActionListener(event -> {
            depopulateFields();
            try {
                // create temporary file with the lines from /etc/dhpcd.conf
                // as modified
                try (BufferedWriter bw = new BufferedWriter(
                 new FileWriter(TEMP_FILE))) {
                    for (String line : LINES) {
                        bw.write(line);
                        bw.newLine();
                    }
                }

                // create the script file
                createScriptFile(dhcpcdScript());

                // run the script and capture stdout and stderr to the terminal
                runScript();
            } catch (IOException ioe) {
                JOptionPane.showMessageDialog(IPConfig.this,ioe,
                 "Error saving dhcpcd.conf file",JOptionPane.ERROR_MESSAGE);
                ioe.printStackTrace();
            }
        });
        add(b,c);

        // create menu bar and menus
        menuBar = new JMenuBar();
        JMenu file = menuBar.add(new JMenu("File"));
        file.setForeground(RASPBERRY);
        JMenu edit = menuBar.add(new JMenu("Edit"));
        edit.setForeground(RASPBERRY);
        JMenu help = menuBar.add(new JMenu("Help"));
        help.setForeground(RASPBERRY);
        JMenuItem mi;

        mi = edit.add("Clear eth0");
        mi.setForeground(RASPBERRY);
        mi.addActionListener(event -> {
            eth0AddressField.setText("");
            eth0BitsField.setText("");
            eth0RoutersField.setText("");
            eth0NameServersField.setText("");
        });

        mi = edit.add("Clear wlan0");
        mi.setForeground(RASPBERRY);
        mi.addActionListener(event -> {
            wlan0AddressField.setText("");
            wlan0BitsField.setText("");
            wlan0RoutersField.setText("");
            wlan0NameServersField.setText("");
        });
       
        mi = edit.add("Recover");
        mi.setForeground(RASPBERRY);
        mi.addActionListener(event -> {
            try {
                // write data to temp file
                try (BufferedWriter bw = new BufferedWriter(
                 new FileWriter(TEMP_FILE))) {
                    bw.write(interfaces());
                }

                // create the recover script
                createScriptFile(recoverScript());
    
                // run the script
                runScript();
            } catch (IOException ioe) {
                JOptionPane.showMessageDialog(file,
                 "Error attempting to recover /etc/network/interfaces\n" + ioe,
                 "IPConfig",JOptionPane.ERROR_MESSAGE);
                ioe.printStackTrace();
            }
        });

        file.addSeparator();
        mi = file.add("Quit");
        mi.setForeground(RASPBERRY);
        mi.addActionListener(event -> frame.dispose());

        mi = help.add("Directions");
        mi.setForeground(RASPBERRY);
        mi.addActionListener(event ->
         JOptionPane.showMessageDialog(help,new JLabel(directions()),
         "IPConfig",JOptionPane.INFORMATION_MESSAGE));
        help.addSeparator();

        mi = help.add("About");
        mi.setForeground(RASPBERRY);
        mi.addActionListener(event -> JOptionPane.showMessageDialog(help,
         new RaspberryJLabel(
         "<html>IPConfig - Version " + VERSION + "<br>\n" +
         "A program to set static IP parameters in /etc/dhcpcd.conf<br>\n" +
         "Written by: Knute Johnson"),
         "About IPConfig",JOptionPane.INFORMATION_MESSAGE));

        loadConfFile();
        populateFields();
    }

    /**
     * Gets a reference to the program JMenuBar
     *
     * @return program JMenuBar
     */
    public JMenuBar getJMenuBar() {
        return menuBar;
    }

    /**
     * Loads the dhcpcd.conf file data into the LINES array list
     *
     * @throws IOException if an I/O error occurs reading the dhcpcd.conf file
     */
    private void loadConfFile() throws IOException {
        try (BufferedReader br = new BufferedReader(
         new FileReader(CONF_FILE))) {
            String str;
            while ((str = br.readLine()) != null)
                    LINES.add(str);
        }
    }

    /**
     * Populates the GUI data fields with the appropriate data from the
     * dhcpcd.conf file
     */
    private void populateFields() {
        // find eth0 data
        for (int i=0; i<LINES.size(); i++) {
            if (LINES.get(i).trim().equals("interface eth0")) {
                eth0 = i;
                if (LINES.get(i+1).trim().startsWith("static ip_address=")) {
                    String arr[] = LINES.get(i+1).split("=");
                    String arr2[] = arr[1].split("/");
                    eth0AddressField.setText(arr2[0]);
                    eth0BitsField.setText(arr2[1]);
                }
                if (LINES.get(i+2).trim().startsWith("static routers=")) {
                    String arr[] = LINES.get(i+2).split("=");
                    eth0RoutersField.setText(arr[1]);
                }
                if (LINES.get(i+3).trim().startsWith(
                 "static domain_name_servers=")) {
                    String arr[] = LINES.get(i+3).split("=");
                    eth0NameServersField.setText(arr[1]);
                }
            }

            // find wlan0 data
            if (LINES.get(i).trim().equals("interface wlan0")) {
                wlan0 = i;
                if (LINES.get(i+1).trim().startsWith("static ip_address=")) {
                    String arr[] = LINES.get(i+1).split("=");
                    String arr2[] = arr[1].split("/");
                    wlan0AddressField.setText(arr2[0]);
                    wlan0BitsField.setText(arr2[1]);
                }
                if (LINES.get(i+2).trim().startsWith("static routers=")) {
                    String arr[] = LINES.get(i+2).split("=");
                    wlan0RoutersField.setText(arr[1]);
                }
                if (LINES.get(i+3).trim().startsWith(
                 "static domain_name_servers=")) {
                    String arr[] = LINES.get(i+3).split("=");
                    wlan0NameServersField.setText(arr[1]);
                }
            }
        }
    }

    /**
     * Depopulates the data fields and stores the data in the LINES array list
     */
    private void depopulateFields() {
        eth0 = wlan0 = 0;
        // find eth0
        for (int i=0; i<LINES.size(); i++) {
            if (LINES.get(i).trim().startsWith("interface eth0"))
                eth0 = i;
        }
        // if there is no eth0 data add it
        if (eth0 == 0) {
            eth0 = LINES.size();
            LINES.add("interface eth0");
            LINES.add(eth0 + 1,
             "static ip_address=" + eth0AddressField.getText() + "/" +
             eth0BitsField.getText());
            LINES.add(eth0 + 2,"static routers=" + eth0RoutersField.getText());
            LINES.add(eth0 + 3,
             "static domain_name_servers=" + eth0NameServersField.getText());
        // else modify it
        } else {
            LINES.set(eth0 + 1,
             "static ip_address=" + eth0AddressField.getText() + "/" +
             eth0BitsField.getText());
            LINES.set(eth0 + 2,"static routers=" + eth0RoutersField.getText());
            LINES.set(eth0 + 3,
             "static domain_name_servers=" + eth0NameServersField.getText());
        }

        // find wlan0
        for (int i=0; i<LINES.size(); i++) {
            if (LINES.get(i).trim().startsWith("interface wlan0"))
                wlan0 = i;
        }
        // if there is no wlan0 data add it
        if (wlan0 == 0) {
            wlan0 = LINES.size();
            LINES.add("interface wlan0");
            LINES.add(wlan0 + 1,
             "static ip_address=" + wlan0AddressField.getText() + "/" +
             wlan0BitsField.getText());
            LINES.add(wlan0 + 2,
             "static routers=" + wlan0RoutersField.getText());
            LINES.add(wlan0 + 3,
             "static domain_name_servers=" + wlan0NameServersField.getText());
        // else modify it
        } else {
            LINES.set(wlan0 + 1,
             "static ip_address=" + wlan0AddressField.getText() + "/" +
             wlan0BitsField.getText());
            LINES.set(wlan0 + 2,
             "static routers=" + wlan0RoutersField.getText());
            LINES.set(wlan0 + 3,
             "static domain_name_servers=" + wlan0NameServersField.getText());
        }

        // find wlan0 data in the LINES array list
        for (int i=0; i<LINES.size(); i++) {
            if (LINES.get(i).trim().startsWith("interface wlan0"))
                wlan0 = i;
        }
        // if the wlan0 address field has been cleared 
        if (wlan0AddressField.getText().equals("")) {
            // remove the wlan0 data
            for (int i=wlan0; i<wlan0 + 4; i++)
                LINES.remove(wlan0);
        }

        // find the eth0 data in the LINES array list
        for (int i=0; i<LINES.size(); i++) {
            if (LINES.get(i).trim().startsWith("interface eth0"))
                eth0 = i;
        }
        // if the eth0 address field has been cleared
        if (eth0AddressField.getText().equals("")) {
            // remove the eth0 data
            for (int i=eth0; i<eth0 + 4; i++)
                LINES.remove(eth0);
        }
    }

    /**
     * Create the script to write the new /etc/dhcpcd.conf file
     *
     * @return a String containing the script
     */
    private String dhcpcdScript() {
        StringBuilder sb = new StringBuilder();
        sb.append("#!/bin/bash\n");
        sb.append("chmod 664 $HOME/temp\n");
        sb.append("sudo chown root:netdev $HOME/temp\n");
        sb.append("sudo mv $HOME/temp /etc/dhcpcd.conf\n");
        sb.append("echo dhcpcd script complete!\n");
        
		sb.append("sudo service dhcpcd stop\n");
		sb.append("sudo dhclient -r eth0\n");		
		sb.append("sudo dhclient -r wlan0\n");
		sb.append("sudo service dhcpcd start\n");

        return sb.toString();
    }

    /**
     * Create the script to recover /etc/network/interfaces
     *
     * @return a String containing the script
     */
    private String recoverScript() {
        StringBuilder sb = new StringBuilder();
        sb.append("#!/bin/bash\n");
        sb.append("chmod 644 $HOME/temp\n");
        sb.append("sudo chown root:root $HOME/temp\n");
        sb.append("sudo mv $HOME/temp /etc/network/interfaces\n");
        sb.append("echo recover script complete!\n");

        return sb.toString();
    }

    /**
     * Runs the script create by createScript()
     *
     * @throws IOException if an I/O error occurs starting the process
     */
    private void runScript() throws IOException {
        ProcessBuilder pb = new ProcessBuilder(SCRIPT_NAME);
        pb.redirectErrorStream();
        Process p = pb.start();
        InputStream is = p.getInputStream();
        // separate thread to handle stdout/stderr
        new Thread(() -> {
            int c;
            try {
                while ((c = is.read()) != -1)
                    System.out.print((char)c);
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }).start();

        // separate thread to wait for script to complete and delete the
        // script file
        new Thread(() -> {
            try {
                p.waitFor();
            } catch (InterruptedException ie) {
                EventQueue.invokeLater(() ->
                 JOptionPane.showMessageDialog(IPConfig.this,
                 "Thread Interrupted Waiting for Script to Complete",
                 "IPConfig",JOptionPane.WARNING_MESSAGE));
            }
            if (!SCRIPT_FILE.delete())
                EventQueue.invokeLater(() ->
                 JOptionPane.showMessageDialog(IPConfig.this,
                 "Unable to Delete Script File","IPConfig",
                 JOptionPane.ERROR_MESSAGE));
        }).start();
    }

    /**
     * Create a script file from a String containing the script lines, set it
     * executable and delete on exit
     *
     * @param   script the String containing the lines of the script
     * @throws  IOException if an I/O error occurs during file creation
     */
    private void createScriptFile(String script) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(
         new FileWriter(SCRIPT_FILE))) {
            bw.write(script);
        }
        SCRIPT_FILE.setExecutable(true);
    }

    /**
     * Create the directions message data
     *
     * @return String containing program directions
     */
    private String directions() {
        StringBuilder sb = new StringBuilder();
        sb.append("<html>\n");
        sb.append("<head>\n");
        sb.append("    <title>IPconfig Directions</title>\n");
        sb.append("    <style type=\"text/css\">\n");
        sb.append("        body {\n");
        sb.append("            width: 400px;\n");
        sb.append("            color: #e30b5c;\n");
        sb.append("        }\n");
        sb.append("        h1 {\n");
        sb.append("            text-align: center;\n");
        sb.append("        }\n");
        sb.append("</head>\n");
        sb.append("<body>\n");
        sb.append("    <h1>Directions for IPConfig</h1>\n");
        sb.append("    IPConfig is a program to set static IP addresses on ");
        sb.append("a Raspberry Pi running Raspbian Jessie.\n");
        sb.append("    <ul>\n");
        sb.append("        <li>If you have edited your /etc/network/interfa");
        sb.append("ces file, remove all edits or select the Edit/Recover me");
        sb.append("nu option.\n");
        sb.append("        <li>Fill in the static IP parameters for the net");
        sb.append("work device desired - eth0, wlan0 or both and press the ");
        sb.append("Save button.\n");
        sb.append("        <li>To remove static IP from a network device, c");
        sb.append("lear all the fields for that network device and press th");
        sb.append("e Save button. Edit/Clear eth0 or Edit/Clear wlan0\n");
        sb.append("    </ul>\n");
        sb.append("</body>\n");
        sb.append("</html>\n");

        return sb.toString();
    }

    /**
     * Create the /etc/network/interfaces file data
     *
     * @return a String containing the lines of the file
     */
    private String interfaces() {
        StringBuilder sb = new StringBuilder();
            sb.append("# interfaces(5) file used by ifup(8) and ifdown(8)\n");
            sb.append("\n");
            sb.append(
            "# Please note that this file is written to be used with dhcpcd\n");
            sb.append(
           "# For static IP, consult /etc/dhcpcd.conf and 'man dhcpcd.conf'\n");
            sb.append("\n");
            sb.append("# Include files from /etc/network/interfaces.d:\n");
            sb.append("source-directory /etc/network/interfaces.d\n");
            sb.append("\n");
            sb.append("auto lo\n");
            sb.append("iface lo inet loopback\n");
            sb.append("\n");
            sb.append("iface eth0 inet manual\n");
            sb.append("\n");
            sb.append("allow-hotplug wlan0\n");
            sb.append("iface wlan0 inet manual\n");
            sb.append("    wpa-conf /etc/wpa_supplicant/wpa_supplicant.conf\n");
            sb.append("\n");
            sb.append("allow-hotplug wlan1\n");
            sb.append("iface wlan1 inet manual\n");
            sb.append("    wpa-conf /etc/wpa_supplicant/wpa_supplicant.conf");

            return sb.toString();
    }

    /**
     * Raspberry colored JLabel
     */
    private static class RaspberryJLabel extends JLabel {
        public RaspberryJLabel(String text) {
            super(text);
            setForeground(RASPBERRY);
        }

        public RaspberryJLabel(String text, int horizontalAlignment) {
            super(text,horizontalAlignment);
            setForeground(RASPBERRY);
        }
    }

    /**
     * Raspberry colored JTextField
     */
    private static class RaspberryJTextField extends JTextField {
        public RaspberryJTextField(int columns) {
            super(columns);
            setForeground(RASPBERRY);
        }
    }

    /**
     * Main program entry point, creates the containing frame and an IPConfig
     * GUI
     *
     * @param   args command line arguments, not used
     */
    public static void main(String... args) {
        EventQueue.invokeLater(() -> {
            try {
                JFrame frame = new JFrame("IPConfig " + VERSION);
                frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                IPConfig ipconfig = new IPConfig(frame);
                frame.setJMenuBar(ipconfig.getJMenuBar());
                frame.add(ipconfig,BorderLayout.CENTER);
                frame.pack();
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
            } catch (IOException ioe) {
                JOptionPane.showMessageDialog(null,ioe,
                 "Error loading dhcpcd.conf file",JOptionPane.ERROR_MESSAGE);
                ioe.printStackTrace();
            }
        });
    }
}


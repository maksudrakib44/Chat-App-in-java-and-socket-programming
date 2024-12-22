package Simple.group.chatting.application;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.text.*;
import java.util.*;
import java.net.*;
import java.io.*;

public class Client1 implements ActionListener, Runnable {

    JTextField text;
    static JPanel a1;  // Panel to display chat messages
    static Box vertical = Box.createVerticalBox();  // Box for stacking messages
    static JFrame f = new JFrame();
    static DataOutputStream dout;

    JButton emojiButton; // Emoji picker button
    JPopupMenu emojiMenu; // Emoji menu

    BufferedReader reader;
    BufferedWriter writer;
    String name = "Maksud";

    // Variables to track mouse position for dragging
    int mouseX, mouseY;

    Client1() {
        f.setLayout(null);

        // Header panel with user information
        JPanel p1 = new JPanel();
        p1.setBackground(new Color(128, 0, 128));
        p1.setBounds(0, 0, 450, 80);
        p1.setLayout(null);
        f.add(p1);

        // Make the window draggable by adding a MouseAdapter to the header panel
        p1.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                mouseX = e.getX();
                mouseY = e.getY();
            }
        });

        p1.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                int x = e.getXOnScreen();
                int y = e.getYOnScreen();
                f.setLocation(x - mouseX, y - mouseY);
            }
        });

        // Back button to close the client
        ImageIcon i1 = new ImageIcon(ClassLoader.getSystemResource("icons/3.png"));
        Image i2 = i1.getImage().getScaledInstance(25, 25, Image.SCALE_DEFAULT);
        ImageIcon i3 = new ImageIcon(i2);
        JLabel back = new JLabel(i3);
        back.setBounds(5, 20, 25, 25);
        p1.add(back);

        back.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent ae) {
                System.exit(0);
            }
        });

        // Profile icon
        ImageIcon i4 = new ImageIcon(ClassLoader.getSystemResource("icons/gublogo.png"));
        Image i5 = i4.getImage().getScaledInstance(60, 60, Image.SCALE_DEFAULT);
        ImageIcon i6 = new ImageIcon(i5);
        JLabel profile = new JLabel(i6);
        profile.setBounds(40, 5, 70, 70);
        p1.add(profile);

        JLabel nameLabel = new JLabel("GUB CSE-221");
        nameLabel.setBounds(110, 15, 120, 18);
        nameLabel.setForeground(Color.YELLOW);
        nameLabel.setFont(new Font("SAN_SERIF", Font.BOLD, 16));
        p1.add(nameLabel);

        JLabel status = new JLabel("Maksud, Tutul, Pranto, and others");
        status.setBounds(110, 35, 200, 18);
        status.setForeground(Color.WHITE);
        status.setFont(new Font("SAN_SERIF", Font.BOLD, 12));
        p1.add(status);

        // Panel to display chat messages
        a1 = new JPanel();
        a1.setBackground(Color.WHITE);

        // JScrollPane to make chat window scrollable
        JScrollPane scroll = new JScrollPane(a1);
        scroll.setBounds(5, 75, 440, 470);
        f.add(scroll);

        // Text input area for sending messages
        text = new JTextField();
        text.setBounds(5, 580, 250, 45);
        text.setFont(new Font("SAN_SERIF", Font.PLAIN, 16));
        f.add(text);

        // Emoji picker button
        emojiButton = new JButton("😊");
        emojiButton.setBounds(265, 580, 50, 45);
        emojiButton.addActionListener(this);
        f.add(emojiButton);

        // Emoji menu
        emojiMenu = new JPopupMenu();
        String[] emojis = { "😊", "😂", "❤️", "👍", "🔥", "😢", "🙏", "🎉" };
        for (String emoji : emojis) {
            JMenuItem emojiItem = new JMenuItem(emoji);
            emojiItem.addActionListener(e -> text.setText(text.getText() + emoji));
            emojiMenu.add(emojiItem);
        }

        // Send button
        JButton send = new JButton("Send");
        send.setBounds(320, 580, 123, 40);
        send.setBackground(new Color(255, 69, 0));
        send.setForeground(Color.WHITE);
        send.addActionListener(this);
        send.setFont(new Font("SAN_SERIF", Font.PLAIN, 16));
        f.add(send);

        // Finalize window settings
        f.setSize(450, 700);
        f.setLocation(50, 50);
        f.setUndecorated(true);
        f.getContentPane().setBackground(Color.WHITE);
        f.setVisible(true);

        // Try to connect to server
        try {
            Socket socket = new Socket("localhost", 2024);
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Action listener for sending messages and showing emoji picker
    public void actionPerformed(ActionEvent ae) {
        try {
            if (ae.getSource() == emojiButton) {
                emojiMenu.show(emojiButton, emojiButton.getWidth() / 2, emojiButton.getHeight() / 2);
                return;
            }

            // Get message and format for display
            String out = "<html><p>" + name + "</p><p>" + text.getText() + "</p></html>";
            JPanel p2 = formatLabel(out, true);  // Format sent message

            // Layout messages in vertical stack
            a1.setLayout(new BorderLayout());
            JPanel right = new JPanel(new BorderLayout());
            right.setBackground(Color.WHITE);
            right.add(p2, BorderLayout.LINE_END);
            vertical.add(right);
            vertical.add(Box.createVerticalStrut(15));

            a1.add(vertical, BorderLayout.PAGE_START);

            try {
                writer.write(out);
                writer.write("\r\n");
                writer.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }

            text.setText("");  // Clear input field

            f.repaint();
            f.invalidate();
            f.validate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Format the message with sender name in bold and message text below
    public static JPanel formatLabel(String out, boolean isSender) {
        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // Add the sender's name with distinct styling (bold and larger font)
        String namePart = out.split("</p>")[0];
        JLabel nameLabel = new JLabel(namePart);
        nameLabel.setFont(new Font("Tahoma", Font.BOLD, 14));  // Bold font for name
        nameLabel.setForeground(new Color(0, 102, 204));  // Color for the name
        panel.add(nameLabel);

        // Add message text with regular font
        String messagePart = out.substring(out.indexOf("</p>") + 4);  // Extract message
        JLabel messageLabel = new JLabel("<html><p style=\"width: 10px\">" + messagePart + "</p></html>");
        messageLabel.setFont(new Font("Tahoma", Font.PLAIN, 13));

        if (isSender) {
            messageLabel.setBackground(new Color(0, 255, 0)); // Green for sender
        } else {
            messageLabel.setBackground(new Color(135, 206, 250)); // Light blue for receiver
        }

        messageLabel.setOpaque(true);
        messageLabel.setBorder(new EmptyBorder(0, 15, 0, 50));
        panel.add(messageLabel);

        // Time label
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");

        JLabel time = new JLabel();
        time.setText(sdf.format(cal.getTime()));
        panel.add(time);

        return panel;
    }

    // Listen for incoming messages and display them
    public void run() {
        try {
            String msg = "";
            while (true) {
                msg = reader.readLine();
                if (msg.contains(name)) {
                    continue;  // Skip messages sent by self
                }

                JPanel panel = formatLabel(msg, false); // Format received message

                JPanel left = new JPanel(new BorderLayout());
                left.setBackground(Color.WHITE);
                left.add(panel, BorderLayout.LINE_START);
                vertical.add(left);

                a1.add(vertical, BorderLayout.PAGE_START);

                f.repaint();
                f.invalidate();
                f.validate();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Main function to run the client
    public static void main(String[] args) {
        Client1 one = new Client1();
        Thread t1 = new Thread(one);
        t1.start();
    }
}
